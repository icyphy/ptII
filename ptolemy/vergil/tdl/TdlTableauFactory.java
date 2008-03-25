package ptolemy.vergil.tdl;

import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TableauFactory;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.domains.tt.tdl.kernel.TDLModule;
import ptolemy.domains.tt.tdl.kernel.TDLModuleDirector;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

public class TdlTableauFactory extends TableauFactory {

    public TdlTableauFactory(NamedObj container, String name) throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    public Tableau createTableau(Effigy effigy) throws Exception {
        Configuration configuration = (Configuration) effigy.toplevel();
        
        TDLModule model = (TDLModule) ((PtolemyEffigy) effigy).getModel();
        FSMActor controller = ((TDLModuleDirector) model.getDirector()).getController();
        return configuration.openModel(controller);
    }
    
}
