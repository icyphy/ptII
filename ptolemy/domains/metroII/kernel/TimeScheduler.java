package ptolemy.domains.metroII.kernel;

import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event.Builder;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event.Status;

public class TimeScheduler implements ConstraintSolver {

    public TimeScheduler() {
        current_time = 0; 
    }
    
    @Override
    public void resolve(Iterable<Builder> metroIIEventList) {
        for (Builder event : metroIIEventList) {
            if (event.getStatus() == Status.NOTIFIED) {
                if (event.hasTime()) {
                    System.out.println(event.getTime()+": "+event.getName()); 
                }
                else {
                    System.out.println(current_time+"-: "+event.getName()); 
                } 
            }
        }

        long time = Long.MAX_VALUE;
        boolean hasEventWithoutTime = false;
        for (Builder event : metroIIEventList) {
            if (event.getStatus() == Status.NOTIFIED) {
                if (event.hasTime()) {
                    if (event.getTime() < time) {
                        time = event.getTime();
                    }
                    System.out.println(event.getName()+" time "+event.getTime()); 
                } else {
                    hasEventWithoutTime = true;
                }
            }
        }
        System.out.println("Time Scheduler: "+(double)current_time/Double.valueOf("10000000000")); 
        if (hasEventWithoutTime) {
            for (Builder event : metroIIEventList) {
                if (event.getStatus() == Status.NOTIFIED) {
                    if (event.hasTime()) {
                        event.setStatus(Status.WAITING);
                    }
                }
            }
        } else {
            for (Builder event : metroIIEventList) {
                if (event.getStatus() == Status.NOTIFIED) {
                    if (event.hasTime()) {
                        if (event.getTime() > time) {
                            event.setStatus(Status.WAITING);
                        }
                    }
                }
            }
        }

        // System.out.println("Time Scheduler: "+time); 
        for (Builder event : metroIIEventList) {
            if (event.getStatus() == Status.NOTIFIED) {
                if (event.hasTime()) {
                    System.out.println(event.getTime()+": "+event.getName()); 
                    if (current_time<event.getTime()) {
                        current_time = event.getTime(); 
                    }
                }
            }
        }
        for (Builder event : metroIIEventList) {
            if (event.getStatus() == Status.NOTIFIED) {
                if (!event.hasTime()) {
                    event.setTime(current_time); 
                    System.out.println(current_time+"-: "+event.getName()); 
                } 
            }
        }
    }
    
    private long current_time; 

}
