/* A port for use in component domains.

 Copyright (c) 2003-2014 The Regents of the University of California.
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

 */
package ptolemy.component;

import java.util.Iterator;

import ptolemy.data.TupleToken;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.util.CrossRefList;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// MethodCallPort

/**
 A port for use in the component domain.

 @author Yang Zhao
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating yellow(cxh)
 @Pt.AcceptedRating red(cxh)
 */
public class MethodCallPort extends ComponentPort {
    /** Construct a port in the default workspace with an empty string
     *  as its name. Increment the version number of the workspace.
     *  The object is added to the workspace directory.
     */
    public MethodCallPort() {
        super();
    }

    /** Construct a port in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the port.
     */
    public MethodCallPort(Workspace workspace) {
        super(workspace);
    }

    /** Construct a port with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This port will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty
     *  string.  Increment the version of the workspace.
     *  @param container The container entity.
     *  @param name The name of the port.
     *  @exception IllegalActionException If the port is not of an acceptable
     *   class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   a port already in the container.
     */
    public MethodCallPort(ComponentEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /** Construct an IOPort with a container and a name that is
     *  either an input, an output, or both, depending on the third
     *  and fourth arguments. The specified container must implement
     *  the Actor interface or an exception will be thrown.
     *
     *  @param container The container actor.
     *  @param name The name of the port.
     *  @param isProvidedPort True if this port provides the method.
     *  @exception IllegalActionException If the port is not of an acceptable
     *   class for the container, or if the container does not implement the
     *   Actor interface.
     *  @exception NameDuplicationException If the name coincides with
     *   a port already in the container.
     */
    public MethodCallPort(ComponentEntity container, String name,
            boolean isProvidedPort) throws IllegalActionException,
            NameDuplicationException {
        this(container, name);
        _isProvider = isProvidedPort;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a new port with no connections and no container.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If one or more of the
     *   attributes cannot be cloned.
     *  @return A new ComponentPort.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        MethodCallPort newObject = (MethodCallPort) super.clone(workspace);
        newObject._insideLinks = new CrossRefList(newObject);
        return newObject;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Call the method associated with this port with the specified
     *  arguments.
     *  <p>
     *  If this port is a provider, as indicated by @link{#isProvider()},
     *  then this method returns TupleToken.VOID, an empty tuple token.
     *  Subclasses should override this method to perform whatever
     *  functionality is associated with this method.
     *  <p>
     *  If this port is not a provider of this method, then this method
     *  delegates the call to all ports to which this port is
     *  deeply connected that are providers.  The order in which those
     *  calls are performed is determined by the order in which connections
     *  are made. The returned token is a concatenation of the returned
     *  values of all the called methods. If there is nothing connected,
     *  then this method will return TupleToken.VOID.
     *  @param arguments The arguments to the method.
     *  @see #isProvider()
     *  @see ptolemy.data.TupleToken#VOID
     */
    public synchronized TupleToken call(TupleToken arguments) {
        if (!isProvider()) {
            Iterator ports = this.deepConnectedPortList().iterator();
            TupleToken result = TupleToken.VOID;

            while (ports.hasNext()) {
                MethodCallPort port = (MethodCallPort) ports.next();

                if (port.isProvider()) {
                    result = TupleToken.merge(result, port.call(arguments));
                }
            }

            return result;
        }

        // The port provided should over write this method.
        return TupleToken.VOID;
    }

    /** Return true if this port provides the method,
     *  vs. requires the method.  By default, this method returns
     *  false, meaning that the port requires rather than provides
     *  the method.
     *  @see #setProvider(boolean)
     *  @see #MethodCallPort(ComponentEntity, String, boolean)
     *  @see #call(TupleToken)
     *  @return True if this port provides the method.
     */
    public boolean isProvider() {
        return _isProvider;
    }

    /** Call with argument true to specify that this port provides the method,
     *  and call with argument false to specify that it requires the method.
     *  @see #isProvider()
     *  @see #MethodCallPort(ComponentEntity, String, boolean)
     *  @see #call(TupleToken)
     */
    public void setProvider(boolean isProvider) {
        _isProvider = isProvider;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Indicator of whether this port provides the method. */
    private boolean _isProvider = false;
}
