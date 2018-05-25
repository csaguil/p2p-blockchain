package com.fibremint.blockchain;

import com.fibremint.blockchain.blockchain.Blockchain;
import com.fibremint.blockchain.blockchain.BlockchainServerRunnable;
import com.fibremint.blockchain.net.HeartBeatPeriodicRunnable;
import com.fibremint.blockchain.net.PeriodicCatchupRunnable;
import com.fibremint.blockchain.blockchain.PeriodicCommitRunnable;
import com.fibremint.blockchain.net.ServerInfo;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;

public class BlockchainServer {

    public static void main(String[] args) {
        if (args.length != 3) {
            return;
        }

        int localPort = 0;
        int remotePort = 0;
        String remoteHost = null;

        try {
            localPort = Integer.parseInt(args[0]);
            remoteHost = args[1];
            remotePort = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
		System.out.println("here");
        Blockchain blockchain = new Blockchain();

        HashMap<ServerInfo, Date> remoteServerStatus = new HashMap<ServerInfo, Date>();
        remoteServerStatus.put(new ServerInfo(remoteHost, remotePort), new Date());

        PeriodicCommitRunnable pcr = new PeriodicCommitRunnable(blockchain);
        Thread pct = new Thread(pcr);
        pct.start();
        
        //periodically send heartbeats
        new Thread(new HeartBeatPeriodicRunnable(remoteServerStatus, localPort)).start();
        
        //periodically catchup
        new Thread(new PeriodicCatchupRunnable(blockchain, remoteServerStatus, localPort)).start();
        
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(localPort);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new BlockchainServerRunnable(clientSocket, blockchain, remoteServerStatus, localPort)).start();
                //new Thread(new HeartBeatReceiverRunnable(clientSocket, remoteServerStatus, localPort)).start();
                
            }
        } catch (IllegalArgumentException e) {
        } catch (IOException e) {
        } finally {
            try {
                pcr.setRunning(false);
                pct.join();
                if (serverSocket != null)
                    serverSocket.close();
            } catch (IOException e) {
            } catch (InterruptedException e) {
            }
        }
    }
}
