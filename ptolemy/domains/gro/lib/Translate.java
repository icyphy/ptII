package ptolemy.domains.gro.lib;

import javax.media.opengl.GL;

import ptolemy.actor.TypedIOPort;
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
    }
    
    public TypedIOPort GLPipelineObjectIn;

    public TypedIOPort GLPipelineObjectOut;
  
    float translateT = 0.0f;
    public void fire() throws IllegalActionException {
        GL gl = ((GRODirector) getDirector()).getGL();
        translateT += 0.0001;
        
        // FIXME: need to do the push and pop matrix in the director
        gl.glPopMatrix();
        
        gl.glPushMatrix();
        
        gl.glTranslatef(1.0f, 0.0f, 0.0f);
        
    }
}
