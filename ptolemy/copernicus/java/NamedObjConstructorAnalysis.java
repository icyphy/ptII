/* An analysis for extracting the constructors of named objects.

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
@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.copernicus.java;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import ptolemy.copernicus.kernel.MustAliasAnalysis;
import ptolemy.copernicus.kernel.PtolemyUtilities;
import ptolemy.copernicus.kernel.SootUtilities;
import soot.Local;
import soot.RefType;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.JimpleBody;
import soot.jimple.NewExpr;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.toolkits.scalar.Evaluator;
import soot.toolkits.graph.CompleteUnitGraph;

//////////////////////////////////////////////////////////////////////////
//// NamedObjConstructorAnalysis
/**
An analysis that establishes a correspondence between each constructor
of a named object and the location in the transformed code where that
object is created.  This information is used to create fields with the
appropriate naming convention for
named objects that are constructed, but don't have an appropriate field.

@author Stephen Neuendorffer
@version $Id$
@since Ptolemy II 2.0
*/
public class NamedObjConstructorAnalysis {
    public NamedObjConstructorAnalysis(JimpleBody body) {
        CompleteUnitGraph g = new CompleteUnitGraph(body);
        MustAliasAnalysis analysis = new MustAliasAnalysis(g);

        Local thisLocal = body.getThisLocal();

        _newExprToConstructor = new HashMap();
        for (Iterator units = body.getUnits().iterator();
             units.hasNext();) {
            Stmt unit = (Stmt)units.next();
            if (unit.containsInvokeExpr() &&
                    unit.getInvokeExpr() instanceof NewExpr) {
                _newExprToConstructor.put(unit.getInvokeExpr(),
                        unit);
            }
        }

        _constructorToContainerConstructor = new HashMap();
        _constructorToName = new HashMap();
        _nameToConstructor = new HashMap();

        for (Iterator units = body.getUnits().iterator();
             units.hasNext();) {
            Stmt unit = (Stmt)units.next();
            if (unit.containsInvokeExpr() &&
                    unit.getInvokeExpr() instanceof InstanceInvokeExpr) {
                InstanceInvokeExpr invokeExpr =
                    (InstanceInvokeExpr)unit.getInvokeExpr();
                SootMethod invokedMethod = invokeExpr.getMethod();
                // If we invoke a container, name constructor.
                if (invokedMethod.getName().equals("<init>") &&
                        invokedMethod.getParameterCount() >= 2 &&
                        !analysis.getAliasesOfBefore((Local)invokeExpr.getBase(), unit).contains(thisLocal)
                        && SootUtilities.isSubtypeOf(
                                invokedMethod.getParameterType(0),
                                RefType.v(PtolemyUtilities.namedObjClass)) &&
                        invokedMethod.getParameterType(1).equals(
                                RefType.v("java.lang.String"))) {
                    System.out.println("found 2 arg constructor: " + unit);
                    // Container, name initializer
                    Unit constructor = _findConstructor(
                            (Local)invokeExpr.getBase(), thisLocal, unit, analysis);
                    Unit containerConstructor = _findConstructor(
                            (Local)invokeExpr.getArg(0), thisLocal, unit, analysis);

                    // Save the container.
                    _constructorToContainerConstructor.put(constructor, containerConstructor);

                    // Save the name.
                    Value nameValue = invokeExpr.getArg(1);
                    System.out.println("attribute name = " + nameValue);
                    if (Evaluator.isValueConstantValued(nameValue)) {
                        StringConstant nameConstant =
                            (StringConstant)
                            Evaluator.getConstantValueOf(nameValue);
                        String name = nameConstant.value;
                        _constructorToName.put(constructor, name);
                    } else {
                        String string = "Argument to setName call cannot be statically evaluated";
                        throw new RuntimeException(string);
                    }

                } else if (invokedMethod.getName().equals("setName")) {
                    System.out.println("found setName " + unit);
                    Unit constructor = _findConstructor(
                            (Local)invokeExpr.getBase(), thisLocal, unit, analysis);

                    // Save the name.
                    Value nameValue = invokeExpr.getArg(0);
                    //System.out.println("attribute name = " + nameValue);
                    if (Evaluator.isValueConstantValued(nameValue)) {
                        StringConstant nameConstant =
                            (StringConstant)
                            Evaluator.getConstantValueOf(nameValue);
                        String name = nameConstant.value;
                        _constructorToName.put(constructor, name);
                    } else {
                        String string = "Argument to setName call cannot be statically evaluated";
                        throw new RuntimeException(string);

                    }

                } else if (invokedMethod.getName().equals("setContainer")) {
                    System.out.println("found setContainer " + unit);
                    Unit constructor = _findConstructor(
                            (Local)invokeExpr.getBase(), thisLocal, unit, analysis);
                    Unit containerConstructor = _findConstructor(
                            (Local)invokeExpr.getArg(0), thisLocal, unit, analysis);

                    // Save the container.
                    _constructorToContainerConstructor.put(constructor, containerConstructor);
                }
            }
        }

        // Remap the containers and the names.
        for (Iterator constructors = _constructorToName.keySet().iterator();
             constructors.hasNext();) {
            Unit constructor = (Unit)constructors.next();
            String fullName = _getFullName(constructor);
            System.out.println("fullName = " + fullName);
            System.out.println("constructor = " + constructor);
            _nameToConstructor.put(fullName, constructor);
        }
    }

    /** Return the invocation that creates an object with the given name.
     */
    public Unit getConstructor(String name) {
        return (Unit)_nameToConstructor.get(name);
    }

    private String _getFullName(Unit constructor) {
        if (_constructorToContainerConstructor.get(constructor) == null) {
            return (String)_constructorToName.get(constructor);
        } else {
            String containerName =
                _getFullName((Unit)_constructorToContainerConstructor.get(constructor));
            return containerName + "." + (String)_constructorToName.get(constructor);
        }
    }

    private Unit _findConstructor(Local local, Local thisRef, Unit unit,
            MustAliasAnalysis analysis) {
        Set aliasSet = analysis.getAliasesOfBefore(local, unit);
        System.out.println("unit = " + unit);
        System.out.println("aliases = " + aliasSet);
        NewExpr newExpr = null;
        for (Iterator aliases = aliasSet.iterator();
             aliases.hasNext();) {
            Object alias = aliases.next();
            if (alias instanceof NewExpr) {
                if (newExpr == null) {
                    newExpr = (NewExpr)alias;
                } else {
                    throw new RuntimeException("More than one newExpr found!");
                }
            } else if (alias.equals(thisRef)) {
                return null;
            }
        }
        if (newExpr == null) {
            throw new RuntimeException("No newExpr found!");
        }
        return (Unit)_newExprToConstructor.get(newExpr);
    }

    // The map that stores the information we return;
    private Map _nameToConstructor;
    // Temporary maps used during analysis;
    private Map _newExprToConstructor;
    private Map _constructorToName;
    private Map _constructorToContainerConstructor;
}
