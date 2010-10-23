/* A solver for product lattice-based ontologies.
 * 
 * Copyright (c) 2007-2010 The Regents of the University of California. All
 * rights reserved.
 * 
 * Permission is hereby granted, without written agreement and without license
 * or royalty fees, to use, copy, modify, and distribute this software and its
 * documentation for any purpose, provided that the above copyright notice and
 * the following two paragraphs appear in all copies of this software.
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
 */

package ptolemy.data.ontologies.lattice;

import java.util.List;

import ptolemy.data.StringToken;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.ontologies.OntologyAdapter;
import ptolemy.data.ontologies.OntologySolverModel;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// ProductLatticeOntologySolver

/** A solver for product lattice-based ontologies. This is a derived class
 *  of {@link LatticeOntologySolver} that specially handles adapters for
 *  ProductLatticeOntologies.
 * 
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 8.1
 *  @Pt.ProposedRating Red (cshelton)
 *  @Pt.AcceptedRating Red (cshelton)
 */
public class ProductLatticeOntologySolver extends LatticeOntologySolver {
    
    /** Constructor for the ProductLatticeOntologySolver.
     *  @param container The model that contains the OntologySolver
     *  @param name The name of the OntologySolver
     *  @exception IllegalActionException If there is any problem creating the
     *   OntologySolver object.
     *  @exception NameDuplicationException If there is already a component
     *   in the container with the same name
     */
    public ProductLatticeOntologySolver(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     protected methods                     ////
    
    /** Return the LatticeOntologyAdapter for the specified
     *  component. This instantiates a new OntologyAdapter if it does
     *  not already exist for the specified component.  This returns
     *  specific LatticeOntologyAdapters for the LatticeOntologySolver.
     * 
     *  @param component The specified component.
     *  @return The LatticeOntologyAdapter for the specified component.
     *  @exception IllegalActionException Thrown if the LatticeOntologyAdapter
     *   cannot be instantiated.
     */
    protected OntologyAdapter _getAdapter(Object component)
            throws IllegalActionException {
        OntologyAdapter adapter = null;
        
        if (_adapterStore.containsKey(component)) {
            return _adapterStore.get(component);
        } else {
            // Next look for the adapter in the LatticeOntologySolver model.
            List modelDefinedAdapters = ((OntologySolverModel) _model)
                    .attributeList(ActorConstraintsDefinitionAttribute.class);
            for (Object adapterDefinitionAttribute : modelDefinedAdapters) {
                if (((StringToken) ((ActorConstraintsDefinitionAttribute) adapterDefinitionAttribute).actorClassName
                        .getToken()).stringValue().equals(component.getClass().getName())) {
                    adapter = ((ActorConstraintsDefinitionAttribute) adapterDefinitionAttribute)
                            .createAdapter((ComponentEntity) component);
                    break;
                }
            }
        }
        
        if (adapter == null) {
            adapter = new ProductLatticeOntologyAdapter(this, component, false);            
        }

        _adapterStore.put(component, adapter);
        return adapter;
    }

}
