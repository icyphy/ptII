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

public class Translate extends GROActor implements Transformation {

    public Translate(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        GLPipelineObjectIn = new TypedIOPort(this, "GLPipelineObjectIn", true, false);
        GLPipelineObjectIn.setMultiport(true);
        GLPipelineObjectIn.setTypeEquals(BaseType.OBJECT);
        GLPipelineObjectOut = new TypedIOPort(this, "GLPipelineObjectOut", false, true);
        GLPipelineObjectOut.setTypeEquals(BaseType.OBJECT);
        
        translation = new PortParameter(this, "translation");
        translation.setExpression("{0.0, 0.0, 0.0}");
        
    }
    
    public PortParameter translation;

    public TypedIOPort GLPipelineObjectIn;

    public TypedIOPort GLPipelineObjectOut;
  
    public void fire() throws IllegalActionException {
        translation.update();
        GL gl = ((GRODirector) getDirector()).getGL();

        ArrayToken translationValue = ((ArrayToken) translation.getToken());
    
        gl.glTranslated(
            ((DoubleToken) translationValue.getElement(0)).doubleValue(), 
            ((DoubleToken) translationValue.getElement(1)).doubleValue(), 
            ((DoubleToken) translationValue.getElement(2)).doubleValue() 
        );         
    }
}
