package com.example.bookmark.resource;

import com.example.bookmark.domain.BookmarkDto;
import com.example.bookmark.elasticsearch.LocalSearchService;
import com.example.bookmark.model.Bookmark;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

import javax.inject.Inject;
import javax.ws.rs.*;
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

        return Response.created(URI.create("/bookmark/" + bookmarkDto.getId().toString())).build();
    }

    @PATCH
    @Counted
    @Path("{id}")
    public Response update(@PathParam("id") String id, @RequestBody Bookmark bookmark) {
        BookmarkDto bookmarkDto = BookmarkDto.findById(new ObjectId(id));
        if (bookmarkDto == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        Bookmark oldBookMark = Bookmark.fromDto(bookmarkDto);
        Bookmark updatedBookMark = Bookmark.fromDto(bookmarkDto.update(bookmark));

        searchService.updateIndex(oldBookMark, updatedBookMark);

        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @DELETE
    @Counted
    @Path("{id}")
    public Response delete(@PathParam("id") String id) {
        BookmarkDto bookmarkDto = BookmarkDto.findById(new ObjectId(id));

        if (bookmarkDto == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
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
