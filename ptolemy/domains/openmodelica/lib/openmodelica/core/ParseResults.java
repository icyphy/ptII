/*
 * This file is part of Modelica Development Tooling.
 *
 * Copyright (c) 2005, Linköpings universitet, Department of
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
 * * Neither the name of Linköpings universitet nor the names of its
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
package ptolemy.actor.lib.openmodelica.core;

import ptolemy.actor.lib.openmodelica.core.compiler.CompileError;
import ptolemy.actor.lib.openmodelica.core.compiler.UnexpectedReplyException;

/**
 * This class implements IParseResults on behalf of OMC proxy.
 */
public class ParseResults {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /**
     * @return the list of fully qualified names of top level classes
     * found in the file
     */
    public String[] getClasses() {
        return classes;
    }

    /**
     * @return a list of all errors encountered while loading and parsing
     * the file 
     */
    public CompileError[] getCompileErrors() {
        return errors;
    }

    /**
     * This method assumes that a flat list of class names is
     * passed to it. If the received list is nested an UnexpectedReplyException
     * is thrown
     * @param classNames
     * @throws UnexpectedReplyException
     */
    public void setClassNames(List classNames) throws UnexpectedReplyException {
        int i = 0;
        classes = new String[classNames.size()];

        for (ListElement element : classNames) {
            if (element instanceof List) {
                throw new UnexpectedReplyException(
                        "a nested list of class names recived");
            }
            /* now we know that element is of type Element */
            classes[i++] = ((Element) element).toString();
        }

    }

    public void setCompileErrors(CompileError[] errors) {
        this.errors = errors;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private CompileError[] errors = new CompileError[0];
    private String[] classes;

}
