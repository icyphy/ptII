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
package ptolemy.caltrop.ddi;

import caltrop.interpreter.ChannelID;
import caltrop.interpreter.Context;
import caltrop.interpreter.ExprEvaluator;
import caltrop.interpreter.StmtEvaluator;
import caltrop.interpreter.ast.Action;
import caltrop.interpreter.ast.Actor;
import caltrop.interpreter.ast.Decl;
import caltrop.interpreter.ast.Expression;
import caltrop.interpreter.ast.InputPattern;
import caltrop.interpreter.ast.OutputExpression;
import caltrop.interpreter.ast.PortDecl;
import caltrop.interpreter.ast.Statement;
import caltrop.interpreter.environment.Environment;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.caltrop.actors.CalInterpreter;
import ptolemy.caltrop.ddi.util.DataMapEnvironment;
import ptolemy.data.Token;
import ptolemy.domains.csp.kernel.ConditionalBranch;
import ptolemy.domains.csp.kernel.ConditionalBranchController;
import ptolemy.domains.csp.kernel.ConditionalReceive;
import ptolemy.domains.csp.kernel.ConditionalSend;
import ptolemy.kernel.util.IllegalActionException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

//////////////////////////////////////////////////////////////////////////
//// CSP
/**
@author Christopher Chang <cbc@eecs.berkeley.edu>
@version $Id$
@since Ptolemy II 3.1
*/
public class CSP extends AbstractDDI implements DDI {

    public CSP(CalInterpreter ptActor, Actor actor,
            Context context, Environment env) {
        _ptActor = ptActor;
        _actor = actor;
        _context = context;
        _env = env;
        _eval = new ExprEvaluator(_context, _env);
        _ioPorts = _createIOPortMap();
        _cbc = new ConditionalBranchController(_ptActor);
    }

    private CalInterpreter _ptActor;
    private Actor _actor;
    private Context _context;
    private Environment _env;
    private ExprEvaluator _eval;
    private Map _ioPorts;
    private ConditionalBranchController _cbc;

    public boolean isLegalActor() {
        return true; //FIXME
    }

    public void setupActor() {
    }

    public String getName() {
        return "CSP";
    }

    public void fire() throws IllegalActionException {
        // assume repeat expressions are statically computable,
        // and no multiport support (always assume channel 0) FIXME

        Action [] actions = _actor.getActions();
        Map inputProfile;
        Map dataSoFar = new HashMap();
        while (true) {
            actions = filterActions(actions, dataSoFar);
            if (actions.length <= 1)
                break;
            inputProfile = computeSafeTokens(actions, dataSoFar);
            if (inputProfile.isEmpty())
                break;
            readSafeTokens(inputProfile, dataSoFar);
        }
        if (actions.length == 0) {
            return;
        } else if (actions.length == 1) {
            inputProfile = computeRemainingTokens(actions, dataSoFar);
            mergeData(dataSoFar, new CSPTokenReader(inputProfile, _ioPorts, _cbc).getAll());
            fireAction(actions[0], dataSoFar);
        } else {
            inputProfile = computeRemainingTokens(actions, dataSoFar);
            if (isRemainingProfileValid(inputProfile, actions)) {
                readOneFromRemaining(inputProfile, dataSoFar);
                fireMatchingAction(actions, dataSoFar);
            } else {
                throw new DDIException("Illegal CSP actor encountered.");
            }
        }
    }

    private void _evaluateBody(Statement [] body, Environment env) {
        StmtEvaluator eval = new StmtEvaluator(_context, env);
        for (int i = 0; i < body.length; i++) {
            eval.evaluate(body[i]);
        }
    }

    private Environment _bindActionStateVars(Decl[] decls, Environment env) {
        ExprEvaluator eval = new ExprEvaluator(_context, env);
        for (int i = 0; i < decls.length; i++) {
            Expression v = decls[i].getInitialValue();
            if (v == null)
                env.bind(decls[i].getName(), null);
            else
                env.bind(decls[i].getName(), eval.evaluate(v));
        }
        return env;
    }

