/* A base class for applications that use the SDF domain.

 Copyright (c) 1999 The Regents of the University of California.
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

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY

@ProposedRating Red (vogel@eecs.berkeley.edu)
@AcceptedRating Red (vogel@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.gui;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.actor.gui.PtolemyApplication;
import ptolemy.domains.sdf.kernel.*;

//////////////////////////////////////////////////////////////////////////
//// SDFApplication
/**
A base class for applications that use the SDF domain. This is provided
for convenience, in order to promote certain common elements among
SDF applications. It is by no means required in order to create an 
application that uses the SDF domain. In particular, it creates 
and configures a director. It sets the iterations parameter. Note that
this class does not use any gui elements.

@author Brian K. Vogel
@version $Id$
*/
public class SDFApplication extends PtolemyApplication {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

   
    /** Set the number of iterations. If this method is not called, a
     *  default vaule of 1 is used. This method should be called prior
     *  to calling create(),  otherwise the default value of 1 iteration
     *  will be used.
     */
    public void setIterations(int numIterations) {
	this.iterations = numIterations;
    }

    /** Initialize the application. After calling the base class create() method,
     *  this method creates a director which is accessible
     *  to derived classes via a protected member.
     *  The <i>iterations</i> parameter
     *  sets the iterations parameter of the director.  This method
     *  also creates a scheduler associated with the director.
     */
    public void create() {
        super.create();

       

        try {
            // Initialization
            _director = new SDFDirector(_toplevel, "SDFDirector");
            Parameter iterparam = _director.iterations;
            iterparam.setToken(new IntToken(iterations));
            SDFScheduler scheduler = new SDFScheduler(_workspace);

            _director.setScheduler(scheduler);
            _director.setScheduleValid(false);
        } catch (Exception ex) {
            report("Failed to setup director and scheduler:\n", ex);
            
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////


    /** Execute the system for the number of iterations given by the
     *  _getIterations() method.
     *  @throws IllegalActionException Not thrown.
     */
    protected void _go() throws IllegalActionException {
        try {
            
            Parameter iterparam = _director.iterations;

            iterparam.setToken(new IntToken(iterations));
        } catch (Exception ex) {
            report("Unable to set number of iterations:\n", ex);
        }
        super._go();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected members                 ////

    /** The director for the top-level composite actor, created in the
     *  init() method.
     */
    protected SDFDirector _director;

  
    /** The number of iterations. The default value is 1.
     */
    protected int iterations = 1;
    
}
