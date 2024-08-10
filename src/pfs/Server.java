package pfs;
import org.json.JSONObject;

import java.io.IOException;
import java.io.*;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Server {

    enum Contype {
        Null,
        Register,
        Login,
        ScoreSubmit,
        CreateLobby,
        JoinLobby,
        ListLobbies
    }

    public static List<User> clients;
    public static List<Lobby> lobbies;
    private ServerSocketChannel socket;
    private boolean running;
    private int lastid;

    public Server(int port) {
        Server.clients = new ArrayList<User>();
        Server.lobbies = new ArrayList<Lobby>();
        this.running = false;

        System.out.print(getTimeStamp() + "[Server] Trying to Listen on Port : " + port + "...");
        try {
            ServerSocketChannel channel = ServerSocketChannel.open();
            channel.socket().bind(new java.net.InetSocketAddress(port));
            System.out.println("Success!");
            channel.configureBlocking(false);
            socket = channel;
            running = true;
        } catch (IOException e) {
            System.err.println("Failed!");
            socket = null;
            running = false;
        }

        // Server loop
        while (running) {
            try {
                // Sleep the thread
                Thread.sleep(1);
                // Check for new connections
                SocketChannel newChannel = socket.accept();
                // If a connection is found, create a User and add it to
                // the client list
                if (newChannel != null) {
                    System.out.println("New Connection " + newChannel.socket().getInetAddress().toString());
                    ++lastid;
                    User c = new User(this, newChannel, lastid);
                    Thread t = new Thread(c);
                    t.start();
                    clients.add(c);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void removeClient(User user) {
        clients.remove(user);
    }

    public static void sendData(User user, JSONObject json){
        try {
            String data = json.toString() + ";";
            OutputStream out = user.channel.socket().getOutputStream();
            BufferedOutputStream bout = new BufferedOutputStream(out);
            byte buf[] = data.getBytes();
            bout.flush();
            bout.write(buf);
            bout.flush();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void createLobby(User owner, String name, String password) {
        Lobby l = new Lobby(name, password);
        l.addPlayer(owner);
        lobbies.add(l);
    }

    public static void joinLobby(User user, String name, String password) {
        for (Lobby lobby : lobbies) {
            try {
                System.out.println("Lobby " + lobby.name + " : " + name + " : " + lobby.name.equals(name));
                if (lobby.name.equals(name)) {
                    lobby.addPlayer(user);
                    break;
                }
            }
            catch (Exception e) {
                System.out.println(e.getMessage());
            }

        }
    }

    public static void listLobbies(User user) {
        try {
            List<String> lobbyList = new ArrayList<String>();
            for (Lobby lobby : lobbies) {
                JSONObject lobbyinfo = new JSONObject();
                lobbyinfo.put("name", lobby.name);
                lobbyinfo.put("totalplayers", lobby.players.size());
                lobbyList.add(lobbyinfo.toString());
            }
            JSONObject data = new JSONObject();
            data.put("type", 6);
            data.put("lobbies", lobbyList.toString());
            sendData(user, data);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static String getTimeStamp(){
        int hour = LocalDateTime.now().getHour();
        int minute = LocalDateTime.now().getMinute();
        int second = LocalDateTime.now().getSecond();
        String hourstring = String.valueOf(hour);
        if (hour < 9) {
            hourstring = "0" + String.valueOf(hour);
        }
        String minutestring = String.valueOf(minute);
        if (minute < 9) {
            minutestring = "0" + String.valueOf(minute);
        }
        String secondstring = String.valueOf(second);
        if (second < 9) {
            secondstring = "0" + String.valueOf(second);
        }
        return "[" + hourstring + ":" + minutestring + ":" + secondstring + "] ";
    }

    public static void main(String... args) {
        for (int i = 0; i < 30; i++) {
            System.out.println();
        }
        ConexaoMySQL.getConexaoMySQL();
        System.out.println(ConexaoMySQL.statusConection());
        new Server(21319);
    }

}