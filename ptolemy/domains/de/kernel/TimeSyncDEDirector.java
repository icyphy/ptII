/* A DE domain director that sync with a master clock.

 Copyright (c) 1998-2000 The Regents of the University of California.
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
@AcceptedRating Red (liuj@eecs.berkeley.edu)
*/

package ptolemy.domains.de.kernel;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.actor.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.graph.*;

import java.util.StringTokenizer;
import java.io.IOException;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.net.Socket;


//////////////////////////////////////////////////////////////////////////
//// DEDirector
//
/** This director extends the DEDirector to support a time synchronized
 *  distributed system. It is designed to be a run-time director.
 *  <P>
 *  The extension of this director w.r.t. the base DEDirector is a parameter
 *  'timeBaseHost', which takes an IP address.
 *  FIXME: consider network time protocol.
 *  This director maintains a 'timeOrigin', which is the network time
 *  corresponding to modeling time 0. To map the modeling time 't' to the
 *  network in other systems, use the fomular:
 *  <Pre>
 *      t+timeOrigin
 *  </pre>
 *
 *  The method getTimeOrigin() will return the time origin.
 *
 *
 *  @author Jie Liu
 *  @version $Id$
 *  @see DEDirector
 */
public class TimeSyncDEDirector extends DEDirector {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public TimeSyncDEDirector() {
	this(null);
    }

    /**  Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace of this object.
     */
    public TimeSyncDEDirector(Workspace workspace) {
        super(workspace);
        try {
            timeBaseHost = new Parameter(this, "timeBaseHost",
                    new StringToken("localhost"));
            timeBaseHost.setTypeEquals(BaseType.STRING);
            //delayTolerance = new Parameter(this, "delayTolerance",
            //        new DoubleToken(0.5));
            //delayTolerance.setTypeEquals(BaseType.DOUBLE);
        } catch (KernelException ex) {
            throw new InternalErrorException(ex.getMessage());
        }
    }

    /** Construct a director in the given container with the given name.
     *  If the container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the
     *   director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container is not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public TimeSyncDEDirector(CompositeEntity container , String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        try {
            timeBaseHost = new Parameter(this, "timeBaseHost",
                    new StringToken("localhost"));
            timeBaseHost.setTypeEquals(BaseType.STRING);
            //delayTolerance = new Parameter(this, "delayTolerance",
            //        new DoubleToken(0.5));
            //delayTolerance.setTypeEquals(BaseType.DOUBLE);
        } catch (KernelException ex) {
            throw new InternalErrorException(ex.getMessage());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The IP address of the time sync master.
     */
    public Parameter timeBaseHost;

    /** Time sync compensation. This value is added to the time origin
     *  so that the model time is always this amount ahead of the
     *  global time, in order to compensate the computation delay.
     *  In this design, the parameter is not suggested to be changeable
     *  at run time. The default is 500 milli-seconds0.5 seconds.
     *
    public Parameter delayTolerance;
    */

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** FIXME: Do we allow time base to change at run time?
     *  @param attribute The changed parameter.
     *  @exception IllegalActionException If the parameter set is not valid.
     *     Not thrown in this class. May be needed by derived classes.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        super.attributeChanged(attribute);
    }

    /** Clone the director into the specified workspace. This calls the
     *  base class and then copies the parameter of this director.  The new
     *  actor will have the same parameter values as the old.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If one of the attributes
     *   cannot be cloned.
     */
    public Object clone(Workspace ws)
            throws CloneNotSupportedException {
        TimeSyncDEDirector newobj = (TimeSyncDEDirector)(super.clone(ws));
        newobj.timeBaseHost =
            (Parameter)newobj.getAttribute("timeBaseHost");
        //newobj.delaTolerance =
        //    (Parameter)newobj.getAttribute("delayTolerance");
        try {
            newobj.timeBaseHost.setTypeEquals(BaseType.STRING);
            //newobj.delayTolerance.setTypeEquals(BaseType.DOUBLE);
        } catch (IllegalActionException ex) {
            throw new InternalErrorException("Set type error."
                    + ex.getMessage());
        }
        return newobj;
    }

    /** In addition to do the initialization work specified in the
     *  DEDirector, calculate the time origin.
     *
     *  @exception IllegalActionException If the initialize() method of
     *   one of the associated actors throws it.
     */
    public void initialize() throws IllegalActionException {
        // Uncomment to have a debug listener.
        //addDebugListener(new StreamListener());

        String host = timeBaseHost.getToken().stringValue();
        try {
            Socket socket = new Socket(host, 80);
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out =
                new DataOutputStream(socket.getOutputStream());
            out.writeBytes("get /bin/1451dot2/read"
                    + "\n");
            String form = in.readLine();
            StringTokenizer tokenizer = new StringTokenizer(form);
            if (tokenizer.countTokens() != 3) {
                throw new IOException(getFullName()
                        + ": Data received from "
                        + "NCAP are in unexpected format: " + form);
            }
            tokenizer.nextToken();
            tokenizer.nextToken();
            _timeOrigin = Math.floor(
                    Double.valueOf(tokenizer.nextToken()).doubleValue());
                    //    + ((DoubleToken)delayTolerance.getToken()).doubleValue());
System.out.println(getName() + ": time origin" + _timeOrigin);
        } catch (IOException ex) {
            throw new IllegalActionException(this, "output setup failure.");
        }
        super.initialize();
    }

    /** Return the time origin.
     */
    public double getTimeOrigin() {
        return _timeOrigin;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // time origin wrt the global time.
    private double _timeOrigin;


}
