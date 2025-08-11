package ru.devopsing.quake3.Services;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import ru.devopsing.quake3.Entities.Client;
import ru.devopsing.quake3.Entities.Event;
import ru.devopsing.quake3.Entities.InitGame;
import ru.devopsing.quake3.Entities.Item;
import ru.devopsing.quake3.Entities.ItemPickup;
import ru.devopsing.quake3.Entities.Kill;
import ru.devopsing.quake3.Entities.Match;
import ru.devopsing.quake3.Entities.Weapon;
import ru.devopsing.quake3.Repositories.ClientRepository;
import ru.devopsing.quake3.Repositories.EventRepository;
import ru.devopsing.quake3.Repositories.InitGameRepository;
import ru.devopsing.quake3.Repositories.ItemPickupRepository;
import ru.devopsing.quake3.Repositories.ItemRepository;
import ru.devopsing.quake3.Repositories.KillRepository;
import ru.devopsing.quake3.Repositories.MatchRepository;
import ru.devopsing.quake3.Repositories.WeaponRepository;

@ApplicationScoped
public class MatchLogParserService {

    @Inject
    MatchRepository matchRepository;

    @Inject
    InitGameRepository initGameRepository;

    @Inject
    ClientRepository clientRepository;

    @Inject
    KillRepository killRepository;

    @Inject
    ItemRepository itemRepository;

    @Inject
    ItemPickupRepository itemPickupRepository;

    @Inject
    WeaponRepository weaponRepository;

    @Inject
    EventRepository eventRepository;

