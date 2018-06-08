/* Loader

 Copyright (c) 2000-2008 The Regents of the University of California.
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

import java.io.FileNotFoundException;

import javax.media.j3d.Node;

import com.sun.j3d.loaders.IncorrectFormatException;
import com.sun.j3d.loaders.ParsingErrorException;
import com.sun.j3d.loaders.Scene;
import com.sun.j3d.loaders.objectfile.ObjectFile;

import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 Load a WaveFront .obj file that contains descriptions of 3-D objects.
 See the
 <a href="http://download.java.net/media/java3d/javadoc/1.3.2/com/sun/j3d/loaders/objectfile/ObjectFile.html" target="_top">com.sun.j3d.loader.objectfile.ObjectFile</a> documentation for details.
 @author C. Fong
 @version $Id$
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class Loader3D extends GRShadedShape {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Loader3D(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        filename = new Parameter(this, "filename",
                new StringToken("chopper.obj"));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The name of the file to be opened.  The initial
     *  default value is "chopper.obj".
     */
    public Parameter filename;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the encapsulated Java3D node of this 3D actor.
     *  The encapsulated node for this actor is a Java3D scene group.
     *  @return The scene group.
     */

    @Override
    protected Node _getNodeObject() {
        if (obj == null) {
            throw new NullPointerException(
                    "Call _createModel() before calling _getNodeObject().");
        }
        return obj.getSceneGroup();
    }

    /** Create the model by loading the filename.
     *  @exception IllegalActionException If the file cannot be found or
     *  if the file is not a valid Wavefront .obj file"
     */
    @Override
    protected void _createModel() throws IllegalActionException {
        String fileName = ((StringToken) filename.getToken()).stringValue();

        //Appearance ap = new Appearance();
        //ap.setColoringAttributes(new ColoringAttributes(_color.x,
        // _color.y, _color.z, ColoringAttributes.SHADE_GOURAUD));
        int flags = ObjectFile.RESIZE;

        //if (!noTriangulate) flags |= ObjectFile.TRIANGULATE;
        //if (!noStripify) flags |= ObjectFile.STRIPIFY;
        ObjectFile objectFile = new ObjectFile(flags,
                (float) (creaseAngle * Math.PI / 180.0));
        Scene scene = null;

        try {
            scene = objectFile.load(fileName);
        } catch (FileNotFoundException ex) {
            throw new IllegalActionException(this, ex, "File not found!");
        } catch (ParsingErrorException ex) {
            throw new IllegalActionException(this, ex,
                    "File is not a valid Wavefront .obj file.");
        } catch (IncorrectFormatException ex) {
            throw new IllegalActionException(this, ex,
                    "File is not a valid Wavefront .obj file.");
        }

        obj = scene;
    }

    private double creaseAngle = 60.0;

    private Scene obj = null;
}
