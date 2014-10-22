/*
 Copyright (c) 1998-2014 The Regents of the University of California
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
 PROVIDED HEREUNDER IS ON AN  BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY
 *
 */
package diva.canvas.event;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

/** A class that accepts mouse events. Instances of this class
 * are used by event-handling code to decide whether they are
 * interested in a particular event.
 *
 * @version        $Id$
 * @author         John Reekie
 */
public class MouseFilter {
    /** The default mouse filter -- accepts button 1 with no
     * modifiers.
     */
    public static final MouseFilter defaultFilter = new MouseFilter(1);

    /** The default selection filter -- accepts button 1 with <i>no</i>
     * modifiers.
     */
    public static final MouseFilter selectionFilter = new MouseFilter(1);

    /** The alternate selection filter -- accepts button 1 with shift.
     */
    public static final MouseFilter alternateSelectionFilter = new MouseFilter(
            1, InputEvent.SHIFT_MASK);

    /** The mouse button mask
     */
    private int _buttonMask = InputEvent.BUTTON1_MASK | InputEvent.BUTTON2_MASK
            | InputEvent.BUTTON3_MASK;

    /** The modifier mask.  The Macintosh requires META_MASK for selecting
     *  relations and right clicking.
     */
    private int _modifierMask = InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK
            | InputEvent.META_MASK;

    // Do not or these masks in, they will conflict with right mousing
    // on transitions in modal models
    //| InputEvent.ALT_MASK
    //| InputEvent.ALT_GRAPH_MASK
    //| InputEvent.META_MASK;

    /** The modifier flags, after masking
     */
    private int _modifierFlags = 0;

    /** The button press to trap, or -1 if the button press number is not
     * important, in which events will not be filtered based on press number.
     */
    private int _pressNumber = -1;

    ///////////////////////////////////////////////////////////////////
    //// constructors and public methods

    /**
     * Construct a mouse filter that responds to the given mouse buttons
     * and modifier keys. The arguments must be constructed using the
     * button masks defined by java.awt.event.MouseEvent. More than one
     * button can be specified, in which case the filter will accept
     * events from any of them. In any case, modifier keys are ignored.
     */
    public MouseFilter(int button) {
        // Why don't we be clever and figure out if the button number
        // instead of mask is given? We know this works because we looked
        // at the AWT source.
        if (button <= 3) {
            switch (button) {
            case 1:
                button = InputEvent.BUTTON1_MASK;
                break;

            case 2:
                button = InputEvent.BUTTON2_MASK;
                break;

            case 3:
                button = InputEvent.BUTTON3_MASK;
                break;
            }
        }

        // OK, so just set it.
        // FIXME: check range.
        _buttonMask = button;
    }

    /** Construct a mouse filter that responds to the given mouse
     * buttons and modifier keys. The two arguments must be constructed
     * using the button and modifier masks defined by
     * java.awt.event.MouseEvent. More than one button can be specified,
     * in which case the filter will accept events from any of them. The
     * filter will accept modifier sets that exactly match modifiers.
     */
    public MouseFilter(int button, int modifiers) {
        this(button);
        _modifierFlags = modifiers;
    }

    /**
     * Construct a mouse filter that responds to the given mouse buttons
     * and modifier keys. The three arguments must be constructed using
     * the button and modifier masks defined by
     * java.awt.event.MouseEvent. More than one button can be specified,
     * in which case the filter will accept events triggered by any of
     * them. The mask argument filters out modifiers that will be
     * ignored; the filter will accept modifier sets that, after
     * masking, exactly match modifiers.
     */
    public MouseFilter(int button, int modifiers, int mask) {
        this(button);
        _modifierFlags = modifiers;
        _modifierMask = mask;
    }

    /**
     * Construct a mouse filter that responds to the given mouse buttons
     * and modifier keys. The three arguments must be constructed using
     * the button and modifier masks defined by
     * java.awt.event.MouseEvent. More than one button can be specified,
     * in which case the filter will accept events triggered by any of
     * them. The mask argument filters out modifiers that will be
     * ignored; the filter will accept modifier sets that, after
     * masking, exactly match modifiers.  The MouseFilter will only react to
     * the press number given in the last argument.  This will usually be one
     * or two.  Notice that if you want to react to drag events, they always
     * have a press number of zero.
     */
    public MouseFilter(int button, int modifiers, int mask, int pressNumber) {
        this(button, modifiers, mask);
        _pressNumber = pressNumber;
    }

    /**
     * Test whether the given MouseEvent passes the filter.
     */
    public boolean accept(MouseEvent event) {
        if (_pressNumber != -1 && event.getClickCount() != _pressNumber) {
            return false;
        }

        int m = event.getModifiers();

        // FIXME: Java people have broken the mask mechanism!
        // When you right click, this gets called with m=4,
        // indicating that the right mouse button was used.
        // Unfortunately, the _modifierMask includes the bit
        // corresponding to m=4 (for Shift?).  Since no
        // modifiers are required, _modifierFlags = 0, and
        // this test is never satisfied by a right click.
        // The result is that when you right click on a
        // connection between actors, the interactor for
        // connector does not accept the event, and the
        // event gets passed to the background graph,
        // which accepts it and produces the wrong
        // context menu.
        boolean val = (m & _buttonMask) != 0
                && _modifierFlags == (m & _modifierMask);

        // System.out.println("event = " + event);
        // System.out.println("FILTER = " + this);
        // System.out.println("ACCEPT? = " + val);
        return val;
    }

    /** Print a useful description of the mouse filter.
     */
    @Override
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append(super.toString() + "; Button "
                + MouseEvent.getMouseModifiersText(_buttonMask)
                + "; Modifiers "
                + MouseEvent.getMouseModifiersText(_modifierFlags)
                + "; Modifier mask "
                + MouseEvent.getMouseModifiersText(_modifierMask)
                + "; Press Number " + _pressNumber);
        return result.toString();
    }
}
