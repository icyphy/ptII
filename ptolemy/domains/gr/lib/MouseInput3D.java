/* An actor that listens for mouse clicks on the viewscreen

 Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Red (chf@eecs.berkeley.edu)
@AcceptedRating Red (chf@eecs.berkeley.edu)
*/
package ptolemy.domains.gr.lib;

import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.actor.*;
import ptolemy.actor.lib.*;
import ptolemy.domains.gr.kernel.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.GraphicsConfiguration;
import java.util.*;
import com.sun.j3d.utils.geometry.*;
import com.sun.j3d.utils.universe.*;
import javax.media.j3d.*;
import javax.vecmath.*;


public class MouseInput3D extends GRActor {

    public MouseInput3D(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {

        super(container, name);

        x = new TypedIOPort(this, "x");
        x.setOutput(true);
        x.setTypeEquals(BaseType.INT);
        y = new TypedIOPort(this, "y");
        y.setOutput(true);
        y.setTypeEquals(BaseType.INT);

    }

    public TypedIOPort x;
    public TypedIOPort y;

    public void initialize() throws IllegalActionException {
        super.initialize();
        obj = new BranchGroup();
        _react = new _React();
        _react.setSchedulingBounds(new BoundingSphere());
        obj.addChild(_react);
        _hasData = false;
    }

    public Node getNodeObject() {
        return (Node) obj;
    }

    public void fire() throws IllegalActionException  {
        if (_hasData) {
            x.send(0, new IntToken(_xClicked));
            y.send(0, new IntToken(_yClicked));
            _hasData = false;
            System.out.println("clicked location -> "+_xClicked+" "+_yClicked);
        }
    }

    public void makeSceneGraphConnection() throws IllegalActionException {
        if (_root == null) {
            throw new IllegalActionException(
                      "GR error: no ViewScreen actor");
        } else {
            _root.addChild(getNodeObject());
        }
    }

    private class _React extends Behavior {

        public void initialize() {
            this.wakeupOn(new WakeupOnAWTEvent(MouseEvent.MOUSE_PRESSED));
        }

        public void processStimulus(Enumeration criteria) {
            System.out.println("new stimulus");
            WakeupCriterion wakeup;
            int eventId;
            AWTEvent[] event;

            while (criteria.hasMoreElements()) {
                wakeup = (WakeupCriterion) criteria.nextElement();
                event = ((WakeupOnAWTEvent)wakeup).getAWTEvent();
              	for (int i=0; i<event.length; i++) {
                    eventId = event[i].getID();
                    if (eventId == MouseEvent.MOUSE_PRESSED) {
            	        _xClicked = ((MouseEvent)event[i]).getX();
                        _yClicked = ((MouseEvent)event[i]).getY();
                        _hasData = true;
                        System.out.println(" "+_xClicked+" "+_yClicked);
                    }
                }
            }
            this.wakeupOn(new WakeupOnAWTEvent(MouseEvent.MOUSE_PRESSED));
        }
    }

    protected _React _react;
    protected BranchGroup obj;

    boolean _hasData;
    int _xClicked;
    int _yClicked;
}
