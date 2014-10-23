/* Base class for sequenced actors with multiple fire methods.

 Copyright (c) 2009-2014 The Regents of the University of California.
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
 */

package ptolemy.domains.sequence.lib;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.IOPort;
import ptolemy.domains.sequence.kernel.MultipleFireMethodsInterface;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;

///////////////////////////////////////////////////////////////////
//// BaseMultipleMethodsActor

/** Base class for sequenced actors with multiple fire methods.
 *
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (cshelton)
 *  @Pt.AcceptedRating Red (cshelton)
 */
public abstract class BaseMultipleMethodsActor extends
SequencedSharedMemoryActor implements MultipleFireMethodsInterface {

    /** Create a new instance of an ASCETClassActor with the given
     *  name and container.
     *
     *  @param container The model in which the new actor will be contained.
     *  @param name The name of the new actor
     *  @exception IllegalActionException If the new actor cannot be created.
     *  @exception NameDuplicationException If there is already a NamedObj with
     *   the same name in the container model.
     */
    public BaseMultipleMethodsActor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Remove the inherited SetVariable input and output ports.
        // They are not used in the Srv_Debounce actor.
        input.setContainer(null);
        output.setContainer(null);

        // set name to invisible
        //StringAttribute hideName = new StringAttribute(this, "_hideName");
        //hideName.setExpression("true");

        _methodList = new LinkedList<String>();
        _fireMethodNameToInputPortList = new Hashtable<String, List<IOPort>>();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the name of the default fire method for this actor.
     *
     *  @return The string name of the default fire method for the actor.
     *   If the actor does not have multiple fire methods, return null.
     */
    @Override
    public String getDefaultFireMethodName() {
        if (numFireMethods() > 1) {
            return _defaultFireMethodName;
        } else {
            return null;
        }
    }

    /** Return the list of strings that represent the names of
     *  all the fire methods the actor has.
     *
     *  @return The list of fire method names strings.
     */
    @Override
    public List<String> getFireMethodNames() {
        return _methodList;
    }

    /** Return the list of input ports associated with the given method name.
     *  If the method has no input ports, return an empty list. If the actor
     *  does not have multiple methods, return null.
     *
     *  @param methodName The specified method name.
     *  @return The list of input ports associated with the method name.
     */
    @Override
    public List<IOPort> getMethodInputPortList(String methodName) {
        List<IOPort> result = null;

        if (numFireMethods() > 1) {
            result = _fireMethodNameToInputPortList.get(methodName);
            if (result == null) {
                result = new LinkedList<IOPort>();
            }
        }
        return result;
    }

    /** Return the output port associated with the given method name, if there is one.
     *  If the method does not have any outputs, or the actor does not have multiple
     *  fire methods, return null
     *
     *  @param methodName The specified name of the method.
     *  @return The output port associated with this method, or null is there is none.
     */
    @Override
    public IOPort getMethodOutputPort(String methodName) {
        if (numFireMethods() > 1) {
            for (Object outputPort : outputPortList()) {
                StringAttribute methodNameAttribute = (StringAttribute) ((IOPort) outputPort)
                        .getAttribute(methodName);
                if (methodNameAttribute != null) {
                    if (methodNameAttribute.getValueAsString().equals(
                            methodName)) {
                        return (IOPort) outputPort;
                    }
                }
            }
            return null;
        } else {
            return null;
        }
    }

    /** Return the number of fire methods the actor has.
     *  @return the number of fire methods the actor has, which should be
     *   at least one.
     */
    @Override
    public int numFireMethods() {
        return _methodList.size();
    }

    /** Set the fire method to the method that matches the specified
     *  string name.
     *  @param methodName The name of the method to be used.
     *  @exception IllegalActionException If the specified fire method cannot be found
     *   in the actor.
     */
    @Override
    public void setFireMethod(String methodName) throws IllegalActionException {
        if (!_methodList.contains(methodName)) {
            throw new IllegalActionException(this,
                    "Unrecognized fire method name: " + methodName
                    + " for actor " + getName() + ".");
        } else {
            _fireMethodName = methodName;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Add a fire method for this actor with the specified name.
     *  A fire method can optionally be associated with one output port and
     *  a list of input ports.  No two methods can be associated with the same
     *  input port or output port. And no two methods can have the same name.
     *
     *  @param methodName The name of the fire method to be added.
     *  @param outputPort The output port associated with this method, or null if
     *   there is none.
     *  @param inputPorts The list of input ports associated with this method, or null
     *   if there are none.
     *  @exception IllegalActionException If a the method specifies an output port or input
     *   ports already associated with another fire method.
     *  @exception NameDuplicationException If there is already another method with the same
     *   name specified for this actor.
     */
    protected void _addFireMethod(String methodName, IOPort outputPort,
            List<IOPort> inputPorts) throws IllegalActionException,
            NameDuplicationException {

        if (methodName == null) {
            throw new IllegalActionException(this,
                    "Cannot have a null method name.");
        } else if (_methodList.contains(methodName)) {
            throw new NameDuplicationException(this,
                    "Method name already exists for this actor.");
        } else {
            if (outputPort != null) {
                StringAttribute methodAttribute = (StringAttribute) outputPort
                        .getAttribute(methodName);
                if (methodAttribute != null) {
                    throw new IllegalActionException(this, "Output port "
                            + outputPort.getName()
                            + " already has a method name set: "
                            + methodAttribute.getValueAsString());
                } else {
                    methodAttribute = new StringAttribute(outputPort,
                            "methodName");
                    methodAttribute.setExpression(methodName);
                    methodAttribute.setVisibility(Settable.NOT_EDITABLE);
                }
            }
            if (inputPorts != null) {
                for (IOPort inputPort : inputPorts) {
                    Enumeration methodInputPortsIter = _fireMethodNameToInputPortList
                            .elements();
                    while (methodInputPortsIter.hasMoreElements()) {
                        List<IOPort> methodInputs = (List<IOPort>) methodInputPortsIter
                                .nextElement();
                        if (methodInputs.contains(inputPort)) {
                            throw new IllegalActionException(
                                    this,
                                    "Input port "
                                            + inputPort.getName()
                                            + " is already used by another method in this actor.");
                        }
                    }
                }
                _fireMethodNameToInputPortList.put(methodName, inputPorts);
            }
            _methodList.add(methodName);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The number for the default fire method. */
    protected String _defaultFireMethodName = null;

    /** The list of method names for the actor. */
    protected List<String> _methodList = null;

    /** The string name of the fire method currently being used. */
    protected String _fireMethodName = null;

    /** The hashtable that maps fire method names to their list of input ports. */
    protected Hashtable<String, List<IOPort>> _fireMethodNameToInputPortList = null;
}
