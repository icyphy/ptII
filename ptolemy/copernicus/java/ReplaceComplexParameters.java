/* A transformer that replaces port communication in an SDF model

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

import java.util.*;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedIORelation;
import ptolemy.actor.parameters.*;
import ptolemy.copernicus.kernel.PtolemyUtilities;
import ptolemy.copernicus.kernel.SootUtilities;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.Typeable;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.StringUtilities;

import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.invoke.SiteInliner;
import soot.jimple.toolkits.scalar.Evaluator;
import soot.jimple.toolkits.scalar.LocalNameStandardizer;
import soot.toolkits.graph.CompleteUnitGraph;
import soot.toolkits.scalar.LocalDefs;
import soot.toolkits.scalar.LocalUses;
import soot.toolkits.scalar.SimpleLocalDefs;
import soot.toolkits.scalar.SimpleLocalUses;
import soot.toolkits.scalar.UnitValueBoxPair;


//////////////////////////////////////////////////////////////////////////
//// ReplaceComplexParameters
/**
A Transformer that replaces complex parameters and attributes with a
'simpler' parameter with normal parameter semantics.  This relies on
fields for ports and attributes.

@author Stephen Neuendorffer
@version $Id$
@since Ptolemy II 2.0
*/
public class ReplaceComplexParameters extends SceneTransformer 
    implements HasPhaseOptions {
    /** Construct a new transformer
     */
    private ReplaceComplexParameters(CompositeActor model) {
        _model = model;
    }

    /** Return an instance of this transformer that will operate on
     *  the given model.  The model is assumed to already have been
     *  properly initialized so that resolved types and other static
     *  properties of the model can be inspected.
     */
    public static ReplaceComplexParameters v(CompositeActor model) {
        return new ReplaceComplexParameters(model);
    }

    public String getPhaseName() {
        return "";
    }

    public String getDefaultOptions() {
        return "";
    }

    public String getDeclaredOptions() {
        return "targetPackage debug";
    }

    protected void internalTransform(String phaseName, Map options) {
        System.out.println("ReplaceComplexParameters.internalTransform("
                + phaseName + ", " + options + ")");

        _options = options;
        _phaseName = phaseName;
        _debug = PhaseOptions.getBoolean(options, "debug");

        _replaceComplexParametersIn(ModelTransformer.getModelClass(), _model);

    }

    // Replac ComplexParameters and ParameterPorts in all classes,
    // starting at the bottom of the hierarchy...
    private void _replaceComplexParametersIn(
            SootClass modelClass, CompositeActor model) {
        Director director = model.getDirector();

        // Loop over all the model instance classes.
        for (Iterator entities = model.deepEntityList().iterator();
             entities.hasNext();) {
            ComponentEntity entity = (ComponentEntity)entities.next();
            String className =
                ModelTransformer.getInstanceClassName(entity, _options);
            SootClass entityClass = Scene.v().loadClassAndSupport(className);


            copyAttributesOtherThanVariable(entity, entityClass);

            // recurse.
            if (entity instanceof CompositeActor) {
                _replaceComplexParametersIn(entityClass,
                        (CompositeActor)entity);
            }
        }

        // Reinitialize the hierarchy, since we've added classes.
        Scene.v().setActiveHierarchy(new Hierarchy());
        Scene.v().setFastHierarchy(new FastHierarchy());
    }

    public  void copyAttributesOtherThanVariable(
            Entity entity, SootClass entityClass) {
        // Loop over all the attributes of the actor
        for(Iterator attributes = 
                entity.attributeList(Attribute.class).iterator();
            attributes.hasNext();) {
            Attribute attribute = (Attribute) attributes.next();
            
            // Ignore attributes that are ignorable.
            if(ModelTransformer._isIgnorableAttribute(attribute)) {
                continue;
            }
            
            // PortParameters are handled specially.
           //  if(attribute instanceof PortParameter) {
//                 continue;
//             }

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
                    ModelTransformer.getInstanceClassName(attribute, _options);
                
                // Create a new class for the attribute.
                SootClass newClass = SootUtilities.copyClass(
                        attributeClass, newClassName);
                // Make sure that we generate code for the new class.
                newClass.setApplicationClass();

                // Associate the new class with the attribute.
                ModelTransformer.addAttributeForClass(newClass, attribute);

                // Fold the copied class up to StringAttribute, or parameter
                SootClass superClass = newClass.getSuperclass();
                while (superClass != PtolemyUtilities.objectClass &&
                        superClass != PtolemyUtilities.stringAttributeClass &&
                        superClass != PtolemyUtilities.parameterClass) {
                    superClass.setLibraryClass();
                    SootUtilities.foldClass(newClass);
                    superClass = newClass.getSuperclass();
                }
                
                // Remove problematic methods for PortParameter
                if(newClass.declaresMethodByName("setContainer")) {
                    SootMethod method =
                        newClass.getMethodByName("setContainer");
                    newClass.removeMethod(method);
                }
                if(newClass.declaresMethodByName("setName")) {
                    SootMethod method =
                        newClass.getMethodByName("setName");
                    newClass.removeMethod(method);
                }
                if(newClass.declaresMethodByName("attributeChanged")) {
                    SootMethod method =
                        newClass.getMethodByName("attributeChanged");
                    newClass.removeMethod(method);
                }        
                if(newClass.declaresFieldByName("_port")) {
                    SootField field = 
                        newClass.getFieldByName("_port");
                    Port port = ((PortParameter)attribute).getPort();
                    field.addTag(new ValueTag(port));
                }
                
                // Add a container field to the generated class.
                FieldsForEntitiesTransformer._createContainerField(
                        newClass);

                // Loop over all the methods and replace construction
                // of the old attribute with construction of the
                // copied class.
                for (Iterator classes =
                         Scene.v().getApplicationClasses().iterator();
                     classes.hasNext();) {
                    SootClass theClass = (SootClass)classes.next();
                    _replaceObjectTypesInClass(theClass, attribute,
                            attributeClass, newClass);     
                }
            }
        }
    }

    // This operation is similar to sootUtilities.changeTypesOfFields
    // and SootUtilities.changeTypesInMethods, except that it uses
    // namedObjAnalysis to pick up only references to the given
    // object.
    private void _replaceObjectTypesInClass(SootClass theClass, 
            NamedObj object, SootClass oldClass, SootClass newClass) {
        if(_debug) {
            System.out.println("replacing objects in " + theClass 
                    + " for " + object);
        }
        // FIXME: deal with inner classes.
        // Find the field for the object and change it.
        Iterator fields = theClass.getFields().snapshotIterator();
        while (fields.hasNext()) {
            SootField oldField = (SootField)fields.next();
            // Check that the field has a ValueTag that points to object.
            NamedObj fieldObject = (NamedObj)ValueTag.getFieldObject(oldField);
            if(fieldObject != object) {
                continue;
            }
            // Check that the field has the right type.
            Type type = oldField.getType();
            if (type instanceof RefType) {
                SootClass refClass = ((RefType)type).getSootClass();
                if (refClass == oldClass) {
                    oldField.setType(RefType.v(newClass));
                    // we have to do this seemingly useless
                    // thing, since the scene caches a pointer
                    // to the field based on it's parameter types.
                    theClass.removeField(oldField);
                    theClass.addField(oldField);
               //  } else if (refClass.getName()
//                         .startsWith(oldClass.getName())) {
//                     SootClass changeClass =
//                         _getInnerClassCopy(oldClass,
//                                 refClass,
//                                 newClass);
//                     oldField.setType(RefType.v(changeClass));
//                     // we have to do this seemingly useless
//                     // thing, since the scene caches a pointer
//                     // to the field based on it's parameter types.
//                     theClass.removeField(oldField);
//                     theClass.addField(oldField);
                }
            }
        }
        ArrayList methodList = new ArrayList(theClass.getMethods());
        for (Iterator methods = methodList.iterator();
             methods.hasNext();) {
            SootMethod newMethod = (SootMethod)methods.next();
            //   System.out.println("newMethod = " + newMethod.getSignature());

//             Type returnType = newMethod.getReturnType();
//             if (returnType instanceof RefType &&
//                     ((RefType)returnType).getSootClass() == oldClass) {
//                 newMethod.setReturnType(RefType.v(newClass));
//             }
//             List paramTypes = new LinkedList();
//             for (Iterator oldParamTypes =
//                      newMethod.getParameterTypes().iterator();
//                  oldParamTypes.hasNext();) {
//                 Type type = (Type)oldParamTypes.next();
//                 if (type instanceof RefType &&
//                         ((RefType)type).getSootClass() == oldClass) {
//                     paramTypes.add(RefType.v(newClass));
//                 } else {
//                     paramTypes.add(type);
//                 }
//             }
//             newMethod.setParameterTypes(paramTypes);

//             // we have to do this seemingly useless
//             // thing, since the scene caches a pointer
//             // to the method based on it's parameter types.
//             theClass.removeMethod(newMethod);
//             theClass.addMethod(newMethod);

            Body newBody = newMethod.retrieveActiveBody();
            // Analyze what object each local points to.
            NamedObjAnalysis analysis = new NamedObjAnalysis(newMethod,
                    ModelTransformer.getObjectForClass(theClass));

            for (Iterator locals = newBody.getLocals().iterator();
                 locals.hasNext();) {
                Local local = (Local)locals.next();
                Type type = local.getType();
                try {
                    if (type instanceof RefType &&
                            ((RefType)type).getSootClass() == oldClass &&
                            object == analysis.getObject(local)) {
                        local.setType(RefType.v(newClass));
                    }
                } catch (Exception ex) {
                    if(_debug) {
                        System.out.println("Exception on local = " + ex);
                    }
                }
            }
            
            Iterator j = newBody.getUnits().iterator();
            while (j.hasNext()) {
                Unit unit = (Unit)j.next();
                //    System.out.println("unit = " + unit);
                Iterator boxes = unit.getUseBoxes().iterator();
                while (boxes.hasNext()) {
                    ValueBox box = (ValueBox)boxes.next();
                    Value value = box.getValue();
                    
                    if (value instanceof InstanceFieldRef) {
                        // Fix references to fields
                        InstanceFieldRef r = (InstanceFieldRef)value;
                        if(object != analysis.getObject((Local)r.getBase())) {
                         //    System.out.println("object = " + object);
//                             System.out.println("analysis object = " + analysis.getObject((Local)r.getBase()));
//                             System.out.println("not equal!");        
                            continue;
                        }
                        if (SootUtilities.derivesFrom(oldClass, r.getField().getDeclaringClass()) &&
                            newClass.declaresFieldByName(r.getField().getName())) {
                            r.setField(newClass.getFieldByName(
                                               r.getField().getName()));
                            //   System.out.println("fieldRef = " +
                            //              box.getValue());
                  //       } else if (r.getField().getDeclaringClass().getName()
//                                 .startsWith(oldClass.getName())) {
//                             SootClass changeClass =
//                                 _getInnerClassCopy(oldClass,
//                                         r.getField().getDeclaringClass(),
//                                         newClass);
//                             r.setField(changeClass.getFieldByName(
//                                     r.getField().getName()));
                        }
                    } else if (value instanceof CastExpr) {
                        // Fix casts
                        CastExpr r = (CastExpr)value;
                        try {
                            if(object != analysis.getObject((Local)r.getOp())) {
                                continue;
                            }
                        } catch (Exception ex) {
                            if(_debug) {
                                System.out.println("Exception on cast = " + ex);
                            }
                            continue;
                        }
                        Type type = r.getType();
                        if (type instanceof RefType) {
                            SootClass refClass =
                                ((RefType)type).getSootClass();
                            if (refClass == oldClass) {
                                r.setCastType(RefType.v(newClass));
                                //    System.out.println("newValue = " +
                                //        box.getValue());
//                             } else if (refClass.getName().startsWith(
//                                     oldClass.getName())) {
//                                 SootClass changeClass =
//                                     _getInnerClassCopy(oldClass,
//                                             refClass, newClass);
//                                 r.setCastType(RefType.v(changeClass));
                            }
                        }
                                //     } else if (value instanceof ThisRef) {
//                         // Fix references to 'this'
//                         ThisRef r = (ThisRef)value;
//                         Type type = r.getType();
//                         if (type instanceof RefType &&
//                                 ((RefType)type).getSootClass() == oldClass) {
//                             box.setValue(Jimple.v().newThisRef(
//                                     RefType.v(newClass)));
//                         }
//                     } else if (value instanceof ParameterRef) {
//                         // Fix references to a parameter
//                         ParameterRef r = (ParameterRef)value;
//                         Type type = r.getType();
//                         if (type instanceof RefType &&
//                                 ((RefType)type).getSootClass() == oldClass) {
//                             box.setValue(Jimple.v().newParameterRef(
//                                     RefType.v(newClass), r.getIndex()));
//                         }
                    } else if (value instanceof InstanceInvokeExpr) {
                        // Fix up the method invokes.
                        InstanceInvokeExpr r = (InstanceInvokeExpr)value;
                        try {
                            if(object != analysis.getObject((Local)r.getBase())) {
//                                 System.out.println("object = " + object);
//                                 System.out.println("analysis object = " + analysis.getObject((Local)r.getBase()));
//                                 System.out.println("not equal!");
                  
                                continue;
                            }
                        } catch (Exception ex) {
                            if(_debug) {
                                System.out.println("Exception on invoke = " + ex);
                            }
                            continue;
                        }
                        if (SootUtilities.derivesFrom(oldClass, r.getMethod().getDeclaringClass())) {
                            if(newClass.declaresMethod(r.getMethod().getSubSignature())) {        
                                SootMethod replacementMethod = 
                                    newClass.getMethod(
                                            r.getMethod().getSubSignature());
                                r.setMethod(replacementMethod);
                            }
                            //                         } else if (r.getMethod().getDeclaringClass().getName().
//                                 startsWith(oldClass.getName())) {
//                             SootClass changeClass =
//                                 _getInnerClassCopy(oldClass,
//                                         r.getMethod().getDeclaringClass(),
//                                         newClass);
//                             r.setMethod(changeClass.getMethod(
//                                     r.getMethod().getSubSignature()));
                        }
                    } else if (value instanceof NewExpr) {
                        // Fix up the object creations.
                        NewExpr r = (NewExpr)value;
                        if(!(unit instanceof AssignStmt)) {
                            continue;
                        }
                        AssignStmt stmt = (AssignStmt)unit;
                        try {
                            if(object != analysis.getObject((Local)stmt.getLeftOp())) {
                                continue;
                            }
                        } catch (Exception ex) {
                            if(_debug) {
                                System.out.println("Exception on new = " + ex);
                            }
                            continue;
                        }
                        if (r.getBaseType().getSootClass() == oldClass) {
                            r.setBaseType(RefType.v(newClass));
                            //   System.out.println("newValue = " +
                            //           box.getValue());
//                         } else if (r.getBaseType().getSootClass().getName().
//                                 startsWith(oldClass.getName())) {
//                             SootClass changeClass =
//                                 _getInnerClassCopy(oldClass,
//                                         r.getBaseType().getSootClass(),
//                                         newClass);
//                             r.setBaseType(RefType.v(changeClass));
                        }
                    }
                    //    System.out.println("value = " + value);
                    //   System.out.println("class = " +
                    //            value.getClass().getName());
                }
                //   System.out.println("unit = " + unit);
            }
        }        
    }

    private CompositeActor _model;
    private boolean _debug;
    private Map _options;
    private String _phaseName;
}














