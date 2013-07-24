/* The interface to the Modelica compiler.
 *
 * Copyright (c) 2012-2013,
 * Programming Environment Laboratory (PELAB),
 * Department of Computer and getInformation Science (IDA),
 * Linkoping University (LiU).
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

import java.io.IOException;

import ptolemy.data.IntToken;
import ptolemy.kernel.util.IllegalActionException;

/**
   The interface to the Modelica compiler that should be implemented by OMCProxy.
   
   @author Mana Mirzaei 
   @version $Id$
   @since Ptolemy II 9.1
   @Pt.ProposedRating Red (cxh)
   @Pt.AcceptedRating Red (cxh)
 */
public interface IOMCProxy {

    /**  Build the Modelica model by sending buildModel(className) to the OMC.
     *   @param modelName The Name of the model which should be built.
     *   @return CompilerResult The result of sending buildModel(className) command to the OMC.
     *   @throws ConnectException If buildModel command couldn't
     *   be sent to the OMC.
     */
    public CompilerResult buildModel(String modelName) throws ConnectException;
    
    /** Read a result file and return a matrix corresponding to the variables and given size.
     *  @param fileName The executable result file of simulation in CSV format.
     *  @param modelName Name of the model which should be built.
     *  @return The value of the variables in the simulation file. 
     *  @throws IllegalActionException 
     *  @throws ConnectException If commands couldn't
     *   be sent to the (OpenModelica Compiler)OMC. 
     */
    public String displaySimulationResult(String fileName, String modelName) throws ConnectException, IllegalActionException;

    /** Initialize the communication with the (OpenModelica compiler)OMC.
     *  @throws ConnectException If we're unable to start communicating with
     *  the server.
     */
    public void initServer() throws ConnectException;

    /** Check if there is an error in the return value of sendCommand("command") method and
     *  fetch the error-information of current run.
     *  @param retval The string returned by the (OpenModelica Compiler)OMC.
     *  @return Checks If the string is actually an error.
     */
    public boolean isError(String retval);

    /** load the Modelica file and library.  
     *  @param fileName File which the model should be loaded from.
     *  @param modelName Name of the model which should be built.
     *  @throws ConnectException If commands couldn't
     *   be sent to the (OpenModelic Compiler)OMC.
     *  @throws IllegalActionException 
     */
    public void loadFile(String fileName, String modelName)
            throws ConnectException, IllegalActionException;

    /** Return the components which the model is composed of and modify the value of parameters/variables.
     *  @param inputPortValue The value of OpenModelica actor input port which reads init value of the Ramp actor.
     *  @param modelName Name of the model which should be built.
     *  @throws ConnectException If commands couldn't
     *   be sent to the (OpenModelica Compiler)OMC. 
     *  @throws IllegalActionException 
     */
    public void modifyVariables(IntToken inputPortValue, String modelName)
            throws IllegalActionException, ConnectException;

    /** Plot the plt file by calling PxgraphApplication.main(dcmotor_res.plt).
     *  @param fileNamePrefix User preferable name for the result file.
     *  @param modelName Name of the model which should be built.
     *  @throws ConnectException If commands could not be sent to the OMC.
     */
    public void plotPltFile(String fileNamePrefix, String modelName)
            throws ConnectException;

    /** Leave and quit OpenModelica environment.
     *  @throws ConnectException If quit command couldn't
     *  be sent to the OMC.
     */
    public void quitServer() throws ConnectException;

    /** Build the Modelica model. Then, run the executable result file of
     *  buildModel() in both interactive and non-interactive processing mode
     *  in order to generate the simulation result file.
     *  @param fileName File which the model should be loaded from.
     *  @param modelName Name of the model which should be built.
     *  @param fileNamePrefix User preferable name for the result file.
     *  @param startTime The start time of simulation.
     *  @param stopTime The stop time of simulation.
     *  @param numberOfIntervals Number of intervals in the result file.
     *  @param tolerance Tolerance used by the integration method.
     *  @param method Integration method used for simulation.
     *  @param outputFormat Format of the result file.
     *  @param variableFilter Filter for variables that should be stored in the result file.
     *  @param cflags Any standard C language flags.
     *  @param simflags Simulation flags.
     *  @param processingType Type of processing for running the executable result file of building the Modelica model.
     *  @throws ConnectException If commands couldn't
     *   be sent to the (OpenModelic Compiler)OMC.
     *  @throws IOException If the executable result file of buildModel()
     *   couldn't be executed.
     *  @throws IllegalActionException 
     */
    public void simulateModel(String fileName, String modelName,
            String fileNamePrefix, String startTime, String stopTime,
            int numberOfIntervals, String tolerance, String method,
            String outputFormat, String variableFilter, String cflags,
            String simflags, String processingType) throws ConnectException,
            IOException, IllegalActionException;
}
