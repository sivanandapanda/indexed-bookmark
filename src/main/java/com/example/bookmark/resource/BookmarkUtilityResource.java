package com.example.bookmark.resource;

import com.example.bookmark.jsoup.BookmarkParser;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("util")
@ApplicationScoped
public class BookmarkUtilityResource {

    @Inject
    BookmarkParser bookmarkParser;

    @GET
    @Path("bookmark/import")
    public void saveAdd() {
        //bookmarkParser.load();
    }
}
