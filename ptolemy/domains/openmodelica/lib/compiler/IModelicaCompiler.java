/* The interface to the modelica compiler.
 * This file is part of Modelica Development Tooling(MDT).
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

package ptolemy.domains.openmodelica.lib.compiler;


/**
   The interface to the modelica compiler that should be implemented by OMCProxy.
   
   @author Mana Mirzaei 
   @version $Id$
   @since Ptolemy II 9.1
   @Pt.ProposedRating Red (cxh)
   @Pt.AcceptedRating Red (cxh)
 */
public interface IModelicaCompiler {

    /**  Build the Modelica model.
     *  @param className Main class of the model
     *  @return CompilerResult The result of sendExpression("command") method.
     *  @exception ConnectException If buildModel command couldn't
     *  be sent to the OMC.
     */
    public CompilerResult buildModel(String filename) throws ConnectException;

    /**Initialize the communication with the OpenModelica compiler(OMC).
     * @exception ConnectException If we're unable to start communicating with
     * the server.
     */
    public void initServer() throws ConnectException;

    /** Check if there is an error in the return value of sendCommand("command") method and
     *  fetch the error-information of current run.
     *  @param retval The string returned by the OpenModelica Compiler(OMC).
     *  @return Checks If the string is actually an error.
     */
    public boolean isError(String retval);

    /** Load Modelica models from the file.
     *  @param fname The file name.
     *  @return CompilerResult The result of sendExpression("command") method.
     *  @exception ConnectException If loadFileInteractiveQualified command couldn't
     *  be sent to the OMC.
     */
    public CompilerResult loadFile(String filename) throws ConnectException;

    /** Send a command to the OpenModelica Compiler(OMC) and fetches the string result.
     *  @param command The command which should be sent to the OMC.
     *  @return CompilerResult The result of sendExpression("command").
     *  @exception ConnectException If commands couldn't be sent to the OMC.
     */
    public CompilerResult sendCommand(String command) throws ConnectException;

    /** Leave and quit OpenModelica environment.
     *  @exception ConnectException If quit command couldn't
     *  be sent to OMC.
     */
    public void quit() throws ConnectException;

}
