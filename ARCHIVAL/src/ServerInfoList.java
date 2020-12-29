import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class ServerInfoList {

    ArrayList<ServerInfo> serverInfos;

    public ServerInfoList() {
        serverInfos = new ArrayList<>();
    }

    public void initialiseFromFile(String filename) {


        File configFile = new File(filename);
        Scanner fileScanner = null;
        try {
            fileScanner = new Scanner(configFile);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        int numServer = 0;
        if(fileScanner != null) {
            while (fileScanner.hasNextLine()) {
                String data = fileScanner.nextLine();
                if (data.contains("=")) {
                    String[] dataSplit = data.split("=");
                    if (dataSplit[0].equals("servers.num")) {
                        if (dataSplit.length == 2) {
                            if (isInt(dataSplit[1])) {
                                if (Integer.parseInt(dataSplit[1]) > numServer) {
                                    numServer = Integer.parseInt(dataSplit[1]);
                                }
                            }
                        }
                    }
                }
            }
        }

        if(numServer == 0){
            serverInfos = new ArrayList<>();
            return;
        }



        for(int i = 0; i < numServer; i++) {
            String tempHost = "";
            int tempPort = 0;
            try {
                fileScanner = new Scanner(configFile);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            while (fileScanner.hasNextLine()) {
                String data = fileScanner.nextLine();
                if (data.contains("=")) {
                    String[] dataSplit = data.split("=");
                    if(dataSplit.length == 2){
                        if(dataSplit[0].length() == 12){
                            if(dataSplit[0].contains("server")){
                                if(Character.getNumericValue(dataSplit[0].charAt(6)) == i){
                                    //Host
                                    if(dataSplit[0].contains("host")){
                                        tempHost = dataSplit[1];
                                    }
                                    //Port
                                    if(dataSplit[0].contains("port")){
                                        if(isInt(dataSplit[1])){
                                            if(Integer.parseInt(dataSplit[1]) < 65536){
                                                if(Integer.parseInt(dataSplit[1]) > 1023){
                                                    tempPort = Integer.parseInt(dataSplit[1]);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if(tempHost.equals("") || (tempPort == 0)){
                // init as null
                serverInfos.add(null);
            }else{
                ServerInfo tempInfo = new ServerInfo(tempHost,tempPort);
                serverInfos.add(tempInfo);
            }
        }
        // implement your code here
    }

    public ArrayList<ServerInfo> getServerInfos() {
        return serverInfos;
    }

    public void setServerInfos(ArrayList<ServerInfo> serverInfos) {
        this.serverInfos = serverInfos;
    }

    public boolean addServerInfo(ServerInfo newServerInfo) {
        if((newServerInfo.getPort() < 65536) && (newServerInfo.getPort() > 1023)){
            if(newServerInfo.getHost().length() > 0){
                serverInfos.add(newServerInfo);
                return true;
            }
        }
        return false;
    }

    public boolean updateServerInfo(int index, ServerInfo newServerInfo) {
        if((index >=0) && (index < serverInfos.size())){
            if((newServerInfo.getPort() < 65536) && (newServerInfo.getPort() > 1023)){
                if(newServerInfo.getHost().length() > 0){
                    serverInfos.set(index,newServerInfo);
                    return true;
                }
            }
        }
        return false;
    }
    
    public boolean removeServerInfo(int index) { 
        if((index >=0) && (index < serverInfos.size())){
            serverInfos.set(index,null);
            return true;
        }else{
            return false;
        }
    }

    public boolean clearServerInfo() {

        serverInfos.removeAll(Collections.singletonList(null));
        return true;
    }

    public String toString() {
        String s = "";
        for (int i = 0; i < serverInfos.size(); i++) {
            if (serverInfos.get(i) != null) {
                s += "Server" + i + ": " + serverInfos.get(i).getHost() + " " + serverInfos.get(i).getPort() + "\n";
            }else{
            }
        }
        return s;
    }

    public boolean isInt(String toCheck){
        try{
            Integer.parseInt(toCheck);
            return true;
        }catch(NumberFormatException ex){
            return false;
        }
    }
    // implement any helper method here if you need any
}