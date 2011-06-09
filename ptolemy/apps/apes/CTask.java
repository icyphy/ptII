package ptolemy.apps.apes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.TimedDirector;
import ptolemy.actor.util.BreakCausalityInterface;
import ptolemy.actor.util.CausalityInterface;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

/**
 * The CTask executes legacy C-Code in a separate thread. The thread is started 
 * in the initialize method. The C-Code run in the thread executes and calls back 
 * to the JAVA code via callbacks. These callbacks are either system calls or access
 * point callbacks (@see: AccessPointCallbackDispatcher). At some point, the 
 * accessPointCallback method of this class is called and the thread is stalled in 
 * there. In the fire, the thread is resumed. 
 * In order to maximize concurrency, the thread executing the fire of this actor
 * and the thread created in this actor can run in parallel for a minimum delay time provided
 * in the accesspointCallback method. After this minimum delay, the thread executing the
 * executive director and thus the fire of this actor is blocked until the thread created
 * in this actor returns to the accessPointCallbackMethod. This is done by scheduling
 * a refiring of this actor after the minimum delay after resuming the thread.
 * @author Patricia Derler and Stefan Resmerita
 */
public class CTask extends ApeActor implements Runnable {
    
    public enum Type {
        BASIC_TASK, EXTENDED_TASK, IRS_1, IRS_2
    }

    public CTask() throws IllegalActionException, NameDuplicationException {
        super();
        _initialize();
    }

    public CTask(Workspace workspace) throws IllegalActionException, NameDuplicationException {
        super(workspace);
        _initialize();
    }

    public CTask(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _initialize();
    }
    
    public CPUScheduler cpuScheduler;
    public EventManager eventManager;
    public static AccessPointCallbackDispatcher dispatcher; 
    public Parameter methodName;
    
