package com.fibremint.blockchain;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class HeartBeatSenderRunnable implements Runnable{

    private ServerInfo destServer;
    private String message;

    public HeartBeatSenderRunnable(ServerInfo destServer, String message) {
        this.destServer = destServer;
        this.message = message;
    }

    @Override
    public void run() {
        try {
            // create socket with a timeout of 2 seconds
            Socket s = new Socket();
            s.connect(new InetSocketAddress(this.destServer.getHost(), this.destServer.getPort()), 2000);
            PrintWriter pw =  new PrintWriter(s.getOutputStream(), true);
            
            // send the message forward
        	pw.println(message);
        	pw.flush();

            // close printWriter and socket
            pw.close();
            // sleep for two seconds
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }
            s.close();
            
        } catch (IOException e) {
        }
    }
}