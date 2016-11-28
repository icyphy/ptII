/* A base class for all pickable actors

Copyright (c) 2000-2005 The Regents of the University of California.
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
package ptolemy.domains.gr.lib.experimental;

import ptolemy.actor.*;
import ptolemy.actor.gui.Placeable;
import ptolemy.actor.lib.*;
import ptolemy.actor.lib.gui.Display;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.gr.kernel.*;
import ptolemy.domains.gr.lib.*;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;

import com.sun.j3d.loaders.IncorrectFormatException;
import com.sun.j3d.loaders.ParsingErrorException;
import com.sun.j3d.loaders.Scene;
import com.sun.j3d.utils.geometry.*;
import com.sun.j3d.utils.picking.PickResult;
import com.sun.j3d.utils.picking.PickTool;
import com.sun.j3d.utils.picking.behaviors.PickMouseBehavior;
import com.sun.j3d.utils.universe.*;

import java.awt.event.InputEvent;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;

import javax.media.j3d.*;
import javax.vecmath.*;


///////////////////////////////////////////////////////////////////
//// GRPickActor

/**
   A base class for all pickable actors.

   @author C. Fong
   @version $Id$
   @Pt.ProposedRating Red (chf)
   @Pt.AcceptedRating Red (cxh)
*/
abstract public class GRPickActor extends GRShadedShape {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public GRPickActor(CompositeEntity container, String name)
        throws IllegalActionException, NameDuplicationException {
        super(container, name);
        clicked = new TypedIOPort(this, "click trigger");
        clicked.setOutput(true);
        clicked.setTypeEquals(BaseType.BOOLEAN);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    public TypedIOPort sceneGraphOut;

    /** BooleanToken
     */
    public TypedIOPort clicked;

    /** Return false if the scene graph is already initialized.
     *
     *  @return false if the scene graph is already initialized.
     *  @exception IllegalActionException will not be thrown..
     */
    public boolean prefire() throws IllegalActionException {
        boolean returnValue = true;

        if (_isSceneGraphInitialized) {
            returnValue = false;
        } else {
            returnValue = true;
        }

        if (isDirty) {
            returnValue = true;
        }

        return returnValue;
    }

    public void fire() throws IllegalActionException {
        super.fire();

        if (isDirty) {
            isDirty = false;
            clicked.send(0, new BooleanToken(true));
            System.out.println("sending out");
        }
    }

    boolean isDirty = false;

    public void processCallback() {
        isDirty = true;
        System.out.println("isDirty turned true");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         package protected variables       ////

    static PickCallback pick = null;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Override the base class to set the texture, if one is specified,
     *  now that the view screen is known.
     *  @exception IllegalActionException If the given actor is not a
     *   ViewScreen3D or if an invalid texture is specified.
     */
    protected void _setViewScreen(GRActor actor) throws IllegalActionException {
        super._setViewScreen(actor);
        BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0),
                                                       100.0);
        Canvas3D canvas = _viewScreen.getCanvas();
        BranchGroup branchGroup = _viewScreen.getBranchGroup();
        branchGroup = _getBranchGroup();

        // FIXME: this is one big fat hack!
        //if (pick!=null) pick.setEnable(false);
        try {
            pick = new PickCallback(this, canvas, branchGroup, bounds);
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex, "Failed to create PickCallback");
        }
    }

    abstract protected BranchGroup _getBranchGroup();

    protected Node _getNodeObject() {
        return (Node) branchGroup;
    }

    protected BranchGroup branchGroup;

    ///////////////////////////////////////////////////////////////////
    ////                         private inner classes             ////

    private class PickCallback extends PickMouseBehavior {
        Appearance savedAppearance = null;
        Shape3D oldShape = null;
        Appearance highlightAppearance;
        GRPickActor callbackActor;

        public PickCallback(GRPickActor pickableActor, Canvas3D canvas,
            BranchGroup root, Bounds bounds) {
            super(canvas, root, bounds);
            callbackActor = pickableActor;
            this.setSchedulingBounds(bounds);
            root.addChild(this);

            /*Color3f white = new Color3f(1.0f, 1.0f, 1.0f);
              Color3f black = new Color3f(0.0f, 0.0f, 0.0f);
              Color3f highlightColor = new Color3f(0.0f, 1.0f, 0.0f);
              Material highlightMaterial = new Material(highlightColor, black,
              highlightColor, white, 80.0f);
              highlightAppearance = new Appearance();
              highlightAppearance.setMaterial(new Material(highlightColor, black,
              highlightColor, white,
              80.0f));*/
            pickCanvas.setMode(PickTool.BOUNDS);
        }

        /** If the user presses the left button (button 3), then process the callback.
         *  @param xPosition The X position of the event
         *  @param yPosition The Y position of the event
         */
        public void updateScene(int xPosition, int yPosition) {
            pickCanvas.setShapeLocation(xPosition, yPosition);

            PickResult pickResult = pickCanvas.pickClosest();

            System.out.println("GRPickActor.updateScene(" + xPosition
                               + yPosition + "): " + callbackActor.getFullName() + " " + (pickResult == null) + " " + mevent.getModifiers());
            if (pickResult != null) {
                if (mevent.getModifiers() == InputEvent.BUTTON3_MASK) {
                    Shape3D shape = (Shape3D) pickResult.getNode(PickResult.SHAPE3D);
                    System.out.println("GRPickActor" + callbackActor.getFullName() + " "
                                       + InputEvent.getModifiersExText(mevent.getModifiersEx())
                                       + " the result " + shape + " "
                        + callbackActor);
                    callbackActor.processCallback();
                }
            }
        }
    }
}
