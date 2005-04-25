/* Test for type specialization.

Copyright (c) 2004-2005 The Regents of the University of California.
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
package ptolemy.copernicus.java.test;

import soot.Scene;
import soot.SootClass;

import ptolemy.copernicus.java.TypeSpecializerAnalysis;
import ptolemy.copernicus.kernel.PtolemyUtilities;

import java.util.HashSet;
import java.util.Iterator;


//////////////////////////////////////////////////////////////////////////
//// TestSpecializeTypesMain

/**
   Test for type specialization.

   @author Stephen Neuendorffer
   @version $Id$
   @since Ptolemy II 4.1
   @Pt.ProposedRating Red (cxh)
   @Pt.AcceptedRating Red (cxh)
*/
public class TestSpecializeTypesMain {
    /** First argument is the output directory.
     *  Second argument is the class name.
     */
    public static void main(String[] args) {
        PtolemyUtilities.loadSootReferences();

        SootClass theClass = Scene.v().loadClassAndSupport(args[0]);
        theClass.setApplicationClass();

        TypeSpecializerAnalysis analysis = new TypeSpecializerAnalysis(theClass,
                new HashSet());

        for (Iterator variables = analysis.getSolverVariables();
                        variables.hasNext();) {
            System.out.println(variables.next().toString());
        }
    }
}
