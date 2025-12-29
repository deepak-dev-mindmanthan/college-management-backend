package org.collegemanagement.mapper;

import org.collegemanagement.dto.document.DocumentResponse;
import org.collegemanagement.entity.document.Document;
import org.collegemanagement.entity.student.Student;
import org.collegemanagement.entity.user.User;

public class DocumentMapper {

    /**
     * Convert Document entity to DocumentResponse DTO
     */
    public static DocumentResponse toResponse(Document document) {
        if (document == null) {
            return null;
        }

        DocumentResponse.DocumentResponseBuilder builder = DocumentResponse.builder()
                .uuid(document.getUuid())
                .ownerType(document.getOwnerType())
                .ownerId(null) // Will be set by service layer
                .ownerName(null) // Will be set by service layer
                .documentType(document.getDocumentType())
                .fileName(document.getFileName())
                .fileUrl(document.getFileUrl())
                .contentType(document.getContentType())
                .fileSize(document.getFileSize())
                .uploadedAt(document.getUploadedAt())
                .collegeId(document.getCollege() != null ? document.getCollege().getId() : null);

        return builder.build();
    }

    /**
     * Convert Document entity to DocumentResponse DTO with owner information
     */
    public static DocumentResponse toResponseWithOwner(Document document, String ownerUuid, String ownerName) {
        DocumentResponse response = toResponse(document);
        if (response != null) {
            response.setOwnerId(ownerUuid);
            response.setOwnerName(ownerName);
        }
        return response;
    }

    /**
     * Convert Document entity to DocumentResponse DTO with Student owner information
     */
    public static DocumentResponse toResponseWithStudent(Document document, Student student) {
        String ownerUuid = student != null ? student.getUuid() : null;
        String ownerName = student != null && student.getUser() != null ? student.getUser().getName() : null;
        return toResponseWithOwner(document, ownerUuid, ownerName);
    }
}

