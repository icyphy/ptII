/* Transform Actors using Soot

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

import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.copernicus.kernel.SootUtilities;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.*;
// FIXME: bad dependencies.
import ptolemy.copernicus.java.EntitySootClass;
import ptolemy.copernicus.java.ModelTransformer;
import ptolemy.copernicus.java.PtolemyUtilities;

import soot.util.Chain;

import soot.Local;
import soot.Modifier;
import soot.Options;
import soot.RefType;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.VoidType;

import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.StringConstant;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.toolkits.invoke.SiteInliner;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

//////////////////////////////////////////////////////////////////////////
//// ActorTransformer
/**
Transform Actors using Soot.

@author Stephen Neuendorffer, Christopher Hylands
@version $Id$
*/
public class ActorTransformer extends SceneTransformer {
    /** Construct a new transformer
     */
    private ActorTransformer(CompositeActor model) {
        _model = model;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return an instance of this transformer that will operate on
     *  the given model.  The model is assumed to already have been
     *  properly initialized so that resolved types and other static
     *  properties of the model can be inspected.
     */
    public static ActorTransformer v(CompositeActor model) {
        // FIXME: This should use a map to return a singleton instance
	// for each model
        return new ActorTransformer(model);
    }

    /** Return the list of default options for this transformer.
     *  @return An empty string.
     */
    public String getDefaultOptions() {
        return "";
    }

    /** Return the list of declared options for this transformer.
     *  This is a list of space separated option names.
     *  @return The value of the superclass options, plus the option "deep".
     */
    public String getDeclaredOptions() {
        return super.getDeclaredOptions() + " deep targetPackage";
    }

    /** Transform the Scene according to the information specified
     *  in the model for this transform.
     *  @param phaseName The phase this transform is operating under.
     *  @param options The options to apply.
     */
    protected void internalTransform(String phaseName, Map options) {
	System.out.println("ActorTransformer.internalTransform("
                + phaseName + ", " + options + ")");

        if(!Options.getBoolean(options, "deep")) {
            return;
        }
     
        // Create an instance class for every actor.
        for(Iterator i = _model.entityList().iterator();
            i.hasNext();) {
            Entity entity = (Entity)i.next();
            String className = entity.getClass().getName();
            SootClass entityClass = Scene.v().loadClassAndSupport(className);
            entityClass.setLibraryClass();

            String newClassName = getInstanceClassName(entity, options);

            // FIXME the code below should probably copy the class and then
            // add init stuff.  EntitySootClass handles this nicely, but
            // doesn't let us use copyClass.  Generally adding this init crap
            // is something we have to do alot.  How do we handle it nicely?
            // 
            //            SootClass newClass =
            //     SootUtilities.copyClass(entityClass, newClassName);
            //  newClass.setApplicationClass();

            
            // create a class for the entity instance.
            EntitySootClass entityInstanceClass =
                new EntitySootClass(entityClass, newClassName,
                        Modifier.PUBLIC);
            Scene.v().addClass(entityInstanceClass);
            entityInstanceClass.setApplicationClass();

            // populate the method to initialize this instance.
            // We need to put something here before folding so that
            // the folder can deal with it.
            SootMethod initMethod = entityInstanceClass.getInitMethod();
            JimpleBody body = Jimple.v().newBody(initMethod);
            initMethod.setActiveBody(body);
            // return void
            body.getUnits().add(Jimple.v().newReturnVoidStmt());

            SootClass theClass = (SootClass)entityInstanceClass;
            SootClass superClass = theClass.getSuperclass();
            while(superClass != PtolemyUtilities.objectClass &&
                    superClass != PtolemyUtilities.actorClass &&
                    superClass != PtolemyUtilities.compositeActorClass) {
                superClass.setLibraryClass();
                SootUtilities.foldClass(theClass);
                superClass = theClass.getSuperclass();
            }
            
            // Go through all the initialization code and removing any
            // parameter initialization code.
            // FIXME: This needs to look at all code that is reachable
            // from a constructor.
            _removeAttributeInitialization(theClass);

            // replace the previous dummy body
            // for the initialization method with a new one.
            body = Jimple.v().newBody(initMethod);
            initMethod.setActiveBody(body);
            body.insertIdentityStmts();
            Chain units = body.getUnits();
            Local thisLocal = body.getThisLocal();
            
            // insert code to initialize the settable
            // parameters of this instance and
            // create fields for attributes.
            ModelTransformer.createFieldsForAttributes(
                    body, entity, thisLocal, 
                    entity, thisLocal, entityInstanceClass);
                    
            // return void
            units.add(Jimple.v().newReturnVoidStmt());

            // Remove super calls to the executable interface.
            // FIXME: This would be nice to do by inlining instead of
            // special casing.
            _implementExecutableInterface(entityInstanceClass);            

            // Inline all methods in the class that are called from within the class.
            _inlineLocalCalls(theClass);
        }
    }

    public static String getInstanceClassName(Entity entity, Map options) {
        // Note that we use sanitizeName because entity names can have 
        // spaces, and append leading characters because entity names
        // can start with numbers.
        return Options.getString(options, "targetPackage")
            + ".CG" + SootUtilities.sanitizeName(entity.getName());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private static void _implementExecutableInterface(SootClass theClass) {
        // Loop through all the methods and remove calls to super.
        for(Iterator methods = theClass.getMethods().iterator();
            methods.hasNext();) {
            SootMethod method = (SootMethod)methods.next();
            JimpleBody body = (JimpleBody)method.retrieveActiveBody();
             for(Iterator units = body.getUnits().snapshotIterator();
                units.hasNext();) {
                Unit unit = (Unit)units.next();
                Iterator boxes = unit.getUseBoxes().iterator();
                while(boxes.hasNext()) {
                    ValueBox box = (ValueBox)boxes.next();
                    Value value = box.getValue();
                    if(value instanceof SpecialInvokeExpr) {
                        SpecialInvokeExpr r = (SpecialInvokeExpr)value;
                        if(PtolemyUtilities.executableInterface.declaresMethod(
                                r.getMethod().getSubSignature())) {
                            if(r.getMethod().getName().equals("prefire") ||
                               r.getMethod().getName().equals("postfire")) {
                                box.setValue(IntConstant.v(1));
                            } else {
                                body.getUnits().remove(unit);
                            }
                        }
                    }
                }
            }
        }

        // The initialize method implemented in the actor package is wierd,
        // because it calls getDirector.  Since we don't need it,
        // make sure that we never call the baseclass initialize method.
        // FIXME: When we get to the point where we no longer derive
        // from TypedAtomicActor, we need to implement all of these methods.
        if(!theClass.declaresMethodByName("initialize")) {
            SootMethod method = new SootMethod("initialize",
                    new LinkedList(), VoidType.v(), Modifier.PUBLIC);
            theClass.addMethod(method);
            JimpleBody body = Jimple.v().newBody(method);
            method.setActiveBody(body);
            body.insertIdentityStmts();
            body.getUnits().add(Jimple.v().newReturnVoidStmt());            
        }
    }

    // Inline invocation sites from methods in the given class to
    // another method in the given class
    private static void _inlineLocalCalls(SootClass theClass) {
        // FIXME: what if the inlined code contains another call
        // to this class???
        for(Iterator methods = theClass.getMethods().iterator();
            methods.hasNext();) {
            SootMethod method = (SootMethod)methods.next();
            JimpleBody body = (JimpleBody)method.retrieveActiveBody();
            for(Iterator units = body.getUnits().snapshotIterator();
                units.hasNext();) {
                Stmt stmt = (Stmt)units.next();
                if(stmt.containsInvokeExpr()) {
                    InvokeExpr r = (InvokeExpr)stmt.getInvokeExpr();
                    if(r.getMethod().getDeclaringClass().equals(theClass)) {
                        // FIXME: What if more than one method could be called?
                        SiteInliner.inlineSite(r.getMethod(), stmt, method);
                    }
                }
            }
        }
    }

    private static void _removeAttributeInitialization(SootClass theClass) {
        for(Iterator methods = theClass.getMethods().iterator();
            methods.hasNext();) {
            SootMethod method = (SootMethod)methods.next();
            JimpleBody body = (JimpleBody)method.retrieveActiveBody();
            for(Iterator units = body.getUnits().snapshotIterator();
                units.hasNext();) {
                Stmt stmt = (Stmt)units.next();
                if(stmt.containsInvokeExpr()) {
                    InvokeExpr r = (InvokeExpr)stmt.getInvokeExpr();
                    // This is steve...  This is steve gacking at the ugliest code 
                    // he's written in a while.   See steve gack.
                    if(r.getMethod().getName().equals("attributeChanged") ||
                            r.getMethod().getName().equals("setExpression") ||
                            r.getMethod().getName().equals("setToken") ||
                            r.getMethod().getName().equals("setTokenConsumptionRate") ||
                            r.getMethod().getName().equals("setTokenProductionRate") ||
                            r.getMethod().getName().equals("setTokenInitProduction")) {
                        body.getUnits().remove(stmt);
                    }
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private CompositeActor _model;
}













