/* The interface to the OMCCommand.
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
import java.util.HashMap;

import ptolemy.kernel.util.IllegalActionException;

/**
   The interface to the OMCCommand.

   @author Mana Mirzaei
   @version $Id$
   @since Ptolemy II 10.0
   @Pt.ProposedRating Red (cxh)
   @Pt.AcceptedRating Red (cxh)
 */
public interface IOMCCommand {

    /** Check if the (base-)model inherits from other classes.
     *  @param modelName The (base-)model that should be built.
     *  @return Check Return true, if the number of inherited classes is more than zero.
     */
    public boolean getInheritanceCount(String modelName);

    /** Return the list of component declarations within (base-)model.
     *  @param modelName The (base-)model that should be built.
     *  @return HashMap The list of component declarations within (base-)model.
     */
    public HashMap getModelComponent(String modelName);

    /** Initialize the communication with the (OpenModelica compiler)OMC.
     *  @exception ConnectException If we're unable to start communicating with
     *  the server.
     */
    public void initializeServer() throws ConnectException;

    /** Check if there is an error in the return value of sendCommand("command") method and
     *  fetch the error-information of current run.
     *  @param retval The string returned by the (OpenModelica Compiler)OMC.
     *  @return Check If the string is actually an error.
     */
    public boolean isError(String retval);

    /** load the Modelica file(s) and library.
     *  @param fileName File which the model should be loaded from.
     *  @param modelName Name of the model which should be built.
     *  @exception ConnectException If commands couldn't
     *   be sent to the (OpenModelic Compiler)OMC.
     */
    public void loadModelicaFile(String fileName, String modelName)
            throws ConnectException;

    /** Modify parameter(s) and variable(s) of the Modelica model before building the Modelica model.
     *  @param values New values to change the components' values.
     *  @param modelName The (base-)model that should be built.
     *  @param components The models' components to change.
     *  @exception ConnectException If commands couldn't
     *   be sent to the (OpenModelica Compiler)OMC.
     *  @exception IllegalActionException
     */
    public void modifyComponents(String values, String modelName,
            String components) throws IllegalActionException, ConnectException;

    /** Plot the plt file by calling PxgraphApplication.main(modelName).
     *  @param modelName Name of the model which should be built.
     *  @exception ConnectException If commands could not be sent to the OMC.
     */
    public void plotPltFile(String modelName) throws ConnectException;

    /** Build the Modelica model. Then, run the executable result file of
     *  buildModel() in both interactive and non-interactive processing mode
     *  in order to generate the simulation result file.
     *  @param fileName File which the model should be loaded from.
     *  @param modelName Name of the (base-)model which should be built.
     *  @param startTime The start time of simulation.
     *  @param stopTime The stop time of simulation.
     *  @param numberOfIntervals Number of intervals in the result file.
     *  @param outputFormat Format of the result file.
     *  @param processingMode The mode of processing for running the executable result file of building the Modelica model.
     *  @exception ConnectException If commands couldn't
     *   be sent to the (OpenModelic Compiler)OMC.
     *  @exception IOException If the executable result file of buildModel()
     *   couldn't be executed.
     *  @exception IllegalActionException
     */
    public void runModel(String fileName, String modelName, String startTime,
            String stopTime, int numberOfIntervals, String outputFormat,
            String processingMode)
            throws ConnectException, IOException, IllegalActionException;

    /** Leave and quit OpenModelica environment.
     *  @exception ConnectException If quit command couldn't
     *  be sent to the OMC.
     */
    public void stopServer() throws ConnectException;
}
