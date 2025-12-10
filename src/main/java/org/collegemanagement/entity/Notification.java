package org.collegemanagement.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String message;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver; // Student or Teacher

    private LocalDateTime timestamp;

    private boolean isRead;

}
