/* A panel containing controls for a Ptolemy II model.

Copyright (c) 1998-2005 The Regents of the University of California.
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
package ptolemy.domains.ct.demo.Helicopter;

import java.awt.GridLayout;
import java.util.Iterator;

import javax.swing.JPanel;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.gui.ModelPane;
import ptolemy.actor.gui.Placeable;


//////////////////////////////////////////////////////////////////////////
//// HelicopterModelPane

/**
   This class extends the ModelPane class to place the plots in the
   helicopter applet in a grid way.

   @see ModelPane
   @author Jie Liu
   @version $Id$
   @since Ptolemy II 1.0
   @Pt.ProposedRating Yellow (eal)
   @Pt.AcceptedRating Red (eal)
*/
public class HelicopterModelPane extends ModelPane {
    /** Construct a panel for interacting with the specified Ptolemy II model.
     *  This uses the default layout, which is horizontal, and shows
     *  control buttons, top-level parameters, and director parameters.
     *  @param model The model to control.
     */
    public HelicopterModelPane(CompositeActor model) {
        this(model, HORIZONTAL, BUTTONS | TOP_PARAMETERS | DIRECTOR_PARAMETERS);
    }

    /** Construct a panel for interacting with the specified Ptolemy II model.
     *  The layout argument should be one of HORIZONTAL, VERTICAL, or
     *  CONTROLS_ONLY; it determines whether the controls are put to
     *  the left of, or above the placeable displays. If CONTROLS_ONLY
     *  is given, then no displays are created for placeable objects.
     *  <p>
     *  The show argument is a bitwise
     *  or of any of BUTTONS, TOP_PARAMETERS, or DIRECTOR_PARAMETERS.
     *  Or it can be 0, in which case, no controls are shown.
     *  If BUTTONS is included, then a panel of buttons, go, pause,
     *  resume, and stop, are shown.  If TOP_PARAMETERS is included,
     *  then the top-level parameters of the model are included.
     *  If DIRECTOR_PARAMETERS is included, then the parameters of
     *  the director are included.
     *  @param model The model to control.
     *  @param layout HORIZONTAL or VERTICAL layout.
     *  @param show Indicator of which controls to show.
     */
    public HelicopterModelPane(final CompositeActor model, int layout, int show) {
        super(model, layout, show);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected method                  ////

    /** Place the placeable objects in the model to the display pane.
     *  This method place all placeables vertically. Derived classes
     *  may override this method if the placeable objects are to be
     *  placed differently.
     *  @param model The model that contains the placeable objects.
     */
    protected void _createPlaceable(CompositeActor model) {
        if (_displays != null) {
            remove(_displays);
            _displays = null;
        }

        // place the placeable objects in the model
        _displays = new JPanel();
        _displays.setBackground(null);

        add(_displays);
        _displays.setLayout(new GridLayout(2, 2));
        _displays.setBackground(null);

        // Put placeable objects in a reasonable place.
        Iterator atomicEntities = model.allAtomicEntityList().iterator();

        while (atomicEntities.hasNext()) {
            Object object = atomicEntities.next();

            if (object instanceof Placeable) {
                ((Placeable) object).place(_displays);
            }
        }
    }
}
