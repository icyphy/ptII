/*
 Copyright (c) 1998-2001 The Regents of the University of California
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
 PROVIDED HEREUNDER IS ON AN  BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY
 */
package diva.util.jester;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import diva.sketch.JSketch;

/**
 * EventParser parses an XML file representing a stream of
 * AWT input events into an array of InputEvent objects.
 *
 * @see EventParser
 * @author Michael Shilman      (michaels@eecs.berkeley.edu)
 * @version $Id$
 * @rating Red
 */
public class EventWriter implements diva.util.ModelWriter {
    /**
     * Write the given stream of input events out to the
     * given writer.  Throw a ClasCastException if the
     * parameter is not of type InputEvent[].
     */
    public void writeModel(Object model, Writer writer)
            throws IOException {
        writeEvents((InputEvent[])model, writer);
    }

    /**
     * Write the given stream of input events out to the
     * given writer.
     */
    public void writeEvents(InputEvent[] events, Writer writer)
            throws IOException {
        writeHeader(writer);
        writer.write("<" + EventParser.EVENT_STREAM_TAG + ">\n");
        for (int i = 0; i < events.length; i++) {
            writeEvent(events[i], writer);
        }
        writer.write("</"+ EventParser.EVENT_STREAM_TAG + ">\n");
        writer.flush();
    }

    /**
     * Write the stroke information (x, y, timestamp) and its
     * label (indicating either positive or negative example) to the
     * character-output stream.
     */
    private void writeEvent(InputEvent event, Writer writer) throws IOException {
        if (event instanceof MouseEvent) {
            MouseEvent mouse = (MouseEvent)event;
            writer.write("<" + EventParser.MOUSE_EVENT_TAG + " " +
                    EventParser.ID_ATTR_TAG + "=\"" + mouse.getID() + "\" " +
                    EventParser.WHEN_ATTR_TAG + "=\"" + mouse.getWhen() + "\" " +
                    EventParser.MODIFIERS_ATTR_TAG + "=\"" + mouse.getModifiers() + "\" " +
                    EventParser.X_ATTR_TAG + "=\"" + mouse.getX() + "\" " +
                    EventParser.Y_ATTR_TAG + "=\"" + mouse.getY() + "\" " +
                    EventParser.CLICKCOUNT_ATTR_TAG + "=\"" + mouse.getClickCount() + "\" " +
                    EventParser.POPUPTRIGGER_ATTR_TAG + "=\"" + mouse.isPopupTrigger() + "\"/>\n");
        }
        else if (event instanceof KeyEvent) {
            KeyEvent key = (KeyEvent)event;
            writer.write("<" + EventParser.KEY_EVENT_TAG + " " +
                    EventParser.ID_ATTR_TAG + "=\"" + key.getID() + "\" " +
                    EventParser.WHEN_ATTR_TAG + "=\"" + key.getWhen() + "\" " +
                    EventParser.MODIFIERS_ATTR_TAG + "=\"" + key.getModifiers() + "\" " +
                    EventParser.KEYCODE_ATTR_TAG + "=\"" + key.getKeyCode() + "\" " +
                    EventParser.KEYCHAR_ATTR_TAG + "=\"" + key.getKeyChar() + "\"/>\n");
        }
        else {
            throw new IllegalArgumentException("Unexpected event type: " + event);
        }
    }

    /**
     * Write header information to the character-output stream.
     */
    private void writeHeader(Writer writer) throws IOException {
        writer.write("<?xml version=\"1.0\" standalone=\"no\"?>\n");
        writer.write("<!DOCTYPE " + EventParser.EVENT_STREAM_TAG + " PUBLIC \""
                + EventParser.PUBLIC_ID + "\"\n\t\""
                + EventParser.DTD_URL + "\">\n\n");
    }

    /**
     * Simple test of this class.
     */
    public static void main (String args[]) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: java EventWriter <outputFile>");
            System.exit(-1);
        }
        final String outputFile = args[0];
        final EventWriter demo = new EventWriter();
        final JFrame frame = new JFrame();
        final JSketch sketch = new JSketch();
        final EventRecorder recorder = new EventRecorder(sketch);
        final JButton record = new JButton("Record");
        record.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    recorder.record();
                }
            });
        JButton stop = new JButton("Stop");
        stop.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    InputEvent[] stream = recorder.stop();
                    try {
                        demo.writeModel(stream, new FileWriter(outputFile));
                    }
                    catch (Exception ex) {
                        System.err.println(e.toString());
                    }
                }
            });
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(1,2));
        panel.add(record);
        panel.add(stop);
        frame.getContentPane().add("North", panel);
        frame.getContentPane().add("Center", sketch);
        frame.setSize(600,400);
        frame.setVisible(true);
    }
}

