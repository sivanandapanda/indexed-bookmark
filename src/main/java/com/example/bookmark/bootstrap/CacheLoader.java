package com.example.bookmark.bootstrap;

import com.example.bookmark.domain.BookmarkDto;
import com.example.bookmark.elasticsearch.LocalSearchService;
import com.example.bookmark.model.Bookmark;
import io.quarkus.runtime.StartupEvent;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ApplicationScoped
public class CacheLoader {
    
    @Inject
    LocalSearchService service;

    private ExecutorService executorService;

    public void loadCache(@Observes StartupEvent event) {
        executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> BookmarkDto.streamAll().parallel().map(dto -> Bookmark.fromDto((BookmarkDto) dto)).forEach(bookmark -> service.index(bookmark)));
    }

    @PreDestroy
    public void destroy() {
        if(Objects.nonNull(executorService)) {
            executorService.shutdownNow();
        }
    }

}
