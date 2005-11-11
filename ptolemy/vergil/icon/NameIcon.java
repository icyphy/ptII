/* An icon that renders the name of the container.

 Copyright (c) 1999-2005 The Regents of the University of California.
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
package ptolemy.vergil.icon;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;

import javax.swing.SwingConstants;

import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ConfigurableAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import diva.canvas.CompositeFigure;
import diva.canvas.Figure;
import diva.canvas.toolbox.BasicRectangle;
import diva.canvas.toolbox.LabelFigure;
import diva.canvas.toolbox.PaintedFigure;
import diva.canvas.toolbox.SVGParser;
import diva.gui.toolbox.FigureIcon;
import diva.util.java2d.PaintedList;
import diva.util.xml.XmlDocument;
import diva.util.xml.XmlElement;
import diva.util.xml.XmlReader;

//////////////////////////////////////////////////////////////////////////
//// NameIcon

/**
 An icon that displays the name of the container in an appropriately
 sized box. Put this into a composite actor or in any actor to
 convert the icon for that actor into a simple box with the name
 of the actor instance. You will probably also want to set the
 actor instance to not display its name above its icon. You can
 do that via the Customize Name dialog (obtained by right clicking
 on the icon) or by creating a parameter named "_hideName" with
 value true.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public class NameIcon extends EditorIcon {

    /** Create a new icon with the given name in the given container.
     *  The container is required to implement Settable, or an exception
     *  will be thrown.
     *  @param container The container for this attribute.
     *  @param name The name of this attribute.
     */
    public NameIcon(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        
        // Create an icon for this attribute.
        // This has the side effect of making it visible
        // in Vergil, and giving a reasonable rendition.
        TextIcon icon = new TextIcon(this, "_icon");
        icon.setIconText("-N-");
        icon.setText("NameIcon attribute: This sets the icon to be a box with the name.");
        icon.setPersistent(false);
        
        // Hide the name.
        SingletonParameter hide = new SingletonParameter(this, "_hideName");
        hide.setToken(BooleanToken.TRUE);
        hide.setVisibility(Settable.EXPERT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a new background figure.  This overrides the base class
     *  to draw a box around the value display, where the width of the
     *  box depends on the value.
     *  @return A new figure.
     */
    public Figure createBackgroundFigure() {
        String name = "No Name";
        NamedObj container = (NamedObj) getContainer();
        if (container != null) {
            name = container.getName();
        }

        double width = 60;
        double height = 30;

        // Measure width of the text.  Unfortunately, this
        // requires generating a label figure that we will not use.
        LabelFigure label = new LabelFigure(name, _labelFont, 1.0,
                SwingConstants.CENTER);
        Rectangle2D stringBounds = label.getBounds();
        
        // NOTE: Padding of 20. Quantize the height so that
        // snap to grid still works.
        width = Math.floor(stringBounds.getWidth()) + 20;
        height = Math.floor(stringBounds.getHeight()) + 10;

        return new BasicRectangle(0, 0, width, height, Color.white, 1);
    }

    /** Create a new Diva figure that visually represents this icon.
     *  The figure will be an instance of LabelFigure that renders the
     *  name of the container.
     *  @return A new CompositeFigure consisting of the label.
     */
    public Figure createFigure() {
        CompositeFigure result = (CompositeFigure) super.createFigure();

        String name = "No Name";
        NamedObj container = (NamedObj) getContainer();
        if (container != null) {
            name = container.getName();
        }
        LabelFigure label = new LabelFigure(name, _labelFont, 1.0,
                SwingConstants.CENTER);
        Rectangle2D backBounds = result.getBackgroundFigure().getBounds();
        label.translateTo(backBounds.getCenterX(), backBounds.getCenterY());
        result.add(label);
        return result;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         protected members                 ////

    /** The font used. */
    protected static Font _labelFont = new Font("SansSerif", Font.PLAIN, 12);
}
