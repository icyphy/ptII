/* A type polymorphic FIR filter.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Yellow (neuendor@eecs.berkeley.edu)
@AcceptedRating Yellow (neuendor@eecs.berkeley.edu)
*/

package ptolemy.copernicus.jhdl.test;

import java.util.List;

import ptolemy.copernicus.jhdl.util.*;

import ptolemy.actor.Director;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.data.ArrayToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.FixToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;


import soot.*;
import soot.jimple.*;
import soot.toolkits.graph.*;

public class Test {

    /**
     * This static String constant specifies the default class name
     * used for all testing of JHDL code generation files.
     **/
    public static final String DEFAULT_TESTCLASS =
    "ptolemy.copernicus.jhdl.test.test1";

    /**
     * This static String constant specifies the default method name
     * used for all testing of JHDL code generation files.
     **/
    public static final String DEFAULT_TESTMETHOD = "method1";

    /**
     * This method will return the SootClass object associated with
     * the given fully-qualified String class name.
     **/
    public static SootClass getApplicationClass(String classname) {
        SootClass entityClass = Scene.v().loadClassAndSupport(classname);
        entityClass.setApplicationClass();
        return entityClass;
    }

    /**
     * This static method returns the SootMethod object associated
     * with the class and method name specified in the String
     * arguments. The method/class name are specified
     * as command line arguments as follows:
     * <ul>
     * <li> args[0] = the class name to analyze
     * <li> args[1] = the name of the method to analyze
     * </ul>
     * The following defaults are used if the arguments are not
     * provided:
     * <ul>
     * <li> default classname = class name specified by the static final
     *      string variable {@link Test#DEFAULT_TESTCLASS}.
     * <li> default methodname = the method name specified by the static
     *      final string variable {@link Test#DEFAULT_TESTMETHOD}.
     * </ul>
     *
     * @see Test#getApplicationClass(String)
     **/
    public static soot.SootMethod getSootMethod(String args[]) {
        String classname = DEFAULT_TESTCLASS;
        String methodname = DEFAULT_TESTMETHOD;
        if (args.length > 0)
            classname = args[0];
        if (args.length > 1)
            methodname = args[1];

        soot.SootClass testClass =
            ptolemy.copernicus.jhdl.test.Test.getApplicationClass(classname);
        if (testClass == null) {
            System.err.println("Class "+classname+" not found");
            System.exit(1);
        }
        System.out.println("Loading class "+classname+" method "+methodname);
        if (!testClass.declaresMethodByName(methodname)) {
            System.err.println("Method "+methodname+" not found");
            System.exit(1);
        }

        return testClass.getMethodByName(methodname);
    }

    /**
     * This static method will return the soot Body object associated
     * with the method defined by the String arguments.
     *
     * @see Test#getSootMethod(String[])
     **/
    public static soot.Body getSootBody(String args[]) {
        soot.SootMethod testMethod = getSootMethod(args);
        return testMethod.retrieveActiveBody();
    }

    /**
     * This method returns an array of Block objects for
     * a given Method from a class. Each Block object corresponds
     * to a basic block in the control-flow graph.
     *
     * @param args Specifies the Classname (args[0]) and the
     * Methodname (args[1]).
     *
     * @see Test#getSootMethod(String[])
     **/
    public static Block[] getMethodBlocks(String args[]) {

        soot.SootMethod testMethod = getSootMethod(args);
        soot.Body body = testMethod.retrieveActiveBody();

        BriefBlockGraph bbgraph = new BriefBlockGraph(body);
        //BlockGraphToDotty.writeDotFile("cfg",bbgraph);
        List l = bbgraph.getBlocks();
        Block[] b = new Block[l.size()];
        return (Block[]) l.toArray(b);
    }

}

