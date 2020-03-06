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
import com.jogamp.common.util.ArrayHashSet;
import hla.rti.jlc.EncodingHelpers;
import hla.rti1516e.encoding.*;
import hla.rti1516e.jlc.*;
import hla.rti1516e.jlc.EncoderFactory;
import jxl.read.biff.Record;
import ptolemy.data.*;
import ptolemy.data.type.*;
import ptolemy.kernel.util.IllegalActionException;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

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

//        throw new IllegalActionException(
//                "The current baseType, "+ type.toString()+ ",  received by the HLA/CERTI Federation"
//                        + " is not handled ");

        ByteWrapper byteWrapper = new ByteWrapper(buffer);

        if(type instanceof  BaseType) {
            if (type.equals(BaseType.BOOLEAN)) {
                HLAboolean hlaBoolean = new HLAbooleanImpl();
                hlaBoolean.decode(byteWrapper);
                return hlaBoolean.getValue();
            } else if (type.equals(BaseType.UNSIGNED_BYTE)) {
                HLAbyte hlaByte = new BasicHLAbyteImpl();
                hlaByte.decode(byteWrapper);
                return hlaByte.getValue();
            } else if (type.equals(BaseType.DOUBLE)) {
                HLAfloat64BE hlaDouble = new BasicHLAfloat64BEImpl();
                hlaDouble.decode(byteWrapper);
                return hlaDouble.getValue();
            } else if (type.equals(BaseType.FLOAT)) {
                HLAfloat32BE hlaFloat = new BasicHLAfloat32BEImpl();
                hlaFloat.decode(byteWrapper);
                return hlaFloat.getValue();
            } else if (type.equals(BaseType.INT)) {
                HLAinteger32BE hlaInteger = new BasicHLAinteger32BEImpl();
                hlaInteger.decode(byteWrapper);
                return hlaInteger.getValue();
            } else if (type.equals(BaseType.LONG)) {
                HLAinteger64BE hlaLong = new BasicHLAinteger64BEImpl();
                hlaLong.decode(byteWrapper);
                return hlaLong.getValue();
            } else if (type.equals(BaseType.SHORT)) {
                HLAinteger16BE hlaShort = new BasicHLAinteger16BEImpl();
                hlaShort.decode(byteWrapper);
                return hlaShort.getValue();
            } else if (type.equals(BaseType.STRING)) {
                HLAASCIIstring hlaString = new HLAASCIIstringImpl();
                hlaString.decode(byteWrapper);
                return hlaString.getValue();
            } else {
                throw new IllegalActionException(
                        "The current baseType, "+ type.toString()+ ",  received by the HLA/CERTI Federation"
                                + " is not handled ");
            }
        }
        else if(type instanceof ArrayType){
            String typeArray = "";
            int length;
            try {
                length = ((ArrayType) type).length();
                typeArray = "FixedArray";
            } catch (Exception e) {
                length = 0;
                typeArray = "VariableArray";
            }

//            System.out.println("/*/*/*/*/ Type de l'array à décoder : " +  type.toString()+ " /*/*//*");


            Type elmtType = ((ArrayType) type).getElementType();
            List<Token> elementsTokens = new ArrayList<>();

            if(typeArray == "VariableArray") {
                if (elmtType.equals(BaseType.BOOLEAN)) {
                    //Create a new VariableArray with the right factory
                    DataElementFactory<HLAboolean> factory = index -> EncoderFactory.getInstance().createHLAboolean();
                    HLAvariableArray hlaVariableArray = new HLAvariableArrayImpl(factory, 0);
                    //Decode the buffer and put values in hlaVariableArray
                    hlaVariableArray.decode(byteWrapper);
                    //Transform hlaVariableArray into an ArrayToken (format used next)
                    for (int i = 0; i < hlaVariableArray.size(); i++) {
                        boolean value = ((HLAboolean) hlaVariableArray.get(i)).getValue();
                        elementsTokens.add(new BooleanToken(value));
                    }

                }
                else if (elmtType.equals(BaseType.UNSIGNED_BYTE)) {
                    //Create a new VariableArray with the right factory
                    DataElementFactory<HLAbyte> factory = index -> EncoderFactory.getInstance().createHLAbyte();
                    HLAvariableArray hlaVariableArray = new HLAvariableArrayImpl(factory, 0);
                    //Decode the buffer and put values in hlaVariableArray
                    hlaVariableArray.decode(byteWrapper);
                    //Transform hlaVariableArray into an ArrayToken (format used next)
                    for (int i = 0; i < hlaVariableArray.size(); i++) {
                        byte value = ((HLAbyte) hlaVariableArray.get(i)).getValue();
                        elementsTokens.add(new UnsignedByteToken(value));
                    }

                }
                else if (elmtType.equals(BaseType.DOUBLE)) {
                    //Create a new VariableArray with the right factory
                    DataElementFactory<HLAfloat64BE> factory = index -> EncoderFactory.getInstance().createHLAfloat64BE();
                    HLAvariableArray hlaVariableArray = new HLAvariableArrayImpl(factory, 0);
                    //Decode the buffer and put values in hlaVariableArray
                    hlaVariableArray.decode(byteWrapper);
                    //Transform hlaVariableArray into an ArrayToken (format used next)
                    for (int i = 0; i < hlaVariableArray.size(); i++) {
                        double value = ((HLAfloat64BE) hlaVariableArray.get(i)).getValue();
                        elementsTokens.add(new DoubleToken(value));
                    }
                }
                else if (elmtType.equals(BaseType.FLOAT)) {
                    //Create a new VariableArray with the right factory
                    DataElementFactory<HLAfloat32BE> factory = index -> EncoderFactory.getInstance().createHLAfloat32BE();
                    HLAvariableArray hlaVariableArray = new HLAvariableArrayImpl(factory, 0);
                    //Decode the buffer and put values in hlaVariableArray
                    hlaVariableArray.decode(byteWrapper);
                    //Transform hlaVariableArray into an ArrayToken (format used next)
                    for (int i = 0; i < hlaVariableArray.size(); i++) {
                        float value = ((HLAfloat32BE) hlaVariableArray.get(i)).getValue();
                        elementsTokens.add(new FloatToken(value));
                    }
                }
                else if (elmtType.equals(BaseType.INT)) {
                    //Create a new VariableArray with the right factory
                    DataElementFactory<HLAinteger32BE> factory = index -> EncoderFactory.getInstance().createHLAinteger32BE();
                    HLAvariableArray hlaVariableArray = new HLAvariableArrayImpl(factory, 0);
                    //Decode the buffer and put values in hlaVariableArray
                    hlaVariableArray.decode(byteWrapper);
                    //Transform hlaVariableArray into an ArrayToken (format used next)
                    for (int i = 0; i < hlaVariableArray.size(); i++) {
                        int value = ((HLAinteger32BE) hlaVariableArray.get(i)).getValue();
                        elementsTokens.add(new IntToken(value));
                    }
                }
                else if (elmtType.equals(BaseType.LONG)) {
                    //Create a new VariableArray with the right factory
                    DataElementFactory<HLAinteger64BE> factory = index -> EncoderFactory.getInstance().createHLAinteger64BE();
                    HLAvariableArray hlaVariableArray = new HLAvariableArrayImpl(factory, 0);
                    //Decode the buffer and put values in hlaVariableArray
                    hlaVariableArray.decode(byteWrapper);
                    //Transform hlaVariableArray into an ArrayToken (format used next)
                    for (int i = 0; i < hlaVariableArray.size(); i++) {
                        long value = ((HLAinteger64BE) hlaVariableArray.get(i)).getValue();
                        elementsTokens.add(new LongToken(value));
                    }

                }
                else if (elmtType.equals(BaseType.SHORT)) {
                    //Create a new VariableArray with the right factory
                    DataElementFactory<HLAinteger16BE> factory = index -> EncoderFactory.getInstance().createHLAinteger16BE();
                    //Decode the buffer and put values in hlaVariableArray
                    HLAvariableArray hlaVariableArray = new HLAvariableArrayImpl(factory, 0);
                    hlaVariableArray.decode(byteWrapper);
                    //Transform hlaVariableArray into an ArrayToken (format used next)
                    for (int i = 0; i < hlaVariableArray.size(); i++) {
                        long value = ((HLAinteger64BE) hlaVariableArray.get(i)).getValue();
                        elementsTokens.add(new LongToken(value));
                    }

                }
                else if (elmtType.equals(BaseType.STRING)) {
                    //Create a new VariableArray with the right factory
                    DataElementFactory<HLAASCIIstring> factory = index -> EncoderFactory.getInstance().createHLAASCIIstring();
                    HLAvariableArray hlaVariableArray = new HLAvariableArrayImpl(factory, 0);
                    //Decode the buffer and put values in hlaVariableArray
                    hlaVariableArray.decode(byteWrapper);
                    //Transform hlaVariableArray into an ArrayToken (format used next)
                    for (int i = 0; i < hlaVariableArray.size(); i++) {
                        String value = ((HLAASCIIstring) hlaVariableArray.get(i)).getValue();
                        elementsTokens.add(new StringToken(value));
                    }
                }
                else {
                    throw new IllegalActionException("The current array type of the token, "
                            + type.toString() + ", is not handled  ");
                }
            } else if(typeArray == "FixedArray") {
                System.out.println(typeArray + "<" + elmtType.toString() + ", " + length + ">");

                HLAfixedArrayImpl fixedArray = new HLAfixedArrayImpl(length);
                if (elmtType.equals(BaseType.BOOLEAN)) {
                    //Create FixedArray structure (create all elements)
                    for(int i = 0; i < length; i++)
                        fixedArray.addElement(new HLAbooleanImpl());
                    //Decode the buffer and put values in the fixedArray
                    fixedArray.decode(byteWrapper);
                    //Transform fixedArray into an ArrayToken (format used next)
                     for (int i = 0; i < fixedArray.size(); i++) {
                         boolean value = ((HLAboolean) fixedArray.get(i)).getValue();
                         elementsTokens.add(new BooleanToken(value));
                     }
                }
                else if (elmtType.equals(BaseType.UNSIGNED_BYTE)) {
                    //Create FixedArray structure (create all elements)
                    for(int i = 0; i < length; i++)
                        fixedArray.addElement(new BasicHLAbyteImpl());
                    //Decode the buffer and put values in the fixedArray
                    fixedArray.decode(byteWrapper);
                    //Transform fixedArray into an ArrayToken (format used next)
                    for (int i = 0; i < fixedArray.size(); i++) {
                        byte value = ((HLAbyte) fixedArray.get(i)).getValue();
                        elementsTokens.add(new UnsignedByteToken(value));
                    }
                }
                else if (elmtType.equals(BaseType.DOUBLE)) {
                    //Create FixedArray structure (create all elements)
                    for(int i = 0; i < length; i++)
                        fixedArray.addElement(new BasicHLAfloat64BEImpl());
                    //Decode the buffer and put values in the fixedArray
                    fixedArray.decode(byteWrapper);
                    //Transform fixedArray into an ArrayToken (format used next)
                    for (int i = 0; i < fixedArray.size(); i++) {
                        double value = ((HLAfloat64BE) fixedArray.get(i)).getValue();
                        elementsTokens.add(new DoubleToken(value));
                    }
                }
                else if (elmtType.equals(BaseType.FLOAT)) {
                    //Create FixedArray structure (create all elements)
                    for(int i = 0; i < length; i++)
                        fixedArray.addElement(new BasicHLAfloat32BEImpl());
                    //Decode the buffer and put values in the fixedArray
                    fixedArray.decode(byteWrapper);
                    //Transform fixedArray into an ArrayToken (format used next)
                    for (int i = 0; i < fixedArray.size(); i++) {
                        float value = ((HLAfloat32BE) fixedArray.get(i)).getValue();
                        elementsTokens.add(new FloatToken(value));
                    }
                }
                else if (elmtType.equals(BaseType.INT)) {
                    //Create FixedArray structure (create all elements)
                    for(int i = 0; i < length; i++)
                        fixedArray.addElement(new BasicHLAinteger32BEImpl());
                    //Decode the buffer and put values in the fixedArray
                    fixedArray.decode(byteWrapper);
                    //Transform fixedArray into an ArrayToken (format used next)
                    for (int i = 0; i < fixedArray.size(); i++) {
                        int value = ((HLAinteger32BE) fixedArray.get(i)).getValue();
                        elementsTokens.add(new IntToken(value));
                    }
                }
                else if (elmtType.equals(BaseType.LONG)) {
                    //Create FixedArray structure (create all elements)
                    for(int i = 0; i < length; i++)
                        fixedArray.addElement(new BasicHLAinteger64BEImpl());
                    //Decode the buffer and put values in the fixedArray
                    fixedArray.decode(byteWrapper);
                    //Transform fixedArray into an ArrayToken (format used next)
                    for (int i = 0; i < fixedArray.size(); i++) {
                        long value = ((HLAinteger64BE) fixedArray.get(i)).getValue();
                        elementsTokens.add(new LongToken(value));
                    }
                }
                else if (elmtType.equals(BaseType.SHORT)) {
                    //Create FixedArray structure (create all elements)
                    for(int i = 0; i < length; i++)
                        fixedArray.addElement(new BasicHLAinteger16BEImpl());
                    //Decode the buffer and put values in the fixedArray
                    fixedArray.decode(byteWrapper);
                    //Transform fixedArray into an ArrayToken (format used next)
                    for (int i = 0; i < fixedArray.size(); i++) {
                        short value = ((HLAinteger16BE) fixedArray.get(i)).getValue();
                        elementsTokens.add(new ShortToken(value));
                    }
                }
                else if (elmtType.equals(BaseType.STRING)) {
                    //Create FixedArray structure (create all elements)
                    for(int i = 0; i < length; i++)
                        fixedArray.addElement(new HLAASCIIstringImpl());
                    //Decode the buffer and put values in the fixedArray
                    fixedArray.decode(byteWrapper);
                    //Transform fixedArray into an ArrayToken (format used next)
                    for (int i = 0; i < fixedArray.size(); i++) {
                        String value = ((HLAASCIIstring) fixedArray.get(i)).getValue();
                        elementsTokens.add(new StringToken(value));
                    }
                }
                else {
                    throw new IllegalActionException("The current array type of the token, "
                            + type.toString() + ", is not handled  ");
                }
            }
            //Convert list of tokens in array (need an Token[] to ArrayToken constructor)
            Token[] array = new Token[elementsTokens.size()];
            for(int i = 0; i < elementsTokens.size(); i++) {
                array[i] = elementsTokens.get(i);
                System.out.println(array[i].toString());
            }
            //Create and return a new ArrayToken
            return new ArrayToken(array, array.length);
        }

        else if(type instanceof RecordType){
            throw new IllegalActionException(
                    "The current RECORD type, " + type.toString() + ", received by the HLA/CERTI Federation"
                            + " is not handled ");
        }
        else {
            throw new IllegalActionException(
                    "The current type, " + type.toString() + ", received by the HLA/CERTI Federation"
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
        ByteWrapper byteWrapper;

        // Get the corresponding type of the HLA attribute value.
        if(t.getType() instanceof  BaseType) {
            BaseType type = (BaseType) t.getType();
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
                HLAinteger32BE hlaInt = new BasicHLAinteger32BEImpl(((IntToken) t).intValue());
                byteWrapper = new ByteWrapper(hlaInt.getEncodedLength());
                hlaInt.encode(byteWrapper);
            } else if (type.equals(BaseType.LONG)) {
                HLAinteger64BE hlaLong = new BasicHLAinteger64BEImpl(((LongToken) t).longValue());
                byteWrapper = new ByteWrapper(hlaLong.getEncodedLength());
                hlaLong.encode(byteWrapper);
            } else if (type.equals(BaseType.SHORT)) {
                HLAinteger16BE hlaShort = new BasicHLAinteger16BEImpl(((ShortToken) t).shortValue());
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
        }
        else if (t.getType() instanceof ArrayType){
            System.out.println("/*/*/*/*/ Type de l'array : " +  t.getType().toString()+ " /*/*//*");

            ArrayToken arrayToken = (ArrayToken) t;
            Type elmtType = arrayToken.getElementType();
            HLAvariableArrayImpl hlaVariableArray = new HLAvariableArrayImpl();

            if (elmtType.equals(BaseType.BOOLEAN)) {
                for(int i = 0; i < arrayToken.length(); i++){
                    boolean value = ((BooleanToken) arrayToken.getElement(i)).booleanValue();
                    hlaVariableArray.addElement(new HLAbooleanImpl(value));
                }
            }
            else if (elmtType.equals(BaseType.UNSIGNED_BYTE)) {
                for(int i = 0; i < arrayToken.length(); i++){
                    byte value = ((UnsignedByteToken) arrayToken.getElement(i)).byteValue();
                    hlaVariableArray.addElement(new BasicHLAbyteImpl(value));
                }
            } else if (elmtType.equals(BaseType.DOUBLE)) {
                for(int i = 0; i < arrayToken.length(); i++){
                    double value = ((DoubleToken) arrayToken.getElement(i)).doubleValue();
                    hlaVariableArray.addElement(new BasicHLAfloat64BEImpl(value));
                }
            } else if (elmtType.equals(BaseType.FLOAT)) {
                for(int i = 0; i < arrayToken.length(); i++){
                    float value = ((FloatToken) arrayToken.getElement(i)).floatValue();
                    hlaVariableArray.addElement(new BasicHLAfloat32BEImpl(value));
                }
            } else if (elmtType.equals(BaseType.INT)) {
                for(int i = 0; i < arrayToken.length(); i++){
                    int value = ((IntToken) arrayToken.getElement(i)).intValue();
                    hlaVariableArray.addElement(new BasicHLAinteger32BEImpl(value));
                }
            } else if (elmtType.equals(BaseType.LONG)) {
                for(int i = 0; i < arrayToken.length(); i++){
                    long value = ((LongToken) arrayToken.getElement(i)).longValue();
                    hlaVariableArray.addElement(new BasicHLAinteger64BEImpl(value));
                }
            } else if (elmtType.equals(BaseType.SHORT)) {
                for(int i = 0; i < arrayToken.length(); i++){
                    short value = ((ShortToken) arrayToken.getElement(i)).shortValue();
                    hlaVariableArray.addElement(new BasicHLAinteger16BEImpl(value));
                }
            } else if (elmtType.equals(BaseType.STRING)) {
                for(int i = 0; i < arrayToken.length(); i++){
                    String value = ((StringToken) arrayToken.getElement(i)).stringValue();
                    hlaVariableArray.addElement(new HLAASCIIstringImpl(value));
                }
            } else {
                throw new IllegalActionException(
                        "The current array type of the token " + t + ", " + t.getType() +" is not handled  ");
            }
            byteWrapper = new ByteWrapper(hlaVariableArray.getEncodedLength());
            hlaVariableArray.encode(byteWrapper);
        }
        else if(t.getType() instanceof  RecordType) {
            throw new IllegalActionException(
                    "The RecordType of the token " + t + ", " + t.getType() +" is not handled  ");
        }
        else {
            throw new IllegalActionException(
                    "The current type of the token " + t + ", " + t.getType() +" is not handled  ");
        }

        // Here we are sure that we don't deal with HLA  event CERTI MessageBuffer,
        // so just return the encoded value as array
        // of bytes.
        encodedValue = byteWrapper.array();
        return encodedValue;
    }
}
