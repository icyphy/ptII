/* Exception thrown on detecting type conflicts.

 Copyright (c) 1997-1999 The Regents of the University of California.
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

@ProposedRating Yellow (yuhong@eecs.berkeley.edu)
@AcceptedRating Yellow (lmuliadi@eecs.berkeley.edu)

*/

package ptolemy.actor;

import ptolemy.kernel.util.*;
import ptolemy.data.type.*;
import collections.LinkedList;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// TypeConflictException
/**
Thrown on detecting type conflicts.
This class contains all the Typeable objects where type conflicts
occurred.

@author Yuhong Xiong
@version $Id$
@see ptolemy.data.type.Typeable
*/
public class TypeConflictException extends KernelException {

    /** Construct an Exception with an Enumeration of Typeables.
     *  The Typeables are the places where type conflicts
     *  occurred.  The detailed message of this Exception will be
     *  the string "Type conflicts occurred on the following Typeables:",
     *  followed by a list of Typeables and their types. The Typeables
     *  are represented by their names if the Typeable object is a
     *  NamedObj, otherwise, they are represented by the string
     *  "Unnamed Typeable".  The type is represented by the corresponding
     *  class name. For example, the type "Int" is represented by
     *  "ptolemy.data.IntToken" in the message.
     *  Each Typeable takes one line, and each line starts
     *  with 2 white spaces to make the message more readable.
     *  @param typeables an Enumeration of Typeables.
     */
    public TypeConflictException(Enumeration typeables) {
	this(typeables, "Type conflicts occurred on the following Typeables:");
    }

    /** Construct an Exception with an Enumeration of Typeables.
     *  The Typeables are the places where type conflicts
     *  occurred.  The detailed message of this Exception will be
     *  the specified message,
     *  followed by a list of Typeables and their types. The Typeables
     *  are represented by their names if the Typeable object is a
     *  NamedObj, otherwise, they are represented by the string
     *  "Unnamed Typeable".  The type is represented by the corresponding
     *  class name. For example, the type "Int" is represented by
     *  "ptolemy.data.IntToken" in the message.
     *  Each Typeable takes one line, and each line starts
     *  with 2 white spaces to make the message more readable.
     *  @param typeables an Enumeration of Typeables.
     *  @param detail a message.
     */
    public TypeConflictException(Enumeration typeables, String detail) {
	_typeables.appendElements(typeables);
	_setMessage(detail + "\n" + _getTypeablesAndTypes());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return an Enumeration of Typeables where type conflicts
     *  occurred. The ports are those specified in the Enumeration
     *  argument of the constructor.
     *  @return An Enumeration.
     */
    public Enumeration getTypeables() {
	return _typeables.elements();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Create a string listing all the Typeables in _typeables and
    // their types. Each Typeable takes one line, and each line starts
    // with 2 white spaces to make the String more readable.
    private String _getTypeablesAndTypes() {
	try {
	    String result = "";
	    Enumeration e = getTypeables();
	    while(e.hasMoreElements()) {
	        Typeable typeable = (Typeable)e.nextElement();
	        if (typeable instanceof NamedObj) {
	            result += "  " + ((NamedObj)typeable).getFullName() + ": ";
	        } else {
		    result += "Unnamed Typeable: ";
	        }
	        Type type = typeable.getType();
	        result += type.toString() + "\n";
	    }

	    return result;
        } catch (IllegalActionException ex) {
	    throw new InternalErrorException("TypeConflictException." +
	        "_getTypeablesAndTypes(): Cannot get type of Typeable. " +
		ex.getMessage());
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private LinkedList _typeables = new LinkedList();
}
