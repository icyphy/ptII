/* An actor that copyies bands from an image into another.

 @Copyright (c) 2002-2014 The Regents of the University of California.
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

 */
package ptolemy.actor.lib.jai;

import java.awt.image.renderable.ParameterBlock;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.ArrayToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// JAIBandSelect

/**
 Copy bands of an image to another image.  The number of bands to copy
 is indicated by the length of the array.  The band to copy is indicated
 by each value of the array.  For instance, if the user wants to
 duplicate each band of a three band image, the value of the array
 parameter should be {0, 0, 1, 1, 2, 2}.

 @author James Yeh
 @version $Id$
 @since Ptolemy II 3.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class JAIBandSelect extends Transformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public JAIBandSelect(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // An initial array that simply copies a three banded image. */
        IntToken[] initialArray = { new IntToken(0), new IntToken(1),
                new IntToken(2) };

        bandIndices = new Parameter(this, "bandIndices", new ArrayToken(
                BaseType.INT, initialArray));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The bands to copy to the output.
     */
    public Parameter bandIndices;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class and set the array of indices.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If a contained method throws it.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == bandIndices) {
            Token[] data = ((ArrayToken) bandIndices.getToken()).arrayValue();
            _indiceArray = new int[data.length];

            for (int i = 0; i < data.length; i++) {
                _indiceArray[i] = ((IntToken) data[i]).intValue();
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new attribute
     *  @return A new director.
     *  @exception CloneNotSupportedException If a derived class contains
     *  an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        JAIBandSelect newObject = (JAIBandSelect) super.clone(workspace);
        newObject._indiceArray = null;
        return newObject;
    }

    /** Fire this actor.
     *  @exception IllegalActionException If a contained method throws it,
     *   or if a token is received that contains a null image.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        ParameterBlock parameters = new ParameterBlock();
        JAIImageToken jaiImageToken = (JAIImageToken) input.get(0);
        RenderedOp oldImage = jaiImageToken.getValue();

        parameters.addSource(oldImage);
        parameters.add(_indiceArray);

        RenderedOp newImage = JAI.create("bandSelect", parameters);
        output.send(0, new JAIImageToken(newImage));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The value of the indice array */
    private int[] _indiceArray;
}
