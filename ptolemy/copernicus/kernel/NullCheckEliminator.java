/* A transformer that tried to statically evaluate object == object

 Copyright (c) 2001 The Regents of the University of California.
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


package ptolemy.copernicus.kernel;

import soot.*;
import soot.jimple.*;
import soot.toolkits.scalar.*;
import soot.util.*;
import soot.toolkits.graph.*;
import java.util.*;

/** 
A transformer that removes isNull checks. 
This uses null-pointer analysis to determine which checks can be
removed.
*/

public class InstanceEqualityEliminator extends BodyTransformer
{
    private static InstanceEqualityEliminator instance = new InstanceEqualityEliminator();
    private InstanceEqualityEliminator() {}

    public static InstanceEqualityEliminator v() {
        return instance; 
    }

    public String getDeclaredOptions() { 
        return super.getDeclaredOptions() + " debug"; 
    }
    
    protected void internalTransform(Body b, String phaseName, Map options)
    {
        JimpleBody body = (JimpleBody)b;
        System.out.println("NullCheckEliminator.internalTransform("
                + phaseName + ", " + body.getMethod() + ", " + options + ")");
        
        boolean debug = Options.getBoolean(options, "debug");
        CompleteUnitGraph unitGraph = new CompleteUnitGraph(body);

        MustAliasAnalysis mustAliasAnalysis = new MustAliasAnalysis(unitGraph);
        MaybeAliasAnalysis maybeAliasAnalysis = new MaybeAliasAnalysis(unitGraph);
 
        // Loop through all the unit
        for(Iterator units = body.getUnits().iterator();
            units.hasNext();) {
            Unit unit = (Unit)units.next();
            for(Iterator boxes = unit.getUseBoxes().iterator();
                boxes.hasNext();) {
                ValueBox box = (ValueBox)boxes.next();
                Value value = box.getValue();
              
                if(value instanceof BinopExpr) {
                    BinopExpr binop = (BinopExpr)value;
                    Value left = binop.getOp1();
                    Value right = binop.getOp2();
                    if((left.getType() instanceof RefType ||
                            left.getType() instanceof NullType) &&
                            (right.getType() instanceof RefType ||
                                    right.getType() instanceof NullType)) {
                        Set leftMustAliases, rightMustAliases;
                        Set leftMaybeAliases, rightMaybeAliases;
                        if(left instanceof Local) {
                            leftMustAliases = 
                                mustAliasAnalysis.getAliasesOfBefore(
                                        (Local)left, unit);
                            leftMaybeAliases = 
                                maybeAliasAnalysis.getAliasesOfBefore(
                                        (Local)left, unit);
                        } else {
                            // A null constant.
                            leftMustAliases = new HashSet();
                            leftMustAliases.add(left);
                            leftMaybeAliases = new HashSet();
                            leftMaybeAliases.add(left);
                        } 
                        if(right instanceof Local) {
                            rightMustAliases = 
                                mustAliasAnalysis.getAliasesOfBefore(
                                        (Local)right, unit);
                            rightMaybeAliases = 
                                maybeAliasAnalysis.getAliasesOfBefore(
                                        (Local)right, unit);
                        } else {
                            // A null constant.
                            rightMustAliases = new HashSet();
                            rightMustAliases.add(right);
                            rightMaybeAliases = new HashSet();
                            rightMaybeAliases.add(right);
                         } 
                         
                        if(debug) System.out.println("checking unit = " + unit);
                        if(debug) System.out.println("left aliases = " + 
                                leftMustAliases);
                        if(debug) System.out.println("right aliases = " + 
                                rightMustAliases);
                        if(debug) System.out.println("left maybe aliases = " + 
                                leftMaybeAliases);
                        if(debug) System.out.println("right maybe aliases = " + 
                                rightMaybeAliases);
                        // Utter hack... Should be:
                        // if(mustAliasAnalysis.getAliasesOfBefore((Local)left, unit).contains(right)) {
                        Set intersection = leftMustAliases;
                        intersection.retainAll(rightMustAliases);
                        if(!intersection.isEmpty()) {
                            System.out.println("instances are equal");
                            binop.getOp1Box().setValue(IntConstant.v(0));
                            binop.getOp2Box().setValue(IntConstant.v(0));
                        } else {
                            // If either set of maybe aliases is unknown, then we can do nothing.
                            if(leftMaybeAliases != null && rightMaybeAliases != null) {
                                intersection = leftMaybeAliases;
                                leftMaybeAliases.retainAll(rightMaybeAliases);
                                // If the two sets of aliases have nothing in common, 
                                // then the two object references cannot be equal.
                                if(intersection.isEmpty()) {
                                    System.out.println("instances are not equal");
                                    // Replace with operands that can be statically evaluated.
                                    binop.getOp1Box().setValue(IntConstant.v(0));
                                    binop.getOp2Box().setValue(IntConstant.v(1));
                                }
                            }
                        }                      
                    }
                }
            }
        }
    }
}

