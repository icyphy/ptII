package doc.books.design.modal.test;

import ptolemy.domains.modal.kernel.FSMActor;
import ptolemy.domains.modal.kernel.State;
import ptolemy.domains.modal.kernel.Transition;
import ptolemy.kernel.util.KernelException;

public class SimpleFSMStructure {

    public SimpleFSMStructure() {
        try {
            FSMActor actor = new FSMActor();
            State state1 = new State(actor, "State1");
            State state2 = new State(actor, "State2");
            Transition relation = new Transition(actor, "relation");
            Transition relation2 = new Transition(actor, "relation2");
            state1.incomingPort.link(relation2);
            state1.outgoingPort.link(relation);
            state2.incomingPort.link(relation);
            state2.outgoingPort.link(relation2);

        } catch (KernelException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
