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
import javax.media.opengl.GLAutoDrawable;

import ptolemy.actor.lib.Transformer;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * An actor that is used for translating 3D objects.
 *
 * @author Yasemin Demir
 * @version $Id: JoglDirector.java 57401 2010-03-03 23:11:41Z ydemir $
 */
public class Translate3D extends Transformer {

    /**
     *  Construct a Translate3D object in the given container with the given name.
     *  If the container argument is null, a NullPointerException will
     *  be thrown. If the name argument is null, then the name is set
     *  to the empty string. Increment the version number of the workspace.
     *
     *  @param container Container of the director.
     *  @param name Name of this Translate3D.
     *  @exception IllegalActionException If this actor
     *  is not compatible with the specified container.
     *  @exception NameDuplicationException If the container not a
     *  CompositeActor and the name collides with an entity in the container.
     */
    public Translate3D(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input.setTypeEquals(BaseType.OBJECT);

        output.setTypeEquals(BaseType.OBJECT);
        translation = new PortParameter(this, "translation");
        translation.setExpression("{0.0, 0.0, 0.0}");

    }

    /** Specify the axis of translation. */

    public PortParameter translation;

    public void fire() throws IllegalActionException {
        translation.update();
        if (_debugging) {
            _debug("Called fire()");
        }

        if (input.hasToken(0)) {
            ObjectToken inputToken = (ObjectToken) input.get(0);
            Object inputObject = inputToken.getValue();
            if (!(inputObject instanceof GLAutoDrawable)) {
                throw new IllegalActionException(this,
                        "Input is required to be an instance of GL. Got "
                                + inputObject.getClass());
            }

            GL gl = ((GLAutoDrawable) inputObject).getGL();

            ArrayToken translationValue = ((ArrayToken) translation.getToken());

            gl.glTranslated(((DoubleToken) translationValue.getElement(0))
                    .doubleValue(), ((DoubleToken) translationValue
                    .getElement(1)).doubleValue(),
                    ((DoubleToken) translationValue.getElement(2))
                            .doubleValue());
            output.send(0, new ObjectToken(gl));
        }

    }
}
