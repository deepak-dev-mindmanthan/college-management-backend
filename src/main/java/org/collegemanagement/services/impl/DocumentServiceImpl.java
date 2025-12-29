package org.collegemanagement.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.collegemanagement.dto.document.DocumentResponse;
import org.collegemanagement.dto.document.UploadDocumentRequest;
import org.collegemanagement.entity.document.Document;
import org.collegemanagement.entity.staff.StaffProfile;
import org.collegemanagement.entity.student.Parent;
import org.collegemanagement.entity.student.Student;
import org.collegemanagement.entity.tenant.College;
import org.collegemanagement.enums.DocumentOwnerType;
import org.collegemanagement.enums.DocumentType;
import org.collegemanagement.exception.ResourceNotFoundException;
import org.collegemanagement.mapper.DocumentMapper;
import org.collegemanagement.repositories.*;
import org.collegemanagement.security.tenant.TenantAccessGuard;
import org.collegemanagement.services.CollegeService;
import org.collegemanagement.services.DocumentService;
import org.collegemanagement.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final TenantAccessGuard tenantAccessGuard;
    private final CollegeService collegeService;
    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;
    private final StaffProfileRepository staffProfileRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    public DocumentResponse uploadDocument(UploadDocumentRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Get college and validate tenant access
        College college = getCollegeById(collegeId);

        // Resolve owner UUID to ID and get owner name
        OwnerInfo ownerInfo = resolveOwner(request.getOwnerUuid(), request.getOwnerType(), collegeId);

        // Create document
        Document document = Document.builder()
                .college(college)
                .ownerType(request.getOwnerType())
                .ownerId(ownerInfo.ownerId())
                .documentType(request.getDocumentType())
                .fileName(request.getFileName())
                .fileUrl(request.getFileUrl())
                .contentType(request.getContentType())
                .fileSize(request.getFileSize())
                .build();

        document = documentRepository.save(document);

        // Map to response with owner information
        return DocumentMapper.toResponseWithOwner(document, request.getOwnerUuid(), ownerInfo.ownerName());
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    public DocumentResponse getDocumentByUuid(String documentUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Document document = documentRepository.findByUuidAndCollegeId(documentUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with UUID: " + documentUuid));

        // Resolve owner ID to UUID and name
        OwnerInfo ownerInfo = resolveOwnerById(document.getOwnerId(), document.getOwnerType(), collegeId);

        return DocumentMapper.toResponseWithOwner(document, ownerInfo.ownerUuid(), ownerInfo.ownerName());
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public Page<DocumentResponse> getAllDocuments(Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Page<Document> documents = documentRepository.findAllByCollegeId(collegeId, pageable);

        return documents.map(doc -> {
            OwnerInfo ownerInfo = resolveOwnerById(doc.getOwnerId(), doc.getOwnerType(), collegeId);
            return DocumentMapper.toResponseWithOwner(doc, ownerInfo.ownerUuid(), ownerInfo.ownerName());
        });
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    public Page<DocumentResponse> getDocumentsByOwner(String ownerUuid, DocumentOwnerType ownerType, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Resolve owner UUID to ID
        OwnerInfo ownerInfo = resolveOwner(ownerUuid, ownerType, collegeId);

        Page<Document> documents = documentRepository.findByOwnerTypeAndOwnerIdAndCollegeId(
                ownerType, ownerInfo.ownerId(), collegeId, pageable);

        return documents.map(doc -> DocumentMapper.toResponseWithOwner(
                doc, ownerUuid, ownerInfo.ownerName()));
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public Page<DocumentResponse> getDocumentsByType(DocumentType documentType, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Page<Document> documents = documentRepository.findByDocumentTypeAndCollegeId(documentType, collegeId, pageable);

        return documents.map(doc -> {
            OwnerInfo ownerInfo = resolveOwnerById(doc.getOwnerId(), doc.getOwnerType(), collegeId);
            return DocumentMapper.toResponseWithOwner(doc, ownerInfo.ownerUuid(), ownerInfo.ownerName());
        });
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    public Page<DocumentResponse> getDocumentsByOwnerAndType(
            String ownerUuid, DocumentOwnerType ownerType, DocumentType documentType, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Resolve owner UUID to ID
        OwnerInfo ownerInfo = resolveOwner(ownerUuid, ownerType, collegeId);

        Page<Document> documents = documentRepository.findByOwnerTypeAndOwnerIdAndDocumentTypeAndCollegeId(
                ownerType, ownerInfo.ownerId(), documentType, collegeId, pageable);

        return documents.map(doc -> DocumentMapper.toResponseWithOwner(
                doc, ownerUuid, ownerInfo.ownerName()));
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public void deleteDocument(String documentUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Document document = documentRepository.findByUuidAndCollegeId(documentUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with UUID: " + documentUuid));

        documentRepository.delete(document);
    }

    // Helper methods

    private College getCollegeById(Long collegeId) {
        College college = collegeService.findById(collegeId);
        tenantAccessGuard.assertCurrentTenant(college);
        return college;
    }

    /**
     * Resolve owner UUID to ID and get owner name
     */
    private OwnerInfo resolveOwner(String ownerUuid, DocumentOwnerType ownerType, Long collegeId) {
        return switch (ownerType) {
            case STUDENT -> {
                Student student = studentRepository.findByUuidAndCollegeId(ownerUuid, collegeId)
                        .orElseThrow(() -> new ResourceNotFoundException("Student not found with UUID: " + ownerUuid));
                String ownerName = student.getUser() != null ? student.getUser().getName() : null;
                yield new OwnerInfo(student.getId(), ownerUuid, ownerName);
            }
            case STAFF -> {
                // StaffProfile is linked via User, so find by user UUID
                User user = userRepository.findByUuidAndCollegeId(ownerUuid, collegeId)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found with UUID: " + ownerUuid));
                StaffProfile staffProfile = staffProfileRepository.findByUserIdAndCollegeId(user.getId(), collegeId)
                        .orElseThrow(() -> new ResourceNotFoundException("Staff profile not found for user UUID: " + ownerUuid));
                String ownerName = staffProfile.getUser() != null ? staffProfile.getUser().getName() : null;
                yield new OwnerInfo(staffProfile.getId(), staffProfile.getUuid(), ownerName);
            }
            case PARENT -> {
                Parent parent = parentRepository.findByUuidAndCollegeId(ownerUuid, collegeId)
                        .orElseThrow(() -> new ResourceNotFoundException("Parent not found with UUID: " + ownerUuid));
                String ownerName = parent.getUser() != null ? parent.getUser().getName() : null;
                yield new OwnerInfo(parent.getId(), ownerUuid, ownerName);
            }
            case OTHER -> {
                // For OTHER, we'll use the UUID as-is and assume ownerId matches some other entity
                // This is a limitation - for OTHER type, we'd need additional context
                throw new IllegalArgumentException("OTHER owner type requires additional implementation");
            }
        };
    }

    /**
     * Resolve owner ID to UUID and name
     */
    private OwnerInfo resolveOwnerById(Long ownerId, DocumentOwnerType ownerType, Long collegeId) {
        return switch (ownerType) {
            case STUDENT -> {
                Student student = studentRepository.findById(ownerId)
                        .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + ownerId));
                tenantAccessGuard.assertCurrentTenantId(student.getCollege().getId());
                String ownerName = student.getUser() != null ? student.getUser().getName() : null;
                yield new OwnerInfo(ownerId, student.getUuid(), ownerName);
            }
            case STAFF -> {
                StaffProfile staffProfile = staffProfileRepository.findById(ownerId)
                        .orElseThrow(() -> new ResourceNotFoundException("Staff profile not found with ID: " + ownerId));
                tenantAccessGuard.assertCurrentTenantId(staffProfile.getCollege().getId());
                String ownerName = staffProfile.getUser() != null ? staffProfile.getUser().getName() : null;
                yield new OwnerInfo(ownerId, staffProfile.getUuid(), ownerName);
            }
            case PARENT -> {
                Parent parent = parentRepository.findById(ownerId)
                        .orElseThrow(() -> new ResourceNotFoundException("Parent not found with ID: " + ownerId));
                tenantAccessGuard.assertCurrentTenantId(parent.getCollege().getId());
                String ownerName = parent.getUser() != null ? parent.getUser().getName() : null;
                yield new OwnerInfo(ownerId, parent.getUuid(), ownerName);
            }
            case OTHER -> {
                // For OTHER, we cannot resolve without additional context
                throw new IllegalArgumentException("OTHER owner type requires additional implementation");
            }
        };
    }

    /**
     * Record to hold owner information
     */
    private record OwnerInfo(Long ownerId, String ownerUuid, String ownerName) {}
}

