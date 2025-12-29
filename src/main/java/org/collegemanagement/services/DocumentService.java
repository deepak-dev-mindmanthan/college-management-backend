package org.collegemanagement.services;

import org.collegemanagement.dto.document.DocumentResponse;
import org.collegemanagement.dto.document.UploadDocumentRequest;
import org.collegemanagement.enums.DocumentOwnerType;
import org.collegemanagement.enums.DocumentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DocumentService {

    /**
     * Upload/create a new document
     */
    DocumentResponse uploadDocument(UploadDocumentRequest request);

    /**
     * Get document by UUID
     */
    DocumentResponse getDocumentByUuid(String documentUuid);

    /**
     * Get all documents with pagination
     */
    Page<DocumentResponse> getAllDocuments(Pageable pageable);

    /**
     * Get documents by owner (student, staff, parent, etc.)
     */
    Page<DocumentResponse> getDocumentsByOwner(String ownerUuid, DocumentOwnerType ownerType, Pageable pageable);

    /**
     * Get documents by document type
     */
    Page<DocumentResponse> getDocumentsByType(DocumentType documentType, Pageable pageable);

    /**
     * Get documents by owner and document type
     */
    Page<DocumentResponse> getDocumentsByOwnerAndType(String ownerUuid, DocumentOwnerType ownerType, DocumentType documentType, Pageable pageable);

    /**
     * Delete document by UUID
     */
    void deleteDocument(String documentUuid);
}

