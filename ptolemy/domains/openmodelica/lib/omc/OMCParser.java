/*
 * This file is part of Modelica Development Tooling.
 *
 * Copyright (c) 2005, Linkopings universitet, Department of
 * Computer and Information Science, PELAB
 *
 * All rights reserved.
 *
 * (The new BSD license, see also
 * http://www.opensource.org/licenses/bsd-license.php)
 *
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * * Neither the name of Linkopings universitet nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package ptolemy.domains.openmodelica.lib.omc;

import java.util.StringTokenizer;

import ptolemy.domains.openmodelica.lib.core.compiler.CompileError;
import ptolemy.domains.openmodelica.lib.core.compiler.UnexpectedReplyException;

/**
 * Handles parsing of more complex replys from OMC.
 *
 * @author Andreas Remar
 */
public class OMCParser {

    /**
     * Parses the error string that is set by loadFileInteractive() on compile
     * errors. E.g. error string will look like \n delimeted list of
     *      [/path/to/file.mo:20:1-20:5:writable]: error: some error
     *
     * The actual error string is retrived by calling getErrorString() after
     * calling loadFileInteractive()
     *
     * @param errorString the error string in the above format
     * @return
     */
    public static CompileError[] parseErrorString(String errorString)
            throws UnexpectedReplyException {
        if (errorString.equals("")) {
            throw new UnexpectedReplyException("Empty error message");
        }

        StringTokenizer strTok = new StringTokenizer(errorString, "\r\n");

        if (strTok.countTokens() == 0) {
            throw new UnexpectedReplyException("Empty error message");
        }

        CompileError[] compileErrs = new CompileError[strTok.countTokens()];

        for (int i = 0; strTok.hasMoreTokens(); i++) {
            /* Default line number is 1 in case OMC returns unexpected string */
            int startLineNumber = 1;
            int startColumnNumber = 1;
            int endLineNumber = 1;
            int endColumnNumber = 1;
            String errorLine = strTok.nextToken();

            /*
             * An error looks something like:
             *   [/path/to/file.mo:20:12-34:20:writable]: error: some error
             * We split on ']'
             */

            /*
             * errorParts[0] is now error location
             * and errorParts[1] is error message
             */
            String[] errorParts = errorLine.split("]");

            if (errorParts.length != 2) {
                throw new UnexpectedReplyException("Weird error message from "
                        + "the compiler: [" + errorLine + "]");
            }

            String errorLocation = errorParts[0];
            String errorMessage = errorParts[1];

            /*
             * Parse error location from
             *    "[/path/to/file.mo:20:12-34:20:writable"
             * or
             *    "[c:/path/to/file.mo:20:12-34:20:writable"
             */

            String errorLocationParts[] = errorLocation.split(":");

            /*
             * We expect 5 (on unix) or 6 (on windows) ':' separated parts in a
             * proper error location string
             */
            if (errorLocationParts.length < 5 || errorLocationParts.length > 6) {
                throw new UnexpectedReplyException("Weird error message from "
                        + "the compiler: [" + errorLine + "]");
            }
            /*
             * This is the ugliest hack in a long while. Aaaah, nice.
             *
             * How it actually works:
             *   Because : is both a separator in Windows (C:/path/to/file) and
             *   a separator in error messages, where the line and column
             *   numbers are found varies. If on Windows, the info we want
             *   starts at array index 2, and on Unix, it starts
             *   at array index 1. Simply check if the element at index 1 is
             *   a digit or something else. Use this to set where in the array
             *   that the line and column numbers can be found.
             */
            if (errorLocationParts[1].length() == 0) {
                throw new UnexpectedReplyException("Weird error message from "
                        + "the compiler: [" + errorLine + "]");
            }

            char startCharacter = errorLocationParts[1].charAt(0);
            int infoOffset;
            if (startCharacter >= '0' && startCharacter <= '9') {
                infoOffset = 0;
            } else {
                infoOffset = 1;
            }

            try {
                startLineNumber = Integer
                        .parseInt(errorLocationParts[1 + infoOffset]);

                // Split the 12-34 (start column & end line)
                String startColumnAndEndLine[] = errorLocationParts[2 + infoOffset]
                        .split("-");

                if (startColumnAndEndLine.length != 2) {
                    throw new UnexpectedReplyException("Weird error message "
                            + "from the compiler: [" + errorLine + "]");
                }

                startColumnNumber = Integer.parseInt(startColumnAndEndLine[0]);

                endLineNumber = Integer.parseInt(startColumnAndEndLine[1]);

                endColumnNumber = Integer
                        .parseInt(errorLocationParts[3 + infoOffset]);
            } catch (NumberFormatException e) {
                throw new UnexpectedReplyException("Weird error message from"
                        + " the compiler: [" + errorLine + "]");
            }

            /*
             * Parse error message from
             *   ": error: some error"
             * we are interested in the rest of the string after
             * second colon
             */
            int firstColon = errorMessage.indexOf(":");
            int secondColon = errorMessage.substring(firstColon + 1).indexOf(
                    ":");
            /* We need global position on errorMessage string */
            secondColon += firstColon + 1;

            String errorDesc = errorMessage.substring(secondColon + 1).trim();

            compileErrs[i] = new CompileError(startLineNumber,
                    startColumnNumber, endLineNumber, endColumnNumber,
                    errorDesc);
        }

        return compileErrs;
    }

}
