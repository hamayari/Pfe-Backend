package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequestMapping("/swagger")
public class SwaggerController {
    
    @GetMapping("/ui")
    public ModelAndView redirectToSwaggerUI() {
        return new ModelAndView("redirect:/swagger-ui/index.html");
    }
    
    @GetMapping("/api-docs")
    public ModelAndView redirectToApiDocs() {
        return new ModelAndView("redirect:/v3/api-docs");
    }
}
