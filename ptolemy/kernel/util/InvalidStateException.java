/* Some object or set of objects has a state that in theory is not permitted.

 Copyright (c) 1997- The Regents of the University of California.
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

package pt.kernel;

//////////////////////////////////////////////////////////////////////////
//// InvalidStateException
/** 
Some object or set of objects has a state that in theory is not
permitted. E.g., a NamedObj has a null name. Or a topology has
inconsistent or contradictory information in it, e.g. an entity
contains a port that has a different entity as it container. Our
design should make it impossible for this exception to ever occur,
so occurrence is a bug. This exception supports all the constructor
forms of KernelException.

@author Edward A. Lee
@version $Id$
*/
public class InvalidStateException extends KernelException {
    /** Constructs an Exception with no detail message */  
    public InvalidStateException() {
        super("Serious internal error!");
    }

    /** Constructs an Exception with a detail message */  
    public InvalidStateException(String detail) {
        super(detail);
    }

    /** Constructs an Exception with a detail message that is only the
     * name of the argument.
     */  
    public InvalidStateException(Nameable obj) {
        super(obj, "Serious internal error!");
    }

    /** Constructs an Exception with a detail message that includes the
     * name of the argument.
     */  
    public InvalidStateException(Nameable obj, String detail) {
        super(obj, detail);
    }

    /** Constructs an Exception with a detail message that consists of
     * only the names of the obj1 and obj2 arguments.
     */  
    public InvalidStateException(Nameable obj1, Nameable obj2)  {
        super(obj1, obj2, "Serious internal error!");
    }

    /** Constructs an Exception with a detail message that includes the
     * names of the obj1 and obj2 arguments.
     */  
    public InvalidStateException(Nameable obj1, Nameable obj2,
            String detail) {
        super(obj1, obj2, detail);
    }
}
