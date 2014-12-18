/* Functional Mock-up Interface (FMI) log method implementation.

   Copyright (c) 2012-2014 The Regents of the University of California.
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

import com.sun.jna.Native;
import com.sun.jna.Pointer;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.StringTokenizer;

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
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class FMULog {
    /** Log a message.
     *  Note that arguments after the message are currently ignored.
     *  @param modelDescription The model description that contains
     *  the names of the variables.  The FMI specification states that
     *  the variable names might not be stored in the C-functions,
     *  which is why we can't just use the fmiComponent.
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
    public static void log(FMIModelDescription modelDescription,
	    Pointer fmiComponent, String instanceName, int status,
	    String category, String message
            /*, Pointer*/ /*...*/ /*parameters*/ ) {

        // // FIXME: Use the old logger now.
        // FMULog._logOld(modelDescription,
        //         fmiComponent, instanceName, status,
        //         category, message);
        // // Fake out the compiler so we don't have to uncomment the rest.
        // if (1==1) {
        //     return;
        // }

        // First time through, try to find the JNA variadic extensions
        // methods. See http://chess.eecs.berkeley.edu/ptexternal/wiki/Main/JNA#JNAVarargs
        if (_useVariadicExtensions && _pointerClass == null) {
            try {
                _initialize();
            } catch (Throwable throwable) {
                _useVariadicExtensions = false;
            }
        }

	if (! _useVariadicExtensions) {
            // We don't have the variadic extensions, so we fall back.
            FMULog._nonVariadicLog(modelDescription,
                    fmiComponent, instanceName, status,
                    category, message, null /*parameters*/);
            return;
        }

        try {
            // We need the ffi_cif so we can call the new Native.ffi_closure_va_*
            // functions which allow us to access variadic arguments.

            long ffi_cif = ((Long)_nativeCif.invoke(null, new Object [] {fmiComponent})).longValue();

            // FIXME: Need to handle the fmi-specific # format:
            // #<Type><valueReference#, where <Type> is one of
            // r, i, b or s. To print a #, use ##.

            if (ffi_cif == 0) {
                if (!_printedMessage) {
                    _printedMessage = true;
                    System.err.println("org/ptolemy/fmi/FMULog.java called Pointer.nativeCif(fmiCoomponent), "
                            + "but received a value of 0?  This can happen if the the jna jar file has the "
                            + "Java side of the variadic extensions, but the C side of the variadic extensions "
                            + "have not been compiled for your platform.  To compile them, see "
                            + "http://chess.eecs.berkeley.edu/ptexternal/wiki/Main/JNA#PatchJNAToWorkWithVarargsCallBacks");
                    FMULog._nonVariadicLog(modelDescription,
                            fmiComponent, instanceName, status,
                            category, message, null /*parameters*/);
                }
            } else {
                final char[] msg = message.toCharArray();

                StringBuffer out = new StringBuffer();
                boolean foundEscape = false;
                boolean foundHash = false;

                for (int i = 0; i < msg.length; i++) {
                    // Skipping all fmi-specific formatting options.
                    // In a fmt message, you can say "#r1365#" to mean print the
                    // name of the fmiReal variable with the fmiValueReference =
                    // 1365.
                    //
                    // Need to find out how we can access the fmiValueReferences.
                    if (foundEscape) {
                        // Skip over all the formatting parts besides the
                        // conversion at the end.
                        // XXX There must be a better way...
                        final char[] conversions = new char[] { 'd', 'i', 'o', 'x',
                                                                'X', 'e', 'E', 'f', 'F', 'g', 'G', 'a', 'A', 'c',
                                                                's', 'p', 'n', 'u', '%' };

                        // Find the conversion
                        StringBuffer flags = new StringBuffer();
                        boolean foundConversion = false;
                        boolean foundLong = false;
                        while (!foundConversion) {
                            // While we are delegating to String.format() for the
                            // formatting, find out if we need a long vs. int from
                            // libffi
                            if (msg[i] == 'l' || msg[i] == 'L') {
                                foundLong = true;
                            }
                            for (char c : conversions) {
                                if (msg[i] == c) {
                                    foundConversion = true;
                                    break;
                                }
                            }
                            if (!foundConversion) {
                                flags.append(msg[i]);
                                i++;
                            }
                        }

                        switch (msg[i]) {
                        case 'd':
                        case 'i':
                            out.append(String.format(
                                            "%" + flags.toString() + msg[i],
                                            foundLong
                                            ? _ffi_closure_va_sint64.invoke(null, new Object[] {ffi_cif})
                                            : _ffi_closure_va_sint32.invoke(null, new Object[] {ffi_cif})));
                            break;

                        case 'o': // Unsigned octal
                        case 'x': // Unsigned hex
                        case 'X': // Unsigned hex
                        case 'u': // Unsigned decimal which must be converted to 'd' since String.format() doesnot handle it.
                            out.append(String.format(
                                            "%" + flags.toString()
                                            + (msg[i] == 'u' ? 'd' : msg[i]),
                                            foundLong ? _ffi_closure_va_uint64.invoke(null, new Object[] {ffi_cif})
                                            : _ffi_closure_va_uint32.invoke(null, new Object[]{ffi_cif})));
                            break;

                            // DOU are deprecated.  Does FMI support them?
                        case 'e':
                        case 'E':
                        case 'f':
                        case 'F':
                        case 'g':
                        case 'G':
                        case 'a':
                        case 'A':
                            // XXX Can you handle a long double in Java?  Not checking foundLong
                            out.append(String.format("%" + flags.toString()
                                            + msg[i], _ffi_closure_va_double.invoke(null, new Object[] {ffi_cif})));
                            break;

                        case 'c': // Unsigned char
                            // Assuming 1 byte char
                            out.append(String.format("%" + flags.toString()
                                            + msg[i],
                                            // Don't cast to (char)
                                            // here, thus avoiding
                                            // FB.BX_UNBOXING_IMMEDIATELY_REBOXED
                                            _ffi_closure_va_uint8.invoke(null, new Object[] {ffi_cif})));
                            break;

                        case 's': // String
                            // C strings: Read until you hit NUL (utf-8 is NUL safe).
                            String formatValue = "";
                            Pointer closureVaPointer = (Pointer)
                                _ffi_closure_va_pointer.invoke(null, new Object [] {ffi_cif});
                            if (closureVaPointer == null) {
                                formatValue = "<null>";
                            } else {
                                formatValue = closureVaPointer.getString(0);
                            }
                            out.append(String.format("%" + flags.toString() + "s",
                                            formatValue));
                            break;

                        case 'p': // Pointer
                            out.append(Pointer.nativeValue(
                                            (Pointer)_ffi_closure_va_pointer.invoke(null, new Object [] {ffi_cif})));
                            break;

                        case 'n':
                            // This can take a length modifier but it's the same void * here
                            Pointer p = (Pointer)_ffi_closure_va_pointer.invoke(null, new Object[] {ffi_cif});
                            p.setInt(0, out.length());
                            break;

                        case '%':
                            out.append("%");
                            break;

                        default:
                            // XXX Should not be here: invalid conversion
                            System.out.println("XXX Should not be here: " + msg[i]);
                            out.append(msg[i]);
                            break;
                        }

                        foundEscape = false;

                    } else if (foundHash) {
                        if (msg[i] == '#') {
                            out.append("#");
                            foundHash = false;

                        } else if (msg[i] == '%') {
                            // Assuming this allows you to pass in a variable.
                            // The FMI spec gives an example #r1365# but the demo
                            // here uses #r%u# which is a variadic argument.
                            foundEscape = true;

                        } else {
                            out.append(msg[i]);
                        }

                    } else if (msg[i] == '#') {
                        out.append("#");

                        // XXX Not supporting any FMI formats except '##' and
                        // embedded '%' conversions
                        if (msg[i + 1] == '#') {
                            i++;

                        } else {
                            // XXX Don't know how to map #<type><valueReference>#
                            // to a parameter name yet.
                            // We have to handle extra arguments on the stack
                            // though so look for % conversions.
                            foundHash = true;
                        }

                    } else if (msg[i] == '%') {
                        foundEscape = true;

                    } else {
                        // Normal character
                        out.append(msg[i]);
                    }
                }
                // The format is from FMUSDK.  Please do not change it, we
                // want to keep compatibility with FMUSDK.
                System.out.println(FMULogUtilities.fmiStatusToString(status)
                        + " "
                        + instanceName
                        + "("
                        + category
                        + "): "
                        + FMULogUtilities.replaceVariableReferences(
                                modelDescription, out.toString()));
            }
        } catch (Throwable throwable) {
            FMULog._nonVariadicLog(modelDescription,
                    fmiComponent, instanceName, status,
                    category, message, null /*parameters*/);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private static void _debugMessage(String message) {
        if (_debug) {
            System.out.println(message);
        }
    }

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
                // Use %n instead of \n and avoid FB.VA_FORMAT_STRING_USES_NEWLINE.  %n will produced
                // the plaform dependent end of line character.
                System.out.printf("%n");
            } else {
                System.out.printf(" ");
            }
        }
        System.out.println();
    }

    /** Find the variadic extension methods in the JNA Native and
     *  pointer classes.
     *  We use reflect so that there is not a compile-time dependency
     *  on the variadic extensions.
     */
    private static void _initialize() throws ReflectiveOperationException {
        // Use reflection so that we can compile without
        // the JNA Varargs extensions and check for them at run time.
        try {
            _nativeClass = Class.forName("com.sun.jna.Native");
            _pointerClass = Class.forName("com.sun.jna.Pointer");
        } catch (Throwable throwable) {
            // UnsatsifiedLinkError is an Error, not an Exception, so
            // we catch Throwable
            throw new ClassNotFoundException(
                    "Failed to load com.sun.jna.Native or com.sun.jna.Pointer class",
                    throwable);
        }
        // Native methods;
        _ffi_closure_va_double = _nativeClass.getMethod("ffi_closure_va_double",
                new Class[] { long.class });

        _ffi_closure_va_pointer = _nativeClass.getMethod("ffi_closure_va_pointer",
                new Class[] { long.class });

        _ffi_closure_va_sint32 = _nativeClass.getMethod("ffi_closure_va_sint32",
                new Class[] { long.class });

        _ffi_closure_va_sint64 = _nativeClass.getMethod("ffi_closure_va_sint64",
                new Class[] { long.class });

        _ffi_closure_va_uint8 = _nativeClass.getMethod("ffi_closure_va_uint8",
                new Class[] { long.class });
        
        _ffi_closure_va_uint32 = _nativeClass.getMethod("ffi_closure_va_uint32",
                new Class[] { long.class });

        _ffi_closure_va_uint64 = _nativeClass.getMethod("ffi_closure_va_uint64",
                new Class[] { long.class });

        // Pointer method.
        _nativeCif = _pointerClass.getMethod("nativeCif",
                new Class[] { Pointer.class });
    }

    /** Log a message without using the JNA variadic extensions.
     *  Note that arguments after the message are currently ignored.
     *  @param modelDescription The model description that contains
     *  the names of the variables.  The FMI specification states that
     *  the variable names might not be stored in the C-functions,
     *  which is why we can't just use the fmiComponent.
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
    private static void _nonVariadicLog(FMIModelDescription modelDescription,
            Pointer fmiComponent, String instanceName, int status,
            String category, String message,
            Pointer /*...*/ parameters) {

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

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Set to true to get debugging messages. */
    private static boolean _debug = false;

    /** The Native._ffi_closure_va_double(long) method.
     *  Part of the JNA Varadic extensions.
     */
    private static Method _ffi_closure_va_double;

    /** The Native._ffi_closure_va_pointer(long) method.
     *  Part of the JNA Varadic extensions.
     */
    private static Method _ffi_closure_va_pointer;

    /** The Native._ffi_closure_va_sint32(long) method.
     *  Part of the JNA Varadic extensions.
     */
    private static Method _ffi_closure_va_sint32;

    /** The Native._ffi_closure_va_sint64(long) method.
     *  Part of the JNA Varadic extensions.
     */
    private static Method _ffi_closure_va_sint64;

    /** The Native._ffi_closure_va_uint8(long) method.
     *  Part of the JNA Varadic extensions.
     */
    private static Method _ffi_closure_va_uint8;

    /** The Native._ffi_closure_va_uint32(long) method.
     *  Part of the JNA Varadic extensions.
     */
    private static Method _ffi_closure_va_uint32;

    /** The Native._ffi_closure_va_uint64(long) method.
     *  Part of the JNA Varadic extensions.
     */
    private static Method _ffi_closure_va_uint64;

    /** The Pointer.nativeCif(fmiCoomponent) method. */
    private static Method _nativeCif;

    /** The class of com.sun.jna.Native. */
    private static Class _nativeClass;

    /** The class of com.sun.jna.Pointer. */
    private static Class _pointerClass;

    /** True if we printed the fixme message. */
    private static boolean _printedMessage = false;

    /** True if we should use the variadic extensions. */
    private static boolean _useVariadicExtensions = true;
}
