package ptolemy.domains.wireless.demo.SmartParking;

import java.util.*;

import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;

public class ParkingManager {

    public static HashSet AvailableLots = new HashSet();
    public static HashSet ParkedLots= new HashSet();
      
    public ParkingManager() {
    }
    
    public ParkingManager(HashSet availableLots, HashSet parkedLots) {
        AvailableLots = new HashSet(availableLots);
        ParkedLots = new HashSet(parkedLots);
    }
    
    public synchronized void update(RecordToken updateMsg) {
        String lot = ((StringToken) updateMsg.get("lot")).stringValue();
        int state = ((IntToken) updateMsg.get("state")).intValue();
        if(state == 0) { //use 0 to represent the lot is free.
            AvailableLots.add(lot);
            ParkedLots.remove(lot);
        } else if (state == 1) {
            AvailableLots.remove(lot);
            ParkedLots.add(lot);
        }
    }  
    
    public HashSet getAvailable() {
         return AvailableLots;
    }
}