/* A drop target for the ptolemy editor.

 Copyright (c) 1999-2014 The Regents of the University of California.
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
package ptolemy.vergil.basic;

import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetListener;
import java.util.Vector;

import diva.graph.JGraph;
import ptolemy.kernel.util.InternalErrorException;

///////////////////////////////////////////////////////////////////
//// EditorDropTarget

/**
 This class provides drag-and-drop support. When this drop target
 receives a transferable object containing a ptolemy object, it creates
 a new instance of the object, and adds it to the given graph.
 If the drop location falls on top of an icon associated with an
 instance of NamedObj, then the object may be deposited inside that
 instance (so the instance becomes its container). If the object being
 dropped implements the {@link RelativeLocatable} marker interface,
 then instead of dropping it inside the target object, it is dropped
 into the container of the target object and assigned a location relative
 to the target object. If the drop location is not on top of any object, then
 the object is deposited inside the model associated with the
 target graph. In any case, if the target container implements
 the DropListener interface, then it is informed of the drop by
 calling its dropped() method.
 <p>
 Sometimes, you will want to disable the feature that a drop
 onto a NamedObj results in the dropped object being placed inside
 that NamedObj.  To disable this feature, call setDropIntoEnabled()
 with a false argument.

 @author Steve Neuendorffer and Edward A. Lee, Contributor: Michael Shilman and Sven Koehler
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 */
@SuppressWarnings("serial")
public class EditorDropTarget extends DropTarget {

    /** Construct a new graph target to operate on the given JGraph.
     */
    public EditorDropTarget() {
        // Nullary constructor so that derived classes may extend this
        // class.
    }

    /** Construct a new graph target to operate on the given JGraph.
     *  @param graph The diva graph panel.
     */
    public EditorDropTarget(JGraph graph) {
        setComponent(graph);

        try {
            EditorDropTargetListener listener = new EditorDropTargetListener();
            listener.setDropTarget(this);
            addDropTargetListener(listener);
        } catch (java.util.TooManyListenersException ex) {
            throw new InternalErrorException(ex);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Remove an additional listener.
     *  @param listener The DropTargetListener to be removed.
     */
    public void deRegisterAdditionalListener(DropTargetListener listener) {
        _additionalListeners.remove(listener);
    }

    /** Return true if the feature is enabled that a a drop onto an
     *  instance of NamedObj results in that NamedObj containing the
     *  dropped object. Otherwise, return false.
     *  @return True if drop into is enabled.
     */
    public boolean isDropIntoEnabled() {
        return _dropIntoEnabled;
    }

    /** Register additional DropTargetListeners.
     *  @param listener The DropTargetListener to be added.
     */
    public void registerAdditionalListener(DropTargetListener listener) {
        _additionalListeners.addElement(listener);
    }

    /** If the argument is false, then disable the feature that a
     *  a drop onto an instance of NamedObj results in that NamedObj
     *  containing the dropped object.  If the argument is true, then
     *  reenable the feature.  The feature is enabled by default.
     *  @param enabled False to disable the drop into feature.
     */
    public void setDropIntoEnabled(boolean enabled) {
        _dropIntoEnabled = enabled;
    }

    /** Return the Vector of listeners that have been registered.
     *  @return The listeners that have been registered with
     *  {@link #registerAdditionalListener(DropTargetListener)}.
     */
    public Vector<DropTargetListener> getAdditionalListeners() {
        return _additionalListeners;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Vector to contain additional listeners. */
    private Vector<DropTargetListener> _additionalListeners = new Vector<DropTargetListener>();

    /** Flag indicating whether drop into is enabled. */
    private boolean _dropIntoEnabled = true;
}
