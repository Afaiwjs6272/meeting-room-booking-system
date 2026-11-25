package ru.company.reference.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "equipment")
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Equipment extends BaseEntity {

    @Column(name = "inventory_number", nullable = false, unique = true)
    private String inventoryNumber;

    @Column(nullable = false)
    private String name;

    private String model;

    private String manufacturer;

    @Enumerated(EnumType.STRING)
    private EquipmentType type;

    private String description;

    private Integer quantity;

    @Column(name = "is_portable")
    private Boolean isPortable = false;
}