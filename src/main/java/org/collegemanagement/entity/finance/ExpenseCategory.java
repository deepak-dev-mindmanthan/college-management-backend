package org.collegemanagement.entity.finance;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.collegemanagement.entity.base.BaseEntity;
import org.collegemanagement.entity.tenant.College;


import jakarta.persistence.*;
import lombok.*;
import org.collegemanagement.entity.base.BaseEntity;
import org.collegemanagement.entity.tenant.College;

@Entity
@Table(
        name = "expense_categories",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_expense_category_per_college",
                        columnNames = {"college_id", "name"}
                )
        },
        indexes = {
                @Index(name = "idx_expense_category_college", columnList = "college_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ExpenseCategory extends BaseEntity {

    /**
     * Tenant (College / School)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "college_id", nullable = false)
    private College college;

    /**
     * Category name (Electricity, Maintenance, Fuel, etc.)
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Optional description
     */
    @Column(length = 300)
    private String description;
}


