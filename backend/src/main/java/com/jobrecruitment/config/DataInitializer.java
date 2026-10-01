package com.jobrecruitment.config;

import com.jobrecruitment.classifier.ExperienceRequirementParser;
import com.jobrecruitment.classifier.JobLocationParser;
import com.jobrecruitment.entity.*;
import com.jobrecruitment.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final CandidateProfileRepository candidateProfileRepository;
    private final RecruiterProfileRepository recruiterProfileRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final SkillRepository skillRepository;
    private final PasswordEncoder passwordEncoder;
    private final CompanySourceRepository companySourceRepository;
    private final AggregatedJobRepository aggregatedJobRepository;
    private final JobLocationParser locationParser;
    private final ExperienceRequirementParser experienceParser;
    private final com.jobrecruitment.classifier.FresherJobEligibilityService fresherJobEligibilityService;
    private final com.jobrecruitment.classifier.JavaRelevanceClassifier javaRelevanceClassifier;
    private final com.jobrecruitment.service.JobAggregationService jobAggregationService;

    public DataInitializer(UserRepository userRepository,
                           CandidateProfileRepository candidateProfileRepository,
                           RecruiterProfileRepository recruiterProfileRepository,
                           JobRepository jobRepository,
                           ApplicationRepository applicationRepository,
                           SkillRepository skillRepository,
                           PasswordEncoder passwordEncoder,
                           CompanySourceRepository companySourceRepository,
                           AggregatedJobRepository aggregatedJobRepository,
                           JobLocationParser locationParser,
                           ExperienceRequirementParser experienceParser,
                           com.jobrecruitment.classifier.FresherJobEligibilityService fresherJobEligibilityService,
                           com.jobrecruitment.classifier.JavaRelevanceClassifier javaRelevanceClassifier,
                           com.jobrecruitment.service.JobAggregationService jobAggregationService) {
        this.userRepository = userRepository;
        this.candidateProfileRepository = candidateProfileRepository;
        this.recruiterProfileRepository = recruiterProfileRepository;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.skillRepository = skillRepository;
        this.passwordEncoder = passwordEncoder;
        this.companySourceRepository = companySourceRepository;
        this.aggregatedJobRepository = aggregatedJobRepository;
        this.locationParser = locationParser;
        this.experienceParser = experienceParser;
        this.fresherJobEligibilityService = fresherJobEligibilityService;
        this.javaRelevanceClassifier = javaRelevanceClassifier;
        this.jobAggregationService = jobAggregationService;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // 1. Create default admin
        if (userRepository.findByEmail("admin@jobrecruitment.com").isEmpty()) {
            User admin = new User();
            admin.setFirstName("System");
            admin.setLastName("Admin");
            admin.setEmail("admin@jobrecruitment.com");
            admin.setPassword(passwordEncoder.encode("Admin@123"));
            admin.setRole(Role.ADMIN);
            admin.setActive(true);
            userRepository.save(admin);
        }

        // 2. Create common skills if empty
        Skill javaSkill = getOrCreateSkill("Java");
        Skill springSkill = getOrCreateSkill("Spring Boot");
        Skill reactSkill = getOrCreateSkill("React");
        Skill mysqlSkill = getOrCreateSkill("MySQL");
        Skill dockerSkill = getOrCreateSkill("Docker");
        Skill kubernetesSkill = getOrCreateSkill("Kubernetes");
        Skill awsSkill = getOrCreateSkill("AWS");
        Skill jsSkill = getOrCreateSkill("JavaScript");
        Skill cssSkill = getOrCreateSkill("CSS");
        Skill restSkill = getOrCreateSkill("REST APIs");
        Skill gitSkill = getOrCreateSkill("Git");

        // 3. Create demo recruiter if not exists
        User recruiterUser;
        if (userRepository.findByEmail("recruiter@techcorp.com").isEmpty()) {
            recruiterUser = new User();
            recruiterUser.setFirstName("Sarah");
            recruiterUser.setLastName("Connor");
            recruiterUser.setEmail("recruiter@techcorp.com");
            recruiterUser.setPassword(passwordEncoder.encode("Recruiter@123"));
            recruiterUser.setRole(Role.RECRUITER);
            recruiterUser.setActive(true);
            recruiterUser = userRepository.save(recruiterUser);

            RecruiterProfile recProfile = new RecruiterProfile();
            recProfile.setUser(recruiterUser);
            recProfile.setCompanyName("TechCorp Global");
            recProfile.setDesignation("Lead Technical Talent Partner");
            recProfile.setPhone("+91 9876543210");
            recProfile.setLocation("Hyderabad, Telangana, India");
            recProfile.setCompanyWebsite("https://techcorp.example.com");
            recProfile.setCompanyDescription("TechCorp Global is a premier engineering solutions company specializing in distributed cloud platforms and enterprise web applications.");
            recruiterProfileRepository.save(recProfile);
        } else {
            recruiterUser = userRepository.findByEmail("recruiter@techcorp.com").get();
        }

        // 4. Create demo candidate if not exists
        User candidateUser;
        if (userRepository.findByEmail("candidate@dev.com").isEmpty()) {
            candidateUser = new User();
            candidateUser.setFirstName("Alex");
            candidateUser.setLastName("Mercer");
            candidateUser.setEmail("candidate@dev.com");
            candidateUser.setPassword(passwordEncoder.encode("Candidate@123"));
            candidateUser.setRole(Role.CANDIDATE);
            candidateUser.setActive(true);
            candidateUser = userRepository.save(candidateUser);

            CandidateProfile candProfile = new CandidateProfile();
            candProfile.setUser(candidateUser);
            candProfile.setHeadline("Full Stack Java & React Engineer");
            candProfile.setSummary("Passionate full stack developer with 3 years experience building scalable backend services with Java & Spring Boot, paired with modern responsive React frontends.");
            candProfile.setPhone("+91 9123456780");
            candProfile.setLocation("Hyderabad, India");
            candProfile.setExperienceYears(3);
            candProfile.setLinkedinUrl("https://linkedin.com/in/alex-mercer-dev");
            candProfile.setGithubUrl("https://github.com/alex-mercer-dev");
            candProfile.setResumeFileName("Alex_Mercer_Resume.pdf");

            Set<Skill> candidateSkills = new HashSet<>(List.of(javaSkill, springSkill, reactSkill, mysqlSkill, dockerSkill, restSkill, gitSkill));
            candProfile.setSkills(candidateSkills);
            candidateProfileRepository.save(candProfile);
        } else {
            candidateUser = userRepository.findByEmail("candidate@dev.com").get();
        }

        // 5. Seed sample recruiter jobs if no jobs exist
        if (jobRepository.count() == 0) {
            Job job1 = new Job();
            job1.setTitle("Senior Java Full Stack Developer");
            job1.setDescription("We are looking for a Senior Java Full Stack Developer to design, develop, and maintain high-performance microservices and interactive user interfaces using Spring Boot and React.\n\nKey Responsibilities:\n- Build secure REST APIs with Spring Security and JWT\n- Design normalized relational databases with MySQL and Hibernate\n- Create responsive web applications with React\n- Implement unit and integration tests with JUnit and Mockito.");
            job1.setLocation("Hyderabad, India");
            job1.setEmploymentType(EmploymentType.FULL_TIME);
            job1.setExperienceMin(3);
            job1.setExperienceMax(6);
            job1.setSalaryMin(new BigDecimal("1500000"));
            job1.setSalaryMax(new BigDecimal("2400000"));
            job1.setStatus(JobStatus.OPEN);
            job1.setCompanyName("TechCorp Global");
            job1.setRecruiter(recruiterUser);
            job1.setRequiredSkills(new HashSet<>(List.of(javaSkill, springSkill, reactSkill, mysqlSkill, restSkill)));
            job1.setDeadline(LocalDateTime.now().plusDays(30));
            job1 = jobRepository.save(job1);

            Job job2 = new Job();
            job2.setTitle("Backend Engineer - Spring Boot & Cloud");
            job2.setDescription("Join our core platform engineering team to build scalable microservices. You will work on distributed data processing, automated cloud pipelines, and robust database architecture.\n\nQualifications:\n- Strong knowledge of Java 21, Spring Boot, Spring Data JPA\n- Experience with Docker, Kubernetes, and Cloud native deployments\n- Proficient in SQL performance tuning and clean code architecture.");
            job2.setLocation("Bengaluru, India");
            job2.setEmploymentType(EmploymentType.FULL_TIME);
            job2.setExperienceMin(2);
            job2.setExperienceMax(5);
            job2.setSalaryMin(new BigDecimal("1200000"));
            job2.setSalaryMax(new BigDecimal("2000000"));
            job2.setStatus(JobStatus.OPEN);
            job2.setCompanyName("TechCorp Global");
            job2.setRecruiter(recruiterUser);
            job2.setRequiredSkills(new HashSet<>(List.of(javaSkill, springSkill, mysqlSkill, dockerSkill, awsSkill)));
            job2.setDeadline(LocalDateTime.now().plusDays(45));
            jobRepository.save(job2);

            Job job3 = new Job();
            job3.setTitle("Frontend Developer - React & Modern UI");
            job3.setDescription("Seeking a talented React Developer passionate about user experience, responsive web design, and high performance client-side applications.\n\nRequirements:\n- Strong JavaScript ES6+, React Hooks, and State Management\n- Experience with Bootstrap or Tailwind CSS and responsive layouts\n- Integration with RESTful backends and Axios.");
            job3.setLocation("Remote");
            job3.setEmploymentType(EmploymentType.FULL_TIME);
            job3.setExperienceMin(1);
            job3.setExperienceMax(4);
            job3.setSalaryMin(new BigDecimal("800000"));
            job3.setSalaryMax(new BigDecimal("1500000"));
            job3.setStatus(JobStatus.OPEN);
            job3.setCompanyName("TechCorp Global");
            job3.setRecruiter(recruiterUser);
            job3.setRequiredSkills(new HashSet<>(List.of(reactSkill, jsSkill, cssSkill, restSkill, gitSkill)));
            job3.setDeadline(LocalDateTime.now().plusDays(25));
            jobRepository.save(job3);

            Job job4 = new Job();
            job4.setTitle("DevOps & Site Reliability Engineer");
            job4.setDescription("We need a DevOps / SRE specialist to maintain automated CI/CD pipelines, container orchestration, and multi-region infrastructure reliability.\n\nRequirements:\n- Hands-on experience with Docker, Kubernetes, Linux, and Bash\n- Cloud infrastructure on AWS or GCP\n- Infrastructure as Code and monitoring.");
            job4.setLocation("Pune, India");
            job4.setEmploymentType(EmploymentType.HYBRID);
            job4.setExperienceMin(3);
            job4.setExperienceMax(7);
            job4.setSalaryMin(new BigDecimal("1600000"));
            job4.setSalaryMax(new BigDecimal("2600000"));
            job4.setStatus(JobStatus.OPEN);
            job4.setCompanyName("TechCorp Global");
            job4.setRecruiter(recruiterUser);
            job4.setRequiredSkills(new HashSet<>(List.of(dockerSkill, kubernetesSkill, awsSkill, gitSkill)));
            job4.setDeadline(LocalDateTime.now().plusDays(60));
            jobRepository.save(job4);

            // 6. Seed demo application for candidate on job 1
            if (applicationRepository.count() == 0) {
                Application app = new Application();
                app.setJob(job1);
                app.setCandidate(candidateUser);
                app.setCoverLetter("I am excited to apply for the Senior Java Full Stack Developer position at TechCorp Global. With 3 years of hands-on experience in Java, Spring Boot, and React, I have successfully delivered high-throughput REST APIs and sleek frontend interfaces.");
                app.setStatus(ApplicationStatus.UNDER_REVIEW);
                app.setRecruiterNotes("Strong Java and React background. Reviewed GitHub portfolio.");
                applicationRepository.save(app);
            }
        }

        // 7. Seed curated verified company sources with active India hiring
        List<CompanySource> defaultSources = List.of(
            new CompanySource("Rubrik", "https://www.rubrik.com/company/careers", "GREENHOUSE", "rubrik", "India"),
            new CompanySource("Tower Research Capital", "https://www.tower-research.com/careers", "GREENHOUSE", "towerresearchcapital", "India"),
            new CompanySource("Point72", "https://point72.com/careers", "GREENHOUSE", "point72", "India"),
            new CompanySource("IMC Trading", "https://careers.imc.com", "GREENHOUSE", "imc", "India"),
            new CompanySource("WorldQuant", "https://www.worldquant.com/careers", "GREENHOUSE", "worldquant", "India"),
            new CompanySource("Groww", "https://groww.in/careers", "GREENHOUSE", "groww", "India"),
            new CompanySource("InMobi", "https://www.inmobi.com/company/careers", "GREENHOUSE", "inmobi", "India"),
            new CompanySource("MongoDB", "https://www.mongodb.com/careers", "GREENHOUSE", "mongodb", "India"),
            new CompanySource("ThoughtWorks", "https://www.thoughtworks.com/careers", "GREENHOUSE", "thoughtworks", "India"),
            new CompanySource("HackerRank", "https://www.hackerrank.com/careers", "GREENHOUSE", "hackerrank", "India"),
            new CompanySource("Elastic", "https://jobs.elastic.co", "GREENHOUSE", "elastic", "India"),
            new CompanySource("Stripe", "https://stripe.com/jobs", "GREENHOUSE", "stripe", "India"),
            new CompanySource("Datadog", "https://careers.datadoghq.com", "GREENHOUSE", "datadog", "India"),
            new CompanySource("Twilio", "https://www.twilio.com/company/jobs", "GREENHOUSE", "twilio", "India"),
            new CompanySource("Cloudflare", "https://www.cloudflare.com/careers", "GREENHOUSE", "cloudflare", "India"),
            new CompanySource("New Relic", "https://newrelic.com/about/careers", "GREENHOUSE", "newrelic", "India"),
            new CompanySource("Sumo Logic", "https://www.sumologic.com/company/careers", "GREENHOUSE", "sumologic", "India"),
            new CompanySource("LaunchDarkly", "https://launchdarkly.com/careers", "GREENHOUSE", "launchdarkly", "India"),
            new CompanySource("Groupon", "https://www.grouponcareers.com", "GREENHOUSE", "groupon", "India"),
            new CompanySource("Zscaler", "https://www.zscaler.com/careers", "GREENHOUSE", "zscaler", "India"),
            new CompanySource("Pure Storage", "https://www.purestorage.com/company/careers", "GREENHOUSE", "purestorage", "India"),
            new CompanySource("Netskope", "https://www.netskope.com/company/careers", "GREENHOUSE", "netskope", "India"),
            new CompanySource("SonicWall", "https://www.sonicwall.com/careers", "GREENHOUSE", "sonicwall", "India"),
            new CompanySource("Commvault", "https://careers.commvault.com", "GREENHOUSE", "commvault", "India"),
            new CompanySource("Druva", "https://www.druva.com/about/careers", "GREENHOUSE", "druva", "India"),
            new CompanySource("BitGo", "https://www.bitgo.com/careers", "GREENHOUSE", "bitgo", "India"),
            new CompanySource("SmartBear", "https://smartbear.com/company/careers", "GREENHOUSE", "smartbear", "India"),
            new CompanySource("Sauce Labs", "https://saucelabs.com/careers", "GREENHOUSE", "saucelabs", "India"),
            new CompanySource("Porter", "https://porter.in/careers", "GREENHOUSE", "porter", "India"),
            new CompanySource("CRED", "https://cred.club/careers", "LEVER", "cred", "India"),
            new CompanySource("Paytm", "https://paytm.com/careers", "LEVER", "paytm", "India"),
            new CompanySource("FamPay", "https://fampay.in/careers", "LEVER", "fampay", "India"),
            new CompanySource("Agoda", "https://careersatagoda.com", "GREENHOUSE", "agoda", "India"),
            new CompanySource("Airbnb", "https://careers.airbnb.com", "GREENHOUSE", "airbnb", "India"),
            new CompanySource("Adyen", "https://careers.adyen.com", "GREENHOUSE", "adyen", "India"),
            new CompanySource("Workato", "https://www.workato.com/careers", "GREENHOUSE", "workato", "India"),
            new CompanySource("Yugabyte", "https://www.yugabyte.com/careers", "GREENHOUSE", "yugabyte", "India"),
            new CompanySource("Starburst", "https://www.starburst.io/careers", "GREENHOUSE", "starburst", "India"),
            new CompanySource("Atlan", "https://atlan.com/careers", "ASHBY", "atlan", "India"),
            new CompanySource("Navi", "https://navi.com/careers", "ASHBY", "navi", "India"),
            new CompanySource("Deliveroo", "https://careers.deliveroo.co.uk", "GREENHOUSE", "deliveroo", "India")
        );

        for (CompanySource src : defaultSources) {
            Optional<CompanySource> existingSrc = companySourceRepository.findByProviderAndProviderIdentifier(src.getProvider(), src.getProviderIdentifier());
            if (existingSrc.isEmpty()) {
                companySourceRepository.save(src);
            } else {
                CompanySource s = existingSrc.get();
                s.setEnabled(true);
                companySourceRepository.save(s);
            }
        }

        // 8. Disable foreign-only or inaccessible sources
        List<String> foreignIdentifiers = List.of(
            "gitlab", "coinbase", "samsara", "databricks", "okta",
            "pinterest", "brex", "reddit", "affirm", "discord", "miro",
            "hashicorp", "palantir", "spotify", "netflix", "canva",
            "sentry", "ramp", "linear", "figma", "toasttab", "canonical"
        );
        for (String fid : foreignIdentifiers) {
            companySourceRepository.findAll().stream()
                .filter(s -> fid.equalsIgnoreCase(s.getProviderIdentifier()))
                .forEach(s -> {
                    s.setEnabled(false);
                    companySourceRepository.save(s);
                });
        }

        // 9. Retroactive Audit & Rectification on existing aggregated jobs in database (Section 13)
        // Re-evaluates all historical jobs with the strict parsers to ensure ZERO foreign, experienced, or non-IT jobs remain active.
        log.info("Running retroactive audit and rectification on existing aggregated jobs (Section 13)...");
        List<AggregatedJob> allExisting = aggregatedJobRepository.findAll();
        int rectifiedCount = 0;
        int activeFreshersCount = 0;
        for (AggregatedJob job : allExisting) {
            JobLocationParser.ParsedLocation loc = locationParser.parse(job.getLocation(), job.getTitle(), job.getDescription());
            ExperienceRequirementParser.ParsedExperience exp = experienceParser.parse(job.getTitle(), job.getDescription());
            com.jobrecruitment.classifier.JavaRelevanceClassifier.JavaRelevanceResult javaClf =
                    javaRelevanceClassifier.classify(job.getTitle(), job.getDescription());

            if (loc.classification() == LocationClassification.INDIA) {
                job.setCountry("INDIA");
                job.setCountryCode("IN");
                job.setCountryName("India");
            } else {
                job.setCountry(loc.country() != null ? loc.country().toUpperCase() : "UNKNOWN");
                job.setCountryCode(null);
                job.setCountryName(loc.country());
            }

            job.setState(loc.state());
            job.setCity(loc.city());
            job.setLocationClassification(loc.classification());
            job.setRemote(loc.isRemote());

            job.setEligibilityStatus(exp.experienceClassification());
            job.setMinimumExperienceYears(exp.minimumExperienceYears());
            job.setMaximumExperienceYears(exp.maximumExperienceYears());
            job.setExperienceText(exp.experienceText());

            job.setRoleCategory(javaClf.roleCategory());
            job.setTechnologyMatch(javaClf.technologyMatch());
            job.setRelevanceScore(javaClf.relevanceScore());

            boolean isIndia = loc.classification() == LocationClassification.INDIA && "INDIA".equalsIgnoreCase(job.getCountry());
            boolean isZeroYear = exp.experienceClassification() == EligibilityStatus.ELIGIBLE_ZERO_YEAR &&
                                 (exp.minimumExperienceYears() == null || exp.minimumExperienceYears() == 0);
            boolean isItRole = javaClf.roleCategory() != com.jobrecruitment.entity.RoleCategory.OTHER;

            // Deactivate and remove from fresher dataset any job that fails Section 12/13/IT requirement
            if (!isIndia || !isZeroYear || !isItRole) {
                job.setFresher(false);
                job.setActive(false);
                rectifiedCount++;
            } else {
                job.setFresher(true);
                job.setActive(true);
                activeFreshersCount++;
            }
            aggregatedJobRepository.save(job);
        }
        log.info("Completed retroactive audit (Section 13). Inspected {} jobs, deactivated/rectified {}, active verified Indian IT fresher jobs {}.",
                allExisting.size(), rectifiedCount, activeFreshersCount);

        // 10. Background synchronization after startup stabilization (runs after 15s)
        new Thread(() -> {
            try {
                Thread.sleep(15_000);
                log.info("Starting initial job synchronization after startup delay...");
                jobAggregationService.syncAllSources();
            } catch (Exception e) {
                log.warn("Initial background sync failed or was interrupted: {}", e.getMessage());
            }
        }, "initial-sync-thread").start();
    }

    private Skill getOrCreateSkill(String name) {
        return skillRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> skillRepository.save(new Skill(name)));
    }
}
