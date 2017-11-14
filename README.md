# p2p-blockchain
A peer-to-peer blockchain application in Java I did for a university course on distributed systems.
* Sends messages to multiple peers, and accepts connections from multiple clients simultaneously
* Tolerates the dynamism of the system and unreliability of the network

Given some skeleton files provided by the course coordinators, I implemented:
1. Heartbeat-based dynamic neighbor communication
   * Heartbeat sending
   * Heratbeat receving and ServerInfo sending
   * ServerInfo receiving and ServerInfo relaying
2. Catchup protocol and Blockchain Consensus
   * Latest block message sending
   * Catchup message sending
   * Server catchup algorithm
