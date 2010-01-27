/*
Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 2008-2010 The Regents of the University of California.
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
package ptolemy.data.properties.token;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ptolemy.actor.IOPort;
import ptolemy.data.Token;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.PtParser;
import ptolemy.data.properties.ParseTreeAnnotationEvaluator;
import ptolemy.data.properties.Property;
import ptolemy.data.properties.PropertyHelper;
import ptolemy.data.properties.PropertySolverBase;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// PropertyCombineHelper
/** 

@author Man-Kit Leung
@version $Id$
@since Ptolemy II 7.1
@Pt.ProposedRating Red (mankit)
@Pt.AcceptedRating Red (mankit)
*/
public class PropertyCombineHelper extends PropertyHelper {

    /** 
     * Construct a PropertyCombinedHelper
     * @param solver The solver.
     * @param component The associated components.
     */
    public PropertyCombineHelper(PropertyCombineSolver solver, Object component) {
        setComponent(component);
        _solver = solver;
    }

    /**
     * Return The PropertySolver that uses this adapter.
     * @return The PropertySolver that uses this adapter.
     */
    public PropertyCombineSolver getSolver() {
        // FIXME: This is unusual because there is a getSolver()
        // method in the parent class that returns a PropertySolver.
        return (PropertyCombineSolver) _solver;
    }

    /** Determine the property.
     *  @exception IllegalActionException If thrown while generating  
     *  a parse tree for the solver, evaluating the parse tree or getting
     *  subadapters.
     */
    public void determineProperty() throws IllegalActionException {

        Iterator portIterator = getPropertyables().iterator();

        while (portIterator.hasNext()) {
            IOPort port = (IOPort) portIterator.next();

            // Get the shared parser.
            PtParser parser = PropertySolverBase.getParser();

            // create parse tree
            ASTPtRootNode parseTree = parser.generateParseTree(getSolver()
                    .getPropertyExpression());

            // do evaluation for port
            PropertyCombineParseTreeEvaluator evaluator = new PropertyCombineParseTreeEvaluator(
                    port, _solver);
            Token token = evaluator.evaluateParseTree(parseTree);
            PropertyToken property = new PropertyToken(token);
            if (!((getSolver().getUnconnectedPorts()) && port
                    .connectedPortList().isEmpty())) {
                setEquals(port, property);
            }
        }

        Iterator adapters = _getSubHelpers().iterator();
        while (adapters.hasNext()) {
            PropertyCombineHelper adapter = (PropertyCombineHelper) adapters
                    .next();
            adapter.determineProperty();
        }
    }

    /**
     * Return a list of property-able NamedObj contained by
     * the component. All ports and parameters are considered
     * property-able.
     * @return The list of property-able named object.
     */
    public List<Object> getPropertyables() {
        List<Object> list = new ArrayList<Object>();

        // Add all ports.
        list.addAll(((Entity) getComponent()).portList());

        return list;
    }

    /**
     * Set the property of specified object equal to the specified property.
     *
     * @param object The specified object.
     *
     * @param property The specified property.
     */
    public void setEquals(Object object, Property property) {
        // FIXME: Charles Shelton 05/27/09 - Thomas Mandl's code doesn't call super.setEquals.  We will keep it for now.
        super.setEquals(object, property);
        if (property != null) {
            if (property instanceof PropertyToken) {
                // Findbugs: BC: "Unchecked unconfirmed cast"
                getSolver().putToken(object, (PropertyToken) property);
            } else {
                throw new InternalErrorException((getComponent() instanceof NamedObj ? (NamedObj) getComponent() : null),
                        null,
                        "Property " +
                        property + " is not a PropertyToken, it is a "
                        + property.getClass().getName());
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Create an new ParseTreeAnnotationEvaluator that is tailored for the
     * use-case.
     * @return A new ParseTreeAnnotationEvaluator.
     */
    protected ParseTreeAnnotationEvaluator _annotationEvaluator() {
        return new ParseTreeAnnotationEvaluator();
    }

    /**
     * Return the list of sub-adapters.
     * @return The list of sub-adapters.
     * @exception IllegalActionException Not thrown in this class.
     */
    protected List<PropertyHelper> _getSubHelpers()
            throws IllegalActionException {
        return new ArrayList<PropertyHelper>();
    }

}
