/**
 *
 */
package doc.tutorial.graph.junit;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ptolemy.actor.AtomicActor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.Entity;
import doc.tutorial.graph.ShortestPathFinder;

/** JUnit test class for ShortestPathFinder.
 *  @author Edward A. Lee
 */
public class ShortestPathFinderTest {

    /** Model is created in the setUp() method and stored here. */
    public CompositeActor testModel;

    /** This method creates a Ptolemy II model using the
     *  Java API. It is invoked prior to each test.
     *  @exception java.lang.Exception If creating the model fails.
     */
    @Before
    public void setUp() throws Exception {
        // Build test model.
        testModel = new CompositeActor();

        // Actor a
        AtomicActor a = new AtomicActor(testModel, "a");
        IOPort aOut = new IOPort(a, "aOut", false, true);

        // Actor b
        AtomicActor b = new AtomicActor(testModel, "b");
        IOPort bIn = new IOPort(b, "bIn", true, false);
        IOPort bOut = new IOPort(b, "bOut", false, true);

        // Actor c
        AtomicActor c = new AtomicActor(testModel, "c");
        IOPort cIn1 = new IOPort(c, "cIn1", true, false);
        IOPort cIn2 = new IOPort(c, "cIn2", true, false);

        // Connections
        ComponentRelation relation = testModel.connect(aOut, bIn);
        testModel.connect(bOut, cIn1);
        cIn2.link(relation);
    }

    /**
     * Test method for {@link doc.tutorial.graph.ShortestPathFinder#calculateDistance(java.lang.Object[], int, int)}.
     */
    @Test
    public void testCalculateDistance() {
        // Get a list of entities in the model.
        List<Entity> entitiesList = testModel.entityList();
        // Create an array containing the entities.
        // The entities will appear in the order they are created in setUp().
        Object[] entities = entitiesList.toArray();
        int result = ShortestPathFinder.calculateDistance(entities, 0, 1);
        Assert.assertEquals(1, result);
        result = ShortestPathFinder.calculateDistance(entities, 0, 2);
        Assert.assertEquals(1, result);
        result = ShortestPathFinder.calculateDistance(entities, 2, 0);
        Assert.assertEquals(Integer.MAX_VALUE, result);

        // Modify the model by disconnecting c.cIn2.
        AtomicActor c = (AtomicActor)testModel.getEntity("c");
        IOPort cIn2 = (IOPort)c.getPort("cIn2");
        cIn2.unlinkAll();

        // Check distance again. Should be 2 now.
        result = ShortestPathFinder.calculateDistance(entities, 0, 2);
        Assert.assertEquals(2, result);

        result = ShortestPathFinder.calculateDistance(entities, 0, 0);
        Assert.assertEquals(0, result);

    }
}
