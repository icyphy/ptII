/*
Below is the copyright agreement for the Ptolemy II system.
Version: $Id$

Copyright (c) 2009 The Regents of the University of California.
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
package ptolemy.codegen.rtmaude.kernel;

import java.util.ArrayList;
import java.util.List;

import ptolemy.actor.IOPort;
import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

public class RTMaudeAdaptor extends CodeGeneratorHelper {

    public RTMaudeAdaptor(NamedObj component) {
        super(component);
    }

    // FIXME: the period value is required to be an integer. We have
    // generate an integer even if the model has a double value.

    protected String _generateFireCode() throws IllegalActionException {


        StringBuffer code = new StringBuffer();
        StringBuffer parameterCode = new StringBuffer();
        StringBuffer portCode = new StringBuffer();

        List<Variable> parameters = (List<Variable>) _getParameters();

        ArrayList args = new ArrayList();
        args.add("");

        // FIXME: deal with the last comma later.
        for (Variable parameter : parameters) {
            args.set(0, parameter.getName());
            parameterCode.append(_generateBlockCode("parameterBlock", args) + ", ");
        }

        List<IOPort> ports = (List<IOPort>) _getPorts();

        ArrayList args2 = new ArrayList();
        args2.add("");
        args2.add("");
        args2.add("");

        // FIXME: deal with the last comma later.
        for (IOPort port : ports) {
            args2.set(0, port.getName());

            // Assume we are not dealing with in-out ports.
            args2.set(1, port.isInput()? "InPort" : "OutPort");

            args2.set(2, "absent");
            portCode.append(_generateBlockCode("portBlock", args2) + ", ");
        }

        ArrayList args3 = new ArrayList();
        args3.add(parameterCode);
        args3.add(portCode);
        code.append(_generateBlockCode("fireBlock", args3));

        return code.toString();
    }

    protected List<IOPort> _getPorts() {
        Entity actor = (Entity) getComponent();
        return actor.portList();
    }

    protected List<Variable> _getParameters() {
        // Note: subclass should override this method.
        return new ArrayList();
    }
}
