/* Test file for ASTReflect

 Copyright (c) 2000 The Regents of the University of California.
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

package ptolemy.lang.java.test;

import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Debuggable;

//////////////////////////////////////////////////////////////////////////
//// ReflectTest 

/** This class is a test input file to ASTReflect

@author Christopher Hylands
@version $Id$
@see ptolemy.lang.java.ASTReflect
*/
public class ReflectTest extends NamedObj implements Debuggable {
    ReflectTest() {}
    ReflectTest(int a) {}
    ReflectTest(int[][]b) {}

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public static void publicMethod1() {}
    public void publicMethod2(int a) {}
    public void publicMethod2(NamedObj [] a) {}
    public void publicMethod2(NamedObj [][] a) {}

    public class innerPublicClass {
	innerPublicClass() {}
	public int [][][] innerPublicClassPublicMethod(NamedObj n) {
	    int foo [][][] = {
		{{1,2},{3,4}},
		{{5,6},{7,8}}
	    };	 
	    return foo;
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    protected NamedObj _publicField;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private final int _privateField = 0;
}
