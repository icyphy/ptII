/* A transformer that specializes token types in an actor.

Copyright (c) 2001-2005 The Regents of the University of California.
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
*/
package ptolemy.copernicus.java;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import ptolemy.actor.CompositeActor;
import ptolemy.copernicus.kernel.PtolemyUtilities;
import ptolemy.copernicus.kernel.SootUtilities;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.Entity;
import soot.Body;
import soot.FastHierarchy;
import soot.HasPhaseOptions;
import soot.Hierarchy;
import soot.Local;
import soot.PhaseOptions;
import soot.RefType;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.FieldRef;
import soot.jimple.Jimple;
import soot.jimple.NewArrayExpr;
import soot.jimple.toolkits.scalar.LocalNameStandardizer;
import soot.jimple.toolkits.typing.TypeAssigner;
import soot.toolkits.scalar.LocalSplitter;


//////////////////////////////////////////////////////////////////////////
//// TypeSpecializer

/**
   A transformer that modifies each class using the token types from
   ports.  In particular, this class creates constraints in the same
   fashion as the Ptolemy II type system and solves those constraints.
   The resulting solution should correspond to valid Java types which are
   more specific (in the Ptolemy II sense) than the original Java types.
   The code is then transformed to use these more specific types.

   <p> This transformer is necessary because there are some token types
   that we want to make more specific, but that don't directly depend on
   the the types of a port.  This transformation enables the token unboxing
   performed by the TokenToNativeTransformer

   @author Stephen Neuendorffer
   @version $Id$
   @since Ptolemy II 4.0
   @Pt.ProposedRating Red (cxh)
   @Pt.AcceptedRating Red (cxh)
*/
public class TypeSpecializer extends SceneTransformer implements HasPhaseOptions {
    /** Construct a new transformer
     */
    private TypeSpecializer(CompositeActor model) {
        _model = model;
    }

    /** Return an instance of this transformer that will operate on
     *  the given model.  The model is assumed to already have been
     *  properly initialized so that resolved types and other static
     *  properties of the model can be inspected.
     */
    public static TypeSpecializer v(CompositeActor model) {
        return new TypeSpecializer(model);
    }

    public String getPhaseName() {
        return "";
    }

    public String getDefaultOptions() {
        return "";
    }

    public String getDeclaredOptions() {
        return "debug";
    }

    protected void internalTransform(String phaseName, Map options) {
        int localCount = 0;
        System.out.println("TypeSpecializer.internalTransform(" + phaseName
                + ", " + options + ")");

        boolean debug = PhaseOptions.getBoolean(options, "debug");

        Scene.v().setActiveHierarchy(new Hierarchy());
        Scene.v().setFastHierarchy(new FastHierarchy());

        Hierarchy h = Scene.v().getActiveHierarchy();

        for (Iterator entities = _model.deepEntityList().iterator();
             entities.hasNext();) {
            Entity entity = (Entity) entities.next();
            String className = ModelTransformer.getInstanceClassName(entity,
                    options);
            SootClass theClass = Scene.v().loadClassAndSupport(className);

            Set unsafeLocalSet = new HashSet();
            TypeSpecializerAnalysis analysis = new TypeSpecializerAnalysis(theClass,
                    unsafeLocalSet);
            specializeTypes(debug, theClass, unsafeLocalSet, analysis);
        }
    }

