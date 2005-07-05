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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Image;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import ptolemy.actor.gui.Placeable;
import ptolemy.actor.gui.SizeAttribute;
import ptolemy.actor.gui.TableauFrame;
import ptolemy.actor.gui.WindowPropertiesAttribute;
import ptolemy.actor.lib.Sink;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.data.ImageToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.media.Picture;
import ptolemy.actor.TypedIOPort;

import ij.gui.StackWindow;
import ij.gui.ImageWindow;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.process.ImageProcessor;

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

public class ImageCrop extends TypedAtomicActor{

    /** Create an instance with ... (describe the properties of the
     *  instance). Use the imperative case here.
     *  @param parameterName Description of the parameter.
     *  @exception ExceptionClass If ... (describe what
     *   causes the exception to be thrown).
     */
    public ImageCrop(CompositeEntity container, String name) throws IllegalActionException,
        NameDuplicationException{

        super(container, name);
        imageInput = new TypedIOPort(this,"imageInput");
        imageInput.setInput(true);
        imageInput.setTypeEquals(BaseType.OBJECT);

        roi = new TypedIOPort(this,"roi");
        roi.setInput(true);
        roi.setTypeEquals(BaseType.OBJECT);

        output = new TypedIOPort(this,"output");
        output.setOutput(true);
        output.setTypeEquals(BaseType.OBJECT);

        stack = new Parameter(this, "stack");
        stack.setExpression("true");
        stack.setTypeEquals(BaseType.BOOLEAN_MATRIX);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Desription of the variable. */
    public TypedIOPort imageInput;
    public TypedIOPort roi;
    public TypedIOPort output;
    public Parameter stack;


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Do something... (Use the imperative case here, such as:
     *  "Return the most recently recorded event.", not
     *  "Returns the most recently recorded event."
     *  @param parameterName Description of the parameter.
     *  @return Description of the returned value.
     *  @exception ExceptionClass If ... (describe what
     *   causes the exception to be thrown).
     */
    public void fire()throws IllegalActionException {
            //Do cropping
        ObjectToken objectToken = (ObjectToken)roi.get(0);
        Roi roi = (Roi)objectToken.getValue();
        ImageProcessor imageProcessor = _imagePlus.getProcessor();
        imageProcessor.setRoi(roi);
        ImageProcessor croppedProcessor = imageProcessor.crop();
        _imagePlus = new ImagePlus("Cropped Image", croppedProcessor);
    }

    public void initialize()throws IllegalActionException {
            _stack = ((BooleanToken)stack.getToken()).booleanValue();


    }

    public boolean prefire()throws IllegalActionException {
        //Do the listening in this section, and when approrpiate tokens
        //are receieved return true.
            if (imageInput.hasToken(0)){
                    ObjectToken objectToken = (ObjectToken)imageInput.get(0);
                    _imagePlus = (ImagePlus)objectToken.getValue();

                    //Check to see if stack of image and show user proper frame
                    if (_stack){
                            _stackWindow = new StackWindow(_imagePlus);
                    }else{
                            _imageWindow = new ImageWindow(_imagePlus);
            }
        }
        if (_stack){
            _stackWindow.addMouseListener(new mousePressHandler());
            _stackWindow.addMouseListener(new mouseReleaseHandler());
            _stackWindow.addMouseMotionListener(new mouseMotionHandler());
        }else{
            _imageWindow.addMouseListener(new mousePressHandler());
            _imageWindow.addMouseListener(new mouseReleaseHandler());
            _imageWindow.addMouseMotionListener(new mouseMotionHandler());
        }
        //Create roi
        int height = _finalY - _startY;
        int width = _finalX = _startX;
            _roi = new Roi(_startX, _startY, width, height);

        //Send ROI to input to be used in firing
        output.broadcast(new ObjectToken(_roi));

        //Always returns true
        return true;

    }


    public boolean postfire()throws IllegalActionException {
            //   check for to see if user is finished if not return true
            //if so return false, and broadcast new ImagePlus

        //Show user cropped image and listen for keyboard to return true of false
        //FIXME Is this safe?  Will image wait for keyboard input
        if (_stack){

            _stackWindow = new StackWindow(_imagePlus);
            _stackWindow.addKeyListener(new keyPressHandler());

        }else{
            _imageWindow = new ImageWindow(_imagePlus);
            _imageWindow.addKeyListener(new keyPressHandler());
        }

        //Send the new _imagePlus to output
        if(_cropFinish){
                output.broadcast(new ObjectToken(_imagePlus));
        }
        return !_cropFinish;

    }
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Describe your method, again using imperative case.
     *  @see RelevantClass#methodName()
     *  @param parameterName Description of the parameter.
     *  @return Description of the returned value.
     *  @exception ExceptionClass If ... (describe what
     *   causes the exception to be thrown).
     */
    protected int _protectedMethodName()throws IllegalActionException {
        return 1;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Description of the variable. */
    protected int _aProtectedVariable;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Private methods need not have Javadoc comments, although it can
    // be more convenient if they do, since they may at some point
    // become protected methods.
    //MouseEvent buttonPressed;
    class mousePressHandler extends MouseAdapter
      {
        public void mousePressed (MouseEvent startRoi)
        {
          int button;
          //double startX, startY;
          _tracking = true;
          button = startRoi.getButton();
          _startX = startRoi.getX();
          _startY = startRoi.getY();
          System.out.println("press startX="+_startX+"   startY="+_startY+"   button="+button);
          if (button == 1){
                  if (_stack){
                          _stackWindow.requestFocus();
                          _stackWindow.repaint();
                  }else{
                          _imageWindow.requestFocus();
                          _imageWindow.repaint();
                  }
          }
        }
      }
    class mouseMotionHandler extends MouseMotionAdapter
    {
      public void mouseDragged (MouseEvent drawRoi)
      {
        int button;
        int x, y;
        //double currentX, currentY, x, y;

        button = drawRoi.getButton();
        x = drawRoi.getX();
        y = drawRoi.getY();
        if (_tracking){
                _currentX = x;
                _currentY = y;
        }
        System.out.println("press currentX= "+x+"   currentY="+y+"   button="+button);

            if (_stack){
                _stackWindow.requestFocus();
                _stackWindow.repaint();
            }else{
                _imageWindow.requestFocus();
                _imageWindow.repaint();
            }

      }
    }

    class mouseReleaseHandler extends MouseAdapter
    {
      public void mouseReleased (MouseEvent finishRoi)
      {
        int button;
        //double finalX, finalY;
        _tracking = false;
        button = finishRoi.getButton();
        _finalX = finishRoi.getX();
        _finalY = finishRoi.getY();
        System.out.println("press finalX= "+_finalX+"   finalY="+_finalY+"   button="+button);
        if (button == 1){
            if (_stack){
                _stackWindow.requestFocus();
                _stackWindow.repaint();
            }else{
                _imageWindow.requestFocus();
                _imageWindow.repaint();
            }
        }
      }
    }

    class keyPressHandler extends KeyAdapter
    {
      public void keyPressed (KeyEvent finishKey)
      {
        int z = finishKey.getKeyCode();
        //Press enter key to end session or space bar to continue
        if (z == 10){
          _cropFinish = true;
        }else if (z == 32){
         _cropFinish = false;
        }

      }
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Private variables need not have Javadoc comments, although it can
    // be more convenient if they do, since they may at some point
    // become protected variables.

    private ImagePlus _croppedImage;
    private ImagePlus _imagePlus;
    private Roi _roi;
    private boolean _stack;
    private StackWindow _stackWindow;
    private ImageWindow _imageWindow;
    private boolean _tracking;
    private int _startX, _startY, _finalX, _finalY, _currentX, _currentY;
    private boolean _cropFinish;

}
