/* An OpenModelica Director based on the content of ContinuousDirector.

Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 2012 The Regents of the University of California.
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

import ptolemy.domains.continuous.kernel.ContinuousDirector;
import ptolemy.domains.openmodelica.lib.compiler.ConnectException;
import ptolemy.domains.openmodelica.lib.omc.OMCLogger;
import ptolemy.domains.openmodelica.lib.omc.OMCProxy;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/** 
   This director executes OpenModelica actor in its own threads.
   Creating and starting the threads are at the same time with
   starting the OpenModelica Compiler(OMC) which occurred in the
   preinitialize() method.  This threads finish in the wrapup()
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

    /** Get the private object of OMCLogger. 
     *  OMC stands for OpenModelica Compiler.
     */
    public static OMCLogger getOMCLogger() {
        return _omcLogger;
    }

    /** Get the private object of OMCProxy. 
     *  OMC stands for OpenModelica Compiler.
     *  @return the proxy object to the OpenModelica compiler.
     */
    public static OMCProxy getOMCProxy() {
        return _omcProxy;
    }

    /** Invoke the preinitialize() of the super class.  Preinitialize
     *  the OpenModelica actor and initialize the OpenModelica
     *  Compiler(OMC).
     *  @exception IllegalActionException If the preinitialize() of
     *  one of the associated actors throws it.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        try {
            _omcLogger = new OMCLogger();
            _omcProxy = new OMCProxy();
            _omcProxy.init();
            if (_debugging) {
                _debug("OpenModelica server is intialized.");
            }
        } catch (ConnectException ex) {
            throw new IllegalActionException(this, ex,
                    "Unable to start the OpenModelica server!");
        }
    }

    /** Invoke the wrapup() of the super class. 
     *  Leave and quit OpenModelica environment.
     *  OMCProxy and OMCLogger objects are reset.
     *  @exception IllegalActionException If the wrapup() of
     *  OpenModelica actor throws it.
     */
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        try {
            _omcProxy.quit();
            if (_debugging) {
                _debug("OpenModelica server quits.");
            }
            _omcProxy = null;
            _omcLogger = null;
        } catch (ConnectException ex) {
            _omcLogger
                    .getInfo("Ignore this exception for quit(), it is already quited!");
            ex = new ConnectException(
                    "Ignore this exception for quit(), it is already quited!");
            ex.printStackTrace();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variable                  ////

    /** The OpenModelica Compiler(OMC) log file in the temporary
     * folder.
     */
    private static OMCLogger _omcLogger;

    /** Used for initializing and stopping OpenModelica
     * Compiler(OMC) server.
     */
    private static OMCProxy _omcProxy;
}
