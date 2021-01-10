package com.example.bookmark.resource;

import com.example.bookmark.domain.BookmarkDto;
import com.example.bookmark.elasticsearch.LocalSearchService;
import com.example.bookmark.model.Bookmark;
import com.example.bookmark.model.BookmarkForm;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.qute.api.ResourcePath;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RequestScoped
@Path("/")
@Produces(MediaType.TEXT_HTML)
public class BookmarkResource {

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
        return bookmark.data("bookmarks", listAllFromDb().stream().map(Bookmark::fromDto).collect(Collectors.toList()));
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

    @GET
    @Counted
    @Path("bookmark/from-db")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public List<BookmarkDto> listAllFromDb() {
        return BookmarkDto.listAll();
    }

    @POST
    @Counted
    @Path("bookmark")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response save(@MultipartForm BookmarkForm bookmark) {
        BookmarkDto bookmarkDto = BookmarkDto.createFromModel(bookmark);
        BookmarkDto.persist(bookmarkDto);
        searchService.index(Objects.requireNonNull(Bookmark.fromDto(bookmarkDto)));

        return Response.status(Response.Status.MOVED_PERMANENTLY).location(URI.create("/bookmark/" + bookmarkDto.getId().toString())).build();
    }

    @POST
    @Counted
    @Path("bookmark/{id}/edit")
    public Object update(@PathParam("id") String id, @MultipartForm BookmarkForm bookmark) {
        BookmarkDto bookmarkDto = BookmarkDto.findById(new ObjectId(id));
        /*
         * if(!identity.getPrincipal().getName().equals(bookmarkDto.getUserName())) {
         * throw new ResponseStatusException("Not allowed to update",
         * Response.Status.FORBIDDEN); }
         */
        if (bookmarkDto == null) {
            return error.data("error", "Bookmark with id " + id + " has been deleted after loading this form.");
        }

        BookmarkDto updatedBookNarkDto = bookmarkDto.update(bookmark);
        searchService.updateIndex(Bookmark.fromDto(bookmarkDto), Bookmark.fromDto(updatedBookNarkDto));

        return Response.status(Response.Status.MOVED_PERMANENTLY).location(URI.create("/bookmark/" + bookmarkDto.getId().toString())).build();
    }

    @POST
    @Counted
    @Path("bookmark/{id}/delete")
    public Object delete(@PathParam("id") String id) {
        BookmarkDto bookmarkDto = BookmarkDto.findById(new ObjectId(id));

        if (bookmarkDto == null) {
            return error.data("error", "Bookmark with id " + id + " has been deleted after loading this form.");
        }

        bookmarkDto.delete();
        searchService.deleteIndex(Bookmark.fromDto(bookmarkDto));

        return Response.status(Response.Status.MOVED_PERMANENTLY).location(URI.create("/")).build();

    }

    @DELETE
    @Counted
    @Path("bookmark")
    public void deleteAll() {
        BookmarkDto.streamAll().forEach(bookmark -> {
            bookmark.delete();
            searchService.deleteIndex(Bookmark.fromDto((BookmarkDto) bookmark));
        });
    }
}
