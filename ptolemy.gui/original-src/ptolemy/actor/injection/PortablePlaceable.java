/*
 Copyright (c) 2011-2014 The Regents of the University of California.
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

package ptolemy.actor.injection;

import ptolemy.actor.gui.Placeable;

///////////////////////////////////////////////////////////////////
//// PortablePlaceable
/**
 * This interface is analogous to the {@link Placeable} interface.  However, this
 * interface is platform independent, and it's expected that implementers of the
 * interface are also platform independent.  By platform independent, we mean there is
 * no dependendency between java.awt or java.swing packages that are specific to Java SE
 * version of the Java and not available on Android, for example.
 *
 * <p>Interface for objects that have UI components that can be placed in containers.
 * These objects can be fairly tricky to write because of the fact that they
 * might be placed in a control panel, or be expected to create their own
 * container.  Moreover, from one run to the next, this situation might change.
 * That is, it might create a container on one run, but on the next run, place
 * the display in specified container (like a control panel).
 * Objects that implement this interface should pass the following tests:</p>
 * <ol>
 * <li>Run the model from the toolbar. The object creates a container.</li>
 * <li>Close the container during the run. The run continues without the container.</li>
 * <li>Move and resize the container during the run.</li>
 * <li>Save the model and close it. Then open and re-run.
 * Placement and size is preserved.</li>
 * <li>Re-run the model from the toolbar. Move and resize is preserved.</li>
 * <li>Run the model from the View:Run menu. If a frame is visible, it first
 * gets closed.</li>
 * <li>Close the run control panel and run from the toolbar. A frame is opened,
 * using the last size and placement.
 * <li>Delete the actor. Frame should close, or display in the control
 * panel should disappear.</li>
 * </ol>
 *
 * @author Edward A. Lee Contributor: Anar Huseynov
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (ahuseyno)
 * @Pt.AcceptedRating Red (ahuseyno)
 * @see Placeable
 */
public interface PortablePlaceable {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Place the object that implements this interface into the specified container.
     *  Obviously, this method needs to be called before the object
     *  is actually placed in a container.  Otherwise, the object will be
     *  expected to create its own frame into which to place itself.
     *  For actors, this method should be called before initialize().
     *  @param container The container in which to place the object, or
     *   null to specify that there is no current container.
     */
    public void place(PortableContainer container);
}
