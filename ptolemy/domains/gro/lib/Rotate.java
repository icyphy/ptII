package ptolemy.domains.gro.lib;

import javax.media.opengl.GL;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.domains.gro.kernel.GROActor;
import ptolemy.domains.gro.kernel.GRODirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public class Rotate extends GROActor {

    public Rotate(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        GLPipelineObjectIn = new TypedIOPort(this, "GLPipelineObjectIn", true, false);
        GLPipelineObjectIn.setMultiport(true);
        GLPipelineObjectOut = new TypedIOPort(this, "GLPipelineObjectOut", false, true);

        rotation = new PortParameter(this, "rotation");
        rotation.setExpression("{0.0, 0.0, 0.0, 0.0}");
    }

    public PortParameter rotation;
    
    public TypedIOPort GLPipelineObjectIn;

    public TypedIOPort GLPipelineObjectOut;
  
    
    public void fire() throws IllegalActionException {
        GL gl = ((GRODirector) getDirector()).getGL();
        ArrayToken rotationValue = ((ArrayToken) rotation.getToken());
        
        // FIXME: need to do the push and pop matrix in the director
        gl.glPopMatrix();
        
        gl.glPushMatrix();

        gl.glRotated(
                ((DoubleToken) rotationValue.getElement(0)).doubleValue(), 
                ((DoubleToken) rotationValue.getElement(1)).doubleValue(), 
                ((DoubleToken) rotationValue.getElement(2)).doubleValue(), 
                ((DoubleToken) rotationValue.getElement(3)).doubleValue()
                ); 
    }
}
