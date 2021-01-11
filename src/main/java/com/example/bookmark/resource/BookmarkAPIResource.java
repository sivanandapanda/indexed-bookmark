package com.example.bookmark.resource;

import com.example.bookmark.domain.BookmarkDto;
import com.example.bookmark.elasticsearch.LocalSearchService;
import com.example.bookmark.model.Bookmark;
import io.quarkus.qute.Template;
import io.vertx.core.http.HttpServerRequest;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;
import java.util.Objects;

@Path("/bookmark/api")
@Consumes(MediaType.APPLICATION_JSON)
public class BookmarkAPIResource {

    @Inject
    LocalSearchService searchService;

    @Context
    HttpServerRequest request;

    @Inject
    Template error;

    @GET
    @Counted
    @Path("from-db")
    @Produces(MediaType.APPLICATION_JSON)
    public List<BookmarkDto> listAllFromDb() {
        return BookmarkDto.listAll();
    }

    @POST
    @Counted
    public Response save(@RequestBody Bookmark bookmark) {

        BookmarkDto bookmarkDto = BookmarkDto.createFromModel(bookmark);
        BookmarkDto.persist(bookmarkDto);
        searchService.index(Objects.requireNonNull(Bookmark.fromDto(bookmarkDto)));

        printURIDetails(bookmarkDto);

        return Response.status(Response.Status.MOVED_PERMANENTLY).location(URI.create("/bookmark/" + bookmarkDto.getId().toString())).build();
    }

    private void printURIDetails(BookmarkDto bookmarkDto) {
        System.out.println("Absolute URI => " + request.absoluteURI());
        System.out.println("URI => " + request.uri());
        System.out.println("Host => " + request.host());
        System.out.println("Scheme => " + request.scheme());
        System.out.println("Redirect URI => " + URI.create("/bookmark/" + bookmarkDto.getId().toString()));
    }

    @PATCH
    @Counted
    @Path("{id}")
    public Object update(@PathParam("id") String id, @RequestBody Bookmark bookmark) {
        BookmarkDto bookmarkDto = BookmarkDto.findById(new ObjectId(id));
        if (bookmarkDto == null) {
            return error.data("error", "Bookmark with id " + id + " has been deleted after loading this form.");
        }

        BookmarkDto updatedBookNarkDto = bookmarkDto.update(bookmark);
        searchService.updateIndex(Bookmark.fromDto(bookmarkDto), Bookmark.fromDto(updatedBookNarkDto));

        printURIDetails(bookmarkDto);

        return Response.status(Response.Status.MOVED_PERMANENTLY).location(URI.create("/bookmark/" + bookmarkDto.getId().toString())).build();
    }

    @DELETE
    @Counted
    @Path("{id}")
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
    @Path("delete/all")
    public void deleteAll() {
        BookmarkDto.streamAll().forEach(bookmark -> {
            bookmark.delete();
            searchService.deleteIndex(Bookmark.fromDto((BookmarkDto) bookmark));
        });
    }
}
