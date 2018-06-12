package ptolemy.apps.graph;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import junit.framework.Assert;
import ptolemy.actor.AtomicActor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.Entity;

public class MinimumDistanceCalculatorTest {

    /** Model is created in the setUp() method and stored here. */
    public CompositeActor testModel;

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

    @Test
    public void testCalculateDistance() {
        List<Entity> entityList = testModel.entityList();
        int result = MinimumDistanceCalculator
                .calculateDistance(entityList.toArray(), 0, 2);
        Assert.assertEquals(1, result);

        result = MinimumDistanceCalculator
                .calculateDistance(entityList.toArray(), 0, 0);
        Assert.assertEquals(0, result);

        result = MinimumDistanceCalculator
                .calculateDistance(entityList.toArray(), 1, 0);
        Assert.assertEquals(Integer.MAX_VALUE, result);

    }
}
