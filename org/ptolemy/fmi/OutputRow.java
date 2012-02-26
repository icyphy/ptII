/* Read a Functional Mock-up Unit .fmu file and invoke it as a co-simulation.

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

import com.sun.jna.Function;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Platform;

import org.ptolemy.fmi.FMILibrary.FMIStatus;
import org.ptolemy.fmi.FMICallbackFunctions.ByValue;
import com.ochafik.lang.jnaerator.runtime.NativeSize;
import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

import java.io.File;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;

/**
 * <p>This method is a port of outputRow() from
 * fmusdk/src/shared/sim_support.c which has the following license
 * <pre>
 * FMU SDK license 
 *
 * Copyright (c) 2008-2011, QTronic GmbH. All rights reserved.
 * The FmuSdk is licensed by the copyright holder under the BSD License
 * (http://www.opensource.org/licenses/bsd-license.html):
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY QTRONIC GMBH "AS IS" AND ANY EXPRESS OR 
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES 
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 * IN NO EVENT SHALL QTRONIC GMBH BE LIABLE FOR ANY DIRECT, INDIRECT, 
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY 
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF 
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * </pre>
 * @author Christopher Brooks
 * @version $Id$
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class OutputRow {
    /** Output time and all non-alias variables in CSV format. If the
     *  separator is ',', columns are separated by ',' and '.' is used
     *  for floating-point numbers.  Otherwise, the given separator
     *  (e.g. ';' or '\t') is to separate columns, and ',' is used as
     *  decimal dot in floating-point numbers.
     */
    public static void outputRow(NativeLibrary nativeLibrary,
            FMIModelDescription fmiModelDescription,
            Pointer fmiComponent, double time, 
            PrintStream file, char separator, Boolean header) {
        int k;
        //         fmiReal r;
        //         fmiInteger i;
        //         fmiBoolean b;
        //         fmiString s;
        //         fmiValueReference vr;

        int i;
    
        // print first column
        if (header) {
            file.print("time");
        } else {
            if (separator==',') {
                file.format("%.16g", time);
            } else {
                // separator is e.g. ';' or '\t'
                file.format("%s", Double.toString(time).replace('.', ','));
            }
        }
    
        // Print all other columns
        for (FMIScalarVariable scalarVariable : fmiModelDescription.modelVariables) {
            // FIXME: deal with aliases
            //if (getAlias(scalarVariable) != enu_noAlias) {
            //    continue;
            //}
            if (header) {
                // output names only
                if (separator==',') {
                    // treat array element, e.g. print a[1, 2] as a[1.2]
                    file.format("%c", separator);
                    // FIXME: Just do a replace()
                    char[] s = scalarVariable.name.toCharArray();
                    for(i = 0; i < s.length; i++) {
                        if (s[i] != ' ') {
                            file.format("%c", s[i] == ',' ? '.' : s[i]);
                        }
                    }
                } else {
                    file.format("%c%s", separator, scalarVariable.name);
                }
            }
            else {
                // output values
                int valueReference = scalarVariable.valueReference;
                IntBuffer valueReferenceIntBuffer = IntBuffer.allocate(1).put(0, valueReference);
                if (scalarVariable.type instanceof FMIRealType) {
                    DoubleBuffer valueBuffer = DoubleBuffer.allocate(1);

                    // FMILibrary.INSTANCE.bouncingBall_fmiGetReal(fmiComponent,
                    //        valueReferenceIntBuffer,
                    //        new NativeSize(1), valueBuffer);
                    Function function = nativeLibrary.getFunction("bouncingBall_fmiGetReal");
                    int fmiFlag = ((Integer)function.invokeInt(new Object[] {fmiComponent, valueReferenceIntBuffer, new NativeSize(1), valueBuffer})).intValue();
                    double result = valueBuffer.get(0);
                            
                    if (separator==',') {
                        file.format(",%.16g", result);
                    } else {
                        // separator is e.g. ';' or '\t'
                        file.format("%c%s", separator, Double.toString(result).replace('.', ','));
                    }
                }

                //                 case elm_Integer:
                //                 case elm_Enumeration:
                //                     fmu->getInteger(c, &vr, 1, &i);
                //                     file.format("%c%d", separator, i);
                //                     break;
                //                 case elm_Boolean:
                //                     fmu->getBoolean(c, &vr, 1, &b);
                //                     file.format("%c%d", separator, b);
                //                     break;
                //                 case elm_String:
                //                     fmu->getString(c, &vr, 1, &s);
                //                     file.format("%c%s", separator, s);
                //                     break;
                //                 default: 
                //                     file.format("%cNoValueForType=%d", separator,sv->typeSpec->type);

            }
        } // for
    
        // terminate this row
        file.format("\n"); 
    }
}