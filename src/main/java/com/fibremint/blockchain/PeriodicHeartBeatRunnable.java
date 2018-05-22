package com.fibremint.blockchain;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class PeriodicHeartBeatRunnable implements Runnable {

    private HashMap<ServerInfo, Date> serverStatus;
    private int sequenceNumber;
    private int localPort;

    public PeriodicHeartBeatRunnable(HashMap<ServerInfo, Date> serverStatus, int localPort) {
        this.serverStatus = serverStatus;
        this.sequenceNumber = 0;
        this.localPort = localPort;
        
    }

    @Override
    public void run() {
    	String message;
        while(true) {
            // broadcast HeartBeat message to all peers
            message = "hb|" + String.valueOf(localPort) + "|" + String.valueOf(sequenceNumber);

            for (ServerInfo info : serverStatus.keySet()) {
                Thread thread = new Thread(new HeartBeatSenderRunnable(info, message));
                thread.start();
            }

            // increment the sequenceNumber
            sequenceNumber += 1;
            
            // sleep for two seconds
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }
        }
    }
}
