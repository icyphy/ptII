package ptolemy.domains.metroII.kernel;

import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event.Builder;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event.Status;

public class TimeScheduler implements ConstraintSolver {

    public TimeScheduler() {
    }
    
    @Override
    public void resolve(Iterable<Builder> metroIIEventList) {
        long time = Long.MAX_VALUE;
        boolean hasEventWithoutTime = false;
        for (Builder event : metroIIEventList) {
            if (event.getStatus() == Status.NOTIFIED) {
                if (event.hasTime()) {
                    if (event.getTime() < time) {
                        time = event.getTime();
                    }
                } else {
                    hasEventWithoutTime = true;
                }
            }
        }
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

    }

}
