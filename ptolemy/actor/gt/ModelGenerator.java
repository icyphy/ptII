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
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.moml.MoMLParser;

/**
 * @author tfeng
 *
 */
public class ModelGenerator extends TypedAtomicActor {

    public ModelGenerator(CompositeEntity container, String name)
    throws IllegalActionException, NameDuplicationException {
        super(container, name);

        setClassName("ptolemy.actor.gt.ModelGenerator");

        moml = new TypedIOPort(this, "moml", true, false);
        moml.setTypeEquals(BaseType.STRING);

        modelName = new TypedIOPort(this, "modelName", true, false);
        modelName.setTypeEquals(BaseType.STRING);
        
        model = new TypedIOPort(this, "model", false, true);
        model.setTypeEquals(ActorToken.TYPE);
    }
    
    public boolean prefire() throws IllegalActionException {
        return super.prefire() && (moml.getWidth() > 0 && moml.hasToken(0)
                || modelName.getWidth() > 0 && modelName.hasToken(0));
    }

    public void fire() throws IllegalActionException {
        try {
            Entity entity;
            if (moml.getWidth() > 0 && moml.hasToken(0)) {
                String momlString = ((StringToken) moml.get(0)).stringValue();
                entity = (Entity) _parser.parse(momlString);
            } else {
                if (_emptyModel == null) {
                    _emptyModel = new TypedCompositeActor(workspace());
                }
                entity = _emptyModel;
            }
    
            if (modelName.getWidth() > 0 && modelName.hasToken(0)) {
                String name = ((StringToken) modelName.get(0)).stringValue();
                entity.setName(name);
            }
            
            model.send(0, new ActorToken(entity));
        } catch (Exception e) {
            throw new IllegalActionException(this, "Unable to parse moml.");
        }
    }
    
    private Entity _emptyModel;
    
    private MoMLParser _parser = new MoMLParser();

    public TypedIOPort modelName;
    
    public TypedIOPort moml;

    public TypedIOPort model;
}
