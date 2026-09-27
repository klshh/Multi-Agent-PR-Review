package com.prreview.agents;

import com.prreview.service.ClaudeClient;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
@Service
@RequiredArgsConstructor
public class TestCoverageService implements ReviewAgent{
    private static final String SYSTEM_PROMPT =
            "You are a test-coverage-focused reviewer. Look ONLY at whether the new or changed " +
                    "logic in this diff has corresponding test coverage. Point out specific methods or " +
                    "branches (e.g. error paths, edge cases) that appear untested. Do not comment on " +
                    "security, style, or documentation. If coverage looks adequate, say so explicitly. " +
                    "Keep it to short bullet points.";

    private final ClaudeClient claudeClient;

    @Override
    @Async("agentTaskExecutor")
    public CompletableFuture<String> review(String diff) {
        String result = claudeClient.sendMessage(SYSTEM_PROMPT, diff);
        return CompletableFuture.completedFuture(result);
    }

    @Override
    public String getAgentName() {
        return "Test Coverage Agent";
    }
}
