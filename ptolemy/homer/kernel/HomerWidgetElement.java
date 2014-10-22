/* The definition of any named object that has a Netbeans widget representation.

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

package ptolemy.homer.kernel;

import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

import ptolemy.actor.injection.PortableContainer;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// HomerWidgetElement

/** The definition of any named object that has a Netbeans widget representation.
 *
 *  @author Peter Foldes
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (pdf)
 *  @Pt.AcceptedRating Red (pdf)
 */
public class HomerWidgetElement extends PositionableElement {

    ///////////////////////////////////////////////////////////////////
    ////                constructor                                ////

    /** Create a new definition of an element.
     *
     *  @param element The Ptolemy named object to wrap.
     *  @param scene The graphical area where the widget representing the
     *  element will be added to.
     */
    public HomerWidgetElement(NamedObj element, Scene scene) {
        super(element);
        _scene = scene;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** It's normally used to add the visual representation of the element to the
     *  provided container. In this case getWidget() should be used to get the
     *  representation, and added to the container manually.
     *
     *  @param container The container to place the representation in. Not used.
     *  @exception IllegalActionException The method will always return the exception.
     *  Use getWidget() instead.
     */
    @Override
    public void addToContainer(PortableContainer container)
            throws IllegalActionException {
        // Don't use this.
        throw new IllegalActionException("Don't use this method.");
    }

    /** Return the Netbeans widget representing the element. If it's not yet created,
     *  this method will create it.
     *
     *  @return The Netbeans widget representing the element.
     */
    public Widget getWidget() {
        if (_widget == null) {
            try {
                _widget = WidgetLoader.loadWidget(_scene,
                        HomerWidgetElement.this, getElement().getClass());
            } catch (IllegalActionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NameDuplicationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return _widget;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The Netbeans widget representing the element.
     */
    private Widget _widget;

    /** The scene where the widget will be placed on.
     */
    private Scene _scene;
}
