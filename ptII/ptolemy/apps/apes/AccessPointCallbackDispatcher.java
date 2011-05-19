package ptolemy.apps.apes;

import java.util.HashMap;
import java.util.Map;

import ptolemy.actor.Actor;
import ptolemy.actor.NoRoomException;
import ptolemy.kernel.util.IllegalActionException;

public class AccessPointCallbackDispatcher {  
        
        public native void InitializeC(); 
        
        public AccessPointCallbackDispatcher() { 
        }
        
        /** Map of taskNames and tasks. */
        private Map<String, Actor> _taskNames = new HashMap();
        
        
        
        public void accessPointCallback(double extime, double minNextTime) { 
            CTask task = (CTask) _taskNames.get(Thread.currentThread().getName());
            try {
                task.accessPointCallback(extime, minNextTime);
            } catch (NoRoomException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalActionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        public void accessPointCallback(double extime, double minNextTime, String varName) {
           
            CTask task = (CTask) _taskNames.get(Thread.currentThread().getName());
           try {  
                
                task.accessPointCallback(extime, minNextTime);
                //task.setGlobalVariable(varName);
            } catch (NoRoomException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalActionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            int i = 0;
        }
        
        
        public void accessPointCallback(double extime, double minNextTime, String varName, double value) {
            CTask task = (CTask) _taskNames.get(Thread.currentThread().getName());
            try { 
                
                task.accessPointCallback(extime, minNextTime);
                task.setOutputValue(varName, value);
            } catch (NoRoomException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalActionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        public void addTask(CTask task) { 
            _taskNames.put(task.getName(), task);
        }

    
}
