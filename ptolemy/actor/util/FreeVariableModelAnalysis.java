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
@AcceptedRating Red (cxh@eecs.berkeley.edu)
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
be the set of names that must be defined by variables with the model in scope
for all of the expressions in the model to be evaluatable.

@author Stephen Neuendorffer
@version $Id$
@since Ptolemy II 3.1
*/
public class FreeVariableModelAnalysis {

    /** Analyze the given model to return a set of names which must
     *  be defined externally for the model to be completely specified.
     *  In addition, store the intermediate results for contained actors
     *  so they can be retrieved by the getFreeVariables() method.
     *  @exception IllegalActionException If an exception occurs
     *  during analysis.
     */
    public Set analyzeFreeVariables(Entity model)
            throws IllegalActionException {
        _entityToFreeVariableSet = new HashMap();
        return _freeVariables(model);
    }

    /** Return the computed free variables for the given entity.
     *  @exception RuntimeException If the free variables for the
     *  entity have not already been computed.
     */
    public Set getFreeVariables(Entity entity) {
        Set freeVariables = (Set)_entityToFreeVariableSet.get(entity);
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
        Set set = new HashSet();
        if (model instanceof CompositeEntity) {
            for (Iterator entities =
                     ((CompositeEntity)model).entityList().iterator();
                 entities.hasNext();) {
                Entity entity = (Entity)entities.next();
                set.addAll(_freeVariables(entity));
            }
        }

        Set variableNames = new HashSet();
        for (Iterator variables =
                 model.attributeList(Variable.class).iterator();
             variables.hasNext();) {
            Variable variable = (Variable)variables.next();
            variableNames.add(variable.getName());
        }
        variableNames = Collections.unmodifiableSet(variableNames);

        set.removeAll(variableNames);

        PtParser parser = new PtParser();
        ParseTreeFreeVariableCollector collector =
            new ParseTreeFreeVariableCollector();
        for (Iterator variables =
                 model.attributeList(Variable.class).iterator();
             variables.hasNext();) {
            Variable variable = (Variable)variables.next();
            String expression = variable.getExpression();
            ASTPtRootNode root = parser.generateParseTree(expression);
            Set freeVars = new HashSet(collector.collectFreeVariables(root));
            Set tempSet = new HashSet(variableNames);
            tempSet.remove(variable.getName());

            freeVars.removeAll(tempSet);
            set.addAll(freeVars);
        }

        _entityToFreeVariableSet.put(model, set);
        return set;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private HashMap _entityToFreeVariableSet;
    private CompositeActor _model;
}
