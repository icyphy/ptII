/* A transformer that inlines references to tokens.

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

package ptolemy.copernicus.java;

import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.invoke.SiteInliner;
import soot.jimple.toolkits.invoke.StaticInliner;
import soot.jimple.toolkits.invoke.InvokeGraphBuilder;
import soot.jimple.toolkits.scalar.ConditionalBranchFolder;
import soot.jimple.toolkits.scalar.ConstantPropagatorAndFolder;
import soot.jimple.toolkits.scalar.CopyPropagator;
import soot.jimple.toolkits.scalar.DeadAssignmentEliminator;
import soot.jimple.toolkits.scalar.UnreachableCodeEliminator;
import soot.jimple.toolkits.scalar.Evaluator;
import soot.toolkits.graph.*;
import soot.toolkits.scalar.*;
import soot.dava.*;
import soot.util.*;
import java.io.*;
import java.util.*;

import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.moml.*;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.data.*;
import ptolemy.data.expr.Variable;
import ptolemy.copernicus.kernel.SootUtilities;
import ptolemy.copernicus.kernel.CastAndInstanceofEliminator;
import ptolemy.copernicus.kernel.MustAliasAnalysis;


/**
A Transformer that is responsible for inlining the values of tokens.
The values of the parameters are taken from the model specified for this 
transformer.
*/
public class TokenToNativeTransformer extends SceneTransformer {
    /** Construct a new transformer
     */
    private TokenToNativeTransformer(CompositeActor model) {
        _model = model;
    }

    /** Return an instance of this transformer that will operate on the given model.
     *  The model is assumed to already have been properly initialized so that
     *  resolved types and other static properties of the model can be inspected.
     */
    public static TokenToNativeTransformer v(CompositeActor model) { 
        return new TokenToNativeTransformer(model);
    }

    public String getDefaultOptions() {
        return ""; 
    }

    public String getDeclaredOptions() { 
        return super.getDeclaredOptions() + " deep"; 
    }

