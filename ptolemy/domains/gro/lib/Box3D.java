package ptolemy.domains.gro.lib;

import javax.media.opengl.GL;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.type.BaseType;
import ptolemy.domains.gro.kernel.GROActor;
import ptolemy.domains.gro.kernel.GRODirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public class Box3D extends GROActor {

    public Box3D(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        GLPipelineObject = new TypedIOPort(this, "GLPipelineObject");
        GLPipelineObject.setOutput(true);
        GLPipelineObject.setTypeEquals(BaseType.OBJECT);
    }
    
    public TypedIOPort GLPipelineObject;

  
    public void fire() throws IllegalActionException {
        if (_debugging) {
            _debug("Called fire()");
        }
        GL gl = ((GRODirector) getDirector()).getGL();
        gl.glBegin(GL.GL_QUADS); 
        
        gl.glColor3f(0.3f, 0.7f, 0.3f); 
      
        gl.glVertex3f(-1.0f, -1.0f, -1.0f);  // Bottom Face
        gl.glVertex3f(1.0f, -1.0f, -1.0f);
        gl.glVertex3f(1.0f, -1.0f, 1.0f);
        gl.glVertex3f(-1.0f, -1.0f, 1.0f);

        gl.glVertex3f(-1.0f, -1.0f, 1.0f);  // Front Face
        gl.glVertex3f(1.0f, -1.0f, 1.0f);
        gl.glVertex3f(1.0f, 1.0f, 1.0f);
        gl.glVertex3f(-1.0f, 1.0f, 1.0f);

   
        gl.glVertex3f(-1.0f, -1.0f, -1.0f);  // Back Face
        gl.glVertex3f(-1.0f, 1.0f, -1.0f);
        gl.glVertex3f(1.0f, 1.0f, -1.0f);
        gl.glVertex3f(1.0f, -1.0f, -1.0f);

        gl.glVertex3f(1.0f, -1.0f, -1.0f);  // Right face
        gl.glVertex3f(1.0f, 1.0f, -1.0f);
        gl.glVertex3f(1.0f, 1.0f, 1.0f);
        gl.glVertex3f(1.0f, -1.0f, 1.0f);

        gl.glVertex3f(-1.0f, -1.0f, -1.0f);  // Left Face
        gl.glVertex3f(-1.0f, -1.0f, 1.0f);
        gl.glVertex3f(-1.0f, 1.0f, 1.0f);
        gl.glVertex3f(-1.0f, 1.0f, -1.0f);
        
        gl.glEnd();
    }


}
