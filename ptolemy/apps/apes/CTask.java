package ptolemy.apps.apes;

import java.util.HashMap;
import java.util.Map;

import ptolemy.actor.IOPort;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.Receiver;
import ptolemy.actor.TimedDirector;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.BreakCausalityInterface;
import ptolemy.actor.util.CausalityInterface;
import ptolemy.actor.util.Time;
import ptolemy.data.ResourceToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

public class CTask extends TypedAtomicActor implements Runnable {

    public CTask() {
        _initialize();
    }

    public CTask(Workspace workspace) {
        super(workspace);
        _initialize();
    }

    public CTask(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _initialize();
    }

    /** outgoing port to resource actors */
    public MulticastIOPort toResourcePort;

    /** incoming port from resource actors */
    public IOPort fromResourcePort;

    public IOPort triggerConnector;

    public Parameter methodName;

    public void accessPointCallback(double extime, double minNextTime,
            String syscall) throws NoRoomException, IllegalActionException {
        System.out.println("Time: " + getDirector().getModelTime()
                + "; callback of task: " + this.getName() + " params: ("
                + extime + "/ " + new Time(getDirector(), extime) + ", "
                + minNextTime + ")");

        if (extime >= 0) {
            _sendResourceToken("CPUScheduler", new Time(getDirector(), extime), false);
        }

        synchronized (this) {
            _minDelay = new Time(getDirector(), minNextTime);
            _inExecution = false;
            this.notifyAll(); // wake up the DEDirector thread
            while (!_inExecution) {
                try {
                    System.out.println(getDirector().getModelTime());
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } 
        
        
    } //return to the C part  
    
    public void fire() throws IllegalActionException {
        System.out.println("Time: " + getDirector().getModelTime().toString()
                + "; Task fired: " + this.getName());

        boolean readInputs = false;
        
        for (int i = 0; i < fromResourcePort.getWidth(); i++) {
            if (fromResourcePort.hasToken(i)) {
                fromResourcePort.get(i);
                readInputs = true;
            }
        }

        if (_waitForMinDelay && !readInputs) {
            synchronized (this) {
                while (_inExecution) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            _waitForMinDelay = false;
        } else {
            synchronized (this) {
               if (_minDelay.getDoubleValue() >= 0) {
                getDirector().fireAt(this, getDirector().getModelTime().add(_minDelay));
                _waitForMinDelay = true;
            }
                _inExecution = true; 
                this.notifyAll();
            }
        }

    }

    /**
     * Break the dependency between input and output ports such that there is no infinite loop.
     */
    public CausalityInterface getCausalityInterface()
            throws IllegalActionException {
        if (_causalityInterface == null) {
            _causalityInterface = new BreakCausalityInterface(this,
                    getDirector().defaultDependency());
        }
        return _causalityInterface;
    }

    /**
     * resolve resourceActors and start the thread.
     */
    public void initialize() throws IllegalActionException {
        super.initialize(); 
        if (!(super.getDirector() instanceof TimedDirector)) {
            throw new IllegalActionException(this,
                    "Enclosing director must be a TimedDirector.");
        }

        // parse resources
        for (int channelId = 0; channelId < toResourcePort.getWidth(); channelId++) {
            Receiver[] receivers = toResourcePort.getRemoteReceivers()[channelId];
            if (receivers.length > 0) {
                Receiver receiver = receivers[0];
                _resources.put(((ResourceActor) receiver.getContainer()
                        .getContainer()), channelId);
            }
        }
        _waitForMinDelay = false;
        _thread = new Thread(this);
        _thread.start();
        synchronized(this) {
            while (_inExecution) {
                try {
                    this.wait();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public void run() {
        while (true) { 
            _callCMethod(); 
        }
    }

    public void wrapup() throws IllegalActionException {
        _thread.interrupt();
        _thread = null;
    }

    protected void _callCMethod() {

    }

    private void _initialize() {
        _resources = new HashMap<ResourceActor, Integer>();
        
        try {
            fromResourcePort = new TypedIOPort(this, "fromResourcePort", true,
                    false);
            fromResourcePort.setMultiport(true);

            toResourcePort = new MulticastIOPort(this, "toResourcePort", false,
                    true);
            
            Parameter destinationActorList= (Parameter) toResourcePort.getAttribute("destinationActors");
            destinationActorList.setExpression("CPUScheduler");
            
            triggerConnector = new TypedIOPort(this, "triggerConnector", false,
                    true);

        } catch (IllegalActionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NameDuplicationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void _sendResourceToken(String resourceName, Object requestedValue,
            boolean isMinValue) throws NoRoomException, IllegalActionException {
        int channelId = -1;
        for (ResourceActor actor : _resources.keySet()) {
            if (actor.getName().equals(resourceName))
                channelId = _resources.get(actor).intValue();
        }
        ResourceToken token = new ResourceToken(this, requestedValue);
        toResourcePort.send(channelId, token); // send the output
    }

    /** The causality interface, if it has been created. */
    private CausalityInterface _causalityInterface;

    private Thread _thread;

    private Time _minDelay;

    private boolean _inExecution = true;

    private Map<ResourceActor, Integer> _resources;

    private boolean _waitForMinDelay;

}
