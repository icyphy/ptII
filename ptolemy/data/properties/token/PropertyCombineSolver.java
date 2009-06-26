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

import java.util.HashMap;
import java.util.Map;

import ptolemy.actor.gui.style.TextStyle;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.properties.Property;
import ptolemy.data.properties.PropertyHelper;
import ptolemy.data.properties.PropertySolver;
import ptolemy.data.properties.gui.PropertySolverGUIFactory;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;

public class PropertyCombineSolver extends PropertySolver {

    public PropertyCombineSolver(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        _propertyName = new StringParameter(this, "propertyName");
        _propertyName.setExpression("combinedValueToken");
        _propertyName.setVisibility(Settable.NOT_EDITABLE);

        _propertyExpression = new StringParameter(this, "propertyExpression");
        _propertyExpression.setExpression("");
        TextStyle style = new TextStyle(_propertyExpression, "_style");
        style.height.setExpression("10");
        style.width.setExpression("80");
        /*
                _propertyEmptyString = new StringParameter(this, "propertyEmptyString");
                _propertyEmptyString.setExpression("");
        */
        _inputPorts = new Parameter(this, "inputPorts");
        _inputPorts.setTypeEquals(BaseType.BOOLEAN);
        _inputPorts.setExpression("true");

        _outputPorts = new Parameter(this, "outputPorts");
        _outputPorts.setTypeEquals(BaseType.BOOLEAN);
        _outputPorts.setExpression("true");

        _unconnectedPorts = new Parameter(this, "ignore unconnected Ports");
        _unconnectedPorts.setTypeEquals(BaseType.BOOLEAN);
        _unconnectedPorts.setExpression("true");
        /*
                _atomicActors = new Parameter(this, "atomicActors");
                _atomicActors.setTypeEquals(BaseType.BOOLEAN);
                _atomicActors.setExpression("true");

                _compositeActors = new Parameter(this, "compositeActors");
                _compositeActors.setTypeEquals(BaseType.BOOLEAN);
                _compositeActors.setExpression("false");
        */
        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-50\" y=\"-20\" width=\"115\" height=\"40\" "
                + "style=\"fill:red\"/>" + "<text x=\"-40\" y=\"-5\" "
                + "style=\"font-size:12; font-family:SansSerif; fill:white\">"
                + "Double click to\nResolve Property.</text></svg>");

        new PropertySolverGUIFactory(this, "_portValueSolverGUIFactory");

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Returns the helper that contains property information for
     * the given object.
     * @param object The given object.
     * @return The associated property constraint helper.
     */
    public PropertyHelper getHelper(Object object)
            throws IllegalActionException {
        return _getHelper(object);
    }

    protected void _resolveProperties(NamedObj analyzer) throws KernelException {

        super._resolveProperties(analyzer);

        PropertyCombineCompositeHelper topLevelHelper = (PropertyCombineCompositeHelper) _getHelper(_toplevel());

        topLevelHelper.reinitialize();

        topLevelHelper.determineProperty();

    }

    public String getUseCaseName() {
        return _propertyName.getExpression();
    }

    public String getExtendedUseCaseName() {
        return "token::" + getUseCaseName();
    }

    /*
        public String getPropertyEmptyString() {
            return _propertyEmptyString.getExpression();
        }
    */
    //FIXME: only use method from base class?
    public Property getProperty(Object object) {
        if (object instanceof PortParameter) {
            return super.getProperty(((PortParameter) object).getPort());
        }
        return super.getProperty(object);
    }

    protected StringParameter _propertyName;
    protected StringParameter _propertyExpression;
    protected StringParameter _propertyEmptyString;
    protected Parameter _inputPorts;
    protected Parameter _outputPorts;
    protected Parameter _atomicActors;
    protected Parameter _compositeActors;
    protected Parameter _unconnectedPorts;

    public String getPropertyExpression() {
        return _propertyExpression.getExpression();
    }

    public Boolean getInputPorts() {
        return (_inputPorts.getExpression().equalsIgnoreCase("true")) ? true
                : false;
    }

    public Boolean getOutputPorts() {
        return (_outputPorts.getExpression().equalsIgnoreCase("true")) ? true
                : false;
    }

    public Boolean getUnconnectedPorts() {
        return (_unconnectedPorts.getExpression().equalsIgnoreCase("true")) ? true
                : false;
    }

    public Property getResolvedProperty(Object object) {
        return getToken(object);
    }

    public void putToken(Object object, PropertyToken token) {
        _tokenMap.put(object, token);
    }

    public PropertyToken getToken(Object object) {
        return _tokenMap.get(object);
    }

    /*
    public Boolean getAtomicActors() {
        return (_atomicActors.getExpression().equalsIgnoreCase("true")) ? true : false;
    }

    public Boolean getCompositeActors() {
        return (_compositeActors.getExpression().equalsIgnoreCase("true")) ? true : false;
    }
    */
    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    private Map<Object, PropertyToken> _tokenMap = new HashMap<Object, PropertyToken>();

}
