package ptolemy.actor.gui.test;

import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLParser;

/** This test (from Rome AFRL) was causing a stack trace.
 */
public class RunMultipleModels {

    /** Create multiple models and execute them.
     *  @exception Exception The last exception thrown, if any
     *  by any of the threads.
     */
    public synchronized void run() throws Exception {
        Set threadSet = new HashSet();
        for (int i = 0; i < 100; i++) {
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    try {
                        String threadName = Thread.currentThread().getName();
                        URL url = RunMultipleModels.class
                                .getResource("RunMultipleModelsModel.xml");

                        Workspace workspace = new Workspace("Workspace"
                                + threadName);
                        MoMLParser parser = new MoMLParser(workspace);
                        MoMLParser.purgeModelRecord(url);
                        NamedObj model = parser.parse(null, url);

                        Manager manager = new Manager(workspace, "Manager"
                                + threadName);
                        ((CompositeActor) model).setManager(manager);
                        manager.execute();

                    } catch (Exception ex) {
                        _lastException = ex;
                        Thread thread = Thread.currentThread();
                        System.out.println("Thread: " + thread.getName()
                                + " state: " + thread.getState());
                        ex.printStackTrace();
                    }
                }
            });

            System.out.print(" " + thread.getName());
            // Keep track of which threads we have created so that
            // we can join them later
            threadSet.add(thread);
            thread.start();
        }

        // Join each thread and wait for it to die
        // "Shot a thread in reno, just to watch it die"
        Iterator threads = threadSet.iterator();
        while(threads.hasNext()) {
            Thread thread = (Thread) threads.next();
            // Timeout after 300 seconds so that the nightly build
            // does not hang.
            thread.join(300000);
        }

        System.out.println("");

        if (_lastException != null) {
            throw _lastException;
        }
    }

    /** Create multiple models and execute them by using Manager.startRun().
     *  This is an alternative implementation that uses Manager.startRun(),
     */
    public synchronized void run2() throws Exception {
        for (int i = 0; i < 100; i++) {
            URL url = RunMultipleModels.class
                    .getResource("RunMultipleModelsModel.xml");
            Workspace workspace = new Workspace("Workspace" + i);
            MoMLParser parser = new MoMLParser(workspace);
            MoMLParser.purgeModelRecord(url);
            NamedObj model = parser.parse(null, url);

            Manager manager = new Manager(workspace, "Manager" + i);
            ((CompositeActor) model).setManager(manager);
            manager.startRun();
            System.out.print(".");
        }
    }

    /** Run multiple models.
     */
    public static void main(String[] args) throws Exception {
        RunMultipleModels runMultipleModels = new RunMultipleModels();
        runMultipleModels.run();
    }

    // The last exception that was thrown.
    private Exception _lastException;
}
