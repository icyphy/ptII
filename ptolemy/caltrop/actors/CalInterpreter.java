/*
@Copyright (c) 2003 The Regents of the University of California.
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

@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)


*/
package ptolemy.caltrop.actors;

import caltrop.interpreter.Context;
import caltrop.interpreter.ExprEvaluator;
import caltrop.interpreter.ast.Actor;
import caltrop.interpreter.ast.Decl;
import caltrop.interpreter.ast.Expression;
import caltrop.interpreter.ast.Import;
import caltrop.interpreter.ast.PackageImport;
import caltrop.interpreter.ast.PortDecl;
import caltrop.interpreter.ast.SingleImport;
import caltrop.interpreter.ast.TypeExpr;
import caltrop.interpreter.environment.CacheEnvironment;
import caltrop.interpreter.environment.Environment;
import caltrop.interpreter.environment.HashEnvironment;
import caltrop.interpreter.environment.PackageEnvironment;
import caltrop.interpreter.environment.SingleClassEnvironment;
import caltrop.interpreter.util.ASTFactory;
import caltrop.parser.Lexer;
import caltrop.parser.Parser;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.caltrop.PtolemyPlatform;
import ptolemy.caltrop.ddi.CSPFactory;
import ptolemy.caltrop.ddi.DDI;
import ptolemy.caltrop.ddi.DDIFactory;
import ptolemy.caltrop.ddi.Dataflow;
import ptolemy.caltrop.ddi.SDFFactory;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;

import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

