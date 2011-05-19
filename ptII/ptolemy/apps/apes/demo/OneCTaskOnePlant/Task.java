package ptolemy.apps.apes.demo.OneCTaskOnePlant;

import java.util.List;

import ptolemy.actor.IOPort;
import ptolemy.actor.NoRoomException;
import ptolemy.apps.apes.CTask;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

public class Task extends CTask {

    public Task() throws IllegalActionException, NameDuplicationException {  
        super();
    }

    public Task(Workspace workspace) throws IllegalActionException, NameDuplicationException {
        super(workspace);  
    }

    public Task(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);  
    }
    
    public native void setLower(double lower);
    public native void setUpper(double upper);
    
    @Override
    public boolean prefire() throws IllegalActionException { 
        for (IOPort port : (List<IOPort>)inputPortList()) {
            if (port.getName().equals("lower")) {
                for (int i = 0; i < port.getWidth(); i++) {
                    while (port.hasToken(i)) {
                        IntToken t = (IntToken) port.get(0); 
                        setLower(t.doubleValue());
                    }
                }
            } else if (port.getName().equals("upper")) {
                for (int i = 0; i < port.getWidth(); i++) {
                    while (port.hasToken(i)) {
                        DoubleToken t = (DoubleToken) port.get(0); 
                        setUpper(t.doubleValue());
                    }
                }
            }
        }
        return super.prefire();
    }
    
    
    private native void CMethod(); 
    
    protected void _callCMethod() {  
            
        System.out.println(this.getName() + "._callCMethod()");
        CMethod();   
    }

    public void accessPointCallback(double extime, double minNextTime) throws NoRoomException,
    IllegalActionException {
        // TODO Auto-generated method stub
        super.accessPointCallback(extime, minNextTime);
    }
    public int activateTask(int taskId) throws NoRoomException, IllegalActionException {
        return cpuScheduler.activateTask(taskId);
    }
    
    public void terminateTask() throws NoRoomException, IllegalActionException {
        cpuScheduler.terminateTask();
    }
    
    public void setOutputPort(String varName, double value) throws NoRoomException, IllegalActionException {
        ((IOPort)outputPortList().get(1)).send(0, new DoubleToken(value));
    } 

}
