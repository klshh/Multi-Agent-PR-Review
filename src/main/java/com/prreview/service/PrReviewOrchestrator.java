package com.prreview.service;

import com.prreview.agents.ReviewAgent;
import com.prreview.agents.SummaryAgentService;
import com.prreview.dto.AgentResult;
import com.prreview.dto.PrReviewSummary;
import com.prreview.model.ReviewRecord;
import com.prreview.repository.ReviewRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrReviewOrchestrator {

    private final GitHubService gitHubService;
    private final List<ReviewAgent> reviewAgents;   // Spring auto-injects all 4 implementations
    private final SummaryAgentService summaryAgent; // doesn't implement ReviewAgent - stays separate
    private final ReviewRecordRepository repository;

    /**
     * Fire-and-forget version of runFullReview for the webhook. Nobody is
     * waiting on the result, so failures are logged here - otherwise an
     * exception thrown on the background thread would disappear silently.
     */
    @Async("reviewTaskExecutor")
    public void runFullReviewAsync(String owner, String repo, int prNumber) {
        try {
            runFullReview(owner, repo, prNumber);
        } catch (Exception e) {
            log.error("Background review failed for {}/{} PR #{}", owner, repo, prNumber, e);
        }
    }

    public PrReviewSummary runFullReview(String owner, String repo, int prNumber) {
        long startTime = System.currentTimeMillis();

        String diff = gitHubService.fetchCombinedDiff(owner, repo, prNumber);

        // Step 1: start ALL agents at once - however many there are, without
        // naming them individually. Each .review() call kicks off async work
        // and returns immediately.
        List<CompletableFuture<AgentResult>> futures = reviewAgents.stream()
                .map(agent -> agent.review(diff)
                        .thenApply(result -> new AgentResult(agent.getAgentName(), result)))
                .collect(Collectors.toList());

        // Step 2: wait for all of them together.
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // Step 3: now every future is done, .join() on each returns instantly.
        List<AgentResult> agentResults = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

//        log.info("All {} agents completed in {} ms", reviewAgents.size(),
//                System.currentTimeMillis() - startTime);

        String finalSummary = summaryAgent.summarize(agentResults);

        ReviewRecord saved = repository.save(
                ReviewRecord.builder()
                        .diffContent(diff)
                        .reviewResult(finalSummary)
                        .build()
        );


        try {
            gitHubService.postComment(owner, repo, prNumber, finalSummary);
        } catch (Exception e) {
            // Deliberately don't let a GitHub posting failure erase a review
            // we already generated and saved - log it, still return the
            // result to the caller.
            log.error("Review completed but failed to post comment on {}/{} PR #{}",
                    owner, repo, prNumber, e);
        }

        log.info("Full PR review completed in {} ms total", System.currentTimeMillis() - startTime);

        return new PrReviewSummary(
                saved.getId(), owner, repo, prNumber,
                agentResults, finalSummary, saved.getCreatedAt()
        );
    }
}