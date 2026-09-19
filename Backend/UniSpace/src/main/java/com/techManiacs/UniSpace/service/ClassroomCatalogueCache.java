package com.techManiacs.UniSpace.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techManiacs.UniSpace.model.Classroom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/** Disposable JSON snapshots only; never used for booking conflict checks. */
@Component
public class ClassroomCatalogueCache {
    public static final String KEY = "unispace:classroom-catalogue:v1:available";
    private static final Logger log = LoggerFactory.getLogger(ClassroomCatalogueCache.class);
    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;
    private final boolean enabled;
    private final Duration ttl;

    public ClassroomCatalogueCache(StringRedisTemplate redis, ObjectMapper mapper,
            @Value("${app.classroom-cache.enabled:false}") boolean enabled,
            @Value("${app.classroom-cache.ttl:60s}") Duration ttl) {
        if (ttl.isNegative() || ttl.isZero()) {
            throw new IllegalArgumentException("Classroom cache TTL must be positive");
        }
        this.redis = redis;
        this.mapper = mapper;
        this.enabled = enabled;
        this.ttl = ttl;
    }

    public Optional<List<Classroom>> get() {
        if (!enabled) return Optional.empty();
        try {
            String json = redis.opsForValue().get(KEY);
            return json == null ? Optional.empty()
                    : Optional.of(mapper.readValue(json, new TypeReference<List<Classroom>>() {}));
        } catch (Exception ex) {
            log.debug("Classroom cache read failed; using PostgreSQL", ex);
            return Optional.empty();
        }
    }

    public void put(List<Classroom> classrooms) {
        if (!enabled) return;
        try {
            redis.opsForValue().set(KEY, mapper.writeValueAsString(classrooms), ttl);
        } catch (Exception ex) {
            log.debug("Classroom cache write failed; database response remains available", ex);
        }
    }

    public void evict() {
        if (!enabled) return;
        try {
            redis.delete(KEY);
        } catch (Exception ex) {
            log.debug("Classroom cache eviction failed; TTL bounds catalogue staleness", ex);
        }
    }
}
