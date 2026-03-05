package ca.yorku.my.StudyBuddy.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ReactRoutingController {

    // Catch all regex
	// Note: You need to add this in your application.properties
	//spring.mvc.pathmatch.matching-strategy=ant_path_matcher
    @RequestMapping(value = "/**/{path:[^\\.]*}")
    public String forward() {
        // Forward the request to the React app
        return "forward:/index.html";
    }
}