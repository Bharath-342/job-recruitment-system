package com.jobrecruitment.classifier;

import com.jobrecruitment.entity.RoleCategory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Classifier to evaluate IT/Software fresher relevance with primary focus on Java Full Stack.
 * Implements Sections 1, 2, and 3:
 * - Strictly rejects non-IT roles (HR, Finance, Sales, Marketing, Content, BPO, etc.)
 * - Categorizes IT roles into JAVA_FULL_STACK, JAVA_BACKEND, JAVA_DEVELOPMENT, SOFTWARE_ENGINEERING, QA_AUTOMATION, IT_SUPPORT, etc.
 * - Extracts technology matches (Java, Spring Boot, React, SQL, etc.)
 * - Computes a relevance score (0-100) prioritizing Java Full Stack and Java Backend.
 */
@Component
public class JavaRelevanceClassifier {

    public record JavaRelevanceResult(
            boolean isItSoftware,
            RoleCategory roleCategory,
            String technologyMatch,
            int relevanceScore,
            String details
    ) {}

    // 1. NON-IT ROLE EXCLUSION PATTERN (Hard rejection per Section 2)
    private static final Pattern NON_IT_TITLE_PATTERN = Pattern.compile(
            "\\b(human resources|hr\\b|talent acquisition|recruiter|recruitment|people team|hiring coordinator|" +
            "accounts payable|accounts receivable|payroll|accountant|accounting|finance|financial analyst|audit|tax|" +
            "sales\\b|sales executive|business development|tele caller|telecaller|b2b sales|sales associate|" +
            "marketing|copywriter|content writer|content creator|video editor|social media|creative & communications|pr executive|" +
            "customer service|call center|bpo|hotel operations|operations manager|relationship manager|portfolio specialist|" +
            "mechanical|civil engineer|electrical engineer|chemical engineer|nurse|healthcare|medical|doctor|legal counsel|lawyer)\\b",
            Pattern.CASE_INSENSITIVE);

    // 2. IT / SOFTWARE TITLE PATTERNS
    private static final Pattern IT_SOFTWARE_TITLE_PATTERN = Pattern.compile(
            "\\b(software|developer|engineer|programmer|coder|full[ -]?stack|backend|frontend|java|python|c\\+\\+|golang|react|node|" +
            "data engineer|data analyst|qa\\b|sdet|test engineer|automation engineer|qa automation|system engineer|systems engineer|" +
            "application developer|application engineer|technology analyst|graduate engineer trainee|software trainee|developer trainee|" +
            "junior developer|associate developer|associate engineer|associate software engineer|technical support|application support)\\b",
            Pattern.CASE_INSENSITIVE);

    // 3. TECHNOLOGY DETECTION RULES
    private record TechRule(String name, Pattern pattern, int weight) {}

    private static final List<TechRule> TECH_RULES = List.of(
            new TechRule("Java", Pattern.compile("\\b(?:Java|Core Java|Java 8\\+|Java 11\\+|Java 17\\+|Java 21\\+|J2EE|JDBC)\\b", Pattern.CASE_INSENSITIVE), 30),
            new TechRule("Spring Boot", Pattern.compile("\\bSpring Boot\\b", Pattern.CASE_INSENSITIVE), 25),
            new TechRule("Spring", Pattern.compile("\\bSpring(?: MVC| Data| Security| Framework)?\\b", Pattern.CASE_INSENSITIVE), 15),
            new TechRule("Hibernate/JPA", Pattern.compile("\\b(?:Hibernate|JPA)\\b", Pattern.CASE_INSENSITIVE), 15),
            new TechRule("REST API", Pattern.compile("\\bREST(?:ful)?(?: API)?\\b", Pattern.CASE_INSENSITIVE), 10),
            new TechRule("Microservices", Pattern.compile("\\bMicroservices?\\b", Pattern.CASE_INSENSITIVE), 15),
            new TechRule("React", Pattern.compile("\\bReact(?:\\.js|js)?\\b", Pattern.CASE_INSENSITIVE), 15),
            new TechRule("Angular", Pattern.compile("\\bAngular(?:\\.js|js)?\\b", Pattern.CASE_INSENSITIVE), 15),
            new TechRule("JavaScript/TypeScript", Pattern.compile("\\b(?:JavaScript|TypeScript|JS|TS)\\b", Pattern.CASE_INSENSITIVE), 10),
            new TechRule("SQL/Database", Pattern.compile("\\b(?:SQL|MySQL|PostgreSQL|Postgres|Oracle DB|RDBMS)\\b", Pattern.CASE_INSENSITIVE), 10),
            new TechRule("HTML/CSS", Pattern.compile("\\b(?:HTML|HTML5|CSS|CSS3)\\b", Pattern.CASE_INSENSITIVE), 5),
            new TechRule("Docker/K8s", Pattern.compile("\\b(?:Docker|Kubernetes|K8s)\\b", Pattern.CASE_INSENSITIVE), 5),
            new TechRule("Git", Pattern.compile("\\b(?:Git|GitHub|GitLab)\\b", Pattern.CASE_INSENSITIVE), 5),
            new TechRule("QA Automation", Pattern.compile("\\b(?:Selenium|Cypress|Playwright|JUnit|TestNG|Automation Testing)\\b", Pattern.CASE_INSENSITIVE), 15)
    );

