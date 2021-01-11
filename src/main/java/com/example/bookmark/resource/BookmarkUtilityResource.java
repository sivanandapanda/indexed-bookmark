package com.example.bookmark.resource;

import com.example.bookmark.jsoup.BookmarkParser;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("bookmark/util")
@ApplicationScoped
public class BookmarkUtilityResource {

    @Inject
    BookmarkParser bookmarkParser;

    @GET
    @Path("import")
    public void saveAdd() {
        //bookmarkParser.load();
    }
}
