/* Transform Actors using Soot

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

package ptolemy.copernicus.jhdl;

import ptolemy.actor.*;
import ptolemy.copernicus.kernel.SootUtilities;
import ptolemy.copernicus.java.ModelTransformer;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.moml.*;

import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.invoke.SiteInliner;
import soot.jimple.toolkits.invoke.StaticInliner;
//import soot.jimple.toolkits.invoke.InvokeGraphBuilder;
import soot.jimple.toolkits.scalar.ConditionalBranchFolder;
import soot.jimple.toolkits.scalar.ConstantPropagatorAndFolder;
import soot.jimple.toolkits.scalar.CopyPropagator;
import soot.jimple.toolkits.scalar.DeadAssignmentEliminator;
import soot.jimple.toolkits.scalar.UnreachableCodeEliminator;
import soot.jimple.toolkits.scalar.Evaluator;
import soot.toolkits.graph.*;
import soot.dava.*;
import soot.util.*;
import java.io.*;
import java.util.*;



//////////////////////////////////////////////////////////////////////////
//// JHDLTransformer
/**

@author Michael Wirthlin, Stephen Neuendorffer, Edward A. Lee, Christopher Hylands
@version $Id$
@since Ptolemy II 2.0
*/

class JHDLTransformer extends SceneTransformer {

    private JHDLTransformer(CompositeActor model) {
        // If this fails, then JHDL*.jar is probably not
        // in the classpath.  We call this here so that
        // we find out sooner rather than later.
        try {
            // We check this at run time, not compile time so that
            // GeneratorTableau will compile if JHDL is not present.
            Class.forName("byucc.jhdl.Version");
        } catch (ClassNotFoundException error) {
            // To test this, run make JHDL_JAR= demo
            throw new NoClassDefFoundError("byucc.jhdl.Version."
                    + "GetFullVersion() not "
                    + "found.  Perhaps the JHDL jar file is not in your path. "
                    + "It can be found at "
                    + " http://www.jhdl.org/release-latest/bleedingedge.html"
                    + "Download it into $PTII/vendors/jhdl/ and rerun"
                    + "$PTII/configure.  Error was: " + error);
        }

        _model = model;
    }

