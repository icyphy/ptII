/* An icon that displays specified text.

 Copyright (c) 1999-2003 The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil.icon;

import java.awt.Color;
import java.awt.Font;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import ptolemy.gui.Top;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import diva.canvas.Figure;
import diva.canvas.toolbox.LabelFigure;
import diva.gui.toolbox.FigureIcon;

//////////////////////////////////////////////////////////////////////////
//// DynamicEditorIcon
/**
An icon that provides for the possibility that figures may be
dynamically updated.  While some icons are generally 'static' and it
is simplest to regenerate new figures when their attributes change,
other icons are more 'dynamic'.  For instance, some icons might
display live video or a live plot.  This icon provides some basic
infrastructure for dealing with such cases.

The main difficulty is that, by design, an icon cannot keep track of
the figures it creates, otherwise there is a possibility for memory
leaks or inconsistencies in the case of multiple views.  This class
solves the problem by using a WeakReferences to keep track of
generated figures.  These references do not prevent objects from being
garbage collected.  This class also provides a 'safe' iterator that
can be used to traverse the weak references without the normal
associated nastiness.

@author Steve Neuendorffer
@version $Id$
@since Ptolemy II 2.0
*/
public class DynamicEditorIcon extends EditorIcon {

    /** Construct an icon in the specified workspace and name.
     *  This constructor is typically used in conjuction with
     *  setContainerToBe() and createFigure() to create an icon
     *  and generate a figure without having to have write access
     *  to the workspace.
     *  If the workspace argument is null, then use the default workspace.
     *  The object is added to the directory of the workspace.
     *  @see #setContainerToBe(NamedObj)
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the attribute.
     *  @exception IllegalActionException If the specified name contains
     *   a period.
     */
    public DynamicEditorIcon(Workspace workspace, String name)
            throws IllegalActionException {
        super(workspace, name);
    }

    /** Create a new icon with the given name in the given container.
     *  @param container The container.
     *  @param name The name of the attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public DynamicEditorIcon(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is an object with no container.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException Not thrown in this base class
     *  @return The new Attribute.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        DynamicEditorIcon newObject = 
            (DynamicEditorIcon)super.clone(workspace);
        newObject._figures = new LinkedList();
        return newObject;
    }

    /** Add the figure to the list of figures managed by this icon.
     *  A WeakReference object will be created that points to the figure
     *  which will not prevent it from being garbage collected.
     *  This method should be called in the createBackgroundFigure method
     *  with the figure that will be returned.
     *  @param figure A newly manufactured figure.
     */
    protected void _addLiveFigure(Figure newFigure) {
        _figures.add(new WeakReference(newFigure));
        
        _trimLiveFigures();
    }
        
    /** Return a regular iterator over the figures created by this
     *  icon which have not been garbage collected.  The objects
     *  returned by the iterator are Figures, not WeakReferences.
     *  Furthermore, the objects returned by this iterator are
     *  guaranteed to not be null.
     */
    protected Iterator _liveFigureIterator() {
        final Iterator iterator = _figures.listIterator();
        return new Iterator() {
                public boolean hasNext() {
                    // Pull the next valid element out of the list of
                    // WeakReferences.
                    while(_object == null && iterator.hasNext()) {
                        _object = ((WeakReference)iterator.next()).get();
                        if(_object == null) {
                            iterator.remove();
                        }
                    }
                    return _object != null;
                }
                public Object next() throws NoSuchElementException {
                    // Just to make sure that someone wasn't stupid
                    // and didn't call hasNext();
                    while(_object == null && iterator.hasNext()) {
                        _object = ((WeakReference)iterator.next()).get();
                        if(_object == null) {
                            iterator.remove();
                        }                    
                    } 
                    if(_object == null) {
                        throw new NoSuchElementException(
                                "The iterator is empty.");
                    } else {
                        Object object = _object;
                        _object = null;
                        return object;
                    }
                }
                public void remove() throws UnsupportedOperationException {
                    throw new UnsupportedOperationException(
                            "The remove() operation is unsupported.");
                }
                private Object _object;
            };
    }
    
    /** Trim the list of figures to discard entries that are no longer
     *  live.
     */
    protected void _trimLiveFigures() {
        ListIterator figures = _figures.listIterator();
        while (figures.hasNext()) {
            Object figure = ((WeakReference)figures.next()).get();
            if (figure == null) {
                // The figure has been garbage collected, so we
                // remove it from the list.
                figures.remove();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
        
    // A list of weak references to figures that this has created.
    private List _figures = new LinkedList();
}
