/* Class to test protected methods of InvalidStateException

 Copyright (c) 1999-2000 The Regents of the University of California.
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

@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.kernel.util.test;
import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// TestInvalidStateException
/**
Test class used to test protected method of InvalidStateException

@author Christopher Hylands
@version $Id$
*/
public class TestInvalidStateException extends InvalidStateException {

    /** Constructs an Exception with a detail message that includes the
     *  name of the first argument and the second argument string.
     *  @param obj The object.
     *  @param detail The message.
     */
    public TestInvalidStateException(Nameable obj, String detail) {
        super(obj, null, detail);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the name of a Nameable object.
     *  If the argument is a null reference, return an empty string.
     *  @param obj An object with a name.
     *  @return The name of the argument.
     */
    public String getName(Nameable obj) {
        return _getName(obj);
    }
}
