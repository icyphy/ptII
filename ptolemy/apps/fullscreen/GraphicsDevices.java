/* An actor that generates an ArrayToken containing ObjectTokens where
   each ObjectToken is a GraphicsDevice.

   @Copyright (c) 2001-2005 The Regents of the University of California.
   All rights reserved.

   Permission is hereby granted, without written agreement and without
   license or royalty fees, to use, copy, modify, and distribute this
   software and its documentation for any purpose, provided that the
   above copyright notice and the following two paragraphs appear in all
   copies of this software.

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

   PT_COPYRIGHT_VERSION 2
   COPYRIGHTENDKEY
   @ProposedRating Red (cxh)
   @AcceptedRating Red (cxh)
*/
package ptolemy.apps.fullscreen;

import ptolemy.actor.lib.Source;
import ptolemy.data.ArrayToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;


//////////////////////////////////////////////////////////////////////////
//// GraphicsDevices

/**
   An actor that generates an ArrayToken containing ObjectTokens where
   each ObjectToken is a java.awt.GraphicsDevice that represents a screen
   or other device that we can write to.

   @author  Christopher Hylands
   @version $Id$ */
public class GraphicsDevices extends Source {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public GraphicsDevices(CompositeEntity container, String name)
        throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Set the type of the output port.
        //output.setMultiport(true);
        output.setTypeEquals(new ArrayType(BaseType.OBJECT));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Output the data read in the prefire.
     *  @exception IllegalActionException If there's no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();

        Token[] array = new Token[graphicsDevices.length];

        for (int i = 0; i < graphicsDevices.length; i++) {
            array[i] = new ObjectToken(graphicsDevices[i]);
        }

        output.send(0, new ArrayToken(array));
    }

    /** Get the graphics devices.
     */
    public void initialize() throws IllegalActionException {
        GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment
                    .getLocalGraphicsEnvironment();
        graphicsDevices = graphicsEnvironment.getScreenDevices();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    // Graphics devices that we can write to.
    private GraphicsDevice[] graphicsDevices;
}
