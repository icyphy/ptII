/* Language independent code generator for Ptolemy Ports.

 Copyright (c) 2008 The Regents of the University of California.
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

package ptolemy.codegen.kernel.newInterfaces;

import ptolemy.actor.NoRoomException;
import ptolemy.actor.NoTokenException;
import ptolemy.codegen.util.PartialResult;
import ptolemy.kernel.util.IllegalActionException;

/**
 * @author Man-kit Leung
 * @version $Id: PortCodeGenerator.java 52207 2009-01-27 03:43:37Z cxh $
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public interface ReceiverCodeGenerator {

    public PartialResult clear() throws IllegalActionException;
    
    public PartialResult elementList() throws IllegalActionException;

    public PartialResult get() throws NoTokenException;

    public PartialResult getArray(PartialResult numberOfTokens) throws NoTokenException;

    public PartialResult hasRoom();

    public PartialResult hasRoom(PartialResult numberOfTokens);

    public PartialResult hasToken();

    public PartialResult hasToken(PartialResult numberOfTokens);

    public PartialResult isKnown();

    public PartialResult put(PartialResult token) throws NoRoomException, IllegalActionException;

    public PartialResult putArray(PartialResult tokenArray, PartialResult numberOfTokens);

    public PartialResult putArrayToAll(PartialResult tokens, PartialResult numberOfTokens,
            PartialResult receivers);

    public PartialResult putToAll(PartialResult token, PartialResult receivers);

    public PartialResult reset() throws IllegalActionException;

}
