/* Interface for actors that generates discrete events.

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

@ProposedRating Yellow (liuj@eecs.berkeley.edu)
@AcceptedRating Yellow (johnr@eecs.berkeley.edu)
*/

package ptolemy.domains.ct.kernel;

import ptolemy.actor.Actor;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// CTEventGenerator
/**
Interface for CT actors that generates discrete events. These actors
convert continuous signals (waveforms) to discrete events. Typical
implementations of event generator are samplers, event detectors, etc.

@author Jie Liu
@version $Id$
*/
public interface CTEventGenerator extends Actor{

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Emit the discrete event that happens at the current time. If there
     *  is no such events, do nothing.
     */
    public void emitCurrentEvents() throws IllegalActionException;

    /** Return true if there is an event at the current time.
     */
    public boolean hasCurrentEvent();

}
