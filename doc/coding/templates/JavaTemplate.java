/* One line description of the class.

 Copyright (c) 1999-2003 The Regents of the University of California.
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
@ProposedRating Red (yourname@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)
*/

package MyPackageName;

// Imports go here, in alphabetical order, with no wildcards.

//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
Describe your class here, in complete sentences.
What does it do?  What is its intended use?

@author yourname
@version $Id$
@see classname (refer to relevant classes, but not the base class)
*/

public class ClassName {
    
    /** Create an instance with ... (describe the properties of the
     *  instance). Use the imperative case here.
     *  @param parameterName Description of the parameter.
     *  @exception ExceptionClass If ... (describe what
     *   causes the exception to be thrown).
     */
    public ClassName(ParameterClass parameterName) throws ExceptionClass {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////
    
    /** Desription of the variable. */
    public int variableName;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Do something... (Use the imperative case here, such as:
     *  "Return the most recently recorded event.", not
     *  "Returns the most recently recorded event."
     *  @param parameterName Description of the parameter.
     *  @return Description of the returned value.
     *  @exception ExceptionClass If ... (describe what
     *   causes the exception to be thrown).
     */
    public int publicMethodName(ParameterClass parameterName)
            throws ExceptionClass {
        return 1;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Describe your method, again using imperative case.
     *  @see RelevantClass#methodName()
     *  @param parameterName Description of the parameter.
     *  @return Description of the returned value.
     *  @exception ExceptionClass If ... (describe what
     *   causes the exception to be thrown).
     */
    protected int _protectedMethodName(ParameterClass parameterName)
            throws ExceptionClass {
        return 1;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Description of the variable. */
    protected int _aProtectedVariable;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Private methods need not have Javadoc comments, although it can
    // be more convenient if they do, since they may at some point
    // become protected methods.
    private int _privateMethodName() {
        return 1;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Private variables need not have Javadoc comments, although it can
    // be more convenient if they do, since they may at some point
    // become protected variables.
    private int _aPrivateVariable;
}
