/* IOPort for SDF

 Copyright (c) 1997-2003 The Regents of the University of California.
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

@ProposedRating Yellow (neuendor@eecs.berkeley.edu)
@AcceptedRating Yellow (johnr@eecs.berkeley.edu)

*/

package ptolemy.domains.sdf.kernel;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// SDFIOPort
/**
This class extends IOPort with convenience methods for handling the token
production and consumption rates.  These are merely convenience methods,
as the pertinent attributes can be added to any IOPort and the SDF domain
will respect them.
<p>
It is not recommended to use this port as a port for composite actors
because the presence of the rate parameters will prevent the inner SDF
scheduler from propagating it rates to the outside.  That is, if the
parameters are present, the scheduler does not override them.

@author Stephen Neuendorffer
@version $Id$
@since Ptolemy II 0.2
@deprecated It is prefereable to declare the rate parameters directly
in the actors, instead of using this class.  This allows the
dependence of rates to be understood by various SDF schedulers.
*/
public final class SDFIOPort extends TypedIOPort {

    /** Construct an SDFIOPort with no container and no name that is
     *  neither an input nor an output.
     */
    public SDFIOPort() {
        super();
        _initialize();
    }

    /** Construct a port in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument
     *  is null, then use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the port.
     */
    public SDFIOPort(Workspace workspace) {
        super(workspace);
        _initialize();
    }

    /** Construct an SDFIOPort with a containing actor and a name
     *  that is neither an input nor an output.  The specified container
     *  must implement the Actor interface, or an exception will be thrown.
     *
     *  @param container The container actor.
     *  @param name The name of the port.
     *  @exception IllegalActionException If the port is not of an acceptable
     *   class for the container, or if the container does not implement the
     *   Actor interface.
     *  @exception NameDuplicationException If the name coincides with
     *   a port already in the container.
     */
    public SDFIOPort(ComponentEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _initialize();
    }

    /** Construct an SDFIOPort with a container and a name that is
     *  either an input, an output, or both, depending on the third
     *  and fourth arguments. The specified container must implement
     *  the Actor interface or an exception will be thrown.
     *
     *  @param container The container actor.
     *  @param name The name of the port.
     *  @param isInput True if this is to be an input port.
     *  @param isOutput True if this is to be an output port.
     *  @exception IllegalActionException If the port is not of an acceptable
     *   class for the container, or if the container does not implement the
     *   Actor interface.
     *  @exception NameDuplicationException If the name coincides with
     *   a port already in the container.
     */
    public SDFIOPort(ComponentEntity container, String name,
            boolean isInput, boolean isOutput)
            throws IllegalActionException, NameDuplicationException {
        this(container, name);
        setInput(isInput);
        setOutput(isOutput);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public parameters                 ////

    /** The number of tokens consumed on this port each firing. */
    public Parameter tokenConsumptionRate;

    /** The number of tokens produced on this port during initialization. */
    public Parameter tokenInitProduction;

    /** The number of tokens produced on this port each firing. */
    public Parameter tokenProductionRate;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the port into the specified workspace. This calls the
     *  base class and then creates new parameters.  The new
     *  port will have the same parameter values as the old.
     *  @param workspace The workspace for the new object.
     *  @return A new SDFIOPort.
     *  @exception CloneNotSupportedException If one of the attributes
     *   cannot be cloned.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        SDFIOPort newObject = (SDFIOPort)(super.clone(workspace));
        newObject.tokenConsumptionRate =
            (Parameter)newObject.getAttribute("tokenConsumptionRate");
        newObject.tokenInitProduction =
            (Parameter)newObject.getAttribute("tokenInitProduction");
        newObject.tokenProductionRate =
            (Parameter)newObject.getAttribute("tokenProductionRate");
        return newObject;
    }

    /** Get the number of tokens that are consumed
     *  on every channel of this port.
     *
     *  @return The number of tokens consumed on this port, as specified in
     *  the tokenConsumptionRate Parameter.
     */
    public int getTokenConsumptionRate() throws IllegalActionException {
        return ((IntToken)tokenConsumptionRate.getToken()).intValue();
    }

    /** Get the number of tokens that are produced
     *  on this port during initialization.
     *
     *  @return The number of tokens produced on the port, as specified in
     *  the tokenInitProduction parameter.
     */
    public int getTokenInitProduction() throws IllegalActionException {
        return ((IntToken)tokenInitProduction.getToken()).intValue();
    }

