package ru.company.reference.repository;

import ru.company.reference.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, String> {

    Optional<Department> findByCode(String code);

    List<Department> findByParentDepartmentId(String parentDepartmentId);

    List<Department> findByActiveTrue();
}