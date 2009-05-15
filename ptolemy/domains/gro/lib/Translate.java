package ptolemy.domains.gro.lib;

import javax.media.opengl.GL;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.gro.kernel.GROActor;
import ptolemy.domains.gro.kernel.GRODirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public class Translate extends GROActor {

    public Translate(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        GLPipelineObjectIn = new TypedIOPort(this, "GLPipelineObjectIn", true, false);
        GLPipelineObjectIn.setMultiport(true);
        GLPipelineObjectOut = new TypedIOPort(this, "GLPipelineObjectOut", false, true);
        
        translation = new Parameter(this, "translation");
        translation.setExpression("{0.0, 0.0, 0.0}");
        
    }
    
    public Parameter translation;

    public TypedIOPort GLPipelineObjectIn;

    public TypedIOPort GLPipelineObjectOut;
  
    public void fire() throws IllegalActionException {
        GL gl = ((GRODirector) getDirector()).getGL();

        ArrayToken translationValue = ((ArrayToken) translation.getToken());
        
        // FIXME: need to do the push and pop matrix in the director
        gl.glPopMatrix();
        
        gl.glPushMatrix();
        
        gl.glTranslated(
            ((DoubleToken) translationValue.getElement(0)).doubleValue(), 
            ((DoubleToken) translationValue.getElement(1)).doubleValue(), 
            ((DoubleToken) translationValue.getElement(2)).doubleValue() 
        );         
    }
}