    /** Get the number of tokens that are produced
     *  on the designated port of this Actor during each firing.
     *
     *  @return The number of tokens produced on the port, as specified in
     *  the tokenProductionRate parameter.
     */
    public int getTokenProductionRate() throws IllegalActionException {
        return ((IntToken)tokenProductionRate.getToken()).intValue();
    }

    /**
     * Set whether or not this port is an input.  In addition to the base
     * class operation, set the port rate parameters to reasonable values.
     * If setting the port to be an input, then set the consumption rate to
     * be 1.  If setting the port to not be an input, then set the consumption
     * rate to be 0.
     *  @exception IllegalActionException If changing the port status is
     *   not permitted (for example, the port status is fixed by a class
     *   definition).
     */
    public void setInput(boolean isInput) throws IllegalActionException {
        super.setInput(isInput);
        try {
            if (isInput) {
                tokenConsumptionRate.setToken(new IntToken(1));
            } else {
                tokenConsumptionRate.setToken(new IntToken(0));
            }
        } catch (Exception e) {
            // This should never happen
            throw new InternalErrorException(e.getMessage());
        }
    }

    /**
     * Set whether or not this port is an output.  In addition to the base
     * class operation, set the port rate parameters to reasonable values.
     * If setting the port to be an output, then set the consumption rate to
     * be 1.  If setting the port to not be an output, then set the consumption
     * rate to be 0.
     *  @exception IllegalActionException If changing the port status is
     *   not permitted (for example, the port status is fixed by a class
     *   definition).
     */
    public void setOutput(boolean isOutput) throws IllegalActionException {
        super.setOutput(isOutput);
        try {
            if (isOutput) {
                tokenProductionRate.setToken(new IntToken(1));
            } else {
                tokenProductionRate.setToken(new IntToken(0));
                tokenInitProduction.setToken(new IntToken(0));
            }
        } catch (Exception e) {
            // This should never happen.
            throw new InternalErrorException(e.getMessage());
        }
    }

    /** Set the number of tokens that are consumed
     *  on the appropriate port of this Actor during each firing
     *  by setting the value of the tokenConsumptionRate parameter.
     *
     *  @exception IllegalActionException If the rate is less than zero,
     *  or the port is not an input port.
     */
    public void setTokenConsumptionRate(int rate)
            throws IllegalActionException {
        if (rate < 0) throw new IllegalActionException(
                "Rate must be >= 0");
        if (!isInput()) throw new IllegalActionException(this, "Port " +
                "is not an input port.");
        tokenConsumptionRate.setToken(new IntToken(rate));
    }

    /** Set the number of tokens that are produced
     *  on the appropriate port of this Actor during initialize
     *  by setting the value of the tokenInitProduction parameter.
     *
     *  @exception IllegalActionException If the count is less than zero,
     *  or the port is not an output port.
     */
    public void setTokenInitProduction(int count)
            throws IllegalActionException {
        if (count < 0) throw new IllegalActionException(
                "Count must be >= 0");
        if (!isOutput()) throw new IllegalActionException(this, "Port " +
                "is not an Output Port.");
        tokenInitProduction.setToken(new IntToken(count));
    }

    /** Set the number of tokens that are produced
     *  on the appropriate port of this Actor during each firing
     *  by setting the value of the tokenProductionRate parameter.
     *
     *  @exception IllegalActionException If port is not contained
     *  in this actor, the rate is less than zero, or the port is
     *  not an output port.
     */
    public void setTokenProductionRate(int rate)
            throws IllegalActionException {
        if (rate < 0) throw new IllegalActionException(
                "Rate must be >= 0");
        if (!isOutput()) throw new IllegalActionException(this, "Port " +
                "is not an Output Port.");
        tokenProductionRate.setToken(new IntToken(rate));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Initialize local data members.
     */
    private void _initialize() {
        try {
            tokenConsumptionRate = new Parameter(this, "tokenConsumptionRate",
                    new IntToken(0));
            tokenInitProduction = new Parameter(this, "tokenInitProduction",
                    new IntToken(0));
            tokenProductionRate = new Parameter(this, "tokenProductionRate",
                    new IntToken(0));
        }
        catch (Exception e) {
            // This should never happen.
            throw new InternalErrorException(e.getMessage());
        }
    }
}