    private Environment _bindInputPatternVars(InputPattern[] inputPatterns,
            Map inputData, Environment env) {
        for (int i = 0; i < inputPatterns.length; i++) {
            InputPattern inputPattern = inputPatterns[i];
            ChannelID chID = new ChannelID(inputPattern.getPortname(), 0);
            List data = (List) inputData.get(chID);
            Expression repeatExpr = inputPattern.getRepeatExpr();
            if (repeatExpr == null) {
                String [] vars = inputPattern.getVariables();
                for (int j = 0; j < vars.length; j++) {
                    String varName = vars[j];
                    env.bind(varName, data.get(j));
                }
            } else {
                String [] vars = inputPattern.getVariables();
                List [] l = new List[vars.length];
                for (int j = 0; j < l.length; j++) {
                    l[j] = new ArrayList();
                }
                int repeatVal =_context.intValue(new ExprEvaluator(_context,
                        env).evaluate(repeatExpr));
                for (int j = 0; j < repeatVal; j++) {
                    for (int k = 0; k < vars.length; k++) {
                        l[k].add(data.get(j*vars.length + k));
                    }
                }
                for (int j = 0; j < vars.length; j++) {
                    String varName = vars[j];
                    env.bind(varName, _context.createList(l[j]));
                }
            }
        }
        return env;
    }

    public void initialize() throws IllegalActionException {
    }

    public boolean postfire() throws IllegalActionException {
        return true;
    }

    public boolean prefire() throws IllegalActionException {
        return true;
    }

    public void preinitialize() throws IllegalActionException {
    }

    private Map _createIOPortMap() {
        Map ports = new HashMap();
        PortDecl [] inputPorts = _actor.getInputPorts();
        for (int i = 0; i < inputPorts.length; i++) {
            String name = inputPorts[i].getName();
            TypedIOPort port = (TypedIOPort) _ptActor.getPort(name);
            ports.put(name, port);
        }
        PortDecl [] outputPorts = _actor.getOutputPorts();
        for (int i = 0; i < outputPorts.length; i++) {
            String name = outputPorts[i].getName();
            TypedIOPort port = (TypedIOPort) _ptActor.getPort(name);
            ports.put(name, port);
        }
        return ports;
    }

    private Map _computeOutputData(OutputExpression [] outputExprs,
            Environment env) {
        // FIXME no multi.
        Map data = new HashMap();
        ExprEvaluator eval = new ExprEvaluator(_context, env);
        for (int i = 0; i < outputExprs.length; i++) {
            OutputExpression outputExpr = outputExprs[i];
            Expression repeatExpr = outputExpr.getRepeatExpr();
            List results = new ArrayList();
            Expression [] exprs = outputExpr.getExpressions();
            if (repeatExpr == null) {
                for (int j = 0; j < exprs.length; j++) {
                    Expression expr = exprs[j];
                    results.add(eval.evaluate(expr));
                }
            } else {
                int repeatVal = _context.intValue(eval.evaluate(repeatExpr));
                List [] values = new List[exprs.length];
                for (int j = 0; j < values.length; j++) {
                    values[j] = _context.listValue(eval.evaluate(exprs[j]));
                }
                for (int j = 0; j < repeatVal; j++) {
                    for (int k = 0; k < exprs.length; k++) {
                        results.add(values[k].get(j));
                    }
                }
            }
            data.put(new ChannelID(outputExprs[i].getPortname(), 0), results);
        }
        return data;
    }

    private Map computeSafeTokens(Action [] actions, Map dataSoFar) {
        Map inputProfile = new HashMap();
        if (actions.length == 0)
            return inputProfile;

        int numNeeded;

        if (actions.length == 1) {
            InputPattern [] inputPatterns = actions[0].getInputPatterns();
            for (int i = 0; i < inputPatterns.length; i++) {
                InputPattern inputPattern = inputPatterns[i];
                numNeeded = numTokensNeeded(inputPattern);
                List data = (List) dataSoFar.get(
                        new ChannelID(inputPattern.getPortname(), 0));
                if (data != null) {
                    numNeeded = numNeeded - data.size();
                }
                if (numNeeded > 0)
                    inputProfile.put(new ChannelID(inputPattern.getPortname(),
                            0), new Integer(numNeeded));
            }
            return inputProfile;
        }

        InputPattern [] inputPatterns = actions[0].getInputPatterns();
        if (inputPatterns.length == 0)
            // if the first action has no input patterns,
            // then the "intersection" will be empty.
            return inputProfile;

        for (int i = 0; i < inputPatterns.length; i++) {
            InputPattern inputPattern = inputPatterns[i];
            numNeeded = numTokensNeeded(inputPattern);
            for (int j = 1; j < actions.length; j++) {
                InputPattern ip = getInputPattern(inputPattern.getPortname(),
                        actions[j]);
                if (ip == null) {
                    numNeeded = 0;
                    break;
                } else {
                    numNeeded = Math.min(numNeeded, numTokensNeeded(ip));
                    List data = (List) dataSoFar.get(new ChannelID(inputPattern.getPortname(), 0));
                    if (data != null) {
                        numNeeded = numNeeded - data.size();
                    }
                }
            }
            if (numNeeded > 0) {
                inputProfile.put(new ChannelID(inputPattern.getPortname(), 0), new Integer(numNeeded));
            }
        }
        return inputProfile;
    }