    protected void internalTransform(String phaseName, Map options) {
        int localCount = 0;
        System.out.println("TokenToNativeTransformer.internalTransform("
                + phaseName + ", " + options + ")");

        if(!Options.getBoolean(options, "deep")) {
            return;
        }

        boolean debug = Options.getBoolean(options, "debug");
        // Loop over all the actor instance classes.
        for(Iterator classes = Scene.v().getApplicationClasses().iterator();
            classes.hasNext();) {
            SootClass entityClass = (SootClass)classes.next();
            
            System.out.println("Inlining token methods in " + entityClass);

            // Inline all token methods, until we run out of things to inline
            boolean doneSomething = true;
            int count = 0;
            while(doneSomething && count < 10) {
                doneSomething = false;
                System.out.println("inlining iteration " + count++);
                // First split local variables that are redefined...  
                // This will allow us to get a better type inference below.
                for(Iterator methods = entityClass.getMethods().iterator();
                    methods.hasNext();) {
                    SootMethod method = (SootMethod)methods.next();
                    
                    JimpleBody body = (JimpleBody)method.retrieveActiveBody();
                    
                    LocalSplitter.v().transform(body, phaseName + ".ls", "");
                    // Run some cleanup...  this will speedup the rest of the analysis.
                    // And prevent typing errors.
                    CastAndInstanceofEliminator.v().transform(body, phaseName + ".cie", "");
                    CopyPropagator.v().transform(body, phaseName + ".cp", "");
                    ConstantPropagatorAndFolder.v().transform(body, phaseName + ".cpf", "");
                    ConditionalBranchFolder.v().transform(body, phaseName + ".cbf", "");
                    UnreachableCodeEliminator.v().transform(body, phaseName + ".uce", "");
                }

                // Now run the type specialization algorithm...  This 
                // allows us to resolve the methods that we are inlining
                // with better precision.
                Map objectToTokenType = TypeSpecializer.specializeTypes(debug, entityClass);

                for(Iterator methods = entityClass.getMethods().iterator();
                    methods.hasNext();) {
                    SootMethod method = (SootMethod)methods.next();

                    JimpleBody body = (JimpleBody)method.retrieveActiveBody();

                    if(debug) System.out.println("method = " + method);

                    Hierarchy hierarchy = Scene.v().getActiveHierarchy();
                    // First inline all the methods that execute on Tokens.
                    for(Iterator units = body.getUnits().snapshotIterator();
                        units.hasNext();) {
                        Unit unit = (Unit)units.next();
                        Iterator boxes = unit.getUseBoxes().iterator();
                        while(boxes.hasNext()) {
                            ValueBox box = (ValueBox)boxes.next();
                            Value value = box.getValue();
                            if(value instanceof VirtualInvokeExpr) {
                                VirtualInvokeExpr r = (VirtualInvokeExpr)value;
                                // System.out.println("invoking = " + r.getMethod());
                                
                                if(r.getBase().getType() instanceof RefType) {
                                    RefType type = (RefType)r.getBase().getType();
                                    
                                    if(SootUtilities.derivesFrom(type.getSootClass(),
                                            PtolemyUtilities.tokenClass)) {
                                        List methodList = 
                                            hierarchy.resolveAbstractDispatch(
                                                    type.getSootClass(), 
                                                    r.getMethod());
                                        //System.out.println("checking token method call = " + r);
                                        //System.out.println("baseType = " + type.getSootClass());
                                        if(methodList.size() == 1) {
                                            SootMethod inlinee = (SootMethod)methodList.get(0);
                                            if(inlinee.getName().equals("getClass")) {
                                                // FIXME: do something better here.
                                                SootMethod newGetClassMethod = Scene.v().getMethod(
                                                        "<java.lang.Class: java.lang.Class forName(java.lang.String)>");
                                                box.setValue(Jimple.v().newStaticInvokeExpr(
                                                        newGetClassMethod, StringConstant.v("java.lang.Object")));
                                                        
                                            } else {
                                                SootClass declaringClass = inlinee.getDeclaringClass();
                                                declaringClass.setLibraryClass();
                                                if(!inlinee.isAbstract() && 
                                                        !inlinee.isNative()) {
                                                    // FIXME: only inline things where we are 
                                                    // also inlining the constructor???
                                                    // System.out.println("inlining");
                                                    inlinee.retrieveActiveBody();
                                                    // Then we know exactly what method will
                                                    // be called, so inline it.
                                                    SiteInliner.inlineSite(
                                                            inlinee, (Stmt)unit, method);
                                                    doneSomething = true;
                                                }
                                            }
                                        }
                                    }
                                }
                            } else if(value instanceof SpecialInvokeExpr) {
                                SpecialInvokeExpr r = (SpecialInvokeExpr)value;
                                //System.out.println("special invoking = " + r.getMethod());
                                
                                if(r.getBase().getType() instanceof RefType) {
                                    RefType type = (RefType)r.getBase().getType();
                                    
                                    if(SootUtilities.derivesFrom(type.getSootClass(),
                                            PtolemyUtilities.tokenClass)) {
                                        SootMethod inlinee =
                                            hierarchy.resolveSpecialDispatch(
                                                    r, method);
                                        SootClass declaringClass = inlinee.getDeclaringClass();
                                        declaringClass.setLibraryClass();
                                        if(!inlinee.isAbstract() && 
                                                !inlinee.isNative()) {
                                            //System.out.println("inlining");
                                            inlinee.retrieveActiveBody();
                                            // Then we know exactly what method will
                                            // be called, so inline it.
                                            SiteInliner.inlineSite(
                                                    inlinee, (Stmt)unit, method);
                                            doneSomething = true;
                                        }
                                    }
                                }
                            } else if(value instanceof StaticInvokeExpr) {
                                StaticInvokeExpr r = (StaticInvokeExpr)value;
                                // Inline typelattice methods.
                                if(r.getMethod().getDeclaringClass().equals(PtolemyUtilities.typeLatticeClass)) {
                                    PtolemyUtilities.inlineTypeLatticeMethods(body,
                                            unit, box, r, objectToTokenType);
                                }

                                // System.out.println("static invoking = " + r.getMethod());
                                SootMethod inlinee = (SootMethod)r.getMethod();
                                SootClass declaringClass = inlinee.getDeclaringClass();
                                if(SootUtilities.derivesFrom(declaringClass,
                                        PtolemyUtilities.tokenClass)) {
                                    declaringClass.setLibraryClass();
                                    if(!inlinee.isAbstract() && 
                                            !inlinee.isNative()) {
                                        //System.out.println("inlining");
                                        inlinee.retrieveActiveBody();
                                        // Then we know exactly what method will
                                        // be called, so inline it.
                                        SiteInliner.inlineSite(
                                                inlinee, (Stmt)unit, method);
                                        doneSomething = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Map entityFieldToTokenFieldToReplacementField = new HashMap();
        // Loop over all the classes.
        for(Iterator classes = Scene.v().getApplicationClasses().iterator();
            classes.hasNext();) {
            SootClass entityClass = (SootClass)classes.next();
      
            if(debug) System.out.println("creating replacement fields in Class = " + entityClass);
            // For every Token field of the actor, create new fields
            // that represent the fields of the token class in this actor.
            for(Iterator fields = entityClass.getFields().snapshotIterator();
                fields.hasNext();) {
                SootField field = (SootField)fields.next();
                if(!_isTokenType(field.getType())) {
                    continue;
                }
                RefType type;
                if(field.getType() instanceof RefType) {
                    type = (RefType)field.getType();
                } else if(field.getType() instanceof ArrayType) {
                    ArrayType arrayType = (ArrayType)field.getType();
                    if(arrayType.baseType instanceof RefType) {
                        type = (RefType)arrayType.baseType;
                    } else continue;
                } else continue;

                SootClass fieldClass = type.getSootClass();
                
                if(SootUtilities.derivesFrom(fieldClass, 
                        PtolemyUtilities.tokenClass)) {
                    Map tokenFieldToReplacementField = new HashMap();
                    entityFieldToTokenFieldToReplacementField.put(field, 
                            tokenFieldToReplacementField);
                    for(Iterator tokenFields = _getTokenClassFields(fieldClass).iterator();
                        tokenFields.hasNext();) {
                        SootField tokenField = (SootField)tokenFields.next();
                        // We need a type that is the same shape as the field, with 
                        // the same type as the field in the token.  This is complicated
                        // by the fact that both may be arraytypes.
                        Type replacementType = 
                            SootUtilities.createIsomorphicType(field.getType(), 
                                    tokenField.getType());
                        SootField replacementField = 
                            new SootField("_CG_" + field.getName() + tokenField.getName(),
                                    replacementType, field.getModifiers());
                        tokenFieldToReplacementField.put(tokenField, replacementField);
                        entityClass.addField(replacementField);
                    }
                }
            }
        }
        
        // Now do the actual replacement of remaining operations on token classes.
        // Loop over all the classes.
        for(Iterator classes = Scene.v().getApplicationClasses().iterator();
            classes.hasNext();) {
            SootClass entityClass = (SootClass)classes.next();
            if(debug) System.out.println("creating replacement locals in Class = " + entityClass);
            
            for(Iterator methods = entityClass.getMethods().iterator();
                methods.hasNext();) {
                SootMethod method = (SootMethod)methods.next();

                JimpleBody body = (JimpleBody)method.retrieveActiveBody();

                UnitGraph graph = new CompleteUnitGraph(body);
                MustAliasAnalysis aliasAnalysis = new MustAliasAnalysis(graph);

                System.out.println("creating replacement locals in method = " + method);

                // A map from a local variable that references a token and a field of that token class
                // to the local variable that will replace a fieldReference to that field based on the 
                // local.
                Map localToFieldToLocal = new HashMap();
                Map localToIsNullLocal = new HashMap();

                for(Iterator locals = body.getLocals().snapshotIterator();
                    locals.hasNext();) {
                    Local local = (Local)locals.next();
                    
                    RefType type;
                    if(local.getType() instanceof RefType) {
                        type = (RefType)local.getType();
                    } else if(local.getType() instanceof ArrayType) {
                        ArrayType arrayType = (ArrayType)local.getType();
                        if(arrayType.baseType instanceof RefType) {
                            type = (RefType)arrayType.baseType;
                        } else continue;
                    } else continue;
                    
                    SootClass localClass = type.getSootClass();
                    if(SootUtilities.derivesFrom(localClass, 
                            PtolemyUtilities.tokenClass)) {
                        if(debug) System.out.println("local = " + local);
                        if(debug) System.out.println("localClass = " + localClass);
                        
                        // Create a boolean value that tells us whether or
                        // not the token is null.  Initialize it to true.
                        Local isNullLocal = Jimple.v().newLocal(
                                local.getName() + "_isNull", BooleanType.v());
                        body.getLocals().add(isNullLocal);
                        localToIsNullLocal.put(local, isNullLocal);
                        body.getUnits().insertBefore(
                                Jimple.v().newAssignStmt(
                                        isNullLocal,
                                        IntConstant.v(1)),
                                body.getFirstNonIdentityStmt());

                        Map tokenFieldToReplacementLocal = new HashMap();
                        localToFieldToLocal.put(local,
                                tokenFieldToReplacementLocal);
                        for(Iterator tokenFields = _getTokenClassFields(localClass).iterator();
                            tokenFields.hasNext();) {
                            SootField tokenField = (SootField)tokenFields.next();
                            if(debug) System.out.println("tokenField = " + tokenField);
                            Type replacementType = SootUtilities.createIsomorphicType(local.getType(), 
                                    tokenField.getType());
                            Local replacementLocal = Jimple.v().newLocal(
                                    local.getName() + "_" + tokenField.getName(),
                                    replacementType);
                            body.getLocals().add(replacementLocal);
                            tokenFieldToReplacementLocal.put(tokenField, replacementLocal);
                        }
                    }
                }
                
                // Go back again and replace references to fields in the token with references
                // to local variables.
                for(Iterator units = body.getUnits().snapshotIterator();
                    units.hasNext();) {
                    Unit unit = (Unit)units.next();
                    System.out.println("unit = " + unit);
                    if(unit instanceof AssignStmt) {
                        AssignStmt stmt = (AssignStmt)unit;
                        Type assignmentType = stmt.getLeftOp().getType();
                        if(stmt.getLeftOp() instanceof Local &&
                                stmt.getRightOp() instanceof LengthExpr) {
                            System.out.println("handling as length expr");
                            LengthExpr lengthExpr = (LengthExpr)stmt.getRightOp();
                            Local baseLocal = (Local)lengthExpr.getOp();
                            System.out.println("operating on " + baseLocal);
                            
                            Map fieldToReplacementArrayLocal = 
                                (Map) localToFieldToLocal.get(baseLocal);
                            
                            if(fieldToReplacementArrayLocal == null) {
                                continue;
                            }
                            
                            // Get the length of a random one of the replacement fields.
                            SootField field = (SootField)fieldToReplacementArrayLocal.keySet().iterator().next();
                            System.out.println("replace with  " + fieldToReplacementArrayLocal.get(field));
                            lengthExpr.setOp((Local)
                                    fieldToReplacementArrayLocal.get(field));
                            System.out.println("unit now = " + unit);
  
                            //body.getUnits().remove(unit);   
                        } else if(_isTokenType(assignmentType)) {
                            if(false) {//stmt.getLeftOp() instanceof Local &&
                                //    stmt.getRightOp() instanceof NullConstant) {
                                System.out.println("handling as local-null assign");
                                
                                Map fieldToReplacementLocal = 
                                    (Map) localToFieldToLocal.get(stmt.getLeftOp());
                                                                                
                                if(fieldToReplacementLocal == null) {
                                    continue;
                                }
                               
                                body.getUnits().insertBefore(
                                        Jimple.v().newAssignStmt(
                                                (Local)localToIsNullLocal.get(stmt.getLeftOp()),
                                                IntConstant.v(1)),
                                        unit);
                                System.out.println("local = " + stmt.getLeftOp());
                                for(Iterator tokenFields = fieldToReplacementLocal.keySet().iterator();
                                    tokenFields.hasNext();) {
                                    SootField tokenField = (SootField)tokenFields.next();
                                    System.out.println("tokenField = " + tokenField);
                                    Local replacementLocal = (Local)
                                        fieldToReplacementLocal.get(tokenField);
                                    // FIXME: ??
                                    body.getUnits().insertBefore(
                                            Jimple.v().newAssignStmt(
                                                    replacementLocal,
                                                    IntConstant.v(77)),
                                            unit);
                                }
                            } else if(stmt.getLeftOp() instanceof Local &&
                                    (stmt.getRightOp() instanceof Local ||
                                     stmt.getRightOp() instanceof Constant)) {
                                System.out.println("handling as local-immediate assign");
                                _handleImmediateAssignment(body, unit, 
                                        localToFieldToLocal, localToIsNullLocal,
                                        stmt.getLeftOp(), stmt.getRightOp());
                              
                                   // The below code handles assignment to token fields.
                                   /*List aliasList = MustAliasAnalysis.getAliasesOfBefore(
                                            stmt.getLeftOp(), unit);
                                    for(Iterator aliases = aliasList.iterator();
                                        aliases.hasNext();) {
                                        Object alias = aliases.next();
                                        if(alias instanceof Local) {
                                            Map fieldToReplacementLocal = 
                                                (Map)localToFieldToLocal.get(stmt.getLeftOp());
                                
                                            for(Iterator tokenFields =
                                                    fieldToReplacementLocal.keySet().iterator();
                                                tokenFields.hasNext();) {
                                                SootField tokenField = (SootField)tokenFields.next();
                                                System.out.println("tokenField = " + tokenField);
                                                Local replacementArrayLocal = (Local)
                                                    fieldToReplacementArrayLocal.get(tokenField);
                                                Local replacementLocal = (Local)
                                                    fieldToReplacementLocal.get(tokenField);
                                   */
                         
                                   //}
                            } else if(stmt.getLeftOp() instanceof Local &&
                                    stmt.getRightOp() instanceof CastExpr) {
                                System.out.println("handling as local cast");
                                Value rightLocal = ((CastExpr)stmt.getRightOp()).getOp();
                                
                                _handleImmediateAssignment(body, unit, 
                                        localToFieldToLocal, localToIsNullLocal,
                                        stmt.getLeftOp(), rightLocal);
                                /*
                                Value rightLocal = ((CastExpr)stmt.getRightOp()).getOp();
                                // We have an assignment from one local token to another.
                                Map fieldToReplacementLeftLocal = 
                                    (Map)localToFieldToLocal.get(stmt.getLeftOp());
                                Map fieldToReplacementRightLocal = 
                                    (Map)localToFieldToLocal.get(rightLocal);
                                
                                // We have an assignment from one local token to another.
                               if(fieldToReplacementLeftLocal != null &&
                                       fieldToReplacementRightLocal != null) {
                                   body.getUnits().insertBefore(
                                           Jimple.v().newAssignStmt(
                                                   (Local)localToIsNullLocal.get(stmt.getLeftOp()),
                                                   (Local)localToIsNullLocal.get(rightLocal)),
                                           unit);
                                   System.out.println("local = " + stmt.getLeftOp());
                                   for(Iterator tokenFields = fieldToReplacementLeftLocal.keySet().iterator();
                                       tokenFields.hasNext();) {
                                       SootField tokenField = (SootField)tokenFields.next();
                                       System.out.println("tokenField = " + tokenField);
                                       Local replacementLeftLocal = (Local)
                                           fieldToReplacementLeftLocal.get(tokenField);
                                       Local replacementRightLocal = (Local)
                                           fieldToReplacementRightLocal.get(tokenField);
                                       body.getUnits().insertBefore(
                                               Jimple.v().newAssignStmt(
                                                       replacementLeftLocal,
                                                       replacementRightLocal),
                                               unit);
                                   }
                                   }*/ 
                            } else if(stmt.getLeftOp() instanceof FieldRef &&
                                    stmt.getRightOp() instanceof Local) {
                                System.out.println("handling as assignment to Field");
                                SootField field = ((FieldRef)stmt.getLeftOp()).getField();
                                Map fieldToReplacementField = 
                                    (Map) entityFieldToTokenFieldToReplacementField.get(field);
                                Map fieldToReplacementLocal = 
                                    (Map) localToFieldToLocal.get(stmt.getRightOp());
                                                           
                                if(fieldToReplacementLocal == null ||
                                        fieldToReplacementField == null) {
                                    continue;
                                }

                                // FIXME isNull field?
                                for(Iterator tokenFields = fieldToReplacementField.keySet().iterator();
                                    tokenFields.hasNext();) {
                                    SootField tokenField = (SootField)tokenFields.next();
                                    
                                    SootField replacementField = (SootField)
                                        fieldToReplacementField.get(tokenField);
                                    Local replacementLocal = (Local)
                                        fieldToReplacementLocal.get(tokenField);
                                    FieldRef fieldRef;
                                    if(stmt.getLeftOp() instanceof InstanceFieldRef) {
                                        Local base = (Local)((InstanceFieldRef)stmt.getLeftOp()).getBase();
                                        fieldRef = Jimple.v().newInstanceFieldRef(base, 
                                                replacementField);
                                    } else {
                                        fieldRef = Jimple.v().newStaticFieldRef(
                                                replacementField);
                                    }
                                                                  
                                    body.getUnits().insertBefore(
                                            Jimple.v().newAssignStmt(
                                                    fieldRef,
                                                    replacementLocal),
                                            unit);
                                }
                                //body.getUnits().remove(unit);   
                            } else if(stmt.getLeftOp() instanceof Local &&
                                    stmt.getRightOp() instanceof FieldRef) {
                                System.out.println("handling as assignment from Field");
                                Map fieldToReplacementLocal = 
                                    (Map) localToFieldToLocal.get(stmt.getLeftOp());
                                SootField field = ((FieldRef)stmt.getRightOp()).getField();
                                Map fieldToReplacementField = 
                                    (Map) entityFieldToTokenFieldToReplacementField.get(field);
                                                          
                                if(fieldToReplacementLocal == null ||
                                        fieldToReplacementField == null) {
                                    continue;
                                }
                               
                                // FIXME properly handle isNull field?
                                body.getUnits().insertBefore(
                                        Jimple.v().newAssignStmt(
                                                (Local)localToIsNullLocal.get(stmt.getLeftOp()),
                                                IntConstant.v(0)),
                                        unit);
                                System.out.println("local = " + stmt.getLeftOp());
                                for(Iterator tokenFields = fieldToReplacementField.keySet().iterator();
                                    tokenFields.hasNext();) {
                                    SootField tokenField = (SootField)tokenFields.next();
                                    System.out.println("tokenField = " + tokenField);
                                    SootField replacementField = (SootField)
                                        fieldToReplacementField.get(tokenField);
                                    Local replacementLocal = (Local)
                                        fieldToReplacementLocal.get(tokenField);
                                    FieldRef fieldRef;
                                    if(stmt.getRightOp() instanceof InstanceFieldRef) {
                                        Local base = (Local)((InstanceFieldRef)stmt.getRightOp()).getBase();
                                        fieldRef = Jimple.v().newInstanceFieldRef(base, 
                                                replacementField);
                                    } else {
                                        fieldRef = Jimple.v().newStaticFieldRef(
                                                replacementField);
                                    }

                                    System.out.println("replacementLocal = " + replacementLocal);
                                    
                                    body.getUnits().insertBefore(
                                            Jimple.v().newAssignStmt(
                                                    replacementLocal, 
                                                    fieldRef),
                                            unit);
                                }
                                //body.getUnits().remove(unit);   
                            } else if(stmt.getLeftOp() instanceof ArrayRef &&
                                    stmt.getRightOp() instanceof Local) {
                                System.out.println("handling as assignment to Array");
                                ArrayRef arrayRef = (ArrayRef)stmt.getLeftOp();
                                Local baseLocal = (Local) arrayRef.getBase();
                                Map fieldToReplacementArrayLocal = 
                                    (Map) localToFieldToLocal.get(baseLocal);
                                Map fieldToReplacementLocal = 
                                    (Map) localToFieldToLocal.get(stmt.getRightOp());
                                                          
                                if(fieldToReplacementLocal == null ||
                                        fieldToReplacementArrayLocal == null) {
                                    continue;
                                }
                               
                                body.getUnits().insertBefore(
                                        Jimple.v().newAssignStmt(
                                                (Local)localToIsNullLocal.get(baseLocal),
                                                (Local)localToIsNullLocal.get(stmt.getRightOp())),
                                        unit);
                                System.out.println("local = " + stmt.getLeftOp());
                                for(Iterator tokenFields = fieldToReplacementLocal.keySet().iterator();
                                    tokenFields.hasNext();) {
                                    SootField tokenField = (SootField)tokenFields.next();
                                    System.out.println("tokenField = " + tokenField);
                                    Local replacementArrayLocal = (Local)
                                        fieldToReplacementArrayLocal.get(tokenField);
                                    Local replacementLocal = (Local)
                                        fieldToReplacementLocal.get(tokenField);
                                    
                                    body.getUnits().insertBefore(
                                            Jimple.v().newAssignStmt(
                                                    Jimple.v().newArrayRef(
                                                            replacementArrayLocal,
                                                            arrayRef.getIndex()),
                                                    replacementLocal),
                                            unit);
                                }
                                //body.getUnits().remove(unit);   
                            } else if(stmt.getLeftOp() instanceof Local &&
                                    stmt.getRightOp() instanceof ArrayRef) {
                                System.out.println("handling as assignment from Array");
                                ArrayRef arrayRef = (ArrayRef)stmt.getRightOp();
                                Map fieldToReplacementLocal = 
                                    (Map) localToFieldToLocal.get(stmt.getLeftOp());
                                Local baseLocal = (Local)arrayRef.getBase();
                                Map fieldToReplacementArrayLocal = 
                                    (Map) localToFieldToLocal.get(baseLocal);
                                                          
                                if(fieldToReplacementLocal == null ||
                                        fieldToReplacementArrayLocal == null) {
                                    continue;
                                }
                               
                                body.getUnits().insertBefore(
                                        Jimple.v().newAssignStmt(
                                                (Local)localToIsNullLocal.get(stmt.getLeftOp()),
                                                (Local)localToIsNullLocal.get(baseLocal)),
                                        unit);
                                System.out.println("local = " + stmt.getLeftOp());
                                for(Iterator tokenFields = fieldToReplacementLocal.keySet().iterator();
                                    tokenFields.hasNext();) {
                                    SootField tokenField = (SootField)tokenFields.next();
                                    System.out.println("tokenField = " + tokenField);
                                    Local replacementArrayLocal = (Local)
                                        fieldToReplacementArrayLocal.get(tokenField);
                                    Local replacementLocal = (Local)
                                        fieldToReplacementLocal.get(tokenField);
                                    System.out.println("replacementLocal = " + replacementLocal);
                                    System.out.println("replacementArrayLocal = " + replacementArrayLocal);
  
                                    body.getUnits().insertBefore(
                                            Jimple.v().newAssignStmt(
                                                    replacementLocal, 
                                                    Jimple.v().newArrayRef(
                                                            replacementArrayLocal,
                                                            arrayRef.getIndex())),
                                            unit);
                                }
                                //body.getUnits().remove(unit);   
                            } else if(stmt.getLeftOp() instanceof Local &&
                                    stmt.getRightOp() instanceof NewArrayExpr) {
                                System.out.println("handling as new array object");
                                NewArrayExpr newExpr = (NewArrayExpr)stmt.getRightOp();
                                // We have an assignment from one local token to another.
                                Map map = (Map)localToFieldToLocal.get(stmt.getLeftOp());
                                if(map != null) {
                                    Local isNullLocal = (Local)
                                        localToIsNullLocal.get(stmt.getLeftOp());
                                    body.getUnits().insertBefore(
                                            Jimple.v().newAssignStmt(
                                                    isNullLocal,
                                                    IntConstant.v(0)),
                                            unit);
                                    for(Iterator tokenFields = map.keySet().iterator();
                                        tokenFields.hasNext();) {
                                        SootField tokenField = (SootField)tokenFields.next();
                                        Local replacementLocal = (Local)
                                            map.get(tokenField);
                                        
                                        Type replacementType = 
                                            SootUtilities.createIsomorphicType(newExpr.getBaseType(), 
                                            tokenField.getType());
                                        
                                        // Initialize fields?
                                        body.getUnits().insertBefore(
                                                Jimple.v().newAssignStmt(
                                                        replacementLocal,
                                                        Jimple.v().newNewArrayExpr(
                                                                replacementType,
                                                                newExpr.getSize())),
                                                unit);
                                    }
                                }
                            } else if(stmt.getLeftOp() instanceof Local &&
                                    stmt.getRightOp() instanceof NewExpr) {
                                System.out.println("handling as new object");
                                NewExpr newExpr = (NewExpr)stmt.getRightOp();
                                // We have an assignment from one local token to another.
                                Map map = (Map)localToFieldToLocal.get(stmt.getLeftOp());
                                if(map != null) {
                                    Local isNullLocal = (Local)
                                        localToIsNullLocal.get(stmt.getLeftOp());
                                    body.getUnits().insertBefore(
                                            Jimple.v().newAssignStmt(
                                                    isNullLocal,
                                                    IntConstant.v(0)),
                                            unit);
                                    for(Iterator tokenFields = map.keySet().iterator();
                                        tokenFields.hasNext();) {
                                        SootField tokenField = (SootField)tokenFields.next();
                                        Local replacementLocal = (Local)
                                            map.get(tokenField);
                                        // Initialize fields?
                                    }
                                }
                            } /*else if(stmt.getLeftOp() instanceof Local &&
                                      stmt.getRightOp() instanceof InvokeExpr) {
                                System.out.println("handling as method call.");
                                // We have an assignment from one local token to another.
                                Map map = (Map)localToFieldToLocal.get(stmt.getLeftOp());
                                if(map != null) {
                                    Local isNullLocal = (Local)
                                        localToIsNullLocal.get(stmt.getLeftOp());
                                    body.getUnits().insertAfter(
                                            Jimple.v().newAssignStmt(
                                                    isNullLocal,
                                                    IntConstant.v(0)),
                                            unit);
                                    for(Iterator tokenFields = map.keySet().iterator();
                                        tokenFields.hasNext();) {
                                        SootField tokenField = (SootField)tokenFields.next();
                                        Local replacementLocal = (Local)
                                            map.get(tokenField);
                                        // Initialize fields?
                                        body.getUnits().insertAfter(
                                                Jimple.v().newAssignStmt(
                                                        replacementLocal,
                                                        ),
                                                unit);
                                    }
                                }
                                }*/
                        }
                    }
                    for(Iterator boxes = unit.getUseAndDefBoxes().iterator();
                        boxes.hasNext();) {
                        ValueBox box = (ValueBox)boxes.next();
                        Value value = box.getValue();
                        if(value instanceof BinopExpr) {
                            BinopExpr expr = (BinopExpr) value;
                            boolean op1IsToken = _isTokenType(expr.getOp1().getType());
                            boolean op2IsToken = _isTokenType(expr.getOp2().getType());
                            if(op1IsToken && op2IsToken) {
                                throw new RuntimeException("Unable to handle expression" + 
                                        " of two token types: " + unit);
                            } else if(op1IsToken && expr.getOp2().getType().equals(NullType.v())) {
                                Local isNullLocal = (Local)
                                    localToIsNullLocal.get(expr.getOp1());
                                if(expr instanceof EqExpr) {
                                    box.setValue(Jimple.v().newEqExpr(
                                            isNullLocal,
                                            IntConstant.v(1)));
                                } else if(expr instanceof NeExpr) {
                                    box.setValue(Jimple.v().newEqExpr(
                                            isNullLocal,
                                            IntConstant.v(0))); 
                                }
                            } else if(op2IsToken && expr.getOp1().getType().equals(NullType.v())) {
                                Local isNullLocal = (Local)
                                    localToIsNullLocal.get(expr.getOp2());
                                if(expr instanceof EqExpr) {
                                    box.setValue(Jimple.v().newEqExpr(
                                            isNullLocal,
                                            IntConstant.v(1)));
                                } else if(expr instanceof NeExpr) {
                                    box.setValue(Jimple.v().newEqExpr(
                                            isNullLocal,
                                            IntConstant.v(0))); 
                                }
                            }
                        }
                        if(value instanceof InstanceFieldRef) {
                            //System.out.println("is Instance FieldRef");
                            InstanceFieldRef r = (InstanceFieldRef)value;
                            SootField field = r.getField();  
                            if(r.getBase().getType() instanceof RefType) {
                                RefType type = (RefType)r.getBase().getType();
                                //System.out.println("BaseType = " + type);
                                if(SootUtilities.derivesFrom(type.getSootClass(), 
                                        PtolemyUtilities.tokenClass)) {
                                    // System.out.println("handling " + unit + " token operation");
                                    
                                    // We have a reference to a field of a token class.
                                    Local baseLocal = (Local)r.getBase();
                                    Local instanceLocal = _getInstanceLocal(body, baseLocal,
                                            field, localToFieldToLocal);
                                    box.setValue(instanceLocal);
                                } 
                            }
                        }                        
                        
                        if(value instanceof NewExpr) {
                            NewExpr newExpr = (NewExpr)value;
                            RefType type = newExpr.getBaseType();
                            if(SootUtilities.derivesFrom(type.getSootClass(), 
                                    PtolemyUtilities.tokenClass)) {
                                // remove
                                box.setValue(NullConstant.v());
                            }
                        } 
                        if(value instanceof SpecialInvokeExpr) {
                            SpecialInvokeExpr expr = (SpecialInvokeExpr)value;
                            if(SootUtilities.derivesFrom(expr.getMethod().getDeclaringClass(),
                                    PtolemyUtilities.tokenClass) &&
                               expr.getMethod().getName().equals("<init>")) {
                                // remove
                                body.getUnits().remove(unit);
                            }
                        }                        
                    }                    
                }
                CastAndInstanceofEliminator.v().transform(body, phaseName + ".cie", "");
                CopyPropagator.v().transform(body, phaseName + ".cp", "");
                ConstantPropagatorAndFolder.v().transform(body, phaseName + ".cpf", "");
                ConditionalBranchFolder.v().transform(body, phaseName + ".cbf", "");
                UnreachableCodeEliminator.v().transform(body, phaseName + ".uce", "");

            }
        }
    }

    public static boolean _isTokenType(Type type) {
        RefType refType;
        if(type instanceof RefType) {
            refType = (RefType)type;
        } else if(type instanceof ArrayType) {
            ArrayType arrayType = (ArrayType)type;
            if(arrayType.baseType instanceof RefType) {
                refType = (RefType)arrayType.baseType;
            } else return false;
        } else return false;
        return SootUtilities.derivesFrom(refType.getSootClass(),
                PtolemyUtilities.tokenClass);
    }

    public static Local _getInstanceLocal(Body body, Local baseLocal,
            SootField field, Map localToFieldToLocal) {
        // Get the map for that local from a field of the local's
        // class to the local variable that replaces it.
        Map fieldToLocal = (Map)localToFieldToLocal.get(baseLocal);
        if(fieldToLocal == null) {
            System.out.println("creating new fieldToLocal for " + baseLocal);
            // If we have not seen this local get, then 
            // create a new map.
            fieldToLocal = new HashMap();
            localToFieldToLocal.put(baseLocal, fieldToLocal);
        }
        Local instanceLocal = (Local)fieldToLocal.get(field);
        if(instanceLocal == null) {
            System.out.println("creating new instanceLocal for " + baseLocal);
            // If we have not referenced this particular field
            // then create a new local.
            instanceLocal = Jimple.v().newLocal(
                    baseLocal.getName() + "_" + field.getName(),
                    field.getType());
            body.getLocals().add(instanceLocal);
            fieldToLocal.put(field, instanceLocal);
        }
        return instanceLocal;
    }

    public static Value _getSubFieldValue(SootClass entityClass, Body body,
            Value baseValue, SootField tokenField, Map localToFieldToLocal) {
        Value returnValue;
        if(baseValue instanceof Local) {
           returnValue = _getInstanceLocal(body, (Local)baseValue,
                   tokenField, localToFieldToLocal);
        } else if(baseValue instanceof FieldRef) {
            SootField field = ((FieldRef)baseValue).getField();
            SootField replacementField = 
                entityClass.getFieldByName("_CG_" + field.getName()
                        + tokenField.getName());
            if(baseValue instanceof InstanceFieldRef) {
                returnValue = Jimple.v().newInstanceFieldRef(
                        ((InstanceFieldRef)baseValue).getBase(), replacementField);
            } else {
                returnValue = Jimple.v().newStaticFieldRef(
                        replacementField);
            }
        } else {
            throw new RuntimeException("Unknown value type for subfield: " + baseValue);
        }
        return returnValue;
    }

    public static List _getTokenClassFields(SootClass tokenClass) {
        List list;
        if(tokenClass.equals(PtolemyUtilities.tokenClass)) {
            list = new LinkedList();
        } else {
            list = 
                _getTokenClassFields(tokenClass.getSuperclass());
        }
        for(Iterator fields = tokenClass.getFields().iterator();
            fields.hasNext();) {
            SootField field = (SootField)fields.next();
            int modifiers = field.getModifiers();
            if(!Modifier.isStatic(modifiers) && !Modifier.isFinal(modifiers)) {
                list.add(field);
            }
        }
        return list;
    }

    public static void _handleImmediateAssignment(JimpleBody body, Unit unit, 
            Map localToFieldToLocal, Map localToIsNullLocal,
            Value leftValue, Value rightValue) {
                
        Map fieldToReplacementLeftLocal = 
            (Map)localToFieldToLocal.get(leftValue);
        
        if(rightValue instanceof NullConstant) {
            if(fieldToReplacementLeftLocal != null) {
                
                               
                body.getUnits().insertBefore(
                        Jimple.v().newAssignStmt(
                                (Local)localToIsNullLocal.get(leftValue),
                                IntConstant.v(1)),
                        unit);
                System.out.println("local = " + leftValue);
                for(Iterator tokenFields = fieldToReplacementLeftLocal.keySet().iterator();
                    tokenFields.hasNext();) {
                    SootField tokenField = (SootField)tokenFields.next();
                    System.out.println("tokenField = " + tokenField);
                    Local replacementLocal = (Local)
                        fieldToReplacementLeftLocal.get(tokenField);
                    // FIXME: ??
                    body.getUnits().insertBefore(
                            Jimple.v().newAssignStmt(
                                    replacementLocal,
                                    IntConstant.v(77)),
                            unit);
                }
            }
        } else {
            Map fieldToReplacementRightLocal = 
                (Map)localToFieldToLocal.get(rightValue);
       
            // We have an assignment from one local token to another.
            if(fieldToReplacementLeftLocal != null &&
                    fieldToReplacementRightLocal != null) {
                body.getUnits().insertBefore(
                        Jimple.v().newAssignStmt(
                                (Local)localToIsNullLocal.get(leftValue),
                                (Local)localToIsNullLocal.get(rightValue)),
                        unit);
                System.out.println("local = " + leftValue);
                for(Iterator tokenFields = fieldToReplacementLeftLocal.keySet().iterator();
                    tokenFields.hasNext();) {
                    SootField tokenField = (SootField)tokenFields.next();
                    System.out.println("tokenField = " + tokenField);
                    Local replacementLeftLocal = (Local)
                        fieldToReplacementLeftLocal.get(tokenField);
                    Local replacementRightLocal = (Local)
                        fieldToReplacementRightLocal.get(tokenField);
                    System.out.println("replacementLeftLocal = " + replacementLeftLocal);
                    System.out.println("replacementRightLocal = " + replacementRightLocal);
                    body.getUnits().insertBefore(
                            Jimple.v().newAssignStmt(
                                    replacementLeftLocal,
                                    replacementRightLocal),
                            unit);
                }
            }
        }
        
    }
    private CompositeActor _model;
}














