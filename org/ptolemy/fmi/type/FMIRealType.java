/* An Functional Mock-up Interface Real Type.

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
package org.ptolemy.fmi.type;

import org.w3c.dom.Element;

///////////////////////////////////////////////////////////////////
//// FMIRealType

/**
 * An Functional Mock-up Interface type that represents a Real.
 *
 * <p>A Functional Mock-up Unit file is a .fmu file in zip format that
 * contains a .xml file named "modelDescription.xml".  In that file,
 * the ModelVariables element may contain elements such as
 * ScalarVariable that in turn may contain elements like Real.  This
 * class represents the Real type.</p>
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
public class FMIRealType extends FMIType {
    /** Construct a Real FMU variable.
     *  @param name The name of this variable.
     *  @param description A description of this variable.
     *  @param element The XML element whose attributes are used to
     *  set the fields of this object.
     */
    public FMIRealType(String name, String description, Element element) {
        super(name, description, element);
        if (element.hasAttribute("start")) {
            start = Double.valueOf(element.getAttribute("start"));
        } else {
            // If there is no start attribute, then set the value to NaN.
            start = Double.NaN;
        }
        if (element.hasAttribute("nominal")) {
            nominal = Double.valueOf(element.getAttribute("nominal"));
        } else {
            // If there is no nominal attribute, then set the default value of 1.0.
            // See page 90 of the FMI RC1 specification.
            nominal = 1.0;
        }
        // The derivative is an attribute of element "Real"
        if (element.hasAttribute("derivative")) {
            String derivative = element.getAttribute("derivative");
            try {
                indexState = Integer.parseInt(derivative);
            } catch (NumberFormatException ex) {
                throw new NumberFormatException(
                        "Failed to parse derivative index " + derivative
                                + " of " + name);
            }
        } else {
            indexState = -1;
        }
    }

    /** Return the string value of the base element.
     *  @return The string value.  If the element does not have a start
     *  element, then the string "NaN" is returned.
     */
    @Override
    public String toString() {
        if (start == null) {
            // Dymola had a system.p_start parameter that had no start value.
            return "NaN";
        }
        return Double.toString(start);
    }

    // FIXME: need more documentation and to describe other variables.

    /** The state variable index that is set to the value of the "derivative" element. */
    public int indexState;

    /** The starting value of this real. */
    public Double start;

    /** The nominal value of this real. */
    public Double nominal;
}
