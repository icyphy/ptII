/*
@Copyright (c) 1998-1999 The Regents of the University of California.
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
*/
package ptolemy.domains.pn.demo.TicTacToe;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.media.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;

import java.io.*;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

//////////////////////////////////////////////////////////////////////////
//// TicTacToeDisplay
/**
@author Mudit Goel
@version $Id$
*/

public final class TicTacToeDisplay extends AtomicActor {
    public TicTacToeDisplay(CompositeActor container, String name) 
            throws IllegalActionException, NameDuplicationException {
        super(container,name);
        input = new IOPort(this, "input", true, false);
        output = new IOPort(this, "output", false, true);
	//new Parameter(this, "FrameName", new StringToken("ImageDisplay"));
    }

    /** Initialize the actor */
    public void initialize() throws IllegalActionException {
        super.initialize();

	_notdone = true;
	_nomove = true;
	_playermove = false;
	_moves = new int[3][3];
	if(_panel == null) {
            _frame = new _PictureFrame("Enjoy Mudit's TicTacToe");
	    _frame.addWindowListener(new _PictureFrameListener(_frame));    
            _panel = _frame.getPanel();
	    _frame.setSize(300, 300);
        } else {
            _frame = null;
        }
	_panel.setLayout(new GridLayout(3, 3, 5, 5));
	//Create the 9 subpanels
	for (int i = 0; i < 3; i++ ) {
	    for (int j = 0; j < 3; j++) {
		Panel pan = new Panel(new CardLayout());
		pan.setSize(300, 300);
		Button can = new Button("");
		can.setSize(300, 300);
		can.addActionListener(new TTTListener(pan, i, j));
		//can.setSize(100, 100);
		pan.add(can, "Empty");
		can = new Button("X");
		can.setSize(300, 300);
		//can.setSize(100, 100);
		pan.add(can, "Crosses");
		can = new Button("O");
		can.setSize(300, 300);
		//can.setSize(100, 100);
		pan.add(can, "Nots");
		can = new Button("");
		can.setSize(300, 300);
		pan.add(can, "Done");
		pan.validate();
		//pan.addMouseListener(new TTTListener(pan, i, j));
		//pan.setLayout(new OverlayLayout(pan));
		_panel.add(pan);
	    }
	}
	_panel.validate();
	if (_frame != null) _frame.validate();
    }

    public void fire() throws IllegalActionException {
	//Now do all the computation specific to this actor
	//Assuming first move by user
	if (_playermove) { //player's move
	    synchronized(this) {
		try {
		    while (_nomove) {
			wait();
		    }
		} catch (InterruptedException e) {
		    throw new IllegalActionException(this, 
			    "InterruptedException e: " + e.toString());
		}
	    }
	    _playermove = false;
	    _nomove = true;
	} else { // comp's move
	    output.broadcast(new IntMatrixToken(_moves));
	    int row = ((IntToken)input.get(0)).intValue();
	    int col = ((IntToken)input.get(0)).intValue();
	    moveMade(row, col, NOTS);
	    _playermove = true;
	    _nomove = true;
	}
    }
    
    public synchronized void moveMade(int row, int col, int val) {
	Panel pan = (Panel)_panel.getComponent(row*3 + col);
	CardLayout layout = (CardLayout)pan.getLayout();
	if (val == CROSSES) {
	    layout.show(pan, "Crosses");
	} else {
	    layout.show(pan, "Nots");
	}
	_nomove = false;
	_moves[row][col] = val;
	notifyAll();
    }

    public boolean postfire() {
	if (_isGameOver()) {
	    for (int i = 0; i < 3; i++) {
		for (int j = 0; j < 3; j++) {
		    if (_moves[i][j] == 0) { //Not clicked!!
			//Disable panel with MouseListeners
			Panel pan = (Panel)_panel.getComponent(i*3 + j);
			CardLayout layout = (CardLayout)pan.getLayout();
			layout.show(pan, "Done");
		    }
		}
	    }
	    return false; //Game over
	} else {
	    return true;
	}
    }
    
    public void setPanel(Panel panel) {
        _panel = panel;
    }
    
    public final static int  CROSSES = 1;
    public final static int NOTS = -1;
    public IOPort input;
    public IOPort output;

    /** Determine whether the game is over. 
     *  @return true if game is over
     */
    private boolean _isGameOver() {
	for (int i = 0; i < 3; i++ ) {
	    if ((Math.abs(_moves[i][0] + _moves[i][1] + _moves[i][2]) == 3) ||
		    (Math.abs(_moves[0][i] + _moves[1][i] + _moves[2][i]) 
			    == 3)) {
		_notdone = false;
	    }
	}
	if (!_notdone) return !_notdone;
	//Diagonal entries
	else if ((Math.abs(_moves[0][0] + _moves[1][1] + _moves[2][2]) == 3) ||
		Math.abs(_moves[2][0] + _moves[1][1] + _moves[0][2]) == 3) {
	    _notdone = false;
	    return !_notdone;
	} //Draw
	else {
	    int total = 0;
	    for (int i = 0; i < 3; i++) {
		for (int j = 0; j < 3; j++) {
		    total += Math.abs(_moves[i][j]);
		}
	    }
	    if (total == 9) {
		_notdone = false;
	    }
	}
	return !_notdone;
    }


    //private Picture _panel;
    private boolean _notdone;
    private boolean _nomove;
    private boolean _playermove;
    private int[][] _moves;
    private Picture _picture;
    private _PictureFrame _frame;
    private Panel _panel;
    private String _framename;


    private class _PictureFrame extends Frame {
        public _PictureFrame(String title) {
            super(title);
            this.setLayout(new BorderLayout(15, 15));
            this.show();
            _panel = new Panel();
            this.add("Center", _panel);
            this.pack();
            this.validate();
        }
        public Panel getPanel() {
            return _panel;
        }
        private Panel _panel;
    }

    private class _PictureFrameListener implements WindowListener {

	public _PictureFrameListener(Window window) {
	    _window = window;
	}

	public void windowActivated(WindowEvent e) {}
	public void windowClosed(WindowEvent e) {}

	public void windowClosing(WindowEvent e) {
	    if (e.getID() == WindowEvent.WINDOW_CLOSING) {
		_window.dispose();
	    }
	}
	
	public void windowDeactivated(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowOpened(WindowEvent e) {}
	private Window _window;
    }

    private class TTTListener implements ActionListener {
	public TTTListener(Panel panel, int i, int j) {
	    _pan = panel;
	    _row = i;
	    _col = j;
	}

	public void actionPerformed(ActionEvent event) {
	    TicTacToeDisplay.this.moveMade(
		    _row, _col, TicTacToeDisplay.CROSSES);
	}
	private Panel _pan;
	private int _row;
	private int _col;
    }

}






