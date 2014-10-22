/*
 The base named object widget class that implements {@link NamedObjectWidgetInterface}.

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

import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

import ptolemy.homer.kernel.PositionableElement;

///////////////////////////////////////////////////////////////////
//// NamedObjectWidget

/** The base named object widget class that implements {@link NamedObjectWidgetInterface}.
 *
 *  @author Anar Huseynov
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (ahuseyno)
 *  @Pt.AcceptedRating Red (ahuseyno)
 */
public abstract class NamedObjectWidget extends Widget implements
        NamedObjectWidgetInterface {

    /** Create new instance of the widget.
     *  @param scene The scene of the widget.
     *  @param element The named object to visualize.
     */
    public NamedObjectWidget(Scene scene, PositionableElement element) {
        super(scene);
        _element = element;
        setCheckClipping(true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return positionable element that the instance is visualizing.
     *  @return The positionable element.
     */
    @Override
    public PositionableElement getPositionableElement() {
        return _element;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The positionable element that the widget is visualizing.
     */
    private final PositionableElement _element;
}
