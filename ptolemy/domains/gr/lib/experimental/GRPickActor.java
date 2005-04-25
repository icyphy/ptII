/* A base class for all pickable actor

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
import com.sun.j3d.loaders.vrml97.VrmlLoader;
import com.sun.j3d.utils.geometry.*;
import com.sun.j3d.utils.picking.PickResult;
import com.sun.j3d.utils.picking.PickTool;
import com.sun.j3d.utils.picking.behaviors.PickMouseBehavior;
import com.sun.j3d.utils.universe.*;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;

import javax.media.j3d.*;
import javax.vecmath.*;


//////////////////////////////////////////////////////////////////////////
//// GRPickActor

/**
   @author C. Fong
   @version $Id$
   @Pt.ProposedRating Red (chf)
   @Pt.AcceptedRating Red (cxh)
*/
abstract public class GRPickActor extends GRActor {
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
        sceneGraphOut = new TypedIOPort(this, "sceneGraphOut");
        sceneGraphOut.setOutput(true);
        sceneGraphOut.setTypeEquals(SceneGraphToken.TYPE);
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
        ;

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
    ////                         public methods                    ////

    /*  Create the Java3D geometry and appearance for this GR actors
     *
     *  @exception IllegalActionException if the current director
     *    is not a GRDirector
     */
    /*
      public void initialize() throws IllegalActionException {
      super.initialize();
      _createModel();
      }*/
    public void initialize() throws IllegalActionException {
        super.initialize();
        System.out.println("init picker");
        _createModel();

        //bg = new BranchGroup();
        //bg.addChild(_containedNode);
        BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0),
                100.0);
        Canvas3D canvas = ((ViewScreen) _root).getCanvas();
        BranchGroup branchGroup = ((ViewScreen) _root).getBranchGroup();
        branchGroup = _getBranchGroup();
        System.out.println(" alert " + canvas + " " + branchGroup);

        // FIXME: this is one big fat hack!
        //if (pick!=null) pick.setEnable(false);
        pick = new PickCallback(this, canvas, branchGroup, bounds);
    }

    static PickCallback pick = null;

    abstract protected BranchGroup _getBranchGroup();

    protected Node _getNodeObject() {
        return (Node) branchGroup;
    }

    protected void _makeSceneGraphConnection() throws IllegalActionException {
        sceneGraphOut.send(0, new SceneGraphToken(_getNodeObject()));
    }

    protected void _createModel() throws IllegalActionException {
    }

    protected BranchGroup branchGroup;

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

        public void updateScene(int xpos, int ypos) {
            PickResult pickResult = null;
            Shape3D shape = null;

            pickCanvas.setShapeLocation(xpos, ypos);

            pickResult = pickCanvas.pickClosest();

            if (pickResult != null) {
                if (mevent.getModifiers() == 4) {
                    shape = (Shape3D) pickResult.getNode(PickResult.SHAPE3D);
                    System.out.println("the result " + shape + " "
                        + callbackActor);
                    callbackActor.processCallback();
                }
            }
        }
    }
}
