/* DDEPut is a test class used to test token production.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Red (davisj@eecs.berkeley.edu)

*/

package ptolemy.domains.dde.kernel.test;

import ptolemy.domains.dde.kernel.*;
import ptolemy.actor.*;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;


//////////////////////////////////////////////////////////////////////////
//// DDEPut
/**
DDEPut is a test class used to test token production. It has a single
typed, input multiport. Use this class to test DDEReceiver and DDEThread.


@author John S. Davis II
@version $Id$

*/

public class DDEPut extends TypedAtomicActor {

    /**
     */
    public DDEPut(TypedCompositeActor cont, String name)
            throws IllegalActionException, NameDuplicationException {
        super(cont, name);

        outputPort = new TypedIOPort(this, "output", false, true);
        outputPort.setMultiport(true);
        outputPort.setTypeEquals(BaseType.GENERAL);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    public TypedIOPort outputPort;
}
