package org.collegemanagement.entity;

import jakarta.persistence.*;
import lombok.*;
import org.collegemanagement.enums.FeeStatus;

import java.time.LocalDate;

@Entity
@Table(name = "fees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    private Double amount;
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    private FeeStatus status; // PENDING, PAID

    // Constructors, Getters, and Setters
}
