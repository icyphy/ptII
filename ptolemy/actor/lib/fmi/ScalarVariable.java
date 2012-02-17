/* An FMU ScalarVariable.

 Copyright (c) 2012 The Regents of the University of California.
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
package ptolemy.actor.lib.fmi;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// ScalarVariable

/**
 * An object that represents the ScalarVariable element of a
 * Functional Mock-up Interface .fmu XML file.
 * 
 * <p>FMI documentation may be found at
 * <a href="http://www.modelisar.com/fmi.html">http://www.modelisar.com/fmi.html</a>.
 * </p>
 * 
 * @author Christopher Brooks
 * @version $Id$
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class ScalarVariable {

    /** Create a ScalarVariable. */
    public ScalarVariable() {
    }

    public ScalarVariable(TypedCompositeActor container, Element element) 
            throws IllegalActionException, NameDuplicationException {
        _name = element.getAttribute("name");
        _description = element.getAttribute("description");
        // FIXME: alias, variability, causuality etc.

        boolean foundParameter = false;
        if (element.hasAttribute("variability")) {
            String variability = element.getAttribute("variability");
            
            // FIXME: Other types of variability
            if (variability.equals("parameter")) {
                foundParameter = true;
            }
        }

        FMUType type = null;
        NodeList children = element.getChildNodes();  // NodeList. Worst. Ever.
        for (int i = 0; i < children.getLength(); i ++) {
            Node child = element.getChildNodes().item(i);
            if (child instanceof Element) {
                Element childElement = (Element) child;
                if (childElement.getNodeName().equals("Real")) {
                    type = new FMURealType(_name, _description, childElement);
                    // We could use FMURealType.start here

                    if (foundParameter) {
                        Parameter parameter = new Parameter(container, _name);
                        parameter.setExpression(childElement.getAttribute("start"));
                        // Prevent exporting this to MoML unless it has
                        // been overridden.
                        parameter.setDerivedLevel(1);
                    } {
                        // FIXME: All output ports?
                        TypedIOPort port = new TypedIOPort(container, _name, false, true);
                        port.setDerivedLevel(1);
                    }
                } else {
                    throw new InternalErrorException("Child element " + element 
                            + " not implemented yet.");
                }
            }
        }
    }

    /** Get the description.
     *  @return The description, which is typically documentation.
     *  @see #setDescription(String)
     */
    public String getDescription() {
        return _description;
    }

    /** Get the name.
     *  @return The name
     *  @see #setName(String)
     */
    public String getName() {
        return _name;
    }

    // FIXME: valueReference getters and setters? And others?

    /** Set the description.
     *  @param description the new description, which is typically
     *  documentation.
     *  @see #getDescription()
     */
    public void setDescription(String description) {
        _description = description;
    }


    /** Set the name.
     *  @param name the new fmi name.
     *  @see #getName()
     */
    public void setName(String name) {
        _name = name;
    }

    private String _description;

    private String _name;
}
