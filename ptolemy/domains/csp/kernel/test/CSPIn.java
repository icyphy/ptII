package ptolemy.domains.csp.kernel.test;

import ptolemy.domains.csp.kernel.CSPActor;
import ptolemy.domains.csp.kernel.ConditionalReceive;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.Token;

/**
 * An actor that performs a concurrent read on its two input ports, trying to read exactly one
 * token from each port. It then adds the values and sends the result to the output port.
 *
 * @author Christopher Chang <cbc@eecs.berkeley.edu>
 */
public class CSPIn extends CSPActor {
    public CSPIn(CompositeEntity cont, String name)
            throws IllegalActionException, NameDuplicationException {
        super(cont, name);
        in1 = new TypedIOPort(this, "in1", true, false);
        in1.setTypeEquals(ptolemy.data.type.BaseType.INT);
        in2 = new TypedIOPort(this, "in2", true, false);
        in2.setTypeEquals(ptolemy.data.type.BaseType.INT);
        _ports = new TypedIOPort[2];
        _ports[0] = in1;
        _ports[1] = in2;
        out = new TypedIOPort(this, "out", false, true);
        out.setTypeEquals(ptolemy.data.type.BaseType.INT);
    }

    public void fire() throws IllegalActionException {
        boolean [] hasRead = new boolean[2];
        for (int i = 0; i < hasRead.length; i++) {
            hasRead[i] = false;
        }
        Token result = null;
        ConditionalReceive [] branches = new ConditionalReceive[2];
        while (true) {
            for (int i = 0; i < branches.length; i++) {
                branches[i] = new ConditionalReceive(!hasRead[i], _ports[i], 0, i, this.getConditionalBranchController());
            }
            int successfulBranch = chooseBranch(branches);
             System.out.println("CSPIn chooseBranch returns:" + successfulBranch);
            if (successfulBranch != -1) {
                if (result == null)
                    result = branches[successfulBranch].getToken();
                else {
                    result = result.add(branches[successfulBranch].getToken());
                }
                hasRead[successfulBranch] = true;
            } else {
                out.send(0,result);
                return;
            }
        }
    }

public boolean postfire() {
        return true;
    }

    public TypedIOPort in1, in2, out;

    private TypedIOPort [] _ports;
}
