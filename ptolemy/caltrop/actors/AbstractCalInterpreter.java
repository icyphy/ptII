/*
 @Copyright (c) 2004-2014 The Regents of the University of California.
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
package ptolemy.caltrop.actors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.caltrop.PtolemyPlatform;
import ptolemy.caltrop.ddi.CSPFactory;
import ptolemy.caltrop.ddi.DDFFactory;
import ptolemy.caltrop.ddi.DDI;
import ptolemy.caltrop.ddi.DDIFactory;
import ptolemy.caltrop.ddi.Dataflow;
import ptolemy.caltrop.ddi.SDFFactory;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.ConfigurableAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import caltrop.interpreter.Context;
import caltrop.interpreter.ExprEvaluator;
import caltrop.interpreter.ast.Actor;
import caltrop.interpreter.ast.Decl;
import caltrop.interpreter.ast.Expression;
import caltrop.interpreter.ast.Import;
import caltrop.interpreter.ast.PortDecl;
import caltrop.interpreter.ast.TypeExpr;
import caltrop.interpreter.environment.CacheEnvironment;
import caltrop.interpreter.environment.Environment;
import caltrop.interpreter.environment.HashEnvironment;
import caltrop.interpreter.util.CalScriptImportHandler;
import caltrop.interpreter.util.ClassLoadingImportHandler;
import caltrop.interpreter.util.EnvironmentFactoryImportHandler;
import caltrop.interpreter.util.ImportUtil;

///////////////////////////////////////////////////////////////////
////AbstractCalInterpreter

/**
 This class is the base class for actors that interpret CAL source
 inside the Ptolemy II framework. It configures itself according to an
 {@link caltrop.interpreter.ast.Actor Actor} data structure (setting
 up ports, parameters, types etc.) and then proceeds to execute as the
 actor by interpreting the actions using the {@link
 ptolemy.caltrop.ddi.util.DataflowActorInterpreter
 DataflowActorInterpreter} infrastructure.

 <p> The actor interpreter is configured by a context that injects the
 appropriate <tt>Token</tt>-based value system into the evaluation of
 the actions. This is implemented in the class {@link
 ptolemy.caltrop.PtolemyPlatform PtolemyPlatform}.

 <p> For further documentation on CAL, see the
 <a href = "http://embedded.eecs.berkeley.edu/caltrop/docs/LanguageReport">Language Report</a>.

 @author J&#246;rn W. Janneck <jwj@acm.org>, Christopher Chang, Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 4.1
 @Pt.ProposedRating Yellow (neuendor)
 @Pt.AcceptedRating Red (cxh)
 @see ptolemy.caltrop.ddi.util.DataflowActorInterpreter
 @see caltrop.interpreter.Context
 @see PtolemyPlatform
 */
abstract public class AbstractCalInterpreter extends TypedAtomicActor {
    /** Construct an actor in the given workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the actor.
     */
    public AbstractCalInterpreter(Workspace workspace) {
        super(workspace);
    }

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public AbstractCalInterpreter(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the object into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new NamedObj.
     *  @exception CloneNotSupportedException If any of the attributes
     *   cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        AbstractCalInterpreter newObject = (AbstractCalInterpreter) super
                .clone(workspace);
        newObject._actor = null;
        newObject._ddi = null;
        newObject._env = null;

        return newObject;
    }

