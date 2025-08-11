
package ru.devopsing.quake3.Repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import ru.devopsing.quake3.Entities.Item;

@ApplicationScoped
public class ItemRepository implements PanacheRepository<Item> {

   public Item findByName(String name){
       return find("name", name).firstResult();
   }
}
