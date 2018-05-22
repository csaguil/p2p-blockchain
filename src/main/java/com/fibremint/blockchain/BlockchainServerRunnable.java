package com.fibremint.blockchain;

import java.io.*;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;

public class BlockchainServerRunnable implements Runnable{

    private Socket clientSocket;
    private Blockchain blockchain;
    private HashMap<ServerInfo, Date> serverStatus;
    String remoteIP;
    private int localPort;

    public BlockchainServerRunnable(Socket clientSocket, Blockchain blockchain, HashMap<ServerInfo, Date> serverStatus, int localPort) {
        this.clientSocket = clientSocket;
        this.blockchain = blockchain;
        this.serverStatus = serverStatus;
        this.localPort = localPort;
    }

    public void run() {
        try {
            BufferedReader inputReader = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
        	PrintWriter outWriter = new PrintWriter(clientSocket.getOutputStream(), true);

        	while (true) {
        		String inputLine = inputReader.readLine();
        		if (inputLine == null) {
        			break;
        				
        		}
        		String[] tokens = inputLine.split("\\|");
        		switch (tokens[0]) {
        			case "tx":
        				this.txHandler(inputLine, outWriter);
        			case "pb":
        				this.pbHandler(outWriter);
        			case "cc":
        				//this.serverHandler(inputLine, outWriter, tokens);
        				
        			case "hb":
        			case "si":
        				this.heartBeatHandler(inputReader, inputLine, tokens);
        			
        			case "lb":
        				if (this.lbHandler(inputLine, tokens)) {
        					break;
        				}
        				
        			case "cu":
        				if (tokens[0].equals("cu")) {
        					this.cuHandler(tokens);
        					break;
        				}
        				
        			default:
                       	outWriter.print("Error\n\n");
                       	outWriter.flush();
        		}
        	}
            clientSocket.close();
        } catch (IOException e) {
        }
    }

//Request handlers//-----------------------   

	public void cuHandler(String[] tokens) {
		try (ObjectOutputStream outStream = new ObjectOutputStream(clientSocket.getOutputStream())){
			if (tokens.length == 1) {
			//cu-only case
				outStream.writeObject(blockchain.getHead());
				outStream.flush();
				return;
			
			} else {
			//cu|<block's hash> case
				Block cur = blockchain.getHead();
				while (true) {
					if (Base64.getEncoder().encodeToString(cur.calculateHash()).equals(tokens[1])) {
						outStream.writeObject(cur);
						outStream.flush();
						return;
						
					}
					if (cur == null) {
						break;
					}
					cur = cur.getPreviousBlock();
					
				}
				outStream.writeObject(cur);
				outStream.flush();
			
			}
		} catch (Exception e) {
		}
	}
	
    public boolean lbHandler(String inputLine, String[] tokens) {
    	try {
    		String encodedHash;
    		if (blockchain.getHead() != null) {
    			byte[] latestHash = blockchain.getHead().calculateHash();
    			encodedHash = Base64.getEncoder().encodeToString(latestHash);
    			
    		} else {
    			encodedHash = "null";
    		}
    		
    		if (encodedHash.equals(tokens[3]) && this.blockchain.getLength() > Integer.valueOf(tokens[2]) || this.blockchain.getLength() == Integer.valueOf(tokens[2]) && tokens[3].length() < encodedHash.length()) {
    		//no catchup necessary
    			return true;
    			
    		} else {
    		//catchup case
    			//set up new connection
    			String remoteIP = (((InetSocketAddress) clientSocket.getRemoteSocketAddress()).getAddress()).toString().replace("/", "");
    			Socket s;
    			s = new Socket(remoteIP, Integer.valueOf(tokens[1]));
    			PrintWriter outWriter;
    			outWriter = new PrintWriter(s.getOutputStream(), true);
    			
    			//naive catchup
    			ArrayList<Block> blocks = new ArrayList<Block>();
    			outWriter.println("cu"); //getting head
    			outWriter.flush();
				ObjectInputStream inputStream;
				inputStream = new ObjectInputStream(s.getInputStream());
    			Block b = (Block) inputStream.readObject();
    			
    			inputStream.close();
    			s.close();
    			blocks.add(b);
    			String prevHash = Base64.getEncoder().encodeToString(b.getPreviousHash());

    			while (!prevHash.startsWith("A")) {
    				s = new Socket(remoteIP, Integer.valueOf(tokens[1]));
    				outWriter = new PrintWriter(s.getOutputStream(), true);

    				outWriter.println("cu|" + prevHash);
    				outWriter.flush();
    				
    				inputStream = new ObjectInputStream(s.getInputStream());

    				b = (Block) inputStream.readObject();
    				inputStream.close();
    				s.close();
    				blocks.add(b);
    				prevHash = Base64.getEncoder().encodeToString(b.getPreviousHash());
    			}
    			this.blockchain.setHead(blocks.get(0));
    			this.blockchain.setLength(blocks.size());
    			
    			Block cur = this.blockchain.getHead();
    			
    			for (int i = 0; i < blocks.size(); i++) {
    				if (i <= blocks.size() - 1) {
    					cur.setPreviousBlock(blocks.get(i + 1));
    				} else {
    					cur.setPreviousBlock(null);
    				}
    				cur = cur.getPreviousBlock();
    			}			
    			
    			return false;
    		}
    	
    	} catch (Exception e) {
    	}
    	return false;
    }