    /** Fire the actor.
     *  @exception IllegalActionException If thrown by the parent or by the
     *  fire() method of the domain-dependent interpreter.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        _ddi.fire();
    }

    /**
     * Initialize the actor, clearing its input channels.
     *
     * @exception IllegalActionException If the superclass throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _ddi.initialize();
    }

    /**
     * Commit the last state changes.
     *
     * @return Returns whatever <tt>super.postfire()</tt> returns.
     * @exception IllegalActionException If the superclass throws it.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        super.postfire();
        return _ddi.postfire();
    }

    /**
     * Populate the initial actor environment. This is done by binding
     * the parameters to the user-supplied values and then evaluating
     * the definitions of state variables and creating the
     * corresponding bindings.
     *
     * @exception IllegalActionException If an error occurred during the
     * retrieval of parameter values or the evaluation of actor state
     * variable values.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();

        Environment env = new HashEnvironment(new CacheEnvironment(_env,
                _theContext), _theContext);

        try {
            _bindActorParameters(env);
            _initializeStateVariables(env);
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex,
                    "Failed to initialize CAL actor environment.");
        }

        _ddi = _getDDI(env);

        // FIXME: use exeception to return reasonable error message.
        try {
            _ddi.isLegalActor();
        } catch (RuntimeException ex) {
            throw new IllegalActionException(this, ex, "Actor is not a valid "
                    + _ddi.getName() + " actor.");
        }

        if (!_ddi.isLegalActor()) {
            throw new IllegalActionException(this, "Actor is not a valid "
                    + _ddi.getName() + " actor.");
        }

        _ddi.setupActor();
    }

    /** Prefire the actor.
     *  @return true If the actor can be fired.
     *  @exception IllegalActionException If thrown by the parent or by the
     *  prefire() method of the domain-dependent interpreter.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        super.prefire();
        return _ddi.prefire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected members                   ////

    /** Initialize this ptolemy actor using declarations in the given
     *  CAL actor.
     *  @param actor The CAL actor to be used
     *  @exception Exception  If there is a problem initializing the actor.
     */
    protected void _setupActor(Actor actor) throws Exception {
        assert actor != null;

        _actor = actor;
        _env = _extendEnvWithImports(_globalEnv, actor.getImports());
        _refreshTypedIOPorts(actor.getInputPorts(), true, false);
        _refreshTypedIOPorts(actor.getOutputPorts(), false, true);
        _refreshParameters();

        CompositeEntity container = (CompositeEntity) getContainer();

        if (_lastGeneratedActorName != null
                && _lastGeneratedActorName.equals(getName())) {
            if (container != null
                    && container.getEntity(actor.getName()) != this) {
                _lastGeneratedActorName = ((CompositeEntity) getContainer())
                        .uniqueName(actor.getName());
                setName(_lastGeneratedActorName);
            }
        }

        _attachActorIcon(actor.getName());
    }

    /** Attach the actor icon.
     *  @param name The name of the actor.
     *  @exception IllegalActionException If thrown while getting the
     *  _iconDescription attribute.
     */
    protected void _attachActorIcon(String name) throws IllegalActionException {
        String iconText = "<svg>\n" + "<rect x=\"-20\" y=\"-20\" "
                + "width=\"60\" height=\"40\" " + "style=\"fill:white\"/>\n"
                + "<text x=\"-3\" y=\"5\" " + "style=\"font-size:18\">\n"
                + "CAL\n" + "</text>\n" + "<text x=\"-16\" y=\"17\" "
                + "style=\"font-size:10\">\n" + name + "\n" + "</text>\n"
                + "</svg>\n";
        ConfigurableAttribute iconDescription = (ConfigurableAttribute) getAttribute(
                "_iconDescription", ConfigurableAttribute.class);
        // Only update the parameter if the new value is different from the
        // old value.  This avoids a ConcurrentModificationException in
        // ptolemy/configs/test/allConfigs.tcl
        if (iconDescription == null
                || !iconDescription.getConfigureText().equals(iconText)) {
            //System.out.println("AbstractCalInterpreter: iconText:\n " + iconText
            //        + "\n------\n" + (iconDescription == null ? "null" : iconDescription.getConfigureText()));
            _attachText("_iconDescription", iconText);
        }
    }

