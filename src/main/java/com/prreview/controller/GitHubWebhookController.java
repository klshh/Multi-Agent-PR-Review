package com.prreview.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prreview.service.PrReviewOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
public class GitHubWebhookController {

    private final ObjectMapper objectMapper;
    private final PrReviewOrchestrator prReviewOrchestrator;

    @PostMapping("/github")
    public ResponseEntity<String> handleWebhook(
            @RequestHeader(value = "X-GitHub-Event", defaultValue = "") String event,
            @RequestBody String rawPayload) throws Exception {

        // GitHub sends many event types to the same URL (ping, push, issues...).
        // Only pull_request events carry the PR data we need.
        if (!event.equals("pull_request")) {
            log.info("Ignoring GitHub event: {}", event);
            return ResponseEntity.ok("Ignored event: " + event);
        }

        JsonNode payload = objectMapper.readTree(rawPayload);

        String action = payload.path("action").asText();
        log.info("Received GitHub pull_request webhook, action = {}", action);

        // Only react to a PR being opened or updated with new commits -
        // ignore closed/merged/labeled/etc for now.
        if (!action.equals("opened") && !action.equals("synchronize")) {
            return ResponseEntity.ok("Ignored action: " + action);
        }

        int prNumber = payload.path("pull_request").path("number").asInt();
        String owner = payload.path("repository").path("owner").path("login").asText();
        String repo = payload.path("repository").path("name").asText();

        log.info("PR event: {}/{} #{} - starting review in background", owner, repo, prNumber);

        // The full review (all agents + summary + posting the comment) takes
        // far longer than GitHub's 10-second webhook timeout, so run it in the
        // background and acknowledge the delivery straight away.
        prReviewOrchestrator.runFullReviewAsync(owner, repo, prNumber);

        return ResponseEntity.accepted().body("Review started for PR #" + prNumber);
    }
}
