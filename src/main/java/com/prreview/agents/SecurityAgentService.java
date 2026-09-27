package com.prreview.agents;

import com.prreview.service.ClaudeClient;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;


@Service
@RequiredArgsConstructor
public class SecurityAgentService implements ReviewAgent{
    private static final String SYSTEM_PROMPT =
            "You are a security-focused code reviewer. Look ONLY for security issues in the " +
                    "given diff: SQL injection, hardcoded secrets/credentials, unsafe deserialization, " +
                    "missing input validation, broken authentication/authorization checks, and unsafe " +
                    "use of external input. Ignore naming, style, and anything unrelated to security. " +
                    "If you find nothing, say so explicitly - do not invent issues. " +
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
        return "Security Agent";
    }
}
