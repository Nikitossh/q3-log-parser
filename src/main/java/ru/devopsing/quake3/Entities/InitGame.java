package ru.devopsing.quake3.Entities;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "init_games")
public class InitGame extends PanacheEntity {

    @OneToOne(mappedBy = "initGame", fetch = FetchType.LAZY)
    public Match match;

    @Column(name = "map_name", nullable = false)
    public String mapName;

    @Column(name = "frag_limit", nullable = false)
    public int fragLimit;

    @Column(name = "time_limit", nullable = false)
    public int timeLimit;

    @Column(name = "time", nullable = false)
    public String time;

}
