package ptolemy.domains.metroII.kernel;

import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event.Builder;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event.Status;

public class TimeScheduler implements ConstraintSolver {

    public TimeScheduler() {
        initialize(); 
    }
    
    public void initialize() {
        current_time = 0; 
    }

    public void turnOnDebugging() {
        _debugger.turnOnDebugging();
    }

    public void turnOffDebugging() {
        _debugger.turnOffDebugging();
    }
    
    @Override
    public void resolve(Iterable<Builder> metroIIEventList) {
        _debugger.printTitle("TimeScheduler Begins at Time " + current_time); 
        _debugger.printMetroEvents(metroIIEventList);

        long time = Long.MAX_VALUE;
        boolean hasEventWithoutTime = false;
        for (Builder event : metroIIEventList) {
            if (event.getStatus() == Status.PROPOSED) {
                if (event.hasTime()) {
                    if (event.getTime().getValue() < time) {
                        time = event.getTime().getValue();
                    }
                } else {
                    hasEventWithoutTime = true;
                }
            }
        }
        // System.out.println("Time Scheduler: " + (double) current_time
        //         / Double.valueOf("10000000000"));
        if (hasEventWithoutTime) {
            for (Builder event : metroIIEventList) {
                if (event.getStatus() == Status.PROPOSED) {
                    if (event.hasTime()) {
                        event.setStatus(Status.WAITING);
                    }
                }
            }
        } else {
            for (Builder event : metroIIEventList) {
                if (event.getStatus() == Status.PROPOSED) {
                    if (event.hasTime()) {
                        if (event.getTime().getValue() > time) {
                            event.setStatus(Status.WAITING);
                        }
                    }
                }
            }
        }

        // System.out.println("Time Scheduler: "+time); 
        for (Builder event : metroIIEventList) {
            if (event.getStatus() == Status.PROPOSED) {
                if (event.hasTime()) {
                    if (current_time < event.getTime().getValue()) {
                        current_time = event.getTime().getValue();
                    }
                }
            }
        }
        for (Builder event : metroIIEventList) {
            if (event.getStatus() == Status.PROPOSED) {
                if (!event.hasTime()) {
                    Event.Time.Builder builder = Event.Time.newBuilder();
                    builder.setValue(current_time); 
                    event.setTime(builder);
                }
            }
        }

        for (Builder event : metroIIEventList) {
            if (event.getStatus() == Status.PROPOSED) {
                event.setStatus(Status.NOTIFIED);
            }
        }
        
        _debugger.printMetroEvents(metroIIEventList);
        _debugger.printTitle("TimeScheduler Ends at Time " + current_time); 
    }

    private MetroDebugger _debugger = new MetroDebugger();

    private long current_time;
    
}
