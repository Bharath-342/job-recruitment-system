package com.jobrecruitment.classifier;

import com.jobrecruitment.entity.LocationClassification;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class JobLocationParser {

    public record ParsedLocation(
            String country,
            String state,
            String city,
            boolean isRemote,
            String locationText,
            int locationConfidence,
            LocationClassification classification,
            String details,
            String countryCode,
            String countryName
    ) {
        public ParsedLocation(String country, String state, String city, boolean isRemote,
                              String locationText, int locationConfidence, LocationClassification classification, String details) {
            this(country, state, city, isRemote, locationText, locationConfidence, classification, details,
                 classification == LocationClassification.INDIA ? "IN" : null,
                 classification == LocationClassification.INDIA ? "India" : country);
        }
    }

    // Explicit foreign country patterns (Hard NON_INDIA)
    private static final Pattern NON_INDIA_COUNTRY_PATTERN = Pattern.compile(
            "\\b(usa|united states|united states of america|u\\.s\\.a?\\.?|uk|united kingdom|great britain|england|scotland|wales|canada|australia|germany|france|singapore|uae|united arab emirates|dubai|netherlands|ireland|switzerland|sweden|spain|italy|poland|israel|japan|china|brazil|mexico|new zealand|south africa|philippines|vietnam|colombia|argentina|portugal|belgium|austria|norway|finland|denmark|czech republic|romania|hungary|estonia|taiwan|korea|indonesia|malaysia|egypt|kenya|nigeria)\\b",
            Pattern.CASE_INSENSITIVE);

    // Foreign cities, US state codes, and US/Canadian/European states / regions (Hard NON_INDIA)
    private static final Pattern NON_INDIA_CITY_REGION_PATTERN = Pattern.compile(
            "\\b(new york|nyc|boston|san francisco|sf bay area|bay area|seattle|austin|los angeles|chicago|denver|atlanta|dallas|houston|miami|philadelphia|washington\\s*(?:d\\.?c\\.?)?|california|texas|massachusetts|washington state|virginia|arlington|maryland|colorado|illinois|ohio|florida|georgia|north carolina|new jersey|pennsylvania|michigan|arizona|ontario|quebec|british columbia|london|manchester|birmingham|edinburgh|berlin|munich|frankfurt|hamburg|paris|lyon|amsterdam|rotterdam|dublin|sydney|melbourne|brisbane|toronto|vancouver|montreal|ottawa|tokyo|seoul|beijing|shanghai|hong kong|sao paulo|mexico city|madrid|barcelona|milan|rome|zurich|geneva|stockholm|warsaw|krakow|tel aviv|auckland)\\b|" +
            ",\\s*(?:al|ak|az|ar|ca|co|ct|de|fl|ga|hi|id|il|ia|ks|ky|la|me|md|ma|mi|mn|ms|mo|mt|ne|nv|nh|nj|nm|ny|nc|nd|oh|ok|or|pa|ri|sc|sd|tn|tx|ut|vt|va|wa|wv|wi|wy)\\b",
            Pattern.CASE_INSENSITIVE);

    // Explicit non-India remote regions
    private static final Pattern NON_INDIA_REMOTE_PATTERN = Pattern.compile(
            "\\b(remote\\s*-\\s*(?:us|usa|united states|americas?|north america|emea|europe|latam|uk|canada|apac|australia|germany|france|global|worldwide)|(?:us|usa|uk|canada|europe|emea|latam)\\s*-\\s*remote)\\b",
            Pattern.CASE_INSENSITIVE);

    // Explicit India patterns
    private static final Pattern INDIA_EXPLICIT_PATTERN = Pattern.compile(
            "\\b(india|in)\\b",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern INDIA_REMOTE_PATTERN = Pattern.compile(
            "\\b(remote\\s*-\\s*india|india\\s*-\\s*remote|remote\\s*\\(india\\)|india\\s*\\(remote\\)|remote,\\s*india|india,\\s*remote|work\\s+from\\s+home\\s*-\\s*india|remote\\s+in\\s+india|anywhere\\s+in\\s+india|india\\s+remote|remote\\s+india)\\b",
            Pattern.CASE_INSENSITIVE);

    // Indian Cities with their canonical name and State
    private static final Map<String, String[]> INDIAN_CITIES = new LinkedHashMap<>();
    static {
        INDIAN_CITIES.put("bengaluru", new String[]{"Bengaluru", "Karnataka"});
        INDIAN_CITIES.put("bangalore", new String[]{"Bengaluru", "Karnataka"});
        INDIAN_CITIES.put("hyderabad", new String[]{"Hyderabad", "Telangana"});
        INDIAN_CITIES.put("secunderabad", new String[]{"Secunderabad", "Telangana"});
        INDIAN_CITIES.put("pune", new String[]{"Pune", "Maharashtra"});
        INDIAN_CITIES.put("mumbai", new String[]{"Mumbai", "Maharashtra"});
        INDIAN_CITIES.put("navi mumbai", new String[]{"Navi Mumbai", "Maharashtra"});
        INDIAN_CITIES.put("thane", new String[]{"Thane", "Maharashtra"});
        INDIAN_CITIES.put("chennai", new String[]{"Chennai", "Tamil Nadu"});
        INDIAN_CITIES.put("madras", new String[]{"Chennai", "Tamil Nadu"});
        INDIAN_CITIES.put("delhi", new String[]{"Delhi", "Delhi"});
        INDIAN_CITIES.put("new delhi", new String[]{"New Delhi", "Delhi"});
        INDIAN_CITIES.put("noida", new String[]{"Noida", "Uttar Pradesh"});
        INDIAN_CITIES.put("greater noida", new String[]{"Greater Noida", "Uttar Pradesh"});
        INDIAN_CITIES.put("gurugram", new String[]{"Gurugram", "Haryana"});
        INDIAN_CITIES.put("gurgaon", new String[]{"Gurugram", "Haryana"});
        INDIAN_CITIES.put("delhi ncr", new String[]{"Delhi NCR", "Delhi"});
        INDIAN_CITIES.put("ncr", new String[]{"Delhi NCR", "Delhi"});
        INDIAN_CITIES.put("kolkata", new String[]{"Kolkata", "West Bengal"});
        INDIAN_CITIES.put("calcutta", new String[]{"Kolkata", "West Bengal"});
        INDIAN_CITIES.put("ahmedabad", new String[]{"Ahmedabad", "Gujarat"});
        INDIAN_CITIES.put("gandhinagar", new String[]{"Gandhinagar", "Gujarat"});
        INDIAN_CITIES.put("kochi", new String[]{"Kochi", "Kerala"});
        INDIAN_CITIES.put("cochin", new String[]{"Kochi", "Kerala"});
        INDIAN_CITIES.put("ernakulam", new String[]{"Ernakulam", "Kerala"});
        INDIAN_CITIES.put("thiruvananthapuram", new String[]{"Thiruvananthapuram", "Kerala"});
        INDIAN_CITIES.put("trivandrum", new String[]{"Thiruvananthapuram", "Kerala"});
        INDIAN_CITIES.put("visakhapatnam", new String[]{"Visakhapatnam", "Andhra Pradesh"});
        INDIAN_CITIES.put("vizag", new String[]{"Visakhapatnam", "Andhra Pradesh"});
        INDIAN_CITIES.put("vijayawada", new String[]{"Vijayawada", "Andhra Pradesh"});
        INDIAN_CITIES.put("coimbatore", new String[]{"Coimbatore", "Tamil Nadu"});
        INDIAN_CITIES.put("indore", new String[]{"Indore", "Madhya Pradesh"});
        INDIAN_CITIES.put("bhopal", new String[]{"Bhopal", "Madhya Pradesh"});
        INDIAN_CITIES.put("jaipur", new String[]{"Jaipur", "Rajasthan"});
        INDIAN_CITIES.put("chandigarh", new String[]{"Chandigarh", "Chandigarh"});
        INDIAN_CITIES.put("mohali", new String[]{"Mohali", "Punjab"});
        INDIAN_CITIES.put("panchkula", new String[]{"Panchkula", "Haryana"});
        INDIAN_CITIES.put("bhubaneswar", new String[]{"Bhubaneswar", "Odisha"});
        INDIAN_CITIES.put("mysore", new String[]{"Mysuru", "Karnataka"});
        INDIAN_CITIES.put("mysuru", new String[]{"Mysuru", "Karnataka"});
        INDIAN_CITIES.put("nagpur", new String[]{"Nagpur", "Maharashtra"});
        INDIAN_CITIES.put("vadodara", new String[]{"Vadodara", "Gujarat"});
        INDIAN_CITIES.put("baroda", new String[]{"Vadodara", "Gujarat"});
        INDIAN_CITIES.put("surat", new String[]{"Surat", "Gujarat"});
        INDIAN_CITIES.put("lucknow", new String[]{"Lucknow", "Uttar Pradesh"});
        INDIAN_CITIES.put("kanpur", new String[]{"Kanpur", "Uttar Pradesh"});
        INDIAN_CITIES.put("mangaluru", new String[]{"Mangaluru", "Karnataka"});
        INDIAN_CITIES.put("mangalore", new String[]{"Mangaluru", "Karnataka"});
        INDIAN_CITIES.put("dehradun", new String[]{"Dehradun", "Uttarakhand"});
        INDIAN_CITIES.put("patna", new String[]{"Patna", "Bihar"});
        INDIAN_CITIES.put("raipur", new String[]{"Raipur", "Chhattisgarh"});
        INDIAN_CITIES.put("ranchi", new String[]{"Ranchi", "Jharkhand"});
        INDIAN_CITIES.put("guwahati", new String[]{"Guwahati", "Assam"});
    }

    // Indian States
    private static final Map<String, String> INDIAN_STATES = new LinkedHashMap<>();
    static {
        INDIAN_STATES.put("karnataka", "Karnataka");
        INDIAN_STATES.put("telangana", "Telangana");
        INDIAN_STATES.put("maharashtra", "Maharashtra");
        INDIAN_STATES.put("tamil nadu", "Tamil Nadu");
        INDIAN_STATES.put("andhra pradesh", "Andhra Pradesh");
        INDIAN_STATES.put("uttar pradesh", "Uttar Pradesh");
        INDIAN_STATES.put("haryana", "Haryana");
        INDIAN_STATES.put("delhi", "Delhi");
        INDIAN_STATES.put("west bengal", "West Bengal");
        INDIAN_STATES.put("gujarat", "Gujarat");
        INDIAN_STATES.put("kerala", "Kerala");
        INDIAN_STATES.put("punjab", "Punjab");
        INDIAN_STATES.put("rajasthan", "Rajasthan");
        INDIAN_STATES.put("madhya pradesh", "Madhya Pradesh");
        INDIAN_STATES.put("odisha", "Odisha");
        INDIAN_STATES.put("bihar", "Bihar");
        INDIAN_STATES.put("assam", "Assam");
        INDIAN_STATES.put("uttarakhand", "Uttarakhand");
        INDIAN_STATES.put("goa", "Goa");
        INDIAN_STATES.put("jharkhand", "Jharkhand");
        INDIAN_STATES.put("chhattisgarh", "Chhattisgarh");
    }

    public ParsedLocation parse(String rawLocation, String title, String description) {
        String loc = rawLocation != null ? rawLocation.trim() : "";
        String safeTitle = title != null ? title : "";
        String safeDesc = description != null ? description : "";
        String lowerLoc = loc.toLowerCase();

        boolean isRemote = lowerLoc.contains("remote") ||
                safeTitle.toLowerCase().contains("remote") ||
                lowerLoc.contains("work from home") ||
                lowerLoc.contains("wfh");

        // 1. HARD NON-INDIA CHECKS
        // If location text clearly references a foreign country, city, or foreign remote region
        boolean hasForeignCountry = NON_INDIA_COUNTRY_PATTERN.matcher(lowerLoc).find();
        boolean hasForeignCityRegion = NON_INDIA_CITY_REGION_PATTERN.matcher(lowerLoc).find();
        boolean hasForeignRemote = NON_INDIA_REMOTE_PATTERN.matcher(lowerLoc).find() ||
                NON_INDIA_REMOTE_PATTERN.matcher(safeTitle.toLowerCase()).find();

        if (hasForeignCountry || hasForeignCityRegion || hasForeignRemote) {
            // Check if it's a multi-location listing that includes India explicitly
            boolean explicitlyMentionsIndia = INDIA_EXPLICIT_PATTERN.matcher(lowerLoc).find() ||
                    hasIndianCityOrState(lowerLoc);

            if (!explicitlyMentionsIndia) {
                String foreignCountry = extractForeignCountryName(lowerLoc);
                return new ParsedLocation(
                        foreignCountry.toUpperCase(),
                        null,
                        null,
                        isRemote,
                        loc,
                        95,
                        LocationClassification.NON_INDIA,
                        "Foreign location detected: " + loc,
                        null,
                        foreignCountry
                );
            }
        }

        // 2. EXPLICIT INDIA REMOTE CHECK
        if (INDIA_REMOTE_PATTERN.matcher(lowerLoc).find() || INDIA_REMOTE_PATTERN.matcher(safeTitle.toLowerCase()).find()) {
            return new ParsedLocation(
                    "INDIA",
                    null,
                    "Remote",
                    true,
                    loc,
                    95,
                    LocationClassification.INDIA,
                    "Verified Remote - India position",
                    "IN",
                    "India"
            );
        }

        // 3. INDIAN CITY / STATE MATCHING
        String matchedCity = null;
        String matchedState = null;

        for (Map.Entry<String, String[]> entry : INDIAN_CITIES.entrySet()) {
            Pattern cityPattern = Pattern.compile("\\b" + Pattern.quote(entry.getKey()) + "\\b", Pattern.CASE_INSENSITIVE);
            if (cityPattern.matcher(lowerLoc).find()) {
                matchedCity = entry.getValue()[0];
                matchedState = entry.getValue()[1];
                break;
            }
        }

        if (matchedState == null) {
            for (Map.Entry<String, String> entry : INDIAN_STATES.entrySet()) {
                Pattern statePattern = Pattern.compile("\\b" + Pattern.quote(entry.getKey()) + "\\b", Pattern.CASE_INSENSITIVE);
                if (statePattern.matcher(lowerLoc).find()) {
                    matchedState = entry.getValue();
                    break;
                }
            }
        }

        // 4. EXPLICIT "INDIA" IN LOCATION
        boolean hasIndiaWord = Pattern.compile("\\bindia\\b", Pattern.CASE_INSENSITIVE).matcher(lowerLoc).find();

        if (matchedCity != null || matchedState != null || hasIndiaWord) {
            // Verify there is no conflicting foreign location in a single-location job
            if (hasForeignCountry || hasForeignCityRegion) {
                // If it lists both e.g. "Boston, USA; Bengaluru, India", this job is valid for India
                if (matchedCity != null) {
                    return new ParsedLocation(
                            "INDIA",
                            matchedState,
                            matchedCity,
                            isRemote,
                            loc,
                            90,
                            LocationClassification.INDIA,
                            "Multi-region opening verified with Indian presence in " + matchedCity,
                            "IN",
                            "India"
                    );
                }
            }

            int confidence = matchedCity != null ? 95 : (matchedState != null ? 90 : 85);
            return new ParsedLocation(
                    "INDIA",
                    matchedState,
                    matchedCity,
                    isRemote,
                    loc,
                    confidence,
                    LocationClassification.INDIA,
                    "Verified Indian location: " + (matchedCity != null ? matchedCity + ", " : "") +
                            (matchedState != null ? matchedState + ", " : "") + "India",
                    "IN",
                    "India"
            );
        }

        // 5. AMBIGUOUS / GENERIC LOCATION CHECK (e.g. "Remote", "Worldwide", "Anywhere", "Not Specified", blank)
        // Under strict policy: NEVER assume India without explicit source verification
        return new ParsedLocation(
                "UNKNOWN",
                null,
                null,
                isRemote,
                loc,
                0,
                LocationClassification.UNKNOWN,
                "Ambiguous or non-Indian location without explicit India confirmation: " + loc,
                null,
                null
        );
    }

    private boolean hasIndianCityOrState(String lowerLoc) {
        for (String cityKey : INDIAN_CITIES.keySet()) {
            if (Pattern.compile("\\b" + Pattern.quote(cityKey) + "\\b").matcher(lowerLoc).find()) {
                return true;
            }
        }
        for (String stateKey : INDIAN_STATES.keySet()) {
            if (Pattern.compile("\\b" + Pattern.quote(stateKey) + "\\b").matcher(lowerLoc).find()) {
                return true;
            }
        }
        return false;
    }

    private String extractForeignCountryName(String lowerLoc) {
        Matcher m = NON_INDIA_COUNTRY_PATTERN.matcher(lowerLoc);
        if (m.find()) {
            String match = m.group(1).trim();
            if (match.equalsIgnoreCase("usa") || match.equalsIgnoreCase("united states") || match.startsWith("u.s")) {
                return "United States";
            }
            if (match.equalsIgnoreCase("uk") || match.equalsIgnoreCase("united kingdom") || match.equalsIgnoreCase("england")) {
                return "United Kingdom";
            }
            return Character.toUpperCase(match.charAt(0)) + match.substring(1);
        }
        return "International";
    }
}
