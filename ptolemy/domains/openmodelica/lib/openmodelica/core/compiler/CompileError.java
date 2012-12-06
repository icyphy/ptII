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

package ptolemy.domains.openmodelica.lib.openmodelica.core.compiler;

/**
 * Class that implements ICompileError on behalf of OMC proxy plugin.
 */
public class CompileError implements ICompileError {
    
    public CompileError(int startLineNumber, int startColumnNumber,
            int endLineNumber, int endColumnNumber, String errorDescription) {
        this.startLineNumber = startLineNumber;
        this.startColumnNumber = startColumnNumber;
        this.endLineNumber = endLineNumber;
        this.endColumnNumber = endColumnNumber;
        this.errorDescription = errorDescription;
    }

    ///////////////////////////////////////////////////////////////////
    ////                    public methods                         ////

    public int getStartLine() {
        return startLineNumber;
    }

    public int getStartColumn() {
        return startColumnNumber;
    }

    public int getEndLine() {
        return endLineNumber;
    }

    public int getEndColumn() {
        return endColumnNumber;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private variables                  ////

    private int startLineNumber;
    private int startColumnNumber;
    private int endLineNumber;
    private int endColumnNumber;
    private String errorDescription;

}
