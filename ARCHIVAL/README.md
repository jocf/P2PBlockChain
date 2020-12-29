NOTE: THIS IS FOR ARCHIVAL PURPOSES ONLY. THIS CODE IS NOT UP TO DATE.
This is the outdated version of the code with the client functionality.

This client is capable of reading input from a configuration file to load in the following:
 - The number of servers to load in
 - The servers hostnames
 - The servers port names

The client functions properly with this version of the BlockchainServer but MAY NOT WORK with the updated P2P version provided in the "server" folder.
This was NOT INTENDED to be used with the P2P model.

The client accepts the following arguments:
 - ls "list servers" : "ls"
 - ad "add a server" : "ad|<host name>|<port number>"
 - rm "remove a server" : "rm|<server index>"
 - up "update a server" : "up|<server index>|<host name>|<port number>"
 - tx "send a transaction" : "tx|<sender>|<content>"
 - pb "print all blockchains" : "pb"
 - pb "print a blockchain" : "pb|<server index>"
 - pb "print some blockchains" : "pb|<server index>|<server index>|<server index>..."
 - sd "shut down" : "sd"

If you are unsure, please use the updated version as outlined in the main README file in the parent directory.

Thanks!
