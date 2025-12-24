package org.collegemanagement.entity.document;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.collegemanagement.entity.tenant.College;
import org.collegemanagement.entity.base.BaseEntity;
import org.collegemanagement.enums.DocumentOwnerType;
import org.collegemanagement.enums.DocumentType;

import java.time.Instant;

@Entity
@Table(
        name = "documents",
        indexes = {
                @Index(name = "idx_document_college", columnList = "college_id"),
                @Index(name = "idx_document_owner", columnList = "owner_type, owner_id"),
                @Index(name = "idx_document_type", columnList = "document_type"),
                @Index(name = "idx_document_uploaded", columnList = "uploaded_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Document extends BaseEntity {

    /**
     * Tenant (College / School)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "college_id", nullable = false)
    private College college;

    /**
     * Owner type (STUDENT / STAFF / PARENT / OTHER)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", nullable = false, length = 30)
    private DocumentOwnerType ownerType;

    /**
     * ID of the owner entity
     * (student_id OR staff_profile_id)
     */
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    /**
     * File storage URL (S3, GCS, Azure Blob, etc.)
     */
    @Column(name = "file_url", nullable = false, length = 1000)
    private String fileUrl;

    /**
     * Logical document classification
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 50)
    private DocumentType documentType;

    /**
     * Original uploaded filename
     */
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    /**
     * MIME type (application/pdf, image/png)
     */
    @Column(name = "content_type", length = 100)
    private String contentType;

    /**
     * File size in bytes
     */
    @Column(name = "file_size")
    private Long fileSize;

    /**
     * Upload timestamp
     */
    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt;

    @PrePersist
    protected void onCreate() {
        if (this.uploadedAt == null) {
            this.uploadedAt = Instant.now();
        }
    }
}

