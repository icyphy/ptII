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
 * to include an additional perameter, the error tolerance, which
 * is used by QSSIntegrators.
 * @author Thierry S. Nouidui based on DEDirector.java
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

    /** Error tolerance for QSS integration methods
     *  The default value is 1e-4, and the type is double.
     */
    public Parameter errorTolerance;

    /** The class name of the QSS solver used for integration.  This
     *  is a string that defaults to "QSS1".  Solvers are all required
     *  to be in package "org.ptolemy.qss".
     */
    public StringParameter QSSSolver;

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
        if (attribute == errorTolerance) {
            double value = ((DoubleToken) errorTolerance.getToken())
                    .doubleValue();
            if (value < 0.0) {
                throw new IllegalActionException(this,
                        "Cannot set a negative error tolerance.");
            }
            _errorTolerance = value;
        }
        super.attributeChanged(attribute);
    }

    /** Return the local truncation error tolerance.
     *  @return The local truncation error tolerance.
     */
    public final double getErrorTolerance() {
        // This method is final for performance reason.
        return _errorTolerance;
    }

    /** Return a QSS solver for use.
     *  @return A QSS solver.
     * @throws IllegalActionException 
     */
    public QSSBase get_QSSSolver() throws IllegalActionException {
        // FIXME: Rename to qssSolver().  As there is no setQSSSolver(),
        // there should be no setter.
        
        // Instantiate an QSS solver, using the class name given
        // by QSSSolver parameter, which is a string parameter.
        final String solverClassName = QSSSolver.stringValue().trim();
        // Instantiate the solver.
        return(_instantiateQSSSolver(_solverClasspath + solverClassName));
    }

    /////////////////////////////////////////////////////////////////////
    ////                  protected methods                        ////

    /** Instantiate an QSSSolver from its classname. Given the solver's full
     *  class name, this method will try to instantiate it by looking
     *  for the corresponding java class.
     *  This method is based on _instantiateOEDSolver of the CT domain.
     *  @param className The solver's full class name.
     *  @return a new QSS solver.
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
    ////                         private methods                 ////
    /** initialize parameters. Set all parameters to their default values.
     * @throws NameDuplicationException If adding parameters fails.
     * @throws IllegalActionException If setting parameters fails.
     */
    private void _initSolverParameters()
	    throws IllegalActionException, NameDuplicationException {
	_verbose = true;	// FIXME: Do we really want this?
	
	// The following is probably not needed.
	// The errors it catches only occur with interaction with
	// the continuous domain.
	enforceMicrostepSemantics.setVisibility(Settable.EXPERT);
	
	errorTolerance = new Parameter(this, "errorTolerance");
	errorTolerance.setExpression("1e-4");
	errorTolerance.setTypeEquals(BaseType.DOUBLE);
	QSSSolver = new StringParameter(this, "QSSSolver");
	QSSSolver.setExpression("QSS1");
	QSSSolver.addChoice("QSS1");
	QSSSolver.addChoice("QSS2Fd");
	QSSSolver.addChoice("QSS2Pts");
	QSSSolver.addChoice("QSS2Qts");
	QSSSolver.addChoice("QSS3Fd");
	QSSSolver.addChoice("QSS3Pts");
	QSSSolver.addChoice("LIQSS1");
	QSSSolver.addChoice("LIQSS2Fd");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                 ////
    /** The error tolerance for state resolution. */
    private double _errorTolerance;

    /** The package name for the solvers supported by this director. */
    private static String _solverClasspath = "org.ptolemy.qss.solver.";
}
