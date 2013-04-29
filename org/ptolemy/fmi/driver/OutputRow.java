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
package org.ptolemy.fmi.driver;

import java.io.PrintStream;

import org.ptolemy.fmi.FMIModelDescription;
import org.ptolemy.fmi.FMIScalarVariable;
import org.ptolemy.fmi.FMIScalarVariable.Alias;
import org.ptolemy.fmi.type.FMIBooleanType;
import org.ptolemy.fmi.type.FMIIntegerType;
import org.ptolemy.fmi.type.FMIRealType;
import org.ptolemy.fmi.type.FMIStringType;

import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;

/**
 * Given a fmu component, output the current state.
 *
 * <p>This class is a port of outputRow() from
 * fmusdk/src/shared/sim_support.c which has the following license:
 *
 * <p>FMU SDK license</p>
 *
 * <p>Copyright (c) 2008-2011, QTronic GmbH. All rights reserved.
 * The FmuSdk is licensed by the copyright holder under the BSD License
 * (http://www.opensource.org/licenses/bsd-license.html):
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <br>- Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * <br>- Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.</p>
 *
 * <p>THIS SOFTWARE IS PROVIDED BY QTRONIC GMBH "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL QTRONIC GMBH BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.</p>
 *
 * @author Christopher Brooks, based on sim_support.c by QTronic GmbH.
 * @version $Id$
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class OutputRow {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

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
            FMIModelDescription fmiModelDescription, Pointer fmiComponent,
            double time, PrintStream file, char separator, Boolean header) {
        int i;
        // fmiReal r;
        // fmiInteger i;
        // fmiBoolean b;
        // fmiString s;
        // fmiValueReference vr;

        // Print the first column.
        if (header) {
            file.print("time");
        } else {
            if (separator == ',') {
                file.format("%g", time);
            } else {
                // Separator is ';' or '\t'
                // If the separator is not a comma, then replace the decimal
                // place with a comma.
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
                if (separator == ',') {
                    // Treat array element, e.g. print a[1, 2] as a[1.2]
                    file.format("%c", separator);
                    // FIXME: Just do a replace()
                    char[] s = scalarVariable.name.toCharArray();
                    for (i = 0; i < s.length; i++) {
                        if (s[i] != ' ') {
                            file.format("%c", s[i] == ',' ? '.' : s[i]);
                        }
                    }
                } else {
                    file.format("%c%s", separator, scalarVariable.name);
                }
            } else {
                // Output values.

                // The value reference is an internal-use-only integer that
                // refers to which variable we
                // are to access.
                // int valueReference = scalarVariable.valueReference;
                // IntBuffer valueReferenceIntBuffer =
                // IntBuffer.allocate(1).put(0, valueReference);
                if (scalarVariable.type instanceof FMIBooleanType) {
                    boolean result = scalarVariable.getBoolean(fmiComponent);
                    file.format("%c%b", separator, result);
                } else if (scalarVariable.type instanceof FMIIntegerType) {
                    // FIXME: handle Enumerations?
                    int result = scalarVariable.getInt(fmiComponent);
                    file.format("%c%d", separator, result);
                } else if (scalarVariable.type instanceof FMIRealType) {
                    double result = scalarVariable.getDouble(fmiComponent);
                    if (separator == ',') {
                        file.format(",%.16g", result);
                    } else {
                        // separator is e.g. ';' or '\t'
                        // If the separator is not a comma, then replace the
                        // decimal place with a comma.
                        file.format("%c%s", separator, Double.toString(result)
                                .replace('.', ','));
                    }
                } else if (scalarVariable.type instanceof FMIStringType) {
                    String result = scalarVariable.getString(fmiComponent);
                    file.format("%c%s", separator, result);
                } else if (scalarVariable.type == null) {
                    file.format("%cTypeIsNull???", separator);
                } else {
                    file.format("%cNoValueForType=%s", separator,
                            scalarVariable.type.getClass().getName());
                }
            }
        }

        // Terminate this row.
        file.format("%n");
    }

    /** This class contains only static methods, so there is no public
     * constructor.
     */
    private OutputRow() {
    }
}
