package ru.devopsing.quake3.Entities;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clients")
public class Client extends PanacheEntity {

    @Column(nullable = false)
    public int clientId;

    @Column(nullable = true)
    public String name;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<Event> history = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "match_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_client_match"))
    public Match match;
}
