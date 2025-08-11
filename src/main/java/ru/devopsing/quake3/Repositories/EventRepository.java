
package ru.devopsing.quake3.Repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import ru.devopsing.quake3.Entities.Event;

@ApplicationScoped
public class EventRepository implements PanacheRepository<Event> {
}
