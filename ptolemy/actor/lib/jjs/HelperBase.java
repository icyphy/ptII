/* Embedding of a Vert.x core.

   Copyright (c) 2014-2016 The Regents of the University of California.
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

 */
package ptolemy.actor.lib.jjs;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.math.BigInteger;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.TreeSet;

import javax.imageio.ImageIO;

import io.vertx.core.buffer.Buffer;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
//import jdk.nashorn.internal.objects.NativeArray;
import ptolemy.data.ImageToken;
import ptolemy.data.UnsignedByteToken;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;

///////////////////////////////////////////////////////////////////
//// HelperBase

/**
   A base class for helper classes. The main function of this class is to
   provide a reference to the JavaScript actor for which the helper is helping.
   This is available in a protected method so that it isn't directly available
   in JavaScript. This actor should be used for all synchronized actions
   (to avoid deadlocks and race conditions).

   @author Edward A. Lee, Hokeun Kim
   @version $Id$
   @since Ptolemy II 11.0
   @Pt.ProposedRating Yellow (eal)
   @Pt.AcceptedRating Red (bilung)
 */
public class HelperBase {

    /** Construct a helper for the specified actor and JavaScript object.
     *  @param actor The actor associated with this helper.
     *  @param helping The object that this is helping.
     */
    public HelperBase(Object actor, ScriptObjectMirror helping) {
        // Helping a JavaScript object.
        _currentObj = helping;

        if (actor instanceof RestrictedJavaScriptInterface) {
            _actor = ((RestrictedJavaScriptInterface) actor)
                    ._getActor();
        } else if (actor instanceof JavaScript) {
            _actor = ((JavaScript) actor);
        } else if (actor == null) {
            throw new InternalErrorException("No actor object.");
        } else {
            throw new InternalErrorException("Invalid actor object: "
                    + actor.toString());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the JavaScript object that this helper is helping.
     *  @return The helping object given in the constructor.
     */
    public ScriptObjectMirror getHelping() {
        return _currentObj;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     public fields                         ////

    /** Support data types for reading and writing to buffers. */
    public static enum DATA_TYPE {
        /** Bytes. */
        BYTE,
        /** Doubles. */
        DOUBLE,
        /** Floats. */
        FLOAT,
        /** Images. */
        IMAGE,
        /** Integers. */
        INT,
        /** JSON. */
        JSON,

        // Unsigned ints cannot be represented as a JavaScript number.
        // Interestingly, signed ints can be, because they are
        // losslessly convertible to double.  But neither longs nor
        // unsigned ints are losslessly convertible to double.

        // LONG,

        /** JavaScript numbers.. */
        NUMBER,
        /** Shorts. */
        SHORT,
        /** Strings. */
        STRING,
        /** Unsigned bytes. */
        UNSIGNEDBYTE,

        // UNSIGNEDINT,

        /** Unsigned shorts. */
        UNSIGNEDSHORT
    };

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Handle an error by emitting an error event, or if there is no
     *  error event handler registered, by invoking the error() method
     *  of the associated actor. Note that this may not stop execution.
     *  @param message The error message.
     */
    protected void _error(String message) {
        try {
            _currentObj.callMember("emit", "error", message);
            // NOTE: The error handler may not stop execution.
        } catch (Throwable ex) {
            // There may be no error event handler registered.
            // Use the actor to report the error.
            _actor.error(message);
        }
    }

    /** Handle an error by emitting an error event, or if there is no
     *  error event handler registered, by invoking the error() method
     *  of the associated actor. Note that this may not stop execution.
     *  @param message The error message.
     *  @param throwable The throwable.
     */
    protected void _error(String message, Throwable throwable) {
        try {
            _currentObj.callMember("emit", "error",
                    message + ": " + throwable );
            // NOTE: The error handler may not stop execution.
        } catch (Throwable ex) {
            // There may be no error event handler registered.
            // Use the actor to report the error.
            _actor.error(message, throwable);
        }
    }

    /** Emit the error message in the director thread by emitting
     *  an "error" event on the specified JavaScript object.
     *  @param emitter The JavaScript object that should emit an "error" event.
     *  @param message The error message.
     */
    protected void _error(ScriptObjectMirror emitter, final String message) {
        _issueResponse(new Runnable() {
            @Override
            public void run() {
                try {
                    // FIXME: This should include the name of the actor.
                    emitter.callMember("emit", "error", message);
                    // NOTE: The error handler may not stop execution.
                } catch (Throwable ex) {
                    // There may be no error event handler registered.
                    // Use the actor to report the error.
                    _actor.error(message, new IllegalActionException(_actor, ex, message));
                }
            }
        });
    }

    /** Emit the error message in the director thread by emitting
     *  an "error" event on the specified JavaScript object.
     *  @param emitter The JavaScript object that should emit an "error" event.
     *  @param message The error message.
     *  @param throwable The exception that caused the error.
     */
    protected void _error(ScriptObjectMirror emitter, final String message, final Throwable throwable) {
        _issueResponse(new Runnable() {
            @Override
            public void run() {
                try {
                    emitter.callMember("emit", "error", message);
                    // NOTE: The error handler may not stop execution.
                } catch (Throwable ex) {
                    // There may be no error event handler registered.
                    // Use the actor to report the error.
                    _actor.error(message, new IllegalActionException(_actor, throwable, message));
                }
            }
        });
    }

    /** Append a numeric instance of the specified type to a buffer.
     *  @param buffer The buffer to which to append
     *  @param data The data to be appended
     *  @param type The type of data.
     */
    protected void _appendNumericToBuffer(Buffer buffer, Object data, DATA_TYPE type) {
        if (data instanceof Number) {
            switch(type) {
            case BYTE:
                buffer.appendByte(((Number)data).byteValue());
                break;
            case DOUBLE:
            case NUMBER:
                buffer.appendDouble(((Number)data).doubleValue());
                break;
            case FLOAT:
                buffer.appendFloat(((Number)data).floatValue());
                break;
            case INT:
                buffer.appendInt(((Number)data).intValue());
                break;
            //case LONG:
            //    buffer.appendLong(((Number)data).longValue());
            //    break;
            case SHORT:
                buffer.appendShort(((Number)data).shortValue());
                break;
            case UNSIGNEDBYTE:
                // Number class can't extract an unsigned byte, so we use short.
                buffer.appendUnsignedByte(((Number)data).shortValue());
                break;
            //case UNSIGNEDINT:
                // Number class can't extract an unsigned int, so we use long.
            //    buffer.appendUnsignedInt(((Number)data).longValue());
            //    break;
            case UNSIGNEDSHORT:
                // Number class can't extract an unsigned short, so we use int.
                buffer.appendUnsignedShort(((Number)data).intValue());
                break;
            default:
                _error("Unsupported type for buffer: "
                        + type.toString());
            }
        // } else if (data instanceof LongToken) {
        //     // JavaScript has no long data type, and long is not convertible to
        //     // "number" (which is double), so the Ptolemy host will pass in a
        //     // LongToken.  Handle this specially.
        //     buffer.appendLong(((LongToken)data).longValue());
        } else {
            _toTypeError(type, data);
        }
    }

    /** Append data to be sent to the specified buffer.
     *  @param data The data to append.
     *  @param type The type of data append.
     *  @param imageType If the type is IMAGE, then then the image encoding to use, or
     *   null to use the default (JPG).
     *  @param buffer The buffer.
     */
    protected void _appendToBuffer(
            final Object data, DATA_TYPE type, String imageType, Buffer buffer) {
        if (data == null) {
            // Nothing to do.
            return;
        }
        if (type.equals(DATA_TYPE.STRING) || type.equals(DATA_TYPE.JSON)) {
            // NOTE: Use of toString() method makes this very tolerant, but
            // it won't properly stringify JSON. Is this OK?
            // NOTE: A second argument could take an encoding.
            // Defaults to UTF-8. Is this OK?
            buffer.appendString(data.toString());
        } else if (type.equals(DATA_TYPE.IMAGE)) {
            if (data instanceof ImageToken) {
                Image image = ((ImageToken)data).asAWTImage();
                if (image == null) {
                    _error("Empty image received: " + data);
                    return;
                }
                if (!(image instanceof BufferedImage)) {
                    _error("Unsupported image token type: " + image.getClass());
                    return;
                }
                if (imageType == null) {
                    // If only image is specified, use JPG.
                    imageType = "jpg";
                }
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                try {
                    ImageIO.write((BufferedImage)image, imageType, stream);
                } catch (IOException e) {
                    _error("Failed to convert image to byte array for sending: " + e.toString());
                }
                byte[] imageBytes = stream.toByteArray();
                buffer.appendBytes(imageBytes);
            } else {
                // Coverity Scan: data cannot be null here.
                _error("Expected an image to send, but instead got "
                        + data.getClass().getName());
            }
        } else {
            _appendNumericToBuffer(buffer, data, type);
        }
    }

    /** Extract a numeric instance of the specified type from a buffer.
     *  @param buffer The buffer containing the data.
     *  @param type The type to extract.
     *  @param position The position in the buffer from which to extract it.
     *  @return The numeric instance.
     */
    protected Object _extractFromBuffer(Buffer buffer, DATA_TYPE type, int position) {
        try {
            switch(type) {
            case BYTE:
                return buffer.getByte(position);
            case DOUBLE:
            case NUMBER:
                return buffer.getDouble(position);
            case FLOAT:
                return buffer.getFloat(position);
            case INT:
                return buffer.getInt(position);
            //case LONG:
                // Note that long is not representable in JavaScript.
                // Hence, we return a LongToken.
            //    long result = buffer.getLong(position);
            //    return new LongToken(result);
            case SHORT:
                return buffer.getShort(position);
            case UNSIGNEDBYTE:
                return buffer.getUnsignedByte(position);
            //case UNSIGNEDINT:
                //return buffer.getUnsignedInt(position);
            case UNSIGNEDSHORT:
                return buffer.getUnsignedShort(position);
            default:
                _error("Type has no fixed size: "
                        + type.toString());
                return null;
            }
        } catch (Throwable ex) {
            _fromTypeError(ex, type, buffer);
            return null;
        }
    }

    /** Execute the specified response in the director thread.
     *  If this is called within the director thread and the associated actor
     *  is in its fire() method, then the response is executed immediately.
     *  Otherwise, it is deferred using the director's fireAtCurrentTime() function.
o     *  This is useful, for example, when a response
     *  produces multiple output events or errors, because it ensures that all those
     *  output events and errors are simultaneous in the DE sense. It also prevents
     *  threading issues from having the response execute concurrently
     *  with the swarmlet execution. Generally, this should not be used
     *  for callbacks that will register listeners in reaction to some
     *  event, because the listeners will be registered an arbitrary
     *  amount of time in the future, which can cause events to be missed.
     *  @param response The response to execute.
     */
    protected void _issueResponse(Runnable response) {
        try {
            _actor.invokeCallback(response);
        } catch (IllegalActionException e) {
            _actor.error(_actor.getName()
                    + ": Failed to schedule response handler: "
                    + e.getMessage());
        }
    }

    /** Given an array of strings, return an array where everything is
     *  converted to lower case, duplicates are removed, and the order
     *  is alphabetical.
     *  @param original The array of strings
     *  @return the sorted, lower case, deduped array of Strings.
     */
    protected static TreeSet<String> _removeDuplicates(String[] original) {
        TreeSet<String> result = new TreeSet<String>();
        for (String value : original) {
            result.add(value.toLowerCase());
        }
        return result;
    }

    /** Return the size of a data type.
     *  @param type The object
     *  @return The size
     */
    protected int _sizeOfType(DATA_TYPE type) {
        switch(type) {
        case BYTE:
            return Byte.BYTES;
        case DOUBLE:
        case NUMBER:
            return Double.BYTES;
        case FLOAT:
            return Float.BYTES;
        case INT:
            return Integer.BYTES;
        //case LONG:
        //    return Long.BYTES;
        case SHORT:
            return Short.BYTES;
        case UNSIGNEDBYTE:
            return Byte.BYTES;
        //case UNSIGNEDINT:
        //    return Integer.BYTES;
        case UNSIGNEDSHORT:
            return Short.BYTES;
        default:
            _error("Type has no fixed size: "
                    + type.toString());
            return 0;
        }
    }

    /** Convert a Java byte array into the JavaScript integer array.
     *  @param buffer The input Java byte array to be converted.
     *  @return The resulting JavaScript integer array.
     *  @exception IllegalActionException If the conversion fails.
     */
    protected Object _toJSArray(byte[] buffer) throws IllegalActionException {
        Object[] result = new Object[buffer.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = (Object) buffer[i];
        }
        return _actor.toJSArray(result);
    }

    /** Convert the native JavaScript array or JavaScript string into a Java byte array.
     *  When the input is String that starts with "0x", interpret the input as hex bytes.
     *  @param object The input in JavaScript object, either a JavaScrypt integer array or
     *    a JavaScript string. If the string starts with "0x", it is interpreted as hex bytes.
     *  @return The Java byte array.
     *  @exception IllegalActionException If the conversion fails.
     */
    protected static byte[] _toJavaBytes(Object object) throws IllegalActionException {
        // FIXME: jdk.nashorn.internal.objects.NativeArray is not exported in Java 9
        // At compilation time, the error is:
        // (package jdk.nashorn.internal.objects is declared in module jdk.scripting.nashorn, which does not export it to the unnamed module)
        if (object instanceof ScriptObjectMirror /* || object instanceof NativeArray*/) {
            Collection<Object> values = null;
          // if (object instanceof ScriptObjectMirror) {
                ScriptObjectMirror objectMirror = ((ScriptObjectMirror) object);
                values = objectMirror.values();
          // } else if (object instanceof NativeArray) {
          // NativeArray nativeArray = (NativeArray)object;
          // values = nativeArray.values();
          // } else {
          //    // FindBugs: Avoid a possibly NPE when dereferencing values.
          //   throw new InternalErrorException("The object argument must be an instance of either ScriptObjectMirror or NativeArray.  It was a " + object.getClass().getName());
          // }

            byte[] result = new byte[values.size()];
            int i = 0;
            for (Object value : values) {
                if (value instanceof UnsignedByteToken) {
                    result[i] = ((UnsignedByteToken) value).byteValue();
                }
                else if (value instanceof Byte) {
                    result[i] = ((Byte) value).byteValue();
                }
                else if (value instanceof Integer) {
                    result[i] = ((Integer) value).byteValue();
                }
                // else if (value instanceof Long) {
                //     result[i] = ((Long) value).byteValue();
                // }
                else if (value instanceof Short) {
                    result[i] = ((Short) value).byteValue();
                }
                else if (value instanceof Double) {
                    result[i] = ((Double) value).byteValue();
                }
                else {
                    throw new IllegalActionException("Cannot interpret the input array element type: "
                            + value.getClass().getName());
                }

                i++;
            }
            return result;
        } else if (object instanceof String) {
            String stringObject = (String) object;
            if (stringObject.startsWith("0x")) {
                // hex encoded string

                // Don't use
                // javax.xml.bind.DatatypeConverter.parseHexBinary()
                // here because javax.xml.bind.DatatypeConverter is
                // not directly available in Java 9.  To use it
                // requires compiling with --add-modules
                // java.xml.bind, which seems to not be easily
                // supported in Eclipse.
                // An alternative would be to use Apache commons codec,
                // but this would introduce a compile and runtime dependency.

                return new BigInteger(stringObject.substring(2), 16).toByteArray();
            } else {
                // String.getBytes() causes Dm: Dubious method used (FB.DM_DEFAULT_ENCODING)
                return ((String) object).getBytes(Charset.forName("UTF-8"));
            }
        }
        throw new IllegalActionException("Cannot interpret the input, the input should be either"
                + "JavaScript int array or string.");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     protected fields                      ////

    /** The JavaScript actor that this is helping. All synchronization
     *  done by the helper should synchronize using this object as the monitor.
     */
    protected JavaScript _actor;

    /** The JavaScript object that this is a helper for. */
    protected ScriptObjectMirror _currentObj;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Indicate that the expected type cannot be extracted from the buffer.
     *  @param ex The exception that occurred.
     *  @param type The expected type.
     *  @param buffer The buffer.
     */
    private void _fromTypeError(Throwable ex, DATA_TYPE type, Buffer buffer) {
        String expectedType = type.toString().toLowerCase();
        // ex.printStackTrace();
        _error("Cannot convert buffer data to type "
                + expectedType
                + ": "
                + buffer.toString()
                + "\nException occurred: "
                + ex);
    }

    /** Indicate a conversion error of data to a buffer.
     *  @param type The type to convert to.
     *  @param data The data that cannot be converted to that type.
     */
    private void _toTypeError(DATA_TYPE type, Object data) {
        String expectedType = type.toString().toLowerCase();
        _error("Data cannot be converted to "
                + expectedType
                + ". It is: "
                + data.getClass().getName());
    }
}
