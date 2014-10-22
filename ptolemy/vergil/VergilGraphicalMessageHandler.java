/* Singleton class for displaying exceptions, errors, warnings, and messages that
includes a button to open the actor that caused the problem and that zooms in to the actor.

 Copyright (c) 2012-2014 The Regents of the University of California.
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
package ptolemy.vergil;

import ptolemy.actor.gui.ActorGraphicalMessageHandler;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.basic.BasicGraphFrame;

///////////////////////////////////////////////////////////////////
//// VergilGraphicalMessageHandler

/**
 A message handler that optionally includes a button that opens the model
 that contains the actor that caused the exception and zooms into the actor.

 @author  Christopher Brooks
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class VergilGraphicalMessageHandler extends ActorGraphicalMessageHandler {

    /** Open the level of hierarchy of the model that contains the
     *  Nameable referred to by the KernelException or KernelRuntimeException and
     *  possibly zoom in.
     *  @param throwable The throwable that may be a KernelException
     *  or KernelRuntimeException.
     */
    @Override
    protected void _showNameable(Throwable throwable) {
        Nameable nameable1 = _getNameable(throwable);
        if (nameable1 != null) {
            BasicGraphFrame.openComposite(null, (NamedObj) nameable1);
            return;
        }

        message("Internal Error: The throwable \"" + throwable
                + "\" is not a KernelException or KernelRuntimeException?");
    }
}