    @Transactional
    public void parseLog(BufferedReader reader) {
        Match match = new Match();
        matchRepository.persist(match); // First, we persist a new match

        // Patterns to match lines in the log
        Pattern initGamePattern = Pattern.compile("^\\s*(\\d+:\\d+)\\s+InitGame:\\s+(.*)$");
        Pattern clientConnectPattern = Pattern.compile("^\\s*(\\d+:\\d+)\\s+ClientConnect:\\s+(\\d+)$");
        Pattern clientUserInfoChangedPattern = Pattern
                // .compile("^\\s*(\\d+:\\d+)\\s+ClientUserinfoChanged:\\s+(\\d+)\\s+n\\\\(.*?)\\\\");
                .compile(
                        "(\\d+:\\d+) (ClientUserinfoChanged): (\\d+) n\\\\(\\w+)\\\\t\\\\\\d+\\\\model\\\\(\\S+)\\\\.*");
        Pattern clientBeginPattern = Pattern.compile("^\\s*(\\d+:\\d+)\\s+ClientBegin:\\s+(\\d+)$");
        Pattern itemPattern = Pattern.compile("^\\s*(\\d+:\\d+)\\s+Item:\\s+(\\d+)\\s+(.*)$");
        Pattern killPattern = Pattern.compile(
                "^\\s*(\\d+:\\d+)\\s+Kill:\\s+(\\d+)\\s+(\\d+)\\s+(\\d+):\\s+(.*)\\s+killed\\s+(.*)\\s+by\\s+(.*)$");

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                System.out.println(line);

                Matcher initGameMatcher = initGamePattern.matcher(line);
                if (initGameMatcher.matches()) {
                    InitGame initGame = new InitGame();
                    initGame.time = initGameMatcher.group(1);
                    match.initGame = initGame;
                    initGame.match = match;
                    String[] params = line.split("\\\\");
                    for (int i = 1; i < params.length; i += 2) {
                        String key = params[i];
                        String value = params[i + 1];
                        switch (key) {
                            case "mapname":
                                initGame.mapName = value;
                                break;
                            case "fraglimit":
                                initGame.fragLimit = Integer.parseInt(value);
                                break;
                            case "timelimit":
                                initGame.timeLimit = Integer.parseInt(value);
                                break;
                        }
                    }
                    initGameRepository.persist(initGame);
                    matchRepository.persist(match);
                    continue;
                }

                // Match ClientConnect
                // Example log lines:
                // 0:40 ClientConnect: 7
                Matcher clientConnectMatcher = clientConnectPattern.matcher(line);
                if (clientConnectMatcher.matches()) {
                    Client client = new Client();
                    clientRepository.persist(client);
                    client.match = match;

                    int clientId = (Integer.parseInt(clientConnectMatcher.group(2)));
                    
                    Event event = new Event();
                    event.type = "ClientConnect";
                    event.time = clientConnectMatcher.group(1);
                    event.clientId = clientId;
                    event.client = client; // Set the associated client
                    event.data = "Some default or meaningful data"; // Ensure 'data' is not null
                    
                    client.history.add(event);
                    eventRepository.persist(event); // Persist the event

                    client.clientId = clientId;
                    continue;
                }

                // Match ClientBegin
                // Example log lines:
                // 0:40 ClientBegin: 7
                Matcher clientBeginMatcher = clientBeginPattern.matcher(line);
                if (clientBeginMatcher.matches()) {


                    int clientId = Integer.parseInt(clientBeginMatcher.group(2));
                    Client client = clientRepository.findByMatchAndClientId(match.id, clientId);
                    client.match = match;
                    
                    Event event = new Event();
                    event.type = "ClientBegin";
                    event.time = clientBeginMatcher.group(1);
                    event.clientId = clientId;
                    event.client = client; // Set the associated client
                    event.data = "Some default or meaningful data"; // Ensure 'data' is not null
                    
                    client.history.add(event);
                    eventRepository.persist(event); // Persist the event

                    continue;
                }

                // Match ClientUserinfoChanged
                // 0:10 ClientUserinfoChanged: 0
                // n\Gorre\t\2\model\visor/gorre\hmodel\visor/gorre\c1\4\c2\5\hc\100\w\0\l\0\skill\
                // 4.00\tt\0\tl\1
                Matcher clientUserInfoChangedMatcher = clientUserInfoChangedPattern.matcher(line);
                if (clientUserInfoChangedMatcher.matches()) {

                    // Extract values
                    String time = clientUserInfoChangedMatcher.group(1); // e.g., "0:10"
                    String eventType = clientUserInfoChangedMatcher.group(2); // e.g., "ClientUserinfoChanged"
                    int clientId = Integer.parseInt(clientUserInfoChangedMatcher.group(3)); // e.g., 0
                    String name = clientUserInfoChangedMatcher.group(4); // e.g., "Gorre"
                   
                    Client client = clientRepository.findByMatchAndClientId(match.id, clientId);
                    client.name = name;
                    client.match = match;

                    Event event = new Event();
                    event.type = "ClientConnect";
                    event.time = time;
                    event.clientId = clientId;
                    event.client = client; // Set the associated client
                    event.data = eventType; // Ensure 'data' is not null
                    
                    eventRepository.persist(event); // Persist the event
                    client.history.add(event);

                    continue;
                }

                // Match Item Pickup
                // 0:10 Item: 0 item_armor_combat
                Matcher itemMatcher = itemPattern.matcher(line);
                if (itemMatcher.matches()) {
                    String time = itemMatcher.group(1).toString();
                    int clientId = Integer.parseInt(itemMatcher.group(2));
                    String itemName = itemMatcher.group(3);

                    Client client = clientRepository.findByMatchAndClientId(match.id, clientId);
                    System.out.println("Client ID: " + client.clientId);
                    if (client != null) {
                        ItemPickup itemPickup = new ItemPickup();
                        itemPickup.time = time;
                        itemPickup.match = match;
                        itemPickup.client = client;

                        Item item = new Item();
                        item.name = itemName;
                        itemRepository.persist(item);
                        itemPickup.item = item;
                        itemPickupRepository.persist(itemPickup);
                    }
                    continue;
                }

                // Match Kill
                // 0:56 Kill: 7 5 1: Grunt killed Hossman by MOD_SHOTGUN
                Matcher killMatcher = killPattern.matcher(line);
                if (killMatcher.matches()) {
                    String time = killMatcher.group(1).toString();
                    int killerId = Integer.parseInt(killMatcher.group(2));
                    int victimId = Integer.parseInt(killMatcher.group(3));
                    Long weaponId = Long.parseLong(killMatcher.group(4));
                    String mod = killMatcher.group(7);

                    Client killer = clientRepository.findByMatchAndClientId(match.id, killerId);
                    Client victim = clientRepository.findByMatchAndClientId(match.id, victimId);

                    Weapon weapon = weaponRepository.findById(weaponId);
                    if (weapon == null) {
                        Weapon w = new Weapon();
                        w.name = mod;
                        w.weaponId = weaponId;
                        weaponRepository.persist(w);
                        weapon = w;
                    }

                    if (killer != null && victim != null) {
                        Kill kill = new Kill();
                        kill.time = time;
                        kill.killer = killer;
                        kill.victim = victim;
                        kill.weapon = weapon;
                        kill.mod = mod;
                        killRepository.persist(kill);
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }
}
