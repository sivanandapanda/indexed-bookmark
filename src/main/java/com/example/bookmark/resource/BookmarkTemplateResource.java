package com.example.bookmark.resource;

import com.example.bookmark.domain.BookmarkDto;
import com.example.bookmark.elasticsearch.LocalSearchService;
import com.example.bookmark.model.Bookmark;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.qute.api.ResourcePath;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.metrics.annotation.Counted;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RequestScoped
@Path("/")
@Produces(MediaType.TEXT_HTML)
public class BookmarkTemplateResource {

    @Inject
    LocalSearchService searchService;

    @Inject
    Template error;

    @Inject
    Template bookmark;

    @Inject
    @ResourcePath("add-bookmark")
    Template addBookmark;

    @Inject
    @ResourcePath("edit-bookmark")
    Template editBookmark;

    @GET
    @Counted
    @Path("/")
    public TemplateInstance listAllFromRoot() {
        return bookmark.data("bookmarks", Collections.emptyList());
    }

    @GET
    @Counted
    @Path("/bookmark")
    public TemplateInstance listAll() {
        return bookmark.data("bookmarks", BookmarkDto.streamAll()
                .map(panacheMongoEntityBase -> (BookmarkDto) panacheMongoEntityBase)
                .map(Bookmark::fromDto).collect(Collectors.toList()));
    }

    @GET
    @Counted
    @Path("bookmark/search")
    public TemplateInstance search(@QueryParam("tag") String tag) {
        List<Bookmark> bookmarks = searchService.searchByTag(tag);
        return bookmark.data("bookmarks", bookmarks).data("filtered", true).data("tag", tag);
    }

    @GET
    @Counted
    @Produces(MediaType.TEXT_HTML)
    @Path("bookmark/{id}")
    public TemplateInstance findById(@PathParam("id") String id) {
        try {
            BookmarkDto loaded = BookmarkDto.findById(new ObjectId(id));

            if (loaded == null) {
                return error.data("error", "Bookmark with id " + id + " does not exist.");
            }

            return bookmark.data("bookmarks", Collections.singletonList(Bookmark.fromDto(loaded)));
        } catch (Exception e) {
            return error.data("error", "Bookmark with id " + id + " does not exist.");
        }
    }

    @GET
    @Counted
    @Produces(MediaType.TEXT_HTML)
    @Path("bookmark/add")
    public TemplateInstance addForm() {
        return addBookmark.data("update", false);
    }

    @GET
    @Counted
    @Produces(MediaType.TEXT_HTML)
    @Path("bookmark/{id}/edit")
    public TemplateInstance updateForm(@PathParam("id") String id) {
        BookmarkDto loaded = BookmarkDto.findById(new ObjectId(id));

        if (loaded == null) {
            return error.data("error", "Bookmark with id " + id + " does not exist.");
        }

        return editBookmark.data("bookmark", Bookmark.fromDto(loaded)).data("update", true);
    }
}
