package il.ac.bgu.cs.formalmethodsintro.base;

import il.ac.bgu.cs.formalmethodsintro.base.goal.GoalStructure;
import il.ac.bgu.cs.formalmethodsintro.base.transitionsystem.TSTransition;
import il.ac.bgu.cs.formalmethodsintro.base.transitionsystem.TransitionSystem;
import il.ac.bgu.cs.formalmethodsintro.base.util.GraphvizPainter;
import il.ac.bgu.cs.formalmethodsintro.base.util.Pair;
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
        Pair<TransitionSystem, Object> p  = getTSandNoTerminalState();
        assertFalse(
                fvm.isStateTerminal(p.getFirst(),p.getSecond())
        );
    }

    @Test
    public void testIsStateTerminalYes() {
        Pair<TransitionSystem, Object> p =getTSandTerminalState();
        assertTrue(
                fvm.isStateTerminal(p.getFirst(),p.getSecond())
        );
    }

    @Test
    public void testInterleav() {
        Pair<Pair<TransitionSystem, TransitionSystem>,TransitionSystem> p =T1T2T12();
        System.out.println(GraphvizPainter.toStringPainter().makeDotCode(p.getFirst().getFirst()));
        System.out.println(GraphvizPainter.toStringPainter().makeDotCode(p.getFirst().getSecond()));
        System.out.println(GraphvizPainter.toStringPainter().makeDotCode(p.getSecond()));
        assertEquals(
                fvm.interleave(p.first.first, p.first.second), p.second
        );
    }

    private Pair<Pair<TransitionSystem, TransitionSystem>, TransitionSystem> T1T2T12() {
        TransitionSystem t1 = new TransitionSystem();
        Object s10 = new Object();
        Object s11 = new Object();
        Object alpha1 = new Object();
        Object a1 = new Object();
        Object b1 = new Object();
        t1.addInitialState(s10);
        t1.addState(s11);
        t1.addAction(alpha1);
        t1.addAtomicProposition(a1);
        t1.addAtomicProposition(b1);
        t1.addToLabel(s10,a1);
        t1.addToLabel(s11,b1);
        t1.addTransition(new TSTransition(s10,alpha1,s11));

        TransitionSystem t2 = new TransitionSystem();
        Object s20 = new Object();
        Object s21 = new Object();
        Object alpha2 = new Object();
        Object a2 = new Object();
        Object b2 = new Object();
        t2.addInitialState(s20);
        t2.addState(s21);
        t2.addAction(alpha2);
        t2.addAtomicProposition(a2);
        t2.addAtomicProposition(b2);
        t2.addToLabel(s20,a2);
        t2.addToLabel(s21,b2);
        t2.addTransition(new TSTransition(s20,alpha2,s21));


        TransitionSystem t12 = new TransitionSystem();
        Pair<Object,Object>  p00  = new Pair<>(s10,s20);
        Pair<Object,Object>  p01  = new Pair<>(s10,s21);
        Pair<Object,Object>  p10  = new Pair<>(s11,s20);
        Pair<Object,Object>  p11  = new Pair<>(s11,s21);
        t12.addInitialState(p00);
        t12.addState(p01);
        t12.addState(p10);
        t12.addState(p11);
        t12.addAction(alpha2);
        t12.addAtomicProposition(a1);
        t12.addAtomicProposition(b2);
        t12.addAtomicProposition(a2);
        t12.addAtomicProposition(b2);
        t12.addToLabel(p00,a1);
        t12.addToLabel(p00,a2);
        t12.addToLabel(p01,a1);
        t12.addToLabel(p01,b2);
        t12.addToLabel(p10,b1);
        t12.addToLabel(p10,b1);
        t12.addToLabel(p11,b1);
        t12.addToLabel(p11,b2);

        t12.addTransition(new TSTransition(p00,alpha1,p10));
        t12.addTransition(new TSTransition(p00,alpha2,p01));
        t12.addTransition(new TSTransition(p01,alpha1,p11));
        t12.addTransition(new TSTransition(p10,alpha2,p11));

        return new Pair<>(new Pair<>(t1,t2), t12);
    }

    private Pair<TransitionSystem, Object> getTSandNoTerminalState() {
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
        return new Pair<>(ts,s0);
    }


    private Pair<TransitionSystem, Object> getTSandTerminalState() {
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
        return new Pair<>(ts, s1);
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

}