package ptolemy.actor.lib.jmf;

import java.net.URL;

import javax.media.Manager;
import javax.media.protocol.DataSource;

import ptolemy.actor.Director;
import ptolemy.actor.lib.Source;
import ptolemy.data.ObjectToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// StreamLoader
/**
@author James Yeh
@version $Id$
@since Ptolemy II 3.1
*/
public class StreamLoader extends Source {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public StreamLoader(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        output.setTypeEquals(BaseType.OBJECT);
        fileOrURL = new FileParameter(this, "fileOrURL");
    }

    public FileParameter fileOrURL;
    
    public void initialize() throws IllegalActionException {
        super.initialize();
        URL url = fileOrURL.asURL();
        if (url == null) {
            throw new IllegalActionException("URLToken was null");
        } else {
            try {
                _dataSource = Manager.createDataSource(url);
            } catch (Exception error) {
                throw new IllegalActionException("Invalid URL");
            }
        }
        Director director = getDirector();
        if (director != null) {
            director.fireAt(this, 0.0);
        } else {
            throw new IllegalActionException(this, "No director");
        }
    }

    public boolean postfire() throws IllegalActionException {
        if (getDirector().getCurrentTime() == 0.0) {
            output.send(0, new ObjectToken(_dataSource));
        }
        return super.postfire();
    }

    private DataSource _dataSource;
}