    /** Return an instance of this transformer that will operate on
     *  the given model.  The model is assumed to already have been
     *  properly initialized so that resolved types and other static
     *  properties of the model can be inspected.
     */
    public static JHDLTransformer v(CompositeActor model) {
        // FIXME: This should use a map to return a singleton instance
        // for each model
        return new JHDLTransformer(model);
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
        return "deep targetPackage";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Do the transformation. This method is called by soot.
     *  @param phaseName The name that the transformer was created with,
     *   such as "wjtp.jhdl".
     *  @param options Options to the transformer, specified on the command
     *   line to soot, or when the transformation is created.
     */
    protected void internalTransform(String phaseName, Map options) {
        System.out.println("JHDLTransformer.internalTransform("
                + phaseName + ", " + options + ")");
        // Get a bunch of classes that we will need.
        SootClass namedObjClass = Scene.v().loadClassAndSupport(
                "ptolemy.kernel.util.NamedObj");
        SootClass attributeClass = Scene.v().loadClassAndSupport(
                "ptolemy.kernel.util.Attribute");
        SootClass settableClass = Scene.v().loadClassAndSupport(
                "ptolemy.kernel.util.Settable");
        SootClass actorClass = Scene.v().loadClassAndSupport(
                "ptolemy.actor.TypedAtomicActor");

        // Get a bunch of types that we will need.
        Type attributeType = RefType.v(attributeClass);
        Type settableType = RefType.v(settableClass);
        Type actorType = RefType.v(actorClass);

        // Get a bunch of methods that we will need.
        SootMethod getAttributeMethod = namedObjClass.getMethod(
                "ptolemy.kernel.util.Attribute getAttribute(java.lang.String)");
        SootMethod setExpressionMethod = settableClass.getMethodByName(
                "setExpression");

        // iterate over entities in the model
        for (Iterator entities = _model.entityList().iterator();
             entities.hasNext();) {
            Entity theEntity = (Entity) entities.next();
            String entityName = theEntity.getName();
            String newClassName =
                ModelTransformer.getInstanceClassName(theEntity, options);
            SootClass theClass = Scene.v().loadClassAndSupport(newClassName);

            List entityPorts = theEntity.portList();

            // Currently not modifying super class since it breaks on
            // the call to super in the constructor
            //              _modifySuperClass(theClass);

            // Add Wire fields for each port.
            _addWireFields(theClass, entityPorts);

            // Add a static member that defines the JHDL interface
            // to the cell (where the cell is the component).
            _addCellInterface(theClass, entityPorts);

            _addConnectCalls(theClass, entityPorts);

            // Add a clock() method, which in JHDL is the action method.
            _addClockMethod(theClass);

            //              _modifyClockMethod(theClass, entityPorts);

        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Add a static member that defines the JHDL interface
    // to the cell (where the cell is the component).
    private void _addCellInterface(SootClass theClass, List entityports) {

        // Get the JHDL classes that we will need.
        SootClass cellClass = Scene.v().loadClassAndSupport(
                "byucc.jhdl.base.Cell");
        SootClass cellInterfaceClass = Scene.v().loadClassAndSupport(
                "byucc.jhdl.base.CellInterface");
        SootClass ioPortClass = Scene.v().loadClassAndSupport(
                "ptolemy.actor.TypedIOPort");

        // Create a one-dimensional array type of instances of CellInterface.
        Type interfaceType = ArrayType.v(RefType.v(cellInterfaceClass), 1);

        // Add a public static field for the cell interface.
        SootField interfaceField = new SootField("cell_interface",
                interfaceType,
                Modifier.PUBLIC | Modifier.STATIC);
        theClass.addField(interfaceField);

        // Create a static initializer for the static field.
        SootMethod staticInitializerMethod;
        JimpleBody body;
        // Look for a pre-existing static initializer method, and if
        // we find it, add to it.  Otherwise, create one.
        if (theClass.declaresMethodByName("<clinit>")) {
            staticInitializerMethod = theClass.getMethodByName("<clinit>");
            body = (JimpleBody)staticInitializerMethod.retrieveActiveBody();
        } else {
            staticInitializerMethod = new SootMethod("<clinit>",
                    new LinkedList(), VoidType.v(),
                    Modifier.STATIC);
            theClass.addMethod(staticInitializerMethod);
            body = Jimple.v().newBody(staticInitializerMethod);
            staticInitializerMethod.setActiveBody(body);
            body.insertIdentityStmts();
            // Add a return statement, which a pre-existing static initializer
            // will already have.  We will add statements before the last
            // statement.
            body.getUnits().add(Jimple.v().newReturnVoidStmt());
        }

        Chain units = body.getUnits();
        // In Java byte code, you have to populate a local variable
        // in the static initializer before setting the static field.
        // Create that local variable for each member fo the array.
        Local cellInterfaceLocal =
            Jimple.v().newLocal("_tempCellInterface",
                    RefType.v(cellInterfaceClass));
        body.getLocals().add(cellInterfaceLocal);

        // Temporary for the array.
        Local interfaceArrayLocal =
            Jimple.v().newLocal("_tempCellInterfaceArray",
                    interfaceType);
        body.getLocals().add(interfaceArrayLocal);

        // Get the factory methods used to create the cell interfaces.
        SootMethod inFactoryMethod = cellClass.getMethod(
                "byucc.jhdl.base.CellInterface in(java.lang.String,int)");
        SootMethod outFactoryMethod = cellClass.getMethod(
                "byucc.jhdl.base.CellInterface out(java.lang.String,int)");

        // Create a size one array by default.
        // We will come back and backpatch this later with the actual number of
        // values.
        NewArrayExpr arrayConstructionStmt = Jimple.v().newNewArrayExpr(
                RefType.v(cellInterfaceClass), IntConstant.v(1));
        // Insert before the return statement at the end.
        units.insertBefore(Jimple.v().newAssignStmt(
                interfaceArrayLocal, arrayConstructionStmt),
                units.getLast());

        // Add a cell interface for each port
        int i = 0;
        for (Iterator ports = entityports.iterator();ports.hasNext();) {
            TypedIOPort port = (TypedIOPort) ports.next();
            // FIXME: infer the width, instead of just 32.
            // Insert before the return statement.
            // Create the element to put in the array.

            // FIXME: I am ignoring bidirectional ports and ports without
            // direction. I will need to handle these cases.
            if (port.isInput()) {
                units.insertBefore(Jimple.v().newAssignStmt(
                        cellInterfaceLocal, Jimple.v().newStaticInvokeExpr(
                                inFactoryMethod,
                                StringConstant.v(port.getName()),
                                IntConstant.v(32))),
                        units.getLast());
            } else {
                units.insertBefore(Jimple.v().newAssignStmt(
                        cellInterfaceLocal, Jimple.v().newStaticInvokeExpr(
                                outFactoryMethod,
                                StringConstant.v(port.getName()),
                                IntConstant.v(32))),
                        units.getLast());
            }

            // Put the element into the array.
            units.insertBefore(Jimple.v().newAssignStmt(
                    Jimple.v().newArrayRef(interfaceArrayLocal,
                            IntConstant.v(i)),
                    cellInterfaceLocal),
                    units.getLast());
            i++;
        }

        // Back patch the array constructor.
        arrayConstructionStmt.setSize(IntConstant.v(i));

        units.insertBefore(Jimple.v().newAssignStmt(
                Jimple.v().newStaticFieldRef(interfaceField),
                interfaceArrayLocal),
                units.getLast());
    }

    private void _addClockMethod(SootClass theClass) {
        // Create a new clock method, which is the JHDL
        // equivalent of prefire(), fire(), postfire().
        SootMethod clockMethod = new SootMethod("clock",
                new LinkedList(), VoidType.v(),
                Modifier.PUBLIC);
        theClass.addMethod(clockMethod);
        JimpleBody body = Jimple.v().newBody(clockMethod);
        clockMethod.setActiveBody(body);
        body.insertIdentityStmts();
        Chain units = body.getUnits();
        // Get the local variable for "this".
        Local thisLocal = body.getThisLocal();

        // Insert a call to prefire().
        // We will later inline this method.
        SootMethod prefireMethod =
            SootUtilities.searchForMethodByName(theClass, "prefire");
        Stmt prefireStmt =
            Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(
                    thisLocal, prefireMethod, new LinkedList()));
        units.add(prefireStmt);

        // Insert a call to fire().
        SootMethod fireMethod =
            SootUtilities.searchForMethodByName(theClass, "fire");
        Stmt fireStmt =
            Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(
                    thisLocal, fireMethod, new LinkedList()));
        units.add(fireStmt);

        // Insert a call to postfire().
        SootMethod postfireMethod =
            SootUtilities.searchForMethodByName(theClass, "postfire");
        Stmt postfireStmt =
            Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(
                    thisLocal, postfireMethod, new LinkedList()));
        units.add(postfireStmt);

        // Insert a return statement.
        units.add(Jimple.v().newReturnVoidStmt());

        // Inline each of the method calls we inserted.
        // Note that we have to ensure that there is an active body
        // before we inline, since the the inlinee is probably not
        // an application class.
        prefireMethod.retrieveActiveBody();
        SiteInliner.inlineSite(prefireMethod, prefireStmt, clockMethod);
        fireMethod.retrieveActiveBody();
        SiteInliner.inlineSite(fireMethod, fireStmt, clockMethod);
        postfireMethod.retrieveActiveBody();
        SiteInliner.inlineSite(postfireMethod, postfireStmt, clockMethod);

        // Remove the prefire(), fire(), and postfire() methods,
        // if they are declared in this class (vs. inherited).
        if (prefireMethod.getDeclaringClass() == theClass) {
            theClass.removeMethod(prefireMethod);
        }
        if (fireMethod.getDeclaringClass() == theClass) {
            theClass.removeMethod(fireMethod);
        }
        if (postfireMethod.getDeclaringClass() == theClass) {
            theClass.removeMethod(postfireMethod);
        }



    }

