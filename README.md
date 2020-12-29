This is a decentralized Peer-to-Peer Blockchain that I created while at the University of Sydney for an Assignment.

The blockchain is capable of catching up and meeting consensus on conflicting "latest blocks," and is also P2P meaning that multiple servers can be run simulatenously. 

If you have any other questions about the project, please feel free to contact me.

IMPORTANT NOTE:
The up to date code that you should be using is in the "server" folder. The client code provided is for ARCHIVAL PURPOSES ONLY.
I have also provided the OLD blockchain (non P2P) code in that folder, but this should not be used. The following documentation is ONLY for the update to date code.

+++++++++++++ How to use the provided code +++++++++++++

To compile the code, please use the provided makefile in the server folder.
 - use make or make build to compile the code
 - use make clean to remove the binaries compiled
 
The compiled binaries can be found in ./server/bin
 
To run the code, you will need to execute the BlockchainServer.class file after compilation. This program requires 3 runtime arguments to be provided:
 - the local running port of the server
 - the remote host of the sibling server
 - the remote port of the sibling server

The way in which this functions is as follows:
 - To begin, start at least two servers.
 - The first argument should be the port of the server itself, so, lets say we ran two servers, one on localhost:8333 and one on localhost:8334
 For the first server:
 - Argument 1 will be 8333
 - Argument 2 will be localhost
 - Argument 3 will be 8334
 For the second server:
 - Argument 1 will be 8334
 - Argument 2 will be localhost
 - Argument 3 will be 8333
 
An example execution could be as follows:
 - java -cp bin BlockchainServer 8333 127.0.0.1 8334
 - java -cp bin BlockchainServer 8334 127.0.0.1 8333
 
Once this is completed, you can connect to the server with telnet

To connect with telnet use "telnet HOSTNAME PORT"
i.e. telnet localhost 8333

From here, the following commands are available to you:
pb - print the blockchain as it stands currently
cc - close the connection to the host
tx|<sender>|<content> - sends a transaction to the server. This will then propogate into the blockchain. E.g. tx|test0001|This is my message) 
Note: The sender should be of the format ([4 alphabetic][4 numeric]). The message can be any format.

+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

The updates will only propogate through the chain when a new block is created. Local transactions before a new block has been formed will not propogate.

A client was also created for this project that was capabale of connecting to multiple servers, but, for the simplicity of use and testing, I have not provided it in the main folder.
Instead, using the application protocol 'telnet' is sufficient for this project and for demonstrative purposes.

