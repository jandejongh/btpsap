# btpsap
ETSI BTP Service Access Point with support for remote clients through UDP.

A Java implementation of the ETSI BTP (Basic Transport Protocol) SAP according to ETSI EN 302663-5 (and referenced documents). Clients can connect through a TCP-based access protocol with a simple Command-Line Interface, and can instruct the BtpSap to forward indications to a specific (usually, remote) UDP port, and to route requests among available GN (GeoNetworking) units based upon the Traffic Class (ID). Multi-channel operation is supported.

The current implementation relies on Alex Voronov's pioneering GeoNetworking implementation in Java, and features an open-source access protocol (TCP-based for the client connection and management plane; UDP for the data plane, i.e., Requests and Indications). However, the software framework allows for inclusion of alternative GN implementations, and of alternative access protocols from clients.

The software is still in an immature state, lacking for instance javadoc and a good functional description and deployment instructions and hints. Stay tuned for updates.

Released under Apache License V2.

BR,
Jan
