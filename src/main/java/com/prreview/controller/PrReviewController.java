package com.prreview.controller;

import com.prreview.dto.PrReviewSummary;
import com.prreview.service.PrReviewOrchestrator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PrReviewController {

    private final PrReviewOrchestrator prReviewOrchestrator;


    @PostMapping("/pr-review/{owner}/{repo}/{prNumber}")
    public ResponseEntity<PrReviewSummary> reviewPr(
            @PathVariable String owner,
            @PathVariable String repo,
            @PathVariable int prNumber) {
        PrReviewSummary summary = prReviewOrchestrator.runFullReview(owner, repo, prNumber);
        return ResponseEntity.ok(summary);
    }
}
