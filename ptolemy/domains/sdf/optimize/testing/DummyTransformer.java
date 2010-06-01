package ptolemy.domains.sdf.optimize.testing;

import ptolemy.data.Token;
import ptolemy.domains.sdf.optimize.SharedBufferTransformer;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public class DummyTransformer extends SharedBufferTransformer {

    public DummyTransformer(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    protected void fireCopying() throws IllegalActionException {
        if (input.hasToken(0)) {
            Token t = input.get(0);
            if(!(t instanceof DummyReferenceToken)){
                throw new IllegalActionException("Token is of wrong type. Expected DummyReferenceToken"); 
            }
            DummyReferenceToken rt = (DummyReferenceToken)t;
            // Get and duplicate the frame
            DummyFrame f = ((DummyFrame) rt.getReference()).clone();
            f.value ++;
            // send a new token
            output.send(0, new DummyReferenceToken(f));
        }
    }

    protected void fireExclusive() throws IllegalActionException {
        if (input.hasToken(0)) {
            Token t = input.get(0);
            DummyReferenceToken rt = (DummyReferenceToken)t;
            // Get the frame without duplicating
            DummyFrame f = (DummyFrame) rt.getReference();
            f.value ++;
            // send the original token
            output.send(0, t);
        }
    }

}
