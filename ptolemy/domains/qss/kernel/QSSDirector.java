/** A test director that extends QSSDirector.
 Copyright (c) 2015 The Regents of the University of California.
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
package ptolemy.domains.qss.kernel;

import org.ptolemy.qss.solver.QSSBase;

import ptolemy.data.DoubleToken;
import ptolemy.data.SmoothToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

/**
 * A director that extends the discrete-event model of computation
 * to include a Quantized-State System (QSS) solver to perform integration.
 * This solver performs using a discrete-event style, quantizing the magnitude
 * of signals rather than the time, as done by a conventional ODE solver. 
 * The <i>absoluteQuantum</i> and <i>relativeQuantum</i> parameters determine
 * the quantization granularity.  For information about QSS, see
 * {@link QSSIntegrator} and {@link QSSBase}.
 * <p>
 * Note that the expression function smoothToken(double, {double})
 * is available in the expression language. This function creates
 * a {@link SmoothToken} with value equal to the first argument and any number
 * of derivatives given in the second argument. Actors in this domain
 * can use such tokens, when they are provided, to perform more accurate
 * calculations.
 * @author Thierry S. Nouidui and Edward A. Lee
 * @version $Id$
 * @since Ptolemy 11.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class QSSDirector extends DEDirector {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *  @exception NameDuplicationException If construction of Time objects fails.
     *  @exception IllegalActionException If construction of Time objects fails.
     */
    public QSSDirector() throws IllegalActionException,
            NameDuplicationException {
        _initSolverParameters();
    }

    /** Construct a director in the workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace of this object.
     *  @exception NameDuplicationException If construction of Time objects fails.
     *  @exception IllegalActionException If construction of Time objects fails.
     */
    public QSSDirector(Workspace workspace) throws IllegalActionException,
            NameDuplicationException {
        super(workspace);
        _initSolverParameters();
    }

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the
     *   director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public QSSDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _initSolverParameters();
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The minimum quantum for QSS integrations under the control
     *  of this director. This is a double that defaults to 1E-4.
     */
    public Parameter absoluteQuantum;

    /** The class name of the QSS solver used for integration.  This
     *  is a string that defaults to "QSS1".  Solvers are all required
     *  to be in package "org.ptolemy.qss". Note that if the solver is
     *  changed during the execution of the model, the change will not
     *  take effect until the model re-initialized.
     */
    public StringParameter QSSSolver;

    /** The relative quantum to use for QSS integrations under the control
     *  of this director. If the value here is greater than zero,
     *  then the quantum will be the larger of the
     *  {@link #absoluteQuantum} and |x| * relativeQuantum,
     *  where x is the current value of the state being quantized.
     *  This is a double that defaults to be 0.0, which causes the
     *  absoluteQuantum to be used.
     */
    public Parameter relativeQuantum;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute. If the changed attribute
     *  matches a parameter of the director, then the corresponding
     *  local copy of the parameter value will be updated.
     *  @param attribute The changed parameter.
     *  @exception IllegalActionException If the new parameter value
     *  is not valid.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (_debugging) {
            _debug("attributeChanged: Updating QSSDirector parameter: "
                    + attribute.getName());
        }
        if (attribute == relativeQuantum) {
            double value = ((DoubleToken) relativeQuantum.getToken())
                    .doubleValue();
            if (value < 0.0) {
                throw new IllegalActionException(this,
                        "Cannot set a negative quantum.");
            }
            _relativeQuantum = value;
        } else if (attribute == absoluteQuantum) {
            double value = ((DoubleToken) absoluteQuantum.getToken())
                    .doubleValue();
            if (value <= 0.0) {
                throw new IllegalActionException(this,
                        "absoluteQuantum is required to be greater than 0.0.");
            }
            _absoluteQuantum = value;
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Set the default solver method and list solver alternatives for the specified solver.
     *  @param solverParameter The parameter specifying the solver method.
     *  @param defaultSolver The default solver to use.
     */
    public static void configureSolverParameter(
	    StringParameter solverParameter, String defaultSolver) {
	solverParameter.setExpression(defaultSolver);
	solverParameter.addChoice("QSS1");
	solverParameter.addChoice("QSS2Fd");
	solverParameter.addChoice("QSS2FdJac");
	solverParameter.addChoice("QSS2Pts");
	solverParameter.addChoice("QSS2Qts");
	solverParameter.addChoice("QSS3Fd");
	solverParameter.addChoice("QSS3Pts");
	solverParameter.addChoice("LIQSS1");
	solverParameter.addChoice("LIQSS2Fd");
    }

    /** Return the value of the absoluteQuantum parameter.
     *  @return The absolute quantum.
     */
    public final double getAbsoluteQuantum() {
        // This method is final for performance reason.
        return _absoluteQuantum;
    }

    /** Return the value of the relativeQuantum parameter.
     *  @return The relative quantum.
     */
    public final double getRelativeQuantum() {
        // This method is final for performance reason.
        return _relativeQuantum;
    }

    /** Return a new QSS solver for use.
     *  @return A QSS solver.
     *  @throws IllegalActionException If creating the solver fails.
     */
    public QSSBase newQSSSolver() throws IllegalActionException {
        // Instantiate an QSS solver, using the class name given
        // by QSSSolver parameter, which is a string parameter.
	return newQSSSolver(QSSSolver.stringValue().trim());
    }

    /** Return a new QSS solver of the specified type.
     *  @param type The type of solver.
     *  @return A QSS solver.
     *  @throws IllegalActionException If creating the solver fails.
     */
    public QSSBase newQSSSolver(String type) throws IllegalActionException {
        // Instantiate the solver.
        return(_instantiateQSSSolver(_solverClasspath + type));
    }

    /////////////////////////////////////////////////////////////////////
    ////                  protected methods                        ////

    /** Instantiate an QSSSolver from its class name. Given the solver's full
     *  class name, this method will try to instantiate it by looking
     *  for the corresponding java class.
     *  This method is based on _instantiateODESolver of the CT domain.
     *  @param className The solver's full class name.
     *  @return A new QSS solver.
     *  @exception IllegalActionException If the solver can not be created.
     */
    protected final QSSBase _instantiateQSSSolver(String className)
            throws IllegalActionException {

        // All solvers must be in the package given by _solverClasspath.
        if (!className.trim().startsWith(_solverClasspath)) {
            className = _solverClasspath + className;
        }

        if (_debugging) {
            _debug("instantiating solver..." + className);
        }

        QSSBase newSolver = null;

        try {
            Class solver = Class.forName(className);
            newSolver = (QSSBase) solver.newInstance();
        } catch (ClassNotFoundException e) {
            throw new IllegalActionException(this, "QSSSolver: " + className
                    + " is not found.");
        } catch (InstantiationException e) {
            throw new IllegalActionException(this, "QSSSolver: " + className
                    + " instantiation failed." + e);
        } catch (IllegalAccessException e) {
            throw new IllegalActionException(this, "QSSSolver: " + className
                    + " is not accessible.");
        }
        return newSolver;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    
    /** Initialize parameters. Set all parameters to their default values.
     *  @throws NameDuplicationException If adding parameters fails.
     *  @throws IllegalActionException If setting parameters fails.
     */
    private void _initSolverParameters()
	    throws IllegalActionException, NameDuplicationException {	
	// The following is probably not needed.
	// The errors it catches only occur with interaction with
	// the continuous domain.
	enforceMicrostepSemantics.setVisibility(Settable.EXPERT);
	
	absoluteQuantum = new Parameter(this, "absoluteQuantum");
	absoluteQuantum.setExpression("1e-4");
	absoluteQuantum.setTypeEquals(BaseType.DOUBLE);

	relativeQuantum = new Parameter(this, "relativeQuantum");
	relativeQuantum.setExpression("0.0");
	relativeQuantum.setTypeEquals(BaseType.DOUBLE);
	
	QSSSolver = new StringParameter(this, "QSSSolver");
	configureSolverParameter(QSSSolver, "QSS1");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                 ////
    
    /** The absolute quantum for state resolution. */
    private double _absoluteQuantum;

    /** The relative quantum for state resolution. */
    private double _relativeQuantum;
    
    /** The package name for the solvers supported by this director. */
    private static String _solverClasspath = "org.ptolemy.qss.solver.";
}
