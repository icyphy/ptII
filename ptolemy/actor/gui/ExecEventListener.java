/* An ExecEventListener is a listener for ExecEvents created by actors.

Copyright (c) 1997-1999 The Regents of the University of California.
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
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

//////////////////////////////////////////////////////////////////////////
//// ExecEventListener
/**
An ExecEventListener is a listener for ExecEvents created by actors.
In general, an object that implements this interface will probably be
a front end such as a execution visualization tool for the Ptolemy
II system, or an object that is communicating with a graphical user
interface. The events are issued only when the event actually occurs,
not when it is requested.

@author Mudit Goel, John S. Davis II
@version $Id$
*/

public interface ExecEventListener {

    /** Report that an actor has changed its state. The notion of
     *  'state' is arbitrarily defined by the author of the actor.
     *  The ExecEvent will contain a reference to the actor that
     *  generated the event.
     *  @param event An ExecEvent containing a reference to the
     *   actor that generated the ExecEvent.
     */
    public void stateChanged(ExecEvent event);

}


