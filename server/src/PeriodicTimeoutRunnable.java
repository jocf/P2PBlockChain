import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

public class PeriodicTimeoutRunnable implements Runnable{


    private HashMap<ServerInfo, Date> serverStatus;

    public PeriodicTimeoutRunnable(HashMap<ServerInfo, Date> serverStatus) {
        this.serverStatus = serverStatus;
    }

    @Override
    public void run() {
        while(true) {
            for (Entry<ServerInfo, Date> entry : serverStatus.entrySet()) {
                // if greater than 2T, remove
                if (new Date().getTime() - entry.getValue().getTime() > 4000) {
                    serverStatus.remove(entry);
                }
            }
            System.out.println();

            // sleep for two seconds
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }
        }
    }
}
