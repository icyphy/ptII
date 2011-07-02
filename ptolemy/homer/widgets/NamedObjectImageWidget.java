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

import java.awt.Image;
import java.net.URL;

import javax.swing.ImageIcon;

import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.general.IconNodeWidget;
import org.openide.util.ImageUtilities;

import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// NamedObjectImageWidget

/**
* TODO
* @author Anar Huseynov
* @version $Id$ 
* @since Ptolemy II 8.1
* @Pt.ProposedRating Red (ahuseyno)
* @Pt.AcceptedRating Red (ahuseyno)
*/
public class NamedObjectImageWidget extends IconNodeWidget implements
        NamedObjectWidgetInterface {

    /**
     * TODO
     * @param scene
     * @param namedObject
     * @param imageURL
     */
    public NamedObjectImageWidget(Scene scene, NamedObj namedObject,
            URL imageURL) {
        super(scene);
        _namedObject = namedObject;
        Image image = ImageUtilities.icon2Image(new ImageIcon(imageURL));
        setImage(image);
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
}
