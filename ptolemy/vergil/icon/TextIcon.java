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
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

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
//// TextIcon
/**
An icon that displays specified text.

@author Edward A. Lee
@version $Id$
@since Ptolemy II 2.0
*/
public class TextIcon extends EditorIcon {

    /** Create a new icon with the given name in the given container.
     *  @param container The container.
     *  @param name The name of the attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public TextIcon(NamedObj container, String name)
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
        TextIcon newObject = (TextIcon)super.clone(workspace);
        newObject._figures = new LinkedList();
        return newObject;
    }

    /** Create a new default background figure, which is the text set
     *  by setText, if it has been called, or default text if not.
     *  This must be called in the Swing thread, or a concurrent
     *  modification exception could occur.
     *  @return A figure representing the specified shape.
     */
    public Figure createBackgroundFigure() {
        // NOTE: This gets called every time that the graph gets
        // repainted, which seems excessive to me.  This will happen
        // every time there is a modification to the model that is
        // carried out by a MoMLChangeRequest.
        
        // The Diva graph package implements a model-view-controller
        // architecture, which implies that this needs to return a new
        // figure each time it is called.  The reason is that the figure
        // may go into a different view, and transformations may be applied
        // to that figure in that view.  However, this class needs to be
        // able to update that figure when setShape() is called.  Hence,
        // this class keeps a list of all the figures it has created.
        // The references to these figures, however, have to be weak
        // references, so that this class does not interfere with garbage
        // collection of the figure when the view is destroyed.
        LabelFigure newFigure;
        if (_text != null) {
            newFigure = new LabelFigure(_text, _font);
        } else {
            newFigure = new LabelFigure(_DEFAULT_TEXT, _font);
        }
        // By default, the origin should be the upper left.
        newFigure.setAnchor(SwingConstants.NORTH_WEST);
        newFigure.setFillPaint(_textColor);
        _figures.add(new WeakReference(newFigure));
        
        // Trim the list of figures...
        ListIterator figures = _figures.listIterator();
        while (figures.hasNext()) {
            Object figure = ((WeakReference)figures.next()).get();
            if (figure == null) {
                // The figure has been garbage collected, so we
                // remove it from the list.
                figures.remove();
            }
        }
        return newFigure;
    }

    /** Create a new Swing icon.  This returns an icon with the text
     *  "-A-".
     *  @return A new Swing Icon.
     */
    public javax.swing.Icon createIcon() {
        // In this class, we cache the rendered icon, since creating icons from
        // figures is expensive.
        if (_iconCache != null) {
            return _iconCache;
        }
        // No cached object, so rerender the icon.
        LabelFigure figure = new LabelFigure(_ICON_TEXT, _font);
        figure.setFillPaint(_textColor);
        _iconCache = new FigureIcon(figure, 20, 15);
        return _iconCache;
    }

    /** Specify the text color to use.  This is deferred and executed
     *  in the Swing thread.
     *  @param textColor The fill color to use.
     */
    public void setTextColor(Color textColor) {
        _textColor = textColor;
        
        // Update the shapes of all the figures that this icon has
        // created (which may be in multiple views). This has to be
        // done in the Swing thread.  Assuming that createBackgroundFigure()
        // is also called in the Swing thread, there is no possibility of
        // conflict here where that method is trying to add to the _figures
        // list while this method is traversing it.
        Runnable doSet = new Runnable() {
            public void run() {
                ListIterator figures = _figures.listIterator();
                while (figures.hasNext()) {
                    Object figure = ((WeakReference)figures.next()).get();
                    if (figure == null) {
                        // The figure has been garbage collected, so we
                        // remove it from the list.
                        figures.remove();
                    } else {
                        ((LabelFigure)figure).setFillPaint(_textColor);
                    }
                }
            }
        };
        SwingUtilities.invokeLater(doSet);
    }
    
    /** Specify the font to use.  This is deferred and executed
     *  in the Swing thread.
     *  @param font The font to use.
     */
    public void setFont(Font font) {
        _font = font;
        
        // Update the shapes of all the figures that this icon has
        // created (which may be in multiple views). This has to be
        // done in the Swing thread.  Assuming that createBackgroundFigure()
        // is also called in the Swing thread, there is no possibility of
        // conflict here where that method is trying to add to the _figures
        // list while this method is traversing it.
        Runnable doSet = new Runnable() {
            public void run() {
                ListIterator figures = _figures.listIterator();
                while (figures.hasNext()) {
                    Object figure = ((WeakReference)figures.next()).get();
                    if (figure == null) {
                        // The figure has been garbage collected, so we
                        // remove it from the list.
                        figures.remove();
                    } else {
                        ((LabelFigure)figure).setFont(_font);
                    }
                }
            }
        };
        SwingUtilities.invokeLater(doSet);
    }

    /** Specify text to display.  This is deferred and executed
     *  in the Swing thread.
     *  @param text The text to display.
     */
    public void setText(String text) {
        _text = text;

        // Update the shapes of all the figures that this icon has
        // created (which may be in multiple views). This has to be
        // done in the Swing thread.  Assuming that createBackgroundFigure()
        // is also called in the Swing thread, there is no possibility of
        // conflict here where that method is trying to add to the _figures
        // list while this method is traversing it.
        Runnable doSet = new Runnable() {
            public void run() {
                ListIterator figures = _figures.listIterator();
                while (figures.hasNext()) {
                    Object figure = ((WeakReference)figures.next()).get();
                    if (figure == null) {
                        // The figure has been garbage collected, so we
                        // remove it from the list.
                        figures.remove();
                    } else {
                        ((LabelFigure)figure).setString(_text);
                    }
                }
            }
        };
        Top.deferIfNecessary(doSet);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
        
    // Default text.
    private String _DEFAULT_TEXT = "Double click to edit text.";

    // A list of weak references to figures that this has created.
    private List _figures = new LinkedList();
    
    // The font to use.
    private Font _font = new Font("SansSerif", Font.PLAIN, 12);
    
    // Default text.
    private String _ICON_TEXT = "-A-";

    // The specified text color.
    private Color _textColor = Color.blue;
      
    // The text that is rendered.
    private String _text;
}
