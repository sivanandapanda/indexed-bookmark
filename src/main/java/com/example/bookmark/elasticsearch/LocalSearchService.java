package com.example.bookmark.elasticsearch;

import com.example.bookmark.model.Bookmark;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.Math.toIntExact;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

@ApplicationScoped
public class LocalSearchService {

    private final Map<String, List<Bookmark>> cache = new ConcurrentHashMap<>();

    public void index(Bookmark bookmark) {
        try {
            getAllTags(bookmark)
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .forEach(tag -> {
                        List<Bookmark> list = cache.getOrDefault(tag, new ArrayList<>());
                        if(!list.contains(bookmark)) {
                            list.add(bookmark);
                            cache.put(tag, list);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateIndex(Bookmark old, Bookmark updated) {
        deleteIndex(old);
        index(updated);
    }

    public void deleteIndex(Bookmark bookmark) {
        try {
            getAllTags(bookmark)
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .filter(cache::containsKey)
                    .forEach(tag -> cache.get(tag).removeIf(b -> b.getId().equals(bookmark.getId())));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Bookmark> searchByTag(String tags) {
        try {
            List<String> tagList = Stream.of(tags.split(" ")).map(String::toLowerCase).collect(toList());

            return tagList.stream()
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .map(tag -> cache.containsKey(tag) ? cache.get(tag) : new ArrayList<Bookmark>())
                    .flatMap(List::stream)
                    .distinct()
                    .sorted((b1, b2) -> toIntExact(score(b2, tagList)) - toIntExact(score(b1, tagList)))
                    .collect(toList());
        } catch (Exception e) {
            e.printStackTrace();
            return emptyList();
        }
    }

    private long score(Bookmark bookmark, List<String> list) {
        return getAllTags(bookmark).filter(list::contains).count();
    }

    public List<Bookmark> searchAll() {
        try {
            return cache.values().stream()
                    .flatMap(List::stream)
                    .distinct()
                    .collect(toList());
        } catch (Exception e) {
            e.printStackTrace();
            return emptyList();
        }
    }

    private Stream<String> getAllTags(Bookmark bookmark) {
        return Stream.of(bookmark.getTags().split(","))
                .map(tag -> Stream.of(tag.split(" ")).collect(toList())).flatMap(List::stream)
                .map(tag -> IntStream.rangeClosed(0, tag.length())
                        .boxed()
                        .map(i -> tag.substring(0, i).toLowerCase())
                        .collect(toList()))
                .flatMap(List::stream);
    }
}
