/* Class used to test ComponentPort

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

@ProposedRating Yellow (cxh@eecs.berkeley.edu)
*/

package ptolemy.kernel.test;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;

import java.util.Enumeration;
import java.util.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// TestComponentPort
/**
This class is used to test protected method(s) in ComponentPort

@author Christopher Hylands
@version $Id$
*/
public class TestComponentPort extends ComponentPort {
    /** Construct a TestComponentPort
     *  @param container The container entity.
     *  @param name The name of the port.
     *  @exception IllegalActionException If the port is not of an acceptable
     *   class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   a port already in the container.
     */
    public TestComponentPort(ComponentEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /** @deprecated _deepConnectedPorts is deprecated,
        but we need to test it anyway
    */
    public Enumeration testDeepConnectedPorts(LinkedList path) {
        return _deepConnectedPorts(path);
    }

    /** @deprecated _deepInsidePorts is deprecated,
        but we need to test it anyway
    */
    public Enumeration testDeepInsidePorts(LinkedList path) {
        return _deepInsidePorts(path);
    }
}
