package ru.devopsing.quake3.Repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import ru.devopsing.quake3.Entities.Client;

@ApplicationScoped
public class ClientRepository implements PanacheRepository<Client> {

    // Custom query to find a Client by Match ID and Client ID
    public Client findByMatchAndClientId(Long matchId, int clientId) {
        return find("match.id = ?1 and clientId = ?2", matchId, clientId).firstResult();
    }

    // update:
    // public int updateNameAndTimeById(Client client) {
        // return update("name = ?1, time = ?2 where id = ?3", client.name, client.time, client.id);
    // }
}
