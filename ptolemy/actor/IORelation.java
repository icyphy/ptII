/* Relation supporting message passing.

 Copyright (c) 1997-2000 The Regents of the University of California.
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

@ProposedRating Green (eal@eecs.berkeley.edu)
@AcceptedRating Green (davisj@eecs.berkeley.edu)

*/

package ptolemy.actor;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;

import java.util.List;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Collections;

import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// IORelation
/**
This class mediates connections between ports that can send data to
one another via message passing. One purpose of this relation is to
ensure that IOPorts are only connected to IOPorts. A second purpose
is to support the notion of a <i>width</i> to represent something
like a bus. By default an IORelation is not a bus, which means that
its width can only be zero or one. Calling setWidth() with
an argument larger than one makes the relation a bus of fixed width.
Calling setWidth() with an argument of zero makes the relation
a bus with indeterminate width, in which case the width will be
inferred (if possible) from the context.  The actual width of an IORelation
can never be less than one.
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
     *  as its name. Add the relation to the directory of the workspace.
     */
    public IORelation() {
        super();
    }

    /** Construct a relation in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  Add the relation to the workspace directory.
     *
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
     *  This constructor write-synchronizes on the workspace.
     *
     *  @param container The container.
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

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a new relation with no links and no container, but with
     *  the same width as the original.
     *
     *  @param ws The workspace for the cloned object.
     *  @exception CloneNotSupportedException If one or more of the attributes
     *   cannot be cloned.
     *  @return A new ComponentRelation.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        IORelation newobj = (IORelation)super.clone(ws);
        newobj._inferredWidthVersion = -1;
        return newobj;
    }

    /** Return the receivers of all input ports linked to this
     *  relation, directly or indirectly, except those in the port
     *  given as an argument. The returned value is an array of
     *  arrays. The first index (the row) specifies the group, where
     *  a group receives the same data from a channel.
     *  Each channel normally receives distinct data. The
     *  second index (the column) specifies the receiver number within
     *  the group of receivers that get copies from the same channel.
     *  <p>
     *  The number of groups (rows) is less than or equal to the
     *  width of the relation, which is always at least one. If
     *  there are no receivers then return null.
     *  <p>
     *  For each channel, there may be any number of receivers in the group.
     *  The individual receivers are selected using the second index of the
     *  returned array of arrays.  If there are no receivers in the group,
     *  then the channel is represented by null.  I.e., if the returned
     *  array of arrays is <i>x</i> and the channel number is <i>c</i>,
     *  then <i>x</i>[<i>c</i>] is null.  Otherwise, it is an array, where
     *  the size of the array is the number of receivers in the group.
     *  <p>
     *  NOTE: This method may have the effect of creating new receivers in the
     *  remote input ports and losing the previous receivers in those ports,
     *  together with any data they may contain.  This occurs only if the
     *  topology has changed since the receivers were created, and that change
     *  resulting in one of those ports not having the right number of
     *  receivers.
     *  <p>
     *  This method read-synchronizes on the workspace.
     *
     *  @see IOPort#getRemoteReceivers
     *  @param except The port to exclude.
     *  @return The receivers associated with this relation.
     */
    public Receiver[][] deepReceivers(IOPort except) {
        try {
            _workspace.getReadAccess();
            Receiver[][] result = new Receiver[0][0];
            Iterator inputs =
                linkedDestinationPortList(except).iterator();
            Receiver[][] recvrs = new Receiver[0][0];
            while(inputs.hasNext()) {
                IOPort p = (IOPort) inputs.next();

                if(p.isInsideLinked(this) && !p.isOpaque()) {
                    // if p is a transparent port and this relation links
                    // from the inside, then get the Receivers outside p.
                    try {
                        recvrs = p.getRemoteReceivers(this);
                    } catch (IllegalActionException e) {
                        throw new InternalErrorException(
                                "IORelation.deepReceivers: Internal error: "
                                + e.getMessage());
                    }
                } else {
                    // if p not a transparent port, or this relation is linked
                    // to p from the outside.
                    try {
                        // Note that this may be an inside or outside linked
                        // relation.
                        recvrs = p.getReceivers(this);
                    } catch (IllegalActionException e) {
                        throw new InternalErrorException(
                                "IORelation.deepReceivers: Internal error: "
                                + e.getMessage());
                    }
                }
                result = _cascade(result, recvrs);
            }
            return result;
        } finally {
            _workspace.doneReading();
        }
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
     *
     *  @return The width, which is at least one.
     */
    public int getWidth() {
        if (_width == 0) {
            return _inferWidth();
        }
        return _width;
    }

    /** Return true if the relation has a definite width (i.e.,
     *  setWidth() has not been called with a zero argument).
     *  @return True if the width has been set to non-zero.
     */
    public boolean isWidthFixed() {
        return (_width != 0);
    }

    /** Enumerate the input ports that we are linked to from the outside,
     *  and the output ports that we are linked to from the inside.
     *  I.e., enumerate the ports through or to which we could send data.
     *  This method is deprecated and calls linkedDestinationPortList().
     *  This method read-synchronizes on the workspace.
     *
     *  @see ptolemy.kernel.Relation#linkedPorts
     *  @deprecated Use linkedDestinationPortList() instead.
     *  @return An enumeration of IOPort objects.
     */
    public Enumeration linkedDestinationPorts() {
        return linkedDestinationPorts(null);
    }

    /** List the input ports that we are linked to from the
     *  outside, and the output ports that we are linked to from
     *  the inside. I.e., list the ports through or to which we
     *  could send data. This method read-synchronizes on the workspace.
     *
     *  @see ptolemy.kernel.Relation#linkedPorts
     *  @return An enumeration of IOPort objects.
     */
    public List linkedDestinationPortList() {
        return linkedDestinationPortList(null);
    }

    /** Enumerate the input ports that we are linked to from the
     *  outside, and the output ports that we are linked to from
     *  the inside, except the port given as an argument.
     *  I.e., enumerate the ports through or to which we could send data.
     *  This method is deprecated and calls
     *  linkedDestinationPortList(IOPort).
     *  This method read-synchronizes on the workspace.
     *
     *  @see ptolemy.kernel.Relation#linkedPorts(ptolemy.kernel.Port)
     *  @param except The port not included in the returned Enumeration.
     *  @deprecated Use linkDestinationPortList(IOPort) instead.
     *  @return An enumeration of IOPort objects.
     */
    public Enumeration linkedDestinationPorts(IOPort except) {
        return Collections.enumeration( linkedDestinationPortList(except) );
    }

    /** Enumerate the input ports that we are linked to from the
     *  outside, and the output ports that we are linked to from
     *  the inside, except the port given as an argument and return
     *  a list of list of these ports.
     *  I.e., list the ports through or to which we could send data.
     *  If the given port is null or is not linked to this relation,
     *  then enumerate all the input ports.
     *  This method read-synchronizes on the workspace.
     *
     *  @see ptolemy.kernel.Relation#linkedPorts(ptolemy.kernel.Port)
     *  @param except The port not included in the returned Enumeration.
     *  @return A list of IOPort objects.
     */
    public List linkedDestinationPortList(IOPort except) {
        try {
            _workspace.getReadAccess();
            // NOTE: The result could be cached for efficiency, but
            // it would have to be cached in a hashtable indexed by the
            // except argument.  Probably not worth it.
            LinkedList resultPorts = new LinkedList();
            Iterator ports = linkedPortList().iterator();
            while(ports.hasNext()) {
                IOPort p = (IOPort) ports.next();
                if (p != except) {
                    if(p.isInsideLinked(this)) {
                        // Linked from the inside
                        if(p.isOutput()) resultPorts.addLast(p);
                    } else {
                        if(p.isInput()) resultPorts.addLast(p);
                    }
                }
            }
            return resultPorts;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Enumerate the output ports that we are linked to from the outside
     *  and the input ports that we are linked to from the inside.
     *  I.e. enumerate the ports from or through which we might receive
     *  data. This method is deprecated and calls
     *  linkedSourcePortList().
     *  This method read-synchronizes on the workspace.
     *
     *  @see ptolemy.kernel.Relation#linkedPorts
     *  @deprecated Use linkedSourcePortList() instead.
     *  @return An enumeration of IOPort objects.
     */
    public Enumeration linkedSourcePorts() {
        return Collections.enumeration( linkedSourcePortList() );
    }

    /** List the output ports that we are linked to from the outside
     *  and the input ports that we are linked to from the inside.
     *  I.e. list the ports from or through which we might receive
     *  data. This method read-synchronizes on the workspace.
     *
     *  @see ptolemy.kernel.Relation#linkedPorts
     *  @return An enumeration of IOPort objects.
     */
    public List linkedSourcePortList() {
        return linkedSourcePortList(null);
    }

    /** Enumerate the output ports that we are linked to from the outside
     *  and the input ports that we are linked to from the inside.
     *  I.e. enumerate the ports from or through which we might receive
     *  This method is deprecated and calls
     *  linkedSourcePortList(IOPort).
     *  This method read-synchronizes on the workspace.
     *  @see ptolemy.kernel.Relation#linkedPorts(ptolemy.kernel.Port)
     *  @param except The port not included in the returned Enumeration.
     *  @deprecated Use linkedSourcePortList(IOPort) instead.
     *  @return An enumeration of IOPort objects.
     */
    public Enumeration linkedSourcePorts(IOPort except) {
        return Collections.enumeration( linkedSourcePortList(except) );
    }

    /** List the output ports that we are linked to from the outside
     *  and the input ports that we are linked to from the inside.
     *  I.e. list the ports from or through which we might receive
     *  If the given port is null or is not linked to this relation,
     *  then enumerate all the output ports.
     *  This method read-synchronizes on the workspace.
     *  @see ptolemy.kernel.Relation#linkedPorts(ptolemy.kernel.Port)
     *  @param except The port not included in the returned list.
     *  @return A list of IOPort objects.
     */
    public List linkedSourcePortList(IOPort except) {
        try {
            _workspace.getReadAccess();
            // NOTE: The result could be cached for efficiency, but
            // it would have to be cached in a hashtable indexed by the
            // except argument.  Probably not worth it.
            LinkedList resultPorts = new LinkedList();
            Iterator ports = linkedPortList().iterator();
            while(ports.hasNext()) {
                IOPort p = (IOPort) ports.next();
                if(p != except) {
                    if(p.isInsideLinked(this)) {
                        // Linked from the inside
                        if(p.isInput()) resultPorts.addLast(p);
                    } else {
                        if(p.isOutput()) resultPorts.addLast(p);
                    }
                }
            }
            return resultPorts;
        } finally {
            _workspace.doneReading();
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
     *  instance of CompositeActor or null, otherwise an exception is thrown.
     *  Derived classes may further constrain the class of the container
     *  to a subclass of CompositeActor.
     *  <p>
     *  This method is write-synchronized on the workspace.
     *
     *  @param container The proposed container.
     *  @exception IllegalActionException If the container is not a
     *   CompositeActor or null, or this entity and the container are not in
     *   the same workspace.
     *  @exception NameDuplicationException If the name collides with a name
     *   already on the relations list of the container.
     */
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        if (!(container instanceof CompositeActor) && (container != null)) {
            throw new IllegalActionException (this, container,
                    "IORelation can only be contained by CompositeActor.");
        }
        super.setContainer(container);
    }

    /** Set the width of the IORelation. The width is the number of
     *  channels that the relation represents.  If the argument
     *  is zero, then the relation becomes a bus with unspecified width,
     *  and the width will be inferred from the way the relation is used
     *  (but will never be less than one).
     *  If the argument is not one, then all linked ports must
     *  be multiports, or an exception is thrown.
     *  This method write-synchronizes on the workspace.
     *
     *  @param width The width of the relation.
     *  @exception IllegalActionException If the argument is greater than
     *   one and the relation is linked to a non-multiport, or it is zero and
     *   the relation is linked on the inside to a port that is already
     *   linked on the inside to a relation with unspecified width.
     *  @see ptolemy.kernel.util.Workspace#getWriteAccess()
     */
    public void setWidth(int width) throws IllegalActionException {
        try {
            _workspace.getWriteAccess();
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
                Iterator ports = linkedPortList().iterator();
                while(ports.hasNext()) {
                    IOPort p = (IOPort) ports.next();
                    if (!p.isMultiport()) {
                        throw new IllegalActionException(this, p,
                                "Cannot make bus because the " +
                                "relation is linked to a non-multiport.");
                    }
                }
            }
            _width = width;
        } finally {
            _workspace.doneWriting();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Indicate that the description(int) method should describe the width
     *  of the relation, and whether it has been fixed.
     */
    public static final int CONFIGURATION = 512;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Throw an exception if the specified port cannot be linked to this
     *  relation (is not of class IOPort).
     *  @param port The candidate port to link to.
     *  @exception IllegalActionException If the port is not an IOPort.
     */
    protected void _checkPort (Port port) throws IllegalActionException {
        if (!(port instanceof IOPort)) {
            throw new IllegalActionException(this, port,
                    "IORelation can only link to a IOPort.");
        }
    }

    /** Return a description of the object.  The level of detail depends
     *  on the argument, which is an or-ing of the static final constants
     *  defined in the NamedObj class and in this class.
     *  Lines are indented according to
     *  to the level argument using the protected method _getIndentPrefix().
     *  Zero, one or two brackets can be specified to surround the returned
     *  description.  If one is specified it is the the leading bracket.
     *  This is used by derived classes that will append to the description.
     *  Those derived classes are responsible for the closing bracket.
     *  An argument other than 0, 1, or 2 is taken to be equivalent to 0.
     *  <p>
     *  If the detail argument sets the bit defined by the constant
     *  CONFIGURATION, then append to the description is a field
     *  of the form "configuration {width <i>integer</i> ?fixed?}", where the
     *  word "fixed" is present if the relation has fixed width, and is
     *  absent if the relation is a bus with inferred width (isWidthFixed()
     *  returns false).
     *
     *  This method is read-synchronized on the workspace.
     *
     *  @param detail The level of detail.
     *  @param indent The amount of indenting.
     *  @param bracket The number of surrounding brackets (0, 1, or 2).
     *  @return A description of the object.
     */
    protected String _description(int detail, int indent, int bracket) {
        try {
            _workspace.getReadAccess();
            String result;
            if (bracket == 1 || bracket == 2) {
                result = super._description(detail, indent, 1);
            } else {
                result = super._description(detail, indent, 0);
            }
            if ((detail & CONFIGURATION) != 0) {
                if (result.trim().length() > 0) {
                    result += " ";
                }
                result += "configuration {";
                result += "width " + getWidth();
                if (isWidthFixed()) result += " fixed";
                result += "}";
            }
            if (bracket == 2) result += "}";
            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Cascade two Receiver arrays to form a new array. For each row, each
    // element of the second array is appended behind the elements of the
    // first array. This method is solely for deepReceivers.
    // The two input arrays must have the same number of rows.
    private Receiver[][] _cascade(Receiver[][] array1, Receiver[][] array2)
            throws InvalidStateException {
        if (array1.length <= 0) {
            return array2;
        }
        if (array2.length <= 0) {
            return array1;
        }
        int width = getWidth();
        Receiver[][] result = new Receiver[width][0];

        for (int i = 0; i < width; i++) {
            if(array1[i] == null) {
		result[i] = array2[i];
	    } else if(array1[i].length <= 0) {
		result[i] = array2[i];
            } else if(array2[i] == null) {
		result[i] = array1[i];
	    } else if(array2[i].length <= 0) {
		result[i] = array1[i];
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

    // Infer the width of the port from how it is connected.
    // Throw a runtime exception if this cannot be done (normally,
    // the methods that construct a topology ensure that it can be
    // be done).  The returned value is always at least one.
    // This method is not read-synchronized on the workspace, so the caller
    // should be.
    private int _inferWidth() {
        long version = _workspace.getVersion();
        if (version != _inferredWidthVersion) {
            _inferredWidth = 1;
            Iterator ports = linkedPortList().iterator();
            while(ports.hasNext()) {
                IOPort p = (IOPort) ports.next();
                if (p.isInsideLinked(this)) {
                    // I am linked on the inside...
                    int piw = p._getInsideWidth(this);
                    int pow = p.getWidth();
                    int diff = pow - piw;
                    if (diff > _inferredWidth) _inferredWidth = diff;
                }
            }
            _inferredWidthVersion = version;
        }
        return _inferredWidth;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // width of the relation.
    private int _width = 1;

    // cached inferred width.
    private transient int _inferredWidth;
    private transient long _inferredWidthVersion = -1;
}
