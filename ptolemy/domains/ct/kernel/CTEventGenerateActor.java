/* Interface for event detectors in the CT domain.

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
*/

package ptolemy.domains.ct.kernel;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import java.util.*;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// CTEventGenerateActor
/**
Interface for event generator in the CT domain. All actors that may 
generate unexpected breakpoint should implement this interface.
An unexpected breakpoint is a breakpoint that can not be known beforehand.
In particular, all event generators, including zero-crossing event detector, 
event triggered sampler, and sample-point event generator
should also implement this interface. An event generating actor can be
asked if there is an event happened at the current time. If no, then it
can be further asked if there is a event "missed" during the last 
integration step. If there is a missed event, the event generator should
suggest a new step size to further locating the event time point.

@author Jie Liu
@version  $Id$
@see classname
@see full-classname
*/
public interface CTEventGenerateActor {

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Return true if there is an event missed in the
     *  last step.
     *  @return True if there is an event missed in the
     *  last step.
     */
    public boolean hasMissedEvent();

    /** Suggest a new refined step size if  there is an event missed in the
     *  last step. If no event is missed in the last step, returns
     *  the currentStepSize.
     *  @return The refined step size for locating the missed event.
     */
    public double refineStepSize();

}
