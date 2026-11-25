package ru.company.reference.repository;

import ru.company.reference.entity.Employee;
import ru.company.reference.entity.EmployeeRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String> {

    Optional<Employee> findByEmployeeNumber(String employeeNumber);

    Optional<Employee> findByEmail(String email);

    List<Employee> findByDepartmentId(String departmentId);

    List<Employee> findByRole(EmployeeRole role);

    Page<Employee> findByActiveTrue(Pageable pageable);

    @Query("SELECT e FROM Employee e WHERE " +
            "LOWER(e.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(e.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(e.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(e.employeeNumber) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Employee> searchEmployees(@Param("search") String search, Pageable pageable);

    List<Employee> findByManagerEmployeeId(String managerId);

    @Query("SELECT e FROM Employee e WHERE e.calendarIntegrationEnabled = true")
    List<Employee> findEmployeesWithCalendarIntegration();

}