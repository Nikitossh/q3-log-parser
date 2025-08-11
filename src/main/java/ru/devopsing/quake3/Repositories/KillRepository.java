package ru.devopsing.quake3.Repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import ru.devopsing.quake3.Entities.Kill;

@ApplicationScoped
public class KillRepository implements PanacheRepository<Kill> {

   public Kill findByKillerId(Integer killerId){
       return find("killerId", killerId).firstResult();
   }
}
