/* An OpenModelica Director based on the content of ContinuousDirector.

Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 2012-2013 The Regents of the University of California.
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
 */
package ptolemy.domains.openmodelica.kernel;

import ptolemy.actor.util.Time;
import ptolemy.domains.continuous.kernel.ContinuousDirector;
import ptolemy.domains.openmodelica.lib.omc.ConnectException;
import ptolemy.domains.openmodelica.lib.omc.OMCLogger;
import ptolemy.domains.openmodelica.lib.omc.OMCProxy;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/** 
   This director executes OpenModelica actor in its own threads.
   Creating and starting the threads are at the same time with
   starting the OpenModelica Compiler(OMC) which occurred in the
   initialize() method.  This threads finish in the wrapup()
   method, at the same time with quiting the OMC.

   @author Mana Mirzaei, Based on ContinuousDirector by Edward A. Lee
   @version $Id$
   @since Ptolemy II 9.1
   @Pt.ProposedRating Red (cxh)
   @Pt.AcceptedRating Red (cxh)
 */
public class OpenModelicaDirector extends ContinuousDirector {
    /** Construct a director in the given container with the given name.
     *  The container argument must not be null or a NullPointerException
     *  will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. All the parameters take their default values.
     *  @param container The container.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *   with the specified container. May be thrown by a derived class.
     *  @exception NameDuplicationException If the name collides with
     *   a property in the container.
     */
    public OpenModelicaDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                ////

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException Not thrown in this base class
     *  @return The new Attribute.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        OpenModelicaDirector newObject = (OpenModelicaDirector) super
                .clone(workspace);
        try {
            newObject._omcLogger = OMCLogger.getInstance();
            newObject._omcProxy = OMCProxy.getInstance();
        } catch (Throwable throwable) {
            throw new CloneNotSupportedException("Could not clone "
                    + getFullName() + ": " + throwable);
        }
        return newObject;
    }

    /** Invoke the preinitialize() of the super class.  Preinitialize
     *  the OpenModelica actor and initialize the OpenModelica
     *  Compiler(OMC).
     *  @throws IllegalActionException If the preinitialize() of
     *  one of the associated actors throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        try {

            // Create a unique instance of OMCLogger and OMCProxy.
            _omcLogger = OMCLogger.getInstance();
            _omcProxy = OMCProxy.getInstance();
            _omcProxy.initServer();
        } catch (ConnectException e) {
            e.printStackTrace();
            throw new IllegalActionException(
                    "Unable to start the OpenModelica server!" + e.getMessage());
        }
    }

    /** Return false if a stop has been requested or
     *  if the system has finished executing.
     *  @return Check if the director has detected a stop has been requested return false.
     *  @throws IllegalActionException Not thrown in this base class.
     */
    public boolean postfire() throws IllegalActionException {

        Time stopTime = getModelStopTime();

        if (_debugging) {
            _debug("OpenModelicaDirector: Called postfire().");
        }

        // If the stop time is infinite, then stop execution.
        if (stopTime == Time.POSITIVE_INFINITY) {
            stop();
            return false;
        } else {
            return true;
        }

    }

    /** Invoke the wrapup() of the super class. 
     *  Leave and quit OpenModelica environment.
     *  @throws IllegalActionException If the wrapup() of
     *  OpenModelica actor throws it.
     */
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        try {
            _omcProxy.quitServer();
            String loggerInfo = "OpenModelica Server stopped!";
            _omcLogger.getInfo(loggerInfo);
        } catch (ConnectException e) {
            throw new IllegalActionException(
                    "Unable to stop the OpenModelica server!" + e.getMessage());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variable                  ////
    // OMCLogger object for accessing a unique source of instance.
    private OMCLogger _omcLogger;

    // OMCProxy object for accessing a unique source of instance.
    private OMCProxy _omcProxy;

}
