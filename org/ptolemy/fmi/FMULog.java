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
     */
    public static void log(FMIModelDescription modelDescription,
            Pointer fmiComponent, String instanceName, int status,
            String category, String message) {

        // We need the ffi_cif so we can call the new Native.ffi_closure_va_*
        // functions which allow us to access variadic arguments.
        long ffi_cif = Pointer.nativeCif (fmiComponent);

        // FIXME: Need to handle the fmi-specific # format:
        // #<Type><valueReference#, where <Type> is one of
        // r, i, b or s. To print a #, use ##.

        if (ffi_cif != 0) {
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
                                foundLong ? Native
                                        .ffi_closure_va_sint64(ffi_cif)
                                        : Native.ffi_closure_va_sint32(ffi_cif)));
                        break;

                    case 'o': // Unsigned octal
                    case 'x': // Unsigned hex
                    case 'X': // Unsigned hex
                    case 'u': // Unsigned decimal which must be converted to 'd' since String.format() doesnot handle it.
                        out.append(String.format(
                                "%" + flags.toString()
                                        + (msg[i] == 'u' ? 'd' : msg[i]),
                                foundLong ? Native
                                        .ffi_closure_va_uint64(ffi_cif)
                                        : Native.ffi_closure_va_uint32(ffi_cif)));
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
                                + msg[i], Native.ffi_closure_va_double(ffi_cif)));
                        break;

                    case 'c': // Unsigned char
                        // Assuming 1 byte char
                        out.append(String.format("%" + flags.toString()
                                + msg[i],
                                (char) Native.ffi_closure_va_uint8(ffi_cif)));
                        break;

                    case 's': // String
                        // C strings: Read until you hit NUL (utf-8 is NUL safe).
                        String formatValue = "";
                        Pointer closureVaPointer = Native
                                .ffi_closure_va_pointer(ffi_cif);
                        if (closureVaPointer == null) {
                            formatValue = "<null>";
                        } else {
                            formatValue = closureVaPointer.getString(0);
                        }
                        out.append(String.format("%" + flags.toString() + "s",
                                formatValue));
                        break;

                    case 'p': // Pointer
                        out.append(Pointer.nativeValue(Native
                                .ffi_closure_va_pointer(ffi_cif)));
                        break;

                    case 'n':
                        // This can take a length modifier but it's the same void * here
                        Pointer p = Native.ffi_closure_va_pointer(ffi_cif);
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
    }
}
