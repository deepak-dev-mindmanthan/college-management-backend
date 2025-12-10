package org.collegemanagement.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.*;
import org.collegemanagement.enums.Status;

import java.util.List;

@Entity
@Table(name = "colleges")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class College {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;
    private String phone;
    private String address;

    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE; // ACTIVE, SUSPENDED

    @OneToMany(mappedBy = "college", cascade = CascadeType.ALL)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<User> users;

}
