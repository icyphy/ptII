package ptserver.actor;

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public class RemoteActor extends TypedAtomicActor {
    private String originalActorName;

    public RemoteActor(CompositeEntity container, ComponentEntity entity)
            throws IllegalActionException, NameDuplicationException,
            CloneNotSupportedException {
        super(container, entity.getName() + "_remote");
        for (Object p : entity.portList()) {
            if (!(p instanceof IOPort)) {
                break;
            }
            IOPort port = (IOPort) p;
            IOPort remotePort = (IOPort) port.clone(this.workspace());
            remotePort.setName(port.getName());
            remotePort.setContainer(this);
            for (Object r : port.linkedRelationList()) {
                Relation relation = (Relation) r;
                port.unlink(relation);
                remotePort.link(relation);
            }
            port.unlinkAll();
        }
        entity.setContainer(null);
        setOriginalActorName(entity.getName());
    }

    public void setOriginalActorName(String originalActorName) {
        this.originalActorName = originalActorName;
    }

    public String getOriginalActorName() {
        return originalActorName;
    }

}
