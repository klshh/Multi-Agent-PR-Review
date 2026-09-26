package com.prreview.service;

import com.prreview.dto.ReviewResponse;
import com.prreview.model.ReviewRecord;
import com.prreview.repository.ReviewRecordRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class ReviewService {

    // Phase 1 uses a single general-purpose reviewer prompt.
    // In Phase 2 this gets split into QualityAgent / SecurityAgent / TestAgent / DocAgent,
    // each with a prompt like this but narrower in focus.
    // Text blocks need Java 15+, so this build (Java 11) uses a plain
    // concatenated string instead.
    private static final String SYSTEM_PROMPT =
            "You are an experienced senior software engineer performing a code review.\n" +
            "You will be given a code diff. Review it for:\n" +
            "- bugs or logic errors\n" +
            "- security issues\n" +
            "- readability and naming\n" +
            "- missing tests for new logic\n" +
            "Be specific and reference line content where relevant.\n" +
            "Keep the review concise: use short bullet points, not long paragraphs.\n" +
            "End with one line: \"Verdict: Approve\" or \"Verdict: Changes requested\".";

    private final ClaudeClient claudeClient;
    private final ReviewRecordRepository repository;

    public ReviewService(ClaudeClient claudeClient, ReviewRecordRepository repository) {
        this.claudeClient = claudeClient;
        this.repository = repository;
    }

    public ReviewResponse reviewDiff(String diff) {
        String review = claudeClient.sendMessage(SYSTEM_PROMPT, diff);

        ReviewRecord record = ReviewRecord.builder()
                .diffContent(diff)
                .reviewResult(review)
                .build();

        ReviewRecord saved = repository.save(record);

        return new ReviewResponse(saved.getId(), saved.getReviewResult(), saved.getCreatedAt());
    }

    public List<ReviewRecord> getHistory() {
        return repository.findAll();
    }
}
