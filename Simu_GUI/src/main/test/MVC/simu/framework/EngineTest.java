package MVC.simu.framework;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class EngineFunctionalTest {

    static class DummyEngine extends Engine {
        boolean initialized, ranEvent, resultsDone;

        DummyEngine() { super(null); }

        @Override
        protected void initialization() { initialized = true; }
        @Override
        protected void runEvent(Event t) { ranEvent = true; }
        @Override
        protected void results() { resultsDone = true; }
    }

    private DummyEngine engine;

    @BeforeEach
    void setup() {
        Trace.setTraceLevel(Trace.Level.INFO);
        engine = new DummyEngine();
    }

    @Test
    void testFullRunLifecycle() {
        engine.setSimulationTime(0);
        engine.run();
        assertTrue(engine.initialized, "Engine should call initialization()");
        assertTrue(engine.resultsDone, "Engine should call results()");
    }

    @Test
    void testPauseResumeStop() {
        engine.pause();
        engine.resume();
        engine.stopSimulation();
        assertTrue(engine.isStopped());
    }

    @Test
    void testDelayConfiguration() {
        engine.setDelay(100);
        assertEquals(100, engine.getDelay());
    }
}
