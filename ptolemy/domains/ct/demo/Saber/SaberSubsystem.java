/* An actor that wrap a Saber subsystem

 Copyright (c) 1998-2000 The Regents of the University of California.
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
@ProposedRating Red (liuj@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)

*/

package ptolemy.domains.ct.demo.Saber;
import ptolemy.domains.ct.kernel.*;
import ptolemy.domains.ct.lib.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.data.expr.Parameter;
import java.util.*;
import java.io.*;

//////////////////////////////////////////////////////////////////////////
//// CTSaberSubsystem
/**
A subsystem implements and simulates in Saber. This is basically a tool
interface. The basic actor has no input no output. The ports can be added
by addPort or set the port's container to this actor.
For each input port, there is one parameter, the varName.
For each output port, there is one parameter, the nodeId.
<P>
Note: This actor is still under modification, and is lack of test,
please avoid using it.
@author Jie Liu, William Wu
@version $Id$
*/

public class SaberSubsystem extends CTActor
    implements IPCInterface, CTDynamicActor{

    /** Construct the actor. Default implementation has no
     *  input port, no output port. The ports can be added by
     *  creating new port with this actor as the container.
     * @param container The TypedCompositeActor this star belongs to
     * @param name The name
     * @exception NameDuplicationException If another star already had
     * this name
     * @exception IllegalActionException If there is an internal error
     */
    public SaberSubsystem(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        _netlist = new String("saber");
        paramNetlist = new Parameter(this, "Netlist",
                new StringToken(_netlist));
        _innerStep = new String("10u");
        paramInnerStep = new Parameter(this, "InnerStepSize",
                new StringToken(_innerStep));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the external tool process.
     */
    public Process getToolProcess() {
        return _tool;
    }

    /** Return the name of the tool
     */
    public String getToolName() {
        return _startCommand + _netlist;
    }

    /** Initialize: start the saber process.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _debug(getFullName() + "In initialize");
        super.initialize();
        _debug(getFullName() + "update parameters");
        updateParameters();
        // initialize port table.
        _numout = 0;
        _porttable = new Hashtable();
        _outpvalue = new Hashtable();
        Enumeration ports = getPorts();
        while (ports.hasMoreElements()) {
            TypedIOPort p = (TypedIOPort)ports.nextElement();
            String ppname = p.getName() + "ToolVar";
            Parameter pp = (Parameter)getAttribute(ppname);
            String varname = ((StringToken)pp.getToken()).stringValue();
            if(_porttable.put(varname, p) != null) {
                throw new IllegalActionException( this,
                        "two ports has the same tool variables");
            }
            if(p.isOutput()) {
                _numout ++;
            }
        }
        _outtoken = new DoubleToken[_numout];
        // start saber
        try{
            _debug(getFullName() + "starts saber with "+getToolName());
            _tool = Runtime.getRuntime().exec(getToolName());
            _instream = _tool.getInputStream();
            _errorstream = _tool.getErrorStream();
            _outstream = _tool.getOutputStream();
        } catch (IOException e){
            throw new IllegalActionException(this, "can not execute tool");
        }
        // read in the netlist
        if ((_instream != null) && (_outstream != null)){
            _debug(getFullName() + ": tool start ok");
            _ps = new PrintWriter(_outstream, true);
            if (_ps != null){
                _debug(getFullName() + "read netlist "+_netlist);
            }
            _reader = new BufferedReader(
                    new InputStreamReader(_instream), 1000);
            boolean show = false;
            if(_reader == null) {
                throw new IllegalActionException(this,
                        "instream reading error.");
            }
            _ps.println();
            while (true){
                try {
                    String line = _reader.readLine();
                    _debug(line);
                    if (line.startsWith(new String(">"))){
                        break;
                    }
                }catch (IOException e){
                    throw new IllegalActionException(this,
                            "IO error while in reading" + e.getMessage());
                }
            }
            // perform DC analysis
            _debug(getFullName() + "perform dc analysis");
            _ps.println("dc (dcep " + _startpt);
            _ps.println("di "+_startpt);
	    _ps.println();
            _ps.println("bye");
            while (true){
                try {
                    //if(_reader.ready()) {
                    String line = _reader.readLine();
                    _debug(line);
                    if (line == null) continue;
                    if (line.startsWith(new String("bye"))){
                        break;
                    }
                    //}
                }catch (IOException e){
                    throw new IllegalActionException(this,
                            "IO error while in reading" + e.getMessage());
                }
            }

            //read in the DC analysis as the initial state.
            _ps.println("di");
            _ps.println("bye");
            int outindex = 0;
            while (true) {
                try {
                    String line = _reader.readLine();
                    _debug(line);
                    if (line == null) continue;
                    if (line.startsWith(new String("bye"))){
                        break;
                    }
                    int locsep = line.indexOf(' ');
                    if (locsep < 0) continue;
                    String nname = line.substring(0, locsep);
                    _debug(getFullName() + "gets dc var "+ nname);
                    if(_porttable.containsKey(nname)) {
                        TypedIOPort po = (TypedIOPort)_porttable.get(nname);
                        if(po.isOutput()) {
                            double pd = _parseNumber(line, locsep,
                                    line.length());
                            _debug(getFullName() + " gets var value: "+pd);
                            _outtoken[outindex] = new DoubleToken(pd);
                                //po.broadcast(_outtoken[outindex]);
                            _outpvalue.put(po, _outtoken[outindex]);
                            outindex ++;
                        }
                    }
                } catch (IOException e){
                    throw new IllegalActionException(this,
                            "IO error while in reading" + e.getMessage());

                }
            }
        }
        _first = true;
    }

    /** emit the output again.
     */
    public void emitTentativeOutputs() throws IllegalActionException {
        Enumeration outps = outputPorts();
        while(outps.hasMoreElements()) {
            TypedIOPort p = (TypedIOPort)outps.nextElement();
            DoubleToken d = (DoubleToken)_outpvalue.get(p);
            p.broadcast(d);
            _debug(getFullName() + " port " + p.getName() +
                    " output token" + d.stringValue());
        }
    }

    /** fire
     */
    public void fire() throws IllegalActionException {
        _debug(getFullName() + "In fire");

        //  ps.println("run transient analysis with input, and init file");
        // ps.println("tr (dfile data, pfile plot, tend 2m, tstep 10u"));
        CTDirector dir = (CTDirector)getDirector();
        double now = dir.getCurrentTime();
        double endTime = now + dir.getCurrentStepSize();
        _debug(getFullName() + "endTime: "+endTime);
        Enumeration inports = inputPorts();
        while (inports.hasMoreElements()) {
            IOPort p = (IOPort) inports.nextElement();
            Parameter pparam = (Parameter)getAttribute(p.getName() + "ToolVar");
            String ppstr = ((StringToken)pparam.getToken()).stringValue();
            String indata = ((DoubleToken)p.get(0)).stringValue();
            _ps.println("alter /"+ ppstr +" = " + indata);

        }
        //_ps.println("tr (tend " + endTime +
        //            " , trep " + _endpt + " , trip " + _startpt +
        //            " , tripeqtrep yes, tstep " + _innerStep);

        if(_first) {
            _ps.println("tr (tend " + endTime + ", tstep "+ _innerStep );
            _first = false;
        } else {
            _ps.println("cont tr (tend " + endTime + ", tstep "+_innerStep);
        }
        _ps.println("bye");
        while (true){
            try {
                String line = _reader.readLine();
                _debug(line);
                if (line == null) continue;
                if (line.startsWith(new String("bye"))){
                    break;
                }
            }catch (IOException e){
                //throw new IllegalActionException(this,
                //        "IO error while in reading" + e.getMessage());
                if (_tool == null) {
                    throw new IllegalActionException(this,
                            "External tool terminated.");
                }
                _instream = _tool.getInputStream();
                _errorstream = _tool.getErrorStream();
                _outstream = _tool.getOutputStream();

                _reader = new BufferedReader(
                        new InputStreamReader(_instream));
                if(_reader == null) {
                    throw new IllegalActionException(this,
                            " Can't refresh input buffer." +
                            " IO error while in reading " + e.getMessage());
                }
                _ps = new PrintWriter(_outstream, true);
                if (_ps == null){
                    throw new IllegalActionException(this,
                            " Can't refresh output buffer." +
                            " IO error while in reading " + e.getMessage());
                }
            }
        }
        // get output.
        Enumeration outports = outputPorts();
        String outstr = new String();
        int outindex = 0;
        while(outports.hasMoreElements()) {
            IOPort p = (IOPort) outports.nextElement();
            Parameter pparam = (Parameter)getAttribute(p.getName() + "ToolVar");
            String ppstr = ((StringToken)pparam.getToken()).stringValue();
            outstr = outstr + " " + ppstr;
            outindex ++;
        }
        if (outindex <= 0) {
            return;
        }
        _outvar = new double[outindex];
        _ps.println("pr (cname "+ outstr + ", XSTEP " + endTime);
        _ps.println("bye");
        boolean firstsep = false;
        boolean secondsep = false;
        while (true){
            try {
                String line = _reader.readLine();
                _debug(line);
                if (secondsep){
                    if (line.startsWith(new String(">"))){
                        secondsep = false;
                    }else {
                        // assume the time is in units of m or u
                        int locsep = line.indexOf('|');
                        if (locsep < 0) continue;
                        // get time value
                        //double time = _parseNumber(line, 0, locsep);

                        int tmp = locsep+1;
                        for (int i = 0; i < outindex; i++) {
                            // get ride of space between
                            // separator and first number
                            while (line.charAt(tmp) == ' '){
                                tmp++;
                            }
                            int tmp2;
                            if( i == outindex-1) {
                                tmp2 = line.length();
                            } else {
                                tmp2 = line.indexOf(' ', tmp);
                            }
                            _outvar[i] = _parseNumber(line, tmp, tmp2);
                            _debug(getFullName() + "Read from Saber "+i+" "+
                                    _outvar[i]);
                        }
                    }
                }
                if (line.startsWith(new String("------------"))){
                    if (!firstsep) firstsep = true;
                    else secondsep = true;
                    continue;
                }
                if (line == null) continue;
                if (line.startsWith(new String("bye"))){
                    break;
                }
            }catch (IOException e){
                //throw new IllegalActionException(this,
                //   "IO error while in reading" + e.getMessage());
                if (_tool == null) {
                    throw new IllegalActionException(this,
                            "External tool terminated.");
                }
                _instream = _tool.getInputStream();
                _errorstream = _tool.getErrorStream();
                _outstream = _tool.getOutputStream();
                _reader = new BufferedReader(
                        new InputStreamReader(_instream));
                if(_reader == null) {
                    throw new IllegalActionException(this,
                            " Can't refresh input buffer." +
                            " IO error while in reading " + e.getMessage());
                }
            }
        }
        // Output tokens
        outports = outputPorts();
        int outi = 0;
        while(outports.hasMoreElements()) {
            TypedIOPort p = (TypedIOPort) outports.nextElement();
            _outtoken[outi] = new DoubleToken(_outvar[outi]);
            p.broadcast( _outtoken[outi] );
            _outpvalue.put(p, _outtoken[outi]);
            _debug(getFullName() + "fires output: " + _outvar[outi]
                    + "as token"+ (_outtoken[outi]).doubleValue());
            outi ++;
        }

    }

    /** Update parameters.
     */
    public void updateParameters() throws IllegalActionException {
        _netlist = ((StringToken)paramNetlist.getToken()).stringValue();
        _innerStep = ((StringToken)paramInnerStep.getToken()).stringValue();
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public variables                       ////

    /** Parameter for the file name of the next list; the type is String;
     *  the default value is the empty string.
     */
    public Parameter paramNetlist;

    /** The parameter for the internal step size of the Saber sub system;
     *  the type is string, and the default value is "10u" for 10 microsecond.
     */
    public Parameter paramInnerStep;

    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////

    /** Override Entity addPort. Create parameters for each port.
     */
    protected void _addPort(Port p)
            throws IllegalActionException, NameDuplicationException {
        super._addPort(p);
        _debug(getFullName() + "Port" + p.getName() + " added");
        String portname = p.getName();
        String paramname = portname + "ToolVar";
        new Parameter(this, paramname,
                new StringToken("saber_variable_here"));
        _debug(getFullName() + " parameter " + paramname + " added.");
    }

    /** Override Entity removePort. Remove the corresponding parameter too.
     */
    protected void _removePort(Port p) {
        String portname = p.getName();
        String paramname = portname + "ToolVar";
        Parameter pp = (Parameter)getAttribute(paramname);
        _removeAttribute(pp);
        super._removePort(p);
        _debug(getFullName() + ": port" + portname + "  removed");
    }

    /** parse input from saber.
     */
    protected double _parseNumber(String text, int begpt, int endpt) {
        int locm = text.indexOf('m', begpt);
        int locn, locu;
        double number;
        if ((locm > begpt) && (locm <= endpt)){
            number = new Double(text.substring(begpt, locm)).doubleValue();
            number *= 0.001;
        } else {
            locu = text.indexOf('u', begpt);
            if ((locu > begpt) && (locu <= endpt)){
                number = new Double(text.substring(begpt, locu)).doubleValue();
                number *= 0.001*0.001;
            } else {
                locn = text.indexOf('n', begpt);
                if ((locn > begpt) && (locn <= endpt)){
                    number = new Double(text.substring(begpt,
                            locn)).doubleValue();
                    number *= 0.001*0.001*0.001;
                } else {
                    locn = text.indexOf('p', begpt);
                    if ((locn > begpt) && (locn <= endpt)){
                        number = new Double(text.substring(begpt,
                                locn)).doubleValue();
                        number *= 0.001*0.001*0.001*0.001;
                    } else {
                        locn = text.indexOf('f', begpt);
                        if ((locn > begpt) && (locn <= endpt)){
                            number = new Double(text.substring(begpt,
                                    locn)).doubleValue();
                            number *= 0.001*0.001*0.001*0.001*0.001;
                        } else {
                            locn = text.indexOf('a', begpt);
                            if ((locn > begpt) && (locn <= endpt)){
                                number = new Double(text.substring(begpt,
                                        locn)).doubleValue();
                                number *= 1e-18;
                            } else {
                                number = new Double(text.substring(begpt,
                                        endpt)).doubleValue();
                            }
                        }
                    }
                }
            }
        }
        return number;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    // IO streams
    protected InputStream _instream = null;
    protected InputStream _errorstream = null;
    protected OutputStream _outstream = null;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Tool process
    private Process _tool;

    // IO streams
    private  BufferedReader _reader;
    private  PrintWriter _ps;

    // start command
    private static final String _startCommand = "saber -c ";
    private String _startpt = new String("endpt");
    private String _endpt = new String("endpt");

    // parameters
    private String _netlist;
    private String _innerStep;

    // potential output
    private int _numout;
    private DoubleToken[] _outtoken;
    private double[] _outvar;
    // hash table for output ports and their tool variables.
    private Hashtable _porttable;
    private Hashtable _outpvalue;
    private boolean _first;
}
