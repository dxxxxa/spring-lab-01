
package kz.iitu.springlab.web;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.ProblemDetail;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import kz.iitu.springlab.catalog.Book;
import kz.iitu.springlab.catalog.BookService;
import kz.iitu.springlab.service.BookNotFoundException;
import kz.iitu.springlab.web.dto.BookMapper;
import kz.iitu.springlab.web.dto.BookRequest;
import kz.iitu.springlab.web.dto.BookResponse;

@RestController
@RequestMapping("/api/books")
@Tag(name = "Books", description = "REST API for managing books")
public class BookApiController {

    private final BookService service;
    private final BookMapper mapper;

    public BookApiController(BookService service, BookMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    private EntityModel<BookResponse> toModel(Book book) {
        BookResponse response = mapper.toResponse(book);

        return EntityModel.of(response,
                linkTo(methodOn(BookApiController.class).find(book.id())).withSelfRel(),
                linkTo(methodOn(BookApiController.class).update(
                        book.id(), null)).withRel("update"),
                linkTo(methodOn(BookApiController.class).delete(book.id())).withRel("delete"),
                linkTo(methodOn(BookApiController.class).list(null)).withRel("collection"));
    }
@Operation(summary = "Get all books", description = "Returns all books with HATEOAS links")
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "Books retrieved successfully")
})
    @GetMapping
    public CollectionModel<EntityModel<BookResponse>> list(
            @RequestParam(required = false) String author) {

        List<EntityModel<BookResponse>> books = mapper.toResponseList(service.findAll(author))
                .stream()
                .map(response -> EntityModel.of(response,
                        linkTo(methodOn(BookApiController.class).find(response.id())).withSelfRel()))
                .toList();

        return CollectionModel.of(books,
                linkTo(methodOn(BookApiController.class).list(author)).withSelfRel());
    }

    @Operation(summary = "Get book by ID", description = "Returns a single book with HATEOAS links")
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "Book found"),
    @ApiResponse(responseCode = "404", description = "Book not found",
        content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
})
    @GetMapping("/{id}")
    public EntityModel<BookResponse> find(@PathVariable long id) {
        Book book = service.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
        return toModel(book);
    }

    @Operation(summary = "Create a book", description = "Creates a new book")
@ApiResponses({
    @ApiResponse(responseCode = "201", description = "Book created successfully"),
    @ApiResponse(responseCode = "400", description = "Validation error",
        content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
    @ApiResponse(responseCode = "409", description = "Duplicate book",
        content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
})
    @PostMapping
    public ResponseEntity<EntityModel<BookResponse>> create(
            @Valid @RequestBody BookRequest request) {

        Book saved = service.create(mapper.toDomain(request));
        EntityModel<BookResponse> model = toModel(saved);

        return ResponseEntity
                .created(URI.create("/api/books/" + saved.id()))
                .body(model);
    }

    @Operation(summary = "Update a book", description = "Updates an existing book")
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "Book updated successfully"),
    @ApiResponse(responseCode = "400", description = "Validation error"),
    @ApiResponse(responseCode = "404", description = "Book not found")
})
    @PutMapping("/{id}")
    public EntityModel<BookResponse> update(
            @PathVariable long id,
            @Valid @RequestBody BookRequest request) {

        Book updated = service.update(id, mapper.toDomain(request))
                .orElseThrow(() -> new BookNotFoundException(id));

        return toModel(updated);
    }

    @Operation(summary = "Delete a book", description = "Deletes a book by ID")
@ApiResponses({
    @ApiResponse(responseCode = "204", description = "Book deleted successfully"),
    @ApiResponse(responseCode = "404", description = "Book not found")
})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        if (!service.delete(id)) {
            throw new BookNotFoundException(id);
        }
        return ResponseEntity.noContent().build();
    }
}
