package ptolemy.domains.wireless.demo.SmartParking;

import java.util.*;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public class ParkingClient extends TypedAtomicActor{

    public ParkingClient(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        carArrival = new TypedIOPort(this, "carArrival", true, false);
        parkingTo = new TypedIOPort(this, "parkingTo", false, true);
        parkingTo.setTypeEquals(BaseType.STRING);
        leave = new TypedIOPort(this, "leave", false, true);
        leave.setTypeEquals(BaseType.INT);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Port that receives a car arrival event.
     */
    public TypedIOPort carArrival;

    /** Port for which lot to park.
     */
    public TypedIOPort parkingTo;
    
    /** Port for leave due to no parking lot available.
     */
    public TypedIOPort leave;

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
        if(carArrival.getWidth() >0) {
            if(carArrival.hasToken(0)) {
                carArrival.get(0);
                HashSet lots =_parkingManager.getAvailable(); 
                if(lots.size()>0) {
                    Object[] lotsArray = lots.toArray();
                    int index = _getRandom(lots.size());
                    parkingTo.send(0, new StringToken((String)lotsArray[index]));
                } else {
                    leave.send(0, new IntToken(_LEAVE));
                }
            }
        }
    }    
    
    /** Initialize the private varialbles.
     *  @exception IllegalActionException If thrown by the base class.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _random = new Random();
        _parkingManager = new ParkingManager();
    }
    
    private int _getRandom(int size) {
        // Generate a double between 0 and 1, uniformly distributed.
        double randomValue = _random.nextDouble();
        double cdf = 0.0;
        int value = 0;
        for (int i = 0; i < size; i++) {
            cdf += 1.0/size;
            if (randomValue <= cdf) {
                value = i;
                break;
            }
        }
        return value;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                       private variables                   ////

    private ParkingManager _parkingManager;
    
    private Random _random;
    
    private static int _LEAVE = 1;
}