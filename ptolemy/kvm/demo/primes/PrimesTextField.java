/* Sieve of Eratosthenese for PalmOS

 Copyright (c) 1999-2000 The Regents of the University of California.
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
@ProposedRating Red (yourname@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)
*/

package ptolemy.kvm.demo.primes;

import com.sun.kjava.*;


//////////////////////////////////////////////////////////////////////////
//// PrimesTextField
/**
Generate Prime Numbers on the Palm using a Spotlet and a TextField
@author Christopher Hylands, Tom Parks
@version $Id$
*/
public class PrimesTextField extends Spotlet {
    public PrimesTextField() {
        g = Graphics.getGraphics();
        textField = new TextField("Number of Primes", 5, 20, 100, 20);
        textField.setFocus();
        textBox = new ScrollTextBox("PrimesTextField, PalmOS",5,40,150,80);

        goButton = new Button("Go",5,145);
        exitButton = new Button("Exit",115,145);

        paint();

        textField.setText("10");
    }

    public void keyDown(int key) {
        if(textField.hasFocus()) {
            textField.handleKeyDown(key);
        }
    }

    public static void main(String args[]) {
        primesTextField = new PrimesTextField();
	Graphics.getGraphics().clearScreen();
	//textBox.paint();
        primesTextField.register(NO_EVENT_OPTIONS);
        primesTextField.paint();
    }

    void paint() {
        // display the frames
        textBox.paint();
        goButton.paint();
        exitButton.paint();
        textField.paint();
    }

    public void penDown(int x, int y) {
        if(textField.pressed(x,y) && (!textField.hasFocus())) {
            textField.setFocus();
        }
        if(goButton.pressed(x, y)) {
            Channel c1 = new Channel(1);
            Channel c2 = new Channel(1);

            new Thread(new Ramp(c1, 2)).start();
            new Thread(new Sift(c1, c2)).start();

            int limit = Integer.valueOf(textField.getText()).intValue();

            String outText =  new String();

            outText = "First " + limit + " Primes\n";
            for (int i = 0; i < limit; i++)	{
                outText += c2.get().toString() + " ";
                textBox.setText(outText);
                primesTextField.paint();
            }

        }
        if(exitButton.pressed(x, y)) {
            System.exit(0);
        }
    }

    public Graphics g;
    public Button exitButton;
    public Button goButton;
    public static PrimesTextField primesTextField;
    public static ScrollTextBox textBox;
    public TextField textField;
    //protected ThreadGroup group;
    protected Thread thread;

    protected final int MAX = 40;	// Maximum number of allowed primes.

}
