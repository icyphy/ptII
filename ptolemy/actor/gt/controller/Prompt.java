/*

 Copyright (c) 2008 The Regents of the University of California.
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
package ptolemy.actor.gt.controller;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import ptolemy.data.ArrayToken;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;

//////////////////////////////////////////////////////////////////////////
//// Prompt

/**


 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class Prompt extends TableauControllerEvent {

    /**
     *  @param container
     *  @param name
     *  @throws IllegalActionException
     *  @throws NameDuplicationException
     */
    public Prompt(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        tableau = new TableauParameter(this, "tableau");
        tableau.setPersistent(false);
        tableau.setVisibility(Settable.EXPERT);

        List<Settable> parameters = attributeList(Settable.class);
        for (Settable parameter : parameters) {
            _ignoredParameters.add(parameter.getName());
        }
    }

    public RefiringData fire(ArrayToken arguments)
            throws IllegalActionException {
        RefiringData data = super.fire(arguments);

        JFrame frame = new JFrame();

        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout());
        frame.setContentPane(contentPane);

        Configurer configurer = new Configurer();
        _executeChangeRequests(configurer);
        contentPane.add(configurer, BorderLayout.CENTER);

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancel");
        Listener listener = new Listener(frame);
        ok.addActionListener(listener);
        cancel.addActionListener(listener);
        panel.add(ok);
        panel.add(cancel);
        contentPane.add(panel, BorderLayout.SOUTH);

        synchronized (frame) {
            frame.pack();
            frame.addWindowListener(listener);
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            int x = (toolkit.getScreenSize().width - frame.getSize().width) / 2;
            int y = (toolkit.getScreenSize().height - frame.getSize().height)
                    / 2;
            frame.setLocation(x, y);
            frame.setAlwaysOnTop(true);
            frame.setVisible(true);

            try {
                workspace().wait(frame);
            } catch (InterruptedException e) {
                // Ignore.
            }
        }

        if (listener._cancelled) {
            configurer.restore();
        } else {
            _executeChangeRequests(configurer);
        }

        return data;
    }

    public TableauParameter tableau;

    public StringParameter title;

    protected TableauParameter _getDefaultTableau() {
        return tableau;
    }

    private void _executeChangeRequests(Configurer configurer) {
        List<Settable> parameters = attributeList(Settable.class);
        for (Settable parameter : parameters) {
            if (!_ignoredParameters.contains(parameter.getName()) &&
                    configurer.isVisible(parameter)) {
                ((NamedObj) parameter).executeChangeRequests();
            }
        }
    }

    private Set<String> _ignoredParameters = new HashSet<String>();

    private class Configurer extends ptolemy.actor.gui.Configurer {

        /** Construct a configurer for the specified object.  This stores
         *  the current values of any Settable attributes of the given object,
         *  and then defers to any editor pane factories contained by
         *  the given object to populate this panel with widgets that
         *  edit the attributes of the given object.  If there are no
         *  editor pane factories, then a default editor pane is created.
         */
        public Configurer() {
            super(Prompt.this);
        }

        /** Return true if the given settable should be visible in this
         *  configurer panel for the specified target. Any settable with
         *  visibility FULL or NOT_EDITABLE will be visible.  If the target
         *  contains an attribute named "_expertMode", then any
         *  attribute with visibility EXPERT will also be visible.
         *  @param settable The object whose visibility is returned.
         *  @return True if settable is FULL or NOT_EDITABLE or True
         *  if the target has an _expertMode attribute and the settable
         *  is EXPERT.  Otherwise, return false.
         */
        public boolean isVisible(Settable settable) {
            return !_ignoredParameters.contains(settable.getName()) &&
                    super.isVisible(settable);
        }
    }

    private class Listener implements ActionListener, WindowListener {

        public void actionPerformed(ActionEvent e) {
            String c = e.getActionCommand();
            if (c.equals("OK")) {
                synchronized (_frame) {
                    _cancelled = false;
                    _frame.notify();
                }
            } else if (c.equals("Cancel")) {
                synchronized (_frame) {
                    _frame.notify();
                }
            }
            _frame.dispose();
        }

        public void windowActivated(WindowEvent e) {
        }

        public void windowClosed(WindowEvent e) {
        }

        public void windowClosing(WindowEvent e) {
            synchronized (_frame) {
                _frame.notify();
            }
            _frame.dispose();
        }

        public void windowDeactivated(WindowEvent e) {
        }

        public void windowDeiconified(WindowEvent e) {
        }

        public void windowIconified(WindowEvent e) {
        }

        public void windowOpened(WindowEvent e) {
        }

        Listener(JFrame frame) {
            _frame = frame;
        }

        private boolean _cancelled = true;

        private JFrame _frame;
    }
}
