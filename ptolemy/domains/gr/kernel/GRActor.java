package ptolemy.domains.gr.kernel;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;

import javax.media.j3d.*;
import javax.vecmath.*;

public class GRActor extends TypedAtomicActor {

    public GRActor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }
    
    
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        GRActor newobj = (GRActor)super.clone(workspace);
        return newobj;
    }

    public Node getNodeObject() {
        return null;
    }
    
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        super.attributeChanged(attribute);
        _createModel();
    }
    
    public void initialize() throws IllegalActionException {
        super.initialize();
        _createModel();
    }
    
    public void fire() throws IllegalActionException {
    }

    public void makeSceneGraphConnection() throws IllegalActionException {
    }
    
    public void addChild(Node node) throws IllegalActionException {
        throw new IllegalActionException("3D domain actor" + this +
                        " cannot have children");
    }
    
    protected void _createModel() throws IllegalActionException {
    }
}
