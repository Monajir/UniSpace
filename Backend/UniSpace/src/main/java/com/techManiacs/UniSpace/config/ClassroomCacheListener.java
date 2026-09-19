package com.techManiacs.UniSpace.config;

import com.techManiacs.UniSpace.service.ClassroomCatalogueCache;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/** Hibernate obtains this listener through Spring's managed bean container. */
@Component
public class ClassroomCacheListener {
    private final ClassroomCatalogueCache cache;

    public ClassroomCacheListener(ClassroomCatalogueCache cache) {
        this.cache = cache;
    }

    @PostPersist
    @PostUpdate
    @PostRemove
    public void classroomChanged(Object classroom) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    cache.evict();
                }
            });
        } else {
            cache.evict();
        }
    }
}
