
package ru.devopsing.quake3.Repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import ru.devopsing.quake3.Entities.ItemPickup;

@ApplicationScoped
public class ItemPickupRepository implements PanacheRepository<ItemPickup> {

   public ItemPickup findById(String name){
       return find("name", name).firstResult();
   }
}
