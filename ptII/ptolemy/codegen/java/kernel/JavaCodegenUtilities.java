/* A utility class containing methods for c code generation.

 Copyright (c) 2007-2009 The Regents of the University of California.
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

package ptolemy.codegen.java.kernel;

/**
A utility class used to simplify creating c templates in EmbeddedCActors.

@author Teale Fristoe
@version $Id$
@since Ptolemy II 8.0
@Pt.ProposedRating red (tbf)
@Pt.AcceptedRating
*/
public class JavaCodegenUtilities {

    /** Return a code block to define a constant.
     *  @param constant The name of the constant.
     *  @param value The value of the constant.
     *  @return A block of codegen code to define a constant.
     */
    public static String getDefineBlock(String constant, String value) {
        String code = "#ifndefine " + constant + "\n" + "#define " + constant
                + " " + value + "\n" + "#endif\n";
        return code;
    }

    /** Return a code block to include a file.
     *  @param file The name of the file.
     *  @param constant The name of the constant to check to see if the file
     *          has already been included.
     *  @return A block of codegen code to include a file.
     */
    public static String getIncludeBlock(String file, String constant) {
        String code = "#ifndefine " + constant + "\n" + "#define " + constant
                + "\n" + "#include \"" + file + "\"\n" + "#endif\n";
        return code;
    }
}
