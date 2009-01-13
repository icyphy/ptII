package ptolemy.apps.apes;

import java.util.Iterator;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.TimedDirector;
import ptolemy.actor.util.BreakCausalityInterface;
import ptolemy.actor.util.CausalityInterface;
import ptolemy.actor.util.Time;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

public class CTask extends ApeActor implements Runnable {
    
    public enum Type {
        BASIC_TASK, EXTENDED_TASK, IRS_1, IRS_2
    }

    public CTask() {
        super();
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

    public Parameter methodName;

    public void accessPointCallback(double extime, double minNextTime) throws NoRoomException, IllegalActionException {

        if (!_actorStopped){
            System.out.println(this.getName() + ".accessPointCallback() - Time: " + getDirector().getModelTime()
                    + "(" + extime + ", " + minNextTime + ")");

            
            if (extime >= 0) {
                _sendResourceToken("CPUScheduler", new Time(getDirector(), extime), minNextTime < 0.0);
            }

            synchronized (this) {
                _minDelay = new Time(getDirector(), minNextTime);
                _inExecution = false;
                this.notifyAll(); // wake up the DEDirector thread
                while (!_inExecution) {
                    try {
                        System.out.println(this.getName() + ".wait() at " +  getDirector().getModelTime());
                        this.wait();
                    } catch (InterruptedException e) {
                        if (!_actorStopped){
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    } //return to the C part  
    
    public void fire() throws IllegalActionException {
        System.out.println(this.getName() + ".fire() - Time: " + getDirector().getModelTime().toString());

        boolean readInputs = false;
        
        while (input.hasToken(0)){
            input.get(0);
            readInputs = true;
        }

        if (_waitForMinDelay && !readInputs) {
            synchronized (this) {
                while (_inExecution) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        if (_stopRequested){
                            break;
                        }
                        else{
                            e.printStackTrace();
                        }
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
//        for (int channelId = 0; channelId < toResourcePort.getWidth(); channelId++) {
//            Receiver[] receivers = toResourcePort.getRemoteReceivers()[channelId];
//            if (receivers.length > 0) {
//                Receiver receiver = receivers[0];
//                _resources.put(((ApeActor) receiver.getContainer()
//                        .getContainer()), channelId);
//            }
//        }
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
        
        // just for testing purposes to do system calls
        CompositeActor compositeActor = (CompositeActor) getContainer();
        List entities = compositeActor.entityList();
        for (Iterator it = entities.iterator(); it.hasNext();) {
            Object entity = it.next();
            if (entity instanceof Actor) {
                Actor actor = (Actor) entity;
                if (actor instanceof CPUScheduler) {
                    cpuScheduler = (CPUScheduler) actor;
                    return;
                }
            }
        }
    }
    
    // just for testing purposes to do system calls
    public CPUScheduler cpuScheduler;

    public void run() {
        Thread.currentThread().setName(this.getName());
        while (!_actorStopped) { 
            _callCMethod(); 
        }
    }

    public void wrapup() throws IllegalActionException {
        _actorStopped = true;
        _thread.interrupt();
        _thread = null;
    }

    protected void _callCMethod() {

    }

    private void _initialize() {

        Parameter sourceActorList= (Parameter) input.getAttribute("sourceActors");
        sourceActorList.setExpression("*");

        Parameter destinationActorList= (Parameter) output.getAttribute("destinationActors");
        destinationActorList.setExpression("CPUScheduler");
    }

    private void _sendResourceToken(String resourceName, Object requestedValue,
            boolean isFinished) throws NoRoomException, IllegalActionException {
        CPUScheduler.TaskState state;
        if (isFinished)
            state = CPUScheduler.TaskState.suspended;
        else
            state = CPUScheduler.TaskState.ready_running;
        ResourceToken token = new ResourceToken(this, requestedValue, state);
        output.send(resourceName, token); // send the output
    }

    /** The causality interface, if it has been created. */
    private CausalityInterface _causalityInterface;

    private Thread _thread;

    private Time _minDelay;

    private boolean _inExecution = true;

    private boolean _waitForMinDelay;
    
    private boolean _actorStopped = false;

}
