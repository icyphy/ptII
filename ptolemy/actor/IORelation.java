/* Relation supporting message passing.

 Copyright (c) 1997- The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)

*/

package pt.actors;

import pt.kernel.*;
import java.util.Enumeration;
import collections.*;

//////////////////////////////////////////////////////////////////////////
//// IORelation
/** 
This class mediates connections between ports that can send data to
one another via message passing. One purpose of this relation is to
ensure that IOPorts are only connected to IOPorts. A second purpose
is to support the notion of a <i>width</i> to represent something
like a bus. By default the relation is not a bus, which means that
its width can only be zero or one. Calling <code>setWidth()</code> with
an argument larger than one makes the relation a bus of fixed width.
Calling <code>setWidth()</code> with an argument of zero makes the relation
a bus with indeterminate width, in which case the width will be
inferred (if possible) from the context.
<p>
Instances of IORelation can only be linked to instances of IOPort.
Derived classes may further constrain this to subclasses of IOPort.
Such derived classes should override the protected method _checkPort()
to throw an exception.
<p>
To link a IOPort to a IORelation, use the link() or
liberalLink() method in the IOPort class.  To remove a link,
use the unlink() method.
<p>
The container for instances of this class can only be instances of
CompositeActor.  Derived classes may wish to further constrain the
container to subclasses of ComponentEntity.  To do this, they should
override the setContainer() method.

@author Edward A. Lee, Jie Liu
@version $Id$
*/
public class IORelation extends ComponentRelation {

    /** Construct a relation in the default workspace with an empty string
     *  as its name.
     *  Increment the version number of the workspace.
     */
    public IORelation() {
        super();
    }

    /** Construct a relation in the specified workspace with an empty
     *  string as a name.
     *  If the workspace argument is null, then use the default workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the relation.
     */
    public IORelation(Workspace workspace) {
	super(workspace);
    }

