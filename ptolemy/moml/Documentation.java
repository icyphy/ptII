/* An attribute that contains documentation for the container.

 Copyright (c) 1998 The Regents of the University of California.
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
@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)

*/

package ptolemy.moml;

import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// Documentation
/** 
An attribute that contains documentation for the container.

@author  Edward A. Lee
@version $Id$
*/
public class Documentation extends Attribute {

    /** Construct an attribute with the specified container and name.
     *  The documentation contained by the attribute is initially empty,
     *  but can be set using the setValue() method.
     *  @param container The container.
     *  @param name The name of the attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */	
    public Documentation(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the documentation as a string.
     *  @return The documentation.
     */	
    public String getValue() {
        return _value;
    }

    /** Set the documentation string.
     *  @param value The documentation.
     */	
    public void setValue(String value) {
        _value = value;
    }

    /** Get the documentation as a string, with the class name prepended.
     *  @return A string describing the object.
     */	
    public String toString() {
        return "(" + getClass().getName() + ", " + _value + ")";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The string value of the documentation.
    private String _value;
}
