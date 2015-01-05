/* An Functional Mock-up Interface Derivative.

 Copyright (c) 2012-2013 The Regents of the University of California.
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

import java.util.LinkedList;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

///////////////////////////////////////////////////////////////////
//// FMI20ContinuousStateDerivative

/**
 * An object that represents the Output of an FMU 
 *
 * <p>A Functional Mock-up Unit file is a .fmu file in zip format that
 * contains a .xml file named "modelDescription.xml". In that file,
 * the Derivatives element may contain elements such as
 * Unknown.</p>
 *
 * <p>FMI documentation may be found at
 * <a href="http://www.modelisar.com/fmi.html">http://www.modelisar.com/fmi.html</a>.
 * </p>
 *
 * @author Thierry S. Nouidui
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class FMI20Output {

    /** Create an empty Output. */
    public FMI20Output() {
    }

    /** Create an Output variable element from an XML Element.
     *  @param fmiModelDescription the Model Description for this derivative.
     *  @param element The XML Node that contains attributes.
     */
    public FMI20Output(FMIModelDescription fmiModelDescription, Node element) {
        String indexAttr;
        // Get the index of the output
        indexAttr = ((Element) element).getAttribute("index");
        try {
            index = Integer.parseInt(indexAttr);
        } catch (NumberFormatException ex) {
            throw new NumberFormatException(
                    "Failed to parse output variable index " + indexAttr);
        }
        if (index < 1) {
            throw new IllegalArgumentException("The index for the node \"" + element
                    + "\" was " + index + ", which is less than 1.  The index must be greater than or equal to 1, see p. 61 of the FMI-2.0 spec.");
        }
        // Get the corresponding scalar variable.
        scalarVariable = fmiModelDescription.modelVariables.get(index - 1);
        // Build the list of dependent variables.
        dependentScalarVariables = new LinkedList<FMIScalarVariable>();
        String[] dependencies = (((Element) element)
                .getAttribute("dependencies")).split(" ");
        for (int i = 0; i < dependencies.length; i++) {
            // Convert the dependency index into an integer and retrieve the corresponding ScalarVariable.
            // Substract 1 from the index since the numbering in XML start with 1 whereas the modelVariables start with 0.
            if (dependencies[i].isEmpty()) {
                continue;
            }
            else{
                try {
                    FMIScalarVariable scalar = fmiModelDescription.modelVariables
                            .get(Integer.parseInt(dependencies[i]) - 1);
                    dependentScalarVariables.add(scalar);
                } catch (NumberFormatException ex) {
                    throw new NumberFormatException(
                            "Failed to parse dependencies index "
                                    + dependencies[i]);
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////             public fields                                 ////

    /** The index of the output derivative. */
    public Integer index;

    /** The list of dependent ScalarVariable elements. */
    public LinkedList<FMIScalarVariable> dependentScalarVariables;

    /** The list of dependent ScalarVariable elements. */
    public FMIScalarVariable scalarVariable;
}
