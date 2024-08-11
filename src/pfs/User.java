package pfs;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.time.LocalDateTime;

import org.json.JSONException;
import org.json.JSONObject;
@SuppressWarnings("unused")
public class User implements Runnable {
    public SocketChannel channel;
    private boolean connected = true;
    public String playername = "";
    public int playerid;
    public boolean is_host = false;
    public Lobby lobby;

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
                            json.put("type", Server.Contype.Disconnect.ordinal());
                        }
                        switch (getMessageContype(Integer.parseInt(json.get("type").toString()))) {
                            case Register:
                                String id = ConexaoMySQL.register(json.get("playername").toString());
                                senddata.put("type", Server.Contype.Register.ordinal());
                                senddata.put("id", id);
                                break;
                            case Login:
                                String dbname = ConexaoMySQL.login(json.getInt("playerid"));
                                senddata.put("type", Server.Contype.Login.ordinal());
                                if (json.getString("playername").equals(dbname)) {
                                    senddata.put("login", true);
                                    playername = dbname;
                                    playerid = json.getInt("playerid");
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
                                Server.createLobby(this, json.getString("name"), json.getString("password"));
                                break;
                            case JoinLobby:
                                Server.joinLobby(this, json.getString("name"), json.getString("password"));
                                break;
                            case ListLobbies:
                                Server.listLobbies(this);
                                break;
                            case Disconnect:
                                connected = false;
                                System.out.println(Server.getTimeStamp() + "Player " + playername + "/" + channel.socket().getInetAddress().toString() + " has disconnected.");
                                Server.removeClient(this);
                                try {
                                    channel.close();
                                } catch (IOException ex1) {
                                    System.out.println(ex1.getMessage());
                                }
                                break;
                            default:
                                break;
                        }
                        if (senddata.optInt("type") != 0) {
                            System.out.println(Server.getTimeStamp() + "Sending: " + senddata);
                            Server.sendData(this, senddata);
                        }
                    }catch (Exception err){
                        System.out.println(err.getMessage());
                    }
                }
            } catch (IOException ex) {
                System.out.println(Server.getTimeStamp() + channel.socket().getInetAddress().toString() + " has disconnected.");
                connected = false;
                Server.removeClient(this);
                try {
                    channel.close();
                } catch (IOException ex1) {
                    System.out.println(ex1.getMessage()); //ex1.printStackTrace();
                }
            }
        }
    }

    private Server.Contype getMessageContype(int type){
        return switch (type) {
            default -> Server.Contype.Null;
            case 1 -> Server.Contype.Register;
            case 2 -> Server.Contype.Login;
            case 3 -> Server.Contype.ScoreSubmit;
            case 4 -> Server.Contype.CreateLobby;
            case 5 -> Server.Contype.JoinLobby;
            case 6 -> Server.Contype.ListLobbies;
            case 7 -> Server.Contype.Disconnect;
        };
    }
}