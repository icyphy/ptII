package ptolemy.domains.gro.lib;

import javax.media.opengl.GL;

import ptolemy.actor.TypedIOPort;
import ptolemy.domains.gro.kernel.GROActor;
import ptolemy.domains.gro.kernel.GRODirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public class Square3D extends GROActor {

    public Square3D(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        GLPipelineObject = new TypedIOPort(this, "GLPipelineObject");
        GLPipelineObject.setOutput(true);
        //GLPipelineObject.setTypeEquals(SceneGraphToken.TYPE);

        // TODO Auto-generated constructor stub
    }
    
    public TypedIOPort GLPipelineObject;

  
    public void fire() throws IllegalActionException {
        if (_debugging) {
            _debug("Called fire()");
        }
        GL gl = ((GRODirector) getDirector()).getGL();
        gl.glBegin(GL.GL_QUADS); 
        gl.glColor3f(0.3f, 0.7f, 0.3f); 
        gl.glVertex3f(-1.0f, 0.0f,  1.0f); 
        gl.glVertex3f( 1.0f, 0.0f,  1.0f); 
        gl.glVertex3f( 1.0f, 0.0f, -1.0f); 
        gl.glVertex3f(-1.0f, 0.0f, -1.0f); 
        gl.glEnd();
    }


}
