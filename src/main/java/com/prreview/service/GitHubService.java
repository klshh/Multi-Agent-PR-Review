package com.prreview.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class GitHubService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${github.token}")
    private String githubToken;

    /**
     * Fetches every changed file in the PR and combines their patches into
     * one diff string to hand to the agents.
     *
     * Note: GitHub paginates this endpoint (default 30 files per page) -
     * fine for small test PRs, but a real PR with many files would need
     * pagination handling. Left simple on purpose for now.
     */
    public String fetchCombinedDiff(String owner, String repo, int prNumber) {
        String url = String.format(
                "https://api.github.com/repos/%s/%s/pulls/%d/files",
                owner, repo, prNumber
        );

        HttpEntity<Void> request = new HttpEntity<>(buildHeaders());
        String rawResponse = restTemplate.exchange(url, HttpMethod.GET, request, String.class).getBody();

        return extractCombinedPatch(rawResponse);
    }

    public void postComment(String owner, String repo, int prNumber, String commentBody) {
        String url = String.format(
                "https://api.github.com/repos/%s/%s/issues/%d/comments",
                owner, repo, prNumber
        );

        HttpHeaders headers = buildHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = Map.of("body", commentBody);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        restTemplate.postForObject(url, request, String.class);
//        log.info("Posted review comment on {}/{} PR #{}", owner, repo, prNumber);
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + githubToken);
        headers.set("Accept", "application/vnd.github+json");
        return headers;
    }

    private String extractCombinedPatch(String rawResponse) {
        try {
            JsonNode filesArray = objectMapper.readTree(rawResponse);
            StringBuilder combined = new StringBuilder();

            for (JsonNode file : filesArray) {
                String filename = file.path("filename").asText();
                String patch = file.path("patch").asText("");

                if (!patch.isEmpty()) {
                    combined.append("File: ").append(filename).append("\n")
                            .append(patch).append("\n\n");
                }
            }
            return combined.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse GitHub PR files response: " + rawResponse, e);
        }
    }
}