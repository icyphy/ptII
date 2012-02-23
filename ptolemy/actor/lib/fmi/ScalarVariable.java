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
        name = element.getAttribute("name");
        description = element.getAttribute("description");
        // FIXME: alias, causuality etc.

        if (element.hasAttribute("alias")) {
            String attribute = element.getAttribute("alias");
            if (attribute.equals("alias")) {
                alias = Alias.alias;
            } else if (attribute.equals("negatedAlias")) {
                alias = Alias.negatedAlias;
            } else if (attribute.equals("noAlias")) {
                alias = Alias.noAlias;
            } else {
                throw new IllegalActionException("alias \"" + alias
                        + "\" must be one of alias, negatedAlias or noAlias"
                        + " in " + name + ", " + description); 
            }
        }

        if (element.hasAttribute("causality")) {
            String attribute = element.getAttribute("casuality");
            if (attribute.equals("input")) {
                causality = Causality.input;
            } else if (attribute.equals("internal")) {
                causality = Causality.internal;
            } else if (attribute.equals("output")) {
                causality = Causality.output;
            } else if (attribute.equals("none")) {
                causality = Causality.none;
            } else {
                throw new IllegalActionException("causality \"" + causality
                        + "\" must be one of input, internal, output or, none"
                        + " in " + name + ", " + description); 
            }
        }

        if (element.hasAttribute("variability")) {
            String attribute = element.getAttribute("variability");
            if (attribute.equals("constant")) {
                variability = Variability.constant;
            } else if (attribute.equals("continuous")) {
                variability = Variability.continuous;
            } else if (attribute.equals("discrete")) {
                variability = Variability.discrete;
            } else if (attribute.equals("parameter")) {
                variability = Variability.parameter;
            } else {
                throw new IllegalActionException("variability \"" + variability
                        + "\" must be one of constant, continuous, discrete or parameter "
                        + " in " + name + ", " + description); 
            }
        }


        NodeList children = element.getChildNodes();  // NodeList. Worst. Ever.
        for (int i = 0; i < children.getLength(); i ++) {
            Node child = element.getChildNodes().item(i);
            if (child instanceof Element) {
                Element childElement = (Element) child;
                if (childElement.getNodeName().equals("Real")) {
                    type = new FMURealType(name, description, childElement);
                } else {
                    throw new InternalErrorException("Child element " + element 
                            + " not implemented yet.");
                }
            }
        }
    }

    public enum Alias {alias, negatedAlias, noAlias};
    
    public enum Causality {input, internal, output, none}

    public enum Variability {constant, continuous, discrete, parameter}


    public Alias alias;

    public Causality causality;

    public String description;

    public String name;

    public FMUType type; 

    public Variability variability;
}
