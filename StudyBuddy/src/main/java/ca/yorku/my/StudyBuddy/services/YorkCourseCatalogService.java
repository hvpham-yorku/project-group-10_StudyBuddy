package ca.yorku.my.StudyBuddy.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
// This service loads and manages the York course catalog, providing search functionality for course codes.
public class YorkCourseCatalogService {

    private final List<String> courseCodes = new ArrayList<>();

    @PostConstruct
    public void loadCourseCodes() {
        Set<String> uniqueCodes = new LinkedHashSet<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new ClassPathResource("york_courses_parsed.csv").getInputStream(), StandardCharsets.UTF_8))) {

            // Skip CSV first line
            reader.readLine();

            String line;
            while ((line = reader.readLine()) != null) {
                List<String> columns = parseLine(line);
                if (columns.size() < 3) { 
                    continue;
                }

                String subject = columns.get(1).trim().toUpperCase(Locale.ROOT); // Filter out and trim non-alphabetic characters from subject
                String courseNumber = columns.get(2).trim(); // Filter out and trim non-alphabetic characters from course number
                if (subject.isEmpty() || courseNumber.isEmpty()) {
                    continue;
                }

                uniqueCodes.add(subject + " " + courseNumber); // Combine subject and course number to form the full course code and add to the set of unique codes
            }
        } catch (Exception ex) {
            // Leave catalog empty if file loading fails.
            courseCodes.clear();
            return;
        }

        courseCodes.clear();
        courseCodes.addAll(uniqueCodes.stream()
                .sorted(Comparator.naturalOrder()) // Sort course codes alphabetically
                .collect(Collectors.toList())); // Collect sorted course codes into the final list
    }

    
    public List<String> searchCourseCodes(String rawQuery, int maxResults) {
        String query = normalize(rawQuery);
        if (query.isEmpty()) {
            return List.of();
        }

        int limit = Math.max(1, Math.min(maxResults, 50)); // 
        List<String> results = new ArrayList<>();
        
        // Iterate through the course codes and add matches to the results list until the limit above is reached. Used so not too many courses are shown at once.
        for (String code: courseCodes) {
            String normalizedCode = normalize(code);
            if (!normalizedCode.startsWith(query)) {
                continue;
            }

            results.add(code);
            if (results.size() >= limit) {
                break;
            }
        }

        return results;
    }
    // Converts the string to uppercase and removes non-alphabetic/numeric charaacters.
    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        return value
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]", "");
    }

    // Parses CSV file to only include course codes. It filters out quotes and commas to only show course codes.
    private List<String> parseLine(String line) {
        List<String> columns = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') { // Filter out quotes
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') { // Check for escaped quote in course titles
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
                continue;
            }

            if (ch == ',' && !inQuotes) { // Filter out commas
                columns.add(current.toString());
                current.setLength(0);
                continue;
            }

            current.append(ch); // Append all other characters
        }

        columns.add(current.toString()); 
        return columns;
    }
}
