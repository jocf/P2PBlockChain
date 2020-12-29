import java.util.ArrayList;
import java.util.Scanner;

public class BlockchainClient {

    public static void main(String[] args) {

        if (args.length != 1) {
            return;
        }
        String configFileName = args[0];

        ServerInfoList pl = new ServerInfoList();
        pl.initialiseFromFile(configFileName);

        Scanner sc = new Scanner(System.in);

        BlockchainClient bcc = new BlockchainClient();

        while (true) {
            String message = sc.nextLine();
            // implement your code here
            if(message.equals("sd")){
                return;
            }
            else if(message.equals("cl")){
                if(pl.clearServerInfo()){
                    System.out.print("Succeeded\n\n");
                }else{
                    System.out.print("Failed\n\n");
                }

            }
            else if(message.equals("ls")){
                System.out.print(pl.toString() + "\n");
            }
            else if(message.equals("pb")){
                bcc.broadcast(pl,"pb");
            }

            // Now we will deal with "|" commands
            else if(message.contains("|")){
                String[] messageSplit = message.split("\\|");
                // Two-piece commands
                if(messageSplit.length == 2){
                    // RM command
                    if(messageSplit[0].equals("rm")){
                        // Check index of command
                        if(pl.isInt(messageSplit[1])){
                            if((Integer.parseInt(messageSplit[1]) < pl.getServerInfos().size())
                                    && (Integer.parseInt(messageSplit[1]) >= 0)){
                                pl.removeServerInfo(Integer.parseInt(messageSplit[1]));
                                System.out.print("Succeeded\n\n");
                            }else{
                                System.out.print("Failed\n\n");
                            }
                        }else{
                            System.out.print("Failed\n\n");
                        }
                    }
                    // PB With only 1 server
                    if(messageSplit[0].equals("pb")){
                        // Check index of command
                        if(pl.isInt(messageSplit[1])){
                            if(Integer.parseInt(messageSplit[1]) < pl.getServerInfos().size()){
                                // If the server isn't null then unicast a PB command to the server.
                                if(pl.getServerInfos().get(Integer.parseInt(messageSplit[1])) != null){
                                    bcc.unicast(Integer.parseInt(messageSplit[1]),
                                            pl.getServerInfos().get(Integer.parseInt(messageSplit[1])),"pb");
                                }
                            }
                        }
                    }
                }
                if((messageSplit.length == 3)&&(!messageSplit[0].equals("pb"))){
                    // Process AD command
                    if(messageSplit[0].equals("ad")){
                        if(pl.isInt(messageSplit[2])){
                            ServerInfo tempServer = new ServerInfo(messageSplit[1],Integer.parseInt(messageSplit[2]));
                            if(pl.addServerInfo(tempServer)){
                                System.out.print("Succeeded\n\n");
                            }else{
                                System.out.print("Failed\n\n");
                            }
                        }else{
                            System.out.print("Failed\n\n");
                        }
                    }

                    if(messageSplit[0].equals("tx")){
                        bcc.broadcast(pl,message);
                    }
                }
                if((messageSplit.length == 4) && (messageSplit[0].equals("up"))){
                    if((pl.isInt(messageSplit[1]))&&(pl.isInt(messageSplit[3]))){
                        ServerInfo tempServer = new ServerInfo(messageSplit[2],Integer.parseInt(messageSplit[3]));
                        pl.updateServerInfo(Integer.parseInt(messageSplit[1]),tempServer);
                        System.out.print("Succeeded\n\n");
                    }else{
                        System.out.print("Failed\n\n");
                    }
                }
                if((messageSplit.length >=3) && (messageSplit[0].equals("pb"))){
                    boolean failflag = false;
                    System.out.println(messageSplit.length);
                    ArrayList<Integer> serverIndices = null;
                    for(int i = 1; i < messageSplit.length; i++){
                        // Check that all the indexes are ints
                        if((!(pl.isInt(messageSplit[i])))||(Integer.parseInt(messageSplit[i]) >= pl.getServerInfos().size())){
                            failflag = true;
                        }
                        if(failflag == false){
                            serverIndices = new ArrayList<Integer>();
                            for(int j = 1; j < messageSplit.length; j++){
                                serverIndices.add(Integer.parseInt(messageSplit[j]));
                            }
                        }
                    }
                    bcc.multicast(pl,serverIndices,"pb");
                }



            }else{
                System.out.print("Unknown Command\n\n");
            }

        }
    }

    public void unicast (int serverNumber, ServerInfo p, String message) {
        BlockchainClientRunnable bccr = new BlockchainClientRunnable(serverNumber,p.getHost(),p.getPort(),message);
        Thread bcc = new Thread(bccr);
        bcc.start();
        // Fetch reply
        String reply = "";
        try {
                bcc.join();
                reply = bccr.getReply();
        } catch (InterruptedException e) {
                e.printStackTrace();
        }

        // Print reply
        System.out.println(bccr.getReply());
        
    }

    public void broadcast (ServerInfoList pl, String message) {



        Thread[] threads = new Thread[pl.getServerInfos().size()];
        BlockchainClientRunnable[] runnables = new BlockchainClientRunnable[pl.getServerInfos().size()];
        ServerInfoList infos = new ServerInfoList();
        for(ServerInfo server : pl.getServerInfos()){
            if(server != null){
                infos.addServerInfo(server);
            }
        }

        // Account for null cases
        for(int j = 0; j < infos.getServerInfos().size(); j++){
            runnables[j] = new BlockchainClientRunnable(j,infos.getServerInfos().get(j).getHost(),
                    infos.getServerInfos().get(j).getPort(),message);
        }

        for(int i = 0; i < infos.getServerInfos().size(); i++){
            threads[i] = new Thread(runnables[i]);
            threads[i].start();
        }

        String reply = "";
        // Join the threads here
        /*for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }*/

        for(int k = 0; k < runnables.length; k++){

            System.out.print(runnables[k].getReply());
        }



    }

    public void multicast (ServerInfoList serverInfoList, ArrayList<Integer> serverIndices, String message) {
        Thread[] threads = new Thread[serverIndices.size()];
        BlockchainClientRunnable[] runnables = new BlockchainClientRunnable[serverIndices.size()];

        for(int j = 0; j < runnables.length; j++){
            runnables[j] = new BlockchainClientRunnable(serverIndices.get(j),
                    serverInfoList.getServerInfos().get(serverIndices.get(j)).getHost(),
                    serverInfoList.getServerInfos().get(serverIndices.get(j)).getPort(),message);
        }

        for(int i = 0; i < threads.length; i++){
            threads[i] = new Thread(runnables[i]);
            threads[i].start();
        }

        String reply = "";
        // Join the threads here
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        for(int k = 0; k < runnables.length; k++) {
            System.out.print(runnables[k].getReply());
        }

    }
    // implement any helper method here if you need any
}