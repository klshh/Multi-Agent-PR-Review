package com.prreview.agents;

import com.prreview.dto.AgentResult;
import com.prreview.service.ClaudeClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
public class SummaryAgentService{

    private static final String SYSTEM_PROMPT =
            "You are a senior engineer producing a final, consolidated code review from four " +
                    "specialist reviewers' notes below (security, quality, tests, documentation). " +
                    "Merge their findings into one concise review: " +
                    "- Group related points, don't just repeat each section verbatim " +
                    "- Prioritize security and correctness issues above style/doc issues " +
                    "- If two agents disagree or overlap, reconcile it in your own words " +
                    "- End with exactly one line: \"Verdict: Approve\" or \"Verdict: Changes requested\"";

    private final ClaudeClient claudeClient;

    public String summarize(List<AgentResult> agentResults) {
        String combinedInput = buildCombinedInput(agentResults);
        return claudeClient.sendMessage(SYSTEM_PROMPT, combinedInput);
    }

    private String buildCombinedInput(List<AgentResult> agentResults) {
        StringBuilder sb = new StringBuilder();
        for (AgentResult result : agentResults) {
            sb.append(result.getAgentName())
                    .append(":\n")
                    .append(result.getReviewText())
                    .append("\n\n");
        }
        return sb.toString();
    }


}
