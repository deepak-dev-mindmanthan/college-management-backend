package org.collegemanagement.dto.document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.collegemanagement.enums.DocumentOwnerType;
import org.collegemanagement.enums.DocumentType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadDocumentRequest {

    @NotBlank(message = "Owner UUID is required")
    private String ownerUuid; // UUID of the student, staff, or parent

    @NotNull(message = "Owner type is required")
    private DocumentOwnerType ownerType;

    @NotNull(message = "Document type is required")
    private DocumentType documentType;

    @NotBlank(message = "File URL is required")
    private String fileUrl; // Storage URL (S3, Azure Blob, local path, etc.)

    @NotBlank(message = "File name is required")
    private String fileName;

    private String contentType; // MIME type (optional)

    private Long fileSize; // File size in bytes (optional)
}

