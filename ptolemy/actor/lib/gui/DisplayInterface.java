package ptolemy.actor.lib.gui;

import ptolemy.actor.gui.PortableContainer;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public interface DisplayInterface {

    public void init(Display display) throws IllegalActionException,
            NameDuplicationException;

    public void setRows(int numRows) throws IllegalActionException;

    public void setColumns(int numColumns) throws IllegalActionException;

    public Object getTextArea();

    public void place(PortableContainer container);

    public void display(String tokenValue);

    public void cleanUp();

    public void setTitle(String stringValue) throws IllegalActionException;

    public void openWindow() throws IllegalActionException;

    public void remove();

}
