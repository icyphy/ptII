/* Code generator for JavaScript Accessors that uses SSH to deploy Swarmlets.

Copyright (c) 2009-2017 The Regents of the University of California.
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

package ptolemy.cg.kernel.generic.accessor;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import ptolemy.actor.CompositeActor;
import ptolemy.cg.kernel.generic.CodeGeneratorUtilities;
import ptolemy.cg.kernel.generic.RunnableCodeGenerator;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// AccessorSSHCodeGenerator

/** Generate a JavaScript composite accessor for a model and deploy
 *  it to a remote host.
 *
 *  <p>Accessors are a technology, developed by the
 *  <a href="http://www.terraswarm.org#in_browser" target="_top">TerraSwarm Research Center</a>,
 *  for composing heterogeneous devices and services in the
 *  Internet of Things (IoT).  For more information, see
 *  <a href="http://accessors.org#in_browser" target="_top">http://accessors.org</a>.</p>
 *
 *  <p>The model can only contain JavaScript and JSAccessor actors.</p>
 *
 *  <p>To generate an Accessor version of a model, use:</p>
 *  <pre>
 *  java -classpath $PTII ptolemy.cg.kernel.generic.accessor.AccessorSSHCodeGenerator -language accessor $PTII/ptolemy/cg/kernel/generic/accessor/demo/TestComposite/TestComposite.xml; cat $PTII/org/terraswarm/accessor/accessors/web/hosts/node/TestComposite.js
 *  </pre>
 *  which is shorthand for:
 *  <pre>
 *  java -classpath $PTII ptolemy.cg.kernel.generic.accessor.AccessorSSHCodeGenerator -generatorPackage ptolemy.cg.kernel.generic.accessor -generatorPackageList generic.accessor $PTII/ptolemy/cg/adapter/generic/accessor/adapters/org/test/auto/TestComposite.xml; cat ~/cg/TestComposite.js
 *  </pre>
 *
 *  To use Cape Code, invoke:
 *  <pre>
 *  $PTII/bin/vergil -capecode ~/ptII/ptolemy/cg/kernel/generic/accessor/test/auto/RampJSTestDisplay.xml 
 *  </pre>
 *
 *  <p>This actor runs $PTII/ptolemy/cg/kernel/generic/accessor/accessorInvokeSSH, which does the following on the remote machine:</p>
 *  <ol>
 *
 *    <li>Creates a directory in <code>~/cg/<i>ModelName</i></code>,
 *    for example <code>~/cg/MyCompositeAccessor</code></li>
 *
 *    <li>Installs the <code>@terraswarm/accessors</code> and
 *      <code>pm2</code> modules in
 *      <code>~/cg/<i>ModelName</i></code>. Note
 *      that this means that to run a composite accessor with the
 *      latest accessors, the npm <code>@terraswarm/accessors</code>
 *      module must be updated.  See
 *      <a href="https://accessors.org/wiki/Main/NPMUpload">NPM Upload</a>
 *      in the accessors Wiki.        
 *      In addition, any modules listed in the comma-separated
 *      <<i>modules</i> parameter are also installed.  </li>
 *
 *    <li>Creates a small node script called <code>invoke.js</code> to
 *    run the composite accessor.</li>
 *
 *    <li>Copies the composite accessor to the directory.</li>
 *
 *    <li>Creates a script called <code>runit</code> that uses 
 *    <a href="http://pm2.keymetrics.io/">pm2</a>
 *    to stop any processes with the same name as the accessor
 *    started with forever name and then invokes node on the remote
 *    machine using <code>pm2</code>.  If the director of the model
 *    has a <i>stopTime</i> parameter, then the value is multiplied by
 *    1000 and used as the value of the timeout parameter on the
 *    remote machine. If the <i>stopTime</i> parameter is not set,
 *    then a default value (currently 15000 ms.)  is used.</li>
 *
 *    <li>The stderr and stdout are then
 *    reported using tail</li>
 *
 *   </ol>
 *
 *  <p>The <code>accessorInvokeSSH</code> script should work on any
 *  machine that has node and npm installed.</p>
 *
 *  <p> <a href="http://pm2.keymetrics.io/">pm2</a> is a Node package
 *  installed using npm that can run a process forever and can cause
 *  it to be started upon reboot. <b>Note that for the process to be started
 *  after reboot</b>, a command needs to be run once as root on the host machine.
 *  The command is displayed during code generation.  The command is specific
 *  to the user account on the remote machine.
 *  </p>
 *

 *  <p>To use a SwarmBox, add your <code>~/.ssh/id_rsa.pub</code> file
 *  to <code>swarmboxadmin/ansible/keys/sbuser_authorized_keys</code>.
 *  See <a href="https://www.terraswarm.org/testbeds/wiki/Main/SbuserSSHAccess#in_browser">https://www.terraswarm.org/testbeds/wiki/Main/SbuserSSHAccess</a>.</p>
 *
 *  <p>For more information, see the 
 *  <a href="https://accessors.org/wiki/Main/CapeCodeHost#CodeGeneration">Code Generation wiki</a>.</p>
 *
 *  @author Christopher Brooks.  Based on HTMLCodeGenerator by Man-Kit Leung, Bert Rodiers
 *  @version $Id$
 *  @since Ptolemy II 11.0
 *  @Pt.ProposedRating red (cxh)
 *  @Pt.AcceptedRating red (cxh)
 */
