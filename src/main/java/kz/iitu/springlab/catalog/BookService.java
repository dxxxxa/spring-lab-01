package kz.iitu.springlab.catalog;
import kz.iitu.springlab.service.DuplicateTitleException;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import kz.iitu.springlab.service.DuplicateTitleException;

@Service
public class BookService {

    private final BookRepository repository;

    public BookService(BookRepository repository) {
        this.repository = repository;
    }

    public List<Book> findAll(String author) {
        if (author == null || author.isBlank()) {
            return repository.findAll();
        }

        return repository.findAll().stream()
                .filter(book -> book.author()
                        .toLowerCase()
                        .contains(author.toLowerCase()))
                .toList();
    }

    public Optional<Book> findById(long id) {
        return repository.findById(id);
    }

    public Book create(Book book) {
    boolean duplicate = repository.findAll().stream()
            .anyMatch(existing ->
                    existing.title().equalsIgnoreCase(book.title().trim())
                    && existing.author().equalsIgnoreCase(book.author().trim()));

    if (duplicate) {
        throw new DuplicateTitleException(book.title());
    }

    Book normalized = new Book(
            book.id(),
            book.title().trim(),
            book.author().trim(),
            book.year(),
            book.genre(),
            book.createdAt()
    );

    return repository.save(normalized);
}

    public Optional<Book> update(long id, Book book) {
    if (repository.findById(id).isEmpty()) {
        return Optional.empty();
    }

    return Optional.of(
            repository.save(
                    new Book(
                            id,
                            book.title(),
                            book.author(),
                            book.year(),
                            book.genre(),
                            repository.findById(id).get().createdAt()
                    )
            )
    );
}

    public boolean delete(long id) {
        return repository.deleteById(id);
    }
}