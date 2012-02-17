/* An FMU ModelVariable.

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

import java.util.LinkedList;
import java.util.List;

///////////////////////////////////////////////////////////////////
//// ModelVariable

/**
 * An object that represents the ModelVariable element of a
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
public class ModelVariables {

    /** Create a ModelVariable. */
    public ModelVariables() {
        _scalarVariables = new LinkedList<ScalarVariable>();
    }

    /** Add a ScalarVariable to the list of ScalarVariables for this ModelVariable.
     *  @param scalarVariable The scalarVariable to be added.
     *  @see #getScalarVariables()
     */
    public void addScalarVariable(ScalarVariable scalarVariable) {
        _scalarVariables.add(scalarVariable);
    }

    /** Return the list of ScalarVariables for this scalarDescription.
     *  @return The list of scalarVariables.    
     *  @see #addScalarVariables(ScalarVariable)
     */   
    public List<ScalarVariable> getScalarVariables() {
        // FIXME: should we return a copy?
        return _scalarVariables;
    }

    // FIXME: Add other elements of a modelVariable;
    private List<ScalarVariable> _scalarVariables;
}
