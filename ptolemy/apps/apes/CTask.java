
package ptolemy.apps.apes;

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
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
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
   public IOPort toResourcePort;

   /** incoming port from resource actors */
   public IOPort fromResourcePort;
   
   public IOPort triggerConnector;

   public Parameter methodName;
   
   public void fire() throws IllegalActionException {

       System.out.println("Time: " + getDirector().getModelTime().toString() + "; Task fired: " + this.getName());

       if (_thread != null && _thread.isAlive()) { // resume
           for( int i=0; i < fromResourcePort.getWidth(); i++) {
               if ( fromResourcePort.hasToken(i) ) {
                   fromResourcePort.get(i);
               }
           }
           if(_in_execution){
               while (_in_execution){
                   try {
                       synchronized(_monitor) { 
                           _monitor.wait();
                       }
                   } catch (InterruptedException e) {
                       // TODO Auto-generated catch block
                       e.printStackTrace();
                   }     
               }
           }
           else {
               _in_execution = true;
               synchronized(_monitor) { 
                   _monitor.notifyAll(); //resume execution of the callback and hence of the C task
               }
           }
       }
   }

   public boolean postfire() throws IllegalActionException {
       if (_in_execution) {
           System.out.println("Time: " + getDirector().getModelTime().toString() + "; " + this.getName() + " requests refiring at " + getDirector().getModelTime().add(_min_delay).toString());
           getDirector().fireAt(this, getDirector().getModelTime().add(_min_delay));
       }
       return true;
   }

   
   
   public void initialize() throws IllegalActionException {
       super.initialize();
       _min_delay = new Time(getDirector(), 0.0); // _min_delay will be set by the callback
       _in_execution = false;
       
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
   }

   public CausalityInterface getCausalityInterface()
           throws IllegalActionException {
       if (_causalityInterface == null) {
           _causalityInterface = new BreakCausalityInterface(this,
                   getDirector().defaultDependency());
       }
       return _causalityInterface;
   }

   // public abstract void callCmethod();

   @SuppressWarnings("deprecation")
public void accessPointCallback(double extime, String syscall) throws NoRoomException,
           IllegalActionException {
       // TODO type of access point: requested resource + value for resource
       System.out.println("Time: " + getDirector().getModelTime().toString() + "; callback of task: " + this.getName());
       ResourceActor actor = _resources.keySet().iterator().next();
       Time requestedResourceValue = new Time(getDirector(), extime);
       
       ResourceToken token = new ResourceToken(this, requestedResourceValue);
       
       toResourcePort.send(_resources.get(actor).intValue(), token); // send the output
 
       _in_execution = false;
 
       synchronized(_monitor){  // wake up the DEDirector thread
           _monitor.notifyAll();
       }
       
       while (!_in_execution){
           synchronized(_monitor){
               try {
               _monitor.wait(); // _in_execution must be set to true in fire, before the awakening; 
               } catch (InterruptedException e) {
                   // TODO Auto-generated catch block
                   e.printStackTrace();
                   _thread.stop();
               }
           }
       }
       
   } //return to the C part
   
//   static { 
//       System.loadLibrary("ccode"); 
//   }
//   
   
   @Override
    public boolean prefire() throws IllegalActionException {
        return true;
    }

   public void run() {
       while (true) {
           while (!_in_execution){
               synchronized(_monitor){
                   try {
                       _monitor.wait();
                   } catch (Exception ex) {
                       ex.printStackTrace();
                       break;
                   }
               }
           }
           _callCMethod();
           _in_execution = false;
           System.out.println(this.getName() + ": Execution finished!");
       }
   }

   protected void _callCMethod() {
       
   }

  
   public void wrapup() throws IllegalActionException {
       _thread.interrupt();
       _thread = null;
   }

   private void _initialize() {
       _resources = new HashMap<ResourceActor, Integer>();
       _thread = new Thread(this);
       _thread.start();
       try {
           fromResourcePort = new TypedIOPort(this, "fromResourcePort", true,
                   false);
           fromResourcePort.setMultiport(true);

           toResourcePort = new TypedIOPort(this, "toResourcePort", false,
                   true);
           toResourcePort.setMultiport(true);
           triggerConnector = new TypedIOPort(this, "triggerConnector", false, true);

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
   
   private static Object _monitor = new Object();

   private Time _min_delay; // minimum time delay
   
   private boolean _in_execution;
   
   private Map<ResourceActor, Integer> _resources;

}