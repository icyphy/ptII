/* Action to edit a custom icon.

 Copyright (c) 2006-2013 The Regents of the University of California.
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
package ptolemy.vergil.toolbox;

import java.awt.event.ActionEvent;

import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;

///////////////////////////////////////////////////////////////////
//// RotateOrFlipPorts

/**
 Action to rotate or flip ports.
 What exactly gets done depends on the constructor arguments.
 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public class RotateOrFlipPorts extends FigureAction {

    /** Create an action to rotate the ports.
     *  @param direction One of CLOCKWISE, COUNTERCLOCKWISE, FLIP_HORIZONTAL, or FLIP_VERTICAL.
     *  @param label The label to put in the menu.
     */
    public RotateOrFlipPorts(int direction, String label) {
        super(label);
        _direction = direction;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Indicator to rotate clockwise. */
    public static final int CLOCKWISE = 0;

    /** Indicator to rotate counterclockwise. */
    public static final int COUNTERCLOCKWISE = 1;

    /** Indicator to flip ports horizontally. */
    public static final int FLIP_HORIZONTAL = 2;

    /** Indicator to flip ports vertically. */
    public static final int FLIP_VERTICAL = 3;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Process the rotate command.
     *  @param event The event.
     */
    public void actionPerformed(ActionEvent event) {
        // Determine which entity was selected for the action.
        super.actionPerformed(event);

        final NamedObj object = getTarget();

        String moml = "";
        if (_direction == CLOCKWISE || _direction == COUNTERCLOCKWISE) {
            int rotation = 90;
            if (_direction == COUNTERCLOCKWISE) {
                rotation = -90;
            }
            // First determine whether the ports are already rotated.
            try {
                Attribute attribute = object.getAttribute("_rotatePorts");
                if (attribute instanceof Parameter) {
                    Token token = ((Parameter) attribute).getToken();
                    if (token instanceof IntToken) {
                        rotation += ((IntToken) token).intValue();
                    }
                }
            } catch (IllegalActionException e) {
                // Ignore and assume there is no prior rotation.
            }
            moml = "<property name=\"_rotatePorts\" class=\"ptolemy.data.expr.Parameter\" value=\""
                    + rotation + "\"/>";
        } else if (_direction == FLIP_VERTICAL) {
            // First determine whether the ports are already flipped.
            boolean flipOn = true;
            try {
                Attribute attribute = object.getAttribute("_flipPortsVertical");
                if (attribute instanceof Parameter) {
                    Token token = ((Parameter) attribute).getToken();
                    if (token instanceof BooleanToken) {
                        flipOn = !((BooleanToken) token).booleanValue();
                    }
                }
            } catch (IllegalActionException e) {
                // Ignore and assume there is no prior flip.
            }
            moml = "<property name=\"_flipPortsVertical\" class=\"ptolemy.data.expr.Parameter\" value=\""
                    + flipOn + "\"/>";
        } else if (_direction == FLIP_HORIZONTAL) {
            // First determine whether the ports are already flipped.
            boolean flipOn = true;
            try {
                Attribute attribute = object
                        .getAttribute("_flipPortsHorizontal");
                if (attribute instanceof Parameter) {
                    Token token = ((Parameter) attribute).getToken();
                    if (token instanceof BooleanToken) {
                        flipOn = !((BooleanToken) token).booleanValue();
                    }
                }
            } catch (IllegalActionException e) {
                // Ignore and assume there is no prior flip.
            }
            moml = "<property name=\"_flipPortsHorizontal\" class=\"ptolemy.data.expr.Parameter\" value=\""
                    + flipOn + "\"/>";
        }

        MoMLChangeRequest request = new MoMLChangeRequest(this, object, moml);
        request.setUndoable(true);
        object.requestChange(request);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    /** The direction requested in the constructor. */
    private int _direction;
}