    /** Construct a relation with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This relation will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container entity.
     *  @param name The name of the relation.
     *  @exception IllegalActionException If the container is incompatible
     *   with this relation.
     *  @exception NameDuplicationException If the name coincides with
     *   a relation already in the container.
     */	
    public IORelation(CompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////
    
    /** Return the receivers of all input ports linked to this
     *  relation, directly or indirectly, except those in the port
     *  given as an argument. The returned value is an array of
     *  arrays. The first index (the row) specifies the channel
     *  number. Each channel normally receives distinct data. The
     *  second index (the column) specifies the receiver number within
     *  the group of receivers that get copies of the same channel.
     *  The number of channels (rows) is less than or equal to the
     *  width of the relation, which is always at least one. If
     *  there are no receivers then return null.
     *  This method synchronizes on the workspace.
     *  @see IOPort#getRemoteReceivers
     *  @param except The port to exclude.
     *  @return The receivers associated with this relation.
     *  @exception InvalidStateException If an inconsistency is found
     *   between the width of the input ports and the width of this
     *   relation.
     */
    public Receiver[][] deepReceivers(IOPort except)
            throws InvalidStateException {
        Receiver[][] result = null;
        Enumeration inputs = linkedDestinationPorts(except);
        Receiver[][] recvrs = null;
        while(inputs.hasMoreElements()) {
            IOPort p = (IOPort) inputs.nextElement();
            
            if(p.isInsideLinked(this)) {
                // if p is a transparent port and this relation links from
                // the inside, then get the Receivers outside p.
                try {
                    recvrs = p.getRemoteReceivers(this);
                } catch (IllegalActionException e) {
                    //Ignored. linkedInside is checked.
                }
            } else {
                // if p not a transparent port, or this relation is linked
                // to p from the outside.
                try {
                    recvrs = p.getReceivers(this);
                } catch (IllegalActionException e) {
                    //Ignored. link is checked
                }
            }
            result = _cascade(result, recvrs);
        }
        return result;
    }
            
    /** Return the width of the IORelation, which is always at least one.
     *  If the width has been set to zero, then the relation is a bus with
     *  unspecified width, and the width needs to be inferred from the
     *  way the relation is connected.  This is done by checking the
     *  ports that this relation is linked to from the inside and setting
     *  the width to the maximum of those port widths, minus the widths of
     *  other relations linked to those ports on the inside. Each such port is
     *  allowed to have at most one inside relation with an unspecified
     *  width, or an exception is thrown.  If this inference yields a width
     *  of zero, then return one.
     *  @return width
     *  @exception InvalidStateException If a port has more than one
     *   inside relation with unspecified width.
     */	
    public int getWidth() {
        if (_width == 0) {
            return _inferWidth();
        }
        return _width;
    }

    /** Enumerate the input ports that we are linked to from the
     *  outside, and the output ports that we are linked to from the inside.
     *  I.e., enumerate the ports through or to which we could send data.
     *  This method synchronizes on the workspace.
     *  @see pt.kernel.Relation#linkedPorts
     *  @return An enumeration of the linked input ports
     */	
    public Enumeration linkedDestinationPorts() {
        return linkedDestinationPorts(null);
    }

    /** Enumerate the input ports that we are linked to from the
     *  outside, and the output ports that we are linked to from
     *  the inside, except the port given as an argument.
     *  I.e., enumerate the ports through or to which we could send data.
     *  If the given port is null or is not linked to this relation,
     *  then enumerate all the input ports.
     *  This method synchronizes on the workspace.
     *  @see pt.kernel.Relation#linkedPorts(pt.kernel.Port)
     *  @param except The port not included in the returned Enumeration.
     *  @return An enumeration of IOPort objects.
     */	
    public Enumeration linkedDestinationPorts(IOPort except) {
        synchronized(workspace()) {
            // NOTE: The result could be cached for efficiency.
            LinkedList resultPorts = new LinkedList();
            Enumeration ports = linkedPorts();
            while(ports.hasMoreElements()) {
                IOPort p = (IOPort) ports.nextElement();
                if (p != except) {
                    if(p.isInsideLinked(this)) {
                        // Linked from the inside
                        if(p.isOutput()) resultPorts.insertLast(p);
                    } else {
                        if(p.isInput()) resultPorts.insertLast(p);
                    }
                }
            }
            return resultPorts.elements();
        }
    }

    /** Enumerate the output ports that we are linked to from the outside
     *  and the input ports that we are linked to from the inside.
     *  I.e. enumerate the ports from or through which we might receive
     *  data.
     *  This method synchronizes on the workspace.
     *  @see pt.kernel.Relation#linkedPorts
     *  @return Ann enumeration of the linked input ports
     */	
    public Enumeration linkedSourcePorts() {
        return linkedSourcePorts(null);
    }

    /** Enumerate the output ports that we are linked to from the outside
     *  and the input ports that we are linked to from the inside.
     *  I.e. enumerate the ports from or through which we might receive
     *  If the given port is null or is not linked to this relation,
     *  then enumerate all the output ports.
     *  This method synchronizes on the workspace.
     *  @see pt.kernel.Relation#linkedPorts(pt.kernel.Port)
     *  @param except The port not included in the returned Enumeration.
     *  @return An enumeration of IOPort objects.
     */	
    public Enumeration linkedSourcePorts(IOPort except) {
        synchronized(workspace()) {
            // NOTE: The result could be cached for efficiency.
            LinkedList resultPorts = new LinkedList();
            Enumeration ports = linkedPorts();
            while(ports.hasMoreElements()) {
                IOPort p = (IOPort) ports.nextElement();
                if(p != except) {
                    if(p.isInsideLinked(this)) {
                        // Linked from the inside
                        if(p.isInput()) resultPorts.insertLast(p);
                    } else {
                        if(p.isOutput()) resultPorts.insertLast(p);
                    }
                }
            }
            return resultPorts.elements();
        }
    }

    /** Specify the container, adding the relation to the list 
     *  of relations in the container.  
     *  If this relation already has a container, remove it
     *  from that container first.  Otherwise, remove it from
     *  the list of objects in the workspace. If the argument is null, then
     *  unlink the ports from the relation, remove it from
     *  its container, and add it to the list of objects in the workspace.
     *  If the relation is already contained by the container, do nothing.
     *  <p>
     *  The container must be an
     *  instance of CompositeActor or an exception is thrown.
     *  Derived classes may further constrain the class of the container
     *  to a subclass of CompositeActor.
     *  <p>
     *  This method is synchronized on the
     *  workspace and increments its version number.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the container is not a
     *   CompositeActor, or this entity and the container are not in
     *   the same workspace.
     *  @exception NameDuplicationException If the name collides with a name 
     *   already on the contents list of the container.
     */	
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        if (!(container instanceof CompositeActor)) {
            throw new IllegalActionException (this, container,
                    "IORelation can only be contained by CompositeActor.");
        }
    }

