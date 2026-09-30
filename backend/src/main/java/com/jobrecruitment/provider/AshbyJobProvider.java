package com.jobrecruitment.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobrecruitment.classifier.FresherClassificationResult;
import com.jobrecruitment.classifier.FresherEligibilityService;
import com.jobrecruitment.classifier.FresherJobClassifier;
import com.jobrecruitment.classifier.SkillRelevanceExtractor;
import com.jobrecruitment.entity.AggregatedJob;
import com.jobrecruitment.entity.CompanySource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class AshbyJobProvider implements JobSourceProvider {

    private static final Logger log = LoggerFactory.getLogger(AshbyJobProvider.class);
    private final ObjectMapper objectMapper;
    private final FresherJobClassifier classifier;
    private final FresherEligibilityService eligibilityService;
    private final SkillRelevanceExtractor skillExtractor;
    private final HttpClient httpClient;

    public AshbyJobProvider(ObjectMapper objectMapper,
                            FresherJobClassifier classifier,
                            FresherEligibilityService eligibilityService,
                            SkillRelevanceExtractor skillExtractor) {
        this.objectMapper = objectMapper;
        this.classifier = classifier;
        this.eligibilityService = eligibilityService;
        this.skillExtractor = skillExtractor;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    @Override
    public boolean supports(String providerName) {
        return "ASHBY".equalsIgnoreCase(providerName);
    }

    @Override
    public List<RawJobDto> fetchJobs(CompanySource source) throws Exception {
        String identifier = source.getProviderIdentifier();
        String url = "https://api.ashbyhq.com/posting-api/job-board/" + identifier;

        log.info("Fetching Ashby jobs from URL: {}", url);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "JobRecruitBot/1.0 (+https://jobrecruitment.com)")
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 429) {
            throw new RuntimeException("Rate limit (HTTP 429) exceeded for Ashby board: " + identifier);
        }

        if (response.statusCode() != 200) {
            throw new RuntimeException("Ashby API returned HTTP " + response.statusCode() + " for " + identifier);
        }

        JsonNode rootNode = objectMapper.readTree(response.body());
        JsonNode jobsArray = rootNode.get("jobs");

        List<RawJobDto> dtoList = new ArrayList<>();
        if (jobsArray != null && jobsArray.isArray()) {
            for (JsonNode node : jobsArray) {
                RawJobDto dto = new RawJobDto();
                String jobId = node.path("id").asText();
                dto.setExternalJobId(jobId);
                dto.setSourceJobId(jobId);
                dto.setSourceProvider("ASHBY");
                dto.setTitle(node.path("title").asText(""));

                String appUrl = node.path("applyUrl").asText(node.path("jobUrl").asText(""));
                dto.setApplicationUrl(appUrl);
                dto.setSourceUrl(node.path("jobUrl").asText(appUrl));

                String location = node.path("location").asText("Not Specified");
                dto.setLocation(location);
                dto.setRemote(node.path("isRemote").asBoolean(false) ||
                        location.toLowerCase().contains("remote") ||
                        node.path("title").asText("").toLowerCase().contains("remote"));

                if (location.toLowerCase().contains("india") ||
                    location.toLowerCase().contains("bengaluru") ||
                    location.toLowerCase().contains("bangalore") ||
                    location.toLowerCase().contains("hyderabad") ||
                    location.toLowerCase().contains("pune") ||
                    location.toLowerCase().contains("chennai") ||
                    location.toLowerCase().contains("mumbai") ||
                    location.toLowerCase().contains("noida") ||
                    location.toLowerCase().contains("gurgaon")) {
                    dto.setCountry("India");
                } else {
                    dto.setCountry(source.getCountry() != null ? source.getCountry() : "Global");
                }

                dto.setDepartment(node.path("department").asText("Engineering"));
                dto.setEmploymentType(node.path("employmentType").asText("Full Time"));

                String publishedAt = node.path("publishedAt").asText();
                if (publishedAt != null && !publishedAt.isEmpty()) {
                    try {
                        OffsetDateTime odt = OffsetDateTime.parse(publishedAt);
                        dto.setPostedAt(odt.toLocalDateTime());
                    } catch (Exception e) {
                        dto.setPostedAt(LocalDateTime.now());
                    }
                } else {
                    dto.setPostedAt(LocalDateTime.now());
                }

                dto.setDescription(node.path("descriptionPlain").asText(""));
                dtoList.add(dto);
            }
        }

        log.info("Successfully fetched {} raw jobs from Ashby ({})", dtoList.size(), identifier);
        return dtoList;
    }

    @Override
    public AggregatedJob normalize(RawJobDto rawJob, CompanySource source) {
        AggregatedJob job = new AggregatedJob();
        job.setExternalJobId(rawJob.getExternalJobId());
        job.setSourceJobId(rawJob.getSourceJobId());
        job.setSourceProvider("ASHBY");
        job.setCompanyId(source.getId());
        job.setCompanyName(source.getCompanyName());
        job.setTitle(rawJob.getTitle());
        job.setDescription(rawJob.getDescription());
        job.setLocation(rawJob.getLocation());
        job.setCountry(rawJob.getCountry());
        job.setDepartment(rawJob.getDepartment());
        job.setEmploymentType(rawJob.getEmploymentType());
        job.setPostedAt(rawJob.getPostedAt() != null ? rawJob.getPostedAt() : LocalDateTime.now());
        job.setApplicationUrl(rawJob.getApplicationUrl());
        job.setSourceUrl(rawJob.getSourceUrl());
        job.setRemote(rawJob.isRemote());
        job.setCurrency("INR");

        // Strict 0-year fresher eligibility check
        com.jobrecruitment.classifier.FresherEligibilityService.EligibilityResult eligibility = eligibilityService.determineEligibility(rawJob.getTitle(), rawJob.getDescription());
        job.setEligibilityStatus(eligibility.status());
        job.setMinimumExperienceYears(eligibility.minimumExperienceYears());
        boolean isZeroYear = eligibility.status() == com.jobrecruitment.entity.EligibilityStatus.ELIGIBLE_ZERO_YEAR;
        job.setFresher(isZeroYear);

        // Classification
        FresherClassificationResult result = classifier.classify(rawJob.getTitle(), rawJob.getDescription());
        job.setExperienceLevel(isZeroYear ? com.jobrecruitment.entity.ExperienceLevel.FRESHER : result.getExperienceLevel());
        job.setFresherConfidence(isZeroYear ? Math.max(result.getConfidence(), 30) : result.getConfidence());

        job.setSkills(skillExtractor.extractSkills(rawJob.getTitle(), rawJob.getDescription()));
        job.setActive(true);
        job.setLastVerifiedAt(LocalDateTime.now());

        return job;
    }
}
