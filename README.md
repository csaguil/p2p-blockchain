# p2p-blockchain
A peer-to-peer blockchain application in Java I did for a university course on distributed systems.
The application:
* Uses multithreading and Java Sockets to send messages to multiple peers, and accept connections from multiple clients simultaneously
* Allows peers to act as both a client and a server
* Tolerates the dynamism of the system and unreliability of the network using a catchup protocol

Given some skeleton files provided by the course coordinators, I implemented:
1. Heartbeat-based dynamic neighbor communication
   * Heartbeat sending
   * Heratbeat receving and server info sending
   * Server info receiving and server info relaying
2. Catchup protocol and Blockchain Consensus
   * Latest block message sending
   * Catchup message sending
   * Server catchup algorithm
