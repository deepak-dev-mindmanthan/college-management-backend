package org.collegemanagement.repositories;

import org.collegemanagement.entity.student.ParentStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ParentStudentRepository extends JpaRepository<ParentStudent, Long> {

    /**
     * Find ParentStudent by parent ID and student ID
     */
    @Query("""
            SELECT ps FROM ParentStudent ps
            WHERE ps.parent.id = :parentId
            AND ps.student.id = :studentId
            """)
    Optional<ParentStudent> findByParentIdAndStudentId(@Param("parentId") Long parentId, @Param("studentId") Long studentId);

    /**
     * Find all ParentStudent by student ID
     */
    @Query("""
            SELECT ps FROM ParentStudent ps
            WHERE ps.student.id = :studentId
            """)
    List<ParentStudent> findByStudentId(@Param("studentId") Long studentId);

    /**
     * Find all ParentStudent by parent ID
     */
    @Query("""
            SELECT ps FROM ParentStudent ps
            WHERE ps.parent.id = :parentId
            """)
    List<ParentStudent> findByParentId(@Param("parentId") Long parentId);

    /**
     * Check if ParentStudent exists by parent ID and student ID
     */
    @Query("""
            SELECT COUNT(ps) > 0 FROM ParentStudent ps
            WHERE ps.parent.id = :parentId
            AND ps.student.id = :studentId
            """)
    boolean existsByParentIdAndStudentId(@Param("parentId") Long parentId, @Param("studentId") Long studentId);
}

