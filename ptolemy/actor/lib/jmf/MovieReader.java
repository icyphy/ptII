/* An actor that scales a javax.media.jai.RenderedOp

@Copyright (c) 2002-2003 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

                                                PT_COPYRIGHT_VERSION 2
                                                COPYRIGHTENDKEY

@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.jmf;

import java.net.URL;

import javax.media.Buffer;
import javax.media.ConfigureCompleteEvent;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.EndOfMediaEvent;
import javax.media.Manager;
import javax.media.Player;
import javax.media.PrefetchCompleteEvent;
import javax.media.RealizeCompleteEvent;
import javax.media.ResourceUnavailableEvent;
import javax.media.control.FrameGrabbingControl;
import javax.media.control.FramePositioningControl;
import javax.media.protocol.DataSource;

import ptolemy.actor.lib.Source;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.attributes.FileAttribute;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// blahblah
/**
   Scale a RenderedOp using the javax.media.jai.JAI class.

   @author James Yeh
   @version $Id$
   @since Ptolemy II 3.0
*/

public class MovieReader extends Source implements ControllerListener {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */    
    public MovieReader(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        output.setTypeEquals(BaseType.OBJECT);
        fileOrURL = new FileAttribute(this, "fileOrURL");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    public FileAttribute fileOrURL;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == fileOrURL) {
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
        } else {
            super.attributeChanged(attribute);
        }
    }
    
    public void controllerUpdate(ControllerEvent event) {
        if (event instanceof ConfigureCompleteEvent ||
                event instanceof RealizeCompleteEvent ||
                event instanceof PrefetchCompleteEvent) {
            synchronized (_waitSync) {
                _stateTransitionOK = true;
                _waitSync.notifyAll();
            }
        } else if (event instanceof ResourceUnavailableEvent) {
            synchronized (_waitSync) {
                _stateTransitionOK = false;
                _waitSync.notifyAll();
            }
        } else if (event instanceof EndOfMediaEvent) {
            _player.close();
            _playerOpen = false;
            // check on this in the postfire, if the processor is open
            // return super.postfire(), otherwise return false.
        }
    }

    public void fire() throws IllegalActionException {
        super.fire();
        output.send(0, new JMFImageToken(_frame));
    }

    public void initialize() throws IllegalActionException {
        super.initialize();
        try {
            _player = Manager.createPlayer(_dataSource);
        } catch (Exception e) {
            throw new IllegalActionException(null, e,
                    "Failed to create a player for the data source. "
                    + "Note that you may need to run jmfinit, which is found "
                    + "in the JMF directory, for example c:/Program Files/"
                    + "JMF2.1.1/bin.  The original exception was: "
                    + _dataSource);
        }
        
        _player.addControllerListener(this);

        _player.realize();

        if (!_waitForState(_player.Realized)) {
            throw new IllegalActionException(null,
                    "Failed to realize player");
        }

        _framePositioningControl = 
            (FramePositioningControl)_player.getControl(
                    "javax.media.control.FramePositioningControl");

        if (_framePositioningControl == null) {
            throw new IllegalActionException(null,
                    "Failed to get Frame Poisitioning Control");
        }

        _frameGrabbingControl = 
            (FrameGrabbingControl)_player.getControl(
                    "javax.media.control.FrameGrabbingControl");
        
        if (_frameGrabbingControl == null) {
            throw new IllegalActionException(null,
                    "Failed to get Frame Grabbing Control");
        }

        _player.prefetch();
        
        if (!_waitForState(_player.Prefetched)) {
            throw new IllegalActionException(null,
                    "Failed to prefetch player");
        }

        //load first frame

        _frame = _frameGrabbingControl.grabFrame();
        _framePositioningControl.skip(1);
        
    }

    public boolean postfire() throws IllegalActionException {
        if (_playerOpen == false) {
           _dataSource.disconnect(); 
           return false;  
        } else {
            _frame = _frameGrabbingControl.grabFrame();
            _framePositioningControl.skip(1);
            return super.postfire();
        }
    }

    //public void wrapup() {
    //    _dataSource.disconnect();
    //}

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Block until the processor has transitioned to the given state.
     *  Return false if the transition failed.
     */
    protected boolean _waitForState(int state) throws IllegalActionException {
        synchronized (_waitSync) {
            try {
                while (_player.getState() != state && _stateTransitionOK)
                    _waitSync.wait();
            } catch (Exception e) {
                throw new IllegalActionException(null, e, 
                        "Failed block the processor until it state"
                        + " transition completed.");
            }
        }
        return _stateTransitionOK;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private DataSource _dataSource;
    private Buffer _frame;
    private FrameGrabbingControl _frameGrabbingControl;
    private FramePositioningControl _framePositioningControl;
    private Player _player;
    private boolean _playerOpen = true;
    private boolean _stateTransitionOK = true;
    private Object _waitSync = new Object();

}



    
