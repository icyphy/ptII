/* This widget visualizes a named object by displaying its icon.
 *
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
package ptolemy.homer.widgets;

import java.util.List;

import org.netbeans.api.visual.widget.Scene;

import ptolemy.homer.kernel.PositionableElement;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.SingletonAttribute;
import ptolemy.vergil.icon.EditorIcon;
import ptolemy.vergil.icon.XMLIcon;
import diva.canvas.Figure;
import diva.gui.toolbox.FigureIcon;

///////////////////////////////////////////////////////////////////
//// NamedObjectIconWidget

/** This widget visualizes a named object by displaying its icon.
 *  @author Anar Huseynov
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (ahuseyno)
 *  @Pt.AcceptedRating Red (ahuseyno)
 */
public class NamedObjectIconWidget extends ResizableImageWidget implements
        NamedObjectWidgetInterface {

    /** Create new instance of the widget by finding the element's EditorIcon attribute
     *  and rendering it as an image.
     *  @param scene The scene containing the widget.
     *  @param element The element to visualize.
     *  @exception IllegalActionException If there is problem with creating an icon.
     *  @exception NameDuplicationException If there is a problem hiding the icon's name.
     */
    public NamedObjectIconWidget(Scene scene, PositionableElement element)
            throws NameDuplicationException, IllegalActionException {
        super(scene);
        _element = element;

        NamedObj namedObject = element.getElement();
        List<EditorIcon> attributeList = namedObject
                .attributeList(EditorIcon.class);
        EditorIcon icon;

        if (!attributeList.isEmpty()) {
            icon = attributeList.get(0);
        } else {
            icon = XMLIcon.getXMLIcon(namedObject, "_icon");
        }

        new SingletonAttribute(namedObject, "_hideName");

        Figure figure = icon.createFigure();
        double ratio = figure.getBounds().getHeight()
                / figure.getBounds().getWidth();
        FigureIcon figureIcon = new FigureIcon(figure, _ICON_WIDTH,
                (int) (_ICON_HEIGHT * ratio));

        setImage(figureIcon.getImage());
        setCheckClipping(true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /* Get the positionable element.
     * @see ptolemy.homer.widgets.NamedObjectWidgetInterface#getNamedObject()
     */
    @Override
    public PositionableElement getPositionableElement() {
        return _element;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The element that is visualized by the widget.
     */
    private final PositionableElement _element;

    /** Icon window width.
     */
    private static int _ICON_HEIGHT = 200;

    /** Icon window width.
     */
    private static int _ICON_WIDTH = 200;
}
