package com.prreview.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AgentResult {
    private final String reviewText;
    private final String agentName;
}
