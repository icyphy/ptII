/**
 *
 */
package ptolemy.actor.gt;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ActorToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * @author tfeng
 *
 */
public class EmptyModel extends TypedAtomicActor {

    public EmptyModel(CompositeEntity container, String name)
    throws IllegalActionException, NameDuplicationException {
        super(container, name);

        setClassName("ptolemy.actor.gt.EmptyModel");

        trigger = new TypedIOPort(this, "trigger", true, false);
        trigger.setMultiport(true);

        modelOutput = new TypedIOPort(this, "modelOutput", false, true);
        modelOutput.setTypeEquals(ActorToken.TYPE);

        modelName = new StringParameter(this, "modelName");
        modelName.setExpression("generatedModel");
    }

    public void fire() throws IllegalActionException {
        if (trigger.getWidth() > 0 && trigger.hasToken(0)) {
            trigger.get(0);
            TypedCompositeActor entity =
                new TypedCompositeActor(workspace());
            try {
                entity.setName(
                		((StringToken) modelName.getToken()).stringValue());
            } catch (NameDuplicationException e) {
                throw new IllegalActionException(this, e,
                        "Unexpected exception.");
            }
            modelOutput.send(0, new ActorToken(entity));
        }
    }

    public StringParameter modelName;

    public TypedIOPort modelOutput;

    public TypedIOPort trigger;
}
