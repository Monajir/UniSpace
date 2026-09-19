package com.techManiacs.UniSpace.config;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/** Covers collection-only edits and bulk repository deletes that bypass JPA callbacks. */
@Aspect
@Component
public class ClassroomRepositoryCacheAdvice {
    private final ClassroomCacheListener listener;

    public ClassroomRepositoryCacheAdvice(ClassroomCacheListener listener) {
        this.listener = listener;
    }

    @AfterReturning("bean(classroomRepo) && (execution(* save*(..)) || execution(* delete*(..)))")
    public void repositoryChanged() {
        listener.classroomChanged(null);
    }
}
