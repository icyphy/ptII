package ptolemy.actor.lib.gui;

import ptolemy.actor.gui.PortablePlaceable;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.plot.PlotBoxInterface;

public interface PlotterBaseInterface extends PortablePlaceable {

    public void init(PlotterBase plotterBase);

    public void initWindowAndSizeProperties() throws IllegalActionException,
            NameDuplicationException;

    public void exportWindowAndSizeMoML();

    public void setTableauTitle(String title);

    public void setFrame(Object frame);

    public void cleanUp();

    public void remove();

    public void removeOldContainer();

    public Object getTableau();

    public Object getFrame();

    public void setPlatformContainer(Object container);

    public Object getPlatformContainer();

    public void updateSize();

    public void bringToFront();

    public void initializeEffigy() throws IllegalActionException;

    public PlotBoxInterface newPlot();
}