    /** Set the width of the IORelation. The width is the number of
     *  channels that the relation represents.  If the argument
     *  is zero, then the relation becomes a bus with unspecified width,
     *  and the width will be inferred from the way the relation is used
     *  (but will never be less than one).
     *  If the argument is not one, then all linked ports must
     *  be multiports, or an exception is thrown.
     *  This method increments the workspace version number.
     *  @param width The width of the relation.
     *  @exception IllegalActionException If the argument is greater than
     *   one and the relation is linked to a non-multiport, or it is zero and
     *   the relation is linked on the inside to a port that is already
     *   linked on the inside to a relation with unspecified width.
     */
    public void setWidth(int width) throws IllegalActionException {
        if(width == 0) {
            // Check legitimacy of the change.
            try {
                _inferWidth();
            } catch (InvalidStateException ex) {
                throw new IllegalActionException(this,
                        "Cannot use unspecified width on this relation " +
                        "because of its links.");
            }
        }
        if (width != 1) {
            // Check for non-multiports
            Enumeration ports = linkedPorts();
            while(ports.hasMoreElements()) {
                IOPort p = (IOPort) ports.nextElement();
                if (!p.isMultiport()) {
                    throw new IllegalActionException(this, p,
                            "Cannot make bus because the " +
                            "relation is linked to a non-multiport.");
                }
            }
        }
        _width = width;
        workspace().incrVersion();
    }

    /** Return true if the relation has a definite width (i.e.,
     *  <code>setWidth()</code> has not been called with a zero argument).
     */
    public boolean widthFixed() {
        return (_width != 0);
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         protected methods                        ////

    /** Throw an exception if the specified port cannot be linked to this
     *  relation (is not of class IOPort).
     *  @param port The port to link to.
     *  @exception IllegalActionException If the port is not an IOPort.
     */
    protected void _checkPort (Port port) throws IllegalActionException {
        if (!(port instanceof IOPort)) {
            throw new IllegalActionException(this, port,
                    "IORelation can only link to a IOPort.");
        }
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         private methods                          ////

    /** Cascade two Receiver arrays to form a new array. For each row, each 
     *  element of the second array is appended behind the elements of the
     *  first array. This method is solely for deepReceivers.
     *  The two input arrays must have the same number of rows. 
     *  @param array1 the first array.
     *  @param array2 the second array.
     *  @exception InvalidStateException If the two arrays do not have
     *   the same number of rows.
     */
    private Receiver[][] _cascade(Receiver[][] array1, Receiver[][] array2)
            throws InvalidStateException {
        if (array1 == null) {
            return array2;
        }
        if (array2 == null) {
            return array1;
        }
        int width = getWidth();
        Receiver[][] result = new Receiver[width][];
        
        for (int i = 0; i < width; i++) {
            if(array1[i] == null) {
                result[i]= array2[i];
            } else if(array2[i] == null) {
                result[i]= array1[i];
            } else {
                int m1 = array1[i].length;
                int m2 = array2[i].length;
                result[i] = new Receiver[m1+m2];
                for (int j = 0; j < m1; j++) {
                    result[i][j] = array1[i][j];
                }
                for (int j = m1; j < m1+m2; j++) {
                    result[i][j] = array2[i][j-m1];
                }
            }
        }
        return result;
    }

    /** Infer the width of the port from how it is connected.
     *  Throw an exception if this cannot be done.  Returned value
     *  is always at least one.
     *  @InvalidStateException If the width cannot be inferred.
     */
    private int _inferWidth() throws InvalidStateException {
        int result = 1;
        Enumeration ports = linkedPorts();
        while(ports.hasMoreElements()) {
            IOPort p = (IOPort) ports.nextElement();
            if (p.isInsideLinked(this)) {
                // I am linked on the inside...
                int piw = p._getInsideWidth(this);
                int pow = p.getWidth();
                int diff = pow - piw;
                if (diff > result) result = diff;
            }
        }
        // FIXME: Should cache this result.
        return result;
    }

    //////////////////////////////////////////////////////////////////////////
            ////                         private variables                        ////

            // whether the relation is a bus
            private boolean _bus = false;

    // width of the relation. 
    private int _width = 1;
}