    /**
     * Add the connect calls to the local Wire fields to
     * the Wire arguments of the constructor. In addition,
     * make the call to super(parent).
     **/
    private void _addConnectCalls(SootClass theClass, List entityPorts) {

        SootClass cellClass = Scene.v().loadClassAndSupport(
                "byucc.jhdl.base.Cell");
        SootMethod connectMethod = cellClass.getMethod(
                "byucc.jhdl.base.Wire "
                + "connect(java.lang.String,byucc.jhdl.base.Wire)");
        SootClass wireClass = Scene.v().loadClassAndSupport(
                "byucc.jhdl.base.Wire");
        Type wireType = RefType.v(wireClass);

        // iterate over all constructors
        for (Iterator methods = theClass.getMethods().iterator();
             methods.hasNext();) {
            // get constructor
            SootMethod constructor = (SootMethod)methods.next();
            // Ignore things that aren't constructors.
            if (constructor.getName() != "<init>")
                continue;
            // get body & units
            JimpleBody body = (JimpleBody) constructor.retrieveActiveBody();
            Chain units = body.getUnits();

            // iterate over all ports in constructor
            for (Iterator ports = entityPorts.iterator();ports.hasNext();) {
                TypedIOPort port = (TypedIOPort) ports.next();
                SootField wireField = theClass.getField(port.getName(), wireType);

                // can I get the local from the field directly?
                Local localWire = Jimple.v().newLocal(port.getName(),
                        wireClass.getType());
                body.getLocals().add(localWire);

                Stmt s = Jimple.v().newAssignStmt(localWire,
                        Jimple.v().newInstanceFieldRef(body.getThisLocal(),
                                wireField));
                units.insertBefore(s, units.getLast());

                InvokeExpr invoke =
                    Jimple.v().newStaticInvokeExpr(connectMethod,
                            StringConstant.v(port.getName()),
                            localWire);
                units.insertBefore(Jimple.v().newInvokeStmt(invoke),
                        units.getLast());
            }
        }
    }

