package org.collegemanagement.services;

import org.collegemanagement.dto.exam.*;
import org.collegemanagement.enums.ExamType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ExamService {

    // ========== Exam Management ==========

    /**
     * Create a new exam
     */
    ExamResponse createExam(CreateExamRequest request);

    /**
     * Update exam information
     */
    ExamResponse updateExam(String examUuid, UpdateExamRequest request);

    /**
     * Get exam by UUID
     */
    ExamResponse getExamByUuid(String examUuid);

    /**
     * Get all exams with pagination
     */
    Page<ExamResponse> getAllExams(Pageable pageable);

    /**
     * Get all exams (without pagination)
     */
    List<ExamResponse> getAllExams();

    /**
     * Get exams by type with pagination
     */
    Page<ExamResponse> getExamsByType(ExamType examType, Pageable pageable);

    /**
     * Get exams by academic year UUID with pagination
     */
    Page<ExamResponse> getExamsByAcademicYear(String academicYearUuid, Pageable pageable);

    /**
     * Search exams by name
     */
    Page<ExamResponse> searchExams(String searchTerm, Pageable pageable);

    /**
     * Delete exam by UUID
     */
    void deleteExam(String examUuid);

    /**
     * Get exam summary
     */
    ExamSummaryResponse getExamSummary(String examUuid);

    // ========== Exam Class Management ==========

    /**
     * Add class to exam
     */
    ExamClassResponse addClassToExam(String examUuid, AddClassToExamRequest request);

    /**
     * Remove class from exam
     */
    void removeClassFromExam(String examUuid, String examClassUuid);

    /**
     * Get exam class by UUID
     */
    ExamClassResponse getExamClassByUuid(String examClassUuid);

    // ========== Exam Subject Management ==========

    /**
     * Add subject to exam class
     */
    ExamSubjectResponse addSubjectToExamClass(String examClassUuid, AddSubjectToExamClassRequest request);

    /**
     * Update exam subject
     */
    ExamSubjectResponse updateExamSubject(String examSubjectUuid, AddSubjectToExamClassRequest request);

    /**
     * Remove subject from exam class
     */
    void removeSubjectFromExamClass(String examSubjectUuid);

    /**
     * Get exam subject by UUID
     */
    ExamSubjectResponse getExamSubjectByUuid(String examSubjectUuid);

    /**
     * Get exam subjects by exam class UUID
     */
    List<ExamSubjectResponse> getExamSubjectsByExamClass(String examClassUuid);

    /**
     * Assign or reassign teacher to exam subject
     */
    ExamSubjectResponse assignTeacherToExamSubject(String examSubjectUuid, AssignTeacherToExamSubjectRequest request);

    // ========== Student Marks Management ==========

    /**
     * Create student marks
     */
    StudentMarksResponse createStudentMarks(CreateStudentMarksRequest request);

    /**
     * Update student marks
     */
    StudentMarksResponse updateStudentMarks(String studentMarksUuid, UpdateStudentMarksRequest request);

    /**
     * Bulk create/update student marks for an exam subject
     */
    List<StudentMarksResponse> bulkUpdateStudentMarks(BulkStudentMarksRequest request);

    /**
     * Get student marks by UUID
     */
    StudentMarksResponse getStudentMarksByUuid(String studentMarksUuid);

    /**
     * Get student marks by exam subject UUID
     */
    List<StudentMarksResponse> getStudentMarksByExamSubject(String examSubjectUuid);

    /**
     * Get student marks by student UUID and exam UUID
     */
    List<StudentMarksResponse> getStudentMarksByStudentAndExam(String studentUuid, String examUuid);

    /**
     * Get student exam result
     */
    StudentExamResultResponse getStudentExamResult(String studentUuid, String examUuid);

    /**
     * Delete student marks
     */
    void deleteStudentMarks(String studentMarksUuid);

    // ========== Grade Scale Management ==========

    /**
     * Create grade scale
     */
    GradeScaleResponse createGradeScale(CreateGradeScaleRequest request);

    /**
     * Update grade scale
     */
    GradeScaleResponse updateGradeScale(String gradeScaleUuid, UpdateGradeScaleRequest request);

    /**
     * Get grade scale by UUID
     */
    GradeScaleResponse getGradeScaleByUuid(String gradeScaleUuid);

    /**
     * Get all grade scales with pagination
     */
    Page<GradeScaleResponse> getAllGradeScales(Pageable pageable);

    /**
     * Get all grade scales (without pagination)
     */
    List<GradeScaleResponse> getAllGradeScales();

    /**
     * Delete grade scale
     */
    void deleteGradeScale(String gradeScaleUuid);

    // ========== Student Transcript Management ==========

    /**
     * Generate student transcript for academic year
     */
    StudentTranscriptResponse generateTranscript(String studentUuid, String academicYearUuid);

    /**
     * Update transcript
     */
    StudentTranscriptResponse updateTranscript(String transcriptUuid, PublishTranscriptRequest request);

    /**
     * Publish transcript
     */
    StudentTranscriptResponse publishTranscript(String transcriptUuid, PublishTranscriptRequest request);

    /**
     * Unpublish transcript
     */
    StudentTranscriptResponse unpublishTranscript(String transcriptUuid);

    /**
     * Get transcript by UUID
     */
    StudentTranscriptResponse getTranscriptByUuid(String transcriptUuid);

    /**
     * Get transcripts by student UUID
     */
    List<StudentTranscriptResponse> getTranscriptsByStudent(String studentUuid);

    /**
     * Get transcripts by academic year UUID with pagination
     */
    Page<StudentTranscriptResponse> getTranscriptsByAcademicYear(String academicYearUuid, Pageable pageable);

    /**
     * Get published transcripts by academic year UUID with pagination
     */
    Page<StudentTranscriptResponse> getPublishedTranscriptsByAcademicYear(String academicYearUuid, Pageable pageable);

    // ========== Reports and Summaries ==========

    /**
     * Get class exam summary
     */
    ClassExamSummaryResponse getClassExamSummary(String examUuid, String classUuid);

    /**
     * Get class exam summaries for all classes in an exam
     */
    List<ClassExamSummaryResponse> getClassExamSummaries(String examUuid);
}

