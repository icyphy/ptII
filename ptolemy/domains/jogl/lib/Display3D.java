/*
Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 2010-2011 The Regents of the University of California.
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

Ptolemy II includes the work of others, to see those copyrights, follow
the copyright link on the splash page or see copyright.htm.
*/
package ptolemy.domains.jogl.lib;

import java.awt.Color;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.media.opengl.GL;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLDrawableFactory;
import javax.media.opengl.glu.GLU;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gui.Placeable;
import ptolemy.domains.jogl.kernel.GLActor3D;
import ptolemy.domains.jogl.kernel.GLToken;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * An actor that is used for displaying 3D animation.
 *
 * @author Yasemin Demir
 * @version $Id: JoglDirector.java 57401 2010-03-03 23:11:41Z ydemir $
 */
public class Display3D extends TypedAtomicActor implements Placeable {

    /**
     *  Construct a Display3D object in the given container with the given name.
     *  If the container argument is null, a NullPointerException will
     *  be thrown. If the name argument is null, then the name is set
     *  to the empty string. Increment the version number of the workspace.
     *
     *  @param container Container of the director.
     *  @param name Name of this Display3D.
     *  @exception IllegalActionException If this actor
     *  is not compatible with the specified container.
     *  @exception NameDuplicationException If the container not a
     *  CompositeActor and the name collides with an entity in the container.
     */

    public Display3D(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        glIn = new TypedIOPort(this, "glIn");
        glIn.setInput(true);
        glIn.setTypeEquals(GLToken.GL_TYPE);
        glIn.setMultiport(true);

    }

    ///////////////////////////////////////////////////////////////////
    ////                     Ports and Parameters                  ////

    /** The input scene graph. Actors that produce 3D objects
     *  can be connected to this port for rendering.
     *  The type of this port is sceneGraph.
     */
    public TypedIOPort glIn;

    public void fire() throws IllegalActionException {
        if (_debugging) {
            _debug("Called fire()");
        }

        if (isResized) {
            _resize(_canvas);
            isResized = false;
        }

        _gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        _gl.glClear(GL.GL_DEPTH_BUFFER_BIT);

        _gl.glMatrixMode(GL.GL_MODELVIEW);
        _gl.glLoadIdentity();

        for (int i = 0; i < _width; i++) {
            GL object = (GL) glIn.get(i);
            if (!(object instanceof ptolemy.domains.jogl.kernel.GLActor3D)) {
                ((GLActor3D) object).render(_gl);
            }
        }

    }

    public void place(Container container) {
        _container = container;

        if (_container == null) {
            return;
        }

        Container c = _container.getParent();

        while (c.getParent() != null) {
            c = c.getParent();
        }

        // If we had created a frame before, then blow it away.
        if (_frame != null) {
            _frame.dispose();
            _frame.add(_canvas);
            _frame.setSize(300, 300);
            _frame.setBackground(Color.WHITE);

            _frame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });
        }

    }

    /**
     * Initializes rendering frame, canvas, gl object.
     *
     */

    public void initialize() throws IllegalActionException {
        super.initialize();

        _capabilities = new GLCapabilities();
        _canvas = new GLCanvas(_capabilities);
        _context = GLDrawableFactory.getFactory().createExternalGLContext();
        _context.makeCurrent();
        _gl = _context.getGL();
        _gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        _gl.glColor3f(0.0f, 0.0f, 0.0f);
        _gl.glClearDepth(1.0f);
        _gl.glEnable(GL.GL_DEPTH_TEST);
        _gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);

    }

    /**
     * Resizes the screen.
     *
     */

    void _resize(GLCanvas canvas) {

        _gl.glViewport(0, 0, _width, _height);
        _gl.glMatrixMode(GL.GL_PROJECTION);
        _gl.glLoadIdentity();
        float aspect = 1;
        _glu.gluPerspective(45.0f, aspect, 0.5f, 400.0f);
        _gl.glMatrixMode(GL.GL_MODELVIEW);
        _gl.glLoadIdentity();
    }

    /** This is added to the frame and drawable part of the frame. */
    protected GLCanvas _canvas;

    /**
     * Specify a set of OpenGL capabilities that a rendering
     * context must support,such as color depth and whether stereo
     * is enabled.
     */
    protected GLCapabilities _capabilities;

    /**
     * Specify the frame associated with the container.
     */
    protected Frame _frame;

    GLContext _context;

    /** Specify height of the frame. */
    protected int _height = 480;

    /** Specify width of the frame. */
    protected int _width = 640;
    int i = 0;

    private Container _container;

    // OpenGL
    private GL _gl;
    private GLU _glu;

    // window sizing
    private boolean isResized = false;

    GraphicsConfiguration config;

}
