/*
 * A property helper that is used by the PropertyTokenSolver.
 * 
 * Below is the copyright agreement for the Ptolemy II system.
 * 
 * Copyright (c) 2008-2009 The Regents of the University of California. All
 * rights reserved.
 * 
 * Permission is hereby granted, without written agreement and without license
 * or royalty fees, to use, copy, modify, and distribute this software and its
 * documentation for any purpose, provided that the above copyright notice and
 * the following two paragraphs appear in all copies of this software.
 * 
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
 * CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN
 * "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO PROVIDE
 * MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 */
package ptolemy.data.properties.token;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.properties.ParseTreeAnnotationEvaluator;
import ptolemy.data.properties.PropertyHelper;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.KernelException;

/**
 * A property helper that is used by the PropertyTokenSolver.
 * 
 * @author Man-Kit Leung
 * @version $Id$
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 */
public class PropertyTokenHelper extends PropertyHelper {

    public PropertyTokenHelper(PropertyTokenSolver solver, Object component) {

        setComponent(component);
        _solver = solver;
    }

    public PropertyTokenSolver getSolver() {
        return (PropertyTokenSolver) _solver;
    }

    public void addListener(boolean listenInputs, boolean listenOutputs)
            throws IllegalActionException {
        Iterator propertyables = getPropertyables().iterator();
        while (propertyables.hasNext()) {
            Object propertyable = propertyables.next();

            if (propertyable instanceof IOPort) {
                IOPort port = (IOPort) propertyable;
                if (listenOutputs && port.isOutput()) {

                    port.addIOPortEventListener(getSolver().getSentListener());
                }

                if (listenInputs && port.isInput()) {

                    port.addIOPortEventListener(getSolver().getGotListener());
                }
            }
        }
    }

    public void removeListener(boolean listenInputs, boolean listenOutputs)
            throws IllegalActionException {
        Iterator propertyables = getPropertyables().iterator();
        while (propertyables.hasNext()) {
            Object propertyable = propertyables.next();

            if (propertyable instanceof IOPort) {
                IOPort port = (IOPort) propertyable;

                if (listenOutputs && port.isOutput()) {
                    port.removeIOPortEventListener(getSolver()
                            .getSentListener());
                }

                if (listenInputs && port.isInput()) {
                    port
                            .removeIOPortEventListener(getSolver()
                                    .getGotListener());
                }
            }
        }
    }

    public void determineProperty() throws IllegalActionException,
            KernelException {
        // determine ASTNodeHelpers
        List<IOPort> inputPortList = new ArrayList<IOPort>();
        List<IOPort> outputPortList = new ArrayList<IOPort>();
        List<Attribute> attributeList = new ArrayList<Attribute>();

        // separate propertyables
        Iterator propertyableIterator = getPropertyables().iterator();
        while (propertyableIterator.hasNext()) {
            Object propertyable = propertyableIterator.next();

            if (propertyable instanceof IOPort) {
                IOPort port = (IOPort) propertyable;
                // treat InOut ports as Out ports
                if (port.isInput() && !port.isOutput()) {
                    inputPortList.add(port);
                } else {
                    outputPortList.add(port);
                }
                //            } else if ((propertyable instanceof Attribute) && (!((propertyable instanceof StringAttribute)))) {
            } else if (propertyable instanceof Attribute
                    || propertyable instanceof PortParameter) {
                attributeList.add((Attribute) propertyable);
            } else {
                //FIXME: throw exception?
            }
        }

        // extract ASTHelpers from helperlist
        List<PropertyTokenASTNodeHelper> ASTHelperList = new ArrayList<PropertyTokenASTNodeHelper>();
        List<PropertyHelper> helperList = new ArrayList<PropertyHelper>();
        helperList.addAll(_getSubHelpers());

        Iterator helpers = helperList.iterator();
        while (helpers.hasNext()) {
            PropertyHelper helper = (PropertyHelper) helpers.next();
            if (helper instanceof PropertyTokenASTNodeHelper) {
                ASTHelperList.add((PropertyTokenASTNodeHelper) helper);
            }
        }

        // determine inputPorts first, ASTNodes may depend on it
        _determineInputPorts(inputPortList);

        // determine Attributes
        _determineAttributes(attributeList, ASTHelperList);

        _determineRefinement();

        // determine all subhelpers except attribute helpers
        helpers = helperList.iterator();
        while (helpers.hasNext()) {
            PropertyTokenHelper helper = (PropertyTokenHelper) helpers.next();
            if (!ASTHelperList.contains(helper)) {
                helper.determineProperty();
            }
        }

        // determine outputPorts last, may depend on inports and outports
        _determineOutputPorts(outputPortList);
    }

    public void setEquals(Object object, PropertyToken property) {
        if (!_solver.isSettable(object)) {
            return;
        }

        super.setEquals(object, property);
        if (property != null) {
            getSolver().putToken(object, property.getToken());
        }
    }

    private boolean useChannel = false;

    /**
     * Return a list of property-able NamedObj contained by the component. All
     * ports and parameters are considered property-able.
     * @return The list of property-able named object.
     * @exception IllegalActionException
     */
    public List<Object> getPropertyables() throws IllegalActionException {
        List<Object> list = new ArrayList<Object>();

        // Add all channels.

        if (!useChannel) {
            list.addAll(((Entity) getComponent()).portList());
        } else {
            for (IOPort port : (List<IOPort>) ((Entity) getComponent())
                    .portList()) {
                for (int i = 0; i < port.getWidth(); i++) {
                    list.add(new Channel(port, i));
                }
            }
        }

        // Add attributes.
        //        if (!getSolver().isListening()) {
        list.addAll(_getPropertyableAttributes());
        //        }

        return list;
    }

