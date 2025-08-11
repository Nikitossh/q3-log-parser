package ru.devopsing.quake3.Entities;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "item_pickups")
public class ItemPickup extends PanacheEntity {

    @ManyToOne
    @JoinColumn(name = "client_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_itempickup_client"))
    public Client client;

    @ManyToOne
    @JoinColumn(name = "item_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_itempickup_item"))
    public Item item;

    @Column(nullable = false)
    public String time;

    @ManyToOne
    @JoinColumn(name = "match_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_itempickup_match"))
    public Match match;
}
