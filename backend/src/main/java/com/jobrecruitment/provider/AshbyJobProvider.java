package com.jobrecruitment.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobrecruitment.classifier.*;
import com.jobrecruitment.entity.*;
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
    private final FresherJobEligibilityService eligibilityService;
    private final FresherJobClassifier classifier;
    private final SkillRelevanceExtractor skillExtractor;
    private final HttpClient httpClient;

    public AshbyJobProvider(ObjectMapper objectMapper,
                            FresherJobEligibilityService eligibilityService,
                            FresherJobClassifier classifier,
                            SkillRelevanceExtractor skillExtractor) {
        this.objectMapper = objectMapper;
        this.eligibilityService = eligibilityService;
        this.classifier = classifier;
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
        job.setDepartment(rawJob.getDepartment());
        job.setEmploymentType(rawJob.getEmploymentType());
        job.setPostedAt(rawJob.getPostedAt() != null ? rawJob.getPostedAt() : LocalDateTime.now());
        job.setApplicationUrl(rawJob.getApplicationUrl());
        job.setSourceUrl(rawJob.getSourceUrl());
        job.setCurrency("INR");

        // Central Eligibility Decision (Strict India + Strict 0-Year Experience)
        FresherJobEligibilityService.FresherEligibilityDecision decision =
                eligibilityService.evaluateEligibility(rawJob.getTitle(), rawJob.getDescription(), rawJob.getLocation());

        // Location classification
        if (decision.location().classification() == LocationClassification.INDIA) {
            job.setCountry("INDIA");
            job.setCountryCode("IN");
            job.setCountryName("India");
        } else {
            job.setCountry(decision.location().country() != null ? decision.location().country().toUpperCase() : "UNKNOWN");
            job.setCountryCode(null);
            job.setCountryName(decision.location().country());
        }
        job.setState(decision.location().state());
        job.setCity(decision.location().city());
        job.setLocationClassification(decision.location().classification());
        job.setRemote(decision.location().isRemote());

        // Experience classification
        if (decision.experience() != null) {
            job.setEligibilityStatus(decision.experience().experienceClassification());
            job.setMinimumExperienceYears(decision.experience().minimumExperienceYears());
            job.setMaximumExperienceYears(decision.experience().maximumExperienceYears());
            job.setExperienceText(decision.experience().experienceText());
        } else {
            job.setEligibilityStatus(EligibilityStatus.UNKNOWN);
            job.setMinimumExperienceYears(null);
            job.setMaximumExperienceYears(null);
            job.setExperienceText(null);
        }

        job.setFresher(decision.isEligible());

        // Java relevance and categorization
        if (decision.relevance() != null) {
            job.setRoleCategory(decision.relevance().roleCategory());
            job.setTechnologyMatch(decision.relevance().technologyMatch());
            job.setRelevanceScore(decision.relevance().relevanceScore());
        } else {
            job.setRoleCategory(RoleCategory.OTHER);
            job.setTechnologyMatch("");
            job.setRelevanceScore(0);
        }

        // Classification
        FresherClassificationResult result = classifier.classify(rawJob.getTitle(), rawJob.getDescription());
        job.setExperienceLevel(decision.isEligible() ? ExperienceLevel.FRESHER : result.getExperienceLevel());
        job.setFresherConfidence(decision.isEligible() ? Math.max(result.getConfidence(), 50) : 0);

        job.setSkills(skillExtractor.extractSkills(rawJob.getTitle(), rawJob.getDescription()));
        job.setActive(decision.isEligible());
        job.setLastSeenAt(LocalDateTime.now());
        job.setLastSyncedAt(LocalDateTime.now());
        job.setLastVerifiedAt(LocalDateTime.now());

        return job;
    }
}
