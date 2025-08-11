
package ru.devopsing.quake3.Repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import ru.devopsing.quake3.Entities.Weapon;

@ApplicationScoped
public class WeaponRepository implements PanacheRepository<Weapon> {

   public Weapon findByName(String name){
       return find("name", name).firstResult();
   }
}
