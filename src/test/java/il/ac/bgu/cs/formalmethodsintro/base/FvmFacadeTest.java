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

    @Test
    public void testIsAPDeterministicNonDeter() {
        assertFalse(fvm.isAPDeterministic(getNonDeterAP()));
    }

    @Test
    public void testIsAPDeterministicYesDeter() {
        assertTrue(fvm.isAPDeterministic(getDeterAP()));
    }

    @Test
    public void testIsStateTerminalNo() {
        TSandState tss = getTSandNoTerminalState();
        assertFalse(
                fvm.isStateTerminal(tss.getTs(),tss.getState())
        );
    }

    @Test
    public void testIsStateTerminalYes() {
        TSandState tss = getTSandTerminalState();
        assertTrue(
                fvm.isStateTerminal(tss.getTs(),tss.getState())
        );
    }

    private TSandState getTSandNoTerminalState() {
        TransitionSystem ts = new TransitionSystem();
        Object s0 = new Object();
        Object s1 = new Object();
        Object s2 = new Object();
        Object alpha = new Object();
        Object a = new Object();
        ts.addInitialState(s0);
        ts.addState(s1);
        ts.addState(s2);
        ts.addAction(alpha);
        ts.addTransition(new TSTransition(s0,alpha,s1));
        ts.addTransition(new TSTransition(s0,alpha,s2));
        ts.addAtomicProposition(a);
        ts.addToLabel(s1, a);
        ts.addToLabel(s2, a);
        return new TSandState(ts, s0);
    }


    private TSandState getTSandTerminalState() {
        TransitionSystem ts = new TransitionSystem();
        Object s0 = new Object();
        Object s1 = new Object();
        Object s2 = new Object();
        Object alpha = new Object();
        Object a = new Object();
        ts.addInitialState(s0);
        ts.addState(s1);
        ts.addState(s2);
        ts.addAction(alpha);
        ts.addTransition(new TSTransition(s0,alpha,s1));
        ts.addTransition(new TSTransition(s0,alpha,s2));
        ts.addAtomicProposition(a);
        ts.addToLabel(s1, a);
        ts.addToLabel(s2, a);
        return new TSandState(ts, s1);
    }

    private TransitionSystem getNonDeterAP() {
        TransitionSystem ts = new TransitionSystem();
        Object s0 = new Object();
        Object s1 = new Object();
        Object s2 = new Object();
        Object alpha = new Object();
        Object a = new Object();
        ts.addInitialState(s0);
        ts.addState(s1);
        ts.addState(s2);
        ts.addAction(alpha);
        ts.addTransition(new TSTransition(s0,alpha,s1));
        ts.addTransition(new TSTransition(s0,alpha,s2));
        ts.addAtomicProposition(a);
        ts.addToLabel(s1, a);
        ts.addToLabel(s2, a);
        return ts;
    }

    private TransitionSystem getDeterAP() {
        TransitionSystem ts = new TransitionSystem();
        Object s0 = new Object();
        Object s1 = new Object();
        Object s2 = new Object();
        Object alpha = new Object();
        Object a = new Object();
        Object b = new Object();
        ts.addInitialState(s0);
        ts.addState(s1);
        ts.addState(s2);
        ts.addAction(alpha);
        ts.addTransition(new TSTransition(s0,alpha,s1));
        ts.addTransition(new TSTransition(s0,alpha,s2));
        ts.addAtomicProposition(a);
        ts.addAtomicProposition(b);
        ts.addToLabel(s1, a);
        ts.addToLabel(s2, b);
        return ts;
    }


    private TransitionSystem getNonDeterAc() {
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
    private TransitionSystem getDeterAc() {
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

    private class TSandState{
        private TransitionSystem ts;
        private Object state;

        public TSandState(TransitionSystem ts, Object state) {
            this.ts = ts;
            this.state = state;
        }

        public TransitionSystem getTs() {
            return ts;
        }

        public Object getState() {
            return state;
        }
    }
}