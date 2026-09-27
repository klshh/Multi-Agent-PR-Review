package com.prreview.controller;

import com.prreview.agents.DocAgentService;
import com.prreview.agents.QualityAgentService;
import com.prreview.agents.SecurityAgentService;
import com.prreview.agents.TestCoverageService;
import com.prreview.dto.ReviewRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/agents")
@RequiredArgsConstructor
public class AgentController {

    private final SecurityAgentService securityAgent;
    private final QualityAgentService qualityAgent;
    private final TestCoverageService testCoverageAgent;
    private final DocAgentService docAgent;

    @PostMapping("/security")
    public ResponseEntity<String> reviewSecurity(@RequestBody ReviewRequest request) {
        String result = securityAgent.review(request.getDiff()).join();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/quality")
    public ResponseEntity<String> reviewQuality(@RequestBody ReviewRequest request) {
        String result = qualityAgent.review(request.getDiff()).join();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/tests")
    public ResponseEntity<String> reviewTests(@RequestBody ReviewRequest request) {
        String result = testCoverageAgent.review(request.getDiff()).join();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/docs")
    public ResponseEntity<String> reviewDocs(@RequestBody ReviewRequest request) {
        String result = docAgent.review(request.getDiff()).join();
        return ResponseEntity.ok(result);
    }
}