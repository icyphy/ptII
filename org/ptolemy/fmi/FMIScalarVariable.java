/* An Functional Mock-up Interface ScalarVariable.

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
package org.ptolemy.fmi;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

///////////////////////////////////////////////////////////////////
//// ScalarVariable

/**
 * An object that represents the ScalarVariable element of a
 * Functional Mock-up Interface .fmu XML file.
 * 
 * <p>A Functional Mock-up Unit file is a .fmu file in zip format that
 * contains a .xml file named "modelDescription.xml".  In that file,
 * the ModelVariables element may contain elements such as
 * ScalarVariable.</p>
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
public class FMIScalarVariable {

    /** Create an empty ScalarVariable. */
    public FMIScalarVariable() {
    }

    /** Create a ScalarVariable from an XML Element.
     *  @param element The XML Element that contains attributes.
     */
    public FMIScalarVariable(Element element) {
        name = element.getAttribute("name");
        description = element.getAttribute("description");

        alias = Alias.noAlias;
        if (element.hasAttribute("alias")) {
            String attribute = element.getAttribute("alias");
            if (attribute.equals("alias")) {
                alias = Alias.alias;
            } else if (attribute.equals("negatedAlias")) {
                // In bouncingBall, 'g' has a negatedAlias.
                alias = Alias.negatedAlias;
            } else if (attribute.equals("noAlias")) {
                // FIXME: I'm not sure if alias="noAlias" ever appears
                alias = Alias.noAlias;
            } else {
                throw new IllegalArgumentException("alias \"" + attribute
                        + "\" must be one of alias, negatedAlias or noAlias"
                        + " in " + name + ", " + description); 
            }
        }

        causality = Causality.none;
        if (element.hasAttribute("causality")) {
            String attribute = element.getAttribute("causality");
            if (attribute.equals("input")) {
                causality = Causality.input;
            } else if (attribute.equals("internal")) {
                causality = Causality.internal;
            } else if (attribute.equals("output")) {
                causality = Causality.output;
            } else if (attribute.equals("none")) {
                causality = Causality.none;
            } else {
                throw new IllegalArgumentException("causality \"" + attribute
                        + "\" must be one of input, internal, output or none"
                        + " in " + name + ", " + description); 
            }
        }

        if (element.hasAttribute("valueReference")) {
            valueReference = Integer.valueOf(element.getAttribute("valueReference"));
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
                throw new IllegalArgumentException("variability \"" + attribute
                        + "\" must be one of constant, continuous, discrete or parameter "
                        + " in " + name + ", " + description); 
            }
        }


        NodeList children = element.getChildNodes();  // NodeList. Worst. Ever.
        for (int i = 0; i < children.getLength(); i ++) {
            Node child = element.getChildNodes().item(i);
            if (child instanceof Element) {
                Element childElement = (Element) child;
                if (childElement.getNodeName().equals("Boolean")) {
                    type = new FMIBooleanType(name, description, childElement);
                } else if (childElement.getNodeName().equals("Enumeration")) {
                    type = new FMIIntegerType(name, description, childElement);
                } else if (childElement.getNodeName().equals("Integer")) {
                    type = new FMIIntegerType(name, description, childElement);
                } else if (childElement.getNodeName().equals("Real")) {
                    type = new FMIRealType(name, description, childElement);
                } else if (childElement.getNodeName().equals("String")) {
                    type = new FMIStringType(name, description, childElement);
                } else {
                    throw new IllegalArgumentException(element + ": Child element \""
                            + childElement.getNodeName()
                            + "\" not implemented yet.");
                }
            }
        }
    }

    /** Acceptable values for the alias xml attribute.
     *  Alias variables occur during assignment operations.   
     */
    public enum Alias {
        /** This is an alias variable, use the valueReference handle
         * to set or get the actual value.
         */
        alias,
        /** This is an alias value, but the value returned must be
         * negated.
         */
        negatedAlias,
        /** This is not an alias (the default). */
        noAlias
    };
    
    /** Acceptable values for the causality xml attribute.
     *  Causality defines the visibility of the variable from outside of the model.
     */
    public enum Causality {
        /** The value is defined from the outside.  The value is
         * initially the value of the start attribute.
         */
        input,
        /** After initialization, a result may be stored.
         *  However, the value cannot be used in a connection.   
         *  The default Causality is "internal".
         */
        internal,
        /** The value can be read from the outside with a connection.
         */
        output,
        /** The value does not affect computation.  Typically, "none"
         *  values are tool specific and used to enable logging.
         */
        none}

    /** Acceptable values for the variability xml attribute.
     *  The variablity attribute defines when a value changes,
     *  which determines when the value should be read. 
     */
    public enum Variability {
        /** The value does not change.
         */
        constant,
        /** The value changes at any moment.  A continuous
         *  variable must be of type Real.
         *  The default Variability is "continuous".
         */
        continuous,
        /** The value only changes during initialization
         *  and at event instances.   
         */
        discrete,
        /** The value does not change after initailization.
         */
        parameter}


    /** The value of the alias xml attribute. */
    public Alias alias;

    /** The value of the causality xml attribute. */
    public Causality causality;

    /** The value of the description xml attribute. */
    public String description;

    /** The value of the name xml attribute. */
    public String name;

    /** The value of the type xml attribute. */
    public FMIType type; 

    /** The value of the valueReference xml attribute. */
    public int valueReference;

    /** The value of the variability xml attribute. */
    public Variability variability;
}
