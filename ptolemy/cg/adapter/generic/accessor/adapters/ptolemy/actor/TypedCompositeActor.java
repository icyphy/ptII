/* Code generator adapter for typed composite actor.

 Copyright (c) 2005-2016 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.accessor.adapters.ptolemy.actor;

import java.util.Iterator;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.cg.kernel.generic.accessor.AccessorCodeGeneratorAdapter;
import ptolemy.data.Token;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.util.StringUtilities;

//////////////////////////////////////////////////////////////////////////
//// TypedCompositeActor

/**
 * JavaScript Code generator adapter for typed composite actor.
 *
 * @author Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class TypedCompositeActor extends AccessorCodeGeneratorAdapter {

    /** Construct the code generator adapter associated
     *  with the given TypedCompositeActor.
     *  @param component The associated component.
     */
    public TypedCompositeActor(ptolemy.actor.TypedCompositeActor component) {
        super(component);
    }

    /** Generate Accessor code.
     *  @return The generated Accessor.
     *  @exception IllegalActionException If there is a problem getting the adapter, getting
     *  the director or generating Accessor for the director.
     */
    @Override
    public String generateAccessor() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(_eol + _INDENT1 + "// Ports: " + getComponent().getName()
                + ": ptolemy/cg/adapter/generic/accessor/adapters/ptolemy/actor/TypedCompositeActor.java"
                + _eol);

        // Generate Accessor for the ports.
        code.append(_generatePorts(
                ((CompositeActor) getComponent()).inputPortList()));
        code.append(_generatePorts(
                ((CompositeActor) getComponent()).outputPortList()));

        // Generate Accessor for the JSAccessor actor.
        Iterator<?> actors = ((CompositeActor) getComponent()).entityList()
                .iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            String actorClassName = actor.getClass().getName();
            // We could check to see if the class is a JavaScript actor here.
            if (!actorClassName.equals("org.terraswarm.accessor.JSAccessor")
                    && !actorClassName
                            .equals("ptolemy.actor.lib.jjs.JavaScript")) {
                throw new IllegalActionException(actor,
                        "The Accessor code generator only works on JavaScript actors, JSAccessor actors and TypedCompositeActors, the name of the class was:"
                                + actorClassName);
            }

            AccessorCodeGeneratorAdapter adapter = null;
            Object object = getCodeGenerator().getAdapter(actor);
            try {
                adapter = (AccessorCodeGeneratorAdapter) object;
            } catch (ClassCastException ex) {
                throw new IllegalActionException(getComponent(), ex,
                        "Failed to cast " + object + " of class "
                                + object.getClass().getName() + " to "
                                + AccessorCodeGeneratorAdapter.class.getName()
                                + ".");

            }
            code.append(adapter.generateAccessor());
        }

        code.append(_eol + _INDENT1 + "// Connections: "
                + getComponent().getName()
                + ": ptolemy/cg/adapter/generic/accessor/adapters/ptolemy/actor/TypedCompositeActor.java"
                + _eol);

        // Generate Accessor for the toplevel input ports to actors or other ports.
        List<Port> inputPorts = ((CompositeActor) getComponent())
                .inputPortList();
        for (Port port : inputPorts) {
            if (port instanceof IOPort) {
                List<IOPort> connectedPorts = ((IOPort) port)
                        .insideSinkPortList();
                for (IOPort connectedPort : connectedPorts) {
                    IOPort ioPort = (IOPort) port;
                    code.append(_INDENT1 + "this.connect('" + ioPort.getName());
                    if (connectedPort.getContainer() == port.getContainer()) {
                        // Port to port connection?
                        code.append(", " + connectedPort.getName());
                    } else {
                        // Port to contained Actor connection
                        code.append("', "
                                + StringUtilities.sanitizeName(
                                        connectedPort.getContainer().getName())
                                + ", '" + connectedPort.getName());
                    }
                    code.append("');" + _eol);
                }
            } else {
                code.append(_INDENT1 + "// port " + port.getName()
                        + " is not an IOPort." + _eol);
            }
        }

        // Generate Accessor for the toplevel output ports to actors or other ports.
        List<Port> outputPorts = ((CompositeActor) getComponent())
                .outputPortList();
        for (Port port : outputPorts) {
            if (port instanceof IOPort) {
                List<IOPort> connectedPorts = ((IOPort) port)
                        .insideSourcePortList();
                for (IOPort connectedPort : connectedPorts) {
                    IOPort ioPort = (IOPort) port;
                    if (connectedPort.getContainer() != port.getContainer()) {
                        // Port to Port connections would have been handled with the input ports.
                        // Port to contained Actor connection.
                        code.append(_INDENT1 + "this.connect("
                                + StringUtilities.sanitizeName(
                                        connectedPort.getContainer().getName())
                                + ", '" + connectedPort.getName() + "', '"
                                + ioPort.getName() + "');" + _eol);
                    }
                }
            } else {
                code.append(_INDENT1 + "// port " + port.getName()
                        + " is not an IOPort." + _eol);
            }
        }

        // Generate Accessor for the toplevel actor to actor connections.
        actors = ((CompositeActor) getComponent()).entityList().iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            List<IOPort> inputs = actor.inputPortList();
            for (IOPort inputPort : inputs) {
                List<IOPort> sourcePorts = inputPort.sourcePortList();
                for (IOPort sourcePort : sourcePorts) {
                    // Skip ports at the toplevel.
                    if (sourcePort.getContainer() != getComponent()) {
                        code.append(_INDENT1 + "this.connect("
                                + StringUtilities.sanitizeName(
                                        sourcePort.getContainer().getName())
                                + ", '" + sourcePort.getName() + "', "
                                + StringUtilities.sanitizeName(
                                        inputPort.getContainer().getName())
                                + ", '" + inputPort.getName() + "');" + _eol);
                    }
                }
            }
        }

        return /*processCode(code.toString())*/code.toString();
    }

    /** Generate the JavaScript initialize for the ports.
     *  @param ports The ports.
     *  @return The JavaScript initialization code for the ports.
     *  @exception IllegalActionException If thrown while reading the
     *  value of the defaultValue parameter.
     */
    private StringBuffer _generatePorts(List<TypedIOPort> ports)
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        for (TypedIOPort port : ports) {
            String inputOrOutput = port.isInput() ? "input"
                    : port.isOutput() ? "output" : "neitherInputNorOutput";
            code.append(_INDENT1 + "this." + inputOrOutput + "('"
                    + port.getName() + "'");
            String targetType = targetType(port.getType());
            if (!targetType.equals("unknown")) {
                code.append(", {'type' : '" + targetType + "'");
                if (port instanceof ParameterPort) {
                    code.append(", 'value':" + targetExpression(
                            ((ParameterPort) port).getParameter()));
                } else if (port instanceof IOPort) {
                    IOPort ioPort = port;
                    Token value = ioPort.defaultValue.getToken();
                    if (value != null) {
                        code.append(", 'value':"
                                + targetExpression(ioPort.defaultValue));
                    }
                }
                code.append("}");
            }
            code.append(");");
            if (targetType.equals("unknown")) {
                code.append(" // Type was " + port.getType() + ".");
            }
            code.append(_eol);
        }
        return code;
    }
}
