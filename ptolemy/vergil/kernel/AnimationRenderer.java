/* Animation renderer.

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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil.kernel;

import diva.canvas.Figure;
import diva.canvas.FigureContainer;
import diva.canvas.FigureDecorator;
import diva.canvas.interactor.SelectionRenderer;
import diva.canvas.toolbox.BasicHighlighter;

import java.awt.Color;
import java.util.Hashtable;

import javax.swing.SwingUtilities;

//////////////////////////////////////////////////////////////////////////
//// AnimationRenderer

/**
An implementation of a selection renderer that is used for animation.
It highlights specified figures by wrapping them in an instance of
a FigureDecorator. The figure decorator is obtained by
cloning a prototype decorator, accessible through the
get/setFigureDecorator() methods. The default prototype
is an instance of BasicHighlighter that highlights in red.
<p>
This class is fashioned after BasicSelectionRenderer, but differs
in that it ensures that selection and deselection occurs in the event
thread. Also, it highlights objects in red rather than yellow.

@author Edward A. Lee
@version $Id$
@since Ptolemy II 2.0
*/
public class AnimationRenderer implements SelectionRenderer {

    /** Create a new selection renderer with the default prototype
     *  decorator.
     */
    public AnimationRenderer () {
        this(Color.red);
    }
    
    /** Create a new selection renderer with the default prototype
     *  decorator using the specified color.
     *  @param color The color for the highlight.
     */
    public AnimationRenderer (Color color) {
        _prototypeDecorator = new BasicHighlighter(color, 4.0f);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a new renderer with the given prototype decorator.
     *  @param decorator The prototype decorator.
     */
    public AnimationRenderer (FigureDecorator decorator) {
        _prototypeDecorator = decorator;
    }

    /** Get the prototype decorator.
     *  @return The prototype decorator.
     */
    public FigureDecorator getDecorator () {
        return _prototypeDecorator;
    }

    /** Test whether the given figure is currently rendered highlighted.
     *  @param figure The figure that may be highlighted.
     *  @return True if the figure is highlighted.
     */
    public synchronized boolean isRenderedSelected (Figure figure) {
        return _decorators.containsKey(figure);
    }

    /** Set the rendering of the figure to not be highlighted. The figure
     *  has the decorator unwrapped off it and is inserted back into
     *  its parent figure container, if there is one. If the figure is
     *  not rendered highlighted, do nothing.
     *  The rendering is performed later in the event thread,
     *  and the calling thread is suspended by calling Thread.yield(),
     *  to give the event thread a chance to catch up.
     *  @param figure The figure to deselect.
     */
    public void renderDeselected (final Figure figure) {
        Runnable doUndecorate = new Runnable() {
                public void run() {
                    synchronized(AnimationRenderer.this) {
                        if ( !_decorators.containsKey(figure)) {
                            return;
                        }
                        // Rather than just get the parent of the figure, we must
                        // get the decorator out of the hashtable, since other
                        // wrappers may have been inserted between the figure and
                        // its decorator
                        FigureDecorator d = (FigureDecorator)_decorators.get(figure);
                        if (d.getParent() != null) {
                            figure.repaint();
                            ((FigureContainer) d.getParent()).undecorate(d);
                        }
                        _decorators.remove(figure);
                    }
                }
            };
        SwingUtilities.invokeLater(doUndecorate);
        Thread.yield();
    }

    /** Set the rendering of the figure as highlighted. If the figure is
     *  already rendered highlighted, just repaint. Otherwise create a new
     *  BasicHighlighter, and wrap the figure in the decorator,
     *  inserting the decorator into the figure's parent.
     *  The rendering is performed later in the event thread,
     *  and the calling thread is suspended by calling Thread.yield(),
     *  to give the event thread a chance to catch up.
     *  @param figure The figure to highlight.
     */
    public void renderSelected (final Figure figure) {
        Runnable doDecorate = new Runnable() {
                public void run() {
                    synchronized(AnimationRenderer.this) {
                        if (_decorators.containsKey(figure)) {
                            ((Figure)_decorators.get(figure)).repaint();
                        } else {
                            FigureContainer parent
                                = (FigureContainer) figure.getParent();
                            if (parent != null) {
                                FigureDecorator d
                                    = _prototypeDecorator.newInstance(figure);
                                parent.decorate(figure,d);
                                _decorators.put(figure,d);
                            }
                        }
                    }
                }
            };
        SwingUtilities.invokeLater(doDecorate);
        Thread.yield();
    }

    /** Set the prototype decorator.
     *  @param decorator The prototype decorator.
     */
    public void setDecorator (FigureDecorator decorator) {
        _prototypeDecorator = decorator;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The prototype decorator
     */
    protected FigureDecorator _prototypeDecorator;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Mapping from figures to decorators
     */
    private Hashtable _decorators = new Hashtable();

}
