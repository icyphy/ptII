/* An actor that crops images.

 Copyright (c) 1999-2005 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

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

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY
 */
package ptolemy.domains.gr.lib.vr;

import ij.ImagePlus;

import ij.gui.ImageWindow;
import ij.gui.Roi;
import ij.gui.StackWindow;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.FilePortParameter;
import ptolemy.data.AWTImageToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;


//////////////////////////////////////////////////////////////////////////
//// ImageCrop

/**
 Describe your class here, in complete sentences.
 What does it do?  What is its intended use?

 @author Tiffany Crawford
 @version $Id$
 @see classname (refer to relevant classes, but not the base class)
 @since Ptolemy II x.x
 @Pt.ProposedRating Red (yourname)
 @Pt.AcceptedRating Red (reviewmoderator)
 */
public class ROISelect extends TypedAtomicActor {
    /** Create an instance with ... (describe the properties of the
     *  instance). Use the imperative case here.
     *  @param parameterName Description of the parameter.
     *  @exception ExceptionClass If ... (describe what
     *   causes the exception to be thrown).
     */
    public ROISelect(CompositeEntity container, String name)
        throws IllegalActionException, NameDuplicationException {
        super(container, name);
        imageURL = new FilePortParameter(this, "imageURL");

        imageInput = new TypedIOPort(this, "imageInput");
        imageInput.setInput(true);
        imageInput.setTypeEquals(BaseType.OBJECT);

        imageOutput = new TypedIOPort(this, "imageOutput");
        imageOutput.setOutput(true);
        imageOutput.setTypeEquals(BaseType.OBJECT);

        ROI = new TypedIOPort(this, "ROI");
        ROI.setOutput(true);
        ROI.setTypeEquals(BaseType.OBJECT);

        stack = new Parameter(this, "stack");
        stack.setExpression("true");
        stack.setTypeEquals(BaseType.BOOLEAN);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Desription of the variable. */
    public FilePortParameter imageURL;
    public TypedIOPort imageInput;
    public TypedIOPort imageOutput;
    public TypedIOPort ROI;
    public Parameter stack;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Do something... (Use the imperative case here, such as:
     *  "Return the most recently recorded event.", not
     *  "Returns the most recently recorded event."
     *  @param parameterName Description of the parameter.
     *  @exception ExceptionClass If ... (describe what
     *   causes the exception to be thrown).
     */
    public void fire() throws IllegalActionException {
        super.fire();
        ROI.broadcast(new ObjectToken(_roi));

        //Must give actor image to operate on 
        //imageOutput.broadcast(new ObjectToken(_imagePlus));
    }

    public void initialize() throws IllegalActionException {
        super.initialize();
        _stack = ((BooleanToken) stack.getToken()).booleanValue();
        _listeningImageWindow = null;
        _roi = null;
        _imagePlus = new ImagePlus();
        _image = null;
        _startX = 0;
        _startY = 0;
    }

    public boolean postfire() throws IllegalActionException {
        super.postfire();

        //FIXME Is this needed here?  Maybe I should leverage this function...
        //  _listeningImageWindow = null;
        return true;
    }

    public boolean prefire() throws IllegalActionException {
        super.prefire();
        _keyCode = 0;
        _startX = 0;
        _startY = 0;

        //Create ImagePlus from token at input port
        if (imageInput.hasToken(0)) {
            if (_debugging) {
                _debug("there was a token");
            }

            AWTImageToken imageToken = (AWTImageToken) imageInput.get(0);

            if (_debugging) {
                _debug("got token");
            }

            _image = (Image) imageToken.getValue();

            if (_debugging) {
                _debug("got value of token");
            }

            //_imagePlus.setImage(_image);
            System.out.println("Image = " + _image);
            _imagePlus = new ImagePlus("Image", _image);

            if (_debugging) {
                _debug("Input new token");
            }
        }

        if (_debugging) {
            _debug("_imagePlus = " + _imagePlus);
        }

        //Display Image or Stack in appropriate window
        if (_stack && (_stackWindow == null)) {
            _stackWindow = new StackWindow(_imagePlus);
        } else if (_listeningImageWindow == null) {
            System.out.println("Image Window was null");
            System.out.println("Image Window = " + _listeningImageWindow);

            if (_debugging) {
                _debug("Image Window was null");
            }

            _listeningImageWindow = new ListeningImageWindow(_imagePlus);
            System.out.println("Image Window = " + _imagePlus.getWindow());
        } else if (!(_listeningImageWindow == null)) {
            System.out.println("Image Window was not null");
            _listeningImageWindow = new ListeningImageWindow(_imagePlus);

            //_listeningImageWindow.setIconImage(_image);
            //_listeningImageWindow.requestFocus();
        }

        //Listen for keyboard or mouse button input
        _paused = true;
        /*synchronized(this)*/ {
            System.out.println("Inside of critical code");

            while (_paused) { //Wait for keyboard input

                int keyCode = -1;

                synchronized (this) {
                    keyCode = _keyCode;
                }

                if (_debugging) {
                    _debug("_keyCOde = " + _keyCode);
                }

                if (_debugging) {
                    _debug("In while loop, _paused = true");
                }

                if (keyCode == 10) {
                    if (_debugging) {
                        _debug("In while loop, _keyCode = 10");
                    }

                    System.out.println("In EVent thread in prefire() = "
                        + java.awt.EventQueue.isDispatchThread());

                    synchronized (this) {
                        _keyCode = 0;
                    }

                    _createROI();
                    _paused = false;

                    //FIXME Must always return true, in fire resend same ROI
                    //return true;   
                } else if (keyCode == 32) {
                    if (_debugging) {
                        _debug("In while loop, _keyCode = 32");
                    }

                    //FIXME Want to wait for mouse input, set true on MouseReleased?
                    System.out.println("In EVent thread in prefire() = "
                        + java.awt.EventQueue.isDispatchThread());
                    _createROI();
                    _paused = false;

                    synchronized (this) {
                        _keyCode = 0;
                    }

                    //return true;   
                }

                if (_debugging) {
                    _debug("_keyCOde = " + _keyCode);
                }
            }

            System.out.println("Left critical code");
        }

        System.out.println("Left critical code");

        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // Private methods need not have Javadoc comments, although it can
    // be more convenient if they do, since they may at some point
    // become protected methods.
    //MouseEvent buttonPressed;
    private void _createROI() throws IllegalActionException {
        if (_debugging) {
            _debug("creating ROI");
        }

        //FIXME Make user wait for mouse
        //while (_paused) {
        //Listen for mouse released and then _paused = false;
        //draw rectangle as it is being outline.
        //}
        System.out.println("In EVent thread in beginning of createROI() = "
            + java.awt.EventQueue.isDispatchThread());

        int height = _finalY = _startY;
        int width = _finalX = _startX;

        if (_keyCode == 10) {
            _roi = null;
        } else if (_keyCode == 32) {
            //FIXME Mouse does not seem tp pick up until this is called
            _roi = new Roi(_startX, _startY, width, height);
        }

        System.out.println("In EVent thread in end of createROI() = "
            + java.awt.EventQueue.isDispatchThread());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private ImagePlus _imagePlus;
    private Roi _roi;
    private boolean _stack;
    private StackWindow _stackWindow;
    private ListeningImageWindow _listeningImageWindow;
    private Image _image;
    private int _startX;
    private int _startY;
    private int _finalX;
    private int _finalY;
    private boolean _paused;
    private int _keyCode;
    private int _button;

    //FIXME Does this class have to have this structure
    class keyPressHandler extends KeyAdapter {
        public void keyPressed(KeyEvent finishKey) {
            synchronized (this) {
                // this.notifyAll();
                _keyCode = finishKey.getKeyCode();

                //FIXME Switch focus back to keyboard after mouse is pressed
                // _listeningImageWindow.requestFocus();
                //_listeningImageWindow.repaint();
                System.out.println("In EVent thread in keyPressHandler() = "
                    + java.awt.EventQueue.isDispatchThread());
                System.out.println("key pressed = " + _keyCode);
            }
        }
    }

    class mousePressHandler extends MouseAdapter {
        public void mousePressed(MouseEvent startRoi) {
            synchronized (this) {
                //this.notifyAll();
                System.out.println("inside of mousePressed");
                System.out.println("In EVent thread in MousePressHandler() = "
                    + java.awt.EventQueue.isDispatchThread());

                int button;
                System.out.println("defined button");
                System.out.println("tracking is true");
                button = startRoi.getButton();
                System.out.println("got button");
                _startX = startRoi.getX();
                _startY = startRoi.getY();
                System.out.println("mouse button pressed");
                System.out.println("press startX=" + _startX + "   startY="
                    + _startY + "   button=" + button);

                //if (button == 1) {
                if (_stack) {
                    _stackWindow.requestFocus();
                    _stackWindow.repaint();
                } else {
                    _listeningImageWindow.requestFocus();
                    _listeningImageWindow.repaint();
                }
            }
        }
    }

    class mouseMotionHandler extends MouseMotionAdapter {
        public void mouseDragged(MouseEvent drawRoi) {
            synchronized (this) {
                //   this.notifyAll();
                System.out.println("In EVent thread in mouseMotionHandler() = "
                    + java.awt.EventQueue.isDispatchThread());

                int x;
                int y;

                //double currentX, currentY, x, y;
                _button = drawRoi.getButton();
                x = drawRoi.getX();
                y = drawRoi.getY();

                System.out.println("mouse dragged");
                System.out.println("press currentX= " + x + "   currentY=" + y
                    + "   button=" + _button);

                if (_stack) {
                    _stackWindow.requestFocus();
                    _stackWindow.repaint();
                } else {
                    //   _listeningImageWindow.requestFocus();
                    // _listeningImageWindow.repaint();
                }
            }
        }
    }

    class mouseReleaseHandler extends MouseAdapter {
        public void mouseReleased(MouseEvent finishRoi) {
            synchronized (this) {
                //   this.notifyAll();
                System.out.println(
                    "In EVent thread in mouseReleaseHandler() = "
                    + java.awt.EventQueue.isDispatchThread());

                //double finalX, finalY
                _button = finishRoi.getButton();
                _finalX = finishRoi.getX();
                _finalY = finishRoi.getY();
                System.out.println("mouse button released");
                System.out.println("press finalX= " + _finalX + "   finalY="
                    + _finalY + "   button=" + _button);

                //if (_button == 1) {
                if (_stack) {
                    _stackWindow.requestFocus();
                    _stackWindow.repaint();
                } else {
                    //_listeningImageWindow.requestFocus();
                    //_listeningImageWindow.repaint();
                }

                //}
                // _paused = false;
            }
        }
    }

    private class ListeningImageWindow extends ImageWindow {
        //FIXME Once Mouse is pressed keyboard doesn't seem to register
        public ListeningImageWindow(ImagePlus imagePlus) {
            super(imagePlus);

            //FIXME How does this work?
            //setVisible(true);
            this.setTitle("Select ROI");

            //this.addWindowListener(new WindowAdapter(){});
            //this.addMouseListener(new FocusMouseListener());
            this.addKeyListener(new keyPressHandler());
            this.addMouseListener(new mousePressHandler());
            this.addMouseMotionListener(new mouseMotionHandler());
            this.addMouseListener(new mouseReleaseHandler());
            requestFocus();
            repaint();
        }
    }
}
