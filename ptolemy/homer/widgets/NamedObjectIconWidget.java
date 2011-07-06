/* TODO
 Copyright (c) 2011 The Regents of the University of California.
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

/**
* TODO
* @author Anar Huseynov
* @version $Id$ 
* @since Ptolemy II 8.1
* @Pt.ProposedRating Red (ahuseyno)
* @Pt.AcceptedRating Red (ahuseyno)
*/
public class NamedObjectIconWidget extends ResizableImageWidget implements
        NamedObjectWidgetInterface {

    /**
     * TODO
     * @param scene
     * @param namedObject
     * @param imageURL
     * @throws IllegalActionException 
     * @throws NameDuplicationException 
     */
    public NamedObjectIconWidget(Scene scene, NamedObj namedObject)
            throws NameDuplicationException, IllegalActionException {
        super(scene);
        _namedObject = namedObject;
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

    /* TODO
     *  (non-Javadoc)
     * @see ptolemy.homer.widgets.NamedObjectWidgetInterface#getNamedObject()
     */
    public NamedObj getNamedObject() {
        return _namedObject;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    /**
     * TODO
     */
    private final NamedObj _namedObject;

    /** Icon window width. */
    private static int _ICON_HEIGHT = 200;

    /** Icon window width. */
    private static int _ICON_WIDTH = 200;
}