    private int numTokensNeeded(InputPattern ip) {
        int repeatVal = 1;
        Expression repeatExpr = ip.getRepeatExpr();
        if (repeatExpr != null) {
            repeatVal = _context.intValue(_eval.evaluate(repeatExpr));
        }
        return repeatVal * ip.getVariables().length;
    }

    private InputPattern getInputPattern(String name, Action action) {
        InputPattern [] inputPatterns = action.getInputPatterns();
        for (int i = 0; i < inputPatterns.length; i++) {
            InputPattern inputPattern = inputPatterns[i];
            if (inputPattern.getPortname().equals(name))
                return inputPattern;
        }
        return null;
    }

    private void readSafeTokens(Map inputProfile, Map dataSoFar) {
        CSPTokenReader tokenReader = new CSPTokenReader(inputProfile, _ioPorts, _cbc);
        Map inputData = tokenReader.getAll();
        mergeData(dataSoFar, inputData);
    }

    // oldData and newData are both Map: ChannelID -> List[Object]
    private void mergeData(Map oldData, Map newData) {
        for (Iterator iterator = newData.keySet().iterator(); iterator.hasNext();) {
            ChannelID chID = (ChannelID) iterator.next();
            if (oldData.containsKey(chID)) {
                ((List) oldData.get(chID)).addAll((List) newData.get(chID));
            } else {
                oldData.put(chID, newData.get(chID));
            }
        }
    }

    // for each action in the list, try to evaluate its guards if possible, given
    // the data we've read so far. if any of the guards evaluates to false, eliminate that action from the set.
    private Action [] filterActions(Action [] actions, Map dataSoFar) {
        List result = new LinkedList();
        for (int i = 0; i < actions.length; i++) {
            Action action = actions[i];
            Expression [] guardExprs = action.getGuards();
            boolean guardVal = true;
            if (guardExprs.length > 0) {
                InputPattern [] inputPatterns = action.getInputPatterns();
                Environment env = new DataMapEnvironment(inputPatterns, dataSoFar, _env, _context);
                ExprEvaluator eval = new ExprEvaluator(_context, env);
                for (int j = 0; j < guardExprs.length; j++) {
                    Expression guardExpr = guardExprs[j];
                    try {
                        if (!_context.booleanValue(eval.evaluate(guardExpr))) {
                            guardVal = false;
                            break;
                        }
                    } catch (DataMapEnvironment.UnboundPortVarException e) {
                        continue; // don't know if it's true or false yet.
                    }
                }
            }
            if (guardVal) {
                result.add(action);
            }
        }
        Action [] as = new Action[result.size()];
        for (int i = 0; i < as.length; i++) {
            as[i] = (Action) result.get(i);
        }
        return as;
    }

    private Map computeRemainingTokens(Action [] actions, Map dataSoFar) {
        Map profile = new HashMap();
        for (int i = 0; i < actions.length; i++) {
            Action action = actions[i];
            for (int j = 0; j < action.getInputPatterns().length; j++) {
                InputPattern inputPattern = action.getInputPatterns()[j];
                ChannelID chID = new ChannelID(inputPattern.getPortname(), 0);
                int numNeeded = numTokensNeeded(inputPattern);
                int numHave = 0;
                List  data = (List) dataSoFar.get(chID);
                if (data != null) {
                    numHave = data.size();
                }
                if (numHave >= numNeeded)
                    continue;
                Integer needSoFar = (Integer) profile.get(chID);
                if (needSoFar == null) {
                    profile.put(chID, new Integer(numNeeded - numHave));
                } else {
                    profile.put(chID, new Integer(Math.max(numNeeded - numHave, needSoFar.intValue())));
                }
            }
        }
        return profile;
    }

