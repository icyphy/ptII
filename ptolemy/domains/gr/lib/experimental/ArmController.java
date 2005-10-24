/*
  @Copyright (c) 2000-2005 The Regents of the University of California.
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

  PT_COPYRIGHT_VERSION_2
  COPYRIGHTENDKEY


*/
package ptolemy.domains.gr.lib.experimental;

import ptolemy.actor.*;
import ptolemy.actor.gui.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.data.type.*;
import ptolemy.kernel.util.*;

import java.awt.*;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.*;
import java.io.*;
import java.lang.*;
import java.util.*;

import javax.comm.*;


public class ArmController extends TypedAtomicActor {
    public ArmController(TypedCompositeActor container, String name)
        throws NameDuplicationException, IllegalActionException {
        super(container, name);

        input = new TypedIOPort(this, "input");
        input.setInput(true);
        input.setTypeEquals(BaseType.INT);
        output = new TypedIOPort(this, "output");
        output.setOutput(true);
        output.setTypeEquals(BaseType.INT);
        stepSize = new Parameter(this, "step size", new IntToken(1));
    }

    public Parameter stepSize;
    public TypedIOPort input;
    public TypedIOPort output;

    public void initialize() throws IllegalActionException {
        baseValue = 165;
        shoulderValue = 128;
        elbowValue = 128;
        wristValue = 115;
        gripperValue = 128;
        servo6Value = 128;
        currentValue = 128;
        delta = 1;
    }

    private IntToken value;
    private boolean sendsignal = false;

    public void fire() throws IllegalActionException {
        if (input.getWidth() != 0) {
            if (input.hasToken(0)) {
                int value = ((IntToken) input.get(0)).intValue();
                ProcessKey(value);

                try {
                    output.send(0, delimiter);
                    output.send(0, currentControl);
                    output.send(0, new IntToken(currentValue));

                    //Thread.sleep(50);
                    currentControl = nullControl;
                } catch (Exception e) {
                    System.out.println("Serial Exception " + e);
                }
            }
        }
    }

    public void ProcessKey(int value) throws IllegalActionException {
        char chValue = (char) value;
        int stepsize = ((IntToken) stepSize.getToken()).intValue();

        switch (chValue) {
        case 'q': {
            currentControl = base;
            delta = stepsize;
            baseValue = baseValue + delta;

            if (baseValue >= baseUpperLimit) {
                baseValue = baseUpperLimit;
            }

            currentValue = baseValue;

            //System.out.println(currentValue);
            sendsignal = true;
            break;
        }

        case 'a': {
            currentControl = base;
            delta = -stepsize;
            baseValue = baseValue + delta;

            if (baseValue <= baseLowerLimit) {
                baseValue = baseLowerLimit;
            }

            currentValue = baseValue;

            //System.out.println(currentValue);
            sendsignal = true;
            break;
        }

        case 'w': {
            currentControl = shoulder;
            delta = stepsize;
            shoulderValue = shoulderValue + delta;

            if (shoulderValue >= shoulderUpperLimit) {
                shoulderValue = shoulderUpperLimit;
            }

            currentValue = shoulderValue;

            //System.out.println(currentValue);
            sendsignal = true;
            break;
        }

        case 's': {
            currentControl = shoulder;
            delta = -stepsize;
            shoulderValue = shoulderValue + delta;

            if (shoulderValue <= shoulderLowerLimit) {
                shoulderValue = shoulderLowerLimit;
            }

            currentValue = shoulderValue;

            //System.out.println(currentValue);
            sendsignal = true;
            break;
        }

        case 'e': {
            currentControl = elbow;
            delta = stepsize;
            elbowValue = elbowValue + delta;

            if (elbowValue >= elbowUpperLimit) {
                elbowValue = elbowUpperLimit;
            }

            currentValue = elbowValue;

            //System.out.println(currentValue);
            sendsignal = true;
            break;
        }

        case 'd': {
            currentControl = elbow;
            delta = -stepsize;
            elbowValue = elbowValue + delta;

            if (elbowValue <= elbowLowerLimit) {
                elbowValue = elbowLowerLimit;
            }

            currentValue = elbowValue;

            //System.out.println(currentValue);
            sendsignal = true;
            break;
        }

        case 'r': {
            currentControl = wrist;
            delta = stepsize;
            wristValue = wristValue + delta;

            if (wristValue >= wristUpperLimit) {
                wristValue = wristUpperLimit;
            }

            currentValue = wristValue;

            //System.out.println(currentValue);
            sendsignal = true;
            break;
        }

        case 'f': {
            currentControl = wrist;
            delta = -stepsize;
            wristValue = wristValue + delta;

            if (wristValue <= wristLowerLimit) {
                wristValue = wristLowerLimit;
            }

            currentValue = wristValue;

            //System.out.println(currentValue);
            sendsignal = true;
            break;
        }

        case 't': {
            currentControl = gripper;
            delta = stepsize;
            gripperValue = gripperValue + delta;

            if (gripperValue >= gripperUpperLimit) {
                gripperValue = gripperUpperLimit;
            }

            currentValue = gripperValue;

            //System.out.println(currentValue);
            sendsignal = true;
            break;
        }

        case 'g': {
            currentControl = gripper;
            delta = -stepsize;
            gripperValue = gripperValue + delta;

            if (gripperValue <= gripperLowerLimit) {
                gripperValue = gripperLowerLimit;
            }

            currentValue = gripperValue;

            //System.out.println(currentValue);
            sendsignal = true;
            break;
        }

        case 'y': {
            currentControl = servo6;
            delta = stepsize;
            servo6Value = servo6Value + delta;

            if (servo6Value >= servo6UpperLimit) {
                servo6Value = servo6UpperLimit;
            }

            currentValue = servo6Value;
            System.out.println("servo 6:" + currentValue);
            sendsignal = true;
            break;
        }

        case 'h': {
            currentControl = servo6;
            delta = -stepsize;
            servo6Value = servo6Value + delta;

            if (servo6Value <= servo6LowerLimit) {
                servo6Value = servo6LowerLimit;
            }

            currentValue = servo6Value;
            System.out.println("servo 6:" + currentValue);
            sendsignal = true;
            break;
        }
        }
    }

    int baseValue = 165;
    int shoulderValue = 128;
    int elbowValue = 128;
    int wristValue = 115;
    int gripperValue = 128;
    int servo6Value = 128;
    int currentValue = 128;
    int delta = 1;
    private final IntToken delimiter = new IntToken(255);
    private final IntToken base = new IntToken(0);
    private final IntToken shoulder = new IntToken(1);
    private final IntToken elbow = new IntToken(2);
    private final IntToken wrist = new IntToken(3);
    private final IntToken gripper = new IntToken(4);
    private final IntToken servo6 = new IntToken(5);
    private final IntToken nullControl = new IntToken(255);
    private IntToken currentControl = nullControl;
    private final int baseLowerLimit = 1;
    private final int baseUpperLimit = 254;
    private final int shoulderLowerLimit = 1;
    private final int shoulderUpperLimit = 229;
    private final int elbowLowerLimit = 1;
    private final int elbowUpperLimit = 231;
    private final int wristLowerLimit = 1;
    private final int wristUpperLimit = 254;
    private final int gripperLowerLimit = 40;
    private final int gripperUpperLimit = 190;
    private final int servo6LowerLimit = 40;
    private final int servo6UpperLimit = 200;
}
