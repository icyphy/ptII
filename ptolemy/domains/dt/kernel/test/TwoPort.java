package ptolemy.domains.dt.kernel.test;

import ptolemy.actor.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.sdf.kernel.*;
import ptolemy.actor.lib.*;
import ptolemy.math.Complex;


public class TwoPort extends TypedAtomicActor {

    public TwoPort(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {

        super(container, name);

        input1 = (SDFIOPort) newPort("input1");
        input1.setInput(true);
        input1.setTokenConsumptionRate(1);
        input1.setTypeEquals(BaseType.DOUBLE);
        
        input2 = (SDFIOPort) newPort("input2");
        input2.setInput(true);
        input2.setTokenConsumptionRate(1);
        input2.setTypeEquals(BaseType.DOUBLE);

        output1 = (SDFIOPort) newPort("output1");
        output1.setOutput(true);
        output1.setTokenProductionRate(1);
        output1.setTypeEquals(BaseType.DOUBLE);
        
        output2 = (SDFIOPort) newPort("output2");
        output2.setOutput(true);
        output2.setTokenProductionRate(1);
        output2.setTypeEquals(BaseType.DOUBLE);
        
        inrate1= new Parameter(this, "inrate1", new IntToken(1));
        _inrate1 = 1;

        inrate2= new Parameter(this, "inrate2", new IntToken(1));
        _inrate2 = 1;
 
        outrate1 = new Parameter(this, "outrate1", new IntToken(1));
        _outrate1 = 1;

        outrate2 = new Parameter(this, "outrate2", new IntToken(1));
        _outrate2 = 1;

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The input port. This has type IntToken. */
    public SDFIOPort input1;
    public SDFIOPort input2;

    /** The output port. This has type BooleanToken. */
    public SDFIOPort output1;
    public SDFIOPort output2;
    
    public Parameter inrate1;
    public Parameter inrate2;
    public Parameter outrate1;
    public Parameter outrate2;
    

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports and parameters.  The new
     *  actor will have the same parameter values as the old.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        TwoPort newObject = (TwoPort)(super.clone(workspace));
        newObject.input1 = (SDFIOPort)newObject.getPort("input1");
        newObject.input2 = (SDFIOPort)newObject.getPort("input2");
        newObject.output1 = (SDFIOPort)newObject.getPort("output1");
        newObject.output2 = (SDFIOPort)newObject.getPort("output2");
        newObject.inrate1 = (Parameter) newObject.getAttribute("inrate1");
        newObject.inrate2 = (Parameter) newObject.getAttribute("inrate2");
        newObject.outrate1 = (Parameter) newObject.getAttribute("outrate1");
        newObject.outrate2 = (Parameter) newObject.getAttribute("outrate2");

        
        return newObject;
    }
    
    public void attributeChanged(Attribute attribute) throws IllegalActionException {
        Director dir = getDirector();
        
        if (dir != null) {
            _inrate1 = ((IntToken) inrate1.getToken()).intValue();
            _inrate2 = ((IntToken) inrate2.getToken()).intValue();
            _outrate1 = ((IntToken) outrate1.getToken()).intValue();
            _outrate2 = ((IntToken) outrate2.getToken()).intValue();
            input1.setTokenConsumptionRate(_inrate1);
            input2.setTokenConsumptionRate(_inrate2);
            output1.setTokenProductionRate(_outrate1);
            output2.setTokenProductionRate(_outrate2);
            dir.invalidateSchedule();
        }
    }



    /** Consume a single IntToken on the input. Produce 32 consecutive
     *  BooleanTokens on the output port which is the bitwise
     *  representation of the input IntToken.
     *  The most significant bit is the first boolean
     *  token send out. The least significant bit is the last
     *  boolean token send out.
     *
     *  @exception IllegalActionException If there is no director.
     */

    public final void fire() throws IllegalActionException  {
        int i;
        int integer, remainder;
        DoubleToken token;

        if (input1.getWidth() >= 1) {
            for(i=0; i < _inrate1;i++) {
                token = (DoubleToken) (input1.get(0));
            }
        }
        
        if (input2.getWidth() >= 1) {
            for(i=0; i < _inrate2;i++) {
                token = (DoubleToken) (input2.get(0));
            }
        }
                
        
        for(i=0; i < _outrate1;i++) {
                output1.send(0, new DoubleToken(i));
        }
        for(i=0; i < _outrate2;i++) {
                output2.send(0, new DoubleToken(i));
        }
    }
    
    private int _inrate1;
    private int _inrate2;
    private int _outrate1;
    private int _outrate2;
}
