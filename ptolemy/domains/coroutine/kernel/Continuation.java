/*
Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 1995-2014 The Regents of the University of California.
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

Ptolemy II includes the work of others, to see those copyrights, follow
the copyright link on the splash page or see copyright.htm.
 */

package ptolemy.domains.coroutine.kernel;

import java.util.List;

import ptolemy.kernel.util.IllegalActionException;

/**
 * Continuation class.
 *
 * @author shaver
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public interface Continuation {

    public ControlExitToken controlEnter(ControlEntryToken entry)
            throws IllegalActionException;

    public ControlExitToken controlInit() throws IllegalActionException;

    public ControlExitToken controlResume() throws IllegalActionException;

    public ControlExitToken controlEnterAt(
            ControlEntryToken.EntryLocation location)
                    throws IllegalActionException;

    /** Returns the set of exposed Entry Locations.
     *  Some internal entry locations may exist that are not exposed
     *  to the interface.
     */
    public List<ControlEntryToken.EntryLocation> entryLocations();

    /** Returns the set of exposed Exit Locations.
     *  Some internal exit locations may exist that are not exposed
     *  to the interface.
     *
     */
    public List<ControlExitToken.ExitLocation> exitLocations();

}