    // for the last "burst" of reads, we can only read at most one token from each channel.
    private boolean isRemainingProfileValid(Map inputProfile, Action [] actions) {
        for (int i = 0; i < actions.length; i++) {
            Action action = actions[i];
            InputPattern [] inputPatterns = action.getInputPatterns();
            boolean encountered = false;
            for (int j = 0; j < inputPatterns.length; j++) {
                InputPattern inputPattern = inputPatterns[j];
                String name = inputPattern.getPortname();
                ChannelID chID = new ChannelID(name, 0);
                if (inputProfile.containsKey(chID)) {
                    if (!encountered) {
                        encountered = true;
                    } else {
                        return false;
                    }
                    if (((Integer) inputProfile.get(chID)).intValue() > 1)
                        return false;
                }
            }
        }
        return true;
    }

    private void readOneFromRemaining(Map inputProfile, Map dataSoFar) {
        if (!moreDataToRead(inputProfile))
            return;
        CSPTokenReader reader = new CSPTokenReader(inputProfile, _ioPorts, _cbc);
        CSPTokenReader.DataChannelID dchID = reader.getOne();
        ChannelID chID = dchID.getChannelID();
        Object newData = dchID.getData();
        List data = (List) dataSoFar.get(chID);
        if (data == null) {
            List l = new ArrayList();
            l.add(newData);
            dataSoFar.put(chID, l);
        } else {
            data.add(newData);
        }
    }

    private boolean moreDataToRead(Map inputProfile) {
        for (Iterator iterator = inputProfile.keySet().iterator(); iterator.hasNext();) {
            ChannelID chID = (ChannelID) iterator.next();
            if (((Integer) inputProfile.get(chID)).intValue() > 0)
                return true;
        }
        return false;
    }


    private void fireMatchingAction(Action [] actions, Map dataSoFar) {
        Action action = selectAction(actions, dataSoFar);
        fireAction(action, dataSoFar);
    }

    private void fireAction(Action action, Map dataSoFar) {
        Environment env = _bindInputPatternVars(action.getInputPatterns(), dataSoFar,  _env.newFrame());
        env = _bindActionStateVars(action.getDecls(), env.newFrame());
        _evaluateBody(action.getBody(), env);
        Map outputData = _computeOutputData(action.getOutputExpressions(), env);
        CSPTokenWriter tokenWriter = new CSPTokenWriter(_ioPorts, _cbc);
        tokenWriter.put(outputData);
    }

    private Action selectAction(Action [] actions, Map dataSoFar) {
        for (int i = 0; i < actions.length; i++) {
            Action action = actions[i];
            if (isFirable(action, dataSoFar)) {
                return action;
            }
        }
        throw new DDIException("selectAction() failed to find a firable action.");
    }

    private boolean isFirable(Action action, Map dataSoFar) {
        InputPattern [] inputPatterns = action.getInputPatterns();
        for (int i = 0; i < inputPatterns.length; i++) {
            InputPattern inputPattern = inputPatterns[i];
            int numNeeded = numTokensNeeded(inputPattern);
            List data = (List) dataSoFar.get(new ChannelID(inputPattern.getPortname(), 0));
            int numHave = (data == null) ? 0 : data.size();
            if (numNeeded > numHave) {
                return false;
            }
        }
        return true;
    }
}

class CSPTokenReader {
    // List[Object]
    private List [] _data;
    // (ChannelID -> Integer)
    private Map _profile;
    private ChannelID [] _indexToChannelID;
    // (ChannelID -> Integer)
    private Map _channelIDToIndex;
    // (String -> TypedIOPort)
    private Map _ioPorts;
    private int [] _count;
    private ConditionalBranchController _cbc;
    private boolean _done;

