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

package ptolemy.actor.lib.openmodelica.core.compiler;

import ptolemy.actor.lib.openmodelica.core.CompilerResult;


/**
 * The interface to the modelica compiler.
 *  This interface must be implemented by OMCProxy. 
 */
public interface IModelicaCompiler {

    /** build the model
     *  @param className name of the main class of your model
     *  @exception ConnectException If if buildModel command couldn't 
     *  be sent to the OpenModelica Compiler.
     */
    public CompilerResult buildModel(String filename) throws ConnectException;

    /**
     * Initialize the communication with OMC
     * @throws ConnectException if we're unable to start communicating with
     * the server
     */
    public void init() throws ConnectException;

    /**
     * Fetches the error-information of current run
     *
     * @param className the name of the class where to look for information
     * @return a list consisting the error
     */
    public boolean isError(String retval);

    /** Load models from file.
     *  @param String fileName.
     *  @exception ConnectException, if loadFileInteractiveQualified command couldn't 
     *  be sent to the OpenModelica Compiler.
     */
    public CompilerResult loadFile(String filename) throws ConnectException;

    /** Send a command to the compiler and gets the result string
     *  @param command, the command which should be sent to the compiler.
     *  @return ICompilerResult
     *  @exception ConnectException If command couldn't 
     *  be sent to the OpenModelica Compiler.
     */
    public CompilerResult sendCommand(String command) throws ConnectException;
    
    /** 
     * Leave and quit OpenModelica environment
     *  @exception ConnectException If quit command couldn't 
     *  be sent to the OpenModelica Compiler.
     */

    public void quit() throws ConnectException;

}
