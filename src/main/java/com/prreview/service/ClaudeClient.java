package com.prreview.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prreview.config.exception.ClaudeApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;


/**
 * Thin wrapper around the Anthropic Messages API.
 * Every "agent" in this project is really just this same client called
 * with a different system prompt - that's the whole trick behind
 * "multi-agent": specialization through prompts, not separate infrastructure.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ClaudeClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${claude.api.key}")
    private String apiKey;

    @Value("${claude.api.url}")
    private String apiUrl;

    @Value("${claude.api.model}")
    private String model;

    @Value("${claude.api.version}")
    private String apiVersion;

    @Value("${claude.api.max-tokens}")
    private int maxTokens;

    /**
     * Sends one message to Claude with the given system prompt and returns
     * the plain-text reply.
     *
     * @param systemPrompt defines the agent's role/persona (e.g. "You are a
     *                      security-focused code reviewer...")
     * @param userContent   the actual diff/code the agent should look at
     */
    public String sendMessage(String systemPrompt, String userContent) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ClaudeApiException(
                    "ANTHROPIC_API_KEY is not set. Export it as an environment variable before starting the app.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);
        headers.set("anthropic-version", apiVersion);

        Map<String, Object> body = Map.of(
                "model", model,
                "max_tokens", maxTokens,
                "system", systemPrompt,
                "messages", List.of(
                        Map.of("role", "user", "content", userContent)
                )
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            String rawResponse = restTemplate.postForObject(apiUrl, request, String.class);
            return extractText(rawResponse);
        } catch (HttpStatusCodeException ex) {
            // 4xx/5xx from Anthropic - log the body, it usually explains exactly what's wrong
            // (bad key, rate limit, invalid model name, out of credits, etc.)
            HttpStatus status = ex.getStatusCode();
//            log.error("Claude API returned {}: {}", status, ex.getResponseBodyAsString());
            throw new ClaudeApiException(
                    "Claude API call failed with status " + status + ": " + ex.getResponseBodyAsString(), ex);
        } catch (ResourceAccessException ex) {
//            log.error("Claude API call timed out or network error", ex);
            throw new ClaudeApiException("Could not reach Claude API (timeout/network issue)", ex);
        }
    }

    private String extractText(String rawResponse) {
        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            JsonNode contentArray = root.path("content");
            if (contentArray.isArray() && !contentArray.isEmpty()) {
                return contentArray.get(0).path("text").asText();
            }
            throw new ClaudeApiException("Claude response had no content: " + rawResponse);
        } catch (Exception e) {
            throw new ClaudeApiException("Failed to parse Claude response: " + rawResponse, e);
        }
    }
}
