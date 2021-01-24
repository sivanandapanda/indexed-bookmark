package com.example.bookmark.elasticsearch;

import com.example.bookmark.model.Bookmark;
import io.quarkus.redis.client.RedisClient;
import io.vertx.redis.client.Response;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.lang.Math.toIntExact;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

@ApplicationScoped
public class LocalSearchService {

    @Inject
    Jsonb jsonb;

    @Inject
    RedisClient redisClient;

    private static final Type BOOKMARK_LIST_TYPE = new ArrayList<Bookmark>() {}.getClass().getGenericSuperclass();
    private static final List<String> ignoredStrings = Arrays.asList(",", ".", "", " ", "-", "--", "=", "'", "\"", "!", "|", "/", ";", "@", "#",
            "$", "%", "*");

    public void index(Bookmark bookmark) {
        try {
            getAllTags(bookmark).map(String::trim).map(String::toLowerCase)
                    .forEach(tag -> {
                        Response response = redisClient.get(tag);

                        if(Objects.isNull(response)) {
                            redisClient.set(Arrays.asList(tag, jsonb.toJson(singletonList(bookmark))));
                        } else {
                            List<Bookmark> bookmarks = jsonb.fromJson(response.toString(), BOOKMARK_LIST_TYPE);
                            if(!bookmarks.contains(bookmark)) {
                                bookmarks.add(bookmark);
                                redisClient.set(Arrays.asList(tag, jsonb.toJson(bookmarks)));
                            }
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
            getAllTags(bookmark).map(String::trim).map(String::toLowerCase)
                    .forEach(tag -> {
                        Response response = redisClient.get(tag);

                        if(Objects.nonNull(response)) {
                            List<Bookmark> bookmarks = jsonb.fromJson(response.toString(), BOOKMARK_LIST_TYPE);
                            boolean remove = bookmarks.removeIf(b -> b.getId().equals(bookmark.getId()));
                            if(remove) {
                                redisClient.set(Arrays.asList(tag, jsonb.toJson(bookmarks)));
                            }
                        }
                    });
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
                    .map(tag -> {
                        Response response = redisClient.get(tag);
                        if(Objects.nonNull(response)) {
                            return jsonb.fromJson(response.toString(), BOOKMARK_LIST_TYPE);
                        } else {
                            return new ArrayList<Bookmark>();
                        }
                    })
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
        long score = getAllTags(bookmark).filter(list::contains).count();
        System.out.println("bookmark = " + bookmark + ", list = " + list + ", score = " + score);
        return score;
    }

    public List<Bookmark> searchAll() {
        try {
            Response response = redisClient.keys("*");
            return jsonb.fromJson(response.toString(), BOOKMARK_LIST_TYPE); //todo test

            //return cache.values().stream().flatMap(List::stream).distinct().collect(toList());
            //return Collections.emptyList();
        } catch (Exception e) {
            e.printStackTrace();
            return emptyList();
        }
    }

    private Stream<String> getAllTags(Bookmark bookmark) {
        return Stream.of(bookmark.getTags().split(" ")).parallel()
                .filter(tag -> !(ignoredStrings.contains(tag)))
                .map(this::getAllStrings)
                .flatMap(List::parallelStream);
    }

    private List<String> getAllStrings(String str) {
        List<String> strings = new ArrayList<>();

        for (int i = 0; i < str.length(); i++) {
            for (int j = i; j <= str.length(); j++) {
                if(j > i) {
                    strings.add(str.substring(i, j));
                }
            }
        }

        return strings;
    }
}
