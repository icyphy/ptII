/* Exception thrown on detecting type conflicts.

 Copyright (c) 1997-2001 The Regents of the University of California.
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

import ptolemy.data.type.Type;
import ptolemy.data.type.Typeable;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NamedObj;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// TypeConflictException
/**
Thrown on detecting type conflicts.
This class contains all the objects where type conflicts occurred.
The objects are usually instances of Typeables or StructuredTypes.

@author Yuhong Xiong
@version $Id$
*/
public class TypeConflictException extends KernelException {

    /** Construct an Exception with a list of objects where type conflicts
     *  occurred.
     *  The detailed message of this Exception will be the string
     *  "Type conflicts occurred at the following places:",
     *  followed by a list of objects and their types. The objects
     *  are represented by their names if it is a NamedObj, otherwise,
     *  they are represented by the result of the toString() method.
     *  Each object takes one line, and each line starts
     *  with 2 white spaces to make the message more readable.
     *  @param objects a list of objects containing types.
     */
    public TypeConflictException(List objects) {
	this(objects, "Type conflicts occurred at the following places:");
    }

    /** Construct an Exception with a list of objects where type conflicts
     *  occurred.
     *  The detailed message of this Exception will be the specified message,
     *  followed by a list of objects and their types. The objects
     *  are represented by their names if it is a NamedObj, otherwise,
     *  they are represented by the result of the toString() method.
     *  Each object takes one line, and each line starts
     *  with 2 white spaces to make the message more readable.
     *  @param objects a list of objects containing types.
     *  @param detail a message.
     */
    public TypeConflictException(List objects, String detail) {
	_objects.addAll(objects);
	_setMessage(detail + "\n" + _getObjectsAndTypes());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a list of objects where type conflicts occurred.
     *  @return A List.
     */
    public List objectList() {
	return _objects;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Create a string listing all the objects in _objects and
    // their types. Each object takes one line, and each line starts
    // with 2 white spaces to make the String more readable.
    private String _getObjectsAndTypes() {
	try {
	    String result = "";
	    Iterator objects = objectList().iterator();
	    while(objects.hasNext()) {
	        Object object = objects.next();
	        if (object instanceof NamedObj) {
	            result += "  " + ((NamedObj)object).getFullName() + ": ";
	        } else {
		    result += "  " + object.toString();
	        }

		if (object instanceof Typeable) {
	            Type type = ((Typeable)object).getType();
	            result += type.toString();
		}
		result += "\n";
	    }

	    return result;
        } catch (IllegalActionException ex) {
	    throw new InternalErrorException("TypeConflictException." +
                    "_getObjectsAndTypes(): Cannot get type of Typeable. " +
                    ex.getMessage());
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private List _objects = new LinkedList();
}
