/*

 Copyright (c) 1997-1999 The Regents of the University of California.
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

@ProposedRating Red (davisj@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/


package ptolemy.domains.csp.demo;

import ptolemy.kernel.util.*;
import ptolemy.domains.csp.kernel.*;
import ptolemy.domains.csp.lib.*;
import ptolemy.actor.*;
import ptolemy.data.*;

import java.awt.*;
import java.io.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

//////////////////////////////////////////////////////////////////////////
////
/**


@author John S. Davis II
@version $Id$
*/


public class BusContentionGraphic extends Frame {

    /*
      public BusContentionGraphic()
      throws IllegalActionException, NameDuplicationException {
      _demo = new BusContentionDemo(this);
      }
    */

    /**
     */
    public BusContentionGraphic( Dimension size ) {
        try {
            _demo = new BusContentionDemo(this);
            _demo.makeConnections();
            _size = size;
            setResizable(true);
            setSize( _size );
            setResizable(false);
        } catch(Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /**
     */
    public static void main(String[] args) {
        try {
	    BusContentionGraphic graphic =
                new BusContentionGraphic( new Dimension( 500, 500 ) );
            graphic.layoutGraphics();
	    graphic.setVisible(true);
            graphic.run();
        } catch(Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     */
    public void run() {
        _demo.run();
    }

    /**
     */
    public void layoutGraphics() {
	//
	// Top Panel
	//
	GridBagLayout outerGBag = new GridBagLayout();
	GridBagConstraints outerConstraints = new GridBagConstraints();
	setLayout(outerGBag);

	Panel top = new Panel();
	{
	    GridBagLayout innerGBag = new GridBagLayout();
	    GridBagConstraints innerConstraints = new GridBagConstraints();
	    top.setLayout(innerGBag);

	    Panel topLeft = new Panel();
	    innerConstraints.fill = GridBagConstraints.BOTH;
	    innerConstraints.weightx = 0.9;
	    innerConstraints.gridwidth = 4;
	    innerGBag.setConstraints(topLeft, innerConstraints);
	    top.add(topLeft);
	    {
		Label label = new Label("Bus Contention Demo");
		Font font = new Font("Helvetica", Font.BOLD, 16);
		label.setFont( font );
		label.setForeground(SystemColor.text);
		topLeft.add(label);
	    }
	    Button helpButton = new Button("Info");
	    // helpButton.addActionListener(this);
	    innerConstraints.fill = GridBagConstraints.BOTH;
	    innerConstraints.gridwidth = GridBagConstraints.REMAINDER;
	    innerConstraints.weightx = 0.1;
	    innerConstraints.gridwidth = 1;
	    innerGBag.setConstraints(helpButton, innerConstraints);
	    top.add(helpButton);
	}
	top.setVisible(true);
	outerConstraints.fill = GridBagConstraints.BOTH;
	outerConstraints.weightx = 1;
	outerConstraints.weighty = 0.1;
	outerConstraints.gridheight = 1;
	outerConstraints.gridwidth = GridBagConstraints.REMAINDER;
	outerGBag.setConstraints(top, outerConstraints);
	add(top);

	Panel bottom = new Panel();
	bottom.setVisible(true);
	bottom.setBackground( Color.white );
	outerConstraints.fill = GridBagConstraints.BOTH;
	outerConstraints.weightx = 1;
	outerConstraints.weighty = 0.9;
	outerConstraints.gridheight = 4;
	outerConstraints.gridwidth = GridBagConstraints.REMAINDER;
	outerGBag.setConstraints(bottom , outerConstraints);
	add(bottom);

	GridLayout gridLayout = new GridLayout( _rows, _columns );
	bottom.setLayout( gridLayout );
	_blocks = new DisplayBlock[_rows][_columns];

	for( int i = 0; i < _rows; i++ ) {
	    for( int j = 0; j < _columns; j++ ) {
		_blocks[i][j] = new DisplayBlock();
		_blocks[i][j].setVisible(false);
                /*
                  if( i != _rows/2 || j != _columns/2 ) {
                  _blocks[i][j].setVisible(false);
                  }
                */
	        bottom.add( _blocks[i][j] );
	    }
	}
        _blocks[4][4].setVisible(true);
    }

    /**
     */
    public void receiveEvent(CSPActor actor, int state) {
        System.out.println(actor.getName() + " is in state " + state);
        if( _blocks[3][3].isVisible() ) {
            _blocks[4][4].setVisible(true);
            _blocks[3][3].setVisible(false);
        } else if( _blocks[4][4].isVisible() ) {
            _blocks[3][3].setVisible(true);
            _blocks[4][4].setVisible(false);
        }
        try {
            Thread.sleep(800);
        } catch( InterruptedException e ) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /*
      public void resetMark(int i, int j) {
      if( _random == null ) {
      _random = new Random();
      }

      double newi = _random.nextDouble();
      while( newi == 0.0 || newi == 1.0 ) {
      newi = _random.nextDouble();
      }
      int newI = (int)(_rows * newi) + i;
      newI = newI%_rows;

      double newj = _random.nextDouble();
      while( newj == 0.0 || newj == 1.0 ) {
      newj = _random.nextDouble();
      }
      int newJ = (int)(_columns * newj) + j;
      newJ = newJ%_columns;

      _blocks[newI][newJ].setVisible(true);
      _currI = newI;
      _currJ = newJ;
      }
    */

    /*
      public void actionPerformed(ActionEvent event) {
      Point myLocation = getLocationOnScreen();
      if( _infoMessage == null ) {
      _infoMessage = new InfoMessage(this);
      }
      _infoMessage.setLocation(myLocation);
      _infoMessage.setVisible(true);
      }
    */


    ///////////////////////////////////////////////////////////////////
    ////                        private variables                  ////

    /*
      // The help message window that is popped up
      // when the help button is depressed.
      private InfoMessage _infoMessage;
    */

    // The size of the outer frame of this application
    private Dimension _size;

    // The array of secret canvases upon which the
    // secret of time wastage is determined.
    private DisplayBlock[][] _blocks;

    /*
      // The random number generator used to reset positioning
      private Random _random;
    */

    // The number of rows and columns used for positioning
    private int _rows = 6;
    private int _columns = 6;

    /*
      // The current position within the grid of rows and columns
      private int _currI;
      private int _currJ;
    */

    private BusContentionDemo _demo;
}











