package blockchain;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.io.*;

public class HeartBeatReceiverRunnable implements Runnable{

    private Socket toClient;
    private HashMap<ServerInfo, Date> serverStatus;
    private int localPort;

    public HeartBeatReceiverRunnable (Socket toClient, HashMap<ServerInfo, Date> serverStatus, int localPort) {
        this.toClient = toClient;
        this.serverStatus = serverStatus;
        this.localPort = localPort;
    }

    @Override
    public void run() {
        try {
			heartBeatServerHandler(toClient.getInputStream());
			toClient.close();
        } catch (IOException e) {
    	}
    }
    
    public void heartBeatServerHandler(InputStream clientInputStream) {
    	BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientInputStream));
        
        try {
        	while (true) {
            	String line = bufferedReader.readLine();
            	if (line == null) {
            		break;
            	}
            	System.out.println("receiving " + line);
            	
            	String[] tokens = line.split("\\|");
            	String remoteIP = (((InetSocketAddress) toClient.getRemoteSocketAddress()).getAddress()).toString().replace("/", "");
            	
            	ServerInfo serverInQuestion;
            	switch (tokens[0]) {
                	case "hb":
                		serverInQuestion = new ServerInfo(remoteIP, Integer.valueOf(tokens[1]));
                		
                		if (!serverStatus.containsKey(serverInQuestion)) {
                			String forwardMessage = "si|" + String.valueOf(localPort) + "|" + remoteIP + "|" + tokens[1];
                    		this.broadcast(forwardMessage, new ArrayList<ServerInfo>());
                		}
                		
                		serverStatus.put(serverInQuestion, new Date());
                		this.removeUnresponsive();
            			
                	case "si":
                		serverInQuestion = new ServerInfo(tokens[2], Integer.valueOf(tokens[3]));
                		ServerInfo originator = new ServerInfo(remoteIP, Integer.valueOf(tokens[1]));
                		
                		if (!serverStatus.containsKey(serverInQuestion)) {
                    		ArrayList<ServerInfo> exempt = new ArrayList<ServerInfo>();
                    		exempt.add(originator);
                    		exempt.add(serverInQuestion);
                    		String relayMessage = "si|" + String.valueOf(localPort) + "|" + tokens[2] + "|" + tokens[3];
                    		this.broadcast(relayMessage, exempt);
                    		
                		}
                		
                		serverStatus.put(serverInQuestion, new Date());
                		serverStatus.put(originator, new Date());
                		this.removeUnresponsive();
                    	
                	default:     
            	}
			}
        } catch (Exception e) {
    	}
    }
    
    public void removeUnresponsive() {
    	//check for servers that havent responded in 4 secs
        for (ServerInfo server: serverStatus.keySet()) {
            if (new Date().getTime() - serverStatus.get(server).getTime() > 4000) {
            	serverStatus.remove(server);
            	System.out.println("removed " + server.getHost());
            }
        }
    }
    
    public void broadcast(String message, ArrayList<ServerInfo> exempt) {
    	ArrayList<Thread> threadArrayList = new ArrayList<Thread>();
    	for (ServerInfo info: this.serverStatus.keySet()) {
            if (!exempt.contains(info)) {
                Thread thread = new Thread(new MessageSenderRunnable(info, message));
                thread.start();
                threadArrayList.add(thread);
            }
        }
        
        for (int i = 0; i < threadArrayList.size(); i++) {
            try {
            	threadArrayList.get(i).join();
            } catch (InterruptedException e) {
            }
        }
    }
}
