/* Converter Range Model

 Copyright (c) 1999-2014 The Regents of the University of California.
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
package ptolemy.actor.lib.javasound.test.pitchshift;

import javax.swing.BoundedRangeModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

//////////////////////////////////////////////////////////////////////////
//// ConverterRangeModel

/**
 Based on the source code for DefaultBoundedRangeModel,
 this class stores its value as a double, rather than
 an int.  The minimum value and extent are always 0.

 @author Brian Vogel
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Red (vogel)
 @Pt.AcceptedRating Red (vogel)
 */
public class ConverterRangeModel implements BoundedRangeModel {
    protected ChangeEvent changeEvent = null;

    protected EventListenerList listenerList = new EventListenerList();

    //protected int maximum = 10000;
    protected int maximum = 3000;

    //protected int minimum = 0;
    protected int minimum = 400;

    protected int extent = 0;

    //protected double value = 0.0;
    protected double value = 1000.0;

    protected double multiplier = 1.0;

    protected boolean isAdjusting = false;

    final static boolean DEBUG = false;

    public ConverterRangeModel() {
    }

    public double getMultiplier() {
        if (DEBUG) {
            System.out.println("In ConverterRangeModel getMultiplier");
        }

        return multiplier;
    }

    public void setMultiplier(double multiplier) {
        if (DEBUG) {
            System.out.println("In ConverterRangeModel setMultiplier");
        }

        this.multiplier = multiplier;
        fireStateChanged();
    }

    @Override
    public int getMaximum() {
        if (DEBUG) {
            System.out.println("In ConverterRangeModel getMaximum");
        }

        return maximum;
    }

    @Override
    public void setMaximum(int newMaximum) {
        if (DEBUG) {
            System.out.println("In ConverterRangeModel setMaximum");
        }

        setRangeProperties(value, extent, minimum, newMaximum, isAdjusting);
    }

    @Override
    public int getMinimum() {
        return minimum;
    }

    @Override
    public void setMinimum(int newMinimum) {
        System.out.println("In ConverterRangeModel setMinimum");

        //Do nothing.
    }

    @Override
    public int getValue() {
        if (DEBUG) {
            System.out.println("In ConverterRangeModel getValue");
        }

        return (int) getDoubleValue();
    }

    @Override
    public void setValue(int newValue) {
        if (DEBUG) {
            System.out.println("In ConverterRangeModel setValue");
        }

        setDoubleValue(newValue);
    }

    public double getDoubleValue() {
        if (DEBUG) {
            System.out.println("In ConverterRangeModel getDoubleValue");
        }

        return value;
    }

    public void setDoubleValue(double newValue) {
        if (DEBUG) {
            System.out.println("In ConverterRangeModel setDoubleValue");
        }

        setRangeProperties(newValue, extent, minimum, maximum, isAdjusting);
    }

    @Override
    public int getExtent() {
        return extent;
    }

    @Override
    public void setExtent(int newExtent) {
        //Do nothing.
    }

    @Override
    public boolean getValueIsAdjusting() {
        return isAdjusting;
    }

    @Override
    public void setValueIsAdjusting(boolean b) {
        setRangeProperties(value, extent, minimum, maximum, b);
    }

    @Override
    public void setRangeProperties(int newValue, int newExtent, int newMin,
            int newMax, boolean newAdjusting) {
        System.out.println("In ConverterRangeModel setRangeProperties");
        setRangeProperties((double) newValue, newExtent, newMin, newMax,
                newAdjusting);
    }

    public void setRangeProperties(double newValue, int unusedExtent,
            int unusedMin, int newMax, boolean newAdjusting) {
        if (DEBUG) {
            System.out.println("setRangeProperties(): " + "newValue = "
                    + newValue + "; newMax = " + newMax);
        }

        if (newMax <= minimum) {
            newMax = minimum + 1;

            if (DEBUG) {
                System.out.println("maximum raised by 1 to " + newMax);
            }
        }

        if (Math.round(newValue) > newMax) { //allow some rounding error
            newValue = newMax;

            if (DEBUG) {
                System.out.println("value lowered to " + newMax);
            }
        }

        boolean changeOccurred = false;

        if (newValue != value) {
            if (DEBUG) {
                System.out.println("value set to " + newValue);
            }

            value = newValue;
            changeOccurred = true;
        }

        if (newMax != maximum) {
            if (DEBUG) {
                System.out.println("maximum set to " + newMax);
            }

            maximum = newMax;
            changeOccurred = true;
        }

        if (newAdjusting != isAdjusting) {
            maximum = newMax;
            isAdjusting = newAdjusting;
            changeOccurred = true;
        }

        if (changeOccurred) {
            fireStateChanged();
        }
    }

    /*
     * The rest of this is event handling code copied from
     * DefaultBoundedRangeModel.
     */
    @Override
    public void addChangeListener(ChangeListener l) {
        listenerList.add(ChangeListener.class, l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        listenerList.remove(ChangeListener.class, l);
    }

    protected void fireStateChanged() {
        Object[] listeners = listenerList.getListenerList();

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ChangeListener.class) {
                if (changeEvent == null) {
                    changeEvent = new ChangeEvent(this);
                }

                ((ChangeListener) listeners[i + 1]).stateChanged(changeEvent);
            }
        }
    }
}
