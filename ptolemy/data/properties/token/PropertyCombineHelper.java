/*
Below is the copyright agreement for the Ptolemy II system.
Version: $Id$

Copyright (c) 2008-2009 The Regents of the University of California.
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
import ptolemy.data.properties.PropertySolver;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public class PropertyCombineHelper extends PropertyHelper {

    public PropertyCombineHelper(PropertyCombineSolver solver, Object component) {

        setComponent(component);
        _solver = solver;
    }

    public PropertyCombineSolver getSolver() {
        return (PropertyCombineSolver)_solver;
    }

    public void determineProperty()
    throws IllegalActionException, NameDuplicationException {

        Iterator portIterator = getPropertyables().iterator();

        while (portIterator.hasNext()) {
            IOPort port = (IOPort) portIterator.next();

            // Get the shared parser.
            PtParser parser = PropertySolver.getParser();

            // create parse tree
            ASTPtRootNode parseTree = parser.generateParseTree(getSolver().getPropertyExpression());

            // do evaluation for port
            PropertyCombineParseTreeEvaluator evaluator = new PropertyCombineParseTreeEvaluator(port, _solver);
            Token token = evaluator.evaluateParseTree(parseTree);
            PropertyToken property = (PropertyToken) new PropertyToken(token);
            if (!((getSolver().getUnconnectedPorts()) && port.connectedPortList().isEmpty())) {
                setEquals(port, property);
            }
        }

        Iterator helpers = _getSubHelpers().iterator();
        while (helpers.hasNext()) {
            PropertyCombineHelper helper =
                (PropertyCombineHelper) helpers.next();
            helper.determineProperty();
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

    public void setEquals(Object object, Property property) {
// FIXME: Charles Shelton 05/27/09 - Thomas Mandl's code doesn't call super.setEquals.  We will keep it for now.
        super.setEquals(object, property);
        if (property != null) {
            getSolver().putToken(object, (PropertyToken) property);
        }
    }

    @Override
    protected ParseTreeAnnotationEvaluator _annotationEvaluator() {
        return new ParseTreeAnnotationEvaluator();
    }

    protected List<PropertyHelper> _getSubHelpers() throws IllegalActionException {
        return new ArrayList<PropertyHelper>();
    }

}
