/* Base class for exceptions that report the names of Nameable objects.

 Copyright (c) 1997 The Regents of the University of California.
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

@ProposedRating Yellow (cxh@eecs.berkeley.edu)

*/

package pt.kernel;

//////////////////////////////////////////////////////////////////////////
//// KernelException
/**
Base class for exceptions that report the names of Nameable objects.
This exception should not be thrown directly, since it provides very
little information about the cause of the exception.
Constructors are provided that take 0, 1, or 2 Nameable references
plus an arbitrary String.  The constructors are robust in that null
references are ignored.

@author John S. Davis, II, Christopher Hylands
@version $Id$
*/
public class KernelException extends Exception {

    /** Constructs an Exception with no names or message. */  
    public KernelException() {
        this(null, null, null);
    }

    /** Constructs an Exception with only a detail message. */  
    public KernelException(String detail) {
        this(null, null, detail);
    }

    /** Constructs an Exception with a message that is only the
     * name of the argument.
     */  
    public KernelException(Nameable obj) {
        this(obj, null, null);
    }

    /** Constructs an Exception with a detail message that includes the
     * name of the first argument and the second argument string.
     */  
    public KernelException(Nameable obj, String detail) {
        this(obj, null, detail);
    }

    /** Constructs an Exception with a detail message that consists of
     * only the names of the two arguments.
     */  
    public KernelException(Nameable obj1, Nameable obj2)  {
        this(obj1, obj2, null);
    }

    /** Constructs an Exception with a detail message that includes the
     * names of the first two arguments plus the third argument string.
     */  
    public KernelException(Nameable obj1, Nameable obj2,
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
            if (name.equals("")) {
                name = new String("<Unnamed Object>");
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
