package com.techManiacs.UniSpace.migration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class LegacyMongoExportReader {
    private final ObjectMapper objectMapper;

    public LegacyMongoExportReader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public LegacyExport read(Path inputDirectory) throws IOException {
        if (!Files.isDirectory(inputDirectory)) {
            throw new IllegalArgumentException("Migration input directory does not exist: " + inputDirectory);
        }

        Map<LegacyCollection, List<JsonNode>> documents = new EnumMap<>(LegacyCollection.class);
        for (LegacyCollection collection : LegacyCollection.values()) {
            Path source = findSource(inputDirectory, collection);
            documents.put(collection, source == null ? List.of() : readDocuments(source));
        }
        return new LegacyExport(documents);
    }

    private Path findSource(Path inputDirectory, LegacyCollection collection) {
        List<Path> matches = collection.fileNames().stream()
                .map(inputDirectory::resolve)
                .filter(Files::isRegularFile)
                .toList();
        if (matches.size() > 1) {
            throw new IllegalArgumentException("Multiple export files match " + collection.reportName()
                    + ": " + matches);
        }
        return matches.isEmpty() ? null : matches.getFirst();
    }

    private List<JsonNode> readDocuments(Path source) throws IOException {
        String content = Files.readString(source).trim();
        if (content.isEmpty()) {
            return List.of();
        }

        if (content.startsWith("[")) {
            JsonNode root = objectMapper.readTree(content);
            if (!root.isArray()) {
                throw new IllegalArgumentException("Expected a JSON array in " + source);
            }
            List<JsonNode> result = new ArrayList<>();
            root.forEach(result::add);
            return List.copyOf(result);
        }

        List<JsonNode> result = new ArrayList<>();
        int lineNumber = 0;
        for (String line : Files.readAllLines(source)) {
            lineNumber++;
            if (line.isBlank()) {
                continue;
            }
            try {
                result.add(objectMapper.readTree(line));
            } catch (IOException exception) {
                throw new IllegalArgumentException("Invalid JSON in " + source + " at line " + lineNumber, exception);
            }
        }
        return List.copyOf(result);
    }
}
