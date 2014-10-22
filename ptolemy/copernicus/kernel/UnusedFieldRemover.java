/* A transformer that removes unused fields from classes.

 Copyright (c) 2001-2014 The Regents of the University of California.
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
package ptolemy.copernicus.kernel;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import soot.RefType;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Value;
import soot.ValueBox;
import soot.jimple.FieldRef;
import soot.jimple.JimpleBody;
import soot.jimple.Stmt;

//////////////////////////////////////////////////////////////////////////
//// UnusedFieldRemover

/**
 A Transformer that removes any private fields from a class that are
 never read.  This transformer also transforms the bodies of the
 methods in a class to remove any writes to a removed field.

 @author Stephen Neuendorffer
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)

 */
public class UnusedFieldRemover extends SceneTransformer {
    /** Return an instance of this transformer that will operate on
     *  the given model.  The model is assumed to already have been
     *  properly initialized so that resolved types and other static
     *  properties of the model can be inspected.
     */
    public static UnusedFieldRemover v() {
        return _instance;
    }

    @Override
    protected void internalTransform(String phaseName, Map options) {
        System.out.println("UnusedFieldRemover.internalTransform(" + phaseName
                + ", " + options + ")");

        SootClass stringClass = Scene.v().loadClassAndSupport(
                "java.lang.String");
        /*Type stringType = */RefType.v(stringClass);
        SootClass objectClass = Scene.v().loadClassAndSupport(
                "java.lang.Object");
        /*SootMethod toStringMethod =*/objectClass
                .getMethod("java.lang.String toString()");
        SootClass namedObjClass = Scene.v().loadClassAndSupport(
                "ptolemy.kernel.util.NamedObj");
        /*SootMethod getAttributeMethod = */namedObjClass
                .getMethod("ptolemy.kernel.util.Attribute getAttribute(java.lang.String)");
        /*SootMethod attributeChangedMethod = */namedObjClass
                .getMethod("void attributeChanged(ptolemy.kernel.util.Attribute)");

        SootClass attributeClass = Scene.v().loadClassAndSupport(
                "ptolemy.kernel.util.Attribute");
        /*Type attributeType =*/RefType.v(attributeClass);
        SootClass settableClass = Scene.v().loadClassAndSupport(
                "ptolemy.kernel.util.Settable");
        /*Type settableType =*/RefType.v(settableClass);
        /*SootMethod getExpressionMethod = */settableClass
                .getMethod("java.lang.String getExpression()");
        /*SootMethod setExpressionMethod = */settableClass
                .getMethod("void setExpression(java.lang.String)");

        SootClass tokenClass = Scene.v().loadClassAndSupport(
                "ptolemy.data.Token");
        /*Type tokenType = */RefType.v(tokenClass);
        SootClass parameterClass = Scene.v().loadClassAndSupport(
                "ptolemy.data.expr.Variable");
        /*SootMethod getTokenMethod = */parameterClass
                .getMethod("ptolemy.data.Token getToken()");
        /*SootMethod setTokenMethod = */parameterClass
                .getMethod("void setToken(ptolemy.data.Token)");

        Set unusedFieldSet = new HashSet();

        // Loop over all the actor instance classes and create the set of
        // all fields.
        for (Iterator i = Scene.v().getApplicationClasses().iterator(); i
                .hasNext();) {
            SootClass entityClass = (SootClass) i.next();

            unusedFieldSet.addAll(entityClass.getFields());
        }

        // Loop through all the methods and kill all the used fields.
        for (Iterator i = Scene.v().getApplicationClasses().iterator(); i
                .hasNext();) {
            SootClass entityClass = (SootClass) i.next();

            for (Iterator methods = entityClass.getMethods().iterator(); methods
                    .hasNext();) {
                SootMethod method = (SootMethod) methods.next();
                JimpleBody body = (JimpleBody) method.retrieveActiveBody();

                for (Iterator stmts = body.getUnits().iterator(); stmts
                        .hasNext();) {
                    Stmt stmt = (Stmt) stmts.next();

                    for (Iterator boxes = stmt.getUseBoxes().iterator(); boxes
                            .hasNext();) {
                        ValueBox box = (ValueBox) boxes.next();
                        Value value = box.getValue();

                        if (value instanceof FieldRef) {
                            Object field = ((FieldRef) value).getField();
                            unusedFieldSet.remove(field);
                        }
                    }
                }
            }
        }

        // Loop through the methods again, and kill the statements
        // that write to an unused field.
        for (Iterator i = Scene.v().getApplicationClasses().iterator(); i
                .hasNext();) {
            SootClass entityClass = (SootClass) i.next();

            for (Iterator methods = entityClass.getMethods().iterator(); methods
                    .hasNext();) {
                SootMethod method = (SootMethod) methods.next();
                JimpleBody body = (JimpleBody) method.retrieveActiveBody();

                for (Iterator stmts = body.getUnits().snapshotIterator(); stmts
                        .hasNext();) {
                    Stmt stmt = (Stmt) stmts.next();

                    for (Iterator boxes = stmt.getDefBoxes().iterator(); boxes
                            .hasNext();) {
                        ValueBox box = (ValueBox) boxes.next();
                        Value value = box.getValue();

                        if (value instanceof FieldRef) {
                            Object field = ((FieldRef) value).getField();

                            if (unusedFieldSet.contains(field)) {
                                body.getUnits().remove(stmt);
                            }
                        }
                    }
                }
            }

            for (Iterator fields = entityClass.getFields().snapshotIterator(); fields
                    .hasNext();) {
                SootField field = (SootField) fields.next();

                if (unusedFieldSet.contains(field)) {
                    entityClass.removeField(field);
                }
            }
        }
    }

    private static UnusedFieldRemover _instance = new UnusedFieldRemover();
}
