package ptolemy.domains.wireless.demo.SmartParking;

import java.util.*;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.RecordType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public class DataCollector extends TypedAtomicActor{

    public DataCollector(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        update = new TypedIOPort(this, "update", true, false);
        String[] labels = {"lot", "state"};
        Type[] types = {BaseType.STRING, BaseType.INT};
        RecordType recordType = new RecordType(labels, types);
        update.setTypeEquals(recordType);
        
        debug = new TypedIOPort(this, "debug", false, true);
        debug.setTypeEquals(recordType);
        
        isFull = new TypedIOPort(this, "isFull", false, true);
        isFull.setTypeEquals(BaseType.BOOLEAN);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Port that receives parking lot updates.
     */
    public TypedIOPort update;
    
    /** Output port for sending out the received message just for 
     *  debugging. 
     */
    public TypedIOPort debug;
    
    /** Output port for seting a signal light to tell people 
     *  whehter the parking lot is full.
     */
    public TypedIOPort isFull;
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** When it receives token from the signal port, which is
     *  used to receive signal from the pursuer or the evader.
     *  it tells what the signal is from by checking the signal header.
     *  If it is from the evader, it set itself to be the root node
     *  and broadcast a message for updating the tree. Otherwise, it
     *  output a message to the pursuer to tell it the location of
     *  its parent node, and the pursuer will move closer to the evader
     *  using this information.
     *  When it receives token from the input port, which is used to
     *  receive message from other sensors, it check whether the rootnode
     *  has been changed or whether there is a shorter path. If so, it
     *  performs update and broadcast a message. Otherwise, simply
     *  consumes the messge.
     */
    public void fire() throws IllegalActionException {

        super.fire();
        if(update.getWidth() >0) {
            if(update.hasToken(0)) {
                RecordToken updateMsg = (RecordToken)update.get(0);
                _parkingManager.update(updateMsg);
                debug.send(0, updateMsg);
                if (_parkingManager.getAvailable().size() == 0 && !_isFull) {
                    _isFull = true;
                    isFull.send(0, new BooleanToken("true"));
                }
                if (_parkingManager.getAvailable().size() > 0 && _isFull) {
                    _isFull = false;
                    isFull.send(0, new BooleanToken("false"));
                }
            }
        }
    }    
    
    /** Initialize the private varialbles.
     *  @exception IllegalActionException If thrown by the base class.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _parkingManager = new ParkingManager();
        _isFull = false;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                       private variables                   ////

    private ParkingManager _parkingManager;
    
    private boolean _isFull = false;
    
}