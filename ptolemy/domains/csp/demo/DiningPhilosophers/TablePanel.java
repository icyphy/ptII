/* A panel that graphical represents the Dining Philosophers

 Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

/**
 * A panel that graphically represents the Dining Philosophers.
 * This contains all the objects and controls the whole thing.
 *
 * @author Neil Smyth, modified from a file by John Hall
 * @version $Id$
 */

package ptolemy.domains.csp.demo.DiningPhilosophers;

import java.awt.*;
import java.util.Random;
import ptolemy.domains.csp.demo.DiningPhilosophers.Philosopher;

class TablePanel extends Panel {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Paint the panel. The state of all the chopsticks and philosophers
     * must be checked.
     */
    public void paint(Graphics g) {
        Coordinate newOrigin;

        synchronized (this) {

            Dimension d = this.size();
            newOrigin = new Coordinate (d.width / 2, d.height / 2);

            /*
             * Test to see if the panel has been resized. If so then all
             * coordinates must be recalculated.
             */
            if (! newOrigin.equals(origin)) {
                origin = newOrigin;
                initPos();
            }

            // draw the table.
            g.drawOval(origin.X - tableR,
                    origin.Y - tableR, tableR * 2, tableR * 2);

            for (int i = 0; i < 5; i++) {
				// draw each philosopher.
                if (_philosophers[i].gotLeft && _philosophers[i].gotRight) {

                    g.fillOval(philsLoc[i].pos.X,
                            philsLoc[i].pos.Y, 2 * pR, 2 * pR);
                } else {
                    g.drawOval(philsLoc[i].pos.X,
                            philsLoc[i].pos.Y, 2 * pR, 2 * pR);
                }

                int j = (i - 1 + 5) % 5;
                if (! (_philosophers[i].gotLeft ||
                        _philosophers[j].gotRight)) {
                    // chopstick is on the table.
                    g.drawLine(chopsticksLoc[i].pos[0].X,
                            chopsticksLoc[i].pos[0].Y,
                            chopsticksLoc[i].pos[1].X,
                            chopsticksLoc[i].pos[1].Y);
                }
                else {
                    if (_philosophers[i].gotLeft) {
                        // the philosopher on the right has it.
                        g.drawLine(philsLoc[i].leftPos[0].X,
                                philsLoc[i].leftPos[0].Y,
                                philsLoc[i].leftPos[1].X,
                                philsLoc[i].leftPos[1].Y);
                        if (_philosophers[j].waitingRight) {
                            // the philosopher on the left is waiting for it.
                            g.fillOval(philsLoc[j].rightPos[1].X - 2,
                                    philsLoc[j].rightPos[1].Y - 2, 4, 4);
                        }
                    }
                    else {
                        // the philosopher on the left has it.
                        g.drawLine(philsLoc[j].rightPos[0].X,
                                philsLoc[j].rightPos[0].Y,
                                philsLoc[j].rightPos[1].X,
                                philsLoc[j].rightPos[1].Y);
                        if (_philosophers[i].waitingLeft) {
                            // the philosopher on the right is waiting for it.
                            g.fillOval(philsLoc[i].leftPos[1].X - 2,
                                    philsLoc[i].leftPos[1].Y - 2, 4, 4);
                        }
                    }
                }
            }
        }
    }


    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Constructs the new panel. Initializes the objects and the display.
     */
    protected TablePanel(Philosopher[] philosophers) {
        _initialize(philosophers);
    }

