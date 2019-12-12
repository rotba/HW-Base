package il.ac.bgu.cs.formalmethodsintro.base;

import il.ac.bgu.cs.formalmethodsintro.base.transitionsystem.AlternatingSequence;
import il.ac.bgu.cs.formalmethodsintro.base.transitionsystem.TSTransition;
import il.ac.bgu.cs.formalmethodsintro.base.transitionsystem.TransitionSystem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
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