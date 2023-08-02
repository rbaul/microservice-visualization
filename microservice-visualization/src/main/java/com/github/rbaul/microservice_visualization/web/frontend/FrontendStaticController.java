package com.github.rbaul.microservice_visualization.web.frontend;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontendStaticController {
    @GetMapping({"/ui/**"})
    public String redirectToUI() {
        return "forward:/";
    }
}