package pfs;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Lobby {
    public List<User> players = new ArrayList<>();
    public String name;
    public String password;

    public Lobby(String name, String password) {
        System.out.println("New Lobby: " + name);
        this.name = name;
        this.password = password;
    }

    public void addPlayer(User user) {
        System.out.println("Adding player " + user.playername + " to lobby " + this.name);
        JSONObject senddata = new JSONObject();
        try {
            user.is_host = players.isEmpty();
            if (!players.contains(user)) players.add(user);
            String[] playerdata = {"", ""};
            for (int i = 0; i < players.size(); i++) {
                JSONObject current_player = new JSONObject();
                current_player.put("name", players.get(i).playername);
                current_player.put("host", players.get(i).is_host);
                playerdata[i] = current_player.toString();
            }

            for (User player : players) {
                senddata.put("type", 5);
                senddata.put("players", Arrays.toString(playerdata));
                Server.sendData(player, senddata);
            }
        } catch (JSONException e) {
            System.out.println(e.getMessage());
        }
    }

    public void delPlayer(User user) {
        players.remove(user);
    }
}
