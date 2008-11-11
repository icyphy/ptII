package ptolemy.actor.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.Receiver;
import ptolemy.actor.TimedDirector;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.BooleanDependency;
import ptolemy.actor.util.BreakCausalityInterface;
import ptolemy.actor.util.CausalityInterface;
import ptolemy.actor.util.DefaultCausalityInterface;
import ptolemy.actor.util.Dependency;
import ptolemy.actor.util.RealDependency;
import ptolemy.actor.util.Time;
import ptolemy.data.ResourceToken;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

public class Task extends TypedAtomicActor implements Runnable {

    public Task() {
        _initialize();
    }
    
    public Task(Workspace workspace) {
        super(workspace);
        _initialize();
    }

    public Task(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _initialize();
    }
    
    
    /** outgoing port to resource actors */
    public IOPort resourceOutPort;
    
    /** incoming port from resource actors */
    public IOPort resourceInPort;
    
    /** used to connect trigger actors - no events are sent on this port */
    public IOPort trigger;
    
    public void fire() throws IllegalActionException { 
        if (_thread != null && _thread.isAlive()) { // resume 
            synchronized(this) 
            {                
                _threadisactive = true; 
                this.notifyAll(); 
            } 
            try { 
                while (_threadisactive) 
                    workspace().wait(this, 1);   
            } catch (Exception ex) { 
                ex.printStackTrace(); 
            } 
        } 
    }
    

    public void initialize() throws IllegalActionException { 
        super.initialize();
        if (!(super.getDirector() instanceof TimedDirector)) {
            throw new IllegalActionException(this, "Enclosing director must be a TimedDirector.");
        }
        
        // parse resources
        for (int channelId = 0; channelId < resourceInPort.getWidth(); channelId++) {
            Receiver[] receivers = resourceInPort.getRemoteReceivers()[channelId];
            if (receivers.length > 0) {
                Receiver receiver = receivers[0];
                
                ArrayList channels = new ArrayList();
                channels.add(channelId);
                
                _resources.put(((ResourceActor)receiver.getContainer().getContainer()), channels);
            }
        }
        for (int channelId = 0; channelId < resourceOutPort.getWidth(); channelId++) {
            Receiver[] receivers = resourceOutPort.getRemoteReceivers()[channelId];
            if (receivers.length > 0) {
                Receiver receiver = receivers[0];
                
                ArrayList channels = (ArrayList) _resources.get(((ResourceActor)receiver.getContainer().getContainer()));
                channels.add(channelId);
                
                _resources.put(((ResourceActor)receiver.getContainer().getContainer()), channels);
            }
        }
    }

    public void run() { 
        while (true) { 
            try {  
                for (int channelId = 0; channelId < resourceInPort.getWidth(); channelId++) {
                    if (resourceInPort.hasToken(channelId)) { 
                        
                        //callMethod();
                    }
                }   
                _threadisactive = false; 
                workspace().wait(this);  
            } catch (Exception ex) { 
                ex.printStackTrace(); 
                System.exit(0); 
            } 
        }
    }
    
    public CausalityInterface getCausalityInterface() throws IllegalActionException { 
        if (_causalityInterface == null) {  
            _causalityInterface = new BreakCausalityInterface(
                  this, getDirector().defaultDependency());
        }
        return _causalityInterface;
    }
    
    //public abstract void callCmethod();
    
    public synchronized void callbackFromC() throws NoRoomException, IllegalActionException {
        // type of access point: requested resource + value for resource
        
        ResourceActor actor = _resources.keySet().iterator().next();
        int requestedResourceValue = 5;
        ResourceToken token = new ResourceToken(this, requestedResourceValue);
        resourceOutPort.send(_resources.get(actor).get(1), token);
        
    }
    
    public void wrapup() throws IllegalActionException { 
        _thread = null; 
    }
    
//    static { 
//        System.loadLibrary("simulatordemo/ccode"); 
//    }
    

    private void _initialize() {
        _resources = new HashMap();
        _thread = new Thread(this);
        _threadisactive = true;
        _thread.start();
        try {
            resourceInPort = new TypedIOPort(this, "resourceTrigger", false, true);
            resourceInPort.setMultiport(true);
    
            resourceOutPort = new TypedIOPort(this, "triggerResource", true, false);
            resourceOutPort.setMultiport(true);
            
            trigger = new TypedIOPort(this, "connectTrigger", true, false);
        } catch (IllegalActionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NameDuplicationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
   
    /** The causality interface, if it has been created. */
    private CausalityInterface _causalityInterface;
    
    private Thread _thread;
    
    private boolean _threadisactive;
    
    private Map<ResourceActor, List<Integer>> _resources;
    
    
}
