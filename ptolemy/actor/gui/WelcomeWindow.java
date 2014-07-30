/* A viewer for Welcome Windows

 Copyright (c) 2006-2014 The Regents of the University of California.
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
package ptolemy.actor.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.util.MessageHandler;

///////////////////////////////////////////////////////////////////
//// WelcomeWindow

/**
 A toplevel frame that can view HTML documents, but has no menus.

 <p>This window uses the _showWelcomeWindowAtStartup
 SingletonParameter preference in
 {@link ptolemy.actor.gui.PtolemyPreferences} to determine whether the
 window is shown or not.  If this parameter is not present or is true,
 then the window is show, if the parameter is false, then it is not shown.
 This parameter is set in ~/.ptolemyII/PtolemyPreferences.xml, which under
 Windows might be in
 <code>c:/Documents and Settings/<i>yourlogin</i>/.ptolemyII/PtolemyPreferences.xml</code>
 <p>The easiest way to adjust the configuration so that this window
 is used is to edit the <code>welcomeWindow.xml</code> file that corresponds
 with the configuration and set the tableau to be
 <code>ptolemy.actor.gui.WelcomeWindowTableau</code>
 For example, <code>$PTII/ptolemy/configs/full/welcomeWindow.xml</code>
 might look like:
 <pre>
 &lt;?xml version="1.0" standalone="no"?&gt;
 &lt;!DOCTYPE plot PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
 "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd"&gt;
 &lt;entity name="directory"&gt;
 &lt;entity name="doc" class="ptolemy.actor.gui.HTMLEffigy"&gt;
 &lt;entity name="tableau" class="ptolemy.actor.gui.WelcomeWindowTableau"&gt;
 &lt;!-- If you adjust the size, be sure to try it under Java 1.5 --&gt;
 &lt;property name="size" value="[600, 220]"/&gt;
 &lt;property name="url" value="ptolemy/configs/full/intro.htm"/&gt;
 &lt;/entity&gt;
 &lt;/entity&gt;
 &lt;/entity&gt;
 </pre>
 The above configuration will create a WelcomeWindow with a 600x200
 size that displays the contents of
 <code>ptolemy/configs/full/intro.htm</code>

 <p>Note that since this widow has no menus, if you use this window, then
 you will want to set the
 <code>_applicationBlankPtolemyEffigyAtStartup</code> parameter in the
 configuration so that a blank Graph Editor window pops up along with
 this welcome window.  For example:
 <pre>
 &lt;property name="_applicationBlankPtolemyEffigyAtStartup"
 class="ptolemy.data.expr.Parameter"
 value="true"/&gt;
 </pre>


 @author Christopher Brooks, Nandita Mangal
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
@SuppressWarnings("serial")
public class WelcomeWindow extends HTMLViewer {
    /** Construct a blank HTML Viewer with no menu items.
     */
    public WelcomeWindow() {
        super();
        //setUndecorated(true);
        setResizable(false);
        _statusBar = null;

        // Add the close panel
        _closePanel = new JPanel();
        _closePanel.setBackground(new Color(227, 231, 236));
        _closePanel.setSize(401, 100);
        _closePanel.setLayout(new BorderLayout());

        _startupCheck = new JCheckBox(
                "<html><table cellpadding=0><tr><td width=9/><td><font size=3>Show this dialog upon startup </font></td></tr></table></html>",
                true);
        _startupCheck.setBackground(new Color(227, 231, 236));

        _closeButton = new JButton("Close");
        _closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                setVisible(false);
                close();
            }
        });

        _closePanel.add(_startupCheck, BorderLayout.WEST);
        _closePanel.add(_closeButton, BorderLayout.EAST);

        super.hideMenuBar();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Make this window displayable.  The menubar is hidden and
     *  close panel is added.
     */
    @Override
    public void pack() {
        hideMenuBar();
        getContentPane().add(_closePanel, BorderLayout.SOUTH);
        super.pack();

    }

    /** Always set the title to the string "Welcome".
     *  @param title The title, which is ignored.  The title is always
     *  the value returned by {@link #_getName()}.
     */
    @Override
    public void setTitle(String title) {
        super.setTitle(_getName());
    }

    /** Show the window if the _showWelcomeWindowAtStartup parameter
     *  is not set or is true.
     */
    @Override
    public void show() {

        Configuration configuration = getConfiguration();

        _showWelcomeWindowAtStartup = (BooleanToken) PtolemyPreferences
                .preferenceValue(configuration, "_showWelcomeWindowAtStartup");

        if (_showWelcomeWindowAtStartup != null
                && !_showWelcomeWindowAtStartup.booleanValue()) {
            // Call super.close() because _close() expects that the window has been
            // rendered via successful completion of show() and that we can query the
            // checkbox.  There was a bug where if user started Kepler, unchecked
            // "Show this Window at Startup" and then restarted Kepler, Kepler would
            // not exit.
            super._close();
            return;
        }
        super.show();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Read the "Show this dialog on startup" checkbox, update the
     *  preferences if necessary and then call the super class and close
     *  this window.  This method is called by {@link ptolemy.gui.Top#close()}.
     */
    @Override
    protected boolean _close() {
        Configuration configuration = getConfiguration();
        if (_showWelcomeWindowAtStartup == null
                && !_startupCheck.isSelected()
                || _showWelcomeWindowAtStartup != null
                && _showWelcomeWindowAtStartup.booleanValue() != _startupCheck
                .isSelected()) {
            // Update the preferences if there is no preference and
            // the user unchecked the "Show this dialog on startup"
            // or if the value of the preference and the checkbox differ.
            try {
                PtolemyPreferences preferences = PtolemyPreferences
                        .getPtolemyPreferencesWithinConfiguration(configuration);
                // FIXME: is ok to create a new parameter each time?
                SingletonParameter showWelcomeWindowAtStartupParameter = new SingletonParameter(
                        preferences, "_showWelcomeWindowAtStartup");

                // FIXME: is there a better way to set a BooleanToken?
                showWelcomeWindowAtStartupParameter.setToken(_startupCheck
                        .isSelected() ? BooleanToken.TRUE : BooleanToken.FALSE);
                preferences.save();
            } catch (Exception ex) {
                MessageHandler.error("Failed to update preferences and"
                        + "save _showWelcomeWindowAtStarupPreferences", ex);
            }
        }
        return super._close();
    }

    /** Get the name of this object, which in this case is the
     *  string "Welcome".
     *  @return The string "Welcome";
     */
    @Override
    protected String _getName() {
        return "Welcome";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The panel at the bottom that contains the
     *  "Show this dialog on startup" checkbox and the close button.
     */
    private JPanel _closePanel;

    /** True if the welcome window is shown at startup. */
    private BooleanToken _showWelcomeWindowAtStartup;

    private JCheckBox _startupCheck;

    private JButton _closeButton;
}
