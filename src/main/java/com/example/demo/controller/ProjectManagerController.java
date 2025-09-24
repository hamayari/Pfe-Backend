package com.example.demo.controller;

import com.example.demo.service.ProjectManagerService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/project-manager")
public class ProjectManagerController {

    private final ProjectManagerService projectManagerService;

    public ProjectManagerController(ProjectManagerService projectManagerService) {
        this.projectManagerService = projectManagerService;
    }

    @GetMapping("/conventions")
    public List<Map<String, Object>> getFilteredConventions(
            @RequestParam(required = false) String commercial,
            @RequestParam(required = false) String governorate,
            @RequestParam(required = false) String status) {
        return projectManagerService.getConventionsWithFilters(commercial, governorate, status);
    }

    @GetMapping("/alerts")
    public List<Map<String, Object>> getAlerts() {
        return projectManagerService.getLatePaymentsAndIssues();
    }

    @PostMapping("/comments/{conventionId}")
    public void addComment(@PathVariable String conventionId, @RequestBody String comment) {
        projectManagerService.addCommentToConvention(conventionId, comment);
    }

    @GetMapping("/export/conventions")
    public byte[] exportConventionsReport() {
        return projectManagerService.exportConventionsReport();
    }
}
