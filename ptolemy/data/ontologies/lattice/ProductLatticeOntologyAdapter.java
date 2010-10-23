/** A product lattice-based ontology adapter whose constraints are derived from
 *  the component ontology solvers.
 * 
 * Copyright (c) 2007-2010 The Regents of the University of California. All
 * rights reserved. Permission is hereby granted, without written agreement and
 * without license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, provided that the above
 * copyright notice and the following two paragraphs appear in all copies of
 * this software.
 * 
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
 * CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN
 * "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO PROVIDE
 * MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 * 
 * PT_COPYRIGHT_VERSION_2 COPYRIGHTENDKEY
 */

package ptolemy.data.ontologies.lattice;

import java.util.List;

import ptolemy.graph.Inequality;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// ProductLatticeOntologyAdapter

/** A product lattice-based ontology adapter whose constraints are derived from
 *  the component ontology solvers.
 *  
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 8.1
 *  @Pt.ProposedRating Red (cshelton)
 *  @Pt.AcceptedRating Red (cshelton)
 */
public class ProductLatticeOntologyAdapter extends LatticeOntologyAdapter {
    
    /**
     * Construct the product lattice ontology adapter associated with the given
     * component and solver. The constructed adapter implicitly uses the default
     * constraints set by the solver.
     * @param solver The specified lattice-based ontology solver.
     * @param component The associated component.
     * @exception IllegalActionException Thrown if the adapter cannot be
     * initialized.
     */
    public ProductLatticeOntologyAdapter(ProductLatticeOntologySolver solver, Object component)
            throws IllegalActionException {
        this(solver, component, true);
    }

    /**
     * Construct the product lattice ontology adapter for the given component and
     * property lattice.
     * @param solver The specified lattice-based ontology solver.
     * @param component The given component.
     * @param useDefaultConstraints Indicate whether this adapter uses the
     * default actor constraints.
     * @exception IllegalActionException Thrown if the adapter cannot be
     * initialized.
     */
    public ProductLatticeOntologyAdapter(ProductLatticeOntologySolver solver,
            Object component, boolean useDefaultConstraints)
            throws IllegalActionException {
        super(solver, component, useDefaultConstraints);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     * Return the constraints of this component. The constraints is a list of
     * inequalities. The constraints are generated from the component ontology
     * constraint lists.
     * @return The constraints of this component.
     * @exception IllegalActionException Thrown if there is a problem creating
     *  the constraints.
     */
    public List<Inequality> constraintList() throws IllegalActionException {
        if (!_useDefaultConstraints) {
            
        }

        return super.constraintList();
    }

}
