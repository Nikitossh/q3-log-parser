package ru.devopsing.quake3.Repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import ru.devopsing.quake3.Entities.Match;

@ApplicationScoped
public class MatchRepository implements PanacheRepository<Match> {

   public Match findByName(String name){
       return find("name", name).firstResult();
   }

//    public List<Match> findAlive(){
//        return list("status", Status.Alive);
//    }

//    public void deleteStefs(){
    //    delete("name", "Stef");
//   }
}