    /** Specialize all token types that appear in the given class,
     *  based on the given analysis. Return a map from locals and
     *  fields in the class to their new specific Ptolemy type.
     *  Exclude locals in the given set from the typing algorithm.
     */
    public static Map specializeTypes(boolean debug, SootClass theClass,
            Set unsafeLocals, TypeSpecializerAnalysis typeAnalysis) {
        if (debug) {
            System.out.println("updating types for " + theClass);
        }

        Map map = new HashMap();

        // Loop through all the methods and update types of locals.
        // Note that unlike the types of fields, the types of locals
        // are not stored in the bytecode, hence we don't have to insert
        // casts to please the bytecode verifier.   On the other hand,
        // this information will be lost once we actually write to bytecode.
        // We are updating it because further passes of code generation
        // use this information (for example, when converting
        // token types (like IntToken) to native types (like int).
        for (Iterator methods = theClass.getMethods().iterator();
             methods.hasNext();) {
            SootMethod method = (SootMethod) methods.next();

            if (debug) {
                System.out.println("updating types for " + method);
            }

            Body body = method.retrieveActiveBody();

            for (Iterator units = body.getUnits().snapshotIterator();
                 units.hasNext();) {
                Unit unit = (Unit) units.next();

                //System.out.println("unit = " + unit);
                Iterator boxes = unit.getUseBoxes().iterator();

                while (boxes.hasNext()) {
                    ValueBox box = (ValueBox) boxes.next();
                    Value value = box.getValue();

                    // Replace Array creations with a more specific
                    // type, if possible.
                    if (box.getValue() instanceof NewArrayExpr) {
                        NewArrayExpr newArrayExpr = (NewArrayExpr) box.getValue();

                        if (debug) {
                            System.out.println("newArrayExpr = " + newArrayExpr);
                        }

                        Type baseType = newArrayExpr.getBaseType();
                        Type newType = typeAnalysis.getSpecializedSootType(newArrayExpr);

                        if ((newType != null) && !newType.equals(baseType)) {
                            if (debug) {
                                System.out.println("replacing with " + newType);
                            }

                            box.setValue(Jimple.v().newNewArrayExpr(newType,
                                                 newArrayExpr.getSize()));
                        }
                    }
                }

                // Ignore anything that isn't an assignment.
                if (!(unit instanceof AssignStmt)) {
                    continue;
                }

                AssignStmt assignStmt = (AssignStmt) unit;

                // Ignore anything that isn't an assignment to a field.
                if (!(assignStmt.getLeftOp() instanceof FieldRef)) {
                    continue;
                }

                if (!PtolemyUtilities.isTokenType(assignStmt.getLeftOp()
                            .getType())) {
                    continue;
                }

                if (debug) {
                    System.out.println("checking assignment " + assignStmt);
                }

                // FIXME: We need to figure out a way to insert casts where appropriate.
                // See RampFiringLimitSDF
                //                 ptolemy.data.type.Type leftType, rightType;
                //                 leftType = _getReplacementTokenType(
                //                         assignStmt.getLeftOp(), typeAnalysis);
                //                 rightType = _getReplacementTokenType(
                //                         assignStmt.getRightOp(), typeAnalysis);
                //                 if (leftType != null && rightType != null && !leftType.equals(rightType)) {
                //                     if (debug) System.out.println("inserting conversion: leftType = " +
                //                             leftType + ", rightType = " + rightType);
                //                     // insert a call to convert(), and a cast.
                //                     FieldRef ref = (FieldRef)assignStmt.getLeftOp();
                //                     SootField field = ref.getField();
                //                     Type newType =
                //                         typeAnalysis.getSpecializedSootType(field);
                //                     Local tempLocal =
                //                         Jimple.v().newLocal("fieldUpdateLocal", newType);
                //                     body.getLocals().add(tempLocal);
                //                     Local tokenLocal =
                //                         Jimple.v().newLocal("tokenLocal", PtolemyUtilities.tokenType);
                //                     body.getLocals().add(tokenLocal);
                //                     Local typeLocal =
                //                         PtolemyUtilities.buildConstantTypeLocal(body, unit, leftType);
                //                     body.getUnits().insertBefore(
                //                             Jimple.v().newAssignStmt(tokenLocal,
                //                                     Jimple.v().newVirtualInvokeExpr(
                //                                             typeLocal,
                //                                             PtolemyUtilities.typeConvertMethod,
                //                                             assignStmt.getRightOp())),
                //                             unit);
                //                     body.getUnits().insertBefore(
                //                             Jimple.v().newAssignStmt(tempLocal,
                //                                     Jimple.v().newCastExpr(
                //                                             tokenLocal,
                //                                             newType)),
                //                             unit);
                //                     assignStmt.setRightOp(tempLocal);
                //                 } else {
                FieldRef ref = (FieldRef) assignStmt.getLeftOp();
                SootField field = ref.getField();

                Type type = field.getType();

                // Things that aren't token types are ignored.
                // Things that are already the same type are ignored.
                Type newType = typeAnalysis.getSpecializedSootType(field);

                if ((newType != null) && !newType.equals(type)) {
                    if (debug) {
                        System.out.println("inserting cast");
                    }

                    Local tempLocal = Jimple.v().newLocal("fieldUpdateLocal",
                            newType);
                    body.getLocals().add(tempLocal);
                    body.getUnits().insertBefore(Jimple.v().newAssignStmt(tempLocal,
                                                         Jimple.v().newCastExpr(assignStmt.getRightOp(),
                                                                 newType)), unit);
                    assignStmt.setRightOp(tempLocal);
                }

                //     }
            }
        }

        // Loop through all the fields and update the types.
        for (Iterator fields = theClass.getFields().iterator();
             fields.hasNext();) {
            SootField field = (SootField) fields.next();

            if (debug) {
                System.out.println("updating types for " + field);
            }

            Type baseType = field.getType();
            RefType refType = PtolemyUtilities.getBaseTokenType(baseType);

            if ((refType != null)
                    && SootUtilities.derivesFrom(
                            refType.getSootClass(),
                            PtolemyUtilities.tokenClass)) {
                Type type = typeAnalysis.getSpecializedSootType(field);

                if (debug) {
                    System.out.println("replacing with " + type);
                }

                field.setType(type);

                // Update the type tag.
                // FIXME: Correct?
                ptolemy.data.type.Type specializedType = typeAnalysis
                    .getSpecializedType(field);

                if ((specializedType != BaseType.UNKNOWN)
                        && (specializedType != BaseType.GENERAL)
                        && specializedType.isInstantiable()) {
                    if (debug) {
                        System.out.println("updating type tag of " + field
                                + " to " + typeAnalysis.getSpecializedType(field));
                    }

                    field.removeTag("_CGType");
                    field.addTag(new TypeTag(typeAnalysis.getSpecializedType(
                                                     field)));
                }

                map.put(field, typeAnalysis.getSpecializedType(field));
            }
        }

        for (Iterator methods = theClass.getMethods().iterator();
             methods.hasNext();) {
            SootMethod method = (SootMethod) methods.next();

            if (debug) {
                System.out.println("updating types for " + method);
            }

            Body body = method.retrieveActiveBody();

            // First split local variables that are used in
            // multiple places.
            LocalSplitter.v().transform(body, "ls");

            // We may have locals with the same name.  Rename them.
            LocalNameStandardizer.v().transform(body, "lns");

            // Assign types to local variables... This types
            // everything that isn't a token type.
            TypeAssigner.v().transform(body, "ta");
        }

        return map;
    }

    private static ptolemy.data.type.Type _getReplacementTokenType(
            Value value, TypeSpecializerAnalysis typeAnalysis) {
        if (value instanceof FieldRef) {
            FieldRef ref = (FieldRef) value;
            SootField field = ref.getField();
            return typeAnalysis.getSpecializedType(field);
        } else if (value instanceof Local) {
            Local local = (Local) value;
            return typeAnalysis.getSpecializedType(local);

            //   } else if (value.getType().equals(NullType.v())) {
            //             return tokenClass
        } else {
            return null;

            //throw new RuntimeException("Unrecognized value:" + value);
        }
    }

    private CompositeActor _model;
}
