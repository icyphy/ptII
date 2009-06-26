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

import ptolemy.actor.Actor;
import ptolemy.actor.Manager;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.properties.Property;
import ptolemy.data.properties.PropertyHelper;
import ptolemy.data.properties.PropertySolver;
import ptolemy.data.properties.gui.PropertySolverGUIFactory;
import ptolemy.data.properties.token.firstValueToken.FirstTokenGotListener;
import ptolemy.data.properties.token.firstValueToken.FirstTokenSentListener;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.domains.fsm.kernel.FSMDirector;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

public class PropertyTokenSolver extends PropertySolver {

    public PropertyTokenSolver(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        useCase = new StringParameter(this, "portValue");
        useCase.setExpression("firstValueToken");

        listeningMethod = new StringParameter(this, "listeningMethod");
        listeningMethod.setExpression("Input & Output Ports");

        numberIterations = new Parameter(this, "numberIterations");
        numberIterations.setExpression("1");

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-50\" y=\"-20\" width=\"115\" height=\"40\" "
                + "style=\"fill:yellow\"/>" + "<text x=\"-40\" y=\"-5\" "
                + "style=\"font-size:12; font-family:SansSerif; fill:black\">"
                + "Double click to\nResolve Property.</text></svg>");

        new PropertySolverGUIFactory(this, "_portValueSolverGUIFactory");

        _addChoices();
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

    public Property getProperty(Object object) {
        Property result;

        //        if (object instanceof PortParameter) {
        //            result = getProperty(((PortParameter)object).getPort());
        //        } else {
        result = super.getProperty(object);
        //        }

        return (result == null) ? new PropertyToken(Token.NIL) : result;
    }

    public String getUseCaseName() {
        return useCase.getExpression();
    }

    protected void _resolveProperties(NamedObj analyzer) throws KernelException {
        NamedObj toplevel = _toplevel();
        PropertyTokenCompositeHelper topLevelHelper = (PropertyTokenCompositeHelper) _getHelper(toplevel);
        super._resolveProperties(analyzer);

        topLevelHelper.reinitialize();

        // run model
        if (!getListening().equals("NONE")) {

            topLevelHelper.addListener(getListening().contains("Input"),
                    getListening().contains("Output"));

            // run simulation
            Manager manager = new Manager(toplevel.workspace(),
                    "PortValueManager");

            if (toplevel instanceof TypedCompositeActor) {

                ((TypedCompositeActor) toplevel).setManager(manager);

            } else if (toplevel instanceof FSMActor) {

                TypedCompositeActor compositeActor = new TypedCompositeActor(
                        this.workspace());
                FSMDirector fsmDirector = new FSMDirector(this.workspace());

                compositeActor.setDirector(fsmDirector);
                ((FSMActor) toplevel).setContainer(compositeActor);

                compositeActor.setManager(manager);

                ((FSMActor) toplevel).setContainer(null);
            } else {
                throw new IllegalActionException(
                        "Not able to fire this type of toplevel actor ("
                                + toplevel + ").");
            }

            manager.preinitializeAndResolveTypes();
            ((Actor) toplevel).initialize();
            ((Actor) toplevel)
                    .iterate(((IntToken) (numberIterations.getToken()))
                            .intValue());
            ((Actor) toplevel).wrapup();
            //FIXME: stoping the manager conflicts with extendedFirstListener. No iterations can be done there.
            //            ((Actor) topLevel).stop();

            manager.wrapup();
            manager.finish();
            manager.stop();

            topLevelHelper.removeListener(getListening().contains("Input"),
                    getListening().contains("Output"));

        }

        topLevelHelper.determineProperty();

    }

    public String getExtendedUseCaseName() {
        return "token::" + getUseCaseName();
    }

    public void reset() {
        super.reset();
        _clearTokenMap();
    }

    public StringParameter useCase;
    public StringParameter listeningMethod;
    public Parameter numberIterations;

    /** Add choices to the parameters.
     *  @exception IllegalActionException If there is a problem
     *  accessing files or parameters.
     */
    private void _addChoices() throws IllegalActionException {
        // Add all the subdirectories in token/ directory as
        // choices.  Directories named "CVS" and ".svn" are skipped.
        _addChoices(useCase, "$CLASSPATH/ptolemy/data/properties/token");

        listeningMethod.addChoice("NONE");
        listeningMethod.addChoice("Input & Output Ports");
        listeningMethod.addChoice("Input Ports");
        listeningMethod.addChoice("Output Ports");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    public String getListening() {
        return listeningMethod.getExpression();
    }

    public Boolean isListening() {
        return !getListening().equalsIgnoreCase("NONE");
    }

    public FirstTokenSentListener getSentListener() {
        return _sentListener;
    }

    public FirstTokenGotListener getGotListener() {
        return _gotListener;
    }

    public void putToken(Object object, Token token) {
        _tokenMap.put(object, token);
    }

    public Token getToken(Object object) {
        return _tokenMap.get(object);
    }

    private void _clearTokenMap() {
        _tokenMap.clear();
    }

    private FirstTokenSentListener _sentListener = new FirstTokenSentListener(
            this);
    private FirstTokenGotListener _gotListener = new FirstTokenGotListener(this);

    private Map<Object, Token> _tokenMap = new HashMap<Object, Token>();
}