public class AccessorSSHCodeGenerator extends AccessorCodeGenerator {

    /** Create a new instance of the AccessorSSHCodeGenerator.
     *  The value of the <i>generatorPackageList</i> parameter of the
     *  base class is set to <code>generic.accessor</code>
     *  @param container The container.
     *  @param name The name of the AccessorSSHCodeGenerator.
     *  @exception IllegalActionException If the super class throws the
     *   exception or error occurs when setting the file path.
     *  @exception NameDuplicationException If the super class throws the
     *   exception or an error occurs when setting the file path.
     */
    public AccessorSSHCodeGenerator(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        npmInstall = new Parameter(this, "npmInstall");
        npmInstall.setTypeEquals(BaseType.BOOLEAN);
        npmInstall.setExpression("true");

        modules = new StringParameter(this, "modules");
        modules.setExpression("");

        runForever = new Parameter(this, "runForever");
        runForever.setTypeEquals(BaseType.BOOLEAN);
        runForever.setExpression("false");

        stopForeverAccessors = new Parameter(this, "stopForeverAccessors");
        stopForeverAccessors.setTypeEquals(BaseType.BOOLEAN);
        stopForeverAccessors.setExpression("false");

        userHost = new StringParameter(this, "userHost");
        userHost.setExpression("sbuser@10.0.0.1");

        // Invoke the accessoInvokeSSH script
        runCommand.setExpression("@PTII@/ptolemy/cg/kernel/generic/accessor/accessorInvokeSSH @userHost@ @codeDirectory@/@modelName@.js @timeoutFlagAndValue@ @modulesFlagAndValue@ @npmInstall@ @runForever@ @stopForeverAccessors@");

    }

    ///////////////////////////////////////////////////////////////////
    ////                     parameters                            ////

    /** A comma separated list of modules to be installed
     *  on the remote machine.
     *  For to install the Global Data Plane module,
     *  use <code>@terraswarm/gdp</code>.
     *  Note that the <code>@terraswarm/accessors</code>
     *  module is always installed and need not be
     *  specified here.  The initial default value
     *  is the empty string, which signifies that
     *  only the <code>@terraswarm/accessors</code> module
     *  will be installed.
     */
    public StringParameter modules;

    /** If true, then use npm install to install the modules.
     *  The reason to set this to false is if the remote host is
     *  not connected to the internet or if the remote host already
     *  has the modules installed in <code>~/cg/node_modules.</code>
     *  Setting this to false means that the composite accessor
     *  will be deployed more quickly because npm install will not
     *  be run.
     *  The default value is false, indicating that
     *  <code>npm install <i>modules</i></code>
     *  should not be run.
     */
    public Parameter npmInstall;

