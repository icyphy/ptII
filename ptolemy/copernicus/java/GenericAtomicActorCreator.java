/* An interface for classes that replaces port methods.

 Copyright (c) 2001-2003 The Regents of the University of California.
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

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.actor.util.ConstVariableModelAnalysis;
import ptolemy.copernicus.kernel.EntitySootClass;
import ptolemy.copernicus.kernel.PtolemyUtilities;
import ptolemy.copernicus.kernel.SootUtilities;
import ptolemy.data.expr.Variable;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.StringAttribute;
import soot.FastHierarchy;
import soot.Hierarchy;
import soot.Local;
import soot.Modifier;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.Stmt;
import soot.jimple.toolkits.scalar.LocalNameStandardizer;
import soot.toolkits.scalar.LocalSplitter;
import soot.util.Chain;



//////////////////////////////////////////////////////////////////////////
//// GenericAtomicActorCreator
/**

@author Stephen Neuendorffer
@version $Id$
@since Ptolemy II 2.0
*/
public class GenericAtomicActorCreator implements AtomicActorCreator {

    /** Generate a new class with the given name that can take the
     *  place of the given actor.  Use the given options when
     *  necessary.  The given entity is assumed to be an expression actor.
     */
    public SootClass createAtomicActor(
            Entity actor, String newClassName,
            ConstVariableModelAnalysis constAnalysis, Map options) {
        TypedAtomicActor entity = (TypedAtomicActor)actor;

        String className = entity.getClass().getName();

        SootClass entityClass = Scene.v().loadClassAndSupport(className);
        entityClass.setLibraryClass();

        // Create a class for the entity instance.
        EntitySootClass entityInstanceClass =
            new EntitySootClass(entityClass, newClassName,
                    Modifier.PUBLIC);
        Scene.v().addClass(entityInstanceClass);
        entityInstanceClass.setApplicationClass();

        // Record everything that the class creates.
        HashSet tempCreatedSet = new HashSet();

        // Populate the method to initialize this instance.
        // We need to put something here before folding so that
        // the folder can deal with it.
        SootMethod initMethod = entityInstanceClass.getInitMethod();
        {
            JimpleBody body = Jimple.v().newBody(initMethod);
            initMethod.setActiveBody(body);
            // return void
            body.getUnits().add(Jimple.v().newReturnVoidStmt());
        }
        SootClass theClass = (SootClass)entityInstanceClass;
        SootClass superClass = theClass.getSuperclass();
        while (superClass != PtolemyUtilities.objectClass &&
                superClass != PtolemyUtilities.actorClass &&
                superClass != PtolemyUtilities.compositeActorClass) {
            superClass.setLibraryClass();
            SootUtilities.foldClass(theClass);
            superClass = theClass.getSuperclass();
        }

        // Go through all the initialization code and remove any old
        // parameter initialization code.  This has to happen after
        // class folding so that all of the parameter initialization
        // is available, but before we add the correct initialization.
        // FIXME: This needs to look at all code that is reachable
        // from a constructor.
        _removeAttributeInitialization(theClass);

        Entity classEntity = null;
        try {
            classEntity = (Entity)
                ModelTransformer._findDeferredInstance(entity).clone(null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        ModelTransformer.updateCreatedSet(
                entity.getFullName(),
                classEntity, classEntity, tempCreatedSet);

        {
            // replace the previous dummy body
            // for the initialization method with a new one.
            JimpleBody body = Jimple.v().newBody(initMethod);
            initMethod.setActiveBody(body);
            body.insertIdentityStmts();
            Chain units = body.getUnits();
            Local thisLocal = body.getThisLocal();

            // create attributes for those in the class
            ModelTransformer.createAttributes(body, entity, thisLocal,
                    entity, thisLocal, entityInstanceClass, tempCreatedSet);

            // Create and initialize ports
            ModelTransformer.createPorts(body, thisLocal, entity,
                    thisLocal, entity, entityInstanceClass, tempCreatedSet);

            // return void
            units.add(Jimple.v().newReturnVoidStmt());
        }

        // Remove super calls to the executable interface.
        // FIXME: This would be nice to do by inlining instead of
        // special casing.
        ModelTransformer.implementExecutableInterface(entityInstanceClass);

        _copyAttributesOtherThanVariable(entity, theClass, options);

        {
            // Add code to the beginning of the preinitialize method that
            // initializes the attributes.

            SootMethod method = theClass.getMethodByName("preinitialize");
            JimpleBody body = (JimpleBody)method.getActiveBody();
            Stmt insertPoint = body.getFirstNonIdentityStmt();
            ModelTransformer.initializeAttributesBefore(body, insertPoint,
                    entity, body.getThisLocal(),
                    entity, body.getThisLocal(),
                    entityInstanceClass);
         
            LocalNameStandardizer.v().transform(body, "at.lns");
            LocalSplitter.v().transform(body, "at.ls");
        }

        {
            LinkedList notConstantAttributeList = new LinkedList(
                    entity.attributeList(Variable.class));
            notConstantAttributeList.removeAll(
                    constAnalysis.getConstVariables(entity));
            // Sort according to dependancies.

            // Add code to the beginning of the prefire method that
            // computes the attribute values of anything that is not a
            // constant.
            SootMethod method = theClass.getMethodByName("prefire");
            JimpleBody body = (JimpleBody)method.getActiveBody();
            Stmt insertPoint = body.getFirstNonIdentityStmt();
            ModelTransformer.computeAttributesBefore(body, insertPoint,
                    entity, body.getThisLocal(),
                    entity, body.getThisLocal(),
                    entityInstanceClass,
                    notConstantAttributeList);
            LocalNameStandardizer.v().transform(body, "at.lns");
            LocalSplitter.v().transform(body, "at.ls");
        }

        // Reinitialize the hierarchy, since we've added classes.
        try {
            Scene.v().setActiveHierarchy(new Hierarchy());
        } catch (Error ex) {
            ex.printStackTrace(System.out);
        }
     
        try {
            Scene.v().setFastHierarchy(new FastHierarchy());
        } catch (Error ex) {
            System.out.println("foo");
            ex.printStackTrace(System.out);
        }

        // Inline all methods in the class that are called from
        // within the class.
        ModelTransformer.inlineLocalCalls(entityInstanceClass);

        // Remove the __CGInit method.  This should have been
        // inlined above.
        entityInstanceClass.removeMethod(
                entityInstanceClass.getInitMethod());

        return entityInstanceClass;
    }

    private static void _copyAttributesOtherThanVariable(
            Entity entity, SootClass entityClass, Map options) {
        // Loop over all the attributes of the actor
        for(Iterator attributes = 
                entity.attributeList(Attribute.class).iterator();
            attributes.hasNext();) {
            Attribute attribute = (Attribute) attributes.next();

            // PortParameters are handled specially.
            if(attribute instanceof PortParameter) {
                continue;
            }

            // If we have an attribute that derives from
            // stringAttribute, or Parameter then we need to grab some
            // code for it. (i.e. FileAttribute, and FileParameter)
            if((attribute instanceof StringAttribute &&
                    !attribute.getClass().equals(StringAttribute.class)) ||
               (attribute instanceof Parameter &&
                    !attribute.getClass().equals(Parameter.class))) {
                String className = attribute.getClass().getName();
                
                SootClass attributeClass = 
                    Scene.v().loadClassAndSupport(className);
                attributeClass.setLibraryClass();
                String newClassName = 
                    ModelTransformer.getInstanceClassName(attribute, options);
                
                // Create a new class for the attribute.
                SootClass newClass = SootUtilities.copyClass(
                        attributeClass, newClassName);
                // Make sure that we generate code for the new class.
                newClass.setApplicationClass();

                // Associate the new class with the attribute.
                ModelTransformer.addAttributeForClass(newClass, attribute);

                // Loop over all the methods and replace construction
                // of the old attribute with construction of the
                // copied class.
                SootUtilities.changeTypesOfFields(entityClass, 
                        attributeClass, newClass);
                SootUtilities.changeTypesInMethods(entityClass, 
                        attributeClass, newClass);
              
                // Fold the copied class up to StringAttribute, or parameter
                SootClass superClass = newClass.getSuperclass();
                while (superClass != PtolemyUtilities.objectClass &&
                        superClass != PtolemyUtilities.stringAttributeClass &&
                        superClass != PtolemyUtilities.parameterClass) {
                    superClass.setLibraryClass();
                    SootUtilities.foldClass(newClass);
                    superClass = newClass.getSuperclass();
                }
            }
        }
    }

    private static void _removeAttributeInitialization(SootClass theClass) {
        for (Iterator methods = theClass.getMethods().iterator();
             methods.hasNext();) {
            SootMethod method = (SootMethod)methods.next();
            JimpleBody body = (JimpleBody)method.retrieveActiveBody();
            for (Iterator units = body.getUnits().snapshotIterator();
                 units.hasNext();) {
                Stmt stmt = (Stmt)units.next();
                if (!stmt.containsInvokeExpr()) {
                    continue;
                }
                InvokeExpr r = (InvokeExpr)stmt.getInvokeExpr();
                // This is steve...
                // This is steve gacking at the ugliest code
                // he's written in a while.   See steve gack.
                // gack steve, gack.
                // This is Christopher.
                // This is Christopher gacking on Steve's code
                // gack Christopher, gack.
                if (r.getMethod().getName().equals("attributeChanged") ||
                        r.getMethod().getName().equals("setExpression") ||
                        r.getMethod().getName().equals("setToken") ||
                        r.getMethod().getName()
                        .equals("setTokenConsumptionRate") ||
                        r.getMethod().getName()
                        .equals("setTokenProductionRate") ||
                        r.getMethod().getName()
                        .equals("setTokenInitProduction")) {
                    body.getUnits().remove(stmt);
                }
                if (r.getMethod().getSubSignature().equals(
                        PtolemyUtilities
                        .variableConstructorWithToken.getSubSignature())) {
                    SootClass variableClass =
                        r.getMethod().getDeclaringClass();
                    SootMethod constructorWithoutToken =
                        variableClass.getMethod(
                                PtolemyUtilities
                                .variableConstructorWithoutToken.getSubSignature());
                    // Replace the three-argument
                    // constructor with a two-argument
                    // constructor.  We do this for
                    // several reasons:

                    // 1) The assignment is
                    // redundant...  all parameters
                    // are initialized with the
                    // appropriate value.

                    // 2) The type of the token is
                    // often wrong for polymorphic
                    // actors.

                    // 3) Later on, when we inline all
                    // token constructors, there is no
                    // longer a token to pass to the
                    // constructor.  It is easier to
                    // just deal with it now...

                    // Create a new two-argument constructor.
                    InstanceInvokeExpr expr = (InstanceInvokeExpr)r;
                    stmt.getInvokeExprBox().setValue(
                            Jimple.v().newSpecialInvokeExpr(
                                    (Local)expr.getBase(),
                                    constructorWithoutToken,
                                    r.getArg(0),
                                    r.getArg(1)));
                }
            }
        }
    }
}
