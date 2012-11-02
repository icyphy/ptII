/* Instantiate a Functional Mock-up Unit (FMU) for SDF.

   Copyright (c) 2011-2012 The Regents of the University of California.
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
package ptolemy.actor.lib.fmi;

import java.io.File;
import java.io.IOException;

import org.ptolemy.fmi.FMICallbackFunctions;
import org.ptolemy.fmi.FMILibrary;
import org.ptolemy.fmi.FMIModelDescription;
import org.ptolemy.fmi.FMIScalarVariable;
import org.ptolemy.fmi.FMIScalarVariable.Alias;
import org.ptolemy.fmi.FMIScalarVariable.Causality;
import org.ptolemy.fmi.FMUFile;
import org.ptolemy.fmi.FMULibrary;
import org.ptolemy.fmi.type.FMIBooleanType;
import org.ptolemy.fmi.type.FMIIntegerType;
import org.ptolemy.fmi.type.FMIRealType;
import org.ptolemy.fmi.type.FMIStringType;
import org.ptolemy.fmi.type.FMIType;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;

import com.sun.jna.Function;
import com.sun.jna.Pointer;

///////////////////////////////////////////////////////////////////
//// FMUImport

/**
 * Invoke a Functional Mock-up Interface (FMI) 1.0 Model Exchange 
 * Functional Mock-up Unit (FMU).
 * 
 * <p>Read in a <code>.fmu</code> file named by the 
 * <i>fmuFile</i> parameter.  The <code>.fmu</code> file is a zipped
 * file that contains a file named <code>modelDescription.xml</code>
 * that describes the ports and parameters that are created.
 * At run time, method calls are made to C functions that are
 * included in shared libraries included in the <code>.fmu</code>
 * file.</p>
 * 
 * <p>To use this actor from within Vergil, use Import -&gt; Import
 * FMU, which will prompt for a .fmu file. This actor is <b>not</b>
 * available from the actor pane via drag and drop. The problem is
 * that dragging and dropping this actor ends up trying to read
 * fmuImport.fmu, which does not exist.  If we added such a file, then
 * dragging and dropping the actor would create an arbitrary actor
 * with arbitrary ports.</p>
 *
 * <p>FMI documentation may be found at
 * <a href="http://www.modelisar.com/fmi.html">http://www.modelisar.com/fmi.html</a>.
 * </p>
 * 
 * @deprecated This is a SDF-specific actor to be used for testing.  The real actor will be FMUImport.java
 * @author Christopher Brooks, Michael Wetter, Edward A. Lee, 
 * @version $Id: FMUImport.java 64206 2012-08-06 05:39:20Z cxh $
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class FMUSDFImport extends FMUImport {
    // FIXME: For FMI Co-simulation, we want to extend TypedAtomicActor.
    // For model exchange, we want to extend TypedCompositeActor.

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public FMUSDFImport(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /** Read data from output ports, set the input ports and invoke
     * fmiDoStep() of the slave fmu.
     *   
     * <p>Note that we get the outputs <b>before</b> invoking
     * fmiDoStep() of the slave fmu so that we can get the data for
     * time 0.  This is done so that FMUs can share initialization
     * data if necessary.  For details, see the Section 3.4, Pseudo
     * Code Example in the FMI-1.0 Co-simulation Specification at
     * <a href="http://www.modelisar.com/specifications/FMI_for_CoSimulation_v1.0.pdf">http://www.modelisar.com/specifications/FMI_for_CoSimulation_v1.0.pdf</a>.
     * For an explanation, see figure 4 of
     * <br>
     * Michael Wetter,
     * "<a href="http://dx.doi.org/10.1080/19401493.2010.518631">Co-simulation of building energy and control systems with the Building Controls Virtual Test Bed</a>,"
     * Journal of Building Performance Simulation, Volume 4, Issue 3, 2011.
     *
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        // Don't call super.fire(), it invokes Continuous domain code.
        // super.fire();
        if (_debugging) {
            _debug("FMUImport.fire()");
        }

        String modelIdentifier = _fmiModelDescription.modelIdentifier;

        // Ptolemy parameters are read in initialize() because the fmi
        // version of the parameters must be written before
        // fmiInitializeSlave() is called.

        ////////////////
        // Iterate through the scalarVariables and get all the outputs.
        // See the method comment for why we do this before calling fmiDoStep()
        for (FMIScalarVariable scalarVariable : _fmiModelDescription.modelVariables) {
            if (_debugging) {
                _debug("FMUImport.fire(): " + scalarVariable.name);
            }
            if (scalarVariable.alias != null
                    && scalarVariable.alias != Alias.noAlias) {
                // If the scalarVariable has an alias, then skip it.
                // In bouncingBall.fmu, g has an alias, so it is skipped.
                continue;
            }

            if (scalarVariable.variability != FMIScalarVariable.Variability.parameter) {
                TypedIOPort port = (TypedIOPort) getPort(scalarVariable.name);

                if (port == null || port.getWidth() <= 0) {
                    // Either it is not a port or not connected.
                    // Check to see if we should update the parameter.
                    String sanitizedName = StringUtilities.sanitizeName(scalarVariable.name);
                    Parameter parameter = (Parameter)getAttribute(sanitizedName, Parameter.class);
                    if (parameter != null) {
                        _setParameter(parameter, scalarVariable);
                    }
                    continue;
                }

                Token token = null;

                if (scalarVariable.type instanceof FMIBooleanType) {
                    boolean result = scalarVariable.getBoolean(_fmiComponent);
                    token = new BooleanToken(result);
                } else if (scalarVariable.type instanceof FMIIntegerType) {
                    // FIXME: handle Enumerations?
                    int result = scalarVariable.getInt(_fmiComponent);
                    token = new IntToken(result);
                } else if (scalarVariable.type instanceof FMIRealType) {
                    double result = scalarVariable.getDouble(_fmiComponent);
                    token = new DoubleToken(result);
                } else if (scalarVariable.type instanceof FMIStringType) {
                    String result = scalarVariable.getString(_fmiComponent);
                    token = new StringToken(result);
                } else {
                    throw new IllegalActionException("Type "
                            + scalarVariable.type + " not supported.");
                }

                if (_debugging) {
                    _debug("FMUImport.fire(): " + scalarVariable.name + " "
                            + token + " " + scalarVariable.causality + " "
                            + Causality.output);
                }
                port.send(0, token);
            }
        }

        ////////////////
        // Iterate through the scalarVariables and set all the inputs.
        for (FMIScalarVariable scalarVariable : _fmiModelDescription.modelVariables) {
            // FIXME: Page 27 of the FMI-1.0 CS spec says that for
            // variability==parameter and causality==input, we can
            // only call fmiSet* between fmiInstantiateSlave() and
            // fmiInitializeSlave()

            // However, the example on p32 has fmiSetReal called
            // inside the while() loop?

            if (_debugging) {
                _debug("FMUImport.fire(): " + scalarVariable.name);
            }
            if (scalarVariable.alias != null
                    && scalarVariable.alias != Alias.noAlias) {
                // If the scalarVariable has an alias, then skip it.
                // In bouncingBall.fmu, g has an alias, so it is skipped.
                continue;
            }

            if (scalarVariable.variability != FMIScalarVariable.Variability.parameter) {
                if (scalarVariable.causality != Causality.input) {
                    continue;
                }
                TypedIOPort port = (TypedIOPort) getPort(scalarVariable.name);

                if (port != null && port.hasToken(0)) {
                    Token token = port.get(0);
                    _setScalarVariable(scalarVariable, token);
                }
            }
        }

        ////////////////
        // Call fmiDoStep() with the current data.

        // FIXME: FMI-1.0 uses doubles for time.
        double time = getDirector().getModelTime().getDoubleValue();

        // FIXME: depending on SDFDirector here.
        double stepSize = ((ptolemy.domains.sdf.kernel.SDFDirector) getDirector())
                .periodValue();

        if (_debugging) {
            _debug("FMIImport.fire(): about to call " + modelIdentifier
                    + "_fmiDoStep(Component, /* time */ " + time
                    + ", /* stepSize */" + stepSize + ", 1)");

        }

        int fmiFlag = ((Integer) _fmiDoStep.invokeInt(new Object[] {
                _fmiComponent, time, stepSize, (byte) 1 })).intValue();

        if (fmiFlag != FMILibrary.FMIStatus.fmiOK) {
            throw new IllegalActionException(this, "Could not simulate, "
                    + modelIdentifier + "_fmiDoStep(Component, /* time */ "
                    + time + ", /* stepSize */" + stepSize + ", 1) returned "
                    + fmiFlag);
        }

        if (_debugging) {
            _debug("FMUImport done calling " + modelIdentifier + "_fmiDoStep()");
        }

    }
}