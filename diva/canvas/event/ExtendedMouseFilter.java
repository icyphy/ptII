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
 * interested in a particular event.  This class supports the extended
 * event mechanism in jdk1.4, which allows proper distinction between
 * ALT and Button2 and Meta and Button3.
 *
 * @version        $Id$
 * @author         Steve Neuendorffer
 */
public class ExtendedMouseFilter extends MouseFilter {
    /** The default mouse filter -- accepts button 1 with no
     * modifiers.
     */
    public static final MouseFilter defaultFilter = new ExtendedMouseFilter(1);

    /** The default selection filter -- accepts button 1 with <i>no</i>
     * modifiers.
     */
    public static final MouseFilter selectionFilter = new ExtendedMouseFilter(1);

    /** The alternate selection filter -- accepts button 1 with shift.
     */
    public static final MouseFilter alternateSelectionFilter = new ExtendedMouseFilter(
            1, InputEvent.SHIFT_DOWN_MASK);

    /** The mouse button mask
     */
    private int _button = 1;

    /** The modifier mask
     */
    private int _modifierMask;

    /** The modifier flags, after masking
     */
    private int _modifierFlags;

    /** The button press to trap, or -1 if the button press number is not
     * important, in which events will not be filtered based on press number.
     */
    private int _pressNumber;

    ///////////////////////////////////////////////////////////////////
    //// constructors and public methods

    /**
     * Construct a mouse filter that responds to the given mouse buttons
     * and modifier keys. The arguments must be constructed using the
     * button masks defined by java.awt.event.MouseEvent. More than one
     * button can be specified, in which case the filter will accept
     * events from any of them. In any case, modifier keys are ignored.
     */
    public ExtendedMouseFilter(int button) {
        this(button, 0);
    }

    /** Construct a mouse filter that responds to the given mouse
     * buttons and modifier keys. The two arguments must be constructed
     * using the button and modifier masks defined by
     * java.awt.event.MouseEvent. More than one button can be specified,
     * in which case the filter will accept events from any of them. The
     * filter will accept modifier sets that exactly match modifiers.
     */
    public ExtendedMouseFilter(int button, int extendedModifiers) {
        this(button, extendedModifiers, InputEvent.SHIFT_DOWN_MASK
                | InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK
                | InputEvent.ALT_GRAPH_DOWN_MASK | InputEvent.META_DOWN_MASK);
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
    public ExtendedMouseFilter(int button, int extendedModifiers, int mask) {
        this(button, extendedModifiers, mask, -1);
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
    public ExtendedMouseFilter(int button, int extendedModifiers, int mask,
            int pressNumber) {
        // bogus super constructor...  It would have been better if the
        // base class was an interface, but oh well.
        super(0);
        _button = button;
        _modifierFlags = extendedModifiers;
        _modifierMask = mask;
        _pressNumber = pressNumber;
    }

    /**
     * Test whether the given MouseEvent passes the filter.
     */
    @Override
    public boolean accept(MouseEvent event) {
        if (_pressNumber != -1 && event.getClickCount() != _pressNumber) {
            return false;
        }

        int m = event.getModifiersEx();
        boolean val = event.getButton() == _button
                && _modifierFlags == (m & _modifierMask);

        //         System.out.println("event = " + event);
        //         System.out.println("FILTER = " + this);
        //         System.out.println("ACCEPT? = " + val);
        return val;
    }

    /** Print a useful description of the mouse filter.
     */
    @Override
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append(getClass().toString() + "; Button " + _button
                + "; Modifiers "
                + InputEvent.getModifiersExText(_modifierFlags)
                + "; Modifier mask "
                + InputEvent.getModifiersExText(_modifierMask)
                + "; Press Number " + _pressNumber);
        return result.toString();
    }
}
