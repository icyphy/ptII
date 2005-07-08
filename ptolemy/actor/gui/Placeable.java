/* Interface for objects that have UI components that can be placed in panels.

 Copyright (c) 1997-2005 The Regents of the University of California.
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
package ptolemy.actor.gui;

import java.awt.Container;

//////////////////////////////////////////////////////////////////////////
//// Placeable

/**
 Interface for objects that have UI components that can be placed in containers.
 These objects can be fairly tricky to write because of the fact that they
 might be placed in a control panel, or be expected to create their own
 frame.  Moreover, from one run to the next, this situation might change.
 That is, it might create a frame on one run, but on the next run, place
 the display in specified frame (like a control panel).
 Objects that implement this interface should pass the following tests:
 <ol>
 <li>Run the model from the toolbar. The object creates a frame.</li>
 <li>Close the frame during the run. The run continues without the display.</li>
 <li>Move and resize the frame during the run.</li>
 <li>Save the model and close it. Then open and re-run.
 Placement and size is preserved.</li>
 <li>Re-run the model from the toolbar. Move and resize is preserved.</li>
 <li>Run the model from the View:Run menu. If a frame is visible, it first
 gets closed.</li>
 <li>Close the run control panel and run from the toolbar. A frame is opened,
 using the last size and placement.
 <li>Delete the actor. Frame should close, or display in the control
 panel should disappear.</li>
 </ol>

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.3
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Yellow (cxh)
 */
public interface Placeable {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Specify the container into which this object should be placed.
     *  Obviously, this method needs to be called before the object
     *  is actually placed in a container.  Otherwise, the object will be
     *  expected to create its own frame into which to place itself.
     *  For actors, this method should be called before initialize().
     *  @param container The container in which to place the object, or
     *   null to specify that there is no current container.
     */
    public void place(Container container);
}
