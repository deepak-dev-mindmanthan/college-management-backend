package org.collegemanagement.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.collegemanagement.api.response.ApiResponse;
import org.collegemanagement.dto.document.DocumentResponse;
import org.collegemanagement.dto.document.UploadDocumentRequest;
import org.collegemanagement.enums.DocumentOwnerType;
import org.collegemanagement.enums.DocumentType;
import org.collegemanagement.services.DocumentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/documents")
@AllArgsConstructor
@Tag(name = "Document Management", description = "APIs for managing documents (student, staff, parent documents)")
public class DocumentController {

    private final DocumentService documentService;

    @Operation(
            summary = "Upload a document",
            description = "Uploads a document for a student, staff, parent, etc. Requires appropriate role based on document owner type."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Document uploaded successfully",
                    content = @Content(schema = @Schema(implementation = DocumentResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Owner not found"
            )
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<ApiResponse<DocumentResponse>> uploadDocument(
            @Valid @RequestBody UploadDocumentRequest request
    ) {
        DocumentResponse document = documentService.uploadDocument(request);
        return ResponseEntity.ok(ApiResponse.success(document, "Document uploaded successfully", HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get document by UUID",
            description = "Retrieves a document by its UUID. Accessible by appropriate roles based on document owner."
    )
    @GetMapping("/{documentUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<ApiResponse<DocumentResponse>> getDocument(
            @Parameter(description = "UUID of the document")
            @PathVariable String documentUuid
    ) {
        DocumentResponse document = documentService.getDocumentByUuid(documentUuid);
        return ResponseEntity.ok(ApiResponse.success(document, "Document retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get all documents",
            description = "Retrieves all documents with pagination. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Page<DocumentResponse>>> getAllDocuments(
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "uploadedAt") String sortBy,
            @Parameter(description = "Sort direction (ASC or DESC)")
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<DocumentResponse> documents = documentService.getAllDocuments(pageable);
        return ResponseEntity.ok(ApiResponse.success(documents, "Documents retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get documents by owner",
            description = "Retrieves all documents for a specific owner (student, staff, parent). Accessible by appropriate roles."
    )
    @GetMapping("/owner/{ownerUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<ApiResponse<Page<DocumentResponse>>> getDocumentsByOwner(
            @Parameter(description = "UUID of the owner (student, staff, parent)")
            @PathVariable String ownerUuid,
            @Parameter(description = "Owner type (STUDENT, STAFF, PARENT, OTHER)")
            @RequestParam DocumentOwnerType ownerType,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "uploadedAt") String sortBy,
            @Parameter(description = "Sort direction (ASC or DESC)")
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<DocumentResponse> documents = documentService.getDocumentsByOwner(ownerUuid, ownerType, pageable);
        return ResponseEntity.ok(ApiResponse.success(documents, "Documents retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get documents by type",
            description = "Retrieves all documents of a specific type. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @GetMapping("/type/{documentType}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Page<DocumentResponse>>> getDocumentsByType(
            @Parameter(description = "Document type")
            @PathVariable DocumentType documentType,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "uploadedAt") String sortBy,
            @Parameter(description = "Sort direction (ASC or DESC)")
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<DocumentResponse> documents = documentService.getDocumentsByType(documentType, pageable);
        return ResponseEntity.ok(ApiResponse.success(documents, "Documents retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get documents by owner and type",
            description = "Retrieves documents for a specific owner filtered by document type. Accessible by appropriate roles."
    )
    @GetMapping("/owner/{ownerUuid}/type/{documentType}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<ApiResponse<Page<DocumentResponse>>> getDocumentsByOwnerAndType(
            @Parameter(description = "UUID of the owner")
            @PathVariable String ownerUuid,
            @Parameter(description = "Owner type")
            @RequestParam DocumentOwnerType ownerType,
            @Parameter(description = "Document type")
            @PathVariable DocumentType documentType,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "uploadedAt") String sortBy,
            @Parameter(description = "Sort direction (ASC or DESC)")
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<DocumentResponse> documents = documentService.getDocumentsByOwnerAndType(
                ownerUuid, ownerType, documentType, pageable);
        return ResponseEntity.ok(ApiResponse.success(documents, "Documents retrieved successfully",HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Delete document",
            description = "Deletes a document by UUID. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @DeleteMapping("/{documentUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(
            @Parameter(description = "UUID of the document to delete")
            @PathVariable String documentUuid
    ) {
        documentService.deleteDocument(documentUuid);
        return ResponseEntity.ok(ApiResponse.success(null, "Document deleted successfully",HttpStatus.OK.value()));
    }
}

