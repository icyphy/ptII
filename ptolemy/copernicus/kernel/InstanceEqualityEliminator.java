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
An attempt to remove instance equality checks.  This is not strictly correct.  I think a better way to 
formulate it is as a forward dataflow problem that colors all of the locals based on the
set of objects that they can refer to.

Namely, this is not correct because it assumes that all fields contain distinct objects.  It also depends on the fact that the values are defined at fields.

*/

public class InstanceEqualityEliminator extends BodyTransformer
{
    private static InstanceEqualityEliminator instance = new InstanceEqualityEliminator();
    private InstanceEqualityEliminator() {}

    public static InstanceEqualityEliminator v() { return instance; }

    public String getDeclaredOptions() { return super.getDeclaredOptions(); }
    
    protected void internalTransform(Body b, String phaseName, Map options)
    {
        JimpleBody body = (JimpleBody)b;
        if(Main.isVerbose)
            System.out.println("[" + body.getMethod().getName() +
                "] Eliminating instance equality checks...");
        
        CompleteUnitGraph unitGraph = new CompleteUnitGraph(body);
        //        SimpleLocalDefs localDefs = new SimpleLocalDefs(unitGraph);        
        MustAliasAnalysis aliasAnalysis = new MustAliasAnalysis(unitGraph);

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
                    if(left.getType() instanceof RefType &&
                            right.getType() instanceof RefType) {
                        System.out.println("checking unit = " + unit);
                        System.out.println("left aliases = " + aliasAnalysis.getAliasesOfBefore((Local)left, unit));
                        System.out.println("right aliases = " + aliasAnalysis.getAliasesOfBefore((Local)right, unit));
                        // Utter hack... Should be:
                        // if(aliasAnalysis.getAliasesOfBefore((Local)left, unit).contains(right)) {
                        Set intersection = aliasAnalysis.getAliasesOfBefore((Local)left, unit);
                        intersection.retainAll(aliasAnalysis.getAliasesOfBefore((Local)right, unit));
                        if(!intersection.isEmpty()) {
                            binop.getOp1Box().setValue(IntConstant.v(0));
                            binop.getOp2Box().setValue(IntConstant.v(0));
                        } else {
                            // Another awful hack.   In order to do this, we need maybe analysis...  
                            // System.out.println("unequivalent definitions of -" + binop.getSymbol() + "!");
                            // Replace with operands that can be statically evaluated.
                            binop.getOp1Box().setValue(IntConstant.v(0));
                            binop.getOp2Box().setValue(IntConstant.v(1));
                        }
                        /*
                        // System.out.println("expr = " + binop);
                        Object leftDef = _getUniqueDef(left, unit, localDefs);
                        Object rightDef = _getUniqueDef(right, unit, localDefs);
                        // System.out.println("def = " + leftDef);
                        // System.out.println("def = " + rightDef);
                        if(leftDef instanceof EquivTo &&
                                leftDef instanceof FieldRef &&
                                rightDef instanceof EquivTo &&
                                rightDef instanceof FieldRef &&
                                Modifier.isFinal(
                                        ((FieldRef)leftDef).getField().getModifiers())) {
                            if(((EquivTo)leftDef).equivTo(rightDef)) {
                                // System.out.println("equivalent definitions of -" + binop.getSymbol() + "!");
                                // Replace with operands that can be statically evaluated.
                                binop.getOp1Box().setValue(IntConstant.v(0));
                                binop.getOp2Box().setValue(IntConstant.v(0));
                            } else {
                                // System.out.println("unequivalent definitions of -" + binop.getSymbol() + "!");
                                // Replace with operands that can be statically evaluated.
                                binop.getOp1Box().setValue(IntConstant.v(0));
                                binop.getOp2Box().setValue(IntConstant.v(1));
                            }
                        }
                       */
                    }
                }
            }
        }
    }

    private Object _getUniqueDef(Value value, Unit unit, LocalDefs localDefs) {
        List defs = localDefs.getDefsOfAt((Local)value, unit);
        if(defs.size() == 1) {
            DefinitionStmt stmt = (DefinitionStmt)defs.get(0);
            if(stmt.getRightOp() instanceof CastExpr) {
                CastExpr castExpr = (CastExpr)stmt.getRightOp();
                return _getUniqueDef(castExpr.getOp(), stmt, localDefs);
            } else if(stmt.getRightOp() instanceof Local) {
                return _getUniqueDef(stmt.getRightOp(), stmt, localDefs);
            } else {
                return stmt.getRightOp();
            }           
        } else {
            return value;
        }
    }
}

