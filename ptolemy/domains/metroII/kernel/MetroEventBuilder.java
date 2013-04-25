package ptolemy.domains.metroII.kernel;

import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event.Builder;

public class MetroEventBuilder {

    public MetroEventBuilder() {
        // TODO Auto-generated constructor stub
    }

    static public Builder newProposedEvent(String eventName, String Owner, long timeValue,
            double resolution) {
        Event.Builder builder = Event.newBuilder();
        builder.setName(eventName);
        builder.setOwner(Owner);
        builder.setStatus(Event.Status.PROPOSED);
        builder.setType(Event.Type.GENERIC);
        Event.Time.Builder timeBuilder = Event.Time.newBuilder();
        double scaler = resolution / timeBuilder.getResolution();

        assert scaler > 0;
        assert Math.abs(scaler - (int) scaler) < 0.00001;

        timeValue = timeValue * ((int) scaler);
        timeBuilder.setValue(timeValue);
        builder.setTime(timeBuilder);
        
        return builder; 
    }
    
    static public Builder newProposedEvent(String eventName, String Owner) {
        Event.Builder builder = Event.newBuilder();
        builder.setName(eventName);
        builder.setOwner(Owner);
        builder.setStatus(Event.Status.PROPOSED);
        builder.setType(Event.Type.GENERIC);
        
        return builder; 
    }
    
    
}
