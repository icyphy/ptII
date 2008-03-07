// $Id$
// Example code from Paul Allen (Cornell)
public class MoMLRunner {

    private String id = "unknown";
    private Manager _manager = null;
    private String workflowLocation = "unknown";

    public MoMLRunner() throws Exception {
    }

    public MoMLRunner(String jobID, String xmlFileName) throws Throwable {
        id = jobID;
        workflowLocation = xmlFileName;

        MoMLParser parser = new MoMLParser();

        MoMLParser.setMoMLFilters(BackwardCompatibility.allFilters());
        MoMLParser.addMoMLFilter(new RemoveGraphicalClasses());

        CompositeActor toplevel = (CompositeActor) parser.parse(null,
                new File(workflowLocation).toURI().toURL());
        toplevel.setName(id);

        _manager = new Manager(toplevel.workspace(), "MoMLRunner");
        toplevel.setManager(_manager);
    }

    public void execute() throws Throwable {
        _manager.execute();
    }
}
