package com.prreview.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class ReviewResponse {
    private final Long id;
    private final String review;
    private final Instant reviewedAt;
}