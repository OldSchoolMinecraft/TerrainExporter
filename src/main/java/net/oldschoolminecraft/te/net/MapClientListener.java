package net.oldschoolminecraft.te.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class MapClientListener
{
    private ServerSocket serverSocket;
    private ArrayList<MapClient> clients = new ArrayList<>();

    public void start()
    {
        new Thread(() ->
        {
            try
            {
                while (true)
                {
                    Socket socket = serverSocket.accept();
                    if (socket == null) continue;
                    MapClient mapClient = new MapClient(socket);
                    mapClient.start();
                    clients.add(mapClient);
                }
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
        }).start();
    }
}
