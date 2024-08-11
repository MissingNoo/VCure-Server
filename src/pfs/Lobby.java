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
        System.out.println(Server.getTimeStamp() + "New Lobby: " + name);
        this.name = name;
        this.password = password;
    }

    public void addPlayer(User user) {
        if (!players.contains(user)) {
            System.out.println(Server.getTimeStamp() + user.playername + " joined " + name);
            user.is_host = players.isEmpty();
            players.add(user);
            user.lobby = this;
            JSONObject senddata = new JSONObject();
            senddata.put("type", Server.Contype.JoinLobby.ordinal());
            Server.sendData(user, senddata);
            updatePlayers();
        }
    }

    public void delPlayer(User user) {
        System.out.println(Server.getTimeStamp() + user.playername + " left " + name);
        user.lobby = null;
        players.remove(user);
        updatePlayers();
        if (players.isEmpty()) {
            System.out.println(Server.getTimeStamp() + "Removing lobby " + name + " as it is empty");
            Server.delLobby(this);
        }
    }

    public void updatePlayers(){
        JSONObject senddata = new JSONObject();
        String[] playerdata = {"", ""};
        try {
            for (int i = 0; i < players.size(); i++) {
                JSONObject current_player = new JSONObject();
                current_player.put("name", players.get(i).playername);
                current_player.put("character", players.get(i).character);
                current_player.put("host", players.get(i).is_host);
                playerdata[i] = current_player.toString();
            }

            for (User player : players) {
                senddata.put("type", Server.Contype.UpdatePlayers.ordinal());
                senddata.put("players", Arrays.toString(playerdata));
                Server.sendData(player, senddata);
            }
        } catch (JSONException e) {
            System.out.println(e.getMessage());
        }
    }
}
