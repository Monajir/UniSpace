package com.techManiacs.UniSpace.migration;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

public record LegacyExport(Map<LegacyCollection, List<JsonNode>> documents) {
    public List<JsonNode> documents(LegacyCollection collection) {
        return documents.getOrDefault(collection, List.of());
    }
}