    public void accessPointCallback(double extime, double minNextTime) throws NoRoomException, IllegalActionException {
        if (!_actorStopped){
            if (extime >= 0) {
                ResourceToken token = new ResourceToken(this, new Time(getDirector(), extime), null);
                _buffer = token;
            }

            synchronized (this) {
                _minDelay = new Time(getDirector(), minNextTime);
                _inExecution = false;
                this.notifyAll(); // wake up the DEDirector thread
                while (!_inExecution) {
                    try {
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
     * Returns the priority of the actor. The priority is an int value. The
     * default return value is 0.
     * 
     * @param actor
     *            Given actor.
     * @return Priority of the given actor.
     */
    public static int getPriority(Actor actor) {
        try {
            Parameter parameter = (Parameter) ((NamedObj) actor)
                    .getAttribute("priority");
    
            if (parameter != null) {
                IntToken token = (IntToken) parameter.getToken();
    
                return token.intValue();
            } else {
                return 0;
            }
        } catch (ClassCastException ex) {
            return 0;
        } catch (IllegalActionException ex) {
            return 0;
        }
    }
    
    private ArrayList<Object[]> _bufferOutputValue = new ArrayList();
    
    public void setOutputValue(String varName, double value) throws NoRoomException, IllegalActionException {
        for (IOPort port : (List<IOPort>)outputPortList()) {
            if (port != output) {
                if (port.getName().equals(varName)) {
                    _bufferOutputValue.add(new Object[]{port, new DoubleToken(value)});
                }
            }
        } 
    }
    
    private native void setGlobalVariable(String name, double value);
    
    public void setGlobalVariable(String varName) {
        setGlobalVariable(varName, _inputPortValues.get(varName));
    }
    
    public boolean prefire() throws IllegalActionException { 
        for (IOPort port : (List<IOPort>)inputPortList()) {
            if (port != input) {
                for (int i = 0; i < port.getWidth(); i++) {
                    while (port.hasToken(i)) {
                        Token token = port.get(i);
                        double value = 0.0;
                        if (token instanceof DoubleToken) {
                            value = ((DoubleToken)token).doubleValue();
                        } else if (token instanceof IntToken) {
                            value = ((IntToken)token).doubleValue();
                        } // TODO add other token types
                        //_inputPortValues.put(port.getName(), value);
                        setGlobalVariable(port.getName(), value);
                    }
                }
            } 
        }
        return super.prefire();
    }
    
    private HashMap<String, Double> _inputPortValues;

    public void fire() throws IllegalActionException {
        boolean readInputs = false;
        
        while (input.hasToken(0)) {
            input.get(0);
            readInputs = true; 
        } 
        
        // consume all values on ports - if port values should be read and used, 
        // this has to be done in the prefire or in the overriden fire before 
        // calling this fire method
        for (IOPort port : (List<IOPort>)inputPortList()) {
            if (port != input) {
                for (int i = 0; i < port.getWidth(); i++) {
                    while (port.hasToken(i)) {
                        port.get(0);
                    }
                }
            }
        }

        boolean continueExecution = true;
        while (continueExecution) {
            continueExecution = false;
            // fired as a result of the fireAt
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
                if (_buffer != null) {
                    output.send("CPUScheduler", _buffer);
                    _buffer = null;
                }
                for (Object[] entry : bufferedTokens) {
                    CTask task = (CTask) entry[0];
                    Token token = (Token) entry[1];
                    output.send(task, token);
                }
                bufferedTokens.clear();
                for (Object[] entry : _bufferOutputValue) {
                    IOPort port = (IOPort) entry[0]; 
                    Token token = (Token) entry[1];
                    for (int i = 0; i < port.getWidth(); i++) {
                        port.send(i, token);
                    }
                }
                _bufferOutputValue.clear();
                for (ResourceToken token : bufferedResourceTokens) {
                    output.send(token);
                }
                bufferedResourceTokens.clear();
                
                _waitForMinDelay = false;
            } else if (readInputs) { // fired by the CPUScheduler
                synchronized (this) {
                    if (_minDelay.getDoubleValue() > 0) { 
                        _waitForMinDelay = true;
                        getDirector().fireAt(this, getDirector().getModelTime().add(_minDelay));
                    } else if (_minDelay.getDoubleValue() == 0) {
                        _waitForMinDelay = true;
                        continueExecution = true;
                    }
                    _inExecution = true; 
                    this.notifyAll();
                }
                readInputs = false;
            }
        }
    }
    
    
    

    /**
     * resolve resourceActors and start the thread.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize(); 
        if (!(super.getDirector() instanceof TimedDirector)) {
            throw new IllegalActionException(this,
                    "Enclosing director must be a TimedDirector.");
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
                } else if (actor instanceof EventManager) {
                    eventManager = (EventManager) actor;
                }
            }
        }
        if (dispatcher == null) {
            dispatcher = new AccessPointCallbackDispatcher(); 
            try {
                String libName = ((StringToken)((StringParameter)((NamedObj)getContainer()).getAttribute("CCodeLibrary")).getToken()).stringValue();
                System.loadLibrary(libName);  
                dispatcher.InitializeC();
//                cpuScheduler.InitializeC();
//                eventManager.InitializeC();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        dispatcher.addTask(this);
        
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
     
    public void initialize() throws IllegalActionException { 
        super.initialize();
        // TODO call startup function
    }
    
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
        CMethod(this.getName()); 
    }

    private native void CMethod(String taskName); 

    private void _initialize() throws IllegalActionException, NameDuplicationException {
        
        _inputPortValues = new HashMap();
        
        Parameter sourceActorList= (Parameter) input.getAttribute("sourceActors");
        sourceActorList.setExpression("*");

        Parameter destinationActorList= (Parameter) output.getAttribute("destinationActors");
        destinationActorList.setExpression("CPUScheduler");
        
//        priority = new Parameter(this, "priority");
//        priority.setExpression("0");
//        priority.setTypeEquals(BaseType.INT);
//        
//        ID = new Parameter(this, "ID");
//        ID.setExpression("0");
//        ID.setTypeEquals(BaseType.INT);
    }
    
//    public Parameter priority;
//    public Parameter ID;

    /**
     * Buffers the token produced in the accessPointCallback if the DE Director thread running to  avoid concurrent modification of the eventQueue in the DE Director.
     */
    private ResourceToken _buffer;

    /** The causality interface, if it has been created. */
    private CausalityInterface _causalityInterface;

    private Thread _thread;

    private Time _minDelay;

    private boolean _inExecution = true;

    private boolean _waitForMinDelay;
    
    private boolean _actorStopped = false;
    
    private ArrayList<Object[]> bufferedTokens = new ArrayList();
    private ArrayList<ResourceToken> bufferedResourceTokens = new ArrayList();

    public void bufferOutput(Actor task, BooleanToken token) { 
        bufferedTokens.add(new Object[]{task, token});
    }

    public void bufferOutput(ResourceToken resourceToken) { 
        bufferedResourceTokens.add(resourceToken);
    }

    
        
 
    
}
