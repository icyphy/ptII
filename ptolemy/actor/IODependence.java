/* An IODependence contains the input/output dependence information
of an actor.

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

@ProposedRating Red (hyzheng@eecs.berkeley.edu)
@AcceptedRating Red (hyzheng@eecs.berkeley.edu)
*/

package ptolemy.actor;

import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

import java.util.Iterator;
import java.util.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// IODependence
/** An IODependence contains the dependence information between the inputs
and outputs. It is an attribute associated with an actor. For atomic actors,
this attribute is constructed inside the constructor. For composite actors,
this attribute is constructed in the preinitialize method of the composite
actors, after their directors finish preinitialization. The process is in a
bottom-up way because the upper level IO dependence information is built on
those of the lower level.
<p>
It contains a list of elements of <i>IOInformation</i>, each corresponds to
one input port. To access the IOInformation, use <i>getInputPort(IOPort)</i>
method which does name mapping search. To add one IOInformation of an input
port, use <i>addInputPort(IOPort)</i>. This method returns an IOInformation
object.
<p>
Each input port has an IOInformation, which is an inner class providing
access to the relation between the input port and all the output ports.
Output ports are divided into three groups: (a)those immediately dependent
on the input, (b)those not immediately dependent on the input, and (c)those
not dependent on the input. The outputs in group (a) can be accessed with
<i>getDelayToPorts()</i>method, and the outputs in group (b) can be accessed
with <i>getDirectFeedthroughPort</i> method. The outputs in group (c) are
discarded.


@author Haiyang Zheng
@version $Id$
@since Ptolemy II 3.1
*/
public class IODependence extends Attribute {

    /** Construct an IODependence in the default workspace with an
     *  empty string as its name. The director is added to the list
     *  of objects in the workspace. Increment the version number
     *  of the workspace.
     */
    public IODependence() {
        super();
    }

    /** Construct an IODependence in the workspace with an empty name.
     *  The IODependence is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace of this object.
     */
    public IODependence(Workspace workspace) {
        super(workspace);
    }

    /** Construct an IODependence in the given container with the
     *  given name. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *
     *  @param container The container.
     *  @param name The name of this director.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public IODependence(Entity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add an input port into the input ports list of an actor.
     *
     *  @param inputPort An input port of an actor.
     *  @return an IOInformation object associated with the input port.
     *  @exception IllegalActionException If the port is not an input port
     *  or the same port already exists.
     */
    public IOInformation addInputPort(IOPort inputPort)
            throws IllegalActionException {
        if (inputPort.isInput() && !_inputPorts.contains(inputPort)) {
            IOInformation _inputInfo =
                new IOInformation(inputPort.getName());
            _inputPorts.add(_inputInfo);
            return _inputInfo;
        }
        else {
            throw new IllegalActionException (this, inputPort,
                "is not an inputPort or has been added.");
        }
    }

    /** Clear the input ports list. This method is called in the
     *  preinitialize method only by composite actors when the topology
     *  of the embedded entities change.
     */
    public void clear() {
        _inputPorts.clear();
    }

    /** Return the IOInformation associated with the given input port.
     *
     *  @param inputPort An input port of an actor.
     *  @return an IOInformation object associated with the input port, or null
     *  if the input port is not added into the input ports list.
     */
    public IOInformation getInputPort(IOPort inputPort) {
        Iterator _inputPortsIterator = _inputPorts.listIterator();
        while (_inputPortsIterator.hasNext()) {
            IOInformation input =
            (IOInformation) _inputPortsIterator.next();
            if (inputPort.getName().equals(input.getName())) {
                return input;
            }
        }
        return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                    ////

    // The input ports list.
    private LinkedList _inputPorts = new LinkedList();

    ///////////////////////////////////////////////////////////////////
    ////                         inner class                       ////

    /** An IOInformation is associated with an input port of an actor.
     *  It provides the dependence information between the input port and
     *  all the outputs of the actor.
     */
    public class IOInformation {

        /** Constructor for the class.
         *
         * @param name The name for the IOInformation, usually the input
         *  port name.
         */
        public IOInformation(String name) {
            _inputPortName = name;
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /** Add an output port into the delayed ports list.
         *  @param ioPort The output port to be added.
         */
        public void addToDelayToPorts(IOPort ioPort) {
            _delayToPorts.add(ioPort);
        }

        /** Add an output port into the direct feedthrough ports list.
         *  @param ioPort The output port to be added.
         */
        public void addToDirectFeedthroughPorts(IOPort ioPort) {
            _directFeedThroughPorts.add(ioPort);
        }

        /** Return the name of the IOInformation object.
         *  @return The name of the IOInformation object.
         */
        public String getName() {
            return _inputPortName;
        }

        /** Get all the output ports of the delayed ports list.
         *  @return The list of delayed ports list.
         */
        public LinkedList getDelayToPorts() {
            return _delayToPorts;
        }

        /** Get all the output ports of the direct feedthrough
         *  ports list.
         *  @return The list of direct feedthrough ports list.
         */
        public LinkedList getDirectFeedthroughPorts() {
            return _directFeedThroughPorts;
        }
        ///////////////////////////////////////////////////////////////////
        ////                         private variables                    ////

        private String _inputPortName;
        private LinkedList _delayToPorts = new LinkedList();
        private LinkedList _directFeedThroughPorts = new LinkedList();
    }
}
