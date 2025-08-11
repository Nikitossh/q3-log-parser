package ru.devopsing.quake3.Repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import ru.devopsing.quake3.Entities.InitGame;

@ApplicationScoped
public class InitGameRepository implements PanacheRepository<InitGame> {

   public InitGame findByName(String name){
       return find("name", name).firstResult();
   }
}
