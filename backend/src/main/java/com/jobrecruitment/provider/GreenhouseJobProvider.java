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
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class GreenhouseJobProvider implements JobSourceProvider {

    private static final Logger log = LoggerFactory.getLogger(GreenhouseJobProvider.class);
    private final ObjectMapper objectMapper;
    private final FresherJobClassifier classifier;
    private final SkillRelevanceExtractor skillExtractor;
    private final HttpClient httpClient;

    public GreenhouseJobProvider(ObjectMapper objectMapper,
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
        return "GREENHOUSE".equalsIgnoreCase(providerName);
    }

    @Override
    public List<RawJobDto> fetchJobs(CompanySource source) throws Exception {
        String identifier = source.getProviderIdentifier();
        String url = "https://boards-api.greenhouse.io/v1/boards/" + identifier + "/jobs?content=true";

        log.info("Fetching Greenhouse jobs from URL: {}", url);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "JobRecruitBot/1.0 (+https://jobrecruitment.com)")
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 429) {
            throw new RuntimeException("Rate limit (HTTP 429) exceeded for Greenhouse board: " + identifier);
        }

        if (response.statusCode() != 200) {
            throw new RuntimeException("Greenhouse API returned HTTP " + response.statusCode() + " for " + identifier);
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
                dto.setSourceProvider("GREENHOUSE");
                dto.setTitle(node.path("title").asText(""));

                String appUrl = node.path("absolute_url").asText("");
                dto.setApplicationUrl(appUrl);
                dto.setSourceUrl(appUrl);

                String location = node.path("location").path("name").asText("Not Specified");
                dto.setLocation(location);

                boolean isRemote = location.toLowerCase().contains("remote") ||
                        node.path("title").asText("").toLowerCase().contains("remote");
                dto.setRemote(isRemote);

                if (location.toLowerCase().contains("india") ||
                    location.toLowerCase().contains("bengaluru") ||
                    location.toLowerCase().contains("bangalore") ||
                    location.toLowerCase().contains("hyderabad") ||
                    location.toLowerCase().contains("pune") ||
                    location.toLowerCase().contains("chennai") ||
                    location.toLowerCase().contains("mumbai") ||
                    location.toLowerCase().contains("noida") ||
                    location.toLowerCase().contains("gurgaon") ||
                    location.toLowerCase().contains("gurugram")) {
                    dto.setCountry("India");
                } else {
                    dto.setCountry(source.getCountry() != null ? source.getCountry() : "Global");
                }

                // Department
                JsonNode depts = node.path("departments");
                if (depts.isArray() && !depts.isEmpty()) {
                    dto.setDepartment(depts.get(0).path("name").asText("Engineering"));
                } else {
                    dto.setDepartment("Engineering");
                }

                // Updated at / posted at
                String updatedAt = node.path("updated_at").asText();
                if (updatedAt != null && !updatedAt.isEmpty()) {
                    try {
                        OffsetDateTime odt = OffsetDateTime.parse(updatedAt);
                        dto.setPostedAt(odt.toLocalDateTime());
                    } catch (Exception e) {
                        dto.setPostedAt(LocalDateTime.now());
                    }
                } else {
                    dto.setPostedAt(LocalDateTime.now());
                }

                // Content (strip HTML)
                String rawContent = node.path("content").asText("");
                String cleanContent = cleanHtml(rawContent);
                dto.setDescription(cleanContent);
                dto.setEmploymentType("FULL_TIME");

                dtoList.add(dto);
            }
        }

        log.info("Successfully fetched {} raw jobs from Greenhouse ({})", dtoList.size(), identifier);
        return dtoList;
    }

    @Override
    public AggregatedJob normalize(RawJobDto rawJob, CompanySource source) {
        AggregatedJob job = new AggregatedJob();
        job.setExternalJobId(rawJob.getExternalJobId());
        job.setSourceJobId(rawJob.getSourceJobId());
        job.setSourceProvider("GREENHOUSE");
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

        // Fresher classification
        FresherClassificationResult result = classifier.classify(rawJob.getTitle(), rawJob.getDescription());
        job.setExperienceLevel(result.getExperienceLevel());
        job.setFresher(result.isFresher());
        job.setFresherConfidence(result.getConfidence());

        // Skills
        job.setSkills(skillExtractor.extractSkills(rawJob.getTitle(), rawJob.getDescription()));
        job.setActive(true);
        job.setLastVerifiedAt(LocalDateTime.now());

        return job;
    }

    private String cleanHtml(String html) {
        if (html == null || html.isEmpty()) return "";
        return html.replaceAll("<[^>]*>", " ")
                .replaceAll("&nbsp;", " ")
                .replaceAll("&amp;", "&")
                .replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">")
                .replaceAll("&quot;", "\"")
                .replaceAll("\\s{2,}", " ")
                .trim();
    }
}
