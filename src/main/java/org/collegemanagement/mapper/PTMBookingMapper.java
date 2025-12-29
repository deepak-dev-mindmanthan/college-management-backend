package org.collegemanagement.mapper;

import org.collegemanagement.dto.ptm.PTMBookingResponse;
import org.collegemanagement.entity.ptm.PTMBooking;

public class PTMBookingMapper {

    /**
     * Convert PTMBooking entity to PTMBookingResponse DTO
     */
    public static PTMBookingResponse toResponse(PTMBooking booking) {
        if (booking == null) {
            return null;
        }

        return PTMBookingResponse.builder()
                .uuid(booking.getUuid())
                .slotUuid(booking.getSlot() != null ? booking.getSlot().getUuid() : null)
                .slotDate(booking.getSlot() != null ? booking.getSlot().getDate() : null)
                .slotStartTime(booking.getSlot() != null ? booking.getSlot().getStartTime() : null)
                .slotEndTime(booking.getSlot() != null ? booking.getSlot().getEndTime() : null)
                .teacherUuid(booking.getSlot() != null && booking.getSlot().getParentUser() != null
                        ? booking.getSlot().getParentUser().getUuid() : null)
                .teacherName(booking.getSlot() != null && booking.getSlot().getParentUser() != null
                        ? booking.getSlot().getParentUser().getName() : null)
                .parentUuid(booking.getParent() != null ? booking.getParent().getUuid() : null)
                .parentName(booking.getParent() != null ? booking.getParent().getName() : null)
                .studentUuid(booking.getStudent() != null ? booking.getStudent().getUuid() : null)
                .studentName(booking.getStudent() != null && booking.getStudent().getUser() != null
                        ? booking.getStudent().getUser().getName() : null)
                .bookedAt(booking.getBookedAt())
                .remarks(booking.getRemarks())
                .collegeId(booking.getStudent() != null && booking.getStudent().getCollege() != null
                        ? booking.getStudent().getCollege().getId() : null)
                .build();
    }
}