    protected ParseTreeAnnotationEvaluator _annotationEvaluator() {
        return new ParseTreeTokenAnnotationEvaluator();
    }

    protected List<PropertyHelper> _getSubHelpers()
            throws IllegalActionException {
        //        if (!getSolver().isListening()) {
        return _getASTNodeHelpers();
        //        } else {
        //            return new LinkedList<PropertyHelper>();
        //        }
    }

    protected List<IOPort> _getInnerSourcePortList(IOPort port) {
        List<IOPort> result = new ArrayList<IOPort>();

        Iterator iterator = port.deepInsidePortList().iterator();

        while (iterator.hasNext()) {
            IOPort connectedPort = (IOPort) iterator.next();
            boolean isInput = connectedPort.isInput();
            boolean isCompositeInput = connectedPort.getContainer() instanceof CompositeEntity
                    && isInput
                    && port.depthInHierarchy() > connectedPort
                            .depthInHierarchy();

            if (isInput && !isCompositeInput) {
                result.add(connectedPort);
            }
        }
        return result;
    }

    //  FIXME: RENAME? get ports from inside the actors connected to outPort
    //         move to use case?
    protected List<IOPort> _getInnerSinkPortList(IOPort port) {
        List<IOPort> result = new ArrayList<IOPort>();

        Iterator iterator = port.deepInsidePortList().iterator();

        while (iterator.hasNext()) {
            IOPort connectedPort = (IOPort) iterator.next();

            boolean isInput = connectedPort.isInput();
            boolean isCompositeOutput = connectedPort.getContainer() instanceof CompositeEntity
                    && isInput
                    && port.depthInHierarchy() > connectedPort
                            .depthInHierarchy();

            if (!isInput && !isCompositeOutput) {
                result.add(connectedPort);
            }
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    protected List<Channel> _getInnerSourceChannelList(IOPort port) {
        List<Channel> result = new ArrayList<Channel>();

        return result;
    }

    protected List<Channel> _getInnerSinkChannelList(IOPort port)
            throws InvalidStateException, IllegalActionException {
        List<Channel> result = new ArrayList<Channel>();

        Receiver[][] receivers = port.deepGetReceivers();

        for (Receiver[] element : receivers) {
            for (Receiver element2 : element) {
                IOPort sinkPort = element2.getContainer();
                result.add(new Channel(sinkPort, _getChannelNumber(element2,
                        sinkPort)));
            }
        }

        return result;
    }

    private int _getChannelNumber(Receiver receiver, IOPort port) {
        Receiver[][] receivers = port.getReceivers();
        for (int i = 0; i < receivers.length; i++) {
            assert receivers[i].length != 1;
            if (receiver == receivers[i][0]) {
                return i;
            }
        }
        return -1;
    }

    protected List<Channel> _getSinkChannelList(IOPort port) {
        List<Channel> result = new ArrayList<Channel>();

        Receiver[][] receivers = port.getReceivers();

        for (Receiver[] element : receivers) {
            for (Receiver element2 : element) {
                IOPort sinkPort = element2.getContainer();
                result.add(new Channel(sinkPort, _getChannelNumber(element2,
                        sinkPort)));
            }
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////////////

    protected List<Port> _getDeepSinkPortList(IOPort port) {
        List<Port> result = new ArrayList<Port>();

        Iterator iterator = port.deepConnectedPortList().iterator();

        while (iterator.hasNext()) {
            IOPort connectedPort = (IOPort) iterator.next();

            boolean isInput = connectedPort.isInput();
            boolean isCompositeOutput = connectedPort.getContainer() instanceof CompositeEntity
                    && !isInput
                    && port.depthInHierarchy() > connectedPort
                            .depthInHierarchy();

            if (isInput || isCompositeOutput) {
                result.add(connectedPort);
            }
        }
        return result;
    }

    protected List<Port> _getDeepSourcePortList(IOPort port) {
        List<Port> result = new ArrayList<Port>();

        Iterator iterator = port.deepConnectedPortList().iterator();

        while (iterator.hasNext()) {
            IOPort connectedPort = (IOPort) iterator.next();
            boolean isInput = connectedPort.isInput();
            boolean isCompositeInput = connectedPort.getContainer() instanceof CompositeEntity
                    && isInput
                    && port.depthInHierarchy() > connectedPort
                            .depthInHierarchy();

            if (!isInput || isCompositeInput) {
                result.add(connectedPort);
            }
        }
        return result;
    }

    protected void _determineInputPorts(List<IOPort> inputPortList)
            throws IllegalActionException {
        // do nothing in base class
    }

    protected void _determineOutputPorts(List<IOPort> outputPortList)
            throws IllegalActionException {
        // do nothing in base class
    }

    protected void _determineAttributes(List<Attribute> attributeList,
            List<PropertyTokenASTNodeHelper> ASTNodeHelperList)
            throws KernelException {
        Iterator ASTNodeHelperIterator = ASTNodeHelperList.iterator();
        while (ASTNodeHelperIterator.hasNext()) {
            PropertyTokenASTNodeHelper ASTNodeHelper = (PropertyTokenASTNodeHelper) ASTNodeHelperIterator
                    .next();
            ASTNodeHelper.determineProperty();
            ASTNodeHelper.determineProperty(attributeList);
        }
    }

    protected void _determineRefinement() throws IllegalActionException,
            KernelException {
        // do nothing in base class
    }
}
