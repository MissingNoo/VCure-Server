package pfs;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.time.LocalDateTime;

import org.json.JSONException;
import org.json.JSONObject;

public class User implements Runnable {

    public SocketChannel channel;
    private boolean connected = true;
    private Server server;
    private String playername = "";
    private int playerid = 0;
    private int idd = 0;

    ByteBuffer wBuffer = ByteBuffer.allocate(4096);

    public User(Server server, SocketChannel channel, int idd) {
        this.server = server;
        this.channel = channel;
        this.idd = idd;
    }

    @Override
    public void run() {
        while (connected) {
            ByteBuffer buffer;
            try {
                buffer = ByteBuffer.allocate(1024);
                @SuppressWarnings("unused")
                var read = channel.read(buffer);
                if (buffer.hasArray()) {
                    String data = new String(buffer.array(), Charset.defaultCharset());
                    try {
                        JSONObject json = new JSONObject("{type : 99}");
                        //Check if valid JSON received
                        if (data.charAt(0) == '{') { json = new JSONObject(data); }
                        JSONObject senddata = new JSONObject();
                        switch (getMessageContype(Integer.parseInt(json.get("type").toString()))) {
                            case Register:
                                System.out.println(getTimeStamp() + "Register packet received!!!");
                                String id = ConexaoMySQL.register(json.get("playername").toString());
                                senddata.put("type", 1);
                                senddata.put("id", id);
                                break;
                            case Login:
                                String dbname = ConexaoMySQL.login(json.getInt("playerid"));
                                senddata.put("type", 2);
                                if (json.getString("playername").compareTo(dbname) == 0) {
                                    senddata.put("login", true);
                                    playername = dbname;
                                    playerid = json.getInt("playerid");
                                }
                                else {
                                    senddata.put("login", false);
                                    senddata.put("reason", "Invalid credentials");
                                }
                                break;
                            /*case Load:
                            	System.out.println(getTimeStamp() + "Login packet received");
                                username = json.get("usr").toString();
                                String date = json.get("date").toString();
                                String moment = json.get("moment").toString();
                                String result = ConexaoMySQL.loadinfo(username, date, moment);
                                server.sendData(this, "{\"type\" : \"4\", " + result + "}");
                                break;
                            case Save:
                            	System.out.println(getTimeStamp() + "Hour register packet received");
                            	String usr = json.get("usr").toString();
                            	String hour = json.get("hour").toString();
                            	String minute = json.get("minute").toString();
                            	date = json.get("date").toString();
                            	moment = json.get("moment").toString();
                            	ConexaoMySQL.savehours(usr, hour, minute, date, moment, this);
                            	break;*/
                            default:
                                break;
                        }
                        if (senddata.optInt("type") != 0) {
                            System.out.println(getTimeStamp() + "Sending: " + senddata.toString());
                            Server.sendData(this, senddata.toString());
                        }
                    }catch (JSONException err){
                        //System.out.println(err.toString());
                    }
                }
            } catch (IOException ex) {
                //ex.printStackTrace();
                System.out.println(channel.socket().getInetAddress().toString() + " has disconnected.");
                connected = false;
                server.removeClient(this);
                try {
                    channel.close();
                } catch (IOException ex1) {
                    ex1.printStackTrace();
                }
            }
        }
    }

    private Server.Contype getMessageContype(int type){
        Server.Contype r = Server.Contype.Null;
        switch (type) {
            case 0: r = Server.Contype.Null; break;
            case 1: r = Server.Contype.Register; break;
            case 2: r = Server.Contype.Login; break;
            //case 1: r = Server.Contype.Join; break;
            //case 3: r = Server.Contype.Login; break;
            //case 4: r = Server.Contype.Load; break;
            //case 5: r = Server.Contype.Save; break;
        }
        return r;
    }

    private String getTimeStamp(){
        return LocalDateTime.now().getHour() + ":" + LocalDateTime.now().getMinute() + ":" + LocalDateTime.now().getSecond()  + "> ";
    }
}