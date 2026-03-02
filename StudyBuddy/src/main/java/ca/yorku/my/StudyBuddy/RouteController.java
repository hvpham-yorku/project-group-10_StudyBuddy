package ca.yorku.my.StudyBuddy;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class RouteController {

    @RequestMapping(value = {
        "/",
        "/signin",
        "/home",
        "/profile",
        "/events",
        "/map"
    })
    public String forward() {
        return "forward:/index.html";
    }
}
