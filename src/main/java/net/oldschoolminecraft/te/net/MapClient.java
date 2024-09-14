package net.oldschoolminecraft.te.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class MapClient extends Thread
{
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private String negUUID;

    public MapClient(Socket socket)
    {
        this.socket = socket;

        try
        {
            this.dis = new DataInputStream(socket.getInputStream());
            this.dos = new DataOutputStream(socket.getOutputStream());
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    @Override
    public void run()
    {
        while (socket.isConnected())
        {
            try
            {
                int packetID = this.dis.readInt();

                if (packetID == 0) // keep alive
                    sendPacket(new MapKeepAlive());

                if (packetID == 1) // negotiate
                {
                    //
                }

                if (packetID == 2) // chunk data
                {
                    //
                }
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
        }
    }

    public void sendPacket(MapPacket packet)
    {
        //
    }
}
