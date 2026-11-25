package ru.company.reference.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import ru.company.common.exception.BusinessException;
import ru.company.reference.dto.CreateEmployeeRequest;
import ru.company.reference.dto.EmployeeResponse;
import ru.company.reference.dto.UpdateEmployeeRequest;
import ru.company.reference.entity.Department;
import ru.company.reference.entity.Employee;
import ru.company.reference.entity.EmployeeRole;
import ru.company.reference.exception.ResourceNotFoundException;
import ru.company.reference.mapper.EmployeeMapper;
import ru.company.reference.repository.DepartmentRepository;
import ru.company.reference.repository.EmployeeRepository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeMapper employeeMapper;

    public EmployeeRole getEmployeeRole(String userId) {
        Employee employee = employeeRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", userId));
        return employee.getRole() != null ? employee.getRole() : EmployeeRole.EMPLOYEE;
    }

    public EmployeeResponse getEmployee(String id) {
        log.debug("Fetching employee with id: {}", id);

        HttpServletRequest request = getCurrentHttpRequest();

        String internalHeader = request != null ? request.getHeader("X-Internal-Request") : null;
        boolean isInternal = "true".equalsIgnoreCase(internalHeader);

        if (!isInternal) {
            String currentUserId = request != null ? request.getHeader("X-User-Id") : null;
            if (currentUserId == null) {
                throw new SecurityException("X-User-Id header is required for external requests");
            }
            checkAccess(id, currentUserId);
        } else {
            log.debug("Internal request - skipping security check for user: {}", id);
        }

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));

        return employeeMapper.toResponse(employee);
    }

    public EmployeeResponse getEmployeePublic(String id) {
        log.debug("Public fetch employee with id: {}", id);
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
        return employeeMapper.toResponse(employee);
    }

    public Page<EmployeeResponse> getAllEmployees(Pageable pageable, String currentUserId) {
        EmployeeRole currentRole = getEmployeeRole(currentUserId);

        if (EmployeeRole.isAdmin(currentRole.name())) {
            return employeeRepository.findByActiveTrue(pageable)
                    .map(employeeMapper::toResponse);
        }

        return employeeRepository.findById(currentUserId)
                .map(employee -> {
                    List<EmployeeResponse> list = Collections.singletonList(employeeMapper.toResponse(employee));
                    return (Page<EmployeeResponse>) new PageImpl<>(list, pageable, 1);
                })
                .orElse(Page.empty(pageable));
    }

    public Page<EmployeeResponse> searchEmployees(String search, Pageable pageable) {
        return employeeRepository.searchEmployees(search, pageable)
                .map(employeeMapper::toResponse);
    }

    public List<EmployeeResponse> getEmployeesByDepartment(String departmentId) {
        return employeeRepository.findByDepartmentId(departmentId).stream()
                .map(employeeMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public EmployeeResponse createEmployee(CreateEmployeeRequest request, String userId ) {
        EmployeeRole currentRole = getEmployeeRole(userId);

        if (!EmployeeRole.isAdmin(currentRole.name())) {
            throw new SecurityException("Only ADMIN can create employees");
        }
        log.info("Creating new employee with number: {}", request.getEmployeeNumber());

        if (employeeRepository.findByEmployeeNumber(request.getEmployeeNumber()).isPresent()) {
            throw new BusinessException("Employee with number " + request.getEmployeeNumber() + " already exists");
        }

        if (request.getEmail() != null && employeeRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BusinessException("Employee with email " + request.getEmail() + " already exists");
        }

        Employee employee = employeeMapper.toEntity(request);

        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getDepartmentId()));
            employee.setDepartment(department);
        }

        Employee saved = employeeRepository.save(employee);
        log.info("Employee created with id: {}", saved.getId());

        return employeeMapper.toResponse(saved);
    }

    @Transactional
    public EmployeeResponse updateEmployee(String id, UpdateEmployeeRequest request, String userId) {
        EmployeeRole currentRole = getEmployeeRole(userId);
        log.info("Updating employee with id: {}", id);

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));

        if (!EmployeeRole.isAdmin(currentRole.name())) {
                Employee currentUser = employeeRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", userId));
            } else if (!userId.equals(id)) {
                throw new SecurityException("Access denied: You can only update your own profile");
        }

        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getDepartmentId()));
            employee.setDepartment(department);
        }

        employeeMapper.updateEntity(employee, request);

        Employee updated = employeeRepository.save(employee);
        return employeeMapper.toResponse(updated);
    }

    @Transactional
    public void deleteEmployee(String id) {
        EmployeeRole currentRole = getEmployeeRole(id);

        if (!EmployeeRole.canDeleteUsers(currentRole.name())) {
            throw new SecurityException("Only ADMIN can delete employees");
        }
        log.warn("Deleting employee with id: {}", id);
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
        employeeRepository.delete(employee);
    }

    private void checkAccess(String targetUserId, String currentUserId) {
        Employee currentUser = employeeRepository.findById(currentUserId)
                .orElseThrow(() -> new SecurityException("Current user not found: " + currentUserId));

        EmployeeRole currentRole = currentUser.getRole() != null ? currentUser.getRole() : EmployeeRole.EMPLOYEE;

        if (EmployeeRole.isAdmin(currentRole.name())) {
            return;
        }

        if (!currentUserId.equals(targetUserId)) {
            throw new SecurityException("Access denied: You can only view your own profile");
        }
    }

    private HttpServletRequest getCurrentHttpRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }
}