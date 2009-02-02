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

import ptolemy.codegen.util.PartialResult;
import ptolemy.kernel.util.IllegalActionException;

/**
 * @author Man-kit Leung
 * @version $Id: PortCodeGenerator.java 52207 2009-01-27 03:43:37Z cxh $
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public interface IOPortCodeGenerator extends NewComponentCodeGenerator {

    public PartialResult get(PartialResult channel);
    public PartialResult get(PartialResult channel, PartialResult vectorLength);

    public PartialResult send(PartialResult channelIndex, PartialResult token);
    public PartialResult send(PartialResult channelIndex, PartialResult tokenArray, PartialResult vectorLength);
    public PartialResult sendClear(PartialResult channelIndex);
    public PartialResult sendClearInside(PartialResult channelIndex);
    public PartialResult sendInside(PartialResult channelIndex, PartialResult token);
    
    public PartialResult hasRoom(PartialResult channelIndex) throws IllegalActionException;
    public PartialResult hasRoomInside(PartialResult channelIndex) throws IllegalActionException;

    public PartialResult isKnown() throws IllegalActionException;
    public PartialResult isKnown(PartialResult channelIndex) throws IllegalActionException;
    public PartialResult isKnownInside(PartialResult channelIndex);


}
