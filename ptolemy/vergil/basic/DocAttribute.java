/* An attribute for instance-specific documentation.

 Copyright (c) 2006-2014 The Regents of the University of California.
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
package ptolemy.vergil.basic;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.gui.style.TextStyle;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.SingletonAttribute;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// DocAttribute

/**
 An attribute containing documentation for a Ptolemy II object.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class DocAttribute extends SingletonAttribute {

    /** Construct a documentation attribute with the given name contained
     *  by the specified entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public DocAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        description = new StringAttribute(this, "description");
        TextStyle style = new TextStyle(description, "_style");
        style.height.setExpression("10");
        style.width.setExpression("70");

        author = new StringAttribute(this, "author");
        version = new StringAttribute(this, "version");
        since = new StringAttribute(this, "since");

        refreshParametersAndPorts();

        // Hide the name, if this gets instantiated in Vergil.
        SingletonParameter hide = new SingletonParameter(this, "_hideName");
        hide.setToken(BooleanToken.TRUE);
        hide.setVisibility(Settable.EXPERT);
    }

    /**
     * Construct an empty DocAttribute.
     */
    public DocAttribute() {
        super();
    }

    /**
     * Construct a DocAttribute in a given workspace.
     * @param workspace The workspace in which to construct the DocAttribute.
     */
    public DocAttribute(Workspace workspace) {
        super(workspace);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The author field. */
    public StringAttribute author;

    /** The description. */
    public StringAttribute description;

    /** The since field. */
    public StringAttribute since;

    /** The version field. */
    public StringAttribute version;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the documentation for the given parameter, or null if there
     *  none.
     *  @param name The name of the parameter.
     *  @return The documentation for the given parameter, or null if there
     *   is none.
     */
    public String getParameterDoc(String name) {
        Attribute parameterAttribute = getAttribute(name + " (parameter)");
        if (parameterAttribute instanceof Settable) {
            return ((Settable) parameterAttribute).getExpression();
        }
        // Might be a port-parameter.  Try that.
        parameterAttribute = getAttribute(name + " (port-parameter)");
        if (parameterAttribute instanceof Settable) {
            return ((Settable) parameterAttribute).getExpression();
        }
        return null;
    }

    /** Return the documentation for the given port, or null if there
     *  none.
     *  @param name The name of the port.
     *  @return The documentation for the given port, or null if there
     *   is none.
     */
    public String getPortDoc(String name) {
        StringAttribute portAttribute = (StringAttribute) getAttribute(name
                + " (port)");
        if (portAttribute != null) {
            return portAttribute.getExpression();
        }
        return null;
    }

    /** For each parameter and port in the container, create a
     *  parameter with the same name appended with either " (port)"
     *  or " (parameter)".  For parameters, only those that are
     *  settable are shown, and only if the visibility is "FULL".
     *  This method also removes any parameters that have no
     *  corresponding parameter or port in the container.
     *  Since this method modifies the model by adding or
     *  removing parameters to this attribute, it should be
     *  called from inside a change request.
     */
    public void refreshParametersAndPorts() {
        NamedObj container = getContainer();

        // Remove any parameters that are no longer relevant.
        // Operate on a copy of the attribute list to avoid
        // ConcurrentModificationException.
        List copy = new LinkedList(attributeList());
        Iterator parameters = copy.iterator();
        while (parameters.hasNext()) {
            Attribute attribute = (Attribute) parameters.next();
            String name = attribute.getName();
            int n = name.indexOf(" (parameter)");
            if (n >= 0) {
                // Attribute is a parameter.
                name = name.substring(0, n);
                if (container.getAttribute(name) == null) {
                    // Entry is obsolete.  Delete it.
                    try {
                        attribute.setContainer(null);
                    } catch (KernelException ex) {
                        // Should not occur.
                        throw new InternalErrorException(ex);
                    }
                }
            } else {
                n = name.indexOf(" (port-parameter)");
                if (n >= 0) {
                    // Attribute is a port parameter.
                    name = name.substring(0, n);
                    if (!(container instanceof Entity)
                            || ((Entity) container).getPort(name) == null) {
                        // Entry is obsolete.  Delete it.
                        try {
                            attribute.setContainer(null);
                        } catch (KernelException ex) {
                            // Should not occur.
                            throw new InternalErrorException(ex);
                        }
                    }
                } else {
                    n = name.indexOf(" (port)");
                    if (n >= 0) {
                        // Attribute is a port parameter.
                        name = name.substring(0, n);
                        if (!(container instanceof Entity)
                                || ((Entity) container).getPort(name) == null) {
                            // Entry is obsolete.  Delete it.
                            try {
                                attribute.setContainer(null);
                            } catch (KernelException ex) {
                                // Should not occur.
                                throw new InternalErrorException(ex);
                            }
                        }
                    }
                }
            }
        }

        parameters = container.attributeList(Settable.class).iterator();
        while (parameters.hasNext()) {
            NamedObj attribute = (NamedObj) parameters.next();
            if (((Settable) attribute).getVisibility() == Settable.FULL) {
                String modifier = " (parameter)";
                if (attribute instanceof PortParameter) {
                    modifier = " (port-parameter)";
                }
                String name = attribute.getName() + modifier;
                if (getAttribute(name) == null) {
                    try {
                        // FIXME: Using a StringParameter here is a really poor choice.
                        // It will try to parse anything with a dollar sign.
                        // But fixing this in a backward compatible way appears to be hard.
                        new StringParameter(this, name);
                    } catch (KernelException e) {
                        throw new InternalErrorException(e);
                    }
                }
            }
        }

        if (container instanceof Entity) {
            Iterator ports = ((Entity) container).portList().iterator();
            while (ports.hasNext()) {
                Port port = (Port) ports.next();
                if (port instanceof ParameterPort) {
                    // Skip this one.
                    continue;
                }
                String name = port.getName() + " (port)";
                if (getAttribute(name) == null) {
                    try {
                        new StringAttribute(this, name);
                    } catch (KernelException e) {
                        throw new InternalErrorException(e);
                    }
                }
            }
        }
    }
}
