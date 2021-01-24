package com.example.bookmark.model;

import com.example.bookmark.domain.BookmarkDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Bookmark {
    private String id;
    private String url;
    private String name;
    private String tags;
    private String userName;
    private String creationTime;
    private String updationTime;

    public static Bookmark fromDto(BookmarkDto bookmarkDto) {
        if (Objects.nonNull(bookmarkDto)) {
            return Bookmark.builder().id(bookmarkDto.getId().toHexString()).name(bookmarkDto.getName())
                    .tags(String.join(",", bookmarkDto.getTags())).url(bookmarkDto.getUrl())// .userName(bookmarkDto.getUserName())
                    .creationTime(
                            bookmarkDto.getCreationTime().format(DateTimeFormatter.ofPattern("dd MMM yyyy hh:mm:ss")))
                    .updationTime(bookmarkDto.getUpdationTime() != null
                            ? bookmarkDto.getUpdationTime().format(DateTimeFormatter.ofPattern("dd MMM yyyy hh:mm:ss"))
                            : null)
                    .build();
        } else {
            return null;
        }
    }
}
