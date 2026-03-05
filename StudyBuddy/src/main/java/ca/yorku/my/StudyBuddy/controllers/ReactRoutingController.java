package ca.yorku.my.StudyBuddy.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ReactRoutingController {

    // This regex catches all routes that do NOT have a file extension (like .js, .css, .png)
    // Spring Boot will automatically prioritize your actual /api/ @RestControllers first!
    @RequestMapping(value = "/**/{path:[^\\.]*}")
    public String forward() {
        // Forward the request to the React app
        return "forward:/index.html";
    }
}