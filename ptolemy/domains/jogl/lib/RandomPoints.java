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

import javax.media.opengl.GL;

import ptolemy.actor.lib.Sink;
import ptolemy.data.ObjectToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * An actor that is used for displaying 3D randomly generated points.
 *
 * @author Yasemin Demir
 * @version $Id: JoglDirector.java 57401 2010-03-03 23:11:41Z ydemir $
 */

public class RandomPoints extends Sink {

    /**
     *  Construct a RandomPoints object in the given container with the given name.
     *  If the container argument is null, a NullPointerException will
     *  be thrown. If the name argument is null, then the name is set
     *  to the empty string. Increment the version number of the workspace.
     *
     *  @param container Container of the director.
     *  @param name Name of this RandomPoints.
     *  @exception IllegalActionException If this actor
     *  is not compatible with the specified container.
     *  @exception NameDuplicationException If the container not a
     *  CompositeActor and the name collides with an entity in the container.
     */

    public RandomPoints(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        input.setTypeEquals(BaseType.OBJECT);
    }

    public void fire() throws IllegalActionException {

        if (_debugging) {
            _debug("Called fire()");
        }
        if (input.hasToken(0)) {
            ObjectToken inputToken = (ObjectToken) input.get(0);
            Object inputObject = inputToken.getValue();
            if (!(inputObject instanceof GL)) {
                throw new IllegalActionException(this,
                        "Input is required to be an instance of GL. Got "
                                + inputObject.getClass());
            }

            GL gl = (GL) inputObject;

            gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
            /* A random point is generated. Because animator will run display() again and again
             * in its thread, randomly generated points are displayed.
             */
            double x = Math.random() * 480;
            double y = Math.random() * 640;
            // specify to draw a point
            gl.glPointSize(4);
            gl.glBegin(GL.GL_POINTS);
            gl.glVertex2d(x, y);
            gl.glEnd();

            /* Different GL implementations buffer commands in several different locations,
             * including network buffers and the graphics
             * accelerator itself. glFlush empties all of these buffers, causing all issued commands to be
             * executed as quickly as they are accepted by the actual rendering engine.
             */

            gl.glFlush();

        }

    }
}
