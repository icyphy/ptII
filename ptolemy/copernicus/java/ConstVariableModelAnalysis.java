/* An analysis that finds the free variables in a ptolemy model

 Copyright (c) 2001-2002 The Regents of the University of California.
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
@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.copernicus.java;

import soot.*;
import soot.jimple.*;
import soot.toolkits.graph.CompleteUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;
import soot.toolkits.scalar.LocalDefs;
import soot.toolkits.scalar.SimpleLocalDefs;
import soot.toolkits.scalar.SimpleLocalUses;
import soot.toolkits.scalar.LocalUses;

import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.TypeLattice;
import ptolemy.copernicus.kernel.SootUtilities;
import ptolemy.copernicus.kernel.PtolemyUtilities;
import ptolemy.copernicus.kernel.FastForwardFlowAnalysis;

import java.util.*;

//////////////////////////////////////////////////////////////////////////
//// ConstVariableModelAnalysis
/**
An analysis that traverses a model to determine all the constant
variables in a hierarchical model.  Basically, a constant variable in
the context of a particular model is any variable that is defined by a
constant expression in the model, or any variable whos list of
variables is contained within the set of constants at a particular
level.  (Note that computing the set of constant variables requires an
iteration to a fixed-point, because it is self referential.)
 
@author Stephen Neuendorffer
@version $Id$
@since Ptolemy II 2.0
*/
public class ConstVariableModelAnalysis {

    /** Analyze the given model to return a set of names which must 
     *  be defined externally for the model to be completely specified.
     *  In addition, store the intermediate results for contained actors
     *  so they can be retrieved by the getConstVariables() method.
     *  @exception IllegalActionException If an exception occurs
     *  during analysis.
     */
    public Set analyzeConstVariables(Entity model)
            throws IllegalActionException {
        _entityToConstVariableSet = new HashMap();
        return _constVariables(model);
    }

    /** Return the computed free variables for the given entity.
     *  @exception RuntimeException If the free variables for the
     *  entity have not already been computed.
     */
    public Set getConstVariables(Entity entity) {
        Set constVariables = (Set)_entityToConstVariableSet.get(entity);
        if(constVariables == null) {
            throw new RuntimeException("Entity " + entity.getFullName() + 
                    " has not been analyzed.");
        }

        return Collections.unmodifiableSet(constVariables);
    }


    // Recursively compute the set of const variables for all actors
    // deeply contained in the given model.
    private Set _constVariables(Entity model) throws IllegalActionException {
        // Sets of variables used to track the fixed point iteration.
        Set notTestedSet = new HashSet();
        Set testedSet = new HashSet();
        // Set of the names of constants that we are computing.
        Set constants = new HashSet();
        
        // initialize the set of constants to the constants of the container.
        if(model.getContainer() != null) {
            constants.addAll(getConstVariables((Entity)model.getContainer()));
        }

        // initialize the work list to the set of attributes.
        notTestedSet.addAll(model.attributeList(Variable.class));

        PtParser parser = new PtParser();
        ParseTreeFreeVariableCollector collector = 
            new ParseTreeFreeVariableCollector();

        // The fixed point of the constant set.
        boolean doneSomething = true;
        while(doneSomething) {
            doneSomething = false;
            while(!notTestedSet.isEmpty()) {
                Variable variable = (Variable)notTestedSet.iterator().next();
                notTestedSet.remove(variable);
                String expression = variable.getExpression();
                // compute the variables.
                ASTPtRootNode root = parser.generateParseTree(expression);
                Set freeVars = new HashSet(
                        collector.collectFreeVariables(root));
                if(constants.containsAll(freeVars)) {
                    // Then the variable defines a constant.
                    constants.add(variable.getName());
                    doneSomething = true;
                } else {
                    // may not define a constant.
                    testedSet.add(variable);
                }
            }
            // reset for next iteration.
            notTestedSet.addAll(testedSet);
            testedSet.clear();
        }

        _entityToConstVariableSet.put(model, constants);

        // recurse down.
        if(model instanceof CompositeEntity) {
            for(Iterator entities = 
                    ((CompositeEntity)model).entityList().iterator();
                entities.hasNext();) {
                Entity entity = (Entity)entities.next();
                _constVariables(entity);
            }
        }
        return constants;
    }
    
    private HashMap _entityToConstVariableSet;
    private CompositeActor _model;
}
