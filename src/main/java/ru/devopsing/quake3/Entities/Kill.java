package ru.devopsing.quake3.Entities;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "kills")
public class Kill extends PanacheEntity {

    @ManyToOne
    @JoinColumn(name = "killer_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_kill_killer"))
    public Client killer;

    @ManyToOne
    @JoinColumn(name = "victim_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_kill_victim"))
    public Client victim;

    @Column(nullable = false)
    public String time;

    @ManyToOne
    @JoinColumn(name = "weapon_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_kill_weapon"))
    public Weapon weapon;

    @ManyToOne
    @JoinColumn(name = "match_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_kill_match"))
    public Match match;

    public String mod;
}
