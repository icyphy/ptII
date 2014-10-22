/* An actor that listens for mouse clicks on the viewscreen

 Copyright (c) 1998-2014 The Regents of the University of California.
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
import java.awt.event.MouseEvent;
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

/**  An actor that listens for mouse clicks on the viewscreen.

 @author C. Fong
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Red (chf)
 @Pt.AcceptedRating Red (chf)
 */
public class MouseInput3D extends GRActor3D {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public MouseInput3D(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        x = new TypedIOPort(this, "x");
        x.setOutput(true);
        x.setTypeEquals(BaseType.INT);
        y = new TypedIOPort(this, "y");
        y.setOutput(true);
        y.setTypeEquals(BaseType.INT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** An output port containing an integer representing the X
     *  coordinate of the mouse.
     */
    public TypedIOPort x;

    /** An output port containing an integer representing the Y
     *  coordinate of the mouse.
     */
    public TypedIOPort y;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the mouse has moved,  send the coordinates to the <i>x</i>
     *  and <i>y</i> ports.
     *  If mouse has not moved, then no data is sent.
     *  @exception IllegalActionException If thrown while sending the
     *  data to the output ports.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        if (_hasData) {
            x.send(0, new IntToken(_xClicked));
            y.send(0, new IntToken(_yClicked));
            _hasData = false;

            if (_debugging) {
                _debug("clicked location -> " + _xClicked + " " + _yClicked);
            }
        }
    }

    /** Set up this actor to listen to mouse motion events.
     *  @exception IllegalActionException If thrown by the parent class.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _containedNode = new BranchGroup();
        _react = new React();
        _react.setSchedulingBounds(new BoundingSphere());
        _containedNode.addChild(_react);
        _hasData = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Get the user input BranchGroup node.
     *  @return The user input BranchGroup node for this actor.
     */
    @Override
    protected Node _getNodeObject() {
        return _containedNode;
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

        // It would be nice if we did this..
        //sceneGraphOut.send(0, new SceneGraphToken(_getNodeObject()));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    private class React extends Behavior {
        @Override
        public void initialize() {
            this.wakeupOn(new WakeupOnAWTEvent(MouseEvent.MOUSE_PRESSED));
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

                    if (eventId == MouseEvent.MOUSE_PRESSED) {
                        _xClicked = ((MouseEvent) element).getX();
                        _yClicked = ((MouseEvent) element).getY();
                        _hasData = true;
                        try {
                            getDirector().fireAtCurrentTime(MouseInput3D.this);
                        } catch (IllegalActionException e) {
                            // Ignore. The model is not running.
                        }
                    }
                }
            }

            this.wakeupOn(new WakeupOnAWTEvent(MouseEvent.MOUSE_PRESSED));
        }
    }

    /** The user input BranchGroup node contained by this actor. */
    protected BranchGroup _containedNode;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private React _react;

    private boolean _hasData;

    private int _xClicked;

    private int _yClicked;
}
