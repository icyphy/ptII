/* MEMSGlob

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


import ptolemy.domains.de.demo.mems.gui.*;
import ptolemy.actor.*;
import ptolemy.domains.de.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// MEMSGlob
/**
@author Allen Miu
@version $Id$
*/

public class MEMSGlob extends DEActor {

    public MEMSGlob(TypedCompositeActor container, String name, MEMSPlot plot)
            throws IllegalActionException, NameDuplicationException  {
        super(container, name);
	this.plot = plot;
	Debug.log(0, "MEMSGlob instance created");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Schedules the next sampling event for all sensors and 
     *  processes pending tokens at the msgIO and sysIO ports
     *
     *  @exception CloneNotSupportedException If there is more than one
     *   destination and the output token cannot be cloned.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
      Debug.log(0, "MEMSGlob flushes");
      plot.flush();

      synchronized (this) {
	try {
	  wait(100);
	} catch (InterruptedException e) {}
      }

      fireAfterDelay(1.0);
    }

    /** Produce the initializer event that will cause the generation of
     *  the first output at time zero.
     *
     *  FIXME: What to do if the initial current event is less than zero ?
     *  @exception CloneNotSupportedException If the base class throws it.
     *  @exception IllegalActionException If there is no director.
     */
    public void initialize() throws IllegalActionException {
        // FIXME: This should be just DEDirector
        // FIXME: This class should be derived from DEActor, which should
        // ensure that this cast is valid.
        super.initialize();
        double curTime = getCurrentTime();
        // The delay parameter maybe negative, but it's permissible in the
        // director because the start time is not initialized yet.
        fireAfterDelay(0.0-curTime);
    }

    private MEMSPlot plot;
}
