package org.collegemanagement.dto.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.collegemanagement.enums.DocumentOwnerType;
import org.collegemanagement.enums.DocumentType;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse {

    private String uuid;
    private DocumentOwnerType ownerType;
    private String ownerId; // UUID of the owner entity (student UUID, etc.)
    private String ownerName; // Name of the owner (student name, etc.)
    private DocumentType documentType;
    private String fileName;
    private String fileUrl;
    private String contentType;
    private Long fileSize;
    private Instant uploadedAt;
    private Long collegeId;
}

