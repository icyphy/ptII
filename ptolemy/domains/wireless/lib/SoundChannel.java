/*
 * Created on Jul 23, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package ptolemy.domains.sensor.lib;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.apps.superb.sensor.kernel.BaseChannel;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.de.kernel.DEReceiver;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/**
 * @author liuxj
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class SoundChannel extends BaseChannel {

    /** Construct a relation in the default workspace with an empty string
     *  as its name. Add the relation to the directory of the workspace.
     */
    public SoundChannel() {
        super();
    }

    /** Construct a relation in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  Add the relation to the workspace directory.
     *
     *  @param workspace The workspace that will list the relation.
     */
    public SoundChannel(Workspace workspace) {
        super(workspace);
    }

    /** Construct a relation with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This relation will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  This constructor write-synchronizes on the workspace.
     *
     *  @param container The container.
     *  @param name The name of the relation.
     *  @exception IllegalActionException If the container is incompatible
     *   with this relation.
     *  @exception NameDuplicationException If the name coincides with
     *   a relation already in the container.
     */
    public SoundChannel(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        propagationSpeed = new Parameter(this, "propagationSpeed");
        propagationSpeed.setTypeEquals(BaseType.DOUBLE);
        propagationSpeed.setExpression("340.0");
    }

    public Parameter propagationSpeed;

    /* (non-Javadoc)
     * @see ptolemy.actor.IORelation#deepReceivers(ptolemy.actor.IOPort)
     */
    public Receiver[][] deepReceivers(IOPort except) {

        double range = 0.0;
        try {
            range = rangeOf(except, "soundRange");
        } catch (IllegalActionException ex) {
            // ignore
        }
        if (range < 0.0) return EMPTY_RECEIVERS;

        double speed = _getPropagationSpeed();

        List receiverList = new LinkedList();
        Iterator ports = potentialDestinationsOf(except).iterator();
        while (ports.hasNext()) {
            IOPort port = (IOPort)ports.next();
            if (port.getContainer() == except.getContainer()) continue;
            double distance = Double.MAX_VALUE;
            try {
                distance = distanceBetween(locationOf(except), locationOf(port));
            } catch (IllegalActionException ex) {
                // ignore
            }
            if (range >= distance) {
                Receiver[][] receivers = null;
                if (port.getContainer() == getContainer() && port.isOutput()) {
                    receivers = port.getInsideReceivers();
                } else if (port.isInput()){
                    receivers = port.getReceivers();
                }
                //if (receivers == null) continue;
                try {
                    ((DEReceiver)receivers[0][0]).setDelay(distance/speed);
                    receiverList.add(receivers[0][0]);
                } catch (IllegalActionException e) {
                    throw new InternalErrorException(
                            "Failed to set delay of wireless receiver.");
                }
            }
        }
        Receiver[][] receivers = new Receiver[1][receiverList.size()];
        Iterator receiverIterator = receiverList.iterator();
        int i = 0;
        while (receiverIterator.hasNext()) {
            Receiver element = (Receiver)receiverIterator.next();
            receivers[0][i] = element;
            ++i;
        }
        return receivers;
    }

    /**
     * @return
     */
    private double _getPropagationSpeed() {
        double result = 340.0;
        try {
            result = ((DoubleToken)propagationSpeed.getToken()).doubleValue();
        } catch (Exception ex) {
            //FIXME: ignore for now
        }
        return result;
    }
}
