package ptolemy.domains.csp.kernel.test;

import ptolemy.domains.csp.kernel.CSPActor;
import ptolemy.domains.csp.kernel.ConditionalSend;
import ptolemy.actor.TypedIOPort;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.CompositeEntity;
import ptolemy.data.IntToken;
import ptolemy.data.Token;

/**
 * @author Christopher Chang <cbc@eecs.berkeley.edu>
 */
public class CSPOut extends CSPActor {
    public CSPOut(CompositeEntity cont, String name)
            throws IllegalActionException, NameDuplicationException {
        super(cont, name);
        out1 = new TypedIOPort(this, "out1", false, true);
        out1.setTypeEquals(ptolemy.data.type.BaseType.INT);
        out2 = new TypedIOPort(this, "out2", false, true);
        out2.setTypeEquals(ptolemy.data.type.BaseType.INT);
        _ports = new TypedIOPort[2];
        _ports[0] = out1;
        _ports[1] = out2;
        _count = 0;
    }

    public void fire() throws IllegalActionException {
        boolean [] hasSent = new boolean[2];
        for (int i = 0; i < hasSent.length; i++) {
            hasSent[i] = false;
        }
        Token token = new IntToken(_count);
        while(true) {
            ConditionalSend [] branches = new ConditionalSend[2];
            for (int i = 0; i < branches.length; i++) {
                branches[i] = new ConditionalSend(!hasSent[i], _ports[i], 0, i, token, this.getConditionalBranchController());
            }
            int successfulBranch = chooseBranch(branches);
            System.out.println("CSPOut chooseBranch returns:" + successfulBranch);
            if (successfulBranch != -1) {
                hasSent[successfulBranch] = true;
            } else {
                return;
            }
        }
    }

    public boolean postfire() {
        _count++;
        return true;
    }

    public TypedIOPort out1, out2;

    private TypedIOPort [] _ports;
    private int _count;
}
