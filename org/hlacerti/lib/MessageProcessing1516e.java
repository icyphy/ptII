/* Message encoding and decoding for HLA version 1516e.

Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 2015-2019 The Regents of the University of California.
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
package org.hlacerti.lib;

import certi.communication.MessageBuffer;
import hla.rti.jlc.EncodingHelpers;
import hla.rti1516e.encoding.*;
import hla.rti1516e.jlc.*;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.FloatToken;
import ptolemy.data.IntToken;
import ptolemy.data.LongToken;
import ptolemy.data.ShortToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.UnsignedByteToken;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;

/** This class implements the functionalities to encode/decode
 *  HLA values received or sent to an HLA RTI. This version is designed
 *  to work with version 1516-2010 of HLA, and unlike the MessageProcessing,
 *  does not support Certi-specific message buffers.
 *
 *  @author Janette Cardoso and Edward A. Lee
 *  @version $Id: MessageProcessing.java 214 2018-04-01 13:32:02Z j.cardoso $
 *  @since Ptolemy II 11.0
 *
 *  @Pt.ProposedRating Yellow (eal)
 *  @Pt.AcceptedRating Red (eal)
 */
public class MessageProcessing1516e {
    /** This generic method calls the {@link EncodingHelpers} API provided
     *  by CERTI to handle type decoding operations for HLA value attribute
     *  that has been reflected (RAV).
     *  @param type The type to decode the token.
     *  @param buffer The encoded value to decode.
     *  @return The decoded value as an object.
     *  @exception IllegalActionException If the token is not handled or the
     *  decoding has failed.
     */
    static public Object decodeHlaValue(Type type, byte[] buffer)
            throws IllegalActionException, DecoderException {

        ByteWrapper byteWrapper = new ByteWrapper(buffer);
        if (type.equals(BaseType.BOOLEAN)) {
            HLAboolean hlaBoolean = new HLAbooleanImpl();
            hlaBoolean.decode(byteWrapper);
            return hlaBoolean.getValue();
        }
        else if (type.equals(BaseType.UNSIGNED_BYTE)) {
            HLAbyte hlaByte = new BasicHLAbyteImpl();
            hlaByte.decode(byteWrapper);
            return hlaByte.getValue();
        }
        else if (type.equals(BaseType.DOUBLE)) {
            HLAfloat64BE hlaDouble = new BasicHLAfloat64BEImpl();
            hlaDouble.decode(byteWrapper);
            return hlaDouble.getValue();
        }
        else if (type.equals(BaseType.FLOAT)) {
            HLAfloat32BE hlaFloat = new BasicHLAfloat32BEImpl();
            hlaFloat.decode(byteWrapper);
            return hlaFloat.getValue();
        }
        else if (type.equals(BaseType.INT)) {
            HLAinteger32BE hlaInteger = new BasicHLAinteger32BEImpl();
            hlaInteger.decode(byteWrapper);
            return hlaInteger.getValue();
        }
        else if (type.equals(BaseType.LONG)) {
            HLAinteger64BE hlaLong = new BasicHLAinteger64BEImpl();
            hlaLong.decode(byteWrapper);
            return hlaLong.getValue();
        }
        else if (type.equals(BaseType.SHORT)) {
            HLAinteger16BE hlaShort =  new BasicHLAinteger16BEImpl();
            hlaShort.decode(byteWrapper);
            return hlaShort.getValue();
        }
        else if (type.equals(BaseType.STRING)) {
            HLAASCIIstring hlaString = new HLAASCIIstringImpl();
            hlaString.decode(byteWrapper);
            return hlaString.getValue();
        }
        else {
            throw new IllegalActionException(
                    "The current type received by the HLA/CERTI Federation"
                            + " is not handled ");
        }
    }

    /** This generic method calls the {@link EncodingHelpers} API or the
     *  {@link MessageBuffer} API provided by CERTI to handle data encoding
     *  operation for updated value of HLA attribute that will be published
     *  to the federation.
     *  @param tok The token to encode, i.e. the HLA attribute value to encode.
     *  @return The encoded value as an array of byte.
     *  @exception IllegalActionException If the token is not handled or the
     *  encoding has failed.
     */
    public static byte[] encodeHlaValue(Token tok)
            throws IllegalActionException {
        byte[] encodedValue = null;
        Token t = tok;

        // Get the corresponding type of the HLA attribute value.
        BaseType type = (BaseType) t.getType();
        ByteWrapper byteWrapper;

        if (type.equals(BaseType.BOOLEAN)) {
            HLAboolean hlaBoolean = new HLAbooleanImpl(((BooleanToken) t).booleanValue());
            byteWrapper = new ByteWrapper(hlaBoolean.getEncodedLength());
            hlaBoolean.encode(byteWrapper);
        } else if (type.equals(BaseType.UNSIGNED_BYTE)) {
            HLAbyte hlaByte = new BasicHLAbyteImpl(((UnsignedByteToken) t).byteValue());
            byteWrapper = new ByteWrapper(hlaByte.getEncodedLength());
            hlaByte.encode(byteWrapper);
        } else if (type.equals(BaseType.DOUBLE)) {
            HLAfloat64BE hlaDouble = new BasicHLAfloat64BEImpl(((DoubleToken) t).doubleValue());
            byteWrapper = new ByteWrapper(hlaDouble.getEncodedLength());
            hlaDouble.encode(byteWrapper);
        } else if (type.equals(BaseType.FLOAT)) {
            HLAfloat32BE hlaFloat = new BasicHLAfloat32BEImpl(((FloatToken) t).floatValue());
            byteWrapper = new ByteWrapper(hlaFloat.getEncodedLength());
            hlaFloat.encode(byteWrapper);
        } else if (type.equals(BaseType.INT)) {
            HLAinteger32BE hlaInt= new BasicHLAinteger32BEImpl(((IntToken) t).intValue());
            byteWrapper = new ByteWrapper(hlaInt.getEncodedLength());
            hlaInt.encode(byteWrapper);
        } else if (type.equals(BaseType.LONG)) {
            HLAinteger64BE hlaLong= new BasicHLAinteger64BEImpl(((LongToken) t).longValue());
            byteWrapper = new ByteWrapper(hlaLong.getEncodedLength());
            hlaLong.encode(byteWrapper);
        } else if (type.equals(BaseType.SHORT)) {
            HLAinteger16BE hlaShort= new BasicHLAinteger16BEImpl(((ShortToken) t).shortValue());
            byteWrapper = new ByteWrapper(hlaShort.getEncodedLength());
            hlaShort.encode(byteWrapper);
        } else if (type.equals(BaseType.STRING)) {
            HLAASCIIstring hlaString = new HLAASCIIstringImpl(((StringToken) t).stringValue());
            byteWrapper = new ByteWrapper(hlaString.getEncodedLength());
            hlaString.encode(byteWrapper);
        } else {
            throw new IllegalActionException(
                    "The current type of the token " + t + " is not handled  ");
        }
        encodedValue = byteWrapper.array();

        // Here we are sure that we don't deal with HLA  event CERTI MessageBuffer,
        // so just return the encoded value as array
        // of bytes.
        return encodedValue;
    }
}