    // Add Wire fields to the JHDL class.
    private void _addWireFields(SootClass theClass, List portlist) {

        // Get the JHDL classes that we will need.
        SootClass wireClass = Scene.v().loadClassAndSupport(
                "byucc.jhdl.base.Wire");
        SootClass ioPortClass = Scene.v().loadClassAndSupport(
                "ptolemy.actor.TypedIOPort");

        // Get types.
        Type wireType = RefType.v(wireClass);
        Type ioPortType = RefType.v(ioPortClass);

        // FIXME: we are assuming our coding standard.
        // That is, that the ports are public fields.
        // Instead, we should get the ports using portList() methods
        // on an instance of model.
        int i = 0;
        for (Iterator ports = portlist.iterator();ports.hasNext();) {
            TypedIOPort port = (TypedIOPort) ports.next();
            SootField newWire = new SootField(port.getName(),
                    wireType,
                    Modifier.PROTECTED);
            theClass.addField(newWire);

        }

        // FIXME: Disable modification of constructor for now. It
        // breaks the rest of the build
        /*
          // Modify the constructor to correct all initializations
          // of the ports.
          for (Iterator methods = theClass.getMethods().iterator();
          methods.hasNext();) {
          SootMethod constructor = (SootMethod)methods.next();
          // Ignore things that aren't constructors.
          if (constructor.getName() != "<init>")
          continue;
          Body body = constructor.getActiveBody();
          PatchingChain units = body.getUnits();
          Iterator unitIterator = units.snapshotIterator();
          while (unitIterator.hasNext()) {
          Stmt statement = (Stmt)unitIterator.next();
          // Remove assignments to something of type TypedIOPort.
          if (statement instanceof AssignStmt) {
          Type leftOpType =
          ((AssignStmt)statement).getLeftOp().getType();
          if (leftOpType.equals(ioPortType)) {
          units.remove(statement);
          }
          // Remove invocations of methods of TypedIOPort.
          } else if (statement instanceof InvokeStmt) {
          InvokeExpr expr = (InvokeExpr)
          ((InvokeStmt)statement).getInvokeExpr();
          if (expr instanceof InstanceInvokeExpr) {
          units.remove(statement);
          }
          }

          }
        */
    }

    /**
     * look at each statement in the clock method and replace method
     * invocations for TypedIOPorts with the appropriate Wire .get,
     * .put method.
     **/
    private void _modifyClockMethod(SootClass theClass, List portlist) {


        SootMethod clockMethod = theClass.getMethodByName("clock");
        //SootMethod clockMethod = theClass.getMethodByName("clock");
        SootClass TypedIOPortClass = Scene.v().loadClassAndSupport(
                "ptolemy.actor.TypedIOPort");
        SootClass IOPortClass = Scene.v().loadClassAndSupport(
                "ptolemy.actor.IOPort");
        SootClass wireClass = Scene.v().loadClassAndSupport(
                "byucc.jhdl.base.Wire");
        SootMethod getIOPortMethod = IOPortClass.getMethod(
                "ptolemy.data.Token get(int)");
        SootMethod sendIOPortMethod = TypedIOPortClass.getMethod(
                "void send(int, ptolemy.data.Token)");
        // search for method by name

        //            SootMethod sendIOPortMethod = ioPortClass.getMethod(
        //                      "send(int, ptolemy.data.Token)");

        Type ioPortType = RefType.v(TypedIOPortClass);
        Body body = clockMethod.getActiveBody();
        PatchingChain units = body.getUnits();
        Iterator unitIterator = units.snapshotIterator();
        while (unitIterator.hasNext()) {
            Stmt statement = (Stmt)unitIterator.next();
            if (statement instanceof InvokeStmt) {
                InvokeExpr expr = (InvokeExpr)
                    ((InvokeStmt)statement).getInvokeExpr();
                // test to see if the statement represents an instance
                // invoke expression
                if (expr instanceof InstanceInvokeExpr) {
                    // See if the invocation is done on a port
                    Local instance =
                        (Local) ((InstanceInvokeExpr)expr).getBase();
                    // System.out.println("statement="+statement+" base="+
                    // instance + " type="+instance.getType());
                    if (instance.getType()
                            .equals(TypedIOPortClass.getType())) {
                        System.out.println("Found an invoke "
                                + "instance on a port"+
                                statement);
                        SootMethod instanceMethod = expr.getMethod();
                        if (instanceMethod.equals(getIOPortMethod)) {
                            System.out.println("get on "+instance.getName());

                            Local localWire =
                                Jimple.v().newLocal(instance.getName(),
                                        wireClass.getType());
                            // SootField wireField = getField(String name,
                            //                         wireClass.getType());
                            // Jimple.v().newInstanceFieldRef(localWire,
                            //                                SootField);

                            // instancefieldref

                            /*
                              // create a method invocation to the wire.get()
                              Jimple.v().new
                              units.insertBefore(Jimple.v().newAssignStmt(
                              cellInterfaceLocal,
                              Jimple.v().newStaticInvokeExpr(inFactoryMethod,
                              StringConstant.v(field.getName()),
                              IntConstant.v(32))),
                              units.getLast());
                            */
                        } else if (instanceMethod.equals(sendIOPortMethod)) {
                            System.out.println("send");
                        }
                    }
                    // for (Iterator ports = portlist.iterator();
                    //      ports.hasNext();) {
                    //      TypedIOPort port = (TypedIOPort) ports.next();
                    // }
                }
            }
        }
    }


    /**
     * Remove the current arguments to the constructor class and
     * add the appropriate constructor calls for a Cell.
     **/
    private void _modifyConstructorArguments(SootClass theClass) {
        SootMethod constructor = theClass.getMethodByName("<init>");

        List parameterTypes = constructor.getParameterTypes();

        // now go through and remove parameter initialization
        Body body = constructor.getActiveBody();
        PatchingChain units = body.getUnits();
        Iterator unitIterator = units.snapshotIterator();
        while (unitIterator.hasNext()) {
            Stmt statement = (Stmt)unitIterator.next();
            // Remove assignments to something of type TypedIOPort.
            //              System.out.println("***** " + statement + " obj " +
            //                                 statement.getClass());
            if (statement instanceof IdentityStmt) {
                Value rightOpValue =
                    ((IdentityStmt)statement).getRightOp();
                Type rightOpType = rightOpValue.getType();
                //                    System.out.println("******* "+statement+" value = "+
                //                                         rightOpType);

                // compare type with the arguments type
                for (Iterator parameters=parameterTypes.iterator();
                     parameters.hasNext();) {

                    Type parameterType = (Type) parameters.next();
                    if (parameterType.equals(rightOpType)) {
                        // System.out.println("*** Removed statement"+statement
                        //                    +" ***");

                        units.remove(statement);
                    }
                }
            }
        }
        parameterTypes.clear();

        // now that the constructor is cleaned up, add new parameters
        // 1. Get the type of the JHDL parameters
        // 2. Update the parameter list
        // 3. Add the identity initialization
        SootClass nodeClass = Scene.v().loadClassAndSupport(
                "byucc.jhdl.base.Node");
        SootClass wireClass = Scene.v().loadClassAndSupport(
                "byucc.jhdl.base.Wire");
        Type nodeType = RefType.v(nodeClass);
        Type wireType = RefType.v(wireClass);

        parameterTypes.add(nodeType);
        //          parameterTypes.add(wireType);
        //          parameterTypes.add(wireType);
        //          parameterTypes.add(wireType);

        // it doesn't look like I have to add identity statements as
        // they are added by some following step.

        // make call to super

    }

    // Remove all public fields of type TypedIOPort.
    private void _modifySuperClass(SootClass theClass) {
        SootClass logicClass = Scene.v().getSootClass(
                "byucc.jhdl.Logic.Logic");
        theClass.setSuperclass(logicClass);
    }

    // Remove all public fields of type TypedIOPort.
    private void _removePortFields(SootClass theClass) {

        SootClass ioPortClass = Scene.v().loadClassAndSupport(
                "ptolemy.actor.TypedIOPort");
        Type ioPortType = RefType.v(ioPortClass);

        int i = 0;
        for (Iterator fields = theClass.getFields().snapshotIterator();
             fields.hasNext();) {

            SootField field = (SootField)fields.next();
            if (field.getType().equals(RefType.v(ioPortClass))) {
                theClass.removeField(field);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private fields                        ////

    private CompositeActor _model;
}
