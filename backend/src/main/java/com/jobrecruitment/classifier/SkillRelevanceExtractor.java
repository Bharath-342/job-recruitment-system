package com.jobrecruitment.classifier;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class SkillRelevanceExtractor {

    private record SkillRule(String name, Pattern pattern) {}

    private final List<SkillRule> rules = List.of(
            new SkillRule("Java", Pattern.compile("\\bJava\\b", Pattern.CASE_INSENSITIVE)),
            new SkillRule("Spring Boot", Pattern.compile("\\bSpring Boot\\b", Pattern.CASE_INSENSITIVE)),
            new SkillRule("Spring", Pattern.compile("\\bSpring(?: MVC| Data| Security)?\\b", Pattern.CASE_INSENSITIVE)),
            new SkillRule("Hibernate", Pattern.compile("\\bHibernate\\b|\\bJPA\\b", Pattern.CASE_INSENSITIVE)),
            new SkillRule("REST API", Pattern.compile("\\bREST(?:ful)?(?: API)?\\b", Pattern.CASE_INSENSITIVE)),
            new SkillRule("Microservices", Pattern.compile("\\bMicroservices?\\b", Pattern.CASE_INSENSITIVE)),
            new SkillRule("React", Pattern.compile("\\bReact(?:\\.js|js)?\\b", Pattern.CASE_INSENSITIVE)),
            new SkillRule("JavaScript", Pattern.compile("\\bJavaScript\\b|\\bJS\\b|\\bTypeScript\\b|\\bTS\\b", Pattern.CASE_INSENSITIVE)),
            new SkillRule("SQL", Pattern.compile("\\bSQL\\b|\\bMySQL\\b|\\bPostgreSQL\\b|\\bPostgres\\b", Pattern.CASE_INSENSITIVE)),
            new SkillRule("HTML/CSS", Pattern.compile("\\bHTML5?\\b|\\bCSS3?\\b", Pattern.CASE_INSENSITIVE)),
            new SkillRule("Docker", Pattern.compile("\\bDocker\\b|\\bKubernetes\\b|\\bK8s\\b", Pattern.CASE_INSENSITIVE)),
            new SkillRule("Git", Pattern.compile("\\bGit\\b|\\bGitHub\\b|\\bGitLab\\b", Pattern.CASE_INSENSITIVE)),
            new SkillRule("Python", Pattern.compile("\\bPython\\b", Pattern.CASE_INSENSITIVE)),
            new SkillRule("Linux", Pattern.compile("\\bLinux\\b|\\bUbuntu\\b", Pattern.CASE_INSENSITIVE)),
            new SkillRule("Cloud/AWS", Pattern.compile("\\bAWS\\b|\\bAzure\\b|\\bGCP\\b|\\bCloud\\b", Pattern.CASE_INSENSITIVE))
    );

    public String extractSkills(String title, String description) {
        String text = (title != null ? title : "") + " " + (description != null ? description : "");
        List<String> matched = new ArrayList<>();
        for (SkillRule rule : rules) {
            if (rule.pattern().matcher(text).find()) {
                matched.add(rule.name());
            }
        }
        if (matched.isEmpty()) {
            return "Software Engineering, Problem Solving";
        }
        return String.join(", ", matched);
    }
}
