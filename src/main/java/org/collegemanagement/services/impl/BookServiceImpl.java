package org.collegemanagement.services.impl;

import lombok.RequiredArgsConstructor;
import org.collegemanagement.entity.library.LibraryBook;
import org.collegemanagement.exception.ResourceNotFoundException;
import org.collegemanagement.repositories.BookRepository;
import org.collegemanagement.services.BookService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    @Transactional
    @Override
    public LibraryBook create(LibraryBook libraryBook) {
        return bookRepository.save(libraryBook);
    }

    @Transactional
    @Override
    public LibraryBook update(LibraryBook libraryBook) {
        return bookRepository.save(libraryBook);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        bookRepository.deleteById(id);
    }

    @Override
    public LibraryBook findById(Long id) {
        return bookRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
    }

    @Override
    public List<LibraryBook> findByCollege(Long collegeId) {
        return bookRepository.findByCollegeId(collegeId);
    }
}

