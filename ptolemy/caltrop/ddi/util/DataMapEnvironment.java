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


*/
package ptolemy.caltrop.ddi.util;

import caltrop.interpreter.ChannelID;
import caltrop.interpreter.Context;
import caltrop.interpreter.ExprEvaluator;
import caltrop.interpreter.InterpreterException;
import caltrop.interpreter.ast.Expression;
import caltrop.interpreter.ast.InputPattern;
import caltrop.interpreter.environment.Environment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A read-only Environment that wraps a Map of data read from input ports.
 * FIXME: assumes repeat expressions can be evaluated in the environment passed in to the constructor.
 */

public class DataMapEnvironment implements Environment {
    public Object get(Object variable) {
        String varName = (String) variable;
        PortVarInfo pvi = (PortVarInfo) _varNameToVarInfo.get(varName);
        if (pvi != null) {
            // try to find it. if we don't find it, throw an UnboundPortVarException.
            String portName = pvi.getPortName();
            List data = (List) _dataFromInputPorts.get(new ChannelID(portName, 0));
            if (data == null)
                throw new UnboundPortVarException(varName + "unbound.");
            if (pvi.isList()) {
                // if we have read the entire list, return it. otherwise, throw an exception.
                if (data.size() > (pvi._repeatVal * pvi.getLength() - (pvi.getLength()) - pvi.getIndex())) {
                    // we've read enough tokens to construct the entire list.
                    List result = new ArrayList();
                    for (int i = pvi.getIndex(); i < data.size(); i = i + pvi.getLength()) {
                        result.add(data.get(i));
                    }
                    return _context.createList(result);
                } else {
                    throw new UnboundPortVarException(varName + "unbound.");
                }
            } else {
                Object value = data.get(pvi.getIndex());
                if (value == null)
                    throw new UnboundPortVarException(varName + "unbound.");
                else
                    return value;
            }
        } else {
            return _parentEnv.get(variable);
        }
    }

    public Object get(Object variable, Object[] location) {
         throw new InterpreterException("Indices not yet implemented.");  // FIXME
    }

    public void set(Object variable, Object value) {
        throw new InterpreterException("Cannot set() in DataMapEnvironment.");
    }

    public void set(Object variable, Object[] location, Object value) {
        throw new InterpreterException("Cannot set() in DataMapEnvironment.");
    }

    public void bind(Object variable, Object value) {
        throw new InterpreterException("Cannot bind() in DataMapEnvironment.");
    }

    public Set localVars() {
        throw new InterpreterException("localVars() not yet implemented."); // FIXME
    }

    public boolean  isLocalVar(Object variable) {
        throw new InterpreterException("isLocalVar() not yet implemented."); // FIXME
    }

    public Environment newFrame() {
        throw new InterpreterException("Cannot make a new frame in DataMapEnvironment.");
    }

    public void freezeLocal() {
        throw new InterpreterException("Cannot freezeLocal() in DataMapEnvironment.");
    }

    public DataMapEnvironment(InputPattern [] inputPatterns, Map dataFromInputPorts, Environment parentEnv,
                              Context context) {
        _inputPatterns = inputPatterns;
        _dataFromInputPorts = dataFromInputPorts;
        _parentEnv = parentEnv;
        _context = context;
        _eval = new ExprEvaluator(_context, _parentEnv);
        _varNameToVarInfo = createNameToPortVarInfoMap(_inputPatterns);
    }


    private InputPattern [] _inputPatterns;
    private Environment     _parentEnv;
    private Map             _dataFromInputPorts;
    private Context         _context;
    private ExprEvaluator   _eval;
    private Map             _varNameToVarInfo; //String varName -> PortVarInfo

    private Map createNameToPortVarInfoMap(InputPattern [] inputPatterns) {
        Map result = new HashMap();
        for (int i = 0; i < inputPatterns.length; i++) {
            InputPattern inputPattern = inputPatterns[i];
            int repeatVal = -1; // no repeat expression.
            Expression repeatExpr = inputPattern.getRepeatExpr();
            boolean isList = (repeatExpr == null ? false : true);
            if (isList) {
                repeatVal = _context.intValue(_eval.evaluate(repeatExpr));
                if (repeatVal < 0) {
                    throw new InterpreterException("Repeat expressions must evaluate to nonnegative values.");
                }
            }
            String [] variables = inputPattern.getVariables();
            for (int j = 0; j < variables.length; j++) {
                String variable = variables[j];
                result.put(variable, new PortVarInfo(inputPattern.getPortname(), j, variables.length, isList,
                        repeatVal));
            }
        }
        return result;
    }

    public static class UnboundPortVarException extends InterpreterException {
        public UnboundPortVarException(String msg) {
            super(msg);
        }
    }

    private static class PortVarInfo {
        private String _portName;
        private int _index;
        private int _length;
        private boolean _isList;
        private int _repeatVal;

        public PortVarInfo(String portName, int index, int length, boolean isList, int repeatVal) {
            this._portName = portName;
            this._index = index;
            this._length = length;
            this._isList = isList;
            this._repeatVal = repeatVal;
        }

        public String getPortName() {
            return _portName;
        }

        public int getIndex() {
            return _index;
        }

        public int getLength() {
            return _length;
        }

        public boolean isList() {
            return _isList;
        }

        public int getRepeatVal() {
            return _repeatVal;
        }
    }
}
