/* OMCCommand invokes OpenModelica Compiler(OMC) by sending command to the OMC server.
 *
 * Copyright (c) 2012-2013,
 * Programming Environment Laboratory (PELAB),
 * Department of Computer and getInformation Science (IDA),
 * Linkoping University (LiU).
 *
 * All rights reserved.
 *
 * (The new BSD license, see also
 *  http://www.opensource.org/licenses/bsd-license.php)
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
 * * Neither the name of Authors nor the name of Linkopings University nor
 *   the names of its contributors may be used to endorse or promote products
 *   derived from this software without specific prior written permission.
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
 * 
 */

package ptolemy.domains.openmodelica.lib.omc;


/**    
    <p> It invokes OpenModelica Compiler(OMC) by sending command to the 
    OMC server with different purposes such as building Modelica model by 
    sending buildModel(), loading Modelica model by sending loadModel(Modelica) and 
    loading file by sending loadFile().
    It fetches the string result of invocation as well. </p>

    @author Mana Mirzaei
    @version $Id$
    @since Ptolemy II 9.1
    @Pt.ProposedRating Red (cxh)
    @Pt.AcceptedRating Red (cxh)
*/
public class OMCCommand implements IOMCCommand {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                   ////

    /**  Build the Modelica model by sending buildModel() to the OMC server.
     *   @param className Main class of the model
     *   @return CompilerResult The result of sendExpression("command") method.
     *   @exception ConnectException If buildModel command couldn't
     *   be sent to the OMC.
     */
    public CompilerResult buildModel(String className) throws ConnectException {
        CompilerResult result = sendCommand("buildModel(" + className + ")");
        return result;
    }

    /** Create an instance of OMCCommand object in order to provide a global point of access to the instance.
     *  It provides a unique source of OMCCommand instance.
     */
    public static OMCCommand getInstance() {

        if (_omcCommandInstance == null) {
            _omcCommandInstance = new OMCCommand();
        }
        return _omcCommandInstance;
    }

    /** Load Modelica model from the file.
     *  @param fname The file name.
     *  @return CompilerResult The result of sendExpression("command") method.
     *  @exception ConnectException If loadFileInteractiveQualified command couldn't
     *  be sent to the OMC.
     */
    public CompilerResult loadFile(String fname) throws ConnectException {
        CompilerResult result = sendCommand("loadFileInteractiveQualified(\""
                + fname + "\")");
        return result;
    }

    /** Load Modelica model.
     *  @param modelicaScript The default value is loadModel(Modelica).
     *  @return CompilerResult The result of sendExpression("loadModel(Modelica)") method.
     *  @exception ConnectException If the command couldn't
     *  be sent to the OMC.
     */
    public CompilerResult loadModelicaModel(String modelicaScript)
            throws ConnectException {
        CompilerResult result = sendCommand(modelicaScript);
        return result;
    }

    /** Send a command to the OpenModelica Compiler(OMC) server and fetches the string result.
     *  @param command The command which should be sent to the OMC.
     *  @return CompilerResult The result of sendExpression("command").
     *  @exception ConnectException If commands couldn't be sent to the OMC.
     */
    public CompilerResult sendCommand(String modelicaCommand)
            throws ConnectException {
        String error = null;
        String[] retval = { "" };

        if (_couldNotStartOMC) {
            return CompilerResult.makeResult(retval, error);
        }

        if (_numberOfErrors > _showMaxErrors) {
            return CompilerResult.makeResult(retval, error);
        }

        // Trim the start and end spaces.
        modelicaCommand = modelicaCommand.trim();

        // Create a unique instance of OMCProxy.
        _omcProxy = OMCProxy.getInstance();

        if (_omcProxy.hasInitialized == false) {
            _omcProxy.initServer();
        }

        try {

            // Fetch the error string from OpenModelica Compiler(OMC). 
            // This should be called after an "Error"
            // is received or whenever the queue of errors are emptied.

            retval[0] = _omcProxy.omcc.sendExpression(modelicaCommand);

            if (!modelicaCommand.equalsIgnoreCase("quit()")) {
                error = _omcProxy.omcc.sendExpression("getErrorString()");
            }

            // Make sure the error string is not empty.
            if (error != null && error.length() > 2) {
                error = error.trim();
                error = error.substring(1, error.length() - 1);
            } else {
                error = null;
            }

            return CompilerResult.makeResult(retval, error);

        } catch (org.omg.CORBA.COMM_FAILURE x) {
            _numberOfErrors++;

            // Lose connection to OMC(OpenModelica Compiler) server.
            throw new ConnectException(
                    "Couldn't send command to the OpenModelica Compiler. Tried sending: "
                            + modelicaCommand);

        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Indicate if we give up on running OpenModelica Compiler(OMC) as it is unable to start. 
    private boolean _couldNotStartOMC = false;

    // Initialization of the number of errors.
    private int _numberOfErrors = 0;

    // OMCProxy Object for accessing a unique source of instance.
    private OMCProxy _omcProxy;

    // OMCProxy Object for accessing a unique source of instance. 
    private static OMCCommand _omcCommandInstance = null;

    // Maximum number of compiler errors to display. 
    private int _showMaxErrors = 10;
}
