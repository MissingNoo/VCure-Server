package pfs;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.time.LocalDateTime;

import org.json.JSONException;
import org.json.JSONObject;

import static pfs.Server.*;

@SuppressWarnings("unused")
public class User implements Runnable {
    public SocketChannel channel;
    private boolean connected = true;
    public String playername = "";
    public int playerid;
    public boolean is_host = false;
    public Lobby lobby;
    public int character = 0;
    public int x = 0;
    public int y = 0;

    public User(Server server, SocketChannel channel) {
        this.channel = channel;
    }

    @Override
    public void run() {
        while (connected) {
            ByteBuffer buffer;
            try {
                buffer = ByteBuffer.allocate(1024);
                var read = channel.read(buffer);
                if (buffer.hasArray()) {
                    String data = new String(buffer.array(), Charset.defaultCharset());
                    try {
                        JSONObject json;
                        JSONObject senddata = new JSONObject();
                        try {
                            json = new JSONObject(data);
                        }
                        catch (JSONException err){
                            json = new JSONObject("{type : 99, playername : \"-1\", playerid : \"-1\"}");
                        }
                        if (!json.get("playername").toString().equals(playername) && !playername.isEmpty()) {
                            json.put("type", Contype.Disconnect.ordinal());
                        }
                        switch (getMessageContype(Integer.parseInt(json.get("type").toString()))) {
                            case Register:
                                String id = ConexaoMySQL.register(json.get("playername").toString());
                                senddata.put("type", Contype.Register.ordinal());
                                senddata.put("id", id);
                                break;
                            case Login:
                                String dbname = ConexaoMySQL.login(json.getInt("playerid"));
                                senddata.put("type", Contype.Login.ordinal());
                                if (json.getString("playername").equals(dbname)) {
                                    senddata.put("login", true);
                                    playername = dbname;
                                    playerid = json.getInt("playerid");
                                    //gui.addPlayer(this);
                                }
                                else {
                                    senddata.put("login", false);
                                    senddata.put("reason", "Invalid credentials");
                                }
                                break;
                            case ScoreSubmit:
                                ConexaoMySQL.submitScore(this, json);
                                break;
                            case CreateLobby:
                                createLobby(this, json.getString("name"), json.getString("password"));
                                break;
                            case JoinLobby:
                                joinLobby(this, json.getString("name"), json.getString("password"));
                                break;
                            case ListLobbies:
                                listLobbies(this);
                                break;
                            case Disconnect:
                                connected = false;
                                System.out.println(getTimeStamp() + "Player " + playername + "/" + channel.socket().getInetAddress().toString() + " has disconnected.");
                                removeClient(this);
                                try {
                                    channel.close();
                                } catch (IOException ex1) {
                                    System.out.println(ex1.getMessage());
                                }
                                break;
                            case UpdatePlayers:
                                lobby.updatePlayers();
                                break;
                            case LeaveLobby:
                                lobby.delPlayer(this);
                                senddata.put("type", Contype.LeaveLobby.ordinal());
                                break;
                            case SelectCharacter:
                                character = json.getInt("character");
                                lobby.updatePlayers();
                                break;
                            case StartGame:
                                if (is_host) {
                                    lobby.startGame();
                                }
                                break;
                            case MovePlayer:
                                x = json.getInt("x");
                                y = json.getInt("y");
                                lobby.updatePosition(this);
                                break;
                            case SpawnUpgrade:
                                lobby.spawnUpgrade(this, json.getInt("id"), json.getInt("level"), json.getString("updata"));
                                break;
                            case SpawnEnemy:
                                lobby.spawnEnemy(this, json.getString("enemyinfo"));
                                break;
                            case DestroyInstance:
                                lobby.destroyInstance(this, json.getString("instancedata"));
                                break;
                            default:
                                break;
                        }
                        if (senddata.optInt("type") != 0) {
                            System.out.println(getTimeStamp() + "Sending: " + senddata);
                            sendData(this, senddata);
                        }
                    }catch (Exception err){
                        System.out.println(err.getMessage());
                    }
                }
            } catch (IOException ex) {
                System.out.println(getTimeStamp() + channel.socket().getInetAddress().toString() + " has disconnected.");
                connected = false;
                removeClient(this);
                try {
                    channel.close();
                } catch (IOException ex1) {
                    System.out.println(ex1.getMessage()); //ex1.printStackTrace();
                }
            }
        }
    }

    private Contype getMessageContype(int type){
        return switch (type) {
            default -> Contype.Null;
            case 1 -> Contype.Register;
            case 2 -> Contype.Login;
            case 3 -> Contype.ScoreSubmit;
            case 4 -> Contype.CreateLobby;
            case 5 -> Contype.JoinLobby;
            case 6 -> Contype.ListLobbies;
            case 7 -> Contype.Disconnect;
            case 8 -> Contype.UpdatePlayers;
            case 9 -> Contype.LeaveLobby;
            case 10 -> Contype.SelectCharacter;
            case 11 -> Contype.IsHost;
            case 12 -> Contype.StartGame;
            case 13 -> Contype.MovePlayer;
            case 14 -> Contype.SpawnUpgrade;
            case 15 -> Contype.SpawnEnemy;
            case 16 -> Contype.DestroyInstance;
        };
    }
}