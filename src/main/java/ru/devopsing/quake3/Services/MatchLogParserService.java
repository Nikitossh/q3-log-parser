package ru.devopsing.quake3.Services;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
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

    // Helper method to parse userinfo string into a map
    private Map<String, String> parseUserInfo(String userInfo) {
        Map<String, String> result = new LinkedHashMap<>();
        String[] parts = userInfo.split("\\\\");
        for (int i = 0; i < parts.length - 1; i += 2) {
            String key = parts[i];
            String value = (i + 1 < parts.length) ? parts[i + 1] : "";
            result.put(key, value);
        }
        return result;
    }

    @Transactional
    public void parseLog(BufferedReader reader) {
        // Define patterns for parsing the log file
        Pattern initGamePattern = Pattern.compile("^\\s*(\\d+:\\d+)\\s+InitGame:\\s+(.*)$");
        Pattern itemPattern = Pattern.compile("^\\s*(\\d+:\\d+)\\s+Item:\\s+(\\d+)\\s+(.*)$");
        Pattern killPattern = Pattern.compile(
                "^\\s*(\\d+:\\d+)\\s+Kill:\\s+(\\d+)\\s+(\\d+)\\s+(\\d+):\\s+(.*)\\s+killed\\s+(.*)\\s+by\\s+(.*)$");

        // 0:40 (ClientConnect | ClientBegin | ClientDisconnect): 7
        Pattern clientEventPattern = Pattern.compile(
                "^\\s*(\\d+:\\d+)\\s+(ClientConnect|ClientBegin|ClientDisconnect):\\s+(\\d+)$");
        // 0:40 ClientUserinfoChanged: 7
        Pattern clientUserInfoChangedPattern = Pattern.compile(
                "^\\s*(\\d+:\\d+)\\s+(ClientUserinfoChanged):\\s+(\\d+)\\s+(n\\\\.*)$");


        // create and persist a new match
        Match match = new Match();
        matchRepository.persist(match);
        
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

                // Inside your log parsing loop:
                Matcher clientEventMatcher = clientEventPattern.matcher(line);
                if (clientEventMatcher.matches()) {
                    String time = clientEventMatcher.group(1);
                    String eventType = clientEventMatcher.group(2);
                    int clientId = Integer.parseInt(clientEventMatcher.group(3));

                    Client client = clientRepository.findByMatchAndClientId(match.id, clientId);
                    if (client == null) {
                        client = new Client();
                        client.clientId = clientId;
                        client.match = match;
                        clientRepository.persist(client);
                    }

                    Event event = new Event();
                    event.time = time;
                    event.type = eventType;
                    event.clientId = clientId;
                    event.client = client;

                    client.history.add(event);
                    eventRepository.persist(event);
                    continue;
                }

                // Handle ClientUserinfoChanged
                Matcher clientUserInfoMatcher = clientUserInfoChangedPattern.matcher(line);
                if (clientUserInfoMatcher.matches()) {
                    String time = clientUserInfoMatcher.group(1);
                    String eventType = clientUserInfoMatcher.group(2);
                    int clientId = Integer.parseInt(clientUserInfoMatcher.group(3));
                    String userInfo = clientUserInfoMatcher.group(4);

                    Map<String, String> userFields = parseUserInfo(userInfo);
                    String name = userFields.getOrDefault("n", null);

                    Client client = clientRepository.findByMatchAndClientId(match.id, clientId);
                    if (client == null) {
                        client = new Client();
                        client.clientId = clientId;
                        client.match = match;
                        clientRepository.persist(client);
                    }
                    client.name = name;

                    Event event = new Event();
                    event.time = time;
                    event.type = eventType;
                    event.clientId = clientId;
                    event.client = client;

                    client.history.add(event);
                    eventRepository.persist(event);
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
                    if (client != null) {
                        ItemPickup itemPickup = new ItemPickup();
                        itemPickup.time = time;
                        itemPickup.match = match;
                        itemPickup.client = client;

                        Item item = itemRepository.findByName(itemName);
                        if (item == null) {
                            throw new IllegalArgumentException("Unknown item: " + itemName);
                        }

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

                    if (killer != null && victim != null) {
                        Kill kill = new Kill();
                        kill.time = time;
                        kill.killer = killer;
                        kill.victim = victim;
                        kill.weapon = weaponRepository.findById(weaponId);
                        kill.match = match;
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
