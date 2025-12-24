package org.collegemanagement.dto.teacher;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.collegemanagement.enums.DayOfWeek;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherTimetableResponse {

    private String teacherUuid;
    private String teacherName;
    private List<TimetableSlot> timetable;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimetableSlot {
        private String uuid;
        private DayOfWeek dayOfWeek;
        private Integer periodNumber;
        private String classUuid;
        private String className;
        private String section;
        private String subjectUuid;
        private String subjectName;
        private String subjectCode;
    }
}

