/* An attribute that is used for invoking Chic from inside a ptolemy model.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.chic;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import chic.ChicForPtolemy;

///////////////////////////////////////////////////////////////////
//// ChicInvoker

/**
 This attribute is a visible attribute used for invoking Chic (Checker
 for Interface Compatibility) on its container. Chic is invoked by
 right clicking on the attribute and selecting one of the options:
 "CHIC: Asynchronous I/O" and "CHIC: Synchronous A/G".
 The expression of the InterfaceName attribute specifies the name of the
 ChicAttribute that is going to be used upon invocation of Chic.
 Note that every directly or indirectly contained port of the attribute's
 container must have a name syntactically equivalent to a Java identifier.

 @author Eleftherios Matsikoudis
 @version $Id$
 @since Ptolemy II 3.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class ChicInvoker extends Attribute {
    /** Construct an attribute with the specified container and name.
     *  @param container The container.
     *  @param name The name of the attribute.
     *  @exception IllegalActionException If the factory is not of an
     *   acceptable attribute for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public ChicInvoker(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-50\" y=\"-25\" " + "width=\"100\" height=\"50\" "
                + "style=\"fill:white\"/>\n"
                + "<image x=\"-50\" y=\"-25\" width=\"100\" height=\"50\" "
                + "xlink:href=\"ptolemy/chic/chic.gif\"/>\n" + "</svg>\n");

        new ChicControllerFactory(this, "_controllerFactory");

        SingletonParameter hide = new SingletonParameter(this, "_hideName");
        hide.setToken(BooleanToken.TRUE);
        hide.setVisibility(Settable.EXPERT);

        chicAttributeName = new StringAttribute(this, "InterfaceName");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** A StringAttribute representing the name of the ChicAttributes
     *  that are to be queried while collecting the interfaces upon which
     *  Chic is to be invoked.
     */
    public StringAttribute chicAttributeName;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Check the interface compatibility of all entities that are directly
     *  or indirectly contained by the container of this attribute.
     *  @param compiler The compiler of Chic to use.
     *  @see #ASYNCHRONOUS_IO
     *  @see #SYNCHRONOUS_AG
     *  @param silent True if the interface compatibility checking is to be
     *   carried out without invoking the user interface of Chic.
     *  @return True if the interfaces of the contained entities are
     *   compatible.
     *  @exception IllegalActionException If a port contained directly or
     *   indirectly by the <i>model</i> has width greater than one or is
     *   both an input and output port.
     *  @exception NameDuplicationException If the container of the attribute
     *   or one of the contained entities already contains an attribute of
     *   class other than ChicAttribute with the same name as the expression
     *   of the InterfaceName.
     */
    public boolean checkInterfaceCompatibility(int compiler, boolean silent)
            throws IllegalActionException, NameDuplicationException {
        return _checkInterfaceCompatibility(
                (CompositeActor) this.getContainer(), compiler, silent,
                chicAttributeName.getExpression());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Indicate use of the Asynchronous I/O compiler.
     */
    public static final int ASYNCHRONOUS_IO = ChicForPtolemy.ASYN;

    //    /** Indicate use of the Bidirectional Syn A/G compiler
    //     */
    //    public static final int BIDIRECTIONAL_SYN_AG = ChicForPtolemy.BISYNAG;
    //    /** Indiate use of the Stateful Software compiler
    //     */
    //    public static final int STATEFUL_SOFTWARE = ChicForPtolemy.SOFT_STATEFUL;
    //    /** Indicate use of the Stateless Software compiler
    //     */
    //    public static final int STATELESS_SOFTWARE = ChicForPtolemy.SOFT_STATELESS;

    /** Indicate use of the Synchronous A/G compiler.
     */
    public static final int SYNCHRONOUS_AG = ChicForPtolemy.SYNAG;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Check the interface compatibility of all entities that are directly
     *  or indirectly contained by the <i>model</i>.
     *  @param model The model to check interface compatibility.
     *  @param compiler The compiler of Chic to use.
     *  @see #ASYNCHRONOUS_IO
     *  @see #SYNCHRONOUS_AG
     *  @param silent True if the interface compatibility checking is to be
     *   carried out without invoking the user interface of Chic.
     *  @param name The name of the ChicAttribute.
     *  @return True if the interfaces of the contained entities are
     *   compatible.
     *  @exception IllegalActionException If a port contained directly or
     *   indirectly by the <i>model</i> has width greater than one or is
     *   both an input and output port.
     *  @exception NameDuplicationException If the <i>model</i> or one of the
     *   contained entities already contains an attribute of class other than
     *   ChicAttribute with the same name as <i>name</i>.
     */
    protected boolean _checkInterfaceCompatibility(CompositeActor model,
            int compiler, boolean silent, String name)
                    throws IllegalActionException, NameDuplicationException {
        ChicAttribute chicAttribute;
        String chicInterface;
        StringBuffer collectedInterfaces = new StringBuffer("");

        try {
            _workspace.getReadAccess();

            Iterator entities = model.entityList().iterator();

            // Collect the interfaces from all directly contained entities.
            while (entities.hasNext()) {
                ComponentEntity entity = (ComponentEntity) entities.next();

                // Get the interface of the entity.
                if (entity.isAtomic()) {
                    chicAttribute = (ChicAttribute) entity.getAttribute(name);
                } else {
                    if (_checkInterfaceCompatibility((CompositeActor) entity,
                            compiler, true, name)) {
                        chicAttribute = (ChicAttribute) entity
                                .getAttribute(name);
                    } else {
                        return false;
                    }
                }

                // If the entity does not have a declared interface skip it.
                if (chicAttribute == null) {
                    continue;
                }

                chicInterface = chicAttribute.getExpression();

                // Parse and refactor it.
                // Construct an iterator for the list of opaque ports
                // interfacing the entity.
                Iterator ports;

                if (entity.isOpaque()) {
                    ports = entity.portList().iterator();
                } else {
                    List insidePortList = new LinkedList();
                    Iterator transparentPorts = entity.portList().iterator();

                    while (transparentPorts.hasNext()) {
                        IOPort transparentPort = (IOPort) transparentPorts
                                .next();
                        insidePortList.addAll(transparentPort
                                .deepInsidePortList());
                    }

                    ports = insidePortList.iterator();
                }

                // Refactor the interface according to its type.
                switch (compiler) {
                case ASYNCHRONOUS_IO:

                    while (ports.hasNext()) {
                        IOPort port = (IOPort) ports.next();

                        // Currently ports with width greater than one are not supported.
                        if (port.getWidth() > 1) {
                            throw new IllegalActionException("Ports with "
                                    + "width greater than one are not "
                                    + "supported in the current "
                                    + "implementation.");
                        }

                        // Replace port_name.remote.action with
                        // remote_port_ull_name.action
                        if (entity.deepContains(port)) {
                            Iterator connectedPorts = port
                                    .deepConnectedPortList().iterator();

                            while (connectedPorts.hasNext()) {
                                IOPort connectedPort = (IOPort) connectedPorts
                                        .next();

                                if (entity.isOpaque()) {
                                    if (model.deepContains(connectedPort)) {
                                        chicInterface = chicInterface
                                                .replaceAll(
                                                        "(?<!(\\w|\\$|\\.))"
                                                                + port.getName()
                                                                + "\\.remote(?=\\.(\\w|\\$)+(\\s|$))",
                                                                (Actor) connectedPort
                                                                .getContainer() == model ? _sanitizeName(connectedPort
                                                                        .getName()
                                                                        + ".inside")
                                                                        : _sanitizeName(connectedPort
                                                                                .getFullName()));
                                    } else {
                                        chicInterface = chicInterface
                                                .replaceAll(
                                                        "(?<!(\\w|\\$|\\.))"
                                                                + port.getName()
                                                                + "\\.remote(?=\\.(\\w|\\$)+(\\s|$))",
                                                                _sanitizeName(port
                                                                        .getFullName()));
                                    }
                                } else {
                                    if (model.deepContains(connectedPort)) {
                                        chicInterface = chicInterface
                                                .replaceAll(
                                                        "(?<!(\\w|\\$|\\.))"
                                                                + port.getFullName()
                                                                + "\\.remote(?=\\.(\\w|\\$)+(\\s|$))",
                                                                (Actor) connectedPort
                                                                .getContainer() == model ? _sanitizeName(connectedPort
                                                                        .getName()
                                                                        + ".inside")
                                                                        : _sanitizeName(connectedPort
                                                                                .getFullName()));
                                    }
                                }
                            }

                            // Replace port_name.action with port_full_name.action
                            chicInterface = chicInterface.replaceAll(
                                    "(?<!(\\w|\\$|\\.))" + port.getName()
                                    + "(?=\\.(\\w|\\$)+(\\s|$))",
                                    _sanitizeName(port.getFullName()));
                        }
                    }

                    break;

                case SYNCHRONOUS_AG:

                    while (ports.hasNext()) {
                        IOPort port = (IOPort) ports.next();

                        // Currently ports with width greater than one and
                        // ports that are both input and output ports are
                        // not supported.
                        if (port.isInput() && port.isOutput()
                                || port.getWidth() > 1) {
                            throw new IllegalActionException(
                                    "Ports with width greater than one or "
                                            + "that are both input and output are not "
                                            + "supported in the current "
                                            + "implementation.");
                        }

                        if (entity.deepContains(port)) {
                            if (entity.isOpaque()) {
                                if (port.isInput()) {
                                    Iterator sourcePorts = port
                                            .sourcePortList().iterator();

                                    if (sourcePorts.hasNext()) {
                                        // Since its width is less than one
                                        // there is only one port on the
                                        // outside.
                                        IOPort sourcePort = (IOPort) sourcePorts
                                                .next();

                                        if (model.deepContains(sourcePort)) {
                                            chicInterface = chicInterface
                                                    .replaceAll(
                                                            "(?<!(^|[\\n\\r]|\\w|\\$|\\.))"
                                                                    + port.getName()
                                                                    + "(?!(\\w|\\$|\\.))",
                                                                    (Actor) sourcePort
                                                                    .getContainer() == model ? _sanitizeName(sourcePort
                                                                            .getName()
                                                                            + ".inside")
                                                                            : _sanitizeName(sourcePort
                                                                                    .getFullName()));
                                        } else {
                                            chicInterface = chicInterface
                                                    .replaceAll(
                                                            "(?<!(^|[\\n\\r]|\\w|\\$|\\.))"
                                                                    + port.getName()
                                                                    + "(?!(\\w|\\$|\\.))",
                                                                    _sanitizeName(port
                                                                            .getFullName()));
                                        }
                                    } else {
                                        chicInterface = chicInterface
                                                .replaceAll(
                                                        "(?<!(^|[\\n\\r]|\\w|\\$|\\.))"
                                                                + port.getName()
                                                                + "(?!(\\w|\\$|\\.))",
                                                                _sanitizeName(port
                                                                        .getFullName()));
                                    }
                                } else {
                                    chicInterface = chicInterface.replaceAll(
                                            "(?<!(^|[\\n\\r]|\\w|\\$|\\.))"
                                                    + port.getName()
                                                    + "(?!(\\w|\\$|\\.))",
                                                    _sanitizeName(port.getFullName()));
                                }
                            } else {
                                if (port.isInput()) {
                                    Iterator sourcePorts = port
                                            .sourcePortList().iterator();

                                    if (sourcePorts.hasNext()) {
                                        // Since its width is less than one
                                        // there is only one port on the
                                        // outside.
                                        IOPort sourcePort = (IOPort) sourcePorts
                                                .next();

                                        if (model.deepContains(sourcePort)) {
                                            chicInterface = chicInterface
                                                    .replaceAll(
                                                            "(?<!(^|[\\n\\r]|\\w|\\$|\\.))"
                                                                    + port.getFullName()
                                                                    + "(?!(\\w|\\$|\\.))",
                                                                    (Actor) sourcePort
                                                                    .getContainer() == model ? _sanitizeName(sourcePort
                                                                            .getName()
                                                                            + ".inside")
                                                                            : _sanitizeName(sourcePort
                                                                                    .getFullName()));
                                        }
                                    }
                                }
                            }
                        }
                    }

                    break;

                default:
                    throw new IllegalActionException(
                            "Only \"Asynchronous I/O\" and \"Synchronous A/G\" "
                                    + "are supported in current implementation.");
                }

                // Append the refactored interface to the collected interfaces.
                collectedInterfaces.append(chicInterface + "\n");
            }

            // Collect the interfaces from all directly contained opaque ports.
            if (model.isOpaque()) {
                Iterator ports = model.portList().iterator();

                while (ports.hasNext()) {
                    IOPort port = (IOPort) ports.next();

                    // Get the interface of the port.
                    chicAttribute = (ChicAttribute) port.getAttribute(name);

                    // If the port does not have a declared interface skip it.
                    if (chicAttribute == null) {
                        continue;
                    }

                    chicInterface = chicAttribute.getExpression();

                    // Parse and refactor it.
                    switch (compiler) {
                    case ASYNCHRONOUS_IO:

                        // Currently ports with width greater than one are not supported.
                        if (port.getWidth() > 1) {
                            throw new IllegalActionException("Ports with "
                                    + "width greater than one are not "
                                    + "supported in the current "
                                    + "implementation.");
                        }
                        {
                            Iterator insidePorts = port.deepInsidePortList()
                                    .iterator();

                            while (insidePorts.hasNext()) {
                                IOPort insidePort = (IOPort) insidePorts.next();
                                chicInterface = chicInterface
                                        .replaceAll(
                                                "(?<!(\\w|\\$|\\.))"
                                                        + port.getName()
                                                        + "\\.inside\\.remote(?=\\.(\\w|\\$)+(\\s|$))",
                                                        (Actor) insidePort
                                                        .getContainer() == model ? _sanitizeName(insidePort
                                                                .getName() + ".inside")
                                                                : _sanitizeName(insidePort
                                                                        .getFullName()));
                            }
                        }

                        break;

                    case SYNCHRONOUS_AG:

                        // Currently ports with width greater than one and
                        // ports that are both input and output ports are
                        // not supported.
                        if (port.isInput() && port.isOutput()
                                || port.getWidthInside() > 1) {
                            throw new IllegalActionException(
                                    "Ports with width greater than one or "
                                            + "that are both input and output are not "
                                            + "supported in the current "
                                            + "implementation.");
                        }

                        if (port.isOutput()) {
                            Iterator insidePorts = port.deepInsidePortList()
                                    .iterator();

                            if (insidePorts.hasNext()) {
                                // Since it is not a multiport or an input port,
                                // there is only one port on the inside.
                                IOPort sourcePort = (IOPort) insidePorts.next();
                                chicInterface = chicInterface
                                        .replaceAll(
                                                "(?<!(^|[\\n\\r]|\\w|\\$|\\.))"
                                                        + port.getName()
                                                        + ".inside"
                                                        + "(?!(\\w|\\$|\\.))",
                                                        (Actor) sourcePort
                                                        .getContainer() == model ? _sanitizeName(sourcePort
                                                                .getName() + ".inside")
                                                                : _sanitizeName(sourcePort
                                                                        .getFullName()));
                            }
                        }

                        break;

                    default:
                        throw new IllegalActionException(
                                "Only \"Asynchronous I/O\" and \"Synchronous A/G\" "
                                        + "are supported in current implementation.");
                    }

                    // Append the refactored interface to the collected
                    // interfaces.
                    collectedInterfaces.append(chicInterface + "\n");
                }
            }
        } finally {
            _workspace.doneReading();
        }

        if (!(chicInterface = new String(collectedInterfaces)).equals("")) {
            System.out.println(chicInterface + "\n");

            ChicForPtolemy chic = new ChicForPtolemy(new String(
                    collectedInterfaces), compiler, !silent);

            if (chic.areCompatible()) {
                chicAttribute = new ChicAttribute(model, name);
                chicAttribute.setExpression(chic.getCompositeInterface());
                return true;
            } else {
                return false;
            }
        }

        return true;

        //        if (!(chicInterface = new String(collectedInterfaces)).equals("")) {
        //            chicAttribute = new ChicAttribute(model, name);
        //            chicAttribute.setExpression(chicInterface);
        //        }
        //        if (!silent) System.out.println(chicInterface + "\n");
        //        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // Sanitize a String so that it is syntactically equivallent to
    // a (qualified) Java identifier. Characters that are not permitted
    // in a (qualified) Java identifier are changed to underscores.
    // This method does not check that the returned string is a
    // keyword or literal.
    // Note that two different strings can sanitize to the same
    // string.
    private String _sanitizeName(String name) {
        name = name.replaceAll("[^(\\w|\\$|\\.)]", "_");

        // Substitute all $ with \\$ so that you don't get an exception
        // from the matcher.
        return name.replaceAll("\\$", "\\\\\\$");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
}
