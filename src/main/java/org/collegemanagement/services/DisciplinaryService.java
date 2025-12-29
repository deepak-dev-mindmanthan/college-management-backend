package org.collegemanagement.services;

import org.collegemanagement.dto.discipline.CreateDisciplinaryCaseRequest;
import org.collegemanagement.dto.discipline.DisciplinaryCaseResponse;
import org.collegemanagement.dto.discipline.UpdateDisciplinaryCaseRequest;
import org.collegemanagement.enums.DisciplinaryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface DisciplinaryService {

    /**
     * Create a new disciplinary case
     */
    DisciplinaryCaseResponse createDisciplinaryCase(CreateDisciplinaryCaseRequest request);

    /**
     * Update disciplinary case (status, action taken, etc.)
     */
    DisciplinaryCaseResponse updateDisciplinaryCase(String caseUuid, UpdateDisciplinaryCaseRequest request);

    /**
     * Get disciplinary case by UUID
     */
    DisciplinaryCaseResponse getDisciplinaryCaseByUuid(String caseUuid);

    /**
     * Get all disciplinary cases with pagination
     */
    Page<DisciplinaryCaseResponse> getAllDisciplinaryCases(Pageable pageable);

    /**
     * Get disciplinary cases by student UUID
     */
    Page<DisciplinaryCaseResponse> getDisciplinaryCasesByStudent(String studentUuid, Pageable pageable);

    /**
     * Get disciplinary cases by status
     */
    Page<DisciplinaryCaseResponse> getDisciplinaryCasesByStatus(DisciplinaryStatus status, Pageable pageable);

    /**
     * Get disciplinary cases by student UUID and status
     */
    Page<DisciplinaryCaseResponse> getDisciplinaryCasesByStudentAndStatus(String studentUuid, DisciplinaryStatus status, Pageable pageable);

    /**
     * Get disciplinary cases by date range
     */
    Page<DisciplinaryCaseResponse> getDisciplinaryCasesByDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable);
}