    /** If true, then use npm forever to run the Node composite
     *  accessor forever.  If false, then run until <i>stopTime</i>
     *  ms. has passed.  Note that if true, then the process will be
     *  restarted after <i>stopTime</i> ms. or until the code
     *  generator is run with <i>stopForeverAccessors</i> is true or
     *  the remote machine is rebooted.  The default value is true,
     *  indicating that the process should be run forever.
     */
    public Parameter runForever;

    /** If true, then connect to the remote machine and stop any npm
     *  forever processes with the same basename as the model.
     *  Confusingly, to stop any remote processes that have been
     *  previously invoked, <i>run</i> should be true so that the
     *  <code>accessorInvokeSSH</code> script is invoked on the remote
     *  machine with a <code>stop</code> argument.
     *  The default value is false, indicating that
     *  the remote machine will invoke the Node accessor host
     *  composite accessor.
     */
    public Parameter stopForeverAccessors;

    /** The username and hostname that is used with ssh.
     *  The default value is "sbuser@swarmnuc001.eecs.berkeley.edu".
     *  To get ssh access, see
     *  <a href="https://www.terraswarm.org/testbeds/wiki/Main/SbuserSSHAccess#in_browser">https://www.terraswarm.org/testbeds/wiki/Main/SbuserSSHAccess</a>.
     */
    public StringParameter userHost;

    /** Return the command to run the generated code.
     *  @return The command to run the generated code.
     *  @exception IllegalActionException If the there is a problem
     *  substituting the @..@ tags.
     */
    protected String _runCommand() throws IllegalActionException {
        Map<String, String> substituteMap = CodeGeneratorUtilities.newMap(this);
        substituteMap.put("@codeDirectory@", codeDirectory.asFile().toString());
        substituteMap.put("@modelName@", _sanitizedModelName);
        substituteMap.put("@PTII@", StringUtilities.getProperty("ptolemy.ptII.dir"));

        // If the value of the npmInstall parameter is true, then pass
        // "npmInstall" as an argument to accessorInvokeSSH.
        if (((BooleanToken) npmInstall.getToken()).booleanValue()) {
            substituteMap.put("@npmInstall@", "npmInstall");
        } else {
            substituteMap.put("@npmInstall@", "");
        }

        // If the value of the runForever parameter is true, then pass
        // "runForever" as an argument to accessorInvokeSSH.
        if (((BooleanToken) runForever.getToken()).booleanValue()) {
            substituteMap.put("@runForever@", "runForever");
        } else {
            substituteMap.put("@runForever@", "");
        }

        // If the value of the stopForeverAccessors parameter is true, then pass
        // "stopForeverAccessors" as an argument to accessorInvokeSSH.
        if (((BooleanToken) stopForeverAccessors.getToken()).booleanValue()) {
            substituteMap.put("@stopForeverAccessors@", "stopForeverAccessors");
        } else {
            substituteMap.put("@stopForeverAccessors@", "");
        }

        // If stopTime is set in the director, then multiply it by
        // 1000 and use it as the timeout of the accessor.
        if (_model instanceof CompositeActor) {
            String stopTime = ((CompositeActor) _model).getDirector().stopTime.getExpression();
            String timeoutFlagAndValue = "";
            if (stopTime.length() > 0) {
                try {
                    timeoutFlagAndValue = "-timeout " + Double.toString(Double.parseDouble(stopTime) * 1000.0);
                } catch (NumberFormatException ex) {
                    throw new IllegalActionException(_model, ex, "Could not parse " + stopTime);
                }
            }
            substituteMap.put("@timeoutFlagAndValue@", timeoutFlagAndValue);
        }

        // The value of the modules parameter names one or more
        // modules to be installed with npm.
        String modulesFlagAndValue = "";
        if (modules.getExpression().length() > 0) {
            String modulesValue = modules.getExpression();
            if (modulesValue.indexOf(" ") != -1) {
                throw new IllegalActionException(this, "The value of the modules parameter (" + modulesValue
                                                 + ") must not contain spaces.  Use commas to separate modules.");
            }
            modulesFlagAndValue = "-modules " + modulesValue;
        }
        substituteMap.put("@modulesFlagAndValue@", modulesFlagAndValue);

        String command = CodeGeneratorUtilities
            .substitute(((StringToken) runCommand.getToken())
                        .stringValue(), substituteMap);
        return command;
    }

}

