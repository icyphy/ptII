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

import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;

import org.ptolemy.fmi.FMIScalarVariable.Alias;

import com.sun.jna.Function;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * <p>This method is a port of outputRow() from
 * fmusdk/src/shared/sim_support.c which has the following license:
 *
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
 *
 * @author Christopher Brooks, based on sim_support.c by QTronic GmbH.
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
     *  @param nativeLibrary The native library that contains
     *  the fmi*() functions to be invoked.
     *  @param fmiModelDescription An object that represents the
     *  modelDescription.xml file.
     *  @param fmiComponent The component being invoked.
     *  @param time The time of this row.
     *  @param file The output file
     *  @param separator The separator character.
     *  @param header True if the header containing the column
     *  names should be printed.
     */
    public static void outputRow(NativeLibrary nativeLibrary,
            FMIModelDescription fmiModelDescription,
            Pointer fmiComponent, double time, 
            PrintStream file, char separator, Boolean header) {
        int i;
        //         fmiReal r;
        //         fmiInteger i;
        //         fmiBoolean b;
        //         fmiString s;
        //         fmiValueReference vr;

        // Print the first column.
        if (header) {
            file.print("time");
        } else {
            if (separator==',') {
                file.format("%g", time);
            } else {
                // Separator is ';' or '\t'
                // If the separator is not a comma, then replace the decimal place with a comma.
                file.format("%s", Double.toString(time).replace('.', ','));
            }
        }
    
        // Print all the other columns.
        for (FMIScalarVariable scalarVariable : fmiModelDescription.modelVariables) {
            if (scalarVariable.alias != null
                    && scalarVariable.alias != Alias.noAlias) {
                // If the scalarVariable has an alias, then skip it.
                // In bouncingBall.fmu, g has an alias, so it is skipped.
                continue;
            }
            if (header) {
                // Output header names.
                if (separator==',') {
                    // Treat array element, e.g. print a[1, 2] as a[1.2]
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
                // Output values.
                
                // The value reference is an internal-use-only integer that refers to which variable we
                // are to access.
                int valueReference = scalarVariable.valueReference;
                IntBuffer valueReferenceIntBuffer = IntBuffer.allocate(1).put(0, valueReference);
                if (scalarVariable.type instanceof FMIBooleanType) {
                    // The FMI 1.0 spec defines Booleans as being 8 bits.
                    ByteBuffer valueBuffer = ByteBuffer.allocate(1);
                    Function function = nativeLibrary.getFunction(fmiModelDescription.modelIdentifier
                            + "_fmiGetBoolean");
                    int fmiFlag = ((Integer)function.invokeInt(
                                    new Object[] {fmiComponent, valueReferenceIntBuffer,
                                                  new NativeSizeT(1), valueBuffer})).intValue();
                    if (fmiFlag > FMILibrary.FMIStatus.fmiWarning) {
                        throw new RuntimeException("Could not get the boolean with valueReference "
                                + valueReference + ": " + fmiFlag);
                    }
                    byte result = valueBuffer.get(0);
                    file.format("%c%d", separator, result);

                } else if (scalarVariable.type instanceof FMIIntegerType) {
                    // FIXME: handle Enumerations?
                    IntBuffer valueBuffer = IntBuffer.allocate(1);
                    Function function = nativeLibrary.getFunction(fmiModelDescription.modelIdentifier
                            + "_fmiGetInteger");
                    int fmiFlag = ((Integer)function.invokeInt(
                                    new Object[] {fmiComponent, valueReferenceIntBuffer,
                                                  new NativeSizeT(1), valueBuffer})).intValue();
                    if (fmiFlag > FMILibrary.FMIStatus.fmiWarning) {
                        throw new RuntimeException("Could not get the integer with valueReference "
                                + valueReference + ": " + fmiFlag);
                    }
                    int result = valueBuffer.get(0);
                    file.format("%c%d", separator, result);

                } else if (scalarVariable.type instanceof FMIRealType) {
                    DoubleBuffer valueBuffer = DoubleBuffer.allocate(1);
                    Function function = nativeLibrary.getFunction(fmiModelDescription.modelIdentifier
                            + "_fmiGetReal");
                    int fmiFlag = ((Integer)function.invokeInt(
                                    new Object[] {fmiComponent, valueReferenceIntBuffer,
                                                  new NativeSizeT(1), valueBuffer})).intValue();
                    if (fmiFlag > FMILibrary.FMIStatus.fmiWarning) {
                        throw new RuntimeException("Could not get the real with valueReference "
                                + valueReference + ": " + fmiFlag);
                    }
                    double result = valueBuffer.get(0);
                    if (separator==',') {
                        file.format(",%.16g", result);
                    } else {
                        // separator is e.g. ';' or '\t'
                        // If the separator is not a comma, then replace the decimal place with a comma.
                        file.format("%c%s", separator, Double.toString(result).replace('.', ','));
                    }

                } else if (scalarVariable.type instanceof FMIStringType) {       
                    PointerByReference pointerByReference = new PointerByReference();

                    Function function = nativeLibrary.getFunction(fmiModelDescription.modelIdentifier
                            + "_fmiGetString");
                    int fmiFlag = ((Integer)function.invokeInt(
                                    new Object[] {fmiComponent, valueReferenceIntBuffer,
                                                  new NativeSizeT(1), pointerByReference})).intValue();
                    if (fmiFlag > FMILibrary.FMIStatus.fmiWarning) {
                        throw new RuntimeException("Could not get the string with valueReference "
                                + valueReference + ": " + fmiFlag);
                    }
                    Pointer pointer = pointerByReference.getValue();
                    file.format("%c%s", separator, pointer.getString(0));
                } else {
                    file.format("%cNoValueForType=%s", separator, scalarVariable.type.getClass().getName());
                }
            }
        }
    
        // Terminate this row.
        file.format("\n"); 
    }
}
