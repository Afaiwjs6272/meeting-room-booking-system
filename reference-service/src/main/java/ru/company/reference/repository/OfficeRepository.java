package ru.company.reference.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.company.reference.entity.Office;

import java.util.List;
import java.util.Optional;

@Repository
public interface OfficeRepository extends JpaRepository<Office, String> {

    Optional<Office> findByCode(String code);

    List<Office> findByCity(String city);

    List<Office> findByActiveTrue();
}