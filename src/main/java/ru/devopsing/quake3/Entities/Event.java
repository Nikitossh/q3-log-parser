package ru.devopsing.quake3.Entities;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "events")
public class Event extends PanacheEntity {

    @Column(nullable = false)
    public String time;

    @Column(nullable = false)
    public int clientId;

    @Column(nullable = false)
    public String type;

    @Column(nullable = false)
    public String data;

    @ManyToOne
    @JoinColumn(name = "client_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_event_client"))
    public Client client;
}
