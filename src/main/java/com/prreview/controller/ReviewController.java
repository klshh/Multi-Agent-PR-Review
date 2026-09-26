package com.prreview.controller;

import com.prreview.dto.ReviewRequest;
import com.prreview.dto.ReviewResponse;
import com.prreview.model.ReviewRecord;
import com.prreview.service.ReviewService;
import javax.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * POST /review
     * Body: { "diff": "...raw diff or code snippet..." }
     * Returns Claude's review and saves it to history.
     */
    @PostMapping("/review")
    public ResponseEntity<ReviewResponse> review(@Valid @RequestBody ReviewRequest request) {
        ReviewResponse response = reviewService.reviewDiff(request.getDiff());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /reviews
     * Returns every review run so far - your "history" view.
     */
    @GetMapping("/reviews")
    public ResponseEntity<List<ReviewRecord>> history() {
        return ResponseEntity.ok(reviewService.getHistory());
    }
}
