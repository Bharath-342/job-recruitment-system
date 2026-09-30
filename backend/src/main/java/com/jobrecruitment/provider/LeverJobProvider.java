package com.jobrecruitment.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobrecruitment.classifier.FresherClassificationResult;
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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Component
public class LeverJobProvider implements JobSourceProvider {

    private static final Logger log = LoggerFactory.getLogger(LeverJobProvider.class);
    private final ObjectMapper objectMapper;
    private final FresherJobClassifier classifier;
    private final SkillRelevanceExtractor skillExtractor;
    private final HttpClient httpClient;

    public LeverJobProvider(ObjectMapper objectMapper,
                            FresherJobClassifier classifier,
                            SkillRelevanceExtractor skillExtractor) {
        this.objectMapper = objectMapper;
        this.classifier = classifier;
        this.skillExtractor = skillExtractor;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    @Override
    public boolean supports(String providerName) {
        return "LEVER".equalsIgnoreCase(providerName);
    }

    @Override
    public List<RawJobDto> fetchJobs(CompanySource source) throws Exception {
        String identifier = source.getProviderIdentifier();
        String url = "https://api.lever.co/v0/postings/" + identifier + "?mode=json";

        log.info("Fetching Lever jobs from URL: {}", url);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "JobRecruitBot/1.0 (+https://jobrecruitment.com)")
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 429) {
            throw new RuntimeException("Rate limit (HTTP 429) exceeded for Lever board: " + identifier);
        }

        if (response.statusCode() != 200) {
            throw new RuntimeException("Lever API returned HTTP " + response.statusCode() + " for " + identifier);
        }

        JsonNode rootNode = objectMapper.readTree(response.body());
        List<RawJobDto> dtoList = new ArrayList<>();

        if (rootNode != null && rootNode.isArray()) {
            for (JsonNode node : rootNode) {
                RawJobDto dto = new RawJobDto();
                String jobId = node.path("id").asText();
                dto.setExternalJobId(jobId);
                dto.setSourceJobId(jobId);
                dto.setSourceProvider("LEVER");
                dto.setTitle(node.path("text").asText(""));

                String appUrl = node.path("hostedUrl").asText("");
                dto.setApplicationUrl(appUrl);
                dto.setSourceUrl(appUrl);

                JsonNode categories = node.path("categories");
                String location = categories.path("location").asText("Not Specified");
                dto.setLocation(location);

                boolean isRemote = location.toLowerCase().contains("remote") ||
                        node.path("text").asText("").toLowerCase().contains("remote");
                dto.setRemote(isRemote);

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

                dto.setDepartment(categories.path("department").asText("Engineering"));
                dto.setEmploymentType(categories.path("commitment").asText("Full Time"));

                long createdAtMillis = node.path("createdAt").asLong(0);
                if (createdAtMillis > 0) {
                    dto.setPostedAt(Instant.ofEpochMilli(createdAtMillis).atZone(ZoneId.systemDefault()).toLocalDateTime());
                } else {
                    dto.setPostedAt(LocalDateTime.now());
                }

                String desc = node.path("descriptionPlain").asText("") + "\n" + node.path("additionalPlain").asText("");
                dto.setDescription(desc.trim());

                dtoList.add(dto);
            }
        }

        log.info("Successfully fetched {} raw jobs from Lever ({})", dtoList.size(), identifier);
        return dtoList;
    }

    @Override
    public AggregatedJob normalize(RawJobDto rawJob, CompanySource source) {
        AggregatedJob job = new AggregatedJob();
        job.setExternalJobId(rawJob.getExternalJobId());
        job.setSourceJobId(rawJob.getSourceJobId());
        job.setSourceProvider("LEVER");
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

        FresherClassificationResult result = classifier.classify(rawJob.getTitle(), rawJob.getDescription());
        job.setExperienceLevel(result.getExperienceLevel());
        job.setFresher(result.isFresher());
        job.setFresherConfidence(result.getConfidence());

        job.setSkills(skillExtractor.extractSkills(rawJob.getTitle(), rawJob.getDescription()));
        job.setActive(true);
        job.setLastVerifiedAt(LocalDateTime.now());

        return job;
    }
}
