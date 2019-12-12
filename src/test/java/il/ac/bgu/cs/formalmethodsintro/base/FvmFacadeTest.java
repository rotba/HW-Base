package il.ac.bgu.cs.formalmethodsintro.base;

import il.ac.bgu.cs.formalmethodsintro.base.goal.GoalStructure;
import il.ac.bgu.cs.formalmethodsintro.base.programgraph.PGTransition;
import il.ac.bgu.cs.formalmethodsintro.base.programgraph.ProgramGraph;
import il.ac.bgu.cs.formalmethodsintro.base.transitionsystem.AlternatingSequence;
import il.ac.bgu.cs.formalmethodsintro.base.transitionsystem.TSTransition;
import il.ac.bgu.cs.formalmethodsintro.base.transitionsystem.TransitionSystem;
import il.ac.bgu.cs.formalmethodsintro.base.util.GraphvizPainter;
import il.ac.bgu.cs.formalmethodsintro.base.util.Pair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

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
    public void testIsExecutionNotInitial(){
        List input = getASNotInitial();
        assertFalse(fvm.isExecution((TransitionSystem)input.get(0), (AlternatingSequence)input.get(1)));
    }
    @Test
    public void testIsExecutionNotTerminal(){
        List input = getASNotMaximal();
        assertFalse(fvm.isExecution((TransitionSystem)input.get(0), (AlternatingSequence)input.get(1)));
    }
    @Test
    public void testIsExecutionNotValid(){
        List input = getASNotValid();
        assertFalse(fvm.isExecution((TransitionSystem)input.get(0), (AlternatingSequence)input.get(1)));
    }
    @Test
    public void testIsExecutionValid(){
        List input = getASMaximal();
        assertTrue(fvm.isExecution((TransitionSystem)input.get(0), (AlternatingSequence)input.get(1)));
    }
    @Test
    public void testIsExecutionFragmentNotValid(){
        List input = getASNotValid();
        assertFalse(fvm.isExecutionFragment((TransitionSystem)input.get(0), (AlternatingSequence)input.get(1)));
    }
    @Test
    public void testIsExecutionFragmentValid(){
        List input = getASNotInitial();
        assertTrue(fvm.isExecutionFragment((TransitionSystem)input.get(0), (AlternatingSequence)input.get(1)));
    }
    @Test
    public void testIsInitialExecutionFragmentNotIntial(){
        List input = getASNotInitial();
        assertFalse(fvm.isInitialExecutionFragment((TransitionSystem)input.get(0), (AlternatingSequence)input.get(1)));
    }
    @Test
    public void testIsInitialExecutionFragmentNotValid(){
        List input = getASNotValid();
        assertFalse(fvm.isInitialExecutionFragment((TransitionSystem)input.get(0), (AlternatingSequence)input.get(1)));
    }
    @Test
    public void testIsInitialExecutionFragmentValid(){
        List input = getASNotMaximal();
        assertTrue(fvm.isInitialExecutionFragment((TransitionSystem)input.get(0), (AlternatingSequence)input.get(1)));
    }
    @Test
    public void testIsMaximalExecutionFragmentNotMaximal(){
        List input = getASNotMaximal();
        assertFalse(fvm.isMaximalExecutionFragment((TransitionSystem)input.get(0), (AlternatingSequence)input.get(1)));
    }
    @Test
    public void testIsMaximalExecutionFragmentNotValid(){
        List input = getASNotValid();
        assertFalse(fvm.isMaximalExecutionFragment((TransitionSystem)input.get(0), (AlternatingSequence)input.get(1)));
    }
    @Test
    public void testIsMaximalExecutionFragmentValid(){
        List input = getASMaximal();
        assertTrue(fvm.isMaximalExecutionFragment((TransitionSystem)input.get(0), (AlternatingSequence)input.get(1)));}


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
    public void testInterleavTS() {
        Pair<Pair<TransitionSystem, TransitionSystem>,TransitionSystem> p =T1T2T12();
        assertEquals(
                fvm.interleave(p.first.first, p.first.second), p.second
        );
    }

    @Test
    public void testInterleavPG() {
        Pair<Pair<ProgramGraph, ProgramGraph>,ProgramGraph> p =P1P2P12();
        assertEquals(
                fvm.interleave(p.first.first, p.first.second), p.second
        );
    }

    @Test
    public void testtransitionSystemFromProgramGraph() {
        Pair<ProgramGraph, TransitionSystem> p = TS1PG1();
        assertEquals(
                fvm.interleave(p.first.first, p.first.second), p.second
        );
    }

    private Pair<ProgramGraph, TransitionSystem> TS1PG1() {
        ProgramGraph p = new ProgramGraph();
        Object l0 = "loc0";
        Object l1 = "loc1";
        String c0 = "x!=0";
        Object a0 = "x:=x+1";
        p.addInitalization(new ArrayList<>(List.of("x:=0")));
        p.addLocation(l0);
        p.addLocation(l1);
        p.addTransition(new PGTransition(l0,c0, a0, l1));

        TransitionSystem ts = new TransitionSystem();
        HashMap<String, Integer> m0= new HashMap<>();
        HashMap<String, Integer> m1= new HashMap<>();
        HashMap<String, Integer> m2= new HashMap<>();
        m0.put("x",0);
        m1.put("x",1);
        m2.put("x",2);
        Object s00 = new Pair<>(l0,m0);
        Object s01 = new Pair<>(l0,m1);
        Object s02 = new Pair<>(l0,m2);
        Object s10 = new Pair<>(l0,m0);
        Object s11 = new Pair<>(l0,m1);
        Object s12 = new Pair<>(l0,m2);
        ts.addStates(s01,s02,s10,s11,s12);
        ts.addInitialState(s00);
        ts.addTransition(new TSTransition(s00,a0, s11));
        ts.addTransition(new TSTransition(s10,a0, s12));

    }


    private Pair<Pair<ProgramGraph, ProgramGraph>, ProgramGraph> P1P2P12() {
        ProgramGraph p0 = new ProgramGraph();
        Object l00 = "loc00";
        Object l01 = "loc01";
        String c0 = "x!=0";
        Object a0 = "x:=x+1";
        p0.addInitalization(new ArrayList<>(List.of("x:=0")));
        p0.addLocation(l00);
        p0.addLocation(l01);
        p0.addTransition(new PGTransition(l00,c0, a0, l01));

        ProgramGraph p1 = new ProgramGraph();
        Object l10 = "loc10";
        Object l11 = "loc11";
        String c1 = "y>1";
        Object a1 = "y:=y-2";
        p1.addInitalization(new ArrayList<>(List.of("y:=7")));
        p1.addLocation(l10);
        p1.addLocation(l11);
        p1.addTransition(new PGTransition(l10,c1, a1, l11));

        ProgramGraph pg01 = new ProgramGraph();
        Pair p00 = new Pair<>(l00,l10);
        Pair p01 = new Pair<>(l00,l11);
        Pair p10 = new Pair<>(l01,l10);
        Pair p11 = new Pair<>(l01,l11);
        p1.addInitalization(new ArrayList<>(List.of("y:=7", "x:=0")));
        pg01.addLocation(p00);
        pg01.addLocation(p01);
        pg01.addLocation(p10);
        pg01.addLocation(p11);
        pg01.addTransition(new PGTransition(p00,c0, a0, p10));
        pg01.addTransition(new PGTransition(p00,c1, a1, p01));
        pg01.addTransition(new PGTransition(p10,c1, a1, p11));
        pg01.addTransition(new PGTransition(p01,c0, a0, p11));

        return new Pair<>(new Pair<>(p0,p1),pg01);

    }

    private Pair<Pair<TransitionSystem, TransitionSystem>, TransitionSystem> T1T2T12() {
        TransitionSystem t1 = new TransitionSystem();
        Object s10 = "s10";
        Object s11 = "s11";
        Object alpha1 = "alpha1";
        Object a1 = "a1";
        Object b1 = "b1";
        t1.addInitialState(s10);
        t1.addState(s11);
        t1.addAction(alpha1);
        t1.addAtomicProposition(a1);
        t1.addAtomicProposition(b1);
        t1.addToLabel(s10,a1);
        t1.addToLabel(s11,b1);
        t1.addTransition(new TSTransition(s10,alpha1,s11));

        TransitionSystem t2 = new TransitionSystem();
        Object s20 = "s20";
        Object s21 = "s21";
        Object alpha2 = "alpha2";
        Object a2 = "a2";
        Object b2 = "b2";
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
        t12.addToLabel(p10,a2);
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


    private static List getASMaximal() {
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
        LinkedList states = new LinkedList();


        states.addFirst(s1);
        states.addFirst(s0);
        LinkedList actions = new LinkedList();
        actions.addFirst(alpha);
        AlternatingSequence as = new AlternatingSequence(states, actions);
        LinkedList ret = new LinkedList();
        ret.addFirst(as);
        ret.addFirst(ts);
        return ret;
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
    
    private static List getASNotValid() {
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
        LinkedList states = new LinkedList();
        states.addFirst(s2);
        states.addFirst(s0);
        LinkedList actions = new LinkedList();
        actions.addFirst(alpha);
        AlternatingSequence as = new AlternatingSequence(states, actions);
        LinkedList ret = new LinkedList();
        ret.addFirst(as);
        ret.addFirst(ts);
        return ret;
    }

    private static List getASNotMaximal() {
        TransitionSystem ts = new TransitionSystem();
        Object s0 = new Object();
        Object s1 = new Object();
        Object s2 = new Object();
        Object alpha = new Object();
        ts.addInitialState(s0);
        ts.addState(s1);
        ts.addAction(alpha);
        ts.addTransition(new TSTransition(s0,alpha,s1));
        ts.addTransition(new TSTransition(s1,alpha,s0));
        LinkedList states = new LinkedList();
        states.addFirst(s1);
        states.addFirst(s0);
        LinkedList actions = new LinkedList();
        actions.addFirst(alpha);
        AlternatingSequence as = new AlternatingSequence(states, actions);
        LinkedList ret = new LinkedList();
        ret.addFirst(as);
        ret.addFirst(ts);
        return ret;
    }

    private static List getASNotInitial(){
        TransitionSystem ts = new TransitionSystem();
        Object s0 = new Object();
        Object s1 = new Object();
        Object s2 = new Object();
        Object alpha = new Object();
        ts.addInitialState(s0);
        ts.addState(s1);
        ts.addAction(alpha);
        ts.addTransition(new TSTransition(s0,alpha,s1));
        ts.addTransition(new TSTransition(s1,alpha,s0));
        LinkedList states = new LinkedList();
        states.addFirst(s0);
        states.addFirst(s1);
        LinkedList actions = new LinkedList();
        actions.addFirst(alpha);
        AlternatingSequence as = new AlternatingSequence(states, actions);
        LinkedList ret = new LinkedList();
        ret.addFirst(as);
        ret.addFirst(ts);
        return ret;
    }

}