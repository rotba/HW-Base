package il.ac.bgu.cs.formalmethodsintro.base;

import il.ac.bgu.cs.formalmethodsintro.base.transitionsystem.TSTransition;
import il.ac.bgu.cs.formalmethodsintro.base.transitionsystem.TransitionSystem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.*;

public class FvmFacadeTest {

    private FvmFacade fvm = new FvmFacade();

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testIsActionDeterministicNonDeter() {
        assertFalse(fvm.isActionDeterministic(getNonDeterAc()));
    }
    @Test
    public void testIsActionDeterministicYesDeter() {
        assertTrue(fvm.isActionDeterministic(getDeterAc()));
    }


    private static TransitionSystem getNonDeterAc() {
        TransitionSystem ts = new TransitionSystem();
        Object s0 = new Object();
        Object s1 = new Object();
        Object s2 = new Object();
        Object alpha = new Object();
        ts.addInitialState(s0);
        ts.addState(s1);
        ts.addState(s2);
        ts.addAction(alpha);
        ts.addTransition(new TSTransition(s0,alpha,s1));
        ts.addTransition(new TSTransition(s0,alpha,s2));
        return ts;
    }
    private static TransitionSystem getDeterAc() {
        TransitionSystem ts = new TransitionSystem();
        Object s0 = new Object();
        Object s1 = new Object();
        Object s2 = new Object();
        Object alpha = new Object();
        ts.addInitialState(s0);
        ts.addState(s1);
        ts.addState(s2);
        ts.addAction(alpha);
        ts.addTransition(new TSTransition(s0,alpha,s1));
        return ts;
    }
}