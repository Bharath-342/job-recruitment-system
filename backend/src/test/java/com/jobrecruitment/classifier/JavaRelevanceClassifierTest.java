package com.jobrecruitment.classifier;

import com.jobrecruitment.entity.RoleCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Java Relevance Classifier & Role Categorization Tests")
class JavaRelevanceClassifierTest {

    private JavaRelevanceClassifier classifier;

    @BeforeEach
    void setUp() {
        classifier = new JavaRelevanceClassifier();
    }

    @Test
    @DisplayName("Java Full Stack Developer is correctly categorized with score >= 90")
    void testJavaFullStackCategorization() {
        var result = classifier.classify(
                "Java Full Stack Developer",
                "Looking for a Java Full Stack Developer skilled in Java 17, Spring Boot, React, and MySQL database."
        );

        assertTrue(result.isItSoftware());
        assertEquals(RoleCategory.JAVA_FULL_STACK, result.roleCategory());
        assertTrue(result.relevanceScore() >= 90, "Relevance score should be >= 90 for Java Full Stack");
        assertTrue(result.technologyMatch().contains("Java"));
        assertTrue(result.technologyMatch().contains("Spring Boot"));
        assertTrue(result.technologyMatch().contains("React"));
    }

    @Test
    @DisplayName("Associate Software Engineer with Java & Spring Boot has high relevance")
    void testAssociateSoftwareEngineerWithJava() {
        var result = classifier.classify(
                "Associate Software Engineer",
                "Qualifications: Hands-on experience with Core Java, Spring Boot microservices, and PostgreSQL."
        );

        assertTrue(result.isItSoftware());
        assertTrue(result.roleCategory() == RoleCategory.JAVA_BACKEND || result.roleCategory() == RoleCategory.SOFTWARE_ENGINEERING);
        assertTrue(result.relevanceScore() >= 70, "Should have strong relevance score with Java technologies");
        assertTrue(result.technologyMatch().contains("Java"));
    }

    @Test
    @DisplayName("Graduate Software Engineer Trainee without specific tech mentioned defaults to Software Engineering")
    void testGraduateSoftwareEngineerTrainee() {
        var result = classifier.classify(
                "Graduate Software Engineer Trainee",
                "Entry-level opening for 2024/2025 graduates with computer science degrees."
        );

        assertTrue(result.isItSoftware());
        assertEquals(RoleCategory.SOFTWARE_ENGINEERING, result.roleCategory());
        assertTrue(result.relevanceScore() >= 50);
    }

    @Test
    @DisplayName("QA Automation Engineer is categorized as QA_AUTOMATION")
    void testQaAutomationEngineer() {
        var result = classifier.classify(
                "QA Automation Engineer",
                "Experience with Selenium, TestNG, Java, and automated regression suites."
        );

        assertTrue(result.isItSoftware());
        assertEquals(RoleCategory.QA_AUTOMATION, result.roleCategory());
        assertTrue(result.relevanceScore() >= 60);
    }

    @Test
    @DisplayName("Application Support Engineer is categorized as IT_SUPPORT")
    void testApplicationSupportEngineer() {
        var result = classifier.classify(
                "Application Support Engineer",
                "L1/L2 application support, monitoring production logs, SQL queries, and incident resolution."
        );

        assertTrue(result.isItSoftware());
        assertEquals(RoleCategory.IT_SUPPORT, result.roleCategory());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "HR Executive",
            "Human Resources Intern",
            "Talent Acquisition Specialist",
            "Technical Recruiter",
            "Accounts Payable Specialist",
            "Financial Analyst",
            "Sales Executive",
            "Business Development Associate",
            "Content Writer",
            "Social Media Marketing Intern",
            "Customer Support Representative",
            "Civil Engineer",
            "Mechanical Site Supervisor"
    })
    @DisplayName("Non-IT roles must be strictly rejected (isItSoftware=false, RoleCategory.OTHER)")
    void testNonItRolesRejected(String nonItTitle) {
        var result = classifier.classify(nonItTitle, "General duties and responsibilities for the specified role.");

        assertFalse(result.isItSoftware(), "Non-IT title must be rejected: " + nonItTitle);
        assertEquals(RoleCategory.OTHER, result.roleCategory());
        assertEquals(0, result.relevanceScore());
    }
}
