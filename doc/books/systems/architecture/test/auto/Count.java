
package doc.books.systems.architecture.test.auto;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public class Count extends TypedAtomicActor {
    /** Constructor */
    public Count(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        trigger = new TypedIOPort(this, "trigger", true, false);
        initial = new Parameter(this, "initial", new IntToken(0));
        initial.setTypeEquals(BaseType.INT);
        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.INT);
    }
    /** The trigger input port. */
    public TypedIOPort trigger;

    /** The output port. */
    public TypedIOPort output;

    /** The initial count. */
    public Parameter initial;

    /** Reset the count to the initial value. */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _count = ((IntToken)initial.getToken()).intValue();
    }

    /** Consume the trigger input and output the incremented count. */
    public void fire() throws IllegalActionException {
        super.fire();
        if (trigger.getWidth() > 0 && trigger.hasToken(0)) {
             trigger.get(0);
        }
        output.send(0, new IntToken(_count + 1));
    }

    /** Record the most updated count. */
    public boolean postfire() throws IllegalActionException {
        _count += 1;
        return super.postfire();
    }

    /** The local variable. */
    private int _count = 0;
}