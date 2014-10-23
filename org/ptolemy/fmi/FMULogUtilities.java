/* Utilities for FMU Logging.

   Copyright (c) 2013-2014 The Regents of the University of California.
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

///////////////////////////////////////////////////////////////////
//// FMUCoSimulation

/** Utilities for the FMU Logger.
 *
 * <p>This file contains a method based on fmusdk/src/shared/sim_support.c
 * by Jakob Mauss, which has the following license:</p>
 *
 * <p>FMU SDK license</p>
 *
 * <p>Copyright (c) 2011, QTronic GmbH. All rights reserved.
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
 * @author Christopher Brooks, based on shared/sim_support.c
@version $Id$
@since Ptolemy II 10.0
 * @version $Id$
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class FMULogUtilities {

    // This class is a separate class so as to make it obvious
    // which parts are based on FMUSDK.

    /** Replace FMI variable references with the name of the variable.
     *
     * <p>The FMI log method optionally takes a variable number of
     * arguments and handles
     * <code>#<i>Type</i><i>valueReference</i>#</code>, specially
     * where <i>Type</i> is one of r, i, b or s. To print a #, use
     * ##.</p>
     *
     * <p>This method is based on based on a method by the same name
     * in shared/sim_support.c from FMUSDK.</p>
     *
     * @param modelDescription The model description that contains the
     * names of the variables.
     * @param messageString The string that has had the % directed
     * processed.  The input will be of the format "foo #r1234# bar".
     * @return a String with the variable references replaced.
     */
    public static String replaceVariableReferences(
            FMIModelDescription modelDescription, String messageString) {
        int i = 0;
        StringBuffer results = new StringBuffer();
        char[] message = messageString.toCharArray();
        char c = message[i];
        while (i < message.length) {
            if (c != '#') {
                results.append(c);
                i++;
                if (i >= message.length) {
                    break;
                }
                c = message[i];
            } else {
                int end = messageString.indexOf('#', i + 1);
                if (end == -1) {
                    results.append("(Unmatched '#' in \"" + messageString
                            + "\".)");
                    results.append('#');
                    break;
                }
                if (end - i == 1) {
                    // ## detected, output #
                    results.append('#');
                    i += 2;
                    if (i >= message.length) {
                        throw new ArrayIndexOutOfBoundsException(
                                "Internal Error");
                    }
                    c = message[i];
                } else {
                    Class fmiTypeClass = null;
                    char typeName = message[i + 1]; // one of ribs
                    switch (typeName) {
                    case 'r':
                        fmiTypeClass = FMI_REAL_TYPE_CLASS;
                        break;
                    case 'i':
                        fmiTypeClass = FMI_INTEGER_TYPE_CLASS;
                        break;
                    case 'b':
                        fmiTypeClass = FMI_BOOLEAN_TYPE_CLASS;
                        break;
                    case 's':
                        fmiTypeClass = FMI_STRING_TYPE_CLASS;
                        break;
                    default:
                        fmiTypeClass = null;
                        break;
                    }

                    if (fmiTypeClass == null) {
                        results.append("(TypeName: '" + typeName
                                + "' not supported, must be r, i, b or s.)");
                        i++;
                    } else {
                        String integerString = messageString.substring(i + 2,
                                end);
                        try {
                            int variableReference = Integer
                                    .parseInt(integerString);
                            boolean foundIt = false;
                            for (FMIScalarVariable scalarVariable : modelDescription.modelVariables) {
                                if (scalarVariable.valueReference == variableReference
                                        && scalarVariable.type.getClass()
                                        .isAssignableFrom(fmiTypeClass)) {
                                    foundIt = true;
                                    results.append(scalarVariable.name);
                                    break;
                                }
                            }
                            if (!foundIt) {
                                results.append("?");
                            }
                            i += (end - i + 1);
                            if (i >= message.length) {
                                throw new ArrayIndexOutOfBoundsException(
                                        "Internal Error");
                            }
                            c = message[i];
                        } catch (NumberFormatException ex) {
                            results.append("(NumberFormatException: \""
                                    + integerString + "\" in \""
                                    + messageString + "\")");
                            results.append('#');
                            break;
                        }
                    }
                }
            }
        }
        return results.toString();
    }

    /** Given a fmiStatus value, return a string description.
     *  @param status One of the values from
     *  {@link org.ptolemy.fmi.FMILibrary.FMIStatus}
     *  @return A string that describes the status.
     */
    public static String fmiStatusToString(int status) {
        String results = "";
        switch (status) {
        case FMILibrary.FMIStatus.fmiOK:
            results = "ok";
            break;
        case FMILibrary.FMIStatus.fmiWarning:
            results = "warning";
            break;
        case FMILibrary.FMIStatus.fmiDiscard:
            results = "discard";
            break;
        case FMILibrary.FMIStatus.fmiError:
            results = "error";
            break;
        case FMILibrary.FMIStatus.fmiFatal:
            results = "fatal";
            break;
        case FMILibrary.FMIStatus.fmiPending:
            results = "fmiPending";
            break;
        default:
            results = "?";
            break;
        }
        return results;
    }

    /** The FMIRealType class. */
    static Class FMI_REAL_TYPE_CLASS;

    /** The FMIIntegerType class. */
    static Class FMI_INTEGER_TYPE_CLASS;

    /** The FMIBooleanType class. */
    static Class FMI_BOOLEAN_TYPE_CLASS;

    /** The FMIStringType class. */
    static Class FMI_STRING_TYPE_CLASS;

    static {
        try {
            FMI_REAL_TYPE_CLASS = Class
                    .forName("org.ptolemy.fmi.type.FMIRealType");
            FMI_INTEGER_TYPE_CLASS = Class
                    .forName("org.ptolemy.fmi.type.FMIIntegerType");
            FMI_BOOLEAN_TYPE_CLASS = Class
                    .forName("org.ptolemy.fmi.type.FMIBooleanType");
            FMI_STRING_TYPE_CLASS = Class
                    .forName("org.ptolemy.fmi.type.FMIStringType");
        } catch (ClassNotFoundException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }
}
