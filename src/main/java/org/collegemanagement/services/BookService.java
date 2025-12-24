package org.collegemanagement.services;

import org.collegemanagement.entity.library.LibraryBook;

import java.util.List;

public interface BookService {
    LibraryBook create(LibraryBook libraryBook);
    LibraryBook update(LibraryBook libraryBook);
    void delete(Long id);
    LibraryBook findById(Long id);
    List<LibraryBook> findByCollege(Long collegeId);
}

