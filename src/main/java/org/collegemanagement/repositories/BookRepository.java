package org.collegemanagement.repositories;

import org.collegemanagement.entity.library.LibraryBook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<LibraryBook, Long> {
    List<LibraryBook> findByCollegeId(Long collegeId);
    Optional<LibraryBook> findByIsbnAndCollegeId(String isbn, Long collegeId);
}

