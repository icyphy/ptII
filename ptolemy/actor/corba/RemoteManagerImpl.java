/* An actor that outputs the arctan of the input.

 Copyright (c) 1998-2001 The Regents of the University of California.
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

@ProposedRating Red (liuj@eecs.berkeley.edu)
@AcceptedRating Red (moderator@eecs.berkeley.edu)
*/
package ptolemy.actor.corba;

import ptolemy.actor.corba.RemoteManagerUtil.*;
import ptolemy.actor.Manager;
import ptolemy.actor.CompositeActor;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.filter.BackwardCompatibility;
import ptolemy.moml.filter.RemoveGraphicalClasses;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;

import java.io.File;


//////////////////////////////////////////////////////////////////////////
//// RemoteManagerImpl
/**
This is a stateless function that does a nonlinear transformation of the
input data. This is a CORBA servant implement the
ptolemy.actor.corba.util.CorbaActor interface, served as a CORBA servant.
This is designed as independent of the ptolemy packages.

@author Jie Liu
@version $Id$
*/

public class RemoteManagerImpl extends _RemoteManagerImplBase implements ChangeListener{

    /** Construct the servant.
     */
    public RemoteManagerImpl() {
        super();
        manager = null;
        //momlString = null;
    }

    public Manager manager;
    //public String momlString;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
/* Mirror the execute() method of Ptolemy II
		 * manager interface.
		 * @exception CorbaIllegalActionException If the
		 *   method is an illegal action of the actor.
		 */
  public void execute () throws ptolemy.actor.corba.RemoteManagerUtil.CorbaIllegalActionException{
    }

  /* Mirror the initialize() method of Ptolemy II
  		 * manager interface.
  		 * @exception CorbaIllegalActionException If the
  		 *   method is an illegal action of the actor.
  		 */
  public void initialize () throws ptolemy.actor.corba.RemoteManagerUtil.CorbaIllegalActionException
 {
  }

  /* Mirror the pause() method of Ptolemy II
  		 * manager interface.
  		 * @exception CorbaIllegalActionException If the
  		 *   method is an illegal action of the actor.
  		 */
  public void pause () throws ptolemy.actor.corba.RemoteManagerUtil.CorbaIllegalActionException
  {
  }

  /* Mirror the resume() method of Ptolemy II
  		 * manager interface.
  		 * @exception CorbaIllegalActionException If the
  		 *   method is an illegal action of the actor.
  		 */
  public void resume () throws ptolemy.actor.corba.RemoteManagerUtil.CorbaIllegalActionException
          {
  }

  /* Mirror the startRun() method of Ptolemy II
  		 * manager interface.
  		 * @exception CorbaIllegalActionException If the
  		 *   method is an illegal action of the actor.
  		 */
  public void startRun () throws ptolemy.actor.corba.RemoteManagerUtil.CorbaIllegalActionException{
      if (!_executing && manager != null ) {
      try {
          manager.startRun();
          _executing = true;
          System.out.println("---get start command");
      } catch (Exception ex) {
            System.err.println("---can't start to run the modeld: " + ex);
            //ex.printStackTrace();
        }
      }
  }

  /* Mirror the stop() method of Ptolemy II
  		 * manager interface.
  		 * @exception CorbaIllegalActionException If the
  		 *   method is an illegal action of the actor.
  		 */
  public void stop () throws ptolemy.actor.corba.RemoteManagerUtil.CorbaIllegalActionException
  {
      if (_executing && manager != null ) {
        manager.stop();
        _executing = false;
        System.out.println("---get stop command");
      }
  }
  /* Mirror the terminate() method of Ptolemy II
  		 * manager interface.
  		 * @exception CorbaIllegalActionException If the
  		 *   method is an illegal action of the actor.
  		 */
  public void terminate () throws ptolemy.actor.corba.RemoteManagerUtil.CorbaIllegalActionException {
  }

  /* Mirror the terminate() method of Ptolemy II
  		 * remoteManager interface.
		 * @exception CorbaIllegalActionException If the
		 *  query of parameter is not supported by the actor.
		 * @exception CorbaUnknowParamException If the parameter
		 *  name is not known by the actor.
		 */
  public void changeModel (String model) throws
          ptolemy.actor.corba.RemoteManagerUtil.CorbaIllegalActionException,
          ptolemy.actor.corba.RemoteManagerUtil.CorbaUnknownParamException
  {
    if (_executing && manager != null) {
        stop();
    }
    try {

    System.out.println("---get a moml string for a controller");
    MoMLParser parser = new MoMLParser();
    parser.setMoMLFilters(BackwardCompatibility.allFilters());

    // Filter out any graphical classes.
    parser.addMoMLFilter(new RemoveGraphicalClasses());

    _model = (CompositeActor) parser.parse(model);

    System.out.println("---parse the moml string correctly");
    manager = new Manager(_model.workspace(),
                "RemoteModel");
    _model.setManager(manager);
    _model.addChangeListener(this);
    System.out.println("---set manager for the controller");
    } catch (Exception ex) {
            System.err.println("---can't parse the moml string: " + ex);
            //ex.printStackTrace();
    }

  }


   /** React to a change request has been successfully executed by
     *  doing nothing. This method is called after a change request
     *  has been executed successfully.  In this class, we
     *  do nothing.
     *  @param change The change that has been executed, or null if
     *   the change was not done via a ChangeRequest.
     */
    public void changeExecuted(ChangeRequest change) {
    }

    /** React to a change request that has resulted in an exception.
     *  This method is called after a change request was executed,
     *  but during the execution in an exception was thrown.
     *  This method throws a runtime exception with a description
     *  of the original exception.
     *  @param change The change that was attempted or null if
     *   the change was not done via a ChangeRequest.
     *  @param exception The exception that resulted.
     */
    public void changeFailed(ChangeRequest change, Exception exception) {
        // If we do not implement ChangeListener, then ChangeRequest
        // will print any errors to stdout and continue.
        // This causes no end of trouble with the test suite

        // We can't throw and Exception here because this method in
        // the base class does not throw Exception.

	// In JDK1.4, we can construct exceptions from exceptions, but
	// not in JDK1.3.1
        //throw new RuntimeException(exception);

	throw new RuntimeException(exception.toString());
    }
  private CompositeActor _model;
  private boolean _executing = false;
}