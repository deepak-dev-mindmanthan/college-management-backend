package org.collegemanagement.services;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {

    double getAttendancePercentage(long studentId);
}
