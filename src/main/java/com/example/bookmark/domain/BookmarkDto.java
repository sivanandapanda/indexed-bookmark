package com.example.bookmark.domain;

import com.example.bookmark.model.Bookmark;
import io.quarkus.mongodb.panache.MongoEntity;
import io.quarkus.mongodb.panache.PanacheMongoEntity;
import lombok.Getter;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Getter
@MongoEntity(collection = "bookmark")
public class BookmarkDto extends PanacheMongoEntity {

    public String name;
    public String url;
    public String userName;
    public List<String> tags;
    public LocalDateTime creationTime;
    public LocalDateTime updationTime;

    public ObjectId getId() {
        return super.id;
    }

    public BookmarkDto update(Bookmark bookmark) {
        if (Objects.nonNull(bookmark)) {
            boolean isUpdated = false;
            if (hasChanged(bookmark.getName(), this.name)) {
                this.name = bookmark.getName();
                isUpdated = true;
            }

            if (hasChanged(bookmark.getUrl(), this.url)) {
                this.url = bookmark.getUrl();
                isUpdated = true;
            }

            if (hasChanged(bookmark.getTags(), this.tags)) {
                this.tags = Arrays.asList(bookmark.getTags().split(","));
                isUpdated = true;
            }

            if (isUpdated) {
                updationTime = LocalDateTime.now();
                update();
            }
        }
        return this;
    }

    private boolean hasChanged(Object obj, Object toCompare) {
        return Objects.nonNull(obj) && !toCompare.equals(obj);
    }

    public static BookmarkDto createFromModel(Bookmark bookmark) {
        BookmarkDto dto = new BookmarkDto();

        dto.name = bookmark.getName();
        dto.tags = Arrays.asList(bookmark.getTags().split(","));
        dto.url = bookmark.getUrl();
        dto.creationTime = LocalDateTime.now();
        return dto;
    }
}
