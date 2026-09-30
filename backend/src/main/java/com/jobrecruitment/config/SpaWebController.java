package com.jobrecruitment.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller to forward all client-side React Router navigation routes to index.html.
 */
@Controller
public class SpaWebController {

    @GetMapping(value = {
        "/login",
        "/register",
        "/jobs",
        "/jobs/**",
        "/candidate/**",
        "/recruiter/**",
        "/admin/**",
        "/not-authorized"
    })
    public String forwardSpaRoutes() {
        return "forward:/index.html";
    }
}
