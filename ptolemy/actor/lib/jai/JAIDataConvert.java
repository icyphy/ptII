/* An actor that changes the data format in a JAIImageToken.

 Copyright (c) 2002-2003 The Regents of the University of California.
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

@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.jai;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

import java.awt.image.DataBuffer;
import java.awt.image.renderable.ParameterBlock;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

// NOTE: If you update the list of types, then you will want
// to update the list in actor/lib/jai/jai.xml.

//////////////////////////////////////////////////////////////////////////
//// JAIDataConvert
/**
   An actor that converts the data in an image to a new type.  This is
   commonly used when other actors, for instance the DCT, do not preserve
   the data type of the input.
   <p>
   The <i>dataFormat</i> attribute determines what type the data is being
   cast to.  The available options are byte, double, float, int, short,
   and unsigned short.

   @author James Yeh
   @version $Id$
   @since Ptolemy II 3.0
*/

public class JAIDataConvert extends Transformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public JAIDataConvert(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        dataFormat = new StringAttribute(this, "dataFormat");
        dataFormat.setExpression("byte");
        _dataFormat = _BYTE;

        input.setTypeEquals(BaseType.OBJECT);
        output.setTypeEquals(BaseType.OBJECT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The type to cast the data to.  This is a string valued
     *  attribute that defaults to "byte".
     */
    public StringAttribute dataFormat;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to determine which data type is being
     *  specified.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the function is not recognized.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == dataFormat) {
            String dataFormatName = dataFormat.getExpression();
            if (dataFormatName.equals("byte")) {
                _dataFormat = _BYTE;
            } else if (dataFormatName.equals("double")) {
                _dataFormat = _DOUBLE;
            } else if (dataFormatName.equals("float")) {
                _dataFormat = _FLOAT;
            } else if (dataFormatName.equals("int")) {
                _dataFormat = _INT;
            } else if (dataFormatName.equals("short")) {
                _dataFormat = _SHORT;
            } else if (dataFormatName.equals("ushort")) {
                _dataFormat = _USHORT;
            } else {
                throw new IllegalActionException(this,
                        "Unrecognized data type: " + dataFormatName);
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Output the JAIImageToken containing the new converted data.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        JAIImageToken jaiImageToken = (JAIImageToken) input.get(0);
        RenderedOp oldImage = jaiImageToken.getValue();
        ParameterBlock parameters = new ParameterBlock();
        parameters.addSource(oldImage);
        parameters.add(_getDataType());
        RenderedOp newImage = JAI.create("format", parameters);
        output.send(0, new JAIImageToken(newImage));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Determine which datatype is needed.
     *  @return The integer that specifies which type to cast to.
     */
    private int _getDataType() {
        int result;
        switch(_dataFormat) {
        case _BYTE:
            result = DataBuffer.TYPE_BYTE;
            break;
        case _DOUBLE:
            result = DataBuffer.TYPE_DOUBLE;
            break;
        case _FLOAT:
            result = DataBuffer.TYPE_FLOAT;
            break;
        case _INT:
            result = DataBuffer.TYPE_INT;
            break;
        case _SHORT:
            result = DataBuffer.TYPE_SHORT;
            break;
        case _USHORT:
            result = DataBuffer.TYPE_USHORT;
            break;
        default:
            throw new InternalErrorException(this,
                    "Invalid value for _dataFormat private variable. "
                    + "JAIDataConvert actor (" + getFullName()
                    + ") on data type " + _dataFormat);
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // An indicator for the data type to convert to.
    private int _dataFormat;

    // Constants used for more efficient execution.
    private static final int _BYTE = 0;
    private static final int _DOUBLE = 1;
    private static final int _FLOAT = 2;
    private static final int _INT = 3;
    private static final int _SHORT = 4;
    private static final int _USHORT = 5;
}
