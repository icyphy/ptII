/* An analysis that finds the free variables in a ptolemy model

 Copyright (c) 2001-2003 The Regents of the University of California.
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
@ProposedRating Yellow (neuendor@eecs.berkeley.edu)
@AcceptedRating Yellow (neuendor@eecs.berkeley.edu)
*/

package ptolemy.actor.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import ptolemy.actor.CompositeActor;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ParseTreeFreeVariableCollector;
import ptolemy.data.expr.PtParser;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// FreeVariableModelAnalysis
/**
An analysis that traverses a model to determine all the free variables
in a hierarchical model.  The free variables in a model are defined to
be the set of identifiers that are referenced by the model, but are
not defined in the model.  The free variables must be assigned values
for the model to be executable.

This class traverses the model, but it not read synchronized on the
model, therefore its caller should be.

@author Stephen Neuendorffer
@version $Id$
@since Ptolemy II 2.0
*/
public class FreeVariableModelAnalysis {

    /** Analyze the given model to return a set of names which must
     *  be defined externally for the model to be completely specified.
     *  In addition, store the intermediate results for contained actors
     *  so they can be retrieved by the getFreeVariables() method.
     *  @param model The model that will be analyzed. 
     *  @exception IllegalActionException If an exception occurs
     *  during analysis.
     */
    public FreeVariableModelAnalysis(Entity model)
            throws IllegalActionException {
        _entityToFreeVariableNameSet = new HashMap();
        _freeVariables(model);
    }

    /** Return the computed free variables for the given entity.
     *  @param entity An entity, which must be deeply contained by the
     *  model for which this analysis was created.
     *  @exception RuntimeException If the free variables for the
     *  entity have not already been computed.
     */
    public Set getFreeVariables(Entity entity) {
        Set freeVariables = (Set)_entityToFreeVariableNameSet.get(entity);
        if (freeVariables == null) {
            throw new RuntimeException("Entity " + entity.getFullName() +
                    " has not been analyzed.");
        }

        return Collections.unmodifiableSet(freeVariables);
    }


    ///////////////////////////////////////////////////////////////////
    ////                          private methods                  ////

    // Recursively compute the set of free variables for all actors
    // deeply contained in the given model.
    private Set _freeVariables(Entity model) throws IllegalActionException {
        // First get the free variables of contained actors.
        Set set = new HashSet();
        if (model instanceof CompositeEntity) {
            for (Iterator entities =
                     ((CompositeEntity)model).entityList().iterator();
                 entities.hasNext();) {
                Entity entity = (Entity)entities.next();
                set.addAll(_freeVariables(entity));
            }
        }

        // Next, compute the set of variable names defined in this container.
        Set variableNames = new HashSet();
        for (Iterator variables =
                 model.attributeList(Variable.class).iterator();
             variables.hasNext();) {
            Variable variable = (Variable)variables.next();
            variableNames.add(variable.getName());
        }
        variableNames = Collections.unmodifiableSet(variableNames);

        // Free variables of contained actors that are defined in this
        // container are not free variables of this container.
        set.removeAll(variableNames);

        // Iterate over all the variables of this container, and add in 
        // any free variables they reference.
        PtParser parser = new PtParser();
        ParseTreeFreeVariableCollector collector =
            new ParseTreeFreeVariableCollector();
        for (Iterator variables =
                 model.attributeList(Variable.class).iterator();
             variables.hasNext();) {
            Variable variable = (Variable)variables.next();
            String expression = variable.getExpression();
            ASTPtRootNode root;     
            if(variable.isStringMode()) {
                root = parser.generateStringParseTree(expression);
            } else {
                root = parser.generateParseTree(expression);
            }
            Set freeIdentifiers = 
                new HashSet(collector.collectFreeVariables(root));

            // Identifiers that reference other variables in the same container
            // are bound, not free.
            Set tempSet = new HashSet(variableNames);
            tempSet.remove(variable.getName());
            freeIdentifiers.removeAll(tempSet);
            
            set.addAll(freeIdentifiers);
        }

        _entityToFreeVariableNameSet.put(model, set);
        return set;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private HashMap _entityToFreeVariableNameSet;
    private CompositeActor _model;
}
