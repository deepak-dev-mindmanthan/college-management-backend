package org.collegemanagement.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.collegemanagement.dto.exam.*;
import org.collegemanagement.entity.academic.ClassRoom;
import org.collegemanagement.entity.academic.StudentEnrollment;
import org.collegemanagement.entity.academic.Subject;
import org.collegemanagement.entity.exam.*;
import org.collegemanagement.entity.student.Student;
import org.collegemanagement.entity.tenant.AcademicYear;
import org.collegemanagement.entity.tenant.College;
import org.collegemanagement.entity.user.User;
import org.collegemanagement.enums.ExamType;
import org.collegemanagement.enums.ResultStatus;
import org.collegemanagement.exception.ResourceConflictException;
import org.collegemanagement.exception.ResourceNotFoundException;
import org.collegemanagement.mapper.ExamMapper;
import org.collegemanagement.mapper.GradeScaleMapper;
import org.collegemanagement.mapper.StudentMarksMapper;
import org.collegemanagement.mapper.StudentTranscriptMapper;
import org.collegemanagement.repositories.*;
import org.collegemanagement.security.tenant.TenantAccessGuard;
import org.collegemanagement.services.CollegeService;
import org.collegemanagement.services.ExamService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExamServiceImpl implements ExamService {

    private final ExamRepository examRepository;
    private final ExamClassRepository examClassRepository;
    private final ExamSubjectRepository examSubjectRepository;
    private final StudentMarksRepository studentMarksRepository;
    private final GradeScaleRepository gradeScaleRepository;
    private final StudentTranscriptRepository studentTranscriptRepository;
    private final AcademicYearRepository academicYearRepository;
    private final ClassRoomRepository classRoomRepository;
    private final SubjectRepository subjectRepository;
    private final StudentRepository studentRepository;
    private final StudentEnrollmentRepository studentEnrollmentRepository;
    private final TenantAccessGuard tenantAccessGuard;
    private final CollegeService collegeService;

    // ========== Exam Management ==========

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public ExamResponse createExam(CreateExamRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();
        College college = getCollegeById(collegeId);

        // Find academic year
        AcademicYear academicYear = academicYearRepository.findByUuidAndCollegeId(request.getAcademicYearUuid(), collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with UUID: " + request.getAcademicYearUuid()));

        // Check if exam name already exists for this college and academic year
        if (examRepository.existsByNameAndCollegeIdAndAcademicYearId(request.getName(), collegeId, academicYear.getId())) {
            throw new ResourceConflictException("Exam with name '" + request.getName() + "' already exists for this academic year");
        }

        // Create exam
        Exam exam = Exam.builder()
                .college(college)
                .name(request.getName())
                .examType(request.getExamType())
                .academicYear(academicYear)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();

        exam = examRepository.save(exam);

        // Add classes if provided
        if (request.getClassUuids() != null && !request.getClassUuids().isEmpty()) {
            for (String classUuid : request.getClassUuids()) {
                ClassRoom classRoom = classRoomRepository.findByUuidAndCollegeId(classUuid, collegeId)
                        .orElseThrow(() -> new ResourceNotFoundException("Class not found with UUID: " + classUuid));

                // Validate class belongs to same academic year
                if (!classRoom.getAcademicYear().getId().equals(academicYear.getId())) {
                    throw new ResourceConflictException("Class " + classRoom.getName() + " does not belong to the same academic year");
                }

                // Check if exam class already exists
                if (!examClassRepository.existsByExamIdAndClassRoomId(exam.getId(), classRoom.getId())) {
                    ExamClass examClass = ExamClass.builder()
                            .exam(exam)
                            .classRoom(classRoom)
                            .build();
                    examClassRepository.save(examClass);
                }
            }
        }

        // Refresh to get all associations
        exam = examRepository.findByUuidAndCollegeId(exam.getUuid(), collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found"));

        return ExamMapper.toResponse(exam);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public ExamResponse updateExam(String examUuid, UpdateExamRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();
        Exam exam = examRepository.findByUuidAndCollegeId(examUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with UUID: " + examUuid));

        // Update name if provided and validate uniqueness
        if (request.getName() != null && !request.getName().equals(exam.getName())) {
            if (examRepository.existsByNameAndCollegeIdAndAcademicYearIdAndIdNot(
                    request.getName(), collegeId, exam.getAcademicYear().getId(), exam.getId())) {
                throw new ResourceConflictException("Exam with name '" + request.getName() + "' already exists for this academic year");
            }
            exam.setName(request.getName());
        }

        // Update exam type if provided
        if (request.getExamType() != null) {
            exam.setExamType(request.getExamType());
        }

        // Update academic year if provided
        if (request.getAcademicYearUuid() != null) {
            AcademicYear academicYear = academicYearRepository.findByUuidAndCollegeId(request.getAcademicYearUuid(), collegeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with UUID: " + request.getAcademicYearUuid()));
            exam.setAcademicYear(academicYear);
        }

        // Update dates if provided
        if (request.getStartDate() != null) {
            exam.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            exam.setEndDate(request.getEndDate());
        }

        exam = examRepository.save(exam);

        // Update classes if provided
        if (request.getClassUuids() != null) {
            // Remove existing classes not in the new list
            Set<ExamClass> existingExamClasses = exam.getExamClasses();
            for (ExamClass examClass : existingExamClasses) {
                String classUuid = examClass.getClassRoom().getUuid();
                if (!request.getClassUuids().contains(classUuid)) {
                    examClassRepository.delete(examClass);
                }
            }

            // Add new classes
            AcademicYear academicYear = exam.getAcademicYear();
            for (String classUuid : request.getClassUuids()) {
                ClassRoom classRoom = classRoomRepository.findByUuidAndCollegeId(classUuid, collegeId)
                        .orElseThrow(() -> new ResourceNotFoundException("Class not found with UUID: " + classUuid));

                if (!classRoom.getAcademicYear().getId().equals(academicYear.getId())) {
                    throw new ResourceConflictException("Class " + classRoom.getName() + " does not belong to the same academic year");
                }

                if (!examClassRepository.existsByExamIdAndClassRoomId(exam.getId(), classRoom.getId())) {
                    ExamClass examClass = ExamClass.builder()
                            .exam(exam)
                            .classRoom(classRoom)
                            .build();
                    examClassRepository.save(examClass);
                }
            }
        }

        exam = examRepository.findByUuidAndCollegeId(exam.getUuid(), collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found"));

        return ExamMapper.toResponse(exam);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT')")
    public ExamResponse getExamByUuid(String examUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();
        Exam exam = examRepository.findByUuidAndCollegeId(examUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with UUID: " + examUuid));
        return ExamMapper.toResponse(exam);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT')")
    public Page<ExamResponse> getAllExams(Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();
        Page<Exam> exams = examRepository.findAllByCollegeId(collegeId, pageable);
        return exams.map(ExamMapper::toResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT')")
    public List<ExamResponse> getAllExams() {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();
        List<Exam> exams = examRepository.findAllByCollegeId(collegeId);
        return exams.stream().map(ExamMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT')")
    public Page<ExamResponse> getExamsByType(ExamType examType, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();
        Page<Exam> exams = examRepository.findByExamTypeAndCollegeId(examType, collegeId, pageable);
        return exams.map(ExamMapper::toResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT')")
    public Page<ExamResponse> getExamsByAcademicYear(String academicYearUuid, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();
        Page<Exam> exams = examRepository.findByAcademicYearUuidAndCollegeId(academicYearUuid, collegeId, pageable);
        return exams.map(ExamMapper::toResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public Page<ExamResponse> searchExams(String searchTerm, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();
        Page<Exam> exams = examRepository.searchByCollegeId(collegeId, searchTerm, pageable);
        return exams.map(ExamMapper::toResponse);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public void deleteExam(String examUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();
        Exam exam = examRepository.findByUuidAndCollegeId(examUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with UUID: " + examUuid));

        // Delete will cascade to exam classes, subjects, and marks
        examRepository.delete(exam);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public ExamSummaryResponse getExamSummary(String examUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();
        Exam exam = examRepository.findByUuidAndCollegeId(examUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with UUID: " + examUuid));

        int totalClasses = exam.getExamClasses() != null ? exam.getExamClasses().size() : 0;
        int totalSubjects = 0;
        int totalStudents = 0;
        int studentsWithMarks = 0;

        if (exam.getExamClasses() != null) {
            for (ExamClass examClass : exam.getExamClasses()) {
                if (examClass.getSubjects() != null) {
                    totalSubjects += examClass.getSubjects().size();
                    for (ExamSubject examSubject : examClass.getSubjects()) {
                        if (examSubject.getMarks() != null) {
                            studentsWithMarks += (int) examSubject.getMarks().stream()
                                    .filter(m -> m.getMarksObtained() != null)
                                    .count();
                        }
                    }
                }
                // Count students in class
                List<StudentEnrollment> enrollments = studentEnrollmentRepository.findByClassIdAndCollegeId(
                        examClass.getClassRoom().getId(), collegeId);
                totalStudents += enrollments.size();
            }
        }

        boolean isCompleted = exam.getEndDate() != null && Instant.now().isAfter(exam.getEndDate());

        return ExamSummaryResponse.builder()
                .examUuid(exam.getUuid())
                .examName(exam.getName())
                .examType(exam.getExamType())
                .academicYearName(exam.getAcademicYear() != null ? exam.getAcademicYear().getYearName() : null)
                .startDate(exam.getStartDate())
                .endDate(exam.getEndDate())
                .totalClasses(totalClasses)
                .totalSubjects(totalSubjects)
                .totalStudents(totalStudents)
                .studentsWithMarks(studentsWithMarks)
                .isCompleted(isCompleted)
                .build();
    }

    // ========== Exam Class Management ==========

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public ExamClassResponse addClassToExam(String examUuid, AddClassToExamRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();
        Exam exam = examRepository.findByUuidAndCollegeId(examUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with UUID: " + examUuid));

        ClassRoom classRoom = classRoomRepository.findByUuidAndCollegeId(request.getClassUuid(), collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with UUID: " + request.getClassUuid()));

        // Validate class belongs to same academic year as exam
        if (!classRoom.getAcademicYear().getId().equals(exam.getAcademicYear().getId())) {
            throw new ResourceConflictException("Class does not belong to the same academic year as the exam");
        }

        // Check if already exists
        if (examClassRepository.existsByExamIdAndClassRoomId(exam.getId(), classRoom.getId())) {
            throw new ResourceConflictException("Class is already added to this exam");
        }

        ExamClass examClass = ExamClass.builder()
                .exam(exam)
                .classRoom(classRoom)
                .build();

        examClass = examClassRepository.save(examClass);

        return ExamMapper.toExamClassResponse(examClass);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public void removeClassFromExam(String examUuid, String examClassUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();
        ExamClass examClass = examClassRepository.findByUuidAndCollegeId(examClassUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam class not found with UUID: " + examClassUuid));

        // Verify it belongs to the exam
        if (!examClass.getExam().getUuid().equals(examUuid)) {
            throw new ResourceNotFoundException("Exam class does not belong to the specified exam");
        }

        examClassRepository.delete(examClass);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT')")
    public ExamClassResponse getExamClassByUuid(String examClassUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();
        ExamClass examClass = examClassRepository.findByUuidAndCollegeId(examClassUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam class not found with UUID: " + examClassUuid));
        return ExamMapper.toExamClassResponse(examClass);
    }

    // ========== Exam Subject Management ==========

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public ExamSubjectResponse addSubjectToExamClass(String examClassUuid, AddSubjectToExamClassRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();
        ExamClass examClass = examClassRepository.findByUuidAndCollegeId(examClassUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam class not found with UUID: " + examClassUuid));

        Subject subject = subjectRepository.findByUuidAndCollegeId(request.getSubjectUuid(), collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with UUID: " + request.getSubjectUuid()));

        // Validate subject belongs to the class
        if (!subject.getClassRoom().getId().equals(examClass.getClassRoom().getId())) {
            throw new ResourceConflictException("Subject does not belong to the class");
        }

        // Validate pass marks <= max marks
        if (request.getPassMarks() > request.getMaxMarks()) {
            throw new ResourceConflictException("Pass marks cannot be greater than maximum marks");
        }

        // Check if already exists
        if (examSubjectRepository.existsByExamClassIdAndSubjectId(examClass.getId(), subject.getId())) {
            throw new ResourceConflictException("Subject is already added to this exam class");
        }

        ExamSubject examSubject = ExamSubject.builder()
                .examClass(examClass)
                .subject(subject)
                .maxMarks(request.getMaxMarks())
                .passMarks(request.getPassMarks())
                .examDate(request.getExamDate())
                .build();

        examSubject = examSubjectRepository.save(examSubject);

        return ExamMapper.toExamSubjectResponse(examSubject);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public ExamSubjectResponse updateExamSubject(String examSubjectUuid, AddSubjectToExamClassRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();
        ExamSubject examSubject = examSubjectRepository.findByUuidAndCollegeId(examSubjectUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam subject not found with UUID: " + examSubjectUuid));

        // Update subject if provided
        if (request.getSubjectUuid() != null) {
            Subject subject = subjectRepository.findByUuidAndCollegeId(request.getSubjectUuid(), collegeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Subject not found with UUID: " + request.getSubjectUuid()));
            examSubject.setSubject(subject);
        }

        // Update marks if provided
        if (request.getMaxMarks() != null) {
            examSubject.setMaxMarks(request.getMaxMarks());
        }
        if (request.getPassMarks() != null) {
            examSubject.setPassMarks(request.getPassMarks());
        }
        if (request.getExamDate() != null) {
            examSubject.setExamDate(request.getExamDate());
        }

        // Validate pass marks <= max marks
        if (examSubject.getPassMarks() > examSubject.getMaxMarks()) {
            throw new ResourceConflictException("Pass marks cannot be greater than maximum marks");
        }

        examSubject = examSubjectRepository.save(examSubject);

        return ExamMapper.toExamSubjectResponse(examSubject);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public void removeSubjectFromExamClass(String examSubjectUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();
        ExamSubject examSubject = examSubjectRepository.findByUuidAndCollegeId(examSubjectUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam subject not found with UUID: " + examSubjectUuid));

        examSubjectRepository.delete(examSubject);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT')")
    public ExamSubjectResponse getExamSubjectByUuid(String examSubjectUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();
        ExamSubject examSubject = examSubjectRepository.findByUuidAndCollegeId(examSubjectUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam subject not found with UUID: " + examSubjectUuid));
        return ExamMapper.toExamSubjectResponse(examSubject);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT')")
    public List<ExamSubjectResponse> getExamSubjectsByExamClass(String examClassUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();
        List<ExamSubject> examSubjects = examSubjectRepository.findByExamClassUuidAndCollegeId(examClassUuid, collegeId);
        return examSubjects.stream().map(ExamMapper::toExamSubjectResponse).collect(Collectors.toList());
    }

    // ========== Student Marks Management ==========

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public StudentMarksResponse createStudentMarks(CreateStudentMarksRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();
        ExamSubject examSubject = examSubjectRepository.findByUuidAndCollegeId(request.getExamSubjectUuid(), collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam subject not found with UUID: " + request.getExamSubjectUuid()));

        Student student = studentRepository.findByUuidAndCollegeId(request.getStudentUuid(), collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with UUID: " + request.getStudentUuid()));

        // Validate marks
        if (request.getMarksObtained() < 0 || request.getMarksObtained() > examSubject.getMaxMarks()) {
            throw new ResourceConflictException("Marks obtained must be between 0 and " + examSubject.getMaxMarks());
        }

        // Check if marks already exist
        if (studentMarksRepository.existsByExamSubjectIdAndStudentId(examSubject.getId(), student.getId())) {
            throw new ResourceConflictException("Marks already exist for this student in this exam subject");
        }

        // Find grade based on marks
        GradeScale gradeScale = findGradeByMarks(request.getMarksObtained(), collegeId);

        StudentMarks studentMarks = StudentMarks.builder()
                .examSubject(examSubject)
                .student(student)
                .marksObtained(request.getMarksObtained())
                .gradeScale(gradeScale)
                .build();

        studentMarks = studentMarksRepository.save(studentMarks);

        return StudentMarksMapper.toResponse(studentMarks);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public StudentMarksResponse updateStudentMarks(String studentMarksUuid, UpdateStudentMarksRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();
        StudentMarks studentMarks = studentMarksRepository.findByUuidAndCollegeId(studentMarksUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Student marks not found with UUID: " + studentMarksUuid));

        ExamSubject examSubject = studentMarks.getExamSubject();

        // Validate marks
        if (request.getMarksObtained() < 0 || request.getMarksObtained() > examSubject.getMaxMarks()) {
            throw new ResourceConflictException("Marks obtained must be between 0 and " + examSubject.getMaxMarks());
        }

        studentMarks.setMarksObtained(request.getMarksObtained());

        // Update grade based on marks
        GradeScale gradeScale = findGradeByMarks(request.getMarksObtained(), collegeId);
        studentMarks.setGradeScale(gradeScale);

        studentMarks = studentMarksRepository.save(studentMarks);

        return StudentMarksMapper.toResponse(studentMarks);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public List<StudentMarksResponse> bulkUpdateStudentMarks(BulkStudentMarksRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();
        ExamSubject examSubject = examSubjectRepository.findByUuidAndCollegeId(request.getExamSubjectUuid(), collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam subject not found with UUID: " + request.getExamSubjectUuid()));

        List<StudentMarksResponse> responses = new ArrayList<>();

        for (BulkStudentMarksRequest.StudentMarksEntry entry : request.getMarks()) {
            Student student = studentRepository.findByUuidAndCollegeId(entry.getStudentUuid(), collegeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found with UUID: " + entry.getStudentUuid()));

            // Validate marks
            if (entry.getMarksObtained() < 0 || entry.getMarksObtained() > examSubject.getMaxMarks()) {
                throw new ResourceConflictException("Marks obtained must be between 0 and " + examSubject.getMaxMarks() + " for student " + student.getRollNumber());
            }

            // Find or create student marks
            Optional<StudentMarks> existingMarks = studentMarksRepository.findByExamSubjectIdAndStudentId(
                    examSubject.getId(), student.getId());

            StudentMarks studentMarks;
            if (existingMarks.isPresent()) {
                studentMarks = existingMarks.get();
                studentMarks.setMarksObtained(entry.getMarksObtained());
            } else {
                studentMarks = StudentMarks.builder()
                        .examSubject(examSubject)
                        .student(student)
                        .marksObtained(entry.getMarksObtained())
                        .build();
            }

            // Find grade based on marks
            GradeScale gradeScale = findGradeByMarks(entry.getMarksObtained(), collegeId);
            studentMarks.setGradeScale(gradeScale);

            studentMarks = studentMarksRepository.save(studentMarks);
            responses.add(StudentMarksMapper.toResponse(studentMarks));
        }

        return responses;
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT')")
    public StudentMarksResponse getStudentMarksByUuid(String studentMarksUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();
        StudentMarks studentMarks = studentMarksRepository.findByUuidAndCollegeId(studentMarksUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Student marks not found with UUID: " + studentMarksUuid));
        return StudentMarksMapper.toResponse(studentMarks);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT')")
    public List<StudentMarksResponse> getStudentMarksByExamSubject(String examSubjectUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();
        List<StudentMarks> marks = studentMarksRepository.findByExamSubjectUuidAndCollegeId(examSubjectUuid, collegeId);
        return marks.stream().map(StudentMarksMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT')")
    public List<StudentMarksResponse> getStudentMarksByStudentAndExam(String studentUuid, String examUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();
        List<StudentMarks> marks = studentMarksRepository.findByStudentUuidAndExamUuidAndCollegeId(studentUuid, examUuid, collegeId);
        return marks.stream().map(StudentMarksMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT')")
    public StudentExamResultResponse getStudentExamResult(String studentUuid, String examUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();
        Exam exam = examRepository.findByUuidAndCollegeId(examUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with UUID: " + examUuid));

        Student student = studentRepository.findByUuidAndCollegeId(studentUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with UUID: " + studentUuid));

        List<StudentMarks> marks = studentMarksRepository.findByStudentUuidAndExamUuidAndCollegeId(studentUuid, examUuid, collegeId);

        List<StudentMarksResponse> marksResponse = marks.stream()
                .map(StudentMarksMapper::toResponse)
                .collect(Collectors.toList());

        // Calculate totals
        int totalMarks = marks.stream()
                .filter(m -> m.getExamSubject() != null)
                .mapToInt(m -> m.getExamSubject().getMaxMarks() != null ? m.getExamSubject().getMaxMarks() : 0)
                .sum();

        int obtainedMarks = marks.stream()
                .filter(m -> m.getMarksObtained() != null)
                .mapToInt(StudentMarks::getMarksObtained)
                .sum();

        BigDecimal percentage = totalMarks > 0 ?
                BigDecimal.valueOf(obtainedMarks)
                        .divide(BigDecimal.valueOf(totalMarks), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        // Find overall grade
        GradeScale overallGrade = findGradeByPercentage(percentage.intValue(), collegeId).orElse(null);

        // Check if passed (all subjects passed)
        boolean isPassed = marks.stream()
                .allMatch(m -> m.getMarksObtained() != null &&
                        m.getExamSubject() != null &&
                        m.getExamSubject().getPassMarks() != null &&
                        m.getMarksObtained() >= m.getExamSubject().getPassMarks());

        // Get student enrollment for class info
        Optional<StudentEnrollment> enrollment = studentEnrollmentRepository.findActiveByStudentIdAndCollegeId(student.getId(), collegeId);
        String className = enrollment.map(e -> e.getClassRoom().getName()).orElse(null);
        String section = enrollment.map(e -> e.getClassRoom().getSection()).orElse(null);

        // Calculate rank (simplified - would need more complex query in production)
        int rankInClass = 1; // Placeholder - would require ranking query

        return StudentExamResultResponse.builder()
                .examUuid(exam.getUuid())
                .examName(exam.getName())
                .studentUuid(student.getUuid())
                .studentName(student.getUser() != null ? student.getUser().getName() : null)
                .rollNumber(student.getRollNumber())
                .className(className)
                .section(section)
                .marks(marksResponse)
                .totalMarks(totalMarks)
                .obtainedMarks(obtainedMarks)
                .percentage(percentage)
                .overallGrade(overallGrade != null ? overallGrade.getGrade() : null)
                .overallGradePoints(overallGrade != null ? overallGrade.getGradePoints() : null)
                .isPassed(isPassed)
                .rankInClass(rankInClass)
                .build();
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public void deleteStudentMarks(String studentMarksUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();
        StudentMarks studentMarks = studentMarksRepository.findByUuidAndCollegeId(studentMarksUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Student marks not found with UUID: " + studentMarksUuid));
        studentMarksRepository.delete(studentMarks);
    }

    // ========== Grade Scale Management ==========

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public GradeScaleResponse createGradeScale(CreateGradeScaleRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();
        College college = getCollegeById(collegeId);

        // Validate min <= max
        if (request.getMinMarks() > request.getMaxMarks()) {
            throw new ResourceConflictException("Minimum marks cannot be greater than maximum marks");
        }

        // Check if grade already exists
        if (gradeScaleRepository.existsByGradeAndCollegeId(request.getGrade(), collegeId)) {
            throw new ResourceConflictException("Grade scale with grade '" + request.getGrade() + "' already exists");
        }

        GradeScale gradeScale = GradeScale.builder()
                .college(college)
                .grade(request.getGrade())
                .minMarks(request.getMinMarks())
                .maxMarks(request.getMaxMarks())
                .gradePoints(request.getGradePoints())
                .build();

        gradeScale = gradeScaleRepository.save(gradeScale);

        return GradeScaleMapper.toResponse(gradeScale);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public GradeScaleResponse updateGradeScale(String gradeScaleUuid, UpdateGradeScaleRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();
        GradeScale gradeScale = gradeScaleRepository.findByUuidAndCollegeId(gradeScaleUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Grade scale not found with UUID: " + gradeScaleUuid));

        // Update fields if provided
        if (request.getGrade() != null) {
            if (!request.getGrade().equals(gradeScale.getGrade()) &&
                    gradeScaleRepository.existsByGradeAndCollegeIdAndIdNot(request.getGrade(), collegeId, gradeScale.getId())) {
                throw new ResourceConflictException("Grade scale with grade '" + request.getGrade() + "' already exists");
            }
            gradeScale.setGrade(request.getGrade());
        }
        if (request.getMinMarks() != null) {
            gradeScale.setMinMarks(request.getMinMarks());
        }
        if (request.getMaxMarks() != null) {
            gradeScale.setMaxMarks(request.getMaxMarks());
        }
        if (request.getGradePoints() != null) {
            gradeScale.setGradePoints(request.getGradePoints());
        }

        // Validate min <= max
        if (gradeScale.getMinMarks() > gradeScale.getMaxMarks()) {
            throw new ResourceConflictException("Minimum marks cannot be greater than maximum marks");
        }

        gradeScale = gradeScaleRepository.save(gradeScale);

        return GradeScaleMapper.toResponse(gradeScale);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT')")
    public GradeScaleResponse getGradeScaleByUuid(String gradeScaleUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();
        GradeScale gradeScale = gradeScaleRepository.findByUuidAndCollegeId(gradeScaleUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Grade scale not found with UUID: " + gradeScaleUuid));
        return GradeScaleMapper.toResponse(gradeScale);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT')")
    public Page<GradeScaleResponse> getAllGradeScales(Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();
        Page<GradeScale> gradeScales = gradeScaleRepository.findAllByCollegeId(collegeId, pageable);
        return gradeScales.map(GradeScaleMapper::toResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT')")
    public List<GradeScaleResponse> getAllGradeScales() {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();
        List<GradeScale> gradeScales = gradeScaleRepository.findAllByCollegeId(collegeId);
        return gradeScales.stream().map(GradeScaleMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public void deleteGradeScale(String gradeScaleUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();
        GradeScale gradeScale = gradeScaleRepository.findByUuidAndCollegeId(gradeScaleUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Grade scale not found with UUID: " + gradeScaleUuid));
        gradeScaleRepository.delete(gradeScale);
    }

    // ========== Student Transcript Management ==========

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public StudentTranscriptResponse generateTranscript(String studentUuid, String academicYearUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();
        Student student = studentRepository.findByUuidAndCollegeId(studentUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with UUID: " + studentUuid));

        AcademicYear academicYear = academicYearRepository.findByUuidAndCollegeId(academicYearUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with UUID: " + academicYearUuid));

        // Check if transcript already exists
        Optional<StudentTranscript> existingTranscript = studentTranscriptRepository
                .findByStudentUuidAndAcademicYearUuidAndCollegeId(studentUuid, academicYearUuid, collegeId);

        StudentTranscript transcript;
        if (existingTranscript.isPresent()) {
            transcript = existingTranscript.get();
        } else {
            transcript = StudentTranscript.builder()
                    .student(student)
                    .academicYear(academicYear)
                    .cgpa(BigDecimal.ZERO)
                    .totalCredits(0)
                    .resultStatus(ResultStatus.WITHHELD)
                    .published(false)
                    .build();
        }

        // Get all exams for this academic year
        List<Exam> exams = examRepository.findByAcademicYearUuidAndCollegeId(academicYearUuid, collegeId, Pageable.unpaged())
                .getContent();

        // Get all marks for this student in this academic year
        List<StudentMarks> allMarks = new ArrayList<>();
        for (Exam exam : exams) {
            List<StudentMarks> examMarks = studentMarksRepository.findByStudentUuidAndExamUuidAndCollegeId(
                    studentUuid, exam.getUuid(), collegeId);
            allMarks.addAll(examMarks);
        }

        // Calculate CGPA and credits
        BigDecimal totalGradePoints = BigDecimal.ZERO;
        int totalCredits = 0;

        for (StudentMarks marks : allMarks) {
            if (marks.getExamSubject() != null && marks.getExamSubject().getSubject() != null) {
                Integer credits = marks.getExamSubject().getSubject().getCredit();
                if (credits != null && marks.getGradeScale() != null && marks.getGradeScale().getGradePoints() != null) {
                    totalCredits += credits;
                    totalGradePoints = totalGradePoints.add(
                            marks.getGradeScale().getGradePoints().multiply(BigDecimal.valueOf(credits))
                    );
                }
            }
        }

        BigDecimal cgpa = totalCredits > 0 ?
                totalGradePoints.divide(BigDecimal.valueOf(totalCredits), 4, RoundingMode.HALF_UP)
                        .setScale(2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        // Determine result status
        ResultStatus resultStatus = determineResultStatus(allMarks);

        transcript.setCgpa(cgpa);
        transcript.setTotalCredits(totalCredits);
        transcript.setResultStatus(resultStatus);

        transcript = studentTranscriptRepository.save(transcript);

        return StudentTranscriptMapper.toResponse(transcript, allMarks);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public StudentTranscriptResponse updateTranscript(String transcriptUuid, PublishTranscriptRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();
        StudentTranscript transcript = studentTranscriptRepository.findByUuidAndCollegeId(transcriptUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Transcript not found with UUID: " + transcriptUuid));

        if (request.getResultStatus() != null) {
            transcript.setResultStatus(request.getResultStatus());
        }
        if (request.getRemarks() != null) {
            transcript.setRemarks(request.getRemarks());
        }

        transcript = studentTranscriptRepository.save(transcript);

        // Get marks for response
        List<StudentMarks> marks = studentMarksRepository.findByStudentUuidAndCollegeId(
                transcript.getStudent().getUuid(), collegeId);

        return StudentTranscriptMapper.toResponse(transcript, marks);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public StudentTranscriptResponse publishTranscript(String transcriptUuid, PublishTranscriptRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();
        StudentTranscript transcript = studentTranscriptRepository.findByUuidAndCollegeId(transcriptUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Transcript not found with UUID: " + transcriptUuid));

        // Get current user
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        transcript.setPublished(true);
        transcript.setPublishedAt(Instant.now());
        transcript.setApprovedBy(currentUser);

        if (request.getResultStatus() != null) {
            transcript.setResultStatus(request.getResultStatus());
        }
        if (request.getRemarks() != null) {
            transcript.setRemarks(request.getRemarks());
        }

        transcript = studentTranscriptRepository.save(transcript);

        // Get marks for response
        List<StudentMarks> marks = studentMarksRepository.findByStudentUuidAndCollegeId(
                transcript.getStudent().getUuid(), collegeId);

        return StudentTranscriptMapper.toResponse(transcript, marks);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public StudentTranscriptResponse unpublishTranscript(String transcriptUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();
        StudentTranscript transcript = studentTranscriptRepository.findByUuidAndCollegeId(transcriptUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Transcript not found with UUID: " + transcriptUuid));

        transcript.setPublished(false);
        transcript.setPublishedAt(null);
        transcript.setApprovedBy(null);

        transcript = studentTranscriptRepository.save(transcript);

        // Get marks for response
        List<StudentMarks> marks = studentMarksRepository.findByStudentUuidAndCollegeId(
                transcript.getStudent().getUuid(), collegeId);

        return StudentTranscriptMapper.toResponse(transcript, marks);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT')")
    public StudentTranscriptResponse getTranscriptByUuid(String transcriptUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();
        StudentTranscript transcript = studentTranscriptRepository.findByUuidAndCollegeId(transcriptUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Transcript not found with UUID: " + transcriptUuid));

        // Get marks for response
        List<StudentMarks> marks = studentMarksRepository.findByStudentUuidAndCollegeId(
                transcript.getStudent().getUuid(), collegeId);

        return StudentTranscriptMapper.toResponse(transcript, marks);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT')")
    public List<StudentTranscriptResponse> getTranscriptsByStudent(String studentUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();
        List<StudentTranscript> transcripts = studentTranscriptRepository.findByStudentUuidAndCollegeId(studentUuid, collegeId);

        return transcripts.stream()
                .map(t -> {
                    List<StudentMarks> marks = studentMarksRepository.findByStudentUuidAndCollegeId(
                            t.getStudent().getUuid(), collegeId);
                    return StudentTranscriptMapper.toResponse(t, marks);
                })
                .collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public Page<StudentTranscriptResponse> getTranscriptsByAcademicYear(String academicYearUuid, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();
        Page<StudentTranscript> transcripts = studentTranscriptRepository.findByAcademicYearUuidAndCollegeId(
                academicYearUuid, collegeId, pageable);

        return transcripts.map(t -> {
            List<StudentMarks> marks = studentMarksRepository.findByStudentUuidAndCollegeId(
                    t.getStudent().getUuid(), collegeId);
            return StudentTranscriptMapper.toResponse(t, marks);
        });
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT')")
    public Page<StudentTranscriptResponse> getPublishedTranscriptsByAcademicYear(String academicYearUuid, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();
        Page<StudentTranscript> transcripts = studentTranscriptRepository.findPublishedByAcademicYearUuidAndCollegeId(
                academicYearUuid, collegeId, pageable);

        return transcripts.map(t -> {
            List<StudentMarks> marks = studentMarksRepository.findByStudentUuidAndCollegeId(
                    t.getStudent().getUuid(), collegeId);
            return StudentTranscriptMapper.toResponse(t, marks);
        });
    }

    // ========== Reports and Summaries ==========

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public ClassExamSummaryResponse getClassExamSummary(String examUuid, String classUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();
        Exam exam = examRepository.findByUuidAndCollegeId(examUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with UUID: " + examUuid));

        ClassRoom classRoom = classRoomRepository.findByUuidAndCollegeId(classUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with UUID: " + classUuid));

        // Get exam class
        ExamClass examClass = examClassRepository.findByExamIdAndClassRoomId(exam.getId(), classRoom.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Class is not part of this exam"));

        // Get all students in class
        List<StudentEnrollment> enrollments = studentEnrollmentRepository.findByClassUuidAndCollegeId(classUuid, collegeId);
        int totalStudents = enrollments.size();

        // Get all marks for this exam and class
        List<StudentMarks> allMarks = studentMarksRepository.findByClassUuidAndExamUuidAndCollegeId(
                classUuid, examUuid, collegeId);

        int studentsWithMarks = (int) allMarks.stream()
                .map(StudentMarks::getStudent)
                .map(Student::getUuid)
                .distinct()
                .count();

        int totalSubjects = examClass.getSubjects() != null ? examClass.getSubjects().size() : 0;

        // Calculate average percentage
        BigDecimal totalPercentage = BigDecimal.ZERO;
        int studentsCount = 0;

        for (StudentEnrollment enrollment : enrollments) {
            List<StudentMarks> studentMarks = allMarks.stream()
                    .filter(m -> m.getStudent().getId().equals(enrollment.getStudent().getId()))
                    .collect(Collectors.toList());

            if (!studentMarks.isEmpty()) {
                int total = studentMarks.stream()
                        .filter(m -> m.getExamSubject() != null)
                        .mapToInt(m -> m.getExamSubject().getMaxMarks() != null ? m.getExamSubject().getMaxMarks() : 0)
                        .sum();

                int obtained = studentMarks.stream()
                        .filter(m -> m.getMarksObtained() != null)
                        .mapToInt(StudentMarks::getMarksObtained)
                        .sum();

                if (total > 0) {
                    BigDecimal percentage = BigDecimal.valueOf(obtained)
                            .divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100));
                    totalPercentage = totalPercentage.add(percentage);
                    studentsCount++;
                }
            }
        }

        BigDecimal averagePercentage = studentsCount > 0 ?
                totalPercentage.divide(BigDecimal.valueOf(studentsCount), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        // Count passed/failed students
        int passedStudents = 0;
        int failedStudents = 0;

        for (StudentEnrollment enrollment : enrollments) {
            List<StudentMarks> studentMarks = allMarks.stream()
                    .filter(m -> m.getStudent().getId().equals(enrollment.getStudent().getId()))
                    .collect(Collectors.toList());

            if (!studentMarks.isEmpty()) {
                boolean passed = studentMarks.stream()
                        .allMatch(m -> m.getMarksObtained() != null &&
                                m.getExamSubject() != null &&
                                m.getExamSubject().getPassMarks() != null &&
                                m.getMarksObtained() >= m.getExamSubject().getPassMarks());

                if (passed) {
                    passedStudents++;
                } else {
                    failedStudents++;
                }
            }
        }

        BigDecimal passPercentage = totalStudents > 0 ?
                BigDecimal.valueOf(passedStudents)
                        .divide(BigDecimal.valueOf(totalStudents), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        return ClassExamSummaryResponse.builder()
                .examUuid(exam.getUuid())
                .examName(exam.getName())
                .classUuid(classRoom.getUuid())
                .className(classRoom.getName())
                .section(classRoom.getSection())
                .totalStudents(totalStudents)
                .studentsWithMarks(studentsWithMarks)
                .totalSubjects(totalSubjects)
                .averagePercentage(averagePercentage)
                .passedStudents(passedStudents)
                .failedStudents(failedStudents)
                .passPercentage(passPercentage)
                .build();
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public List<ClassExamSummaryResponse> getClassExamSummaries(String examUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();
        Exam exam = examRepository.findByUuidAndCollegeId(examUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with UUID: " + examUuid));

        if (exam.getExamClasses() == null || exam.getExamClasses().isEmpty()) {
            return Collections.emptyList();
        }

        return exam.getExamClasses().stream()
                .map(ec -> getClassExamSummary(examUuid, ec.getClassRoom().getUuid()))
                .collect(Collectors.toList());
    }

    // ========== Helper Methods ==========

    private College getCollegeById(Long collegeId) {
        College college = collegeService.findById(collegeId);
        tenantAccessGuard.assertCurrentTenant(college);
        return college;
    }

    private GradeScale findGradeByMarks(Integer marks, Long collegeId) {
        return gradeScaleRepository.findGradeByMarksAndCollegeId(marks, collegeId)
                .orElse(null);
    }

    private Optional<GradeScale> findGradeByPercentage(Integer percentage, Long collegeId) {
        return gradeScaleRepository.findGradeByPercentageAndCollegeId(
                BigDecimal.valueOf(percentage), collegeId);
    }

    private ResultStatus determineResultStatus(List<StudentMarks> marks) {
        if (marks == null || marks.isEmpty()) {
            return ResultStatus.WITHHELD;
        }

        // Check if all subjects passed
        boolean allPassed = marks.stream()
                .allMatch(m -> m.getMarksObtained() != null &&
                        m.getExamSubject() != null &&
                        m.getExamSubject().getPassMarks() != null &&
                        m.getMarksObtained() >= m.getExamSubject().getPassMarks());

        if (allPassed) {
            // Could add logic to determine PROMOTED vs PASS based on CGPA or other criteria
            return ResultStatus.PASS;
        } else {
            return ResultStatus.FAIL;
        }
    }
}

