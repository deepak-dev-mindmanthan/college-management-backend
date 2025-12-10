package org.collegemanagement.services;

import org.collegemanagement.entity.Book;

import java.util.List;

public interface BookService {
    Book create(Book book);
    Book update(Book book);
    void delete(Long id);
    Book findById(Long id);
    List<Book> findByCollege(Long collegeId);
}

