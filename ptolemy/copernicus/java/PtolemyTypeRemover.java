/* A transformer that unboxes tokens

 Copyright (c) 2006 The Regents of the University of California.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ptolemy.actor.CompositeActor;
import ptolemy.copernicus.kernel.PtolemyUtilities;
import ptolemy.copernicus.kernel.SootUtilities;
import ptolemy.data.type.Typeable;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import soot.ArrayType;
import soot.Body;
import soot.BooleanType;
import soot.ByteType;
import soot.DoubleType;
import soot.FastHierarchy;
import soot.FloatType;
import soot.HasPhaseOptions;
import soot.Hierarchy;
import soot.IntType;
import soot.Local;
import soot.LongType;
import soot.Modifier;
import soot.NullType;
import soot.PhaseOptions;
import soot.RefType;
import soot.Scene;
import soot.SceneTransformer;
import soot.ShortType;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.BinopExpr;
import soot.jimple.CastExpr;
import soot.jimple.Constant;
import soot.jimple.DefinitionStmt;
import soot.jimple.DoubleConstant;
import soot.jimple.EqExpr;
import soot.jimple.FieldRef;
import soot.jimple.FloatConstant;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.IntConstant;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.LengthExpr;
import soot.jimple.LongConstant;
import soot.jimple.NeExpr;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.NewMultiArrayExpr;
import soot.jimple.NullConstant;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticFieldRef;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.toolkits.invoke.SiteInliner;
import soot.jimple.toolkits.scalar.ConditionalBranchFolder;
import soot.jimple.toolkits.scalar.ConstantPropagatorAndFolder;
import soot.jimple.toolkits.scalar.CopyPropagator;
import soot.jimple.toolkits.scalar.DeadAssignmentEliminator;
import soot.jimple.toolkits.scalar.LocalNameStandardizer;
import soot.jimple.toolkits.scalar.UnreachableCodeEliminator;
import soot.jimple.toolkits.typing.TypeAssigner;
import soot.toolkits.graph.CompleteUnitGraph;
import soot.toolkits.scalar.LocalSplitter;
import soot.toolkits.scalar.SimpleLocalDefs;
import soot.toolkits.scalar.SimpleLocalUses;
import soot.toolkits.scalar.UnitValueBoxPair;
import soot.toolkits.scalar.UnusedLocalEliminator;

//////////////////////////////////////////////////////////////////////////
//// PtolemyTypeRemover

/**

 @author Stephen Neuendorffer
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class PtolemyTypeRemover extends SceneTransformer 
    implements HasPhaseOptions {
    /** Construct a new transformer
     */
    private PtolemyTypeRemover(CompositeActor model) {
        _model = model;
    }

    /** Return an instance of this transformer that will operate on
     *  the given model.  The model is assumed to already have been
     *  properly initialized so that resolved types and other static
     *  properties of the model can be inspected.
     */
    public static PtolemyTypeRemover v(CompositeActor model) {
        return new PtolemyTypeRemover(model);
    }

    public String getPhaseName() {
        return "";
    }

    public String getDefaultOptions() {
        return "level:0";
    }

    public String getDeclaredOptions() {
        return "debug level";
    }

    protected void internalTransform(String phaseName, Map options) {
        _phaseName = phaseName;
        System.out.println("PtolemyTypeRemover.internalTransform("
                + phaseName + ", " + options + ")");

        boolean debug = PhaseOptions.getBoolean(options, "debug");
        int level = PhaseOptions.getInt(options, "level");

        boolean doneSomething = false;
        Hierarchy hierarchy = Scene.v().getActiveHierarchy();

        // Inline all methods on tokens that have the given depth.
        for (Iterator classes = Scene.v().getApplicationClasses()
                 .iterator(); classes.hasNext();) {
            SootClass entityClass = (SootClass) classes.next();
            
            fixTypes(entityClass, debug);
        }
    }

    public void fixTypes(SootClass entityClass, boolean debug) {
        for (Iterator methods = entityClass.getMethods().iterator(); methods
                .hasNext();) {
            SootMethod method = (SootMethod) methods.next();

            if (debug) {
                System.out.println("Replacing token assignments in method "
                        + method);
            }

            JimpleBody body = (JimpleBody) method.retrieveActiveBody();
            for (Iterator units = body.getUnits().snapshotIterator(); units
                     .hasNext();) {
                Unit unit = (Unit) units.next();
                
                if (debug) {
                    System.out.println("unit = " + unit);
                }

                if (unit instanceof AssignStmt ) {
                    AssignStmt stmt = (AssignStmt) unit;
                    if (stmt.getRightOp() instanceof ArrayRef &&
                            PtolemyUtilities.isTypeType(
                                    stmt.getRightOp().getType())) {
                        stmt.getRightOpBox().setValue(NullConstant.v());
                    } else if ((stmt.getRightOp() instanceof InstanceFieldRef ||
                            stmt.getRightOp() instanceof StaticFieldRef) &&
                            PtolemyUtilities.isTypeType(
                                    stmt.getRightOp().getType())) {
                        stmt.getRightOpBox().setValue(NullConstant.v());
                    } else if (stmt.getLeftOp() instanceof ArrayRef &&
                            PtolemyUtilities.isTypeType(stmt.getRightOp().getType())) {
                        body.getUnits().remove(stmt);
                    } else if ((stmt.getRightOp() instanceof NewExpr ||
                            stmt.getRightOp() instanceof NewArrayExpr ||
                            stmt.getRightOp() instanceof NewMultiArrayExpr) &&
                            PtolemyUtilities.isTypeType(
                                    stmt.getRightOp().getType())) {
                        stmt.getRightOpBox().setValue(NullConstant.v());
                    }
                }
            }
        }
    }
    private CompositeActor _model;

    private String _phaseName;

    private Map entityFieldToTokenFieldToReplacementField;

    private Map entityFieldToIsNotNullField;

    private Map localToFieldToLocal;

    private Map localToIsNotNullLocal;

    private boolean _mangleExceptionMessages = true;
}
