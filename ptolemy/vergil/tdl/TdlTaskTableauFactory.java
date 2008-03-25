package ptolemy.vergil.tdl;

import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TableauFactory;
import ptolemy.domains.tt.tdl.kernel.TDLTask;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

public class TdlTaskTableauFactory extends TableauFactory {

    public TdlTaskTableauFactory(NamedObj container, String name) throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    public Tableau createTableau(Effigy effigy) throws Exception {
        Configuration configuration = (Configuration) effigy.toplevel();
        
        TDLTask model = (TDLTask) ((PtolemyEffigy) effigy).getModel();
        return configuration.openModel(model);
    }
    
}
