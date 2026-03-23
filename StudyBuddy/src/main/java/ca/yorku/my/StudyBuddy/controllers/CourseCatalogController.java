package ca.yorku.my.StudyBuddy.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ca.yorku.my.StudyBuddy.services.YorkCourseCatalogService;

@RestController
@RequestMapping("/api/courses")
@CrossOrigin(origins = "*")

// This controller provides endpoints for searching and retrieving course codes from the York course catalog.
public class CourseCatalogController {

    @Autowired
    private YorkCourseCatalogService yorkCourseCatalogService;

    @GetMapping("/search")
    public List<String> searchCourses(
            @RequestParam(name = "q", defaultValue = "") String query, 
            @RequestParam(name = "limit", defaultValue = "20") int limit) {

        return yorkCourseCatalogService.searchCourseCodes(query, limit);
    }
}