//////////////////////////////////////////////////////////////////////////
//// CalInterpreter
/**
 This actor interprets CAL source as an actor inside the Ptolemy II
 framework. It has a <tt>calCode</tt> string attribute that contains
 the text of a CAL actor. It configures itself according to CAL code
 string (setting up ports, parameters, types etc.) and then proceeds
 to execute the actor by interpreting the actions using the {@link
 ptolemy.caltrop.ddi.util.DataflowActorInterpreter
 DataflowActorInterpreter} infrastructure.

 <p> The actor interpreter is configured by a context that injects the
 appropriate <tt>Token</tt>-based value system into the evaluation of
 the actions. This is implemented in the class {@link
 ptolemy.caltrop.PtolemyPlatform PtolemyPlatform}.

 <p> For further documentation on CAL, see the
<a href = "http://embedded.eecs.berkeley.edu/caltrop/docs/LanguageReport">Language Report</a>.

@author Jörn W. Janneck <jwj@acm.org> Christopher Chang <cbc@eecs.berkeley.edu>
@version $Id$
@since Ptolemy II 3.1
@see ptolemy.caltrop.ddi.util.DataflowActorInterpreter
@see Context
@see PtolemyPlatform
*/
public class CalInterpreter extends TypedAtomicActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public CalInterpreter(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        calCode = new StringAttribute(this, "calCode");
        calCode.setExpression(defaultActorText);
        calCode.setVisibility(Settable.EXPERT);
        _attachActorIcon(name);
    }

    /**
     * The only attribute whose modifications are handled is the
     * <tt>calCode</tt> attribute, which contains the source code of
     * the CAL actor.
     * <p>
     * Whenever the source is changed, the text is parsed,
     * transformed, and translated into an internal data structure
     * used for interpretation.
     *
     * @param attribute The attribute that changed.
     * @exception IllegalActionException If an error occurs parsing or
     * transforming the CAL source code.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == calCode) {
            String s = calCode.getExpression();
            try {
                Actor actor = _stringToActor(s);
                if (actor != null) {
                    _setupActor(actor);
                }
            } catch (Throwable ex) {
                throw  new IllegalActionException(this, ex,
                        "Failed to set up actor.");
            }

        } else {
            super.attributeChanged(attribute);
        }
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
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        Environment env = new HashEnvironment(new CacheEnvironment(_env,
                _theContext), _theContext);
        try {
            _bindActorParameters(env);
            _bindActorStateVariables(env);
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex,
                   "Cannot initialize actor environment for actor '"
                    + _actor.getName());
        }
        _ddi = _getPlugin(env);
        if (!_ddi.isLegalActor()) {
            throw new IllegalActionException(_actor.getName()
                    + " is not a valid " + _ddi.getName() + " actor.");
        }
        _ddi.setupActor();
    }

    /**
     * Initialize the actor, clearing its input channels.
     *
     * @exception IllegalActionException If the superclass throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _ddi.initialize();
    }

    public boolean prefire() throws IllegalActionException {
        super.prefire();
        return _ddi.prefire();
    }

    public void fire() throws IllegalActionException {
        super.fire();
        _ddi.fire();
    }

    /**
     * Commit the last state changes.
     *
     * @return Returns whatever <tt>super.postfire()</tt> returns.
     * @exception IllegalActionException If the superclass throws it.
     */
    public boolean postfire() throws IllegalActionException {
        // FIXMELATER: commit state changes.
        return super.postfire();
    }

    public boolean superPrefire()
            throws IllegalActionException {
        // FIXME uh this is totally braindead...
        // only needed in SDFPlugin.prefire(), there has GOT
        // to be a better way.
        return super.prefire();
    }

    public void printDebug(String msg) {
        _debug(msg);
    }



    private static ptolemy.data.type.Type _getPtolemyType(TypeExpr typeExpr) {
        if (typeExpr == null)
            return ptolemy.data.type.BaseType.GENERAL;
        ptolemy.data.type.Type t = ptolemy.data.type.BaseType.forName(typeExpr.getName()); //FIXMELATER ignores type parameters
        return (t == null) ? ptolemy.data.type.BaseType.GENERAL : t;
    }

    private static Actor _stringToActor(String code) throws Throwable {
        return caltrop.interpreter.util.SourceReader.readActor(code);
    }

    private void _bindActorParameters(Environment env)
            throws IllegalActionException {
        List pars = this.attributeList(Parameter.class);
        for (Iterator i = pars.iterator(); i.hasNext();) {
            Parameter p = (Parameter) i.next();
            // cbcnote: assumes knowledge of how objects are stored. could maybe have a separate function
            // like ptValue -> ??
            env.bind(p.getName(), p.getToken());
        }
    }

    private void _bindActorStateVariables(Environment env) {
        Decl[] decls = _actor.getStateVars();
        if (decls != null) {
            ExprEvaluator eval = new ExprEvaluator(_theContext, env);
            for (int i = 0; i < decls.length; i++) {
                String var = decls[i].getName();
                Expression valExpr = decls[i].getInitialValue();

                // Note: this assumes that declarations are
                // ordered by eager dependency

                Object value = (valExpr == null)
                    ? _theContext.createNull() : eval.evaluate(valExpr);
                env.bind(var, value);
            }
        }
    }

    private DDI _getPlugin(Environment env) {
        DDIFactory pluginFactory = (DDIFactory) _directorDDIMap
            .get(this.getDirector().getClass().getName());
        if (pluginFactory != null) {
            return pluginFactory.create(this, _actor, _theContext, env);
        } else {
            // default to Dataflow case.
            return new Dataflow(this, _actor, _theContext, env);
        }
    }

    private void _refreshParameters()
            throws IllegalActionException, NameDuplicationException {
        Set parNames = new HashSet();
        if (_actor.getParameters() != null) {
            for (int i = 0; i < _actor.getParameters().length; i++) {
                String name = _actor.getParameters()[i].getName();
                if (this.getAttribute(name, ptolemy.data.expr.Parameter.class)
                        == null)
                    new Parameter(this, name);
                parNames.add(name);
            }
        }
        List parameters =
            this.attributeList(ptolemy.data.expr.Parameter.class);
        for (Iterator i = parameters.iterator(); i.hasNext();) {
            Parameter a = (Parameter) i.next();
            if (!parNames.contains(a.getName()))
                a.setContainer(null);
        }
    }

    private void _refreshTypedIOPorts(PortDecl[] ports,
            boolean isInput, boolean isOutput)
            throws IllegalActionException, NameDuplicationException {
        // a place to store names of ports as we iterate through them later.
        Set portNames = new HashSet();

        // this part creates new ports, and also recreates
        // ports which may have changed their characteristics
        // since the time they were created.

        for (int i = 0; i < ports.length; i++) {
            TypedIOPort port = (TypedIOPort) this.getPort(ports[i].getName());
            if (port != null &&
                    ((port.isInput() != isInput)
                            || (port.isOutput() != isOutput) ||
                    (port.isMultiport() != ports[i].isMultiport()))) {
                port.setContainer(null);
                port = null;
            }
            if (port == null) {
                port = new TypedIOPort(this, ports[i].getName(),
                        isInput, isOutput);
            }
            portNames.add(ports[i].getName());
        }
        // this part releases any ports which are no longer used.
        for (Iterator i = isInput ? this.inputPortList().iterator()
                 : this.outputPortList().iterator();
             i.hasNext();) {
            IOPort p = (IOPort) i.next();
            if (!portNames.contains(p.getName()))
                p.setContainer(null);
        }
        // now set the types.
        for (int i = 0; i < ports.length; i++) {
            ((TypedIOPort) this.getPort(ports[i].getName()))
                .setTypeEquals(_getPtolemyType(ports[i].getType()));
        }
    }

    private void _setupActor(Actor actor) throws Exception {
        assert actor != null;

        _actor = actor;
        _env = _extendEnvWithImports(actor.getImports());
        _refreshTypedIOPorts(actor.getInputPorts(), true, false);
        _refreshTypedIOPorts(actor.getOutputPorts(), false, true);
        _refreshParameters();

        CompositeEntity container = (CompositeEntity)getContainer();
        if (_lastGeneratedActorName != null && _lastGeneratedActorName.equals(this.getName())) {
            if(container != null
                    && container.getEntity(actor.getName()) != this) {
                _lastGeneratedActorName = ((CompositeEntity) this.getContainer())
                                  .uniqueName(actor.getName());
                this.setName(_lastGeneratedActorName);
            }
        }
        _attachActorIcon(actor.getName());
    }

    private void _attachActorIcon(String name) {
        _attachText("_iconDescription", "<svg>\n" +
                "<rect x=\"-20\" y=\"-20\" "
                + "width=\"60\" height=\"40\" "
                + "style=\"fill:white\"/>\n"
                + "<text x=\"-3\" y=\"5\" "
                + "style=\"font-size:18\">\n"
                + "CAL\n"
                + "</text>\n"
                + "<text x=\"-16\" y=\"17\" "
                + "style=\"font-size:10\">\n"
                + name + "\n"
                + "</text>\n"
                + "</svg>\n");
    }

    private Environment _extendEnvWithImports(Import[] imports) 
            throws IllegalActionException {
        Environment lastEnv = _globalEnv;

        for (int i = 0; i < imports.length; i++) {
            Import anImport = imports[i];
            String packagePrefix = anImport.getPackagePrefix();
            if (anImport instanceof PackageImport) {
                lastEnv = new PackageEnvironment(lastEnv,
                        this.getClass().getClassLoader(),
                        _theContext, packagePrefix);
            } else if (anImport instanceof SingleImport) {
                String className = ((SingleImport) anImport).getClassName();
                String alias = ((SingleImport) anImport).getAlias();
                if (alias == "") {
                    lastEnv = new SingleClassEnvironment(lastEnv,
                            this.getClass().getClassLoader(),
                            _theContext, packagePrefix, className);
                } else {
                    lastEnv = new SingleClassEnvironment(lastEnv,
                            this.getClass().getClassLoader(),
                            _theContext, packagePrefix, className, alias);
                }
            } else {
                throw new IllegalActionException(
                    "Unknown import type '" + anImport
                    + "' encountered in '"
                    + _actor.getName() + "'.");
            }
        }
        return lastEnv;
    }

    /**
     * The CAL source to be interpreted.
     */
    public StringAttribute calCode;

    private Actor  _actor;
    private DDI _ddi;
    private Environment _env;
    private final static Context _theContext = PtolemyPlatform.thePlatform.context();
    private final static Environment _globalEnv = PtolemyPlatform.thePlatform.createGlobalEnvironment();
    private final static Map _directorDDIMap = new HashMap();

    private String _lastGeneratedActorName = null;

    static {
        _directorDDIMap.put("ptolemy.domains.sdf.kernel.SDFDirector",
                new SDFFactory());
        _directorDDIMap.put("ptolemy.domains.csp.kernel.CSPDirector",
                new CSPFactory());
    }

    private final static String defaultActorText = "actor CalActor () Input ==> Output : end";
    private final static String defaultNamePrefix = "CalInterpreter";
}


