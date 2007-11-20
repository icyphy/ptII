/**
 *
 */
package ptolemy.actor.gt;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ActorToken;
import ptolemy.data.StringToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * @author tfeng
 *
 */
public class ModelGenerator extends TypedAtomicActor {

    public ModelGenerator(CompositeEntity container, String name)
    throws IllegalActionException, NameDuplicationException {
        super(container, name);

        setClassName("ptolemy.actor.gt.ModelGenerator");

        modelName = new TypedIOPort(this, "modelName", true, false);
        modelName.setTypeEquals(BaseType.STRING);

        modelOutput = new TypedIOPort(this, "modelOutput", false, true);
        modelOutput.setTypeEquals(ActorToken.TYPE);
    }

    public void fire() throws IllegalActionException {
        if (modelName.getWidth() > 0 && modelName.hasToken(0)) {
            String name = ((StringToken) modelName.get(0)).stringValue();
            TypedCompositeActor entity = new TypedCompositeActor(workspace());
            try {
                entity.setName(name);
            } catch (NameDuplicationException e) {
                throw new IllegalActionException(this, e,
                        "Unexpected exception.");
            }
            modelOutput.send(0, new ActorToken(entity));
        }
    }

    public TypedIOPort modelName;

    public TypedIOPort modelOutput;
}