    public JavaRelevanceResult classify(String title, String description) {
        String safeTitle = title != null ? title.trim() : "";
        String safeDesc = description != null ? description.trim() : "";
        String fullText = safeTitle + " \n " + safeDesc;
        String lowerTitle = safeTitle.toLowerCase();

        // 1. HARD NON-IT CHECK: Exclude pure Non-IT roles
        if (NON_IT_TITLE_PATTERN.matcher(lowerTitle).find()) {
            return new JavaRelevanceResult(
                    false,
                    RoleCategory.OTHER,
                    "",
                    0,
                    "Rejected Non-IT role: " + safeTitle
            );
        }

        // Verify it is genuinely an IT / Software role
        boolean isItTitle = IT_SOFTWARE_TITLE_PATTERN.matcher(lowerTitle).find();
        boolean hasTechKeyword = Pattern.compile("\\b(java|spring|software|developer|coding|programming|algorithms?|python|frontend|backend)\\b", Pattern.CASE_INSENSITIVE).matcher(fullText).find();

        if (!isItTitle && !hasTechKeyword) {
            return new JavaRelevanceResult(
                    false,
                    RoleCategory.OTHER,
                    "",
                    0,
                    "Ambiguous or non-software job: " + safeTitle
            );
        }

        // 2. DETECT TECHNOLOGIES
        List<String> matchedTech = new ArrayList<>();
        boolean hasJava = false;
        boolean hasSpring = false;
        boolean hasFrontend = false;
        boolean hasBackend = false;
        boolean hasFullStackKeyword = Pattern.compile("\\bfull[ -]?stack\\b", Pattern.CASE_INSENSITIVE).matcher(fullText).find();
        boolean hasQaKeyword = Pattern.compile("\\b(?:qa|tester|test engineer|sdet|automation testing)\\b", Pattern.CASE_INSENSITIVE).matcher(fullText).find();
        boolean hasSupportKeyword = Pattern.compile("\\b(?:application support|technical support|it support|operations support)\\b", Pattern.CASE_INSENSITIVE).matcher(fullText).find();

        for (TechRule rule : TECH_RULES) {
            if (rule.pattern().matcher(fullText).find()) {
                matchedTech.add(rule.name());
                if (rule.name().equals("Java")) hasJava = true;
                if (rule.name().contains("Spring") || rule.name().contains("Hibernate")) hasSpring = true;
                if (rule.name().equals("React") || rule.name().equals("Angular") || rule.name().equals("HTML/CSS") || rule.name().equals("JavaScript/TypeScript")) hasFrontend = true;
                if (rule.name().equals("Microservices") || rule.name().equals("REST API") || rule.name().contains("SQL")) hasBackend = true;
            }
        }

        String techMatchString = matchedTech.isEmpty() ? "Software Engineering, Problem Solving" : String.join(", ", matchedTech);

        // 3. ROLE CATEGORY & RELEVANCE SCORING
        RoleCategory category;
        int score = 0;

        boolean titleHasQa = lowerTitle.contains("qa") || lowerTitle.contains("test") || lowerTitle.contains("sdet") || lowerTitle.contains("automation");
        boolean titleHasSupport = lowerTitle.contains("support");

        if (titleHasQa && !lowerTitle.contains("developer") && !lowerTitle.contains("software engineer")) {
            category = RoleCategory.QA_AUTOMATION;
            score = hasJava ? 60 : 45;
        } else if (titleHasSupport && !lowerTitle.contains("developer") && !lowerTitle.contains("software engineer")) {
            category = RoleCategory.IT_SUPPORT;
            score = 35;
        } else {
            // Check for Java Full Stack (Highest Priority: 90-100)
            boolean titleIsJavaFullStack = lowerTitle.contains("java") && lowerTitle.contains("full");
            boolean isJavaFullStack = titleIsJavaFullStack ||
                    (hasFullStackKeyword && hasJava) ||
                    (hasJava && (hasSpring || hasBackend) && hasFrontend) ||
                    (lowerTitle.contains("full stack") && (hasJava || hasSpring));

            if (isJavaFullStack) {
                category = RoleCategory.JAVA_FULL_STACK;
                score = 95;
                if (hasJava && hasSpring) score = 100;
            }
            // Check for Java Backend (Priority 2: 80-89)
            else if ((lowerTitle.contains("java") && lowerTitle.contains("backend")) ||
                     (hasJava && hasSpring) ||
                     (hasJava && (hasBackend || lowerTitle.contains("backend")))) {
                category = RoleCategory.JAVA_BACKEND;
                score = 85;
                if (hasSpring) score = 89;
            }
            // Check for Java Developer / Java Engineer (Priority 3: 70-79)
            else if (lowerTitle.contains("java") || (hasJava && lowerTitle.contains("developer"))) {
                category = RoleCategory.JAVA_DEVELOPMENT;
                score = 75;
            }
            // Check for QA Automation from description
            else if (hasQaKeyword || matchedTech.contains("QA Automation")) {
                category = RoleCategory.QA_AUTOMATION;
                score = 45;
            }
            // Check for IT Support / Application Support from description
            else if (hasSupportKeyword) {
                category = RoleCategory.IT_SUPPORT;
                score = 35;
            }
            // General Software Engineering / Associate Developer (Priority 4: 50-69)
            else {
                category = RoleCategory.SOFTWARE_ENGINEERING;
                score = hasJava ? 70 : 60;
                if (hasFullStackKeyword) score = Math.max(score, 65);
                if (!matchedTech.isEmpty()) score = Math.min(68, score + matchedTech.size() * 2);
            }
        }

        return new JavaRelevanceResult(
                true,
                category,
                techMatchString,
                score,
                "Categorized as " + category.getDisplayName() + " with score " + score
        );
    }
}
