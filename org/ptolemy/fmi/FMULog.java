/* Functional Mock-up Interface (FMI) log method implementation.

   Copyright (c) 2012 The Regents of the University of California.
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

package org.ptolemy.fmi;

import java.util.ArrayList;
import java.util.StringTokenizer;

import com.sun.jna.Pointer;

/**
 * A Functional Mock-up Interface (FMI) log method implementation.
 *
 * <p>This Java method is called via a callback from the FMI C-side.
 * The FMI log method optionally takes a variable number of arguments
 * and handles <code>#<i>Type</i><i>valueReference</i>#</code>,
 * specially where <i>Type</i> is one of r, i, b or s. To print a #,
 * use ##.
 *
 * @author Christopher Brooks
 * @version $Id$
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class FMULog {

    /** Log a message.
     *  Note that arguments after the message are currently ignored.
     *  @param fmiComponent The component that was instantiated.
     *  @param instanceName The name of the instance of the FMU.
     *  @param status The fmiStatus, see
     *  {@link org.ptolemy.fmi.FMILibrary.FMIStatus}
     *  @param category The category of the message,
     *  defined by the tool that created the fmu.  Typical
     *  values are "log" or "error".
     *  @param message The message in printf format
     *  @param parameters The printf style parameters.
     */
    public static void log(Pointer fmiComponent, String instanceName,
            int status, String category, String message,
            Pointer /*...*/ parameters) {
        
        // MessageHandler.message(String.format(message, (Object[])parameters));
        // What to do about jni callbacks with varargs?
        // See
        // http://chess.eecs.berkeley.edu/ptexternal/wiki/Main/JNA#fmiCallbackLogger
        _debugMessage("Java FMULogger, status: " + status);
        _debugMessage("Java FMULogger, message: " + message);

        // FIXME: Need to handle the fmi-specific # format:
        // #<Type><valueReference#, where <Type> is one of
        // r, i, b or s. To print a #, use ##.

        if (parameters != null) {
            StringTokenizer tokenizer = new StringTokenizer(message, "%", false ); // Return delimiters
            ArrayList<Object> parameterList = new ArrayList<Object>();
            int offset = 0;
            Pointer newPointer = parameters;
            if (!message.startsWith("%") && tokenizer.hasMoreTokens()) {
                // Throw away the first token.
                tokenizer.nextToken();
            }
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                boolean foundType = false;
                for (int i = 0; i < token.length() && !foundType; i++) {
                    char type = token.charAt(i);
                    _debugMessage("Token: " + token + " " + type + " " + offset);
                    switch (type) {
                    case 'd':
                    case 'i':
                    case 'o': // Unsigned octal
                    case 'u': // Unsigned decimal
                    case 'x': // Unsigned hex
                    case 'X': // Unsigned hex
                        foundType = true;
                        _debugMessage("newPointer: " + newPointer);
                        int value = (int) Pointer.nativeValue(newPointer);
                        _debugMessage("Token: " + token + " type:" + type + " value:" + value + " offset: " + offset);
                        if (offset != 0) {
                            if (!_printedMessage) {
                                _printedMessage = true;
                                System.out
                                    .println("FIXME: logger: only know how to get the first value, using 666 instead");
                            }
                            value = 666;
                        }
                        parameterList.add(Integer.valueOf(value));
                        offset += 4;
                        break;
                    case 'f': // Doubles
                    case 'e':
                    case 'E':
                    case 'g':
                    case 'G':
                        foundType = true;
                        double doubleValue = Pointer.nativeValue(newPointer);
                        _debugMessage("Token: " + token + " type:" + type + " doubleValue:" + doubleValue + " offset: " + offset);
                        if (doubleValue > 9999.0) {
                            _dump(newPointer, 4, 0);
                        }
                        if (offset != 0) {
                            if (!_printedMessage) {
                                _printedMessage = true;
                                System.out
                                    .println("FIXME: logger: only know how to get the first value, using 666.666 instead");
                            }
                            doubleValue = 666.666;
                        }
                        parameterList.add(Double.valueOf(doubleValue));
                        offset += 4;
                        break;
                    case 'c': // Unsigned char
                        foundType = true;
                        if (offset != 0) {
                            if (!_printedMessage) {
                                _printedMessage = true;
                                System.out
                                    .println("FIXME: logger: only know how to get the first value, using 666.666 instead");
                            }
                            doubleValue = 666.666;
                        }

                        //parameterList.add(Character.valueOf(newParameter.getChar(offset++)));
                        if (!_printedMessage) {
                            _printedMessage = true;
                            System.out
                                    .println("FIXME: logger: don't know how to get chars, using ! instead.");
                        }
                        parameterList.add(Character.valueOf('!'));
                        offset += 1;
                        break;
                    case 's': // String
                        foundType = true;
                        _debugMessage("type=s, parameters = " + parameters
                                + " offset: " + offset);
                        _debugMessage("type=s "
                                + parameters.share(offset));

                        if (offset == 0) {
                            String result = newPointer.getString(offset);
                            //offset += result.length() + 1;
                            newPointer = new Pointer(Pointer.nativeValue(newPointer) + 4);
                            _debugMessage("Token: " + token + " type: " + type + " result: " + result);
                            parameterList.add(result);
                        } else {
                            if (!_printedMessage) {
                                _printedMessage = true;
                                System.out
                                    .println("FIXME: logger: don't know how to get string other than the first string, using 666 instead.");
                            }
                            parameterList.add("666");
                        }
                        offset += 4;
                        break;
                    case 'p': // Pointer
                        foundType = true;
                        long longValue = Pointer.nativeValue(newPointer);
                        if (offset != 0) {
                            if (!_printedMessage) {
                                _printedMessage = true;
                                System.out
                                    .println("FIXME: logger: only know how to get the first value, using 6666 instead");
                            }
                            longValue = 6666;
                        }
                        parameterList.add(Long.valueOf(longValue));
                        offset += 4;
                        break;
                    case 'n':
                        // FIXME: Don't know how to handle this:
                        // "The argument shall be a
                        // pointer to an integer into which is
                        // written the number of characters
                        // written to the output stream so far
                        // by this call to fprintf .  No
                        // argument is converted."
                        foundType = true;
                        break;
                    case '%':
                        // FIXME: what about %%?
                        foundType = true;
                        break;
                    default:
                        break;
                    }
                }
            }
            // Java format does not handle %u.  Lamers.
            message = message.replace("%u", "%d");
            _debugMessage("Java FMULogger, message0: " + message + " " + parameterList.size() + " " + parameterList);
            System.out.format("Java FMULogger: " + message,
                    parameterList.toArray());
            System.out.println("");
        }
    }

    /** Set to true to get debugging messages. */
    private static boolean _debug = false;

    private static void _dump(Pointer pointer, int size, int offset) {
        if (!_debug) {
            return;
        }
        System.out.print("dump: " + pointer + "<" + size + "," + offset + "," + Pointer.nativeValue(pointer) + ">");
        System.out.printf("<%x>", Pointer.nativeValue(pointer));
        byte bytes[] = pointer.getByteArray(offset, size);
        for (int i = 0; i < size; ++i) {
            System.out.printf("%02x(%c)", bytes[i], bytes[i]);
            if ((i % 16) == 15) {
                System.out.printf("\n");
            } else {
                System.out.printf(" ");
            }
        }
        System.out.println();
    }

    private static void _debugMessage(String message) {
        if (_debug) {
            System.out.println(message);
        }
    }

    /** True if we printed the fixme message. */
    private static boolean _printedMessage = false;
}
