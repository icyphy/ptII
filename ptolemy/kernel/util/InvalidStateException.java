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
forms of KernelException, but is implemented as a RuntimeException
so that it does not have to be declared.

@author Edward A. Lee
@version $Id$
*/
public class InvalidStateException extends RuntimeException {

    // NOTE: This class has much duplicated code with KernelException,
    // but because it needs to be a RuntimeException, there seemed to
    // be no way to avoid this.  Should there be an interface defined
    // for the commonality?

    /** Constructs an Exception with no names or message. */  
    public InvalidStateException() {
        this(null, null, "Serious internal error!");
    }

    /** Constructs an Exception with only a detail message. */  
    public InvalidStateException(String detail) {
        this(null, null, detail);
    }

    /** Constructs an Exception with a message that is only the
     * name of the argument.
     */  
    public InvalidStateException(Nameable obj) {
        this(obj, null, "Serious internal error!");
    }

    /** Constructs an Exception with a detail message that includes the
     * name of the first argument and the second argument string.
     */  
    public InvalidStateException(Nameable obj, String detail) {
        this(obj, null, detail);
    }

    /** Constructs an Exception with a detail message that consists of
     * only the names of the two arguments.
     */  
    public InvalidStateException(Nameable obj1, Nameable obj2)  {
        this(obj1, obj2, "Serious internal error!");
    }

    /** Constructs an Exception with a detail message that includes the
     * names of the first two arguments plus the third argument string.
     */  
    public InvalidStateException(Nameable obj1, Nameable obj2,
            String detail) {
        String obj1string = getFullName(obj1);
        String obj2string = getFullName(obj2);
        String prefix;
        if (obj1string != "") {
            if (obj2string != "") {
                prefix = new String(obj1string + " and " + obj2string);
            } else {
                prefix = obj1string;
            }
        } else {
            prefix = obj2string;
        }
        setMessage(prefix);
        if (detail != null) {
            if (!detail.equals("")) {
                if (!prefix.equals("")) {
                    setMessage(new String(prefix + ": " + detail));
                } else {
                    setMessage(detail);
                }
            }
        }
    }
    
    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Get a localized version of the detail message.  Note that
     *  in this implementation, we merely return the same result as
     *  getMessage().
     */  
    public String getLocalizedMessage() {
        return getMessage();
    }

    /** Get the detail message. */
    public String getMessage() {
        return _message;
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         protected methods                        ////

    /** Get the name of a Nameable object.
     *  If the argument is a null reference, return an empty string.
     */
    protected String getName(Nameable obj) {
        String name;
        if (obj == null) {
            return "";
        } else {
            name = obj.getName();
            if (name.equals("")) {
                name = new String("<Unnamed Object>");
            }
        }
        return name;
    }

    /** Get the name of a Nameable object.  This method attempts to use
     *  getFullName(), if it is defined, and resorts to getName() if it is
     *  not.  If the argument is a null reference, return an empty string.
     */
    protected String getFullName(Nameable obj) {
        String name;
        if (obj == null) {
            return "";
        } else {
            try {
                name = obj.getFullName();
            } catch (InvalidStateException ex) {
                name = obj.getName();
            }
        }
        return name;
    }

    /** Sets the error message to the specified string.
     */  
    protected void setMessage(String msg) {
        _message = msg;
    }
    
    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    // The detail message.
    private String _message ;
}
