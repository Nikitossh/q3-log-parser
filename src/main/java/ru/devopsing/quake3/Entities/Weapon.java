package ru.devopsing.quake3.Entities;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "weapons")
public class Weapon extends PanacheEntity {

    @Column(nullable = false)
    public Long weaponId;

    @Column(nullable = false)
    public String name;
}
