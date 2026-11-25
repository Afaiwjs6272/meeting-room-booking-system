package ru.company.reference.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "employees", indexes = {
        @Index(name = "idx_employee_number", columnList = "employee_number"),
        @Index(name = "idx_email", columnList = "email"),
        @Index(name = "idx_department_id", columnList = "department_id")
})
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Employee extends BaseEntity {

    @Column(name = "employee_number", nullable = false, unique = true)
    private String employeeNumber;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "middle_name")
    private String middleName;

    @Column(unique = true)
    private String email;

    private String phone;

    private String position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmployeeRole role = EmployeeRole.EMPLOYEE;

    @Column(name = "manager_employee_id")
    private String managerEmployeeId;

    @Column(name = "telegram_chat_id")
    private String telegramChatId;

    @ElementCollection
    @CollectionTable(name = "employee_skills",
            joinColumns = @JoinColumn(name = "employee_id"))
    @Column(name = "skill")
    private Set<String> skills = new HashSet<>();

    @Column(name = "calendar_integration_enabled")
    private Boolean calendarIntegrationEnabled = true;
}