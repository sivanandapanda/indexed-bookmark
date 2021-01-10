package com.example.bookmark.model;

import javax.ws.rs.FormParam;

public class BookmarkForm {

    private @FormParam("url") String url;
    private @FormParam("name") String name;
    private @FormParam("tags") String tags;

    public String getUrl() {
        return url;
    }

    public BookmarkForm setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getName() {
        return name;
    }

    public BookmarkForm setName(String name) {
        this.name = name;
        return this;
    }

    public String getTags() {
        return tags;
    }

    public BookmarkForm setTags(String tags) {
        this.tags = tags;
        return this;
    }
}
