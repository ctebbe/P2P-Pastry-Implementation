# P2P Pastry implementation

The contents of this repo is the deliverable for CS555 (Distributed Systems) assignment at Colorado State University.

The project is a homebrewed implementation of the Pastry peer to peer (P2P) distributed hash table (DHT) protocol. The code supports a scalable implementation to discover and incorporate new nodes as well as distribute, store, and retrieve files in O(lg N) hops where N is the number of nodes in the network.

All networking and routing logistics are original work and do not depend on any 3rd party libraries.
