/* An actor that read an ArrayToken of ObjectTokens where each
   ObjectToken is a java.awt.GraphicsDevice and outputs information
   about each GraphicsDevice.

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

import ptolemy.actor.lib.Transformer;
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


//////////////////////////////////////////////////////////////////////////
//// GraphicsDevicesToString

/**
   This actor that read an ArrayToken of ObjectTokens where each
   ObjectToken is a java.awt.GraphicsDevice and outputs information
   about each GraphicsDevice.

   @author  Christopher Hylands
   @version $Id$
*/
public class GraphicsDevicesToString extends Transformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public GraphicsDevicesToString(CompositeEntity container, String name)
        throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input.setTypeEquals(new ArrayType(BaseType.OBJECT));
        output.setTypeEquals(BaseType.STRING);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read an ArrayToken containing ObjectTokens where each
     *  ObjectToken is a java.awt.GraphicsDevice from each channel and
     *  write information about each GraphicsDevice to the output port
     *  as a StringToken.
     *
     *  @exception IllegalActionException If there is no director.  */
    public void fire() throws IllegalActionException {
        int width = input.getWidth();
        StringBuffer description = new StringBuffer();

        for (int i = 0; i < width; i++) {
            if (input.hasToken(i)) {
                Token[] inputArray = ((ArrayToken) input.get(0)).arrayValue();

                System.out.println("GraphicsDevicesToString.fire(): "
                    + "width: " + width + " token length:" + inputArray.length);

                for (int graphicsDeviceCount = 0;
                            graphicsDeviceCount < inputArray.length;
                            graphicsDeviceCount++) {
                    GraphicsDevice graphicsDevice = (GraphicsDevice) (((ObjectToken) inputArray[graphicsDeviceCount])
                                .getValue());

                    String graphicsDeviceType = "UNKNOWN";

                    switch (graphicsDevice.getType()) {
                    case GraphicsDevice.TYPE_RASTER_SCREEN:
                        graphicsDeviceType = "TYPE_RASTER_SCREEN";
                        break;

                    case GraphicsDevice.TYPE_PRINTER:
                        graphicsDeviceType = "TYPE_PRINTER";
                        break;

                    case GraphicsDevice.TYPE_IMAGE_BUFFER:
                        graphicsDeviceType = "TYPE_IMAGE_BUFFER";
                        break;
                    }

                    description.append("GraphicsDevice: " + graphicsDeviceCount
                        + " " + graphicsDeviceType + "\n");
                }
            }
        }

        Token out = new StringToken(description.toString());
        output.broadcast(out);
    }
}
