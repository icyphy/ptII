/*
Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 2015 The Regents of the University of California.
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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.hlacerti.lib;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import certi.communication.MessageBuffer;
import hla.rti.jlc.EncodingHelpers;
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

/**
 *
 * @author david
 */
public class MessageProcessing {
        /** This generic method should call the {@link EncodingHelpers} API provided
     *  by CERTI to handle type decoding operations for HLA value attribute that
     *  has been reflected.
     *  @param hs The targeted HLA subcriber actor.
     *  @param type The type to decode the token.
     *  @param buffer The encoded value to decode.
     *  @return The decoded value as an object.
     *  @exception IllegalActionException If the token is not handled or the
     *  decoding has failed.
     */
    static public Object decodeHlaValue(HlaSubscriber hs, Type type, byte[] buffer)
            throws IllegalActionException {

        // Case to handle CERTI message buffer.
        if (hs.useCertiMessageBuffer()) {
            ByteArrayInputStream bis = new ByteArrayInputStream(buffer);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            MessageBuffer msgBuffer = new MessageBuffer(bis, bos);
            try {
                msgBuffer.receiveData();
            } catch (Exception e) {
                throw new IllegalActionException(
                        "Error to store CERTI message buffer from"
                                + " ByteArrayInputStream, reason: "
                                + e.getMessage());
            }

            if (type.equals(BaseType.BOOLEAN)) {
                return msgBuffer.readBoolean();
            } else if (type.equals(BaseType.UNSIGNED_BYTE)) {
                return msgBuffer.readByte();
            } else if (type.equals(BaseType.DOUBLE)) {
                return msgBuffer.readDouble();
            } else if (type.equals(BaseType.FLOAT)) {
                return msgBuffer.readFloat();
            } else if (type.equals(BaseType.INT)) {
                return msgBuffer.readInt();
            } else if (type.equals(BaseType.LONG)) {
                return msgBuffer.readLong();
            } else if (type.equals(BaseType.SHORT)) {
                return msgBuffer.readShort();
            } else if (type.equals(BaseType.STRING)) {
                return msgBuffer.readString();
            } else {
                throw new IllegalActionException(
                        "The current type received by the HLA/CERTI Federation"
                                + " , through a CERTI message buffer,"
                                + " is not handled  " ) ;
            }
        }

        // Case to handle "normal" events.
        if (type.equals(BaseType.BOOLEAN)) {
            return EncodingHelpers.decodeBoolean(buffer);
        } else if (type.equals(BaseType.UNSIGNED_BYTE)) {
            return EncodingHelpers.decodeByte(buffer);
        } else if (type.equals(BaseType.DOUBLE)) {
            return EncodingHelpers.decodeDouble(buffer);
        } else if (type.equals(BaseType.FLOAT)) {
            return EncodingHelpers.decodeFloat(buffer);
        } else if (type.equals(BaseType.INT)) {
            return EncodingHelpers.decodeInt(buffer);
        } else if (type.equals(BaseType.LONG)) {
            return EncodingHelpers.decodeLong(buffer);
        } else if (type.equals(BaseType.SHORT)) {
            return EncodingHelpers.decodeShort(buffer);
        } else if (type.equals(BaseType.STRING)) {
            return EncodingHelpers.decodeString(buffer);
        } else {
            throw new IllegalActionException(
                    "The current type received by the HLA/CERTI Federation"
                            + " is not handled ");
        }
    }

    /** This generic method call the {@link EncodingHelpers} API or the
     *  {@link MessageBuffer} API provided by CERTI to handle data encoding
     *  operation for updated value of HLA attribute that will be published
     *  to the federation.
     *  @param hp The HLA publisher actor which sends the HLA attribute value.
     *  @param tok The token to encode, i.e. the HLA attribute value to encode.
     *  @return The encoded value as an array of byte.
     *  @exception IllegalActionException If the token is not handled or the
     *  encoding has failed.
     */
    public static byte[]  encodeHlaValue(HlaPublisher hp, Token tok)
            throws IllegalActionException {
        byte[] encodedValue = null;
        Token t = null;
        double recordTimestamp = -1.0;
        int recordMicrostep = -1;
        double sourceTimestamp = -1.0;

        t = tok;

        // Get the corresponding type of the HLA attribute value.
        BaseType type = (BaseType) t.getType();

        // Case to handle CERTI message buffer.
        if (hp.useCertiMessageBuffer()) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            MessageBuffer msgBuffer = new MessageBuffer(null, bos);

            if (type.equals(BaseType.BOOLEAN)) {
                msgBuffer.write(((BooleanToken) t).booleanValue());
            } else if (type.equals(BaseType.UNSIGNED_BYTE)) {
                msgBuffer.write(((UnsignedByteToken) t).byteValue());
            } else if (type.equals(BaseType.DOUBLE)) {
                msgBuffer.write(((DoubleToken) t).doubleValue());
            } else if (type.equals(BaseType.FLOAT)) {
                msgBuffer.write(((FloatToken) t).floatValue());
            } else if (type.equals(BaseType.INT)) {
                msgBuffer.write(((IntToken) t).intValue());
            } else if (type.equals(BaseType.LONG)) {
                msgBuffer.write(((LongToken) t).longValue());
            } else if (type.equals(BaseType.SHORT)) {
                msgBuffer.write(((ShortToken) t).shortValue());
            } else if (type.equals(BaseType.STRING)) {
                msgBuffer.write(((StringToken) t).stringValue());
            } else {
                throw new IllegalActionException(
                        "The current type of the token " + t
                                + " is not handled");
            }

            try {
            // Write the buffer in the output stream.
            msgBuffer.send();
            } catch (IOException e) {
                throw new IllegalActionException(
                        "Error to write CERTI message buffer"
                                + " in ByteArrayOutputStream, reason: "
                                + e.getMessage());
            }

            // Write the output stream in an array of bytes.
            encodedValue = bos.toByteArray();

            // If we deal with CERTI MessageBuffer then we only need to return
            // the array of bytes.
            return encodedValue;
        }

        // Case to handle "normal" event payload.
        if (type.equals(BaseType.BOOLEAN)) {
            encodedValue = EncodingHelpers.encodeBoolean(((BooleanToken) t)
                    .booleanValue());
        } else if (type.equals(BaseType.UNSIGNED_BYTE)) {
            encodedValue = EncodingHelpers.encodeByte(((UnsignedByteToken) t)
                    .byteValue());
        } else if (type.equals(BaseType.DOUBLE)) {
            encodedValue = EncodingHelpers.encodeDouble(((DoubleToken) t)
                    .doubleValue());
        } else if (type.equals(BaseType.FLOAT)) {
            encodedValue = EncodingHelpers.encodeFloat(((FloatToken) t)
                    .floatValue());
        } else if (type.equals(BaseType.INT)) {
            encodedValue = EncodingHelpers.encodeInt(((IntToken) t).intValue());
        } else if (type.equals(BaseType.LONG)) {
            encodedValue = EncodingHelpers.encodeLong(((LongToken) t)
                    .longValue());
        } else if (type.equals(BaseType.SHORT)) {
            encodedValue = EncodingHelpers.encodeShort(((ShortToken) t)
                    .shortValue());
        } else if (type.equals(BaseType.STRING)) {
            encodedValue = EncodingHelpers.encodeString(((StringToken) t)
                    .stringValue());
        } else {
            throw new IllegalActionException(
                    "The current type of the token " + t
                            + " is not handled  " );
        }

            // Here we are sure that we don't deal with HLA  event CERTI MessageBuffer,
            // so just return the encoded value as array
            // of bytes.
            return encodedValue;
    }
}