    public CSPTokenReader(Map profile, Map ioPorts, ConditionalBranchController cbc) {
        _ioPorts = ioPorts;
        _profile = profile;
        _cbc = cbc;

        _count = new int[profile.keySet().size()];
        _indexToChannelID = new ChannelID[_count.length];
        _data = new List[_count.length];
        for (int i = 0; i < _data.length; i++) {
            _data[i] = new ArrayList();
        }
        _channelIDToIndex = new HashMap();

        int i = 0;
        for (Iterator iterator = profile.keySet().iterator(); iterator.hasNext();) {
            ChannelID chID = (ChannelID) iterator.next();
            _indexToChannelID[i] = chID;
            _channelIDToIndex.put(chID, new Integer(i));
            i++;
        }
        _done = false;
        _resetCount();
    }

    public Object get(ChannelID chID, int index) {
        if (!_done) {
            try {
                _read();
            } catch (IllegalActionException e) {
                throw new DDIException("Error reading token.", e);
            }
        }
        if (index >= ((Integer) _profile.get(chID)).intValue()) {
            throw new DDIException("Attempt to read token beyond input channel profile.");
        }
        return _data[((Integer) _channelIDToIndex.get(chID)).intValue()];
    }

    public void reset() {
        _done = false;
        _resetCount();
        for (int i = 0; i < _data.length; i++) {
            _data[i].clear();
        }
    }

    public Map getAll() {
        if (!_done) {
            try {
                _read();
            } catch (IllegalActionException e) {
                throw new DDIException("Error reading token.", e);
            }
        }
        return _dataToMap();
    }

    private Map _dataToMap() {
        Map data = new HashMap();
        for (int i = 0; i < _data.length; i++) {
            data.put(_indexToChannelID[i], _data[i]);
        }
        return data;
    }

    private void _read()
            throws IllegalActionException {
        CalInterpreter ptActor = (CalInterpreter) _cbc.getParent();

        while (true) {
            ConditionalBranch [] branches = _createBranches();
            ptActor.printDebug("Calling chooseBranch() to receive...");
            int result = _cbc.chooseBranch(branches);
            if (result != -1) {
                Token t = branches[result].getToken();
                _data[result].add(t);
                _count[result]--;
                ptActor.printDebug("Received " + t.toString() + " on port " + _indexToPort(result) + ", channel " +
                        _indexToChannelNumber(result));
            } else {
                _done = true;
                return;
            }
        }
    }

    public DataChannelID getOne() {
        CalInterpreter ptActor = (CalInterpreter) _cbc.getParent();

        try {
            if (!_done) {
                ConditionalBranch [] branches = _createBranches();
                ptActor.printDebug("Calling chooseBranch() to receive...");
                int result = _cbc.chooseBranch(branches);
                if (result != -1) {
                    Token t = branches[result].getToken();
                    _data[result].add(t);
                    _count[result]--;
                    ptActor.printDebug("Received " + t.toString() + " on port " + _indexToPort(result) + ", channel " +
                            _indexToChannelNumber(result));
                    return new DataChannelID(t, _indexToChannelID[result]);
                } else {
                    _done = true;
                    return null;
                }

            } else {
                return null;
            }
        } catch (IllegalActionException e) {
            throw new DDIException("Error reading token.", e);
        }
    }

    private ConditionalReceive [] _createBranches() throws IllegalActionException {
        ConditionalReceive [] branches = new ConditionalReceive[_count.length];
        CalInterpreter ptActor = (CalInterpreter) _cbc.getParent();

        for (int i = 0; i < _count.length; i++) {
            if (_count[i] > 0) {
                ptActor.printDebug("Creating a ConditionalReceive to receive on port " +
                        _indexToPort(i).getFullName() + ", channel " + _indexToChannelNumber(i));
                branches[i] = new ConditionalReceive(true, _indexToPort(i), _indexToChannelNumber(i), i, _cbc);
            } else {
                branches[i] = new ConditionalReceive(false, _indexToPort(i), _indexToChannelNumber(i), i, _cbc);
            }
        }
        return branches;
    }

    // assumes _indexToChannelID has been set correctly.
    private int _indexToChannelNumber(int index) {
        return _indexToChannelID[index].getChannelNumber();
    }

    // assumes _ioPorts and _indexToChannelID have been set correctly.
    private IOPort _indexToPort(int index) {
        return (IOPort) _ioPorts.get(_indexToChannelID[index].getPortName());
    }