    /**
     * Initializes the objects and the display.
     */
    protected void _initialize(Philosopher[] philosophers) {
        _philosophers = philosophers;
        for (int i = 0; i < 5; i++) {
            philsLoc[i] = new PhilosopherCoords();
            chopsticksLoc[i] = new ChopstickCoords();
        }
        initPos();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Coordinates of chopsticks
    private ChopstickCoords[] chopsticksLoc = new ChopstickCoords[5];

    // Coordinates of philosophers
    private PhilosopherCoords[] philsLoc = new PhilosopherCoords[5];
    private Philosopher[] _philosophers;

    /*
     * Constants used to draw the table and philosophers.
     * They allow geometry changes to be made easily.
     */
    // The origin coordinate.
    private Coordinate origin = new Coordinate(100, 100);
    // Radius of the philosophers
    private final int pR = 10;
    // Polar position of the philosophers
    private final double r1 = 70d;
    // Polar position of the chopsticks
    private final double r2 = 25d;
    // Polar position of the chopsticks from the philosophers when in use
    private final double r3 = 15d;
    // Length of the chopsticks
    private final double l = 10d;
    private final int tableGap = 30;
    private final int tableR = (int)r1 - tableGap;

    /**
     * Sets all the coordinates of the chopsticks and philosophers.
     * Lots of simple but tedious trig. :(
     * 4 philosophers would have been so much easier.
     */
    private void initPos() {
        // frequently used sin and cos values.
        double cos18 = Math.cos(Math.PI * 18d / 180d);
        double sin18 = Math.sin(Math.PI * 18d / 180d);
        double cos36 = Math.cos(Math.PI * 36d / 180d);
        double sin36 = Math.sin(Math.PI * 36d / 180d);

        Coordinate p, q, l_1, l_2, r_1, r_2;

        p = new Coordinate(origin.X - pR, origin.Y - (int) r1 - pR);
        l_1 = new Coordinate(p.X + pR + (int) (r3 * sin18),
                p.Y + pR + (int) (r3 * cos18));
        l_2 = new Coordinate(p.X + pR + (int) ((r3 + l) * sin18),
                p.Y + pR + (int) ((r3 + l) * cos18));
        r_1 = new Coordinate(p.X + pR - (int) (r3 * sin18),
                p.Y + pR + (int) (r3 * cos18));
        r_2 = new Coordinate(p.X + pR - (int) ((r3 + l) * sin18),
                p.Y + pR + (int) ((r3 + l) * cos18));
        philsLoc[0].setPos(p,  l_1,  l_2, r_1, r_2);
        p = new Coordinate(origin.X - (int) (r1 * cos18) - pR,
                origin.Y - (int) (r1 * sin18) - pR);
        l_1 = new Coordinate(p.X + pR + (int) r3, p.Y + pR);
        l_2 = new Coordinate(p.X + pR + (int) (r3 + l), p.Y + pR);
        r_1 = new Coordinate(p.X + pR + (int) (r3 * cos36),
                p.Y + pR + (int) (r3 * sin36));
        r_2 = new Coordinate(p.X + pR + (int) ((r3 + l) * cos36),
                p.Y + pR + (int) ((r3 + l) * sin36));
        philsLoc[1].setPos(p, l_1, l_2, r_1, r_2);
        p = new Coordinate(origin.X - (int) (r1 * sin36) - pR,
                origin.Y + (int) (r1 * cos36) - pR);
        l_1 = new Coordinate(p.X + pR + (int) (r3 * sin18),
                p.Y + pR - (int) (r3 * cos18));
        l_2 = new Coordinate(p.X + pR + (int) ((r3 + l) * sin18),
                p.Y + pR - (int) ((r3 + l) * cos18));
        r_1 = new Coordinate(p.X + pR + (int) (r3 * cos36),
                p.Y + pR - (int) (r3 * sin36));
        r_2 = new Coordinate(p.X + pR + (int) ((r3 + l) * cos36),
                p.Y + pR - (int) ((r3 + l) * sin36));
        philsLoc[2].setPos(p, l_1, l_2, r_1, r_2);
        p = new Coordinate(origin.X + (int) (r1 * sin36) - pR,
                origin.Y + (int) (r1 * cos36) - pR);
        l_1 = new Coordinate(p.X + pR - (int) (r3 * cos36),
                p.Y + pR - (int) (r3 * sin36));
        l_2 = new Coordinate(p.X + pR - (int) ((r3 + l) * cos36),
                p.Y + pR - (int) ((r3 + l) * sin36));
        r_1 = new Coordinate(p.X + pR - (int) (r3 * sin18),
                p.Y + pR - (int) (r3 * cos18));
        r_2 = new Coordinate(p.X + pR - (int) ((r3 + l) * sin18),
                p.Y + pR - (int) ((r3 + l) * cos18));
        philsLoc[3].setPos(p, l_1, l_2, r_1, r_2);
        p = new Coordinate(origin.X + (int) (r1 * cos18) - pR,
                origin.Y - (int) (r1 * sin18) - pR);
        l_1 = new Coordinate(p.X + pR - (int) (r3 * cos36),
                p.Y + pR + (int) (r3 * sin36));
        l_2 = new Coordinate(p.X + pR - (int) ((r3 + l) * cos36),
                p.Y + pR + (int) ((r3 + l) * sin36));
        r_1 = new Coordinate(p.X + pR - (int) r3, p.Y + pR);
        r_2 = new Coordinate(p.X + pR - (int) (r3 + l), p.Y + pR);
        philsLoc[4].setPos(p, l_1, l_2, r_1, r_2);

        p = new Coordinate(origin.X + (int) (r2 * sin36),
                origin.Y - (int) (r2 * cos36));
        q = new Coordinate(origin.X + (int) ((r2 + l) * sin36),
                origin.Y - (int) ((r2 + l) * cos36));
        chopsticksLoc[0].setPos(p, q);
        p = new Coordinate(origin.X - (int) (r2 * sin36),
                origin.Y - (int) (r2 * cos36));
        q = new Coordinate(origin.X - (int) ((r2 + l) * sin36),
                origin.Y - (int) ((r2 + l) * cos36));
        chopsticksLoc[1].setPos(p, q);
        p = new Coordinate(origin.X - (int) (r2 * cos18),
                origin.Y + (int) (r2 * sin18));
        q = new Coordinate(origin.X - (int) ((r2 + l) * cos18),
                origin.Y + (int) ((r2 + l) * sin18));
        chopsticksLoc[2].setPos(p, q);
        p = new Coordinate(origin.X, origin.Y + (int) r2);
        q = new Coordinate(origin.X, origin.Y + (int) (r2 + l));
        chopsticksLoc[3].setPos(p, q);
        p = new Coordinate(origin.X + (int) (r2 * cos18),
                origin.Y + (int) (r2 * sin18));
        q = new Coordinate(origin.X + (int) ((r2 + l) * cos18),
                origin.Y + (int) ((r2 + l) * sin18));
        chopsticksLoc[4].setPos(p, q);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    // Inner Classes implementing the coordinates

    public class ChopstickCoords {
        Coordinate[] pos = new Coordinate[2];

        public void setPos(Coordinate p, Coordinate q) {
            pos[0] = p;
            pos[1] = q;
        }
    }

    public class PhilosopherCoords {
        Coordinate[] leftPos = new Coordinate[2];
        Coordinate[] rightPos = new Coordinate[2];
        Coordinate pos;

        public void setPos(Coordinate p, Coordinate l_1, Coordinate l_2,
                Coordinate r_1, Coordinate r_2) {
            pos = p;
            leftPos[0] = l_1;
            leftPos[1] = l_2;
            rightPos[0] = r_1;
            rightPos[1] = r_2;
        }
    }
}
