package pfs;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static pfs.Server.*;

public class Lobby {
    public List<User> players = new ArrayList<>();
    public String name;
    public String password;

    public Lobby(String name, String password) {
        System.out.println(getTimeStamp() + "New Lobby: " + name);
        this.name = name;
        this.password = password;
    }

    public void addPlayer(User user) {
        if (!players.contains(user)) {
            System.out.println(getTimeStamp() + user.playername + " joined " + name);
            user.is_host = players.isEmpty();
            players.add(user);
            user.lobby = this;
            JSONObject senddata = new JSONObject();
            senddata.put("type", Contype.JoinLobby.ordinal());
            sendData(user, senddata);
            updatePlayers();
        }
    }

    public void delPlayer(User user) {
        System.out.println(getTimeStamp() + user.playername + " left " + name);
        user.lobby = null;
        players.remove(user);
        if(user.is_host && !players.isEmpty()) players.getFirst().is_host = true;
        updatePlayers();
        if (players.isEmpty()) {
            System.out.println(getTimeStamp() + "Removing lobby " + name + " as it is empty");
            delLobby(this);
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
                senddata.put("type", Contype.UpdatePlayers.ordinal());
                senddata.put("players", Arrays.toString(playerdata));
                JSONObject hostplayer = new JSONObject();
                hostplayer.put("type", Contype.IsHost.ordinal());
                hostplayer.put("isHost", player.is_host);
                sendData(player, senddata);
                sendData(player, hostplayer);
            }
        } catch (JSONException e) {
            System.out.println(e.getMessage());
        }
    }

    public void updatePosition(User user) {
        JSONObject data = new JSONObject();
        data.put("type", Contype.MovePlayer.ordinal());
        data.put("character", user.character);
        data.put("x", user.x);
        data.put("y", user.y);
        for (User player : players) {
            if (player == user) continue;
            sendData(player, data);
        }
    }

    public void startGame() {
        for (User player : players) {
            JSONObject senddata = new JSONObject();
            senddata.put("type", Contype.StartGame.ordinal());
            sendData(player, senddata);
        }
    }

    public void spawnUpgrade(User player, int id, int level, String updata) {
        JSONObject data = new JSONObject();
        data.put("type", Contype.SpawnUpgrade.ordinal());
        data.put("id", id);
        data.put("level", level);
        data.put("updata", updata);
        for (User user : players) {
            if (user == player) continue;
            sendData(user, data);
        }
    }

    public void spawnEnemy(User player, String enemydata) {
        JSONObject data = new JSONObject();
        data.put("type", Contype.SpawnEnemy.ordinal());
        data.put("enemydata", enemydata);
        for (User user : players) {
            if (user == player) continue;
            sendData(user, data);
        }
    }

    public void destroyInstance(User player, String instancedata) {
        JSONObject data = new JSONObject();
        data.put("type", Contype.DestroyInstance.ordinal());
        data.put("instancedata", instancedata);
        for (User user : players) {
            if (user == player) continue;
            sendData(user, data);
        }
    }
}
