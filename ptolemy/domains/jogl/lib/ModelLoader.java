package ptolemy.domains.jogl.lib;

import javax.media.opengl.GL;
import ptolemy.actor.lib.Sink;
import ptolemy.data.ObjectToken;
import ptolemy.data.expr.StringParameter;
import ptolemy.domains.jogl.objLoader.OBJModel;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public class ModelLoader extends Sink{
    
    /**
     *  Load a name of the 3D object in the given container with the given name.
     *  If the container argument is null, a NullPointerException will
     *  be thrown. If the name argument is null, then the name is set
     *  to the empty string. Increment the version number of the workspace.
     *
     *  @param container Container of the director.
     *  @param name Name of this Line3D.
     *  @exception IllegalActionException If this actor
     *  is not compatible with the specified container.
     *  @exception NameDuplicationException If the container not a
     *  CompositeActor and the name collides with an entity in the container.
     */


    public ModelLoader(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        
        modelName = new StringParameter(this, "3D Model");
        modelName.setExpression("pawn");
        modelName.addChoice("penguin");
        modelName.addChoice("barbell");
        modelName.addChoice("heli");


       
    }
    
    
    public StringParameter modelName;
    
    private OBJModel model;
    
    public void fire() throws IllegalActionException {
        if (_debugging) {
            _debug("Called fire()");
        }
        if (input.hasToken(0)) {
            
            GL gl= null;
            String name = "bo";
            float maxSize = 1;
            model = new OBJModel(name, maxSize, gl, true);
            ObjectToken inputToken = (ObjectToken)input.get(0);
            Object inputObject = inputToken.getValue();
            if (!(inputObject instanceof GL)) {
                throw new IllegalActionException(this,
                        "Input is required to be an instance of GL. Got "
                        + inputObject.getClass());
            }
            
        }
        
    }

}

