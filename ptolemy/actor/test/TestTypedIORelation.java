/* Class for testing TypedIORelations

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

package ptolemy.actor.test;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;


//////////////////////////////////////////////////////////////////////////
//// TestTypedIORelation
/**
Class for testing TypedIORelation 
@author Christopher Hylands
@version $Id$
*/
public class TestTypedIORelation extends TypedIORelation {

    public TestTypedIORelation() {
        super();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Throw an exception if the specified port cannot be linked to this
     *  relation (is not of class TypedIOPort).
     *  @param port The candidate port to link to.
     *  @exception IllegalActionException If the port is not an
     *   TypedIOPort.
     */
    public void checkPort (Port port) throws IllegalActionException {
        _checkPort(port);
    }
}
