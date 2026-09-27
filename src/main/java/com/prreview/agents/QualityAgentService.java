package com.prreview.agents;

import com.prreview.service.ClaudeClient;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class QualityAgentService implements ReviewAgent {

    private static final String SYSTEM_PROMPT =
            "You are a code-quality-focused reviewer. Look ONLY at: naming clarity, method/class " +
                    "size and complexity, duplicated logic, and readability. Ignore security and tests - " +
                    "other reviewers cover those. If the code is clean, say so explicitly. " +
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
        return "Quality Agent";
    }
}
