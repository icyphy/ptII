/* Slider.

 Copyright (c) 1998-1999 The Regents of the University of California.
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
@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (eal@eecs.berkeley.edu)

*/

package ptolemy.gui;

import ptolemy.kernel.*;
import ptolemy.kernel.event.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.event.ChangeListener;

//////////////////////////////////////////////////////////////////////////
//// Slider
/**
Create a slider.

@author Manda Sutijono
@version $Id: Slider.java
*/
public class Slider extends JPanel {
    
    /** Construct a slider with the following properties on it.
     */
    public Slider() {
        _slider = new JSlider(_orientation, _minimum, _maximum, 
                _value);
        _slider.addChangeListener(new SliderListener());
    }

    ///////////////////////////////////////////////////////////////////
    ////                   ports and parameters                    ////    
    
    public Parameter minimum;
    public Parameter maximum;
    public Parameter value;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Specify the preferred maximum value of the slider.
     *  @param max The preferred maximum.
     */
    public void setNewMax(int newMax) 
            throws ChangeFailedException {
        _maximum = newMax;
        _slider.setMaximum(newMax);
    }

    /** Specify the preferred minimum value of the slider.
     *  @param min The preferred minimum.
     */
    public void setNewMin(int newMin) 
            throws ChangeFailedException {
        _minimum = newMin;
        _slider.setMinimum(newMin);
    }

    /** Specify the preferred value of the slider.
     *  @param value The preferred value.
     */
    public void setNewValue(int newValue) 
            throws ChangeFailedException {
        _value = newValue;
        _slider.setValue(newValue);
    }

    /** Switches slider's orientation, from horizontal to vertical, 
     *  or vice versa.
     */
    public void switchOrientation() {
        if(_orientation == JSlider.HORIZONTAL) {
            _orientation = JSlider.VERTICAL;
            _slider.setOrientation(JSlider.VERTICAL);
        } else {
            _orientation = JSlider.HORIZONTAL;
            _slider.setOrientation(JSlider.HORIZONTAL);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  //// 

    public static final int DEFAULT_MIN = 0;
    public static final int DEFAULT_MAX = 100;
    public static final int DEFAULT_VALUE = 50;
    public static final int DEFAULT_ORIENTATION = JSlider.HORIZONTAL;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The minimum value of the slider.
    private int _minimum = DEFAULT_MIN;

    // The maximum value of the slider.
    private int _maximum = DEFAULT_MAX;

    // The value of the slider.
    private int _value = DEFAULT_VALUE;

    // The orientation of the slider.
    private int _orientation = DEFAULT_ORIENTATION;

    // The JSlider.
    private JSlider _slider;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Listener for changes in slider.
     */
    class SliderListener implements ChangeListener {
        public void stateChanged(ChangeEvent event) {
            JSlider tempSlider = (JSlider)event.getSource();  
            if (tempSlider.getValueIsAdjusting()) {
                try {
                    setNewValue(tempSlider.getValue());
                } catch (ChangeFailedException error) {
                    System.out.println("Unable to execute change");
                }
            }
        }
    }
}
