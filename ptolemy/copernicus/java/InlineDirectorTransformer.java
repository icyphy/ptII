/* A transformer that inlines method calls on an SDF director.

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

import java.util.Iterator;
import java.util.Map;

import ptolemy.actor.CompositeActor;
import ptolemy.copernicus.kernel.MakefileWriter;
import ptolemy.copernicus.kernel.PtolemyUtilities;
import ptolemy.domains.fsm.kernel.FSMDirector;
import ptolemy.domains.fsm.kernel.HSFSMDirector;
import ptolemy.domains.giotto.kernel.GiottoDirector;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.kernel.Entity;
import soot.FastHierarchy;
import soot.HasPhaseOptions;
import soot.Hierarchy;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.ValueBox;
import soot.jimple.InvokeExpr;
import soot.jimple.JimpleBody;
import soot.jimple.Stmt;

//////////////////////////////////////////////////////////////////////////
//// InlineDirectorTransformer

/**
 A transformer that inlines an SDF director.  This transformer synthesizes
 methods that properly implement the executable interface inside the class
 representing the model.  The resulting class includes code to properly
 initialize the instance classes for the actors and fire them in the
 order of the SDF schedule.

 @author Michael Wirthlin, Stephen Neuendorffer, Edward A. Lee, Christopher Hylands
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class InlineDirectorTransformer extends SceneTransformer implements
        HasPhaseOptions {
    /** Construct a new transformer
     */
    private InlineDirectorTransformer(CompositeActor model) {
        _model = model;
    }

    /** Return an instance of this transformer that will operate on
     *  the given model.  The model is assumed to already have been
     *  properly initialized so that resolved types and other static
     *  properties of the model can be inspected.
     */
    public static InlineDirectorTransformer v(CompositeActor model) {
        return new InlineDirectorTransformer(model);
    }

    public String getPhaseName() {
        return "";
    }

    public String getDefaultOptions() {
        return "";
    }

    public String getDeclaredOptions() {
        return "targetPackage outDir";
    }

    protected void internalTransform(String phaseName, Map options) {
        System.out.println("InlineDirectorTransformer.internalTransform("
                + phaseName + ", " + options + ")");

        SootClass modelClass = ModelTransformer.getModelClass();
        _inlineDirectorsIn(_model, modelClass, phaseName, options);
    }

    private void _inlineDirectorsIn(CompositeActor model, SootClass modelClass,
            String phaseName, Map options) {
        for (Iterator i = model.deepEntityList().iterator(); i.hasNext();) {
            Entity entity = (Entity) i.next();

            if (entity instanceof CompositeActor) {
                String className = ModelTransformer.getInstanceClassName(
                        entity, options);
                SootClass compositeClass = Scene.v().getSootClass(className);
                _inlineDirectorsIn((CompositeActor) entity, compositeClass,
                        phaseName, options);
            }
        }

        MakefileWriter.addMakefileSubstitution("@extraClassPath@", "");

        try {
            DirectorInliner inliner;

            if (model.getDirector() instanceof SDFDirector) {
                inliner = new SDFDirectorInliner();
            } else if (model.getDirector() instanceof HSFSMDirector
                    || model.getDirector() instanceof FSMDirector) {
                inliner = new HSDirectorInliner();
            } else if (model.getDirector() instanceof GiottoDirector) {
                inliner = new GiottoDirectorInliner();
            } else {
                throw new RuntimeException("Inlining a director can not "
                        + "be performed on a director of class "
                        + model.getDirector().getClass().getName());
            }

            inliner.inlineDirector(model, modelClass, phaseName, options);
        } catch (Exception ex) {
            throw new RuntimeException("Inlining director failed", ex);
        }

        // We're almost always going to be adding methods to classes,
        // so just do this.
        Scene.v().setActiveHierarchy(new Hierarchy());
        Scene.v().setFastHierarchy(new FastHierarchy());

        // First remove methods that are called on the director.
        // Loop over all the entity classes...
        for (Iterator i = model.deepEntityList().iterator(); i.hasNext();) {
            Entity entity = (Entity) i.next();
            String className = ModelTransformer.getInstanceClassName(entity,
                    options);
            SootClass theClass = Scene.v().loadClassAndSupport(className);

            // Loop over all the methods...
            for (Iterator methods = theClass.getMethods().iterator(); methods
                    .hasNext();) {
                SootMethod method = (SootMethod) methods.next();

                JimpleBody body = (JimpleBody) method.retrieveActiveBody();

                // Loop over all the statements.
                for (Iterator units = body.getUnits().snapshotIterator(); units
                        .hasNext();) {
                    Stmt unit = (Stmt) units.next();

                    if (!unit.containsInvokeExpr()) {
                        continue;
                    }

                    ValueBox box = (ValueBox) unit.getInvokeExprBox();
                    InvokeExpr r = (InvokeExpr) box.getValue();

                    if (r.getMethod().getSubSignature().equals(
                            PtolemyUtilities.invalidateResolvedTypesMethod
                                    .getSubSignature())) {
                        // Remove calls to invalidateResolvedTypes()
                        body.getUnits().remove(unit);
                    }
                }
            }
        }
    }

    private CompositeActor _model;
}
