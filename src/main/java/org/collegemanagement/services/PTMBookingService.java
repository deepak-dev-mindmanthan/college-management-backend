package org.collegemanagement.services;

import org.collegemanagement.dto.ptm.CreatePTMBookingRequest;
import org.collegemanagement.dto.ptm.PTMBookingResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface PTMBookingService {

    /**
     * Create a new PTM booking
     */
    PTMBookingResponse createPTMBooking(CreatePTMBookingRequest request);

    /**
     * Cancel a PTM booking
     */
    void cancelPTMBooking(String bookingUuid);

    /**
     * Get PTM booking by UUID
     */
    PTMBookingResponse getPTMBookingByUuid(String bookingUuid);

    /**
     * Get all PTM bookings with pagination
     */
    Page<PTMBookingResponse> getAllPTMBookings(Pageable pageable);

    /**
     * Get PTM bookings by parent UUID
     */
    Page<PTMBookingResponse> getPTMBookingsByParent(String parentUuid, Pageable pageable);

    /**
     * Get PTM bookings by student UUID
     */
    Page<PTMBookingResponse> getPTMBookingsByStudent(String studentUuid, Pageable pageable);

    /**
     * Get PTM bookings by teacher UUID
     */
    Page<PTMBookingResponse> getPTMBookingsByTeacher(String teacherUuid, Pageable pageable);

    /**
     * Get PTM bookings by date
     */
    Page<PTMBookingResponse> getPTMBookingsByDate(LocalDate date, Pageable pageable);
}

