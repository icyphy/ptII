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
            Pointer /*...*/parameters) {
        // What to do about jni callbacks with varargs?
        // See
        // http://chess.eecs.berkeley.edu/ptexternal/wiki/Main/JNA#fmiCallbackLogger
        //System.out.println("Java FMULogger, status: " + status);
        //System.out.println("Java FMULogger, message: " + message);

        // FIXME: Need to handle the fmi-specific # format:
        // #<Type><valueReference#, where <Type> is one of
        // r, i, b or s. To print a #, use ##.

        if (parameters != null) {
            StringTokenizer tokenizer = new StringTokenizer(message, "%", false /* Return delimiters */);
            ArrayList<Object> parameterList = new ArrayList<Object>();
            //long nativeLong = Pointer.nativeValue(parameters);
            int offset = 0;
            if (!message.startsWith("%") && tokenizer.hasMoreTokens()) {
                // Throw away the first token.
                tokenizer.nextToken();
            }
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                boolean foundType = false;
                for (int i = 0; i < token.length() && !foundType; i++) {
                    char type = token.charAt(i);
                    //System.out.println("Token: " + token + " " + type + " " + offset);
                    switch (type) {
                    case 'd':
                    case 'i':
                    case 'o': // Unsigned octal
                    case 'u': // Unsigned decimal
                    case 'x': // Unsigned hex
                    case 'X': // Unsigned hex
                        foundType = true;
                        //String s = parameters.toString();
                        //int [] value0 = newPointer.getIntArray(offset, 1);

                        //int value = newPointer.getInt(offset);
                        //System.out.println("Token: " + token + " " + conversion + ": " + type + " " + value + " " + value0[0] + " " + Integer.toHexString(value) + " " + s);
                        //parameterList.add(Integer.valueOf(value));
                        if (!_printedMessage) {
                            _printedMessage = true;
                            System.out
                                    .println("FIXME: logger: don't know how to get integers, using 666 instead.");
                        }
                        parameterList.add(Integer.valueOf(666));
                        offset += 4;
                        break;
                    case 'f': // Doubles
                    case 'e':
                    case 'E':
                    case 'g':
                    case 'G':
                        foundType = true;
                        if (!_printedMessage) {
                            _printedMessage = true;
                            System.out
                                    .println("FIXME: logger: don't know how to get doubles, using 666.666 instead.");
                        }
                        //parameterList.add(Double.valueOf(parameters.getDouble(offset++)));
                        parameterList.add(Double.valueOf(666.666));
                        offset += 4;
                        break;
                    case 'c': // Unsigned char
                        foundType = true;
                        //parameterList.add(Character.valueOf(parameters.getChar(offset++)));
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
                        String result = "";
                        if (offset == 0) {
                            result = parameters.getString(offset);
                        } else {
                            if (!_printedMessage) {
                                _printedMessage = true;
                                System.out
                                        .println("FIXME: logger: don't know how to get string other than the first string, using FIXME instead.");
                            }
                            result = "FIXME";
                        }
                        offset += 4;
                        //System.out.println("Token: " + token + " " + conversion + ": " + type + " " + result);
                        parameterList.add(result);
                        break;
                    case 'p': // Pointer
                        foundType = true;
                        //parameterList.add(parameters.getPointer(offset++).toString());
                        if (!_printedMessage) {
                            _printedMessage = true;
                            System.out
                                    .println("FIXME: logger: don't know how to get long other than the first string, using 666 instead.");
                        }
                        parameterList.add(Long.valueOf(666));
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
            //System.out.println("Java FMULogger, message0: " + message + " " + parameterList.size() + " " + parameterList);
            System.out.format("Java FMULogger: " + message,
                    parameterList.toArray());
            System.out.println("");
        }
    }

    // True if we printed the fixme message.
    private static boolean _printedMessage = false;
}