    /*
    public void serverHandler(String inputLine, PrintWriter outWriter, String[] tokens) {
        try {
            switch (tokens[0]) {
                case "tx":
                    if (this.blockchain.addTransaction(inputLine))
                        outWriter.print("Accepted\n\n");
                    else
                        outWriter.print("Rejected\n\n");
                        outWriter.flush();
                        break;
                case "pb":
                    outWriter.print(blockchain.toString() + "\n");
                    System.out.println(blockchain.toString() + "\n");
                    outWriter.flush();
                    break;
                case "cc":
                    return;
                    
                default:
            }
        } catch (Exception e) {
        	e.printStackTrace();
        }
    }
    */

    public void txHandler(String inputLine, PrintWriter outWriter) {
    	try {
    		if (this.blockchain.addTransaction(inputLine))
    			outWriter.print("Accepted\n\n");
    		else {
    			outWriter.print("Rejected\n\n");
    			outWriter.flush();
			}
		} catch (Exception e) {
    		e.printStackTrace();
		}
	}

	public void pbHandler(PrintWriter outWriter) {
    	try {
    		outWriter.print(blockchain.toString() + "\n");
    		System.out.println(blockchain.toString() + "\n");
    		outWriter.flush();
		} catch (Exception e) {
    		e.printStackTrace();
		}
	}

    public void heartBeatHandler(BufferedReader bufferedReader, String line, String[] tokens) {
        try {	
            String remoteIP = (((InetSocketAddress) clientSocket.getRemoteSocketAddress()).getAddress()).toString().replace("/", "");
            	
            ServerInfo serverInQuestion;
            switch (tokens[0]) {
                case "hb":
                	serverInQuestion = new ServerInfo(remoteIP, Integer.valueOf(tokens[1]));
                		
                	if (!serverStatus.containsKey(serverInQuestion)) {
                		String forwardMessage = "si|" + String.valueOf(localPort) + "|" + remoteIP + "|" + tokens[1];
                    	this.broadcastHB(forwardMessage, new ArrayList<ServerInfo>());
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
                    	this.broadcastHB(relayMessage, exempt);
                    		
                	}
                		
                	serverStatus.put(serverInQuestion, new Date());
                	serverStatus.put(originator, new Date());
                	this.removeUnresponsive();
                    	
                default:     
            }
        } catch (Exception e) {
    	}
    }

//Helper Functions//--------------------------
    
    public void removeUnresponsive() {
    	//check for servers that havent responded in 4 secs
        for (ServerInfo server: serverStatus.keySet()) {
            if (new Date().getTime() - serverStatus.get(server).getTime() > 4000) {
            	serverStatus.remove(server);
            	System.out.println("removed " + server.getHost());
            }
        }
    }
    
    public void broadcastHB(String message, ArrayList<ServerInfo> exempt) {
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

