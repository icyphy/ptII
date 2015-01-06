/* A Function Mock-up Interface Co-Simulation Capabilities object.

   Copyright (c) 2012-2014 The Regents of the University of California.
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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.w3c.dom.Element;

///////////////////////////////////////////////////////////////////
//// FMICoSimulationCapbilities

/**
 * An object that represents the the capabilities of a FMI co-simulation
 * slave.
 *
 * <p>
 * A Functional Mock-up Unit file is a .fmu file in zip format that
 * contains a .xml file named "modelDescription.xml".  In FMI-1.0, the xml
 * file may optionally contain a "Implementation" element that will contain
 * either a "CoSimulation_Standalone" element or a "CoSimulation_Tool"
 * element.  Those two elements will contain a "Capabilities" element
 * that has attributes that define the capabilities of the slave.
 * This class has public fields that correspond to the attributes of
 * the "Capabilities" element.  The name of this class is taken from
 * the FMI specification.</p>
 *
 * <p>FMI documentation may be found at
 * <a href="http://www.modelisar.com/fmi.html">http://www.modelisar.com/fmi.html</a>.
 * </p>
 *
 * @author Christopher Brooks
@version $Id$
@since Ptolemy II 10.0
 * @version $Id$
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class FMICapabilities {

    /** Create an empty Capability. */
    public FMICapabilities() {
    }

    /** Create a FMICoSimulationCapability from an XML Element.
     *  @param element The XML Element that contains attributes.
     */
    public FMICapabilities(Element element) {
        // We use reflection here so that if Capabilities attributes change,
        // and new fields are added, we don't have to update this method.
        Field fields[] = getClass().getFields();
        for (Field field : fields) {

            // Get the public fields that are attributes with the same name.
            if ((field.getModifiers() & Modifier.PUBLIC) == Modifier.PUBLIC
                    && element.hasAttribute(field.getName())) {
                try {

                    // The field is a primitive boolean, not a Boolean.
                    if (field.getType().equals(Boolean.TYPE)) {
                        boolean value = Boolean.valueOf(element
                                .getAttribute(field.getName()));
                        field.setBoolean(this, value);
                    } else if (field.getType().equals(Integer.TYPE)) {
                        int value = Integer.parseInt(element.getAttribute(field
                                .getName()));
                        field.setInt(this, value);
                    }
                } catch (IllegalAccessException ex) {
                    throw new RuntimeException("Failed to set the "
                            + field.getName() + " field to "
                            + element.getAttribute(field.getName()) + ".", ex);
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the value of a boolean field.
     *  @param fieldName The name of the boolean field.
     *  @return True if the value of the field was true.
     *  @exception IllegalArgumentException If the field is not found
     *  or it is not a boolean.
     */
    public boolean getBoolean(String fieldName) throws IllegalArgumentException {
        try {
            Field field = getClass().getField(fieldName);
            return field.getBoolean(this);
        } catch (Throwable throwable) {
            throw new IllegalArgumentException("Could not find field \""
                    + fieldName + "\" in " + this + ".", throwable);
        }
    }

    /** Return a description of the fields that are true or
     *  non-zero.
     *  @return The true or non-zero fields
     */
    @Override
    public String toString() {
        // We use reflection here so that if Capabilities attributes change,
        // and new fields are added, we don't have to update this method.
        StringBuffer results = new StringBuffer();
        Field fields[] = getClass().getFields();
        for (Field field : fields) {

            // Get the public fields.
            if ((field.getModifiers() & Modifier.PUBLIC) == Modifier.PUBLIC) {
                String valueString = "";
                try {
                    // The field is a primitive boolean, not a Boolean
                    if (field.getType().equals(Boolean.TYPE)) {
                        if (field.getBoolean(this)) {
                            valueString = "true";
                        }
                    } else if (field.getType().equals(Integer.TYPE)) {
                        int value = field.getInt(this);
                        if (value != 0) {
                            valueString = Integer.toString(value);
                        }
                    }
                } catch (IllegalAccessException ex) {
                    throw new RuntimeException("Failed to get the " + field
                            + " field", ex);
                }

                // Optionally append a comma.
                if (valueString.length() > 0) {
                    if (results.length() > 0) {
                        results.append(", ");
                    }
                    results.append(field.getName() + " = " + valueString);
                }
            }
        }
        // We attempt to return a record in the Ptolemy format.
        return "{" + results.toString() + "}";
    }
}
