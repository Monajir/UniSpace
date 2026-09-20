package com.techManiacs.UniSpace.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techManiacs.UniSpace.config.ClassroomCacheListener;
import com.techManiacs.UniSpace.model.Classroom;
import com.techManiacs.UniSpace.repository.BookingRepo;
import com.techManiacs.UniSpace.repository.ClassroomRepo;
import com.techManiacs.UniSpace.repository.RoutineRepo;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ClassroomCatalogueCacheTests {
    private final StringRedisTemplate redis = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    private final ValueOperations<String, String> values = mock(ValueOperations.class);
    private final ClassroomRepo repo = mock(ClassroomRepo.class);
    private final BookingRepo bookingRepo = mock(BookingRepo.class);
    private final RoutineRepo routineRepo = mock(RoutineRepo.class);
    private final ObjectMapper mapper = new ObjectMapper();
    private final Duration ttl = Duration.ofSeconds(60);

    private ClassroomCatalogueCache cache(boolean enabled) {
        when(redis.opsForValue()).thenReturn(values);
        return new ClassroomCatalogueCache(redis, mapper, enabled, ttl);
    }

    private ClassroomService service(ClassroomCatalogueCache cache) {
        return new ClassroomService(repo, cache, bookingRepo, routineRepo);
    }

    private List<Classroom> rooms() {
        return List.of(new Classroom(UUID.randomUUID(), "301", "Academic", 40,
                List.of("Projector"), true));
    }

    @Test
    void cacheHitSkipsDatabaseAndRoundTripsEquipment() throws Exception {
        List<Classroom> expected = rooms();
        ClassroomCatalogueCache cache = cache(true);
        when(values.get(ClassroomCatalogueCache.KEY)).thenReturn(mapper.writeValueAsString(expected));
        assertThat(service(cache).getAllAvailableClassrooms()).isEqualTo(expected);
        verifyNoInteractions(repo);
    }

    @Test
    void missLoadsDatabaseAndWritesJsonWithExpiry() throws Exception {
        List<Classroom> expected = rooms();
        ClassroomCatalogueCache cache = cache(true);
        when(repo.findAllByIsAvailable(true)).thenReturn(expected);
        assertThat(service(cache).getAllAvailableClassrooms()).isEqualTo(expected);
        verify(values).set(ClassroomCatalogueCache.KEY, mapper.writeValueAsString(expected), ttl);
    }

    @Test
    void redisReadAndWriteFailuresDoNotBreakDatabaseResponse() {
        ClassroomCatalogueCache cache = cache(true);
        when(values.get(anyString())).thenThrow(new IllegalStateException("Redis unavailable"));
        doThrow(new IllegalStateException("Redis unavailable")).when(values)
                .set(anyString(), anyString(), any(Duration.class));
        List<Classroom> expected = rooms();
        when(repo.findAllByIsAvailable(true)).thenReturn(expected);
        assertThat(service(cache).getAllAvailableClassrooms()).isEqualTo(expected);
    }

    @Test
    void invalidJsonIsReplacedFromDatabase() {
        ClassroomCatalogueCache cache = cache(true);
        when(values.get(anyString())).thenReturn("invalid JSON");
        when(repo.findAllByIsAvailable(true)).thenReturn(rooms());
        assertThat(service(cache).getAllAvailableClassrooms()).hasSize(1);
        verify(values).set(eq(ClassroomCatalogueCache.KEY), anyString(), eq(ttl));
    }

    @Test
    void disabledCacheNeverContactsRedis() {
        ClassroomCatalogueCache cache = cache(false);
        clearInvocations(redis);
        assertThat(cache.get()).isEmpty();
        cache.put(rooms());
        cache.evict();
        verifyNoInteractions(redis);
    }

    @Test
    void evictionFailureDoesNotThrow() {
        ClassroomCatalogueCache cache = cache(true);
        when(redis.delete(anyString())).thenThrow(new IllegalStateException("Redis unavailable"));
        cache.evict();
    }

    @Test
    void classroomMutationEvictsOnlyAfterCommit() {
        ClassroomCatalogueCache cache = mock(ClassroomCatalogueCache.class);
        TransactionSynchronizationManager.initSynchronization();
        try {
            new ClassroomCacheListener(cache).classroomChanged(new Classroom());
            verifyNoInteractions(cache);
            TransactionSynchronizationManager.getSynchronizations().forEach(sync -> sync.afterCommit());
            verify(cache).evict();
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void rollbackDoesNotEvict() {
        ClassroomCatalogueCache cache = mock(ClassroomCatalogueCache.class);
        TransactionSynchronizationManager.initSynchronization();
        try {
            new ClassroomCacheListener(cache).classroomChanged(new Classroom());
            TransactionSynchronizationManager.getSynchronizations().forEach(sync -> sync.afterCompletion(1));
            verifyNoInteractions(cache);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }
}
