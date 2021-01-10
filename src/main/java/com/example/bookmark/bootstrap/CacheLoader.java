package com.example.bookmark.bootstrap;

import com.example.bookmark.domain.BookmarkDto;
import com.example.bookmark.elasticsearch.LocalSearchService;
import com.example.bookmark.model.Bookmark;
import io.quarkus.runtime.StartupEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@ApplicationScoped
public class CacheLoader {
    
    @Inject
    LocalSearchService service;

    public void loadCache(@Observes StartupEvent event) {
        BookmarkDto.streamAll().map(dto -> Bookmark.fromDto((BookmarkDto) dto)).forEach(bookmark -> service.index(bookmark));
    }

}
