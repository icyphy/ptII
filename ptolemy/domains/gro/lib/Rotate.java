package ptolemy.domains.gro.lib;

import javax.media.opengl.GL;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.type.BaseType;
import ptolemy.domains.gro.kernel.GROActor;
import ptolemy.domains.gro.kernel.GRODirector;
import ptolemy.domains.gro.kernel.Transformation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public class Rotate extends GROActor implements Transformation {

    public Rotate(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        GLPipelineObjectIn = new TypedIOPort(this, "GLPipelineObjectIn", true, false);
        GLPipelineObjectIn.setMultiport(true);
        GLPipelineObjectIn.setTypeEquals(BaseType.OBJECT);
        GLPipelineObjectOut = new TypedIOPort(this, "GLPipelineObjectOut", false, true);
        GLPipelineObjectOut.setTypeEquals(BaseType.OBJECT);
        
        rotation = new PortParameter(this, "rotation");
        rotation.setExpression("{0.0, 0.0, 0.0, 0.0}");
    }

    public PortParameter rotation;
    
    public TypedIOPort GLPipelineObjectIn;

    public TypedIOPort GLPipelineObjectOut;
  
    
    public void fire() throws IllegalActionException {
        rotation.update();
        GL gl = ((GRODirector) getDirector()).getGL();
        ArrayToken rotationValue = ((ArrayToken) rotation.getToken());
        
        gl.glRotated(
                ((DoubleToken) rotationValue.getElement(0)).doubleValue(), 
                ((DoubleToken) rotationValue.getElement(1)).doubleValue(), 
                ((DoubleToken) rotationValue.getElement(2)).doubleValue(), 
                ((DoubleToken) rotationValue.getElement(3)).doubleValue()
                ); 
    }
}
