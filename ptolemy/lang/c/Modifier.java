/* Handle modifiers such as 'public'.



 Copyright (c) 2001 The University of Maryland.

 All rights reserved.

 Permission is hereby granted, without written agreement and without

 license or royalty fees, to use, copy, modify, and distribute this

 software and its documentation for any purpose, provided that the above

 copyright notice and the following two paragraphs appear in all copies

 of this software.



 IN NO EVENT SHALL THE UNIVERSITY OF MARYLAND BE LIABLE TO ANY PARTY

 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES

 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF

 THE UNIVERSITY OF MARYLAND HAS BEEN ADVISED OF THE POSSIBILITY OF

 SUCH DAMAGE.



 THE UNIVERSITY OF MARYLAND SPECIFICALLY DISCLAIMS ANY WARRANTIES,

 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF

 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE

 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF

 MARYLAND HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,

 ENHANCEMENTS, OR MODIFICATIONS.



                                        PT_COPYRIGHT_VERSION_2

                                        COPYRIGHTENDKEY



@ProposedRating Red (ssb@eng.umd.edu)

@AcceptedRating Red (ssb@eng.umd.edu)

*/



package ptolemy.lang.c;

import ptolemy.lang.java.JavaStaticSemanticConstants;



//////////////////////////////////////////////////////////////////////////

//// Modifier

/** This class contains helper methods for use in C code

 *  generation to interpret constants defined in JavaStaticSemanticConstants. 

 *  @author Shuvra S. Bhattacharyya 

 *  @version $Id$
 */

public class Modifier implements JavaStaticSemanticConstants {



    /**

       Convert a Java modifier into a string in the context of

       C code generation. Suppress the following modifiers, which

       either do not have equivalents in C, or do not require back-end

       code generation support:



          public, protected, private, final, abstract, native,

          synchronized, transient, strictfp.



       FIXME: The above list should be reviewed carefully to make

       sure we don't need to support these in any way.



       @param modifier The Java modifier that is to be converted to a string.

       @return The string that results from converting the modifier.

    */

    public static final String toString(final int modifier) {

        StringBuffer modString = new StringBuffer();



        if (modifier == NO_MOD) return "";



        if ((modifier & VOLATILE_MOD) != 0)

            modString.append("volatile ");



        if ((modifier & STATIC_MOD) != 0)

            modString.append("static ");



        return modString.toString();

    }

}



