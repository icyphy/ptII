package ptolemy.actor.lib.jmf;

import java.io.IOException;
import java.net.URL;

import javax.media.Manager;
import javax.media.Player;
import javax.media.Processor;
import javax.media.protocol.DataSource;

import ptolemy.actor.Director;
import ptolemy.actor.lib.Source;
import ptolemy.data.ObjectToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.attributes.FileAttribute;
import ptolemy.kernel.util.*;

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
        fileOrURL = new FileAttribute(this, "fileOrURL");
    }

    public FileAttribute fileOrURL;
    
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
