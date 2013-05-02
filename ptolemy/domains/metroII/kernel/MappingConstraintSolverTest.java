package ptolemy.domains.metroII.kernel;

// FIXME: put this in a test/junit and add makefiles
//import static org.junit.Assert.*;

import java.util.ArrayList;

//import org.junit.Test;

import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event.Builder;

public class MappingConstraintSolverTest {

    //@Test
    public void test() {
        MappingConstraintSolver solver = new MappingConstraintSolver(); 
        solver.addMapping("A", "B"); 
        solver.addMapping("C", "D"); 
        solver.addMapping("E", "D"); 
        solver.addMapping("D", "F"); 
        solver.addMapping("A", "C"); 
        // duplicate constraints
        solver.addMapping("C", "D"); 
        solver.addMapping("D", "E"); 
        solver.addMapping("D", "C"); 
        
        ArrayList<Event.Builder> eventList = new ArrayList<Event.Builder>(); 
        
        Event.Builder eventA = _createMetroIIEvent("A"); 
        Event.Builder eventB = _createMetroIIEvent("B"); 
        Event.Builder eventC = _createMetroIIEvent("C"); 
        Event.Builder eventD = _createMetroIIEvent("D"); 
        Event.Builder eventE = _createMetroIIEvent("E"); 
        Event.Builder eventF = _createMetroIIEvent("F"); 
        Event.Builder eventG = _createMetroIIEvent("G"); 
        
        eventList.add(eventA);
        eventList.add(eventB);
        eventList.add(eventF);
        eventList.add(eventE);
        eventList.add(eventD);
        eventList.add(eventG); 
        
        solver.resolve(eventList); 
        
//         assertEquals(eventA.getStatus(), Event.Status.NOTIFIED); 
//         assertEquals(eventB.getStatus(), Event.Status.NOTIFIED); 
//         assertEquals(eventC.getStatus(), Event.Status.PROPOSED); 
//         assertEquals(eventD.getStatus(), Event.Status.NOTIFIED); 
//         assertEquals(eventE.getStatus(), Event.Status.NOTIFIED); 
//         assertEquals(eventF.getStatus(), Event.Status.WAITING); 
//         assertEquals(eventG.getStatus(), Event.Status.NOTIFIED); 
        
//         assertEquals(solver.numConstraints(), 5); 

    }
    
    private Builder _createMetroIIEvent(String name) {
        Event.Builder builder = Event.newBuilder();
        builder.setName(name);
        builder.setStatus(Event.Status.PROPOSED);
        builder.setType(Event.Type.DEFAULT_NOTIFIED);
        return builder;
    }
    

}
