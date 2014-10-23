/*
@Copyright (c) 2008-2014 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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
package ptolemy.domains.tdl.kernel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ptolemy.actor.IOPort;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.ParseTreeFreeVariableCollector;
import ptolemy.data.expr.PtParser;
import ptolemy.domains.modal.kernel.Transition;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

/**
 * A TDL transition has some specific TDL parameters. - frequency: together with
 * the mode period, this value defines when this transition is tested.
 *
 * @author Patricia Derler
@version $Id$
@since Ptolemy II 8.0
 *
 */
public class TDLTransition extends Transition {

    /** Construct a transition contained by the specified
     *  entity.
     *  @param workspace The workspace for synchronization and version
     *  tracking.
     *  @exception IllegalActionException If the container is incompatible
     *   with this transition.
     *  @exception NameDuplicationException If the name coincides with
     *   any relation already in the container.
     */
    public TDLTransition(Workspace workspace) throws IllegalActionException,
    NameDuplicationException {
        super(workspace);
        _init();
    }

    /**
     * Construct a transition with the given name contained by the specified
     * entity. The container argument must not be null, or a
     * NullPointerException will be thrown. This transition will use the
     * workspace of the container for synchronization and version counts. If the
     * name argument is null, then the name is set to the empty string.
     *
     * @param container
     *            The container.
     * @param name
     *            The name of the transition.
     * @exception IllegalActionException
     *                If the container is incompatible with this transition.
     * @exception NameDuplicationException
     *                If the name coincides with any relation already in the
     *                container.
     */
    public TDLTransition(TDLActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public parameter                  ////

    /**
     * The frequency of the transition.
     */
    public Parameter frequency;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * List of all ports that are used in this guard expression.
     */
    public List requiredPorts;

    /**
     * List of Sensors that are used in this guard expression.
     */
    public List requiredSensors;

    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == guardExpression) {
            _getDependentModuleInputPorts();
        }
        super.attributeChanged(attribute);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Compute the list of ports that are used in this guard expression.
     * @exception IllegalActionException Thrown if guard expression cannot be parsed.
     */
    private void _getDependentModuleInputPorts() throws IllegalActionException {
        String expr = getGuardExpression();
        PtParser parser = new PtParser();
        ASTPtRootNode guardParseTree;
        try {
            guardParseTree = parser.generateParseTree(expr);
        } catch (IllegalActionException ex) {
            throw new IllegalActionException(this, ex,
                    "Failed to parse guard expression \"" + expr + "\"");
        }

        ParseTreeFreeVariableCollector coll = new ParseTreeFreeVariableCollector();
        Set freeVars = coll.collectFreeVariables(guardParseTree);

        for (Iterator it = freeVars.iterator(); it.hasNext();) {
            String name = (String) it.next();
            for (Iterator sensorIt = ((TDLModule) this.getContainer()
                    .getContainer()).portList().iterator(); sensorIt.hasNext();) {
                IOPort port = (IOPort) sensorIt.next();
                if (port.getName().equals(name)) {
                    if (port.isInput()) {
                        requiredSensors.add(port);
                    }
                    requiredPorts.add(port);
                }
            }
        }
    }

    /**
     * Initialize the parameters of a transition.
     * @exception IllegalActionException Thrown if frequency parameter cannot be created.
     * @exception NameDuplicationException Thrown if The frequency parameter cannot be created.
     */
    private void _init() throws NameDuplicationException,
    IllegalActionException {
        outputActions.setVisibility(Settable.NONE);
        setActions.setVisibility(Settable.NONE);
        history.setVisibility(Settable.NONE);
        preemptive.setVisibility(Settable.NONE);
        defaultTransition.setVisibility(Settable.NONE);
        nondeterministic.setVisibility(Settable.NONE);
        refinementName.setVisibility(Settable.NONE);
        frequency = new Parameter(this, "frequency");
        frequency.setExpression("1");

        requiredPorts = new ArrayList();
        requiredSensors = new ArrayList();
    }

}
