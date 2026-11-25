package ru.company.reference.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.company.reference.entity.Equipment;
import ru.company.reference.entity.EquipmentType;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, String> {

    Optional<Equipment> findByInventoryNumber(String inventoryNumber);

    List<Equipment> findByType(EquipmentType type);

    List<Equipment> findByActiveTrue();
}