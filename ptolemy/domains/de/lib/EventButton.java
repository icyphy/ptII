/* An actor that generates an empty token in response to a click of a button.

 @Copyright (c) 1998-2014 The Regents of the University of California.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

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

 PT_COPYRIGHT_VERSION 2
 COPYRIGHTENDKEY
 */
package ptolemy.domains.de.lib;

import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import ptolemy.actor.gui.Placeable;
import ptolemy.actor.lib.Source;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

///////////////////////////////////////////////////////////////////
//// EventButton

/**
 Output a Token in response to a click of a button. This class is closely
 related to the ButtonTime class in SR domain by Paul Whitaker.

 @author  Winthrop Williams
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (winthrop)
 @Pt.AcceptedRating Red (winthrop)
 */
public class EventButton extends Source implements Placeable {
    // FIXME: This actor is almost identitcal to the SR ButtonTime actor.
    // There is no need to have a copy here. An alternative design is to
    // put one of them into actor.lib direcory.

    /** Construct an actor with an input multiport of type GENERAL.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public EventButton(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        text = new StringAttribute(this, "text");
        text.setExpression("Click!");

        output.setTypeEquals(BaseType.GENERAL);

        _self = this;
    }

    ///////////////////////////////////////////////////////////////////
    ////        public variables and parameters                    ////

    /** The text to put on the button. */
    public StringAttribute text;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Fire the actor.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (_buttonPressed) {
            output.broadcast(new Token());
        }
    }

    /** Get the background color.
     *  @return The Color of the background.
     *  @see #setBackground(Color)
     */
    public Color getBackground() {
        return _button.getBackground();
    }

    /** Create a button on the screen, if necessary. If a graphical
     *  container has
     *  not been specified, place the button into its own frame.
     *  Otherwise, place it in the specified container.
     *  @exception IllegalActionException If the parent class throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _buttonPressed = false;

        if (_button == null) {
            place(_container);
        }

        if (_frame != null) {
            _frame.setVisible(true);
            _frame.toFront();
        }
    }

    /** An instance of JButton will be added to the specified container.
     *  This method needs to be called before the first call to initialize().
     *  Otherwise, an instance of JButton will be placed in its own frame.
     *  @param container The container into which to place the button.
     */
    @Override
    public void place(Container container) {
        _container = container;
        _button = new JButton(text.getExpression());
        _button.addActionListener(new ButtonListener());

        if (_container == null) {
            System.out.println("Container is null");

            // place the button in its own frame.
            _frame = new JFrame(getFullName());
            _frame.getContentPane().add(_button);
        } else {
            _container.add(_button);

            //_button.setBackground(Color.red);
        }
    }

    /** Reset the state of the actor and return whatever the superclass
     *  returns.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        if (_buttonPressed) {
            _buttonPressed = false;
        }

        return super.postfire();
    }

    /** Set the background color.
     *  @param background The background color.
     *  @see #getBackground()
     */
    public void setBackground(Color background) {
        _button.setBackground(background);
    }

    /** Override the base class to remove the display from its graphical
     *  container if the argument is null.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the base class throws it.
     *  @exception NameDuplicationException If the base class throws it.
     */
    @Override
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        super.setContainer(container);

        if (container == null) {
            _remove();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Remove the display from the current container, if there is one.
     */
    private void _remove() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (_button != null) {
                    if (_container != null) {
                        _container.remove(_button);
                        _container.invalidate();
                        _container.repaint();
                    } else if (_frame != null) {
                        _frame.dispose();
                    }
                }
            }
        });
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    // The button.
    private JButton _button;

    // A flag indicating whether the button has been pressed in the current
    // iteration
    private boolean _buttonPressed;

    // The container of the JButton.
    private Container _container;

    // The frame into which to put the text widget, if any.
    private JFrame _frame;

    private EventButton _self;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    class ButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            try {
                _buttonPressed = true;
                getDirector().fireAtCurrentTime(_self);
            } catch (IllegalActionException ex) {
                // Should never happen
                throw new InternalErrorException(ex.getMessage());
            }
        }
    }
}
