import java.io.*;
import java.net.Socket;
import java.util.EmptyStackException;

public class BlockchainServerRunnable implements Runnable{

    private Socket clientSocket;
    private Blockchain blockchain;

    public BlockchainServerRunnable(Socket clientSocket, Blockchain blockchain) {
        this.clientSocket = clientSocket;
        this.blockchain = blockchain;
    }

    public void run() {
        InputStream clientInputStream = null;
        OutputStream clientOutputStream = null;
        try {
            clientInputStream = clientSocket.getInputStream();
            clientOutputStream = clientSocket.getOutputStream();

        } catch (IOException e) {
            e.printStackTrace();
        }

        while(true){
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(clientInputStream));
            PrintWriter writer = new PrintWriter(clientOutputStream, true);
            String text = "";

            try{
                text = reader.readLine();
                System.out.println("Received message: " + text);

            }catch(IOException err){
                //err.printStackTrace();
            }




            if (stringHandler(text) == 0) {
                writer.print("\nError\n");
                writer.flush();
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    Thread.sleep(400);
                    //return;
                } catch (InterruptedException e) {
                    System.err.println("PeriodicCommitterInterrupted.");
                }
            } else if (stringHandler(text) == 1) {
                writer.print("\nAccepted\n");
                writer.flush();
                blockchain.addTransaction(text);
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(400);
                    //return;
                } catch (InterruptedException e)
                {
                    System.err.println("PeriodicCommitterInterrupted.");
                }
            } else if (stringHandler(text) == 2) {
                writer.print("\n"+blockchain.toString());
                writer.flush();
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(400);
                    //return;
                } catch (InterruptedException e) {
                    System.err.println("PeriodicCommitterInterrupted.");
                }

            }else if(stringHandler(text) == 3){
                writer.print("\nRejected\n");
                writer.flush();
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    Thread.sleep(400);
                    //return;
                } catch (InterruptedException e) {
                    System.err.println("PeriodicCommitterInterrupted.");
                }
            } else if (stringHandler(text) == 4) {
                try {
                    //writer.print("Closed socket\n\n");
                    clientSocket.close();
                    try {
                        Thread.sleep(400);
                    } catch (InterruptedException e) {
                        System.err.println("PeriodicCommitterInterrupted.");
                    }
                    //return;

                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }


    }
    public int stringHandler(String text){


        if(text == null) {
            return 4;
        }

        if (text.length() == 2) {
            if (text.equals("pb")) {
                return 2;
            } else if (text.equals("cc")) {
                return 4;
            } else if (text.equals("tx")) {
                return 3;
            } else {
                return 0;
            }
        }
        if (text.length() <= 1) {
            return 0;
        }
    /*
    Past this point we assume we are checking a transaction
    so we treat the input sensitisation as such
     */
        if (text.length() < 13) {
            if (text.contains("tx")) {
                return 3;
            }else{
                return 0;
            }
        }

        char[] charArray = text.toCharArray();
        String[] stringArray = text.split("\\|");

        if (!stringArray[0].equals("tx")) {

            return 0;
        }

        if (charArray[2] != '|') {
            return 3;
        }
        if (charArray[11] != '|') {
            return 3;
        }


        if (stringArray[1].length() == 8) {
            for (int i = 0; i < 4; i++) {
                if (Character.isAlphabetic(stringArray[1].toCharArray()[i]) == false) {
                    return 3;
                }
            }

            for (int i = 4; i < 8; i++) {
                if (Character.isDigit(stringArray[1].toCharArray()[i]) == false) {
                    return 3;
                }
            }
        } else {
            return 3;
        }

        for (int i = 3; i < 11; i++) {
            if (charArray[i] == '|') {
                return 3;
            }
        }
        for (int i = 12; i < charArray.length; i++) {
            if (charArray[i] == '|') {
                return 3;
            }
        }

        if (stringArray[2].length() > 70) {
            return 3;
        }

        return 1;

    }
    // implement any helper method here if you need any
}
