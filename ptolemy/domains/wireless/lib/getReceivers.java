/* The base class of communication channels in the sensor domain.

 Copyright (c) 2003 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY

@ProposedRating Red (sanjeev@eecs.berkeley.edu)
@AcceptedRating Red (sanjeev@eecs.berkeley.edu)
*/
package ptolemy.domains.wireless.lib;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.Receiver;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.DoubleToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.RecordToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.wireless.kernel.ModelTopology;
import ptolemy.domains.wireless.kernel.Reception;
import ptolemy.domains.wireless.kernel.WirelessIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// GetReceivers
/**
Get the reachable receivers for a port according to the specified range
in the properties or using a default range if no "range" property specified.

@author Yang Zhao and Edward A. Lee
@version $ $
@since Ptolemy II 2.1
*/
public class getReceivers extends TypedAtomicActor {

    /** Construct a relation with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This relation will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  This constructor write-synchronizes on the workspace.
     *  @param container The container.
     *  @param name The name of the relation.
     *  @exception IllegalActionException If the container is incompatible
     *   with this relation.
     *  @exception NameDuplicationException If the name coincides with
     *   a relation already in the container.
     */
    public getReceivers(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        reception1 = new TypedIOPort(this, "reception1", true, false);
        reception1.setTypeEquals(BaseType.OBJECT);
        reception2 = new TypedIOPort(this, "reception2", false, true);
        reception2.setTypeEquals(BaseType.OBJECT);  
        defaultRange = new Parameter(this, "defaultRange");
        defaultRange.setTypeEquals(BaseType.DOUBLE);
        defaultRange.setExpression("Infinity");      
               
    }
    
    /** The input port.
     */
    public TypedIOPort reception1;
    /** The output port.
     */
    public TypedIOPort reception2;    
    
    public Parameter defaultRange;

    /** Calculate the receivers list for a received Reception.
     *
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        if (_debugging) _debug("invoking fire of : " + this.getName() +" \n");
            if (reception1.hasToken(0)) {
                ObjectToken t = (ObjectToken)reception1.get(0);
                Reception rec = (Reception)t.getValue();
                rec.receivers = (LinkedList) _receiversInRange(rec.sender, rec.properties);
                reception2.send(0, new ObjectToken(rec));
            }
    }
    

    /** Return true if the specified port is in range of the
     *  specified source port, assuming the source port transmits with
     *  the specified properties.  If the properties are an instance of
     *  DoubleToken, then that token is assumed to be the range of the
     *  transmission.  This method returns true if the distance between
     *  the source and the destination is less than or equal to the
     *  value of the properties.  If the properties argument is not
     *  a DoubleToken, then it simply returns true.
     *  @param source The source port.
     *  @param destination The destination port.
     *  @param properties The range of transmission.
     *  @return True if the destination is in range of the source.
     *  @throws IllegalActionException If it cannot be determined
     *   whether the destination is in range (not thrown in this base
     *   class).
     */
    protected boolean _isInRange(
            WirelessIOPort source, WirelessIOPort destination, Token properties)
            throws IllegalActionException {
        double range;
        if (properties instanceof RecordToken) {
            // NOTE: This may throw a NotConvertibleException, if,
            // example, a Complex or a Long is given.
            Token field = ((RecordToken)properties).get("range");
            if (field instanceof ScalarToken) {
                range = ((ScalarToken)field).doubleValue();
            } else {
                throw new IllegalActionException(this,
                "Properties token has a field called \"range\""
                + " but that field does not have a double value.");
            }
        } else {
            
            range = ((DoubleToken)defaultRange.getToken()).doubleValue();
            
        }
        boolean result = (ModelTopology.distanceBetween(source, destination) <= range);
        return result;
    }

    /** Return the list of receivers that can receive from the specified
     *  port with the specified transmit properties. Ports that are contained
     *  by the same container as the specified <i>sourcePort</i> are
     *  not included.  Note that this method does
     *  not guarantee that those receivers will receive.  That is determined
     *  by the transmit() method, which subclasses may override to, for
     *  example, introduce probabilitic message losses.
     *  The calling method is expected to have read access on the workspace.
     *  @param sourcePort The sending port.
     *  @param properties The transmit properties (ignored in this base class).
     *  @return A list of instances of WirelessReceiver.
     *  @exception IllegalActionException If a location of a port cannot be
     *   evaluated.
     */
    protected List _receiversInRange(
            WirelessIOPort sourcePort, Token properties)
            throws IllegalActionException {
    try {
        workspace().getReadAccess();
        // NOTE: Cannot cache this. Properties parameter of the transmitting
        // port may have changed, and the workspace version would not be
        // incremented.  But anyway, how could this be independent of
        // the sourcePort?
        List receiversInRangeList = new LinkedList();
        CompositeEntity container1 = (CompositeEntity) this.getContainer();
        CompositeEntity container = (CompositeEntity) (container1.getContainer());
        Iterator ports = ModelTopology.listeningInputPorts
                (container, container1.getName()).iterator();
        while (ports.hasNext()) {
            WirelessIOPort port = (WirelessIOPort)ports.next();
            
            // Skip ports contained by the same container as the source.
            if (port.getContainer() == sourcePort.getContainer()) continue;
            
            if (_isInRange(sourcePort, port, properties)) {
                Receiver[][] receivers = port.getReceivers();
                for (int i = 0; i < receivers.length; i++) {
                    for (int j = 0; j < receivers[i].length; j++) {
                        receiversInRangeList.add(receivers[i][j]);
                    }
                }
            }
        }
        ports = ModelTopology.listeningOutputPorts
                (container, container1.getName()).iterator();
        while (ports.hasNext()) {
            WirelessIOPort port = (WirelessIOPort)ports.next();
                        
            if (_isInRange(sourcePort, port, properties)) {
                Receiver[][] receivers = port.getInsideReceivers();
                for (int i = 0; i < receivers.length; i++) {
                    for (int j = 0; j < receivers[i].length; j++) {
                        receiversInRangeList.add(receivers[i][j]);
                    }
                }
            }
        }
        return receiversInRangeList;
    } 
    finally {
    workspace().doneReading();
    }
    }
    
        
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Name of the location attribute.
    private static final String LOCATION_ATTRIBUTE_NAME = "_location";
}
