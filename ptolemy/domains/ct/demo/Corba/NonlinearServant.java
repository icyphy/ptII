/* An actor that outputs the arctan of the input.

 Copyright (c) 1998-1999 The Regents of the University of California.
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
package ptolemy.domains.ct.demo.Corba;

import ptolemy.domains.ct.demo.Corba.util.*;

//////////////////////////////////////////////////////////////////////////
//// NonlinearServant
/**
This is a stateless function that does a nonlinear transformation of the
input data. This is a CORBA servant implement the
ptolemy.actor.corba.util.CorbaActor interface, served as a CORBA servant.
This is designed as independent of the ptolemy packages.

@author Jie Liu
@version $Id$
*/

public class NonlinearServant extends _CorbaActorImplBase {

    /** Construct the servant.
     */
    public NonlinearServant() {
        super();
        _input = null;
        _output = null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /**Implement
       <p>
       Operation: <b>::util::CorbaActor::fire</b>.
       <pre>
       #pragma prefix "util/CorbaActor"
       void fire(
       )
       raises(
       ::util::CorbaIllegalActionException
       );
       </pre>
       </p>
       Compute the arctan of the input.
    */
    public void fire( ) throws
            CorbaIllegalActionException {
        if (_input == null){
            _output = null;
            throw new CorbaIllegalActionException(
                    "No input data.");
        }
        _output = new Double(Math.atan(_input.doubleValue()));
        _input = null;
    }

    /** Throws CorbaUnknowParamException always, since there's
     *  no parameter defined in this actor. Implement
       <p>
       Operation: <b>::util::CorbaActor::getParameter</b>.
       <pre>
       #pragma prefix "util/CorbaActor"
       string getParameter(
       in string paramName
       )
       raises(
       ::util::CorbaIllegalActionException,
       ::util::CorbaUnknownParamException
       );
       </pre>
       </p>
    */
    public java.lang.String getParameter(
            java.lang.String paramName
            ) throws
            CorbaIllegalActionException,
            CorbaUnknownParamException {
        throw new CorbaUnknownParamException();
    }

    /** Do nothing.
       <p>
       Operation: <b>::util::CorbaActor::initialize</b>.
       <pre>
       #pragma prefix "util/CorbaActor"
       void initialize(
       )
       raises(
       ::util::CorbaIllegalActionException
       );
       </pre>
       </p>
    */
    public void initialize() throws
            CorbaIllegalActionException {
    }

    /** Return true if there's new data in the output port. Implement
       <p>
       Operation: <b>::util::CorbaActor::hasData</b>.
       <pre>
       #pragma prefix "util/CorbaActor"
       boolean hasData(
       in string portName,
       in short portIndex
       )
       raises(
       ::util::CorbaIllegalActionException,
       ::util::CorbaIndexOutofBoundException,
       ::util::CorbaUnknownPortException
       );
       </pre>
       </p>
       @exception CorbaIllegalActionException If port name is "input".
       @exception CorbaIndexOutofBoundException If index is not 0.
       @exception CorbaUnknownPortException If port name is not known.
    */
    public boolean hasData(
            java.lang.String portName,
            short portIndex
            ) throws
            CorbaIllegalActionException,
            CorbaIndexOutofBoundException,
            CorbaUnknownPortException {
        if (portName.equals("input")) {
            throw new CorbaIllegalActionException(
                    "port is an input");
        }
        if (!portName.equals("output")) {
            throw new CorbaUnknownPortException();
        }
        if (portIndex != 0) {
            throw new CorbaIndexOutofBoundException(portIndex);
        }
        return (_output != null);
    }

    /** Return false always, since there's no parameter defined.
     *  <p>
     *  Operation: <b>::util::CorbaActor::hasParameter</b>.
     *  <pre>
     *  #pragma prefix "util/CorbaActor"
     *  boolean hasParameter(
     *  in string paramName
     *  );
     *  </pre>
     *  </p>
     */
    public boolean hasParameter(
            java.lang.String paramName
            ) {
        return false;
    }

    /** Return true if the port is defined. This actor defines two ports
     *  named "input" and "output", respectively. Input is a single
     *  input port and output is a single output port. Implements
     *  <p>
     *  Operation: <b>::util::CorbaActor::hasPort</b>.
     *  <pre>
     *  #pragma prefix "util/CorbaActor"
     *  boolean hasPort(
     *  in string portName,
     *  in boolean isInput,
     *  in boolean isOutput,
     *  in boolean isMultiport
     *  );
     *  </pre>
     *  </p>
     */
    public boolean hasPort(
            java.lang.String portName,
            boolean isInput,
            boolean isOutput,
            boolean isMultiport
            ) {
        if (portName.equals("input") && isInput && !isOutput && !isMultiport) {
            return true;
        }  else if (portName.equals("output") && !isInput &&
                isOutput && !isMultiport) {
            return true;
        } else {
            return false;
        }
    }

    /** Throws CorbaIllegalActionException, since the port width is fixed.
     *  Implements
     *  <p>
     *  Operation: <b>::util::CorbaActor::setPortWidth</b>.
     *  <pre>
     *  #pragma prefix "util/CorbaActor"
     *  void setPortWidth(
     *  in string portName,
     *  in short width
     *  )
     *  raises(
     *  ::util::CorbaIllegalActionException,
     *  ::util::CorbaUnknownPortException
     *  );
     *  </pre>
     *  </p>
     *  @exception CorbaIllegalActionException Always.
     */
    public void setPortWidth(
            java.lang.String portName,
            short width
            ) throws
            CorbaIllegalActionException,
            CorbaUnknownPortException {
        if(width != 1) {
            throw new CorbaIllegalActionException(
                    " The port width is immutable.");
        }
    }

    /** Return true always. If there's input data, compute output data,
     *  otherwise do nothing. Implements
     *  <p>
     *  Operation: <b>::util::CorbaActor::postfire</b>.
     *  <pre>
     *  #pragma prefix "util/CorbaActor"
     *  boolean postfire(
     *  )
     *  raises(
     *  ::util::CorbaIllegalActionException
     *  );
     *  </pre>
     *  </p>
     *  @exception CorbaIllegalActionException Never thrown.
     */
    public boolean postfire( ) throws
            CorbaIllegalActionException {
        if (_input != null) {
            fire();
        }
        return true;
    }

    /** Return true always. If there's input data, compute output data,
     *  otherwise do nothing. Implements
     *  <p>
     *  Operation: <b>::util::CorbaActor::prefire</b>.
     *  <pre>
     *  #pragma prefix "util/CorbaActor"
     *  boolean prefire(
     *  )
     *  raises(
     *  ::util::CorbaIllegalActionException
     *  );
     *  </pre>
     *  </p>
     */
    public boolean prefire() throws
            CorbaIllegalActionException {
        return postfire();
    }

    /** Throws CorbaIllegalActionException always, since there's no
     *  parameters that is allows to set. Implements
     *  <p>
     *  Operation: <b>::util::CorbaActor::setParameter</b>.
     *  <pre>
     *  #pragma prefix "util/CorbaActor"
     *  void setParameter(
     *  in string paramName,
     *  in string paramValue
     *  )
     *  raises(
     *  ::util::CorbaIllegalActionException,
     *  ::util::CorbaUnknownParamException,
     *  ::util::CorbaIllegalValueException
     *  );
     *  </pre>
     *  </p>
     */
    public void setParameter(
            java.lang.String paramName,
            java.lang.String paramValue
            ) throws
            CorbaIllegalActionException,
            CorbaUnknownParamException,
            CorbaIllegalValueException {
        //throw new CorbaIllegalActionException(
        //        " No parameter is allowed to set.");
    }

    /** Do nothing. Return immediately. Implements
     *  <p>
     *  Operation: <b>::util::CorbaActor::stopFire</b>.
     *  <pre>
     *  #pragma prefix "util/CorbaActor"
     *  void stopFire(
     *  )
     *  raises(
     *  ::util::CorbaIllegalActionException
     *  );
     *  </pre>
     *  </p>
     */
    public void stopFire() throws
            CorbaIllegalActionException {
    }

    /** Do nothing. Implements
     *  <p>
     *  Operation: <b>::util::CorbaActor::terminate</b>.
     *  <pre>
     *  #pragma prefix "util/CorbaActor"
     *  void terminate(
     *  )
     *  raises(
     *  ::util::CorbaIllegalActionException
     *  );
     *  </pre>
     *  </p>
     */
    public void terminate() throws
            CorbaIllegalActionException {
    }

    /** Transfer the input data. The port name must be "input",
     *  the index must be 0, and the value must be able to convert
     *  to a double, otheriwise a corresponding exception will be thrown.
     *  Implement
     *  <p>
     *  Operation: <b>::util::CorbaActor::transferInput</b>.
     *  <pre>
     *  #pragma prefix "util/CorbaActor"
     *  void transferInput(
     *  in string portName,
     *  in short portIndex,
     *  in string tokenValue
     *  )
     *  raises(
     *  ::util::CorbaIllegalActionException,
     *  ::util::CorbaUnknownPortException,
     *  ::util::CorbaIndexOutofBoundException,
     *  ::util::CorbaIllegalValueException
     *  );
     *  </pre>
     *  </p>
     *  @exception CorbaIllegalActionException If port name is "output".
     *  @exception CorbaUnknownPortException If port name is not "input"
     *    nor output.
     *  @exception CorbaIndexOutofBoundException If the channel index != 0
     *  @exception CorbaIllegalValueException If the tokenValue is not
     *    able to convert to double.
     */
    public void transferInput(
            java.lang.String portName,
            short portIndex,
            java.lang.String tokenValue
            ) throws
            CorbaIllegalActionException,
            CorbaUnknownPortException,
            CorbaIndexOutofBoundException,
            CorbaIllegalValueException {
        if (portName.equals("output")) {
            throw new CorbaIllegalActionException(
                    " Cannot transfer input to an output port");
        }
        if (!portName.equals("input")) {
            throw new CorbaUnknownPortException();
        }
        if (portIndex != 0) {
            throw new CorbaIndexOutofBoundException(portIndex);
        }
        try {
            _input = new Double(tokenValue);
        } catch (NumberFormatException ex) {
            _input = null;
            throw new CorbaIllegalValueException(ex.getMessage());
        }
    }

    /** Transfer output data. The port name must be "output" and
     *  the index must be 0.  The hasData query will be false after
     *  transfering output. Implements
     *  <p>
     *  Operation: <b>::util::CorbaActor::transferOutput</b>.
     *  <pre>
     *  #pragma prefix "util/CorbaActor"
     *  string transferOutput(
     *  in string portName,
     *  in short portIndex
     *  )
     *  raises(
     *  ::util::CorbaIllegalActionException,
     *  ::util::CorbaUnknownPortException,
     *  ::util::CorbaIndexOutofBoundException
     *  );
     *  </pre>
     *  </p>
     *  @exception CorbaIllegalActionException If the port name is "input",
     *    or if there's no output data.
     *  @exception CorbaUnknownPortException If the port name is neither
     *    "output" nor "input".
     *  @exception CorbaIndexOutofBoundException If the port index is not 0.
     */
    public java.lang.String transferOutput(
            java.lang.String portName,
            short portIndex
            ) throws
            CorbaIllegalActionException,
            CorbaUnknownPortException,
            CorbaIndexOutofBoundException {
        if (portName.equals("input")) {
            throw new CorbaIllegalActionException(
                    " Cannot transfer output from an input port");
        }
        if (_output == null) {
            throw new CorbaIllegalActionException(
                    " No output data to transfer");
        }
        if (!portName.equals("output")) {
            throw new CorbaUnknownPortException();
        }
        if (portIndex != 0) {
            throw new CorbaIndexOutofBoundException(portIndex);
        }
        String outs = _output.toString();
        _output = null;
        return outs;
    }

    /** Do nothing. Implements
     *  <p>
     *  Operation: <b>::util::CorbaActor::wrapup</b>.
     *  <pre>
     *  #pragma prefix "util/CorbaActor"
     *  void wrapup(
     *  )
     *  raises(
     *  ::util::CorbaIllegalActionException
     *  );
     *  </pre>
     *  </p>
     */
    public void wrapup() throws
            CorbaIllegalActionException {
    }

    ///////////////////////////////////////////////////////////////////
    ////                      private variables                    ////

    private Double _input;
    private Double _output;
}
