package ptolemy.domains.dt.kernel.test;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.actor.*;
import ptolemy.actor.gui.Placeable;
import ptolemy.actor.lib.gui.*;
import ptolemy.actor.lib.SequenceActor;
import ptolemy.actor.lib.Sink;

import java.awt.Container;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.JTextArea;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JScrollBar;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.BadLocationException;


public class TimedDisplay extends Display implements Placeable, SequenceActor {

    public TimedDisplay(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        rowsDisplayed.setToken(new IntToken(20));
    }

    
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        TimedDisplay newobj = (TimedDisplay)super.clone(workspace);
        return newobj;
    }
    
 
    public void place(Container container) {
        super.place(container);
        _scrollPane.setPreferredSize(new Dimension(400,200));
        
    }

    public boolean postfire() throws IllegalActionException {
        int width = input.getWidth();
        for (int i = 0; i < width; i++) {
            if (input.hasToken(i)) {
                Token token = input.get(i);
                Director director = getDirector();
                String value = " ";
                
                if (director != null) {
                    value = ""+director.getCurrentTime();
                }
                
                //String value = (director.getCurrentTime()).toString();
                textArea.append(value);

                // Append a tab character.
                if (width > i + 1) textArea.append("\t");

                try {
                    int lineOffset = textArea
                        .getLineStartOffset(textArea.getLineCount() - 1);
                    textArea.setCaretPosition(lineOffset);
                } catch (BadLocationException ex) {
                    // Ignore ... worst case is that the scrollbar
                    // doesn't move.
                }
            }
        }
        textArea.append("\n");
        return true;
    }

}
