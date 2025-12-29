package org.collegemanagement.repositories;

import org.collegemanagement.entity.document.Document;
import org.collegemanagement.enums.DocumentOwnerType;
import org.collegemanagement.enums.DocumentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    /**
     * Find document by UUID and college ID (college isolation)
     */
    @Query("""
            SELECT d FROM Document d
            WHERE d.uuid = :uuid
            AND d.college.id = :collegeId
            """)
    Optional<Document> findByUuidAndCollegeId(@Param("uuid") String uuid, @Param("collegeId") Long collegeId);

    /**
     * Find all documents by college ID with pagination
     */
    @Query("""
            SELECT d FROM Document d
            WHERE d.college.id = :collegeId
            ORDER BY d.uploadedAt DESC
            """)
    Page<Document> findAllByCollegeId(@Param("collegeId") Long collegeId, Pageable pageable);

    /**
     * Find documents by owner type, owner ID, and college ID
     */
    @Query("""
            SELECT d FROM Document d
            WHERE d.ownerType = :ownerType
            AND d.ownerId = :ownerId
            AND d.college.id = :collegeId
            ORDER BY d.uploadedAt DESC
            """)
    Page<Document> findByOwnerTypeAndOwnerIdAndCollegeId(
            @Param("ownerType") DocumentOwnerType ownerType,
            @Param("ownerId") Long ownerId,
            @Param("collegeId") Long collegeId,
            Pageable pageable
    );

    /**
     * Find documents by owner type and college ID
     */
    @Query("""
            SELECT d FROM Document d
            WHERE d.ownerType = :ownerType
            AND d.college.id = :collegeId
            ORDER BY d.uploadedAt DESC
            """)
    Page<Document> findByOwnerTypeAndCollegeId(
            @Param("ownerType") DocumentOwnerType ownerType,
            @Param("collegeId") Long collegeId,
            Pageable pageable
    );

    /**
     * Find documents by document type and college ID
     */
    @Query("""
            SELECT d FROM Document d
            WHERE d.documentType = :documentType
            AND d.college.id = :collegeId
            ORDER BY d.uploadedAt DESC
            """)
    Page<Document> findByDocumentTypeAndCollegeId(
            @Param("documentType") DocumentType documentType,
            @Param("collegeId") Long collegeId,
            Pageable pageable
    );

    /**
     * Find documents by owner type, owner ID, document type, and college ID
     */
    @Query("""
            SELECT d FROM Document d
            WHERE d.ownerType = :ownerType
            AND d.ownerId = :ownerId
            AND d.documentType = :documentType
            AND d.college.id = :collegeId
            ORDER BY d.uploadedAt DESC
            """)
    Page<Document> findByOwnerTypeAndOwnerIdAndDocumentTypeAndCollegeId(
            @Param("ownerType") DocumentOwnerType ownerType,
            @Param("ownerId") Long ownerId,
            @Param("documentType") DocumentType documentType,
            @Param("collegeId") Long collegeId,
            Pageable pageable
    );

    /**
     * Count documents by owner type and owner ID
     */
    @Query("""
            SELECT COUNT(d) FROM Document d
            WHERE d.ownerType = :ownerType
            AND d.ownerId = :ownerId
            AND d.college.id = :collegeId
            """)
    long countByOwnerTypeAndOwnerIdAndCollegeId(
            @Param("ownerType") DocumentOwnerType ownerType,
            @Param("ownerId") Long ownerId,
            @Param("collegeId") Long collegeId
    );
}

