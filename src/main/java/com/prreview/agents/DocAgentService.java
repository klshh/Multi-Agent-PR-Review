package com.prreview.agents;

import com.prreview.service.ClaudeClient;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class DocAgentService implements ReviewAgent{
    private static final String SYSTEM_PROMPT =
            "You are a documentation-focused reviewer. Look ONLY at whether public methods/classes " +
                    "changed in this diff have adequate Javadoc/comments, and whether any README or API " +
                    "documentation likely needs updating given these changes. Ignore security, tests, and " +
                    "general code quality. If documentation looks adequate, say so explicitly. " +
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
        return "Documentation Agent";
    }
}
