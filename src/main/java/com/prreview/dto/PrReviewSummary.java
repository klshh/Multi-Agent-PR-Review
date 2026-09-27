package com.prreview.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class PrReviewSummary {
    private final Long id;
    private final String owner;
    private final String repo;
    private final int prNumber;
    private final List<AgentResult> agentResults;
    private final String finalSummary;
    private final Instant reviewedAt;
}
