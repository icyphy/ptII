/*

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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import soot.Local;
import soot.Modifier;
import soot.RefType;
import soot.SootClass;
import soot.SootMethod;
import soot.VoidType;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.util.Chain;

//////////////////////////////////////////////////////////////////////////
//// EntitySootClass

/**

 @author Stephen Neuendorffer, Christopher Hylands
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class EntitySootClass extends SootClass {
    public EntitySootClass(SootClass superClass, String name, int modifier) {
        super(name, modifier);
        setSuperclass(superClass);

        /*Type stringType = */RefType.v("java.lang.String");
        /*Type compositeEntityType = */RefType
        .v("ptolemy.kernel.CompositeEntity");
        /*Type workspaceType = */RefType.v("ptolemy.kernel.util.Workspace");

        _initMethod = new SootMethod("__CGInit", new LinkedList(),
                VoidType.v(), Modifier.PUBLIC);
        addMethod(_initMethod);

        // Now create constructors to call the superclass constructors,
        // and then the __CGInit method.
        for (Iterator methods = getSuperclass().getMethods().iterator(); methods
                .hasNext();) {
            SootMethod method = (SootMethod) methods.next();

            if (!method.getName().equals("<init>")) {
                continue;
            }

            // create the new constructor.
            SootMethod constructor = _createConstructor(this, method);
            JimpleBody body = (JimpleBody) constructor.getActiveBody();
            Chain units = body.getUnits();
            Local thisLocal = body.getThisLocal();

            // Call the __CGInit method.
            units.add(Jimple.v().newInvokeStmt(
                    Jimple.v().newVirtualInvokeExpr(thisLocal,
                            _initMethod.makeRef())));

            // return void
            units.add(Jimple.v().newReturnVoidStmt());
        }
    }

    public SootMethod getInitMethod() {
        return _initMethod;
    }

    // Create a constructor in theClass that has the same signature
    // as the given method.
    // Add instructions to the body of the constructor that call the
    // given method with the same arguments, and then calls the
    // shared _CGInit initialization method.
    private SootMethod _createConstructor(SootClass theClass,
            SootMethod superConstructor) {
        // Create the constructor.
        SootMethod constructor = new SootMethod("<init>",
                superConstructor.getParameterTypes(),
                superConstructor.getReturnType(),
                superConstructor.getModifiers());

        theClass.addMethod(constructor);

        // System.out.println("creating constructor = " +
        //        constructor.getSignature());
        // create empty body
        JimpleBody body = Jimple.v().newBody(constructor);

        // Add this and read the parameters into locals
        body.insertIdentityStmts();
        constructor.setActiveBody(body);

        Chain units = body.getUnits();
        Local thisLocal = body.getThisLocal();

        // get a list of the locals that reference the parameters of the
        // constructor.  What a nice hack.
        List parameterList = new ArrayList();
        parameterList.addAll(body.getLocals());
        parameterList.remove(thisLocal);

        // Call the super constructor.
        units.add(Jimple.v().newInvokeStmt(
                Jimple.v().newSpecialInvokeExpr(thisLocal,
                        superConstructor.makeRef(), parameterList)));

        return constructor;
    }

    private SootMethod _initMethod;
}
