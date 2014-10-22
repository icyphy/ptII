/*
 This class visualizes a named object using an image.

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

import java.net.URL;

import javax.swing.ImageIcon;

import org.netbeans.api.visual.widget.Scene;

import ptolemy.homer.kernel.PositionableElement;

///////////////////////////////////////////////////////////////////
//// NamedObjectImageWidget

/**
 * This widget visualizes a named object using an image.
 * @author Anar Huseynov
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (ahuseyno)
 * @Pt.AcceptedRating Red (ahuseyno)
 */
public class NamedObjectImageWidget extends ResizableImageWidget implements
        NamedObjectWidgetInterface {
    /**
     * Create a new instance of the widget.
     * @param scene The scene containing the widget.
     * @param element The element to visualize.
     * @param imageURL The url of the image.
     */
    public NamedObjectImageWidget(Scene scene, PositionableElement element,
            URL imageURL) {
        super(scene);
        _element = element;
        ImageIcon imageIcon = new ImageIcon(imageURL);
        setImage(imageIcon.getImage());
        setCheckClipping(true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Return positionable element that the instance is visualizing.
     * @return the positionable element.
     */
    @Override
    public PositionableElement getPositionableElement() {
        return _element;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    /**
     * The element visualized by the widget.
     */
    private final PositionableElement _element;
}
