/* A GR generic image

Copyright (c) 1998-2005 The Regents of the University of California.
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

import ptolemy.actor.lib.*;
import ptolemy.actor.lib.gui.Display;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.gr.lib.*;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;

import com.sun.j3d.utils.geometry.*;
import com.sun.j3d.utils.image.TextureLoader;

import javax.media.j3d.*;
import javax.vecmath.*;


//////////////////////////////////////////////////////////////////////////
//// Image3D

/** This actor contains the geometry and appearance specifications for a GR
    box.  The output port is used to connect this actor to the Java3D scene
    graph. This actor will only have meaning in the GR domain.

    The parameters <i>xLength</i>, <i>yHeight</i>, and <i>zWidth</i> determine
    the dimensions of box.

    @author C. Fong
*/
public class Image3D extends GRPickActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Image3D(CompositeEntity container, String name)
        throws IllegalActionException, NameDuplicationException {
        super(container, name);

        imageLength = new Parameter(this, "image length", new DoubleToken(0.5));
        imageHeight = new Parameter(this, "image height", new DoubleToken(0.5));
        filename = new Parameter(this, "filename", new StringToken("stripe.gif"));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The length of the box in the x-axis.
     *  This parameter should contain a DoubleToken.
     *  The default value of this parameter is the DoubleToken 0.5
     */
    public Parameter imageLength;
    public Parameter filename;

    /** The width of the box in the z-axis.
     *  This parameter should contain a DoubleToken.
     *  The default value of this parameter is the DoubleToken 0.5
     */
    public Parameter imageHeight;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    protected void _createModel() throws IllegalActionException {
        QuadArray plane = new QuadArray(4,
                GeometryArray.COORDINATES | QuadArray.NORMALS
                        | GeometryArray.TEXTURE_COORDINATE_2);

        Point3f p = new Point3f(-1.0f, 1.0f, 0.0f);
        plane.setCoordinate(0, p);
        p.set(-1.0f, -1.0f, 0.0f);
        plane.setCoordinate(1, p);
        p.set(1.0f, -1.0f, 0.0f);
        plane.setCoordinate(2, p);
        p.set(1.0f, 1.0f, 0.0f);
        plane.setCoordinate(3, p);

        Point2f[] qq = new Point2f[4];
        qq[0] = new Point2f(0.0f, 1.0f);
        qq[1] = new Point2f(0.0f, 0.0f);
        qq[2] = new Point2f(1.0f, 0.0f);
        qq[3] = new Point2f(1.0f, 1.0f);
        plane.setTextureCoordinate(0, 0, new TexCoord2f(qq[0]));
        plane.setTextureCoordinate(0, 1, new TexCoord2f(qq[1]));
        plane.setTextureCoordinate(0, 2, new TexCoord2f(qq[2]));
        plane.setTextureCoordinate(0, 3, new TexCoord2f(qq[3]));

        Appearance appear = new Appearance();

        String fileName = (String) ((StringToken) filename.getToken())
                    .stringValue();

        TextureLoader loader = new TextureLoader(fileName,
                ((ViewScreen) _root).getCanvas());
        ImageComponent2D image = loader.getImage();
        System.out.println("image " + image);

        if (image == null) {
            System.out.println("load failed for texture: " + filename);
        }

        // can't use parameterless constuctor
        Texture2D texture = new Texture2D(Texture.BASE_LEVEL, Texture.RGBA,
                image.getWidth(), image.getHeight());
        texture.setImage(0, image);

        //texture.setEnable(false);
        appear.setTexture(texture);

        appear.setTransparencyAttributes(new TransparencyAttributes(
                TransparencyAttributes.FASTEST, 0.1f));
        top = new BranchGroup();
        _containedNode = new Shape3D(plane, appear);
        top.addChild(_containedNode);

        //_containedNode = new Shape3D(cube);
    }

    BranchGroup top = new BranchGroup();

    public void processCallback() {
        super.processCallback();

        try {
            System.out.println("call "
                + ((StringToken) filename.getToken()).stringValue());
        } catch (Exception e) {
            System.out.println("process call back exception");
        }
    }

    /** Return the encapsulated Java3D node of this 3D actor. The encapsulated
     *  node for this actor is a Java3D box.
     *  @return the Java3D box.
     */
    protected Node _getNodeObject() {
        return (Node) top; //_containedNode;
    }

    protected BranchGroup _getBranchGroup() {
        return (BranchGroup) top;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return the value of the length parameter
     *  @return the length of the box
     *  @exception IllegalActionException If the value of some parameters can't
     *   be obtained
     */
    private double _getLength() throws IllegalActionException {
        double value = ((DoubleToken) imageLength.getToken()).doubleValue();
        return value / 2.0;
    }

    /** Return the value of the height parameter
     *  @return the height of the box
     *  @exception IllegalActionException If the value of some parameters can't
     *   be obtained
     */
    private double _getHeight() throws IllegalActionException {
        double value = ((DoubleToken) imageHeight.getToken()).doubleValue();
        return value / 2.0;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private Shape3D _containedNode;
    private static final float[] verts = {
            
            // front face
            1.0f,
            -1.0f,
            1.0f,
            1.0f,
            1.0f,
            1.0f,
            -1.0f,
            1.0f,
            1.0f,
            -1.0f,
            -1.0f,
            1.0f
        };
    private static final Vector3f[] normals = {
            new Vector3f(0.0f, 0.0f, 1.0f), // front face
        };
}
