/* An Entity is an aggregation of ports.

 Copyright (c) 1997-2000 The Regents of the University of California.
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

@ProposedRating Red (lmuliadi@eecs.berkeley.edu)
*/

package ptolemy.domains.de.demo.mems.lib;

import ptolemy.plot.*;
import ptolemy.actor.*;
import ptolemy.domains.de.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// MEMSActor
/**

@author Allen Miu, Lukito Muliadi
@version $Id$
*/

abstract class MEMSActor extends DEActor {
  
    public MEMSActor(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException  {
        super(container, name);
    }

    /** Produce the initializer event that will cause the generation of
     *  the first output at time zero.
     *
     *  @exception CloneNotSupportedException If the base class throws it.
     *  @exception IllegalActionException If there is no director.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        double curTime = getCurrentTime();
        // The delay parameter maybe negative, but it's permissible in the
        // director because the start time is not initialized yet.
        fireAfterDelay(0.0-curTime);
    }
  

    /** Returns the debug message header for this class 
     */
    public String getDebugHeader() {
        return _debugHeader;
    }
    public int getID() {
        return _myID;
    }

    abstract protected void fireDueEvents() throws IllegalActionException;
    protected String _debugHeader;
    protected int _myID;
    protected double prevFireTime = -1.0;
}
