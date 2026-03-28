package ca.yorku.my.StudyBuddy.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class ReactRoutingController {

    // Forward root path to index.html
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String forwardRoot() {
        return "forward:/index.html";
    }

    // Forward SPA routes (exclude error paths, API paths, and static resources)
    @RequestMapping(value = "/{path:[^\\.]*}", method = RequestMethod.GET)
    public String forward(String path) {
        // Don't forward error pages or API paths
        if (path.startsWith("error") || path.startsWith("api")) {
            return null; // Let Spring handle these normally
        }
        return "forward:/index.html";
    }
}