/* A class used to build a JHDL Class Template

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

//import ptolemy.copernicus.kernel.KernelMain;

import soot.SootClass;
import soot.SootMethod;
import soot.SootField;
import soot.Type;
import soot.RefType;
import soot.ArrayType;
import soot.Scene;
import soot.Local;
import soot.VoidType;
import soot.Modifier;
import soot.Unit;
import soot.Value;

import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.Stmt;
import soot.jimple.internal.JimpleLocal;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.NewArrayExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.IntConstant;
import soot.jimple.AssignStmt;
import soot.jimple.StringConstant;

import soot.util.Chain;

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;

import byucc.jhdl.base.Node;

//////////////////////////////////////////////////////////////////////////
//// JHDLClassBuilder
/**

@author Michael Wirthlin, Stephen Neuendorffer, Edward A. Lee, Christopher Hylands
@version $Id$
@since Ptolemy II 2.0
*/

class JHDLClass extends SootClass  {

    public JHDLClass(String name, int modifiers) {
        super(name,modifiers);
    }

    public JHDLClass(String name) {
        super(name);
    }

    public static void initClass(SootClass c) {
        SootClass logicClass =
            Scene.v().loadClassAndSupport("byucc.jhdl.Logic.Logic");
        SootClass nodeClass =
            Scene.v().loadClassAndSupport("byucc.jhdl.base.Node");
        SootClass wireClass = Scene.v().loadClassAndSupport(
                "byucc.jhdl.base.Wire");

        // set class to extend byucc.jhdl.Logic
        c.setSuperclass(logicClass);


        // create a constructor with (Node parent) as arguments
        // (keep a reference to this call for modifications at
        //  a later time)
        List args = new LinkedList();
        //            Local nodeArg = Jimple.v().newLocal("parent",
        //                                                nodeClass.getType());
        args.add(nodeClass.getType());

        SootMethod constructor = new SootMethod("<init>", args,
                VoidType.v(),Modifier.PUBLIC);
        SootMethod superConstructor = logicClass.getMethod(
                "void <init>(byucc.jhdl.base.Node)");

          c.addMethod(constructor);
        JimpleBody body = Jimple.v().newBody(constructor);
        constructor.setActiveBody(body);
        // add local Wire for return on connect calls
            Local tempWire = Jimple.v().newLocal(tempWireName,
                wireClass.getType());

        body.insertIdentityStmts();

        // Add statements to constructor
        Chain units = body.getUnits();

        // add call to superclass constructor
        Chain locals = body.getLocals();
        Local parent_local = null;
          for (Iterator ls = locals.iterator();ls.hasNext();) {
              JimpleLocal local = (JimpleLocal) ls.next();
            if (local.getType().equals(nodeClass.getType()))
                parent_local = local;

            //              System.out.println("Local name="+local.getType());
          }
        //          System.out.println("this = "+parent_local);


        args = new LinkedList();
        args.add(parent_local);
        SpecialInvokeExpr sie = Jimple.v().newSpecialInvokeExpr(
                body.getThisLocal(),
                superConstructor,
                args);
        body.getUnits().add(Jimple.v().newInvokeStmt(sie));

        // Add a return statement
        body.getUnits().add(Jimple.v().newReturnVoidStmt());

        // Now add cell interface

        SootClass cellInterfaceClass = Scene.v().loadClassAndSupport(
                "byucc.jhdl.base.CellInterface");
        // Create a one-dimensional array type of instances of CellInterface.
        Type interfaceType = ArrayType.v(RefType.v(cellInterfaceClass), 1);
        // Add a public static field for the cell interface.
        SootField interfaceField = new SootField("cell_interface",
                interfaceType,
                Modifier.PUBLIC | Modifier.STATIC);
        c.addField(interfaceField);

        // add static initializer for class (cell interface initialization)
        // Create a static initializer for the static field.
        SootMethod staticInitializerMethod;
        staticInitializerMethod = new SootMethod("<clinit>",
                new LinkedList(), VoidType.v(),
                Modifier.STATIC);
        c.addMethod(staticInitializerMethod);
        body = Jimple.v().newBody(staticInitializerMethod);
        staticInitializerMethod.setActiveBody(body);
        body.insertIdentityStmts();
        body.getUnits().add(Jimple.v().newReturnVoidStmt());

        units = body.getUnits();
        // In Java byte code, you have to populate a local variable
        // in the static initializer before setting the static field.
        // Create that local variable for each member fo the array.
        Local cellInterfaceLocal =
            Jimple.v().newLocal(cellInterfaceLocalName,
                    RefType.v(cellInterfaceClass));
        body.getLocals().add(cellInterfaceLocal);

        // Temporary for the array.
        Local interfaceArrayLocal =
            Jimple.v().newLocal(cellInterfaceArrayLocalName,
                    interfaceType);
        body.getLocals().add(interfaceArrayLocal);
        body.insertIdentityStmts();

        // Create a size one array by default.
        // We will come back and backpatch this later with the actual number of
        // values.
        NewArrayExpr arrayConstructionStmt = Jimple.v().newNewArrayExpr(
                RefType.v(cellInterfaceClass), IntConstant.v(0));
        // Insert before the return statement at the end.
        units.insertBefore(Jimple.v().newAssignStmt(
                interfaceArrayLocal, arrayConstructionStmt),
                units.getLast());

        // assign array to static field
        units.insertBefore(Jimple.v().newAssignStmt(
                Jimple.v().newStaticFieldRef(interfaceField),
                interfaceArrayLocal),
                units.getLast());
    }

    public void addPort(String portname, boolean input, int width) {
        addPort(this,portname,input,width);
    }

    public static void addPort(SootClass c, String portname,
            boolean input, int width) {

        SootClass cellClass = Scene.v().loadClassAndSupport(
                "byucc.jhdl.base.Cell");

        // 1. Add a public Wire field for the input wire
        // 2. Add a parameter to the constructor
        // 3. Update the cell interface
        // 4. Add a call to connect

        // Add public Wire field
        SootClass wireClass = Scene.v().loadClassAndSupport(
                "byucc.jhdl.base.Wire");
        SootField wireField = new SootField(portname,
                wireClass.getType(),
                Modifier.PUBLIC);
        c.addField(wireField);

        // Add a parameter to the constructor
        SootMethod constructor = c.getMethod(
                "void <init>(byucc.jhdl.base.Node)");
        List constructor_params = constructor.getParameterTypes();
        constructor_params.add(wireClass.getType());
        int num_params = constructor_params.size();
        constructor.setParameterTypes(constructor_params);
        JimpleBody c_body = (JimpleBody)constructor.retrieveActiveBody();
        c_body.insertIdentityStmts();

        // add calls to connect in constructor
        // get field member
        Chain c_units = c_body.getUnits();

        SootMethod connectMethod = cellClass.getMethod(
                "byucc.jhdl.base.Wire "
                + "connect(java.lang.String, byucc.jhdl.base.Wire)");

        Local paramWire = c_body.getParameterLocal(num_params-1);

        InvokeExpr invoke =
            Jimple.v().newStaticInvokeExpr(connectMethod,
                    StringConstant.v(portname),
                    paramWire);
        // find temporary wire local
        Chain locals = c_body.getLocals();
        Local localWire = null;
          for (Iterator ls = locals.iterator();ls.hasNext();) {
              JimpleLocal local = (JimpleLocal) ls.next();
            if (local.getName().equals(tempWireName));
            localWire = local;
          }

        //            c_units.insertBefore(Jimple.v().newInvokeStmt(invoke),
        //                                 c_units.getLast());
           c_units.insertBefore(Jimple.v().newAssignStmt(
                localWire,invoke),c_units.getLast());
        c_units.insertBefore(Jimple.v().newAssignStmt(
                Jimple.v().newInstanceFieldRef(c_body.getThisLocal(),
                        wireField),
                localWire), c_units.getLast());



        // Update Cell interface
        SootMethod initializer = c.getMethodByName("<clinit>");
        JimpleBody si_body = (JimpleBody)initializer.retrieveActiveBody();
        Chain si_units = si_body.getUnits();
        // search for array creation (need to increase size)
        int i_size=0;
        for (Iterator i=si_units.iterator();i.hasNext();) {
            Unit unit = (Unit) i.next();
            if (!(unit instanceof AssignStmt))
                continue;
            Value right_op = ((AssignStmt)unit).getRightOp();
            if (!(right_op instanceof NewArrayExpr))
                continue;
            // update array size by one
            Value size = ((NewArrayExpr)right_op).getSize();
            if (size instanceof IntConstant) {
                i_size = ((IntConstant)size).hashCode();
                //                  System.out.println("hascode="+i_size);
                ((NewArrayExpr)right_op).setSize(IntConstant.v(i_size+1));
            }
        }
        SootMethod inFactoryMethod = cellClass.getMethod(
                "byucc.jhdl.base.CellInterface in(java.lang.String,int)");
        SootMethod outFactoryMethod = cellClass.getMethod(
                "byucc.jhdl.base.CellInterface out(java.lang.String,int)");
        SootMethod portMethod = (input ? inFactoryMethod : outFactoryMethod);
        // find temporary locals
        Local cellInterfaceLocal = null;
        Local interfaceArrayLocal = null;
        for (Iterator i=si_body.getLocals().iterator();i.hasNext();) {
            Local l = (Local) i.next();
            if (l.getName().equals(cellInterfaceLocalName))
                cellInterfaceLocal = l;
            if (l.getName().equals(cellInterfaceArrayLocalName))
                interfaceArrayLocal = l;
        }
        // assignment statement to temporary local
        Stmt newport_stmt = Jimple.v().newAssignStmt(
                cellInterfaceLocal,
                Jimple.v().newStaticInvokeExpr(
                        inFactoryMethod,
                        StringConstant.v(portname),
                        IntConstant.v(width)));
          si_units.insertBefore(newport_stmt, si_units.getLast());
        // assign local to array
        si_units.insertBefore(Jimple.v().newAssignStmt(
                Jimple.v().newArrayRef(interfaceArrayLocal,
                        IntConstant.v(i_size)),
                cellInterfaceLocal),
                si_units.getLast());


    }

    // Initialize the class to a bare bones JHDL class
    protected void initClass() {
        initClass(this);
    }

    protected SootMethod constructor;

    public static void main(String[] args) {
        //          args[0] = "java.lang.Object";
        //          soot.Main.main(args);
        //          JHDLClass jclass = new JHDLClass("mycircuit");
          JHDLClass jclass = new JHDLClass("mycircuit",Modifier.PUBLIC);
          Scene.v().addClass(jclass);
        jclass.initClass();
        jclass.addPort("datain", true, 8);
        jclass.addPort("dataout",false, 8);
          jclass.write();
    }

    public static final String cellInterfaceLocalName =
    "_tempCellInterface";
    public static final String cellInterfaceArrayLocalName =
    "_tempCellInterfaceArray";
    public static final String tempWireName =
    "tempWire";
}
