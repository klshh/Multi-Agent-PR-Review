package com.prreview.agents;

import java.util.concurrent.CompletableFuture;

public interface ReviewAgent {

    CompletableFuture<String> review(String diff);

    String getAgentName();
}
