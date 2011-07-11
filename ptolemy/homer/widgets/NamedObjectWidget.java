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

import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// NamedObjectWidget

/**
 * TODO
 * @author Anar Huseynov
 * @version $Id$ 
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (ahuseyno)
 * @Pt.AcceptedRating Red (ahuseyno)
 */
public class NamedObjectWidget extends Widget implements
        NamedObjectWidgetInterface {

    /**
     * TODO
     * @param scene
     * @param namedObject
     */
    public NamedObjectWidget(Scene scene, NamedObj namedObject) {
        super(scene);
        _namedObject = namedObject;
        setCheckClipping(true);

    }

    /* TODO
     * (non-Javadoc)
     * @see ptolemy.homer.widgets.NamedObjectWidgetInterface#getNamedObject()
     */
    public NamedObj getNamedObject() {
        return _namedObject;
    }

    /**
     * TODO
     */
    private final NamedObj _namedObject;

}
