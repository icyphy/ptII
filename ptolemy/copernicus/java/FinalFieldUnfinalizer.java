/* A transformer that removes dead token and type creations.

Copyright (c) 2003-2005 The Regents of the University of California.
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

import soot.HasPhaseOptions;
import soot.Modifier;
import soot.PhaseOptions;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootField;

import java.util.Iterator;
import java.util.Map;


//////////////////////////////////////////////////////////////////////////
//// FinalFieldUnfinalizer

/**
   The code generator generates fields that are final.  These fields are
   used by the code generator to propagate final information, so that
   Soot's constant optimization will inline those fields.  Sun's JVM
   ignores the final specifier on these fields, but other JVMs do not,
   and complain that fields referencing objects are final and not
   initialized properly.  This class removes the unnecessary final
   specifiers.

   @author Stephen Neuendorffer
   @version $Id$
   @since Ptolemy II 4.0
   @Pt.ProposedRating Red (cxh)
   @Pt.AcceptedRating Red (cxh)
*/
public class FinalFieldUnfinalizer extends SceneTransformer
    implements HasPhaseOptions {
    /** Construct a new transformer
     */
    private FinalFieldUnfinalizer() {
    }

    /* Return the instance of this transformer.
     */
    public static FinalFieldUnfinalizer v() {
        return instance;
    }

    public String getPhaseName() {
        return "";
    }

    public String getDefaultOptions() {
        return "";
    }

    public String getDeclaredOptions() {
        return "debug";
    }

    protected void internalTransform(String phaseName, Map options) {
        System.out.println("FinalFieldUnfinalizer.internalTransform("
            + phaseName + ", " + options + ")");

        boolean debug = PhaseOptions.getBoolean(options, "debug");

        // Loop over all the classes...
        for (Iterator i = Scene.v().getApplicationClasses().iterator();
                        i.hasNext();) {
            SootClass theClass = (SootClass) i.next();

            // Assume that any method that is part of an interface that this
            // object implements, is reachable.
            for (Iterator fields = theClass.getFields().iterator();
                            fields.hasNext();) {
                SootField field = (SootField) fields.next();
                field.setModifiers(field.getModifiers() & ~Modifier.FINAL);
            }
        }
    }

    private static FinalFieldUnfinalizer instance = new FinalFieldUnfinalizer();
}
