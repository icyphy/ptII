/* An actor that listens for keys pressed on the viewscreen

 Copyright (c) 2000-2014 The Regents of the University of California.
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
package ptolemy.domains.gr.lib;

import java.awt.AWTEvent;
import java.awt.event.KeyEvent;
import java.util.Enumeration;

import javax.media.j3d.Behavior;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Node;
import javax.media.j3d.WakeupCriterion;
import javax.media.j3d.WakeupOnAWTEvent;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.IntToken;
import ptolemy.data.type.BaseType;
import ptolemy.domains.gr.kernel.GRActor3D;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 An actor that listens for keys pressed on the viewscreen.
 @author C. Fong
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Red (chf)
 @Pt.AcceptedRating Red (chf)
 */
public class KeyInput3D extends GRActor3D {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public KeyInput3D(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        keycode = new TypedIOPort(this, "keycode");
        keycode.setOutput(true);
        keycode.setTypeEquals(BaseType.INT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** An output port that contains the keycode for a key pressed
     *  on the viewscreen.  The default type is int.
     */
    public TypedIOPort keycode;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If a key has been pressed, send the keycode to the <i>keycode</i>
     *  port.  If no key has been pressed, then no data is sent.
     *  @exception IllegalActionException If thrown while sending the
     *  data to the output port.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        if (_hasData) {
            keycode.send(0, new IntToken(_keycode));

            if (_debugging) {
                _debug("KeyCode = " + (int) _keycode);
            }

            _hasData = false;
        }
    }

    /** Set up this actor to listen to key presses.
     *  @exception IllegalActionException If thrown by the parent class.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _userInputNode = new BranchGroup();
        _react = new React();
        _react.setSchedulingBounds(new BoundingSphere());
        _userInputNode.addChild(_react);
        _hasData = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Get the user input BranchGroup node.
     *  @return The user input BranchGroup node for this actor.
     */
    @Override
    protected Node _getNodeObject() {
        return _userInputNode;
    }

    /** Add this node to the viewscreen.
     *  @exception IllegalActionException If throw while adding this
     *  node to the viewscreen.
     */
    @Override
    protected void _makeSceneGraphConnection() throws IllegalActionException {
        if (_viewScreen == null) {
            throw new IllegalActionException("GR error: no ViewScreen actor");
        } else {
            _viewScreen.addChild(_getNodeObject());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The BranchGroup to which is listened for key presses. */
    protected BranchGroup _userInputNode;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    private class React extends Behavior {
        @Override
        public void initialize() {
            this.wakeupOn(new WakeupOnAWTEvent(KeyEvent.KEY_PRESSED));
        }

        @Override
        public void processStimulus(Enumeration criteria) {
            WakeupCriterion wakeup;
            int eventId;
            AWTEvent[] event;

            while (criteria.hasMoreElements()) {
                wakeup = (WakeupCriterion) criteria.nextElement();
                event = ((WakeupOnAWTEvent) wakeup).getAWTEvent();

                for (AWTEvent element : event) {
                    eventId = element.getID();

                    if (eventId == KeyEvent.KEY_PRESSED) {
                        _keycode = ((KeyEvent) element).getKeyChar();
                        _hasData = true;
                    }
                }
            }

            this.wakeupOn(new WakeupOnAWTEvent(KeyEvent.KEY_PRESSED));
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** True if this actor has data. */
    protected boolean _hasData;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private React _react;

    private char _keycode;
}