    /** Get the Ptolemy type that corresponds to the given type expression.
     *  @param typeExpr The type expression, one of "UINT8", "UINT9",
     *  "INT19" or "positive".
     *  @return the corresponding type, which currently is always
     *  ptolemy.data.type.BaseType.int.
     */
    protected static ptolemy.data.type.Type _getPtolemyType(TypeExpr typeExpr) {
        if (typeExpr == null) {
            return ptolemy.data.type.BaseType.GENERAL;
        }

        String s = (String) _typeReplacementMap.get(typeExpr.getName());

        if (s == null) {
            s = typeExpr.getName();
        }

        ptolemy.data.type.Type t = ptolemy.data.type.BaseType.forName(s);
        return t == null ? ptolemy.data.type.BaseType.GENERAL : t;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Get the values of Ptolemy actor parameters and plug them into
    // the given environment.
    private void _bindActorParameters(Environment env)
            throws IllegalActionException {
        List pars = attributeList(Parameter.class);

        for (Iterator i = pars.iterator(); i.hasNext();) {
            Parameter p = (Parameter) i.next();
            env.bind(p.getName(), p.getToken());
        }
    }

    // Bind stat variables in the given environment to the correct
    // initial values.
    private void _initializeStateVariables(Environment env) {
        Decl[] decls = _actor.getStateVars();

        if (decls != null) {
            ExprEvaluator eval = new ExprEvaluator(_theContext, env);

            for (Decl decl : decls) {
                String var = decl.getName();
                Expression valExpr = decl.getInitialValue();

                // Note: this assumes that declarations are
                // ordered by eager dependency
                Object value = valExpr == null ? _theContext.createNull()
                        : eval.evaluate(valExpr);
                env.bind(var, value);
            }
        }
    }

    // Get a DDI appropriate for the actor's director.
    private DDI _getDDI(Environment env) {
        DDIFactory pluginFactory = (DDIFactory) _directorToDDIMap
                .get(getDirector().getClass().getName());

        if (pluginFactory != null) {
            return pluginFactory.create(this, _actor, _theContext, env);
        } else {
            // default to Dataflow case.
            return new Dataflow(this, _actor, _theContext, env);
        }
    }

    // Create parameters of the Ptolemy actor to correspond with the
    // interface specified in the CAL code.
    private void _refreshParameters() throws IllegalActionException,
            NameDuplicationException {
        Set parNames = new HashSet();

        if (_actor.getParameters() != null) {
            for (int i = 0; i < _actor.getParameters().length; i++) {
                String name = _actor.getParameters()[i].getName();
                if (getAttribute(name, ptolemy.data.expr.Parameter.class) == null) {
                    new Parameter(this, name);
                }

                parNames.add(name);
            }
        }

        List parameters = attributeList(ptolemy.data.expr.Parameter.class);

        for (Iterator i = parameters.iterator(); i.hasNext();) {
            Parameter a = (Parameter) i.next();

            if (!parNames.contains(a.getName())) {
                a.setContainer(null);
            }
        }
    }

    // Create ports of the Ptolemy actor to correspond with the
    // interface specified in the CAL code.
    private void _refreshTypedIOPorts(PortDecl[] ports, boolean isInput,
            boolean isOutput) throws IllegalActionException,
            NameDuplicationException {
        Set portNames = new HashSet();

        // Create new ports.
        for (PortDecl port2 : ports) {
            TypedIOPort port = (TypedIOPort) getPort(port2.getName());

            if (port != null
                    && (port.isInput() != isInput
                            || port.isOutput() != isOutput || port
                            .isMultiport() != port2.isMultiport())) {
                port.setContainer(null);
                port = null;
            }

            if (port == null) {
                port = new TypedIOPort(this, port2.getName(), isInput, isOutput);
            }

            portNames.add(port2.getName());
        }

        // Release any ports which are no longer used.
        for (Iterator i = isInput ? inputPortList().iterator()
                : outputPortList().iterator(); i.hasNext();) {
            IOPort p = (IOPort) i.next();

            if (!portNames.contains(p.getName())) {
                p.setContainer(null);
            }
        }

        // Set the types.
        for (PortDecl port : ports) {
            ((TypedIOPort) getPort(port.getName()))
                    .setTypeEquals(_getPtolemyType(port.getType()));
        }
    }

    // Process actor import statements
    private Environment _extendEnvWithImports(Environment env, Import[] imports) {
        Environment newEnv = ImportUtil.handleImportList(env, importHandlers,
                imports);

        if (newEnv == null) {
            throw new RuntimeException("Failed to process import list.");
        }

        return newEnv;
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private members                    ////
    // The CAL actor.
    private Actor _actor;

    // The domain-dependent interpreter.
    private DDI _ddi;

    // The environment used to evaluate parameters.
    private Environment _env;

    // The default global environment.
    private final static Environment _globalEnv = PtolemyPlatform.thePlatform
            .createGlobalEnvironment();

    // The Ptolemy-specific context
    private final static Context _theContext = PtolemyPlatform.thePlatform
            .context();

    // Map from director name to DDI.
    private final static Map _directorToDDIMap = new HashMap();

    static {
        _directorToDDIMap.put("ptolemy.domains.sdf.kernel.SDFDirector",
                new SDFFactory());
        _directorToDDIMap.put("ptolemy.domains.ddf.kernel.DDFDirector",
                new DDFFactory());
        _directorToDDIMap.put("ptolemy.domains.csp.kernel.CSPDirector",
                new CSPFactory());
    }

    // List of import handlers.
    private static List importHandlers;

    static {
        importHandlers = new ArrayList();
        importHandlers.add(new EnvironmentFactoryImportHandler(
                PtolemyPlatform.thePlatform));
        importHandlers.add(new CalScriptImportHandler(
                PtolemyPlatform.thePlatform));
        importHandlers.add(new ClassLoadingImportHandler(
                PtolemyPlatform.thePlatform, AbstractCalInterpreter.class
                        .getClassLoader()));
    }

    // Map of substitutions from CAL types to Ptolemy types.
    private static Map _typeReplacementMap;

    static {
        _typeReplacementMap = new HashMap();
        _typeReplacementMap.put("UINT8", "int");
        _typeReplacementMap.put("UINT9", "int");
        _typeReplacementMap.put("INT19", "int");
        _typeReplacementMap.put("positive", "int");
    }

    private String _lastGeneratedActorName = null;
}
