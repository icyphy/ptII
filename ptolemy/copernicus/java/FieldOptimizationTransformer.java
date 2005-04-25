/* A transformer that removes unnecessary fields from classes.

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

import soot.Modifier;
import soot.PhaseOptions;
import soot.RefType;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Value;

import soot.jimple.DefinitionStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.Stmt;

import soot.util.Chain;

import ptolemy.actor.CompositeActor;
import ptolemy.kernel.Entity;

import java.util.Iterator;
import java.util.Map;


//////////////////////////////////////////////////////////////////////////
//// FieldOptimizationTransformer

/**
   A Transformer that is responsible for inlining the values of parameters.
   The values of the parameters are taken from the model specified for this
   transformer.

   @author Stephen Neuendorffer
   @version $Id$
   @since Ptolemy II 2.0
   @Pt.ProposedRating Red (cxh)
   @Pt.AcceptedRating Red (cxh)

*/
public class FieldOptimizationTransformer extends SceneTransformer {
    /** Construct a new transformer
     */
    private FieldOptimizationTransformer(CompositeActor model) {
        _model = model;
    }

    /** Return an instance of this transformer that will operate on
     *  the given model.  The model is assumed to already have been
     *  properly initialized so that resolved types and other static
     *  properties of the model can be inspected.
     */
    public static FieldOptimizationTransformer v(CompositeActor model) {
        return new FieldOptimizationTransformer(model);
    }

    protected void internalTransform(String phaseName, Map options) {
        int localCount = 0;
        System.out.println("FieldOptimizationTransformer.internalTransform("
            + phaseName + ", " + options + ")");

        SootClass stringClass = Scene.v().loadClassAndSupport("java.lang.String");
        Type stringType = RefType.v(stringClass);
        SootClass objectClass = Scene.v().loadClassAndSupport("java.lang.Object");
        SootMethod toStringMethod = objectClass.getMethod(
                "java.lang.String toString()");
        SootClass namedObjClass = Scene.v().loadClassAndSupport("ptolemy.kernel.util.NamedObj");
        SootMethod getAttributeMethod = namedObjClass.getMethod(
                "ptolemy.kernel.util.Attribute getAttribute(java.lang.String)");
        SootMethod attributeChangedMethod = namedObjClass.getMethod(
                "void attributeChanged(ptolemy.kernel.util.Attribute)");

        SootClass attributeClass = Scene.v().loadClassAndSupport("ptolemy.kernel.util.Attribute");
        Type attributeType = RefType.v(attributeClass);
        SootClass settableClass = Scene.v().loadClassAndSupport("ptolemy.kernel.util.Settable");
        Type settableType = RefType.v(settableClass);
        SootMethod getExpressionMethod = settableClass.getMethod(
                "java.lang.String getExpression()");
        SootMethod setExpressionMethod = settableClass.getMethod(
                "void setExpression(java.lang.String)");

        SootClass tokenClass = Scene.v().loadClassAndSupport("ptolemy.data.Token");
        Type tokenType = RefType.v(tokenClass);
        SootClass parameterClass = Scene.v().loadClassAndSupport("ptolemy.data.expr.Variable");
        SootMethod getTokenMethod = parameterClass.getMethod(
                "ptolemy.data.Token getToken()");
        SootMethod setTokenMethod = parameterClass.getMethod(
                "void setToken(ptolemy.data.Token)");

        // Loop over all the actor instance classes.
        for (Iterator i = _model.deepEntityList().iterator(); i.hasNext();) {
            Entity entity = (Entity) i.next();
            String className = PhaseOptions.getString(options, "targetPackage")
                + "." + entity.getName();
            SootClass entityClass = Scene.v().loadClassAndSupport(className);

            for (Iterator fields = entityClass.getFields().iterator();
                            fields.hasNext();) {
                SootField field = (SootField) fields.next();

                // FIXME: static fields too.
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }

                boolean finalize = true;
                Value fieldValue = null;

                for (Iterator methods = entityClass.getMethods().iterator();
                                (methods.hasNext() && finalize);) {
                    SootMethod method = (SootMethod) methods.next();

                    if (method.getName().equals("<init>")) {
                        Chain units = method.retrieveActiveBody().getUnits();
                        Stmt stmt = (Stmt) units.getLast();

                        while (!stmt.equals(units.getFirst())) {
                            if (stmt instanceof DefinitionStmt
                                            && ((DefinitionStmt) stmt)
                                            .getLeftOp() instanceof InstanceFieldRef) {
                                InstanceFieldRef ref = (InstanceFieldRef) ((DefinitionStmt) stmt)
                                                .getLeftOp();

                                if ((ref.getField() == field)
                                                && (fieldValue == null)) {
                                    fieldValue = ((DefinitionStmt) stmt)
                                                    .getRightOp();
                                    break;
                                } else if (fieldValue != null) {
                                    finalize = false;
                                }
                            }

                            stmt = (Stmt) units.getPredOf(stmt);
                        }
                    }
                }

                if (finalize && (fieldValue != null)) {
                    System.out.println("field " + field + " has final value = "
                        + fieldValue);
                }
            }
        }
    }

    private CompositeActor _model;
}
