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

public class CastAndInstanceofEliminator extends BodyTransformer
{
    private static CastAndInstanceofEliminator instance = new CastAndInstanceofEliminator();
    private CastAndInstanceofEliminator() {}

    public static CastAndInstanceofEliminator v() { return instance; }

    public String getDeclaredOptions() { return super.getDeclaredOptions(); }
    
    protected void internalTransform(Body b, String phaseName, Map options)
    {
        JimpleBody body = (JimpleBody)b;
        if(Main.isVerbose)
            System.out.println("[" + body.getMethod().getName() +
                "] Eliminating unnecessary casts and instanceof...");
        
        for(Iterator units = body.getUnits().iterator();
            units.hasNext();) {
            Unit unit = (Unit)units.next();
            for(Iterator boxes = unit.getUseBoxes().iterator();
                boxes.hasNext();) {
                ValueBox box = (ValueBox)boxes.next();
                Value value = box.getValue();
              
                if(value instanceof CastExpr) {
                    // If the cast is to the same type as the 
                    // operand already is, then replace with 
                    // simple assignment.
                    CastExpr expr = (CastExpr)value;
                    Type castType = expr.getCastType();
                    Value op = expr.getOp();
                    Type opType = op.getType();
                    if(castType.equals(opType)) {
                        box.setValue(op);
                    }
                } else if(value instanceof InstanceOfExpr) {
                    // If the operand of the expression is 
                    // declared to be of a type that implies
                    // the instanceof is true, then replace
                    // with true.
                    InstanceOfExpr expr = (InstanceOfExpr)value;
                    Type checkType = expr.getCheckType();
                    Value op = expr.getOp();
                    Type opType = op.getType();

                    RefType checkRef, opRef;
                    if(checkType instanceof RefType && 
                            opType instanceof RefType) {
                        checkRef = (RefType)checkType;
                        opRef = (RefType)opType;
                      
                    } else if(checkType instanceof ArrayType &&
                              opType instanceof ArrayType) {
                        if(((ArrayType)checkType).numDimensions != 
                                ((ArrayType)opType).numDimensions) {
                            // We know the answer is false.
                            box.setValue(IntConstant.v(0));
                            continue;
                        }
                        Type checkBase = ((ArrayType)checkType).baseType;
                        Type opBase = ((ArrayType)opType).baseType;
                        if(checkBase instanceof RefType &&
                               opBase instanceof RefType) {
                            checkRef = (RefType)checkBase;
                            opRef = (RefType)opBase;
                        } else {
                            // Can't say anything?
                            continue;
                        }
                    } else {
                        // Can't say anything?
                        continue;
                    }
                    SootClass checkClass = ((RefType)checkRef).getSootClass();
                    SootClass opClass = ((RefType)checkRef).getSootClass();
                    Hierarchy hierarchy = Scene.v().getActiveHierarchy();
                    if(hierarchy.isClassSuperclassOfIncluding(checkClass, opClass) || 
                            opClass.getInterfaces().contains(checkClass)) {
                        // Then we know the instanceof will be true.
                        box.setValue(IntConstant.v(1));
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
/*
class InstanceFlowAnalysis extends ForwardFlowAnalysis
{
    FlowSet emptySet;
    Map localToPreserveSet;
    Map localToIntPair;

    public InstanceFlowAnalysis(UnitGraph g)
    {
        super(g);

        Object[] defs;
        FlowUniverse defUniverse;

        if(Main.isProfilingOptimization)
            Main.defsSetupTimer.start();

        // Create a list of all the definitions and group defs of the same local together
        {
            Map localToDefList = new HashMap(g.getBody().getLocalCount() * 2 + 1, 0.7f);

            // Initialize the set of defs for each local to empty
            {
                Iterator localIt = g.getBody().getLocals().iterator();

                while(localIt.hasNext())
                    {
                        Local l = (Local) localIt.next();

                        localToDefList.put(l, new ArrayList());
                    }
            }

            // Fill the sets up
            {
                Iterator it = g.iterator();

                while(it.hasNext())
                    {
                        Unit s = (Unit) it.next();

                    
                        List defBoxes = s.getDefBoxes();
                        if(!defBoxes.isEmpty()) {
                            if(!(defBoxes.size() ==1)) 
                                throw new RuntimeException("FastColorer: invalid number of def boxes");
                            
                            if(((ValueBox)defBoxes.get(0)).getValue() instanceof Local) {
                                Local defLocal = (Local) ((ValueBox)defBoxes.get(0)).getValue();
                                List l = (List) localToDefList.get(defLocal);
                            
                                if(l == null)
                                    throw new RuntimeException("local " + defLocal + " is used but not declared!");
                                else
                                    l.add(s);
                            }
                        }
                    
                    }
            }

            // Generate the list & localToIntPair
            {
                Iterator it = g.getBody().getLocals().iterator();
                List defList = new LinkedList();

                int startPos = 0;

                localToIntPair = new HashMap(g.getBody().getLocalCount() * 2 + 1, 0.7f);

                // For every local, add all its defs
                {
                    while(it.hasNext())
                        {
                            Local l = (Local) it.next();
                            Iterator jt = ((List) localToDefList.get(l)).iterator();

                            int endPos = startPos - 1;

                            while(jt.hasNext())
                                {
                                    defList.add(jt.next());
                                    endPos++;
                                }

                            localToIntPair.put(l, new IntPair(startPos, endPos));

                            // System.out.println(startPos + ":" + endPos);

                            startPos = endPos + 1;
                        }
                }

                defs = defList.toArray();
                defUniverse = new FlowUniverse(defs);
            }
        }

        emptySet = new ArrayPackedSet(defUniverse);

        // Create the preserve sets for each local.
        {
            Map localToKillSet = new HashMap(g.getBody().getLocalCount() * 2 + 1, 0.7f);
            localToPreserveSet = new HashMap(g.getBody().getLocalCount() * 2 + 1, 0.7f);

            Chain locals = g.getBody().getLocals();

            // Initialize to empty set
            {
                Iterator localIt = locals.iterator();

                while(localIt.hasNext())
                    {
                        Local l = (Local) localIt.next();

                        localToKillSet.put(l, emptySet.clone());
                    }
            }

            // Add every definition of this local
            for(int i = 0; i < defs.length; i++)
                {
                    Unit s = (Unit) defs[i];
                    
                    List defBoxes = s.getDefBoxes();
                    if(!(defBoxes.size() ==1)) 
                        throw new RuntimeException("SimpleLocalDefs: invalid number of def boxes");
                            
                    if(((ValueBox)defBoxes.get(0)).getValue() instanceof Local) {
                        Local defLocal = (Local) ((ValueBox)defBoxes.get(0)).getValue();
                        BoundedFlowSet killSet = (BoundedFlowSet) localToKillSet.get(defLocal);
                        killSet.add(s, killSet);
                        
                    }
                }
            
            // Store complement
            {
                Iterator localIt = locals.iterator();

                while(localIt.hasNext())
                    {
                        Local l = (Local) localIt.next();

                        BoundedFlowSet killSet = (BoundedFlowSet) localToKillSet.get(l);

                        killSet.complement(killSet);

                        localToPreserveSet.put(l, killSet);
                    }
            }
        }

        if(Main.isProfilingOptimization)
            Main.defsSetupTimer.end();

        if(Main.isProfilingOptimization)
            Main.defsAnalysisTimer.start();

        doAnalysis();
        
        if(Main.isProfilingOptimization)
            Main.defsAnalysisTimer.end();
    }
    
    protected Object newInitialFlow()
    {
        return emptySet.clone();
}

    protected void flowThrough(Object inValue, Object d, Object outValue)
    {
        FlowSet in = (FlowSet) inValue, out = (FlowSet) outValue;
        Unit unit = (Unit)d;

        List defBoxes = unit.getDefBoxes();
        if(!defBoxes.isEmpty()) {
            if(!(defBoxes.size() ==1)) 
                throw new RuntimeException("FastColorer: invalid number of def boxes");
                          
            Value value = ((ValueBox)defBoxes.get(0)).getValue();
            if(value  instanceof Local) {
                Local defLocal = (Local) value;
            
                // Perform kill on value
                in.intersection((FlowSet) localToPreserveSet.get(defLocal), out);

                // Perform generation
                out.add(unit, out);
            } else { 
                in.copy(out);
                return;
            }


        

        }
        else
            in.copy(out);
    }

    protected void copy(Object source, Object dest)
    {
        FlowSet sourceSet = (FlowSet) source,
            destSet = (FlowSet) dest;
        
        sourceSet.copy(destSet);
    }

    protected void merge(Object in1, Object in2, Object out)
    {
        FlowSet inSet1 = (FlowSet) in1,
            inSet2 = (FlowSet) in2;
        
        FlowSet outSet = (FlowSet) out;
        
        inSet1.union(inSet2, outSet);
    }
}




*/
