/*
Constants associated with translation of Java abstract syntax trees
into C code. 

Copyright (c) 2001 The University of Maryland.  All rights reserved.

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
//// CCodeGeneratorConstants 
/** Constants associated with translation of Java abstract syntax trees (ASTs)
 *  into C code. 
 *
 *  @author Shuvra S. Bhattacharyya 
 *  @version $Id$
 */
public interface CCodeGeneratorConstants extends JavaStaticSemanticConstants {

    /** The key that retrieves the indentation level associated with an AST
     *  node. The indentation level indicates how far to indent the 
     *  C statements that are generated from the associated AST node.
     *  Indentation level property values are non-negative integers.
     */
    public static final Integer INDENTATION_KEY = 
            new Integer(RESERVED_JAVA_PROPERTIES + 1);

    /** The key that indicates whether or not an AST node is associated
     *  with an indentation transistion, which means that code
     *  that "branches off" from the AST node will generally be indented
     *  one position to the right relative to the node. 
     *  This property is primarily for use while computing indentation
     *  levels. Indentation transition properties are Boolean-valued.
     */
    public static final Integer INDENTATION_TRANSITION_KEY = 
            new Integer(RESERVED_JAVA_PROPERTIES + 2);

    /** The key that gives the name to be used for the corresponding object
     *  in generated C code. For example, methods must be renamed
     *  to distinguish multiple versions with different parameter
     *  type signatures. C Name properties are String-valued.
     */
    public static final Integer C_NAME_KEY = 
            new Integer(RESERVED_JAVA_PROPERTIES + 3);
    
    /** The number of properties reserved for static resolution of Java,
     *  and C Code generation.  This number can be used to 
     *  start numbering extended properties.
     */
    public static final int RESERVED_C_PROPERTIES = 
            C_NAME_KEY.intValue() + 1;
}
