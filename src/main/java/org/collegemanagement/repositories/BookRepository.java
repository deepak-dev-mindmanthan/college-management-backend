package org.collegemanagement.repositories;

import org.collegemanagement.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findByCollegeId(Long collegeId);
    Optional<Book> findByIsbnAndCollegeId(String isbn, Long collegeId);
}