    // assumes _count has been created, and _profile and _indexToChannelID are set correctly.
    private void _resetCount() {
        for (int i = 0; i < _count.length; i++) {
            _count[i] = ((Integer) _profile.get(_indexToChannelID[i])).intValue();
        }
    }

    public static class DataChannelID {
        private Object _data;
        private ChannelID _chID;

        public DataChannelID(Object data, ChannelID chID) {
            this._data = data;
            this._chID = chID;
        }

        public Object getData() {
            return _data;
        }

        public ChannelID getChannelID() {
            return _chID;
        }
    }
}

class CSPTokenWriter {
    // (String -> TypedIOPort)
    private Map _ioPorts;
    private ConditionalBranchController _cbc;
    // (ChannelID -> Integer)
    private Map _channelIDToIndex;

    private ChannelID [] _indexToChannelID;
    private int [] _count;
    private Map _data;

    public CSPTokenWriter(Map ioPorts, ConditionalBranchController cbc) {
        _ioPorts = ioPorts;
        _cbc = cbc;
        _channelIDToIndex = new HashMap();
    }

    public void put(Map data) {
        if (data.isEmpty())
            return;
        updateState(data);
        try {
            _write();
        } catch (IllegalActionException e) {
            throw new DDIException("Error writing token.", e);
        }
    }

    private void updateState(Map data) {
        for (Iterator iterator = data.keySet().iterator(); iterator.hasNext();) {
            ChannelID channelID = (ChannelID) iterator.next();
            List l = (List) data.get(channelID);
            for (Iterator i2 = l.iterator(); i2.hasNext();) {
                Object o = i2.next();
                assert o instanceof Token;
            }
        }

        _data = data;
        _indexToChannelID = new ChannelID[data.keySet().size()];
        _channelIDToIndex.clear();
        _count = new int[_indexToChannelID.length];
        _resetCount();

        int i = 0;
        for (Iterator iterator = data.keySet().iterator(); iterator.hasNext();) {
            ChannelID chID = (ChannelID) iterator.next();
            _indexToChannelID[i] = chID;
            _channelIDToIndex.put(chID, new Integer(i));
            i++;
        }
    }

    private void _write()
            throws IllegalActionException {

        CalInterpreter ptActor = (CalInterpreter) _cbc.getParent();

        while (true) {
            ConditionalBranch [] branches = _createBranches();
            ptActor.printDebug("Calling chooseBranch() to send...");
            int result = _cbc.chooseBranch(branches);
            if (result != -1) {
                _count[result]++;
                ptActor.printDebug("Sent on port " + _indexToPort(result) + " channel " + _indexToChannelNumber(result));
            } else {
                return;
            }
        }
    }

    private ConditionalSend [] _createBranches() throws IllegalActionException {
        ConditionalSend [] branches = new ConditionalSend[_count.length];

        CalInterpreter ptActor = (CalInterpreter) _cbc.getParent();
        //int count = 0;
        //List branchl = new ArrayList();

        for (int i = 0; i < _count.length; i++) {
            List datal = (List) _data.get(_indexToChannelID[i]);
            if (_count[i] < datal.size()) {
                ptActor.printDebug("Creating a ConditionalSend to send " +
                        datal.get(_count[i]) +  " on port " +
                        _indexToPort(i).getFullName() + " channel " +
                        _indexToChannelNumber(i));
                branches[i] = new ConditionalSend(true, _indexToPort(i), _indexToChannelNumber(i), i,
                        (Token) datal.get(_count[i]), _cbc);
            } else {
                branches[i] = new ConditionalSend(false, _indexToPort(i), _indexToChannelNumber(i), i, null, _cbc);
            }
            //}
        }
        return branches;
    }

    // assumes _indexToChannelID has been set correctly.
    private int _indexToChannelNumber(int index) {
        return _indexToChannelID[index].getChannelNumber();
    }

    // assumes _ioPorts and _indexToChannelID have been set correctly.
    private IOPort _indexToPort(int index) {
        return (IOPort) _ioPorts.get(_indexToChannelID[index].getPortName());
    }

    private void _resetCount() {
        for (int i = 0; i < _count.length; i++) {
            _count[i] = 0;
        }
    }
}


