/* A switch/demux for GR scene graph objects

Copyright (c) 1997-2005 The Regents of the University of California.
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

@ProposedRating Red (chf)
@AcceptedRating Red (chf)
*/
package ptolemy.domains.gr.lib.experimental;

import ptolemy.actor.*;
import ptolemy.actor.lib.*;
import ptolemy.data.*;
import ptolemy.data.type.*;
import ptolemy.domains.gr.kernel.*;
import ptolemy.domains.gr.lib.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;

import javax.media.j3d.*;
import javax.vecmath.*;


//////////////////////////////////////////////////////////////////////////
//// Switch3D

/**
   @author C. Fong
*/
public class Switch3D extends GRTransform {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public Switch3D(CompositeEntity container, String name)
        throws IllegalActionException, NameDuplicationException {
        super(container, name);

        sceneGraphOut.setMultiport(true);

        select = new TypedIOPort(this, "select");
        select.setInput(true);
        select.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Input for index of port to select. The type is IntToken. */
    public TypedIOPort select;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and sets the public variables to point to the new ports.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Switch3D newObject = (Switch3D) super.clone(workspace);
        newObject.select = (TypedIOPort) newObject.getPort("select");
        return newObject;
    }

    /** Read a token from the select port and each channel of the input port,
     *  and output the token on the selected channel.
     *
     *  @exception IllegalActionException If there is no director, or if
     *  an input port does not have a token.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        if (select.getWidth() != 0) {
            if (select.hasToken(0)) {
                int index = (int) ((DoubleToken) select.get(0)).doubleValue();

                if (index != _previousIndex) {
                    int width = sceneGraphOut.getWidth();

                    if (index < width) {
                        _stopRenderer();
                        detachableGroup.detach();
                        attachmentGroup[index].addChild(detachableGroup);
                        _previousIndex = index;
                        _startRenderer();
                    }
                }
            }
        }
    }

    /** Setup the transform object
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        detachableGroup = new BranchGroup();
        detachableGroup.setCapability(BranchGroup.ALLOW_DETACH);
    }

    protected Node _getNodeObject() {
        return null;
    }

    protected void _makeSceneGraphConnection() throws IllegalActionException {
        _previousIndex = -1;

        int width = sceneGraphIn.getWidth();
        int i;

        for (i = 0; i < width; i++) {
            if (sceneGraphIn.hasToken(i)) {
                SceneGraphToken o = (SceneGraphToken) sceneGraphIn.get(i);
                Node n = (Node) o.getSceneGraphNode();
                detachableGroup.addChild(n);
            }
        }

        width = sceneGraphOut.getWidth();
        System.out.println("width " + width);
        attachmentGroup = new BranchGroup[width];

        for (i = 0; i < width; i++) {
            System.out.println("accessing # " + i);
            attachmentGroup[i] = new BranchGroup();
            attachmentGroup[i].setCapability(Group.ALLOW_CHILDREN_WRITE);
            attachmentGroup[i].setCapability(Group.ALLOW_CHILDREN_EXTEND);
            sceneGraphOut.send(i, new SceneGraphToken(attachmentGroup[i]));
        }
    }

    private BranchGroup detachableGroup;
    private BranchGroup[] attachmentGroup;
    private int _previousIndex = -1;
}
