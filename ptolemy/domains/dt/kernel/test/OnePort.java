package ptolemy.domains.dt.kernel.test;

import ptolemy.actor.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.dt.kernel.*;
import ptolemy.domains.sdf.kernel.*;
import ptolemy.actor.lib.*;
import ptolemy.math.Complex;


public class OnePort extends SDFAtomicActor {
    public OnePort(CompositeEntity container, String name) 
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        input = (SDFIOPort) newPort("input");
        input.setInput(true);
        input.setTokenConsumptionRate(1);
        input.setTypeEquals(BaseType.DOUBLE);
        inrate= new Parameter(this, "inrate", new IntToken(1));
        _inrate = 1;

        
        output = (SDFIOPort) newPort("output");
        output.setOutput(true);
        output.setTokenProductionRate(1);
        output.setTypeSameAs(input);
        outrate = new Parameter(this, "outrate", new IntToken(1));
        _outrate = 1;
        
        
        
        initialOutputs = new Parameter(this, "initialOutputs",
                             new IntMatrixToken(defaultValues));
                             
                             
        //Parameter tokenInitProduction = new Parameter(output,"tokenInitProduction",
        //                     new IntMatrixToken(defaultValues));
        
    }
    
    public SDFIOPort input;
    public SDFIOPort output;
    
    public Parameter inrate;
    public Parameter outrate;

    public Parameter initialOutputs;
    
    public Parameter value;
    public Parameter step;
    //public Parameter tokenInitProduction;
    
    public void attributeChanged(Attribute attribute) throws IllegalActionException {
        Director dir = getDirector();
        
        if (dir != null) {
            _inrate = ((IntToken) inrate.getToken()).intValue();
            _outrate = ((IntToken) outrate.getToken()).intValue();
            input.setTokenConsumptionRate(_inrate);
            output.setTokenProductionRate(_outrate);
            dir.invalidateSchedule();
        }
    }

    
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        OnePort newobj = (OnePort)(super.clone(ws));
        newobj.input =  (SDFIOPort)newobj.getPort("input");
        newobj.output = (SDFIOPort)newobj.getPort("output");
        newobj.inrate = (Parameter) newobj.getAttribute("inrate");
        newobj.outrate = (Parameter) newobj.getAttribute("outrate");
        return newobj;
    }

     
    public final void fire() throws IllegalActionException  {
        int i;
        int integer, remainder;
        DoubleToken token = new DoubleToken(0.0);;
        _buffer = new Token[_inrate];
        
        _buffer[0] = token;

        //DTDebug debug = new DTDebug(true);
        //debug.prompt(""+input.getWidth());
        if (input.getWidth() >= 1) {
            for(i=0;i<_inrate;i++) {
            // FIXME: should consider port widths
                //if (input.hasToken(0)) {
                    //token = (DoubleToken) (input.get(0));
                    _buffer[i] = input.get(0);
                //} else {
                //    throw new IllegalActionException(
                //              "no Tokens available for OnePort during firing");
                //}
            }
        }
   
        for(i=0;i<_outrate;i++) {
            //output.send(0, new DoubleToken(0.0));
            output.send(0, _buffer[i%_inrate]);
        }
    }
    
    private int _inrate;
    private int _outrate;
    private int defaultValues[][] = {{0,0}};
    private Token[] _buffer;
}
