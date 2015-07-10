/* A director for modeling air traffic control systems.
 
 Copyright (c) 2015 The Regents of the University of California.
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
package ptolemy.domains.atc.kernel;

import ptolemy.actor.Receiver;
import ptolemy.data.DoubleToken;
import ptolemy.domains.atc.lib.Track;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/** A director for modeling air traffic control systems.
 *  This director provides a receiver that consults the destination actor
 *  to determine whether it can accept an input, and provides mechanisms
 *  for handling rejection of an input.
 *  @author Marjan Sirjani and Edward A. Lee
 */
public class ATCDirector extends DEDirector {

    /** Construct a director in the workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace of this object.
     *  @exception NameDuplicationException If thrown by the super class.
     *  @exception IllegalActionException If thrown by the super class.
     */
    public ATCDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        // TODO Auto-generated constructor stub
    }

    @Override
    public Receiver newReceiver() {
        return new ATCReceiver();
    }
    
    /** Return an additional delay for a track to keep an aircraft in
     *  transit.
     *  @param track The track
     *  @return An additional delay, or -1.0 to indicate that a rerouting is possible.
     *  @throws IllegalActionException If thrown while getting the value of the track.
     */
    public double handleRejectionWithDelay(Track track) throws IllegalActionException {
        // FIXME: what value should be returned here?
        return ((DoubleToken)track.delay.getToken()).doubleValue();
    }
}
