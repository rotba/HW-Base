package il.ac.bgu.cs.formalmethodsintro.base;

import il.ac.bgu.cs.formalmethodsintro.base.channelsystem.ChannelSystem;
import il.ac.bgu.cs.formalmethodsintro.base.channelsystem.ParserBasedInterleavingActDef;
import il.ac.bgu.cs.formalmethodsintro.base.circuits.Circuit;
import il.ac.bgu.cs.formalmethodsintro.base.exceptions.StateNotFoundException;
import il.ac.bgu.cs.formalmethodsintro.base.nanopromela.NanoPromelaFileReader;
import il.ac.bgu.cs.formalmethodsintro.base.nanopromela.NanoPromelaParser;
import il.ac.bgu.cs.formalmethodsintro.base.programgraph.*;
import il.ac.bgu.cs.formalmethodsintro.base.transitionsystem.AlternatingSequence;
import il.ac.bgu.cs.formalmethodsintro.base.transitionsystem.TSTransition;
import il.ac.bgu.cs.formalmethodsintro.base.transitionsystem.TransitionSystem;
import il.ac.bgu.cs.formalmethodsintro.base.util.GraphvizPainter;
import il.ac.bgu.cs.formalmethodsintro.base.util.Pair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class FvmFacadeTest {

    private static final Object TAU =null;

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
    public void testInterleavTS1() {
        Pair<Pair<TransitionSystem, TransitionSystem>,TransitionSystem> p =T1T2T12();
        assertEquals(
                fvm.interleave(p.first.first, p.first.second), p.second
        );
    }

    @Test
    public void testInterleavTS2() {
        Pair<Pair<TransitionSystem, TransitionSystem>,TransitionSystem> p =ts_interleave_2();
        assertEquals(
                fvm.interleave(p.first.first, p.first.second), p.second
        );
    }

    @Test
    public void isStateTerminal() {
        try{
            Pair<TransitionSystem, Object> data = getTSandTerminalState();
            Object no_state = "no state of ts";
            fvm.isStateTerminal(data.first, no_state);
            assertTrue(false);
        }
        catch (StateNotFoundException e){
            assertTrue(true);
        }
    }

    @Test
    public void testInterleavHS() {
        Pair<Object[],TransitionSystem> p =ts_interleave_hs();
        assertEquals(
                fvm.interleave((TransitionSystem) p.first[0], (TransitionSystem)p.first[1], (Set) Set.of(p.first[2]) ), p.second
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
    public void testCircuit() {
        Pair<Circuit, TransitionSystem> p =circuit();
        TransitionSystem ts = fvm.transitionSystemFromCircuit(p.first);
        assertEquals(
                fvm.transitionSystemFromCircuit(p.first), p.second
        );
    }

    @Test
    public void testNanoPromela() {
        String code =
                "atomic{a:=3;b:=0};\n" +
                        "  do \n" +
                        "    :: a > b -> \n" +
                        "\t\tdo \n" +
                        "\t\t   :: a != b -> \n" +
                        "\t\t\t\tb := b+1 \n" +
                        "\t\tod\n" +
                        "  od\n";

        try {
            ProgramGraph<String,String> pg = fvm.programGraphFromNanoPromelaString(code);
            assertEquals(fvm.programGraphFromNanoPromela(code), pgnp());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public void testSokoban(){
//        TransitionSystem sokoban = fvm.sokobanTS();
//        Set<Object> reachable = fvm.reach(sokoban);
//        for(Object state : reachable){
//            (Pair<Pair<Pair<Pair<Pair<Pair<Integer,Integer>, Pair<Integer,Integer>>, Pair<Integer,Integer>>, Pair<Integer,Integer>>, Pair<Integer,Integer>>, Pair<Integer,Integer>>>) state
//            Pair<Integer,Integer> box_1 = state
//        }
//    }


    //todo: complete function
    private ProgramGraph<String, String> pgnp() {
        ProgramGraph pg = new ProgramGraph();
        return pg;
    }


    @Test
    public void testTransitionSystemFromProgramGraph() {
        testTransitionSystemFromProgramGraph(new pgAndts() {
            @Override
            public Pair<ProgramGraph, TransitionSystem> get() {
                return TS1PG1();
            }
        });
    }

    @Test
    public void testTransitionSystemChannelSystem() {
        testTransitionSystemFromChannelSystem(new csAndts() {
            @Override
            public Pair<ChannelSystem, TransitionSystem> get() {
                return CH1PG1();
            }
        });
    }

    private void testTransitionSystemFromChannelSystem(csAndts csAndts) {
        Pair<ChannelSystem, TransitionSystem> p = csAndts.get();
        TransitionSystem expected = p.getSecond();
        ChannelSystem input = p.getFirst();
        TransitionSystem actual = fvm.transitionSystemFromChannelSystem(
                input,
                new HashSet<ActionDef>(Set.of(new ParserBasedActDef(),new ParserBasedInterleavingActDef())),
                new HashSet<ConditionDef>(Set.of(new ParserBasedCondDef()))
        );
        System.out.println(GraphvizPainter.toStringPainter().makeDotCode(expected));
        System.out.println(GraphvizPainter.toStringPainter().makeDotCode(actual));
        assertEquals(expected,actual);
    }

    private void testTransitionSystemFromProgramGraph(pgAndts pgAndts) {
        Pair<ProgramGraph, TransitionSystem> p = pgAndts.get();
        TransitionSystem expected = p.getSecond();
        ProgramGraph input = p.getFirst();
        TransitionSystem actual = fvm.transitionSystemFromProgramGraph(
                input,
                new HashSet<ActionDef>(Set.of(new ParserBasedActDef())),
                new HashSet<ConditionDef>(Set.of(new ParserBasedCondDef()))
        );
        System.out.println(GraphvizPainter.toStringPainter().makeDotCode(expected));
        System.out.println(GraphvizPainter.toStringPainter().makeDotCode(actual));
        assertEquals(expected,actual);
    }


    private Pair<ChannelSystem, TransitionSystem> CH1PG1() {
        Object c1 = "C1";
        Object c2 = "C2";
        Object d = "_D";
        int m = 1;
        String c1bang1 = "C1!1";
        String c2bang1 = "C2!1";
        String c1bang = "C1!x";
        String c2q = "C2?x";
        String _dbang= "_D!";
        String c2bang = "C2!y";
        String c1q = "C1?y";
        String _dq = "_D?";
        ProgramGraph p1 = new ProgramGraph();
        Object loc11 = "loc11";
        Object loc12 = "loc12";
        Object loc13 = "loc13";
        Object loc14 = "loc14";
        p1.addLocation(loc11);
        p1.addLocation(loc12);
        p1.addLocation(loc13);
        p1.addLocation(loc14);
        p1.setInitial(loc11,true);
        p1.addTransition(new PGTransition(loc11,"true",c1bang1, loc12));
        p1.addTransition(new PGTransition(loc12,"true",c2q, loc13));
        p1.addTransition(new PGTransition(loc13,"true",_dbang, loc14));
        p1.addInitalization(new ArrayList<>(List.of("x:=0")));

        ProgramGraph p2 = new ProgramGraph();
        Object loc21 = "loc21";
        Object loc22 = "loc22";
        Object loc23 = "loc23";
        Object loc24 = "loc24";
        p2.addLocation(loc21);
        p2.addLocation(loc22);
        p2.addLocation(loc23);
        p2.addLocation(loc24);
        p2.setInitial(loc21,true);
        p2.addTransition(new PGTransition(loc21,"true",c2bang1, loc22));
        p2.addTransition(new PGTransition(loc22,"true",c1q, loc23));
        p2.addTransition(new PGTransition(loc23,"true",_dq, loc24));
        p2.addInitalization(new ArrayList<>(List.of("y:=0")));
        ChannelSystem cs =new ChannelSystem(new ArrayList<>(List.of(p1,p2)));

        TransitionSystem ts = new TransitionSystem();
        Map theEmptyEval = new HashMap<>() {{
            put("x", 0);
            put("y", 0);
        }};
        Pair s11e = genPair(loc11,loc21, new HashMap<>() {{
            put("x", 0);
            put("y", 0);
        }});
        Pair s21c1m = genPair(loc12,loc21, new HashMap<>() {{
            put(c1, new LinkedList<>(Arrays.asList(m)));
            put("x", 0);
            put("y", 0);
        }});
        Pair s22c1mc2m = genPair(loc12,loc22, new HashMap<>() {{
            put(c1, new LinkedList<>(Arrays.asList(m)));
            put(c2, new LinkedList<>(Arrays.asList(m)));
            put("x", 0);
            put("y", 0);
        }});
        Pair s12c2m = genPair(loc11,loc22, new HashMap<>() {{
            put(c2, new LinkedList<>(Arrays.asList(m)));
            put("x", 0);
            put("y", 0);
        }});
        Pair s32c2m = genPair(loc13,loc22, new HashMap<>() {{
            put(c1, new LinkedList<>(Arrays.asList(m)));
            put(c2, new LinkedList<>());
            put("x", 1);
            put("y", 0);
        }});
        Pair s23c1m = genPair(loc12,loc23, new HashMap<>() {{
            put(c1, new LinkedList<>());
            put(c2, new LinkedList<>(Arrays.asList(m)));
            put("x", 0);
            put("y", 1);
        }});
        Pair s33e = genPair(loc13,loc23, new HashMap<>() {{
            put(c1, new LinkedList<>());
            put(c2, new LinkedList<>());
            put("x", 1);
            put("y", 1);
        }});
        Pair s44e = genPair(loc14,loc24, new HashMap<>() {{
            put(c1, new LinkedList<>());
            put(c2, new LinkedList<>());
            put("x", 1);
            put("y", 1);
        }});
        ts.addInitialState(s11e);
        ts.addStates(s21c1m,s12c2m,s22c1mc2m,s32c2m,s23c1m,s33e,s44e);
        ts.addTransition(new TSTransition(s11e,TAU, s21c1m));
        ts.addTransition(new TSTransition(s11e,TAU, s12c2m));
        ts.addTransition(new TSTransition(s21c1m,TAU, s22c1mc2m));
        ts.addTransition(new TSTransition(s12c2m,TAU, s22c1mc2m));
        ts.addTransition(new TSTransition(s22c1mc2m,TAU, s32c2m));
        ts.addTransition(new TSTransition(s22c1mc2m,TAU, s23c1m));
        ts.addTransition(new TSTransition(s23c1m,TAU, s33e));
        ts.addTransition(new TSTransition(s32c2m,TAU, s33e));
        ts.addTransition(new TSTransition(s33e,TAU, s44e));
        ts.addToLabel(s11e,"x=0");
        ts.addToLabel(s11e,"y=0");
        ts.addToLabel(s11e,loc11.toString());
        ts.addToLabel(s11e,loc21.toString());
        ts.addToLabel(s21c1m,"x=0");
        ts.addToLabel(s21c1m,"y=0");
        ts.addToLabel(s21c1m,"C1=[1]");
        ts.addToLabel(s21c1m,loc12.toString());
        ts.addToLabel(s21c1m,loc21.toString());
        ts.addToLabel(s12c2m,"x=0");
        ts.addToLabel(s12c2m,"y=0");
        ts.addToLabel(s12c2m,"C2=[1]");
        ts.addToLabel(s12c2m,loc11.toString());
        ts.addToLabel(s12c2m,loc22.toString());
        ts.addToLabel(s22c1mc2m,"x=0");
        ts.addToLabel(s22c1mc2m,"y=0");
        ts.addToLabel(s22c1mc2m,"C1=[1]");
        ts.addToLabel(s22c1mc2m,"C2=[1]");
        ts.addToLabel(s22c1mc2m,loc12.toString());
        ts.addToLabel(s22c1mc2m,loc22.toString());
        ts.addToLabel(s32c2m,"x=1");
        ts.addToLabel(s32c2m,"y=0");
        ts.addToLabel(s32c2m,"C1=[1]");
        ts.addToLabel(s32c2m,"C2=[]");
        ts.addToLabel(s32c2m,loc13.toString());
        ts.addToLabel(s32c2m,loc22.toString());
        ts.addToLabel(s23c1m,"x=0");
        ts.addToLabel(s23c1m,"y=1");
        ts.addToLabel(s23c1m,"C1=[]");
        ts.addToLabel(s23c1m,"C2=[1]");
        ts.addToLabel(s23c1m,loc12.toString());
        ts.addToLabel(s23c1m,loc23.toString());
        ts.addToLabel(s33e,"x=1");
        ts.addToLabel(s33e,"y=1");
        ts.addToLabel(s33e,"C1=[]");
        ts.addToLabel(s33e,"C2=[]");
        ts.addToLabel(s33e,loc13.toString());
        ts.addToLabel(s33e,loc23.toString());
        ts.addToLabel(s44e,"x=1");
        ts.addToLabel(s44e,"y=1");
        ts.addToLabel(s44e,"C1=[]");
        ts.addToLabel(s44e,"C2=[]");
        ts.addToLabel(s44e,loc14.toString());
        ts.addToLabel(s44e,loc24.toString());
        return new Pair<>(cs,ts);
    }

    private Pair genPair(Object s1, Object s2, Map eval) {

        return new Pair(
                new ArrayList<>(List.of(s1,s2)),
                eval
        );
    }

    private Pair<ProgramGraph, TransitionSystem> TS1PG1() {
        ProgramGraph p = new ProgramGraph();
        Object l0 = "loc0";
        Object l1 = "loc1";
        String c0 = "true";
        Object a0 = "x:=2*x";
        p.addInitalization(new ArrayList<>(List.of("x:=3")));
        p.addLocation(l0);
        p.addLocation(l1);
        p.setInitial(l0,true);
        p.addTransition(new PGTransition(l0,c0, a0, l1));

        TransitionSystem ts = new TransitionSystem();
        HashMap<String, Integer> m3= new HashMap<>();
        HashMap<String, Integer> m6= new HashMap<>();
        m3.put("x",3);
        m6.put("x",6);
        Object s03 = new Pair<>(l0,m3);
        Object s16 = new Pair<>(l1,m6);
        ts.addState(s16);
        ts.addInitialState(s03);
        ts.addTransition(new TSTransition(s03,a0, s16));
        ts.addToLabel(s03, "x=3");
        ts.addToLabel(s03, "loc0");
        ts.addToLabel(s16, "x=6");
        ts.addToLabel(s16, "loc1");
        return new Pair<>(p,ts);
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
        p0.setInitial(l00, true);
        p0.addTransition(new PGTransition(l00,c0, a0, l01));

        ProgramGraph p1 = new ProgramGraph();
        Object l10 = "loc10";
        Object l11 = "loc11";
        String c1 = "y>1";
        Object a1 = "y:=y-2";
        p1.addInitalization((List.of("y:=7")));
        p1.addLocation(l10);
        p1.addLocation(l11);
        p1.setInitial(l10, true);
        p1.addTransition(new PGTransition(l10,c1, a1, l11));

        ProgramGraph pg01 = new ProgramGraph();
        Pair p00 = new Pair<>(l00,l10);
        Pair p01 = new Pair<>(l00,l11);
        Pair p10 = new Pair<>(l01,l10);
        Pair p11 = new Pair<>(l01,l11);
        pg01.addInitalization(List.of("x:=0","y:=7"));
//        pg01.addInitalization(List.of("x:=0"));
        pg01.addLocation(p00);
        pg01.addLocation(p01);
        pg01.addLocation(p10);
        pg01.addLocation(p11);
        pg01.setInitial(p00, true);
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

    private Pair<Pair<TransitionSystem, TransitionSystem>, TransitionSystem> ts_interleave_2() {
        TransitionSystem t1 = new TransitionSystem();
        Object l1 = "l1";
        Object l2 = "l2";
        Object l3 = "l3";
        Object alpha = "alpha";
        Object beta = "beta";
        t1.addInitialState(l1);
        t1.addStates(l2, l3);
        t1.addTransition(new TSTransition(l1,alpha,l2));
        t1.addTransition(new TSTransition(l3,alpha,l1));
        t1.addTransition(new TSTransition(l1,beta,l3));

        TransitionSystem t2 = new TransitionSystem();
        Object q1 = "q1";
        Object q2 = "q2";
        t2.addInitialState(q1);
        t2.addState(q2);
        t2.addTransition(new TSTransition(q1, alpha, q2));
        t2.addTransition(new TSTransition(q2, beta, q1));


        TransitionSystem t12 = new TransitionSystem();
        Pair<Object,Object>  l1q1  = new Pair<>(l1,q1);
        Pair<Object,Object>  l1q2  = new Pair<>(l1,q2);
        Pair<Object,Object>  l2q1  = new Pair<>(l2,q1);
        Pair<Object,Object>  l2q2  = new Pair<>(l2,q2);
        Pair<Object,Object>  l3q1  = new Pair<>(l3,q1);
        Pair<Object,Object>  l3q2  = new Pair<>(l3,q2);
        t12.addInitialState(l1q1);
        t12.addStates(l1q2, l2q1, l2q2, l3q1, l3q2);
        t12.addTransition(new TSTransition(l1q1,alpha,l1q2));
        t12.addTransition(new TSTransition(l1q1,alpha,l2q1));
        t12.addTransition(new TSTransition(l1q1,beta,l3q1));
        t12.addTransition(new TSTransition(l1q2,alpha,l2q2));
        t12.addTransition(new TSTransition(l1q2,beta,l1q1));
        t12.addTransition(new TSTransition(l2q1,alpha,l2q2));
        t12.addTransition(new TSTransition(l2q2,beta,l2q1));
        t12.addTransition(new TSTransition(l3q1,alpha,l1q1));
        t12.addTransition(new TSTransition(l3q1,alpha,l3q2));
        t12.addTransition(new TSTransition(l3q2,alpha,l1q2));
        t12.addTransition(new TSTransition(l3q2,beta,l3q1));
        t12.addTransition(new TSTransition(l1q2,beta,l3q2));


        return new Pair<>(new Pair<>(t1,t2), t12);
    }

    private Pair<Object[], TransitionSystem> ts_interleave_hs() {
        TransitionSystem t1 = new TransitionSystem();
        Object l1 = "l1";
        Object l2 = "l2";
        Object l3 = "l3";
        Object l4 = "l4";
        Object alpha1 = "alpha1";
        Object alpha2 = "alpha2";
        Object gama= "gama";
        t1.addInitialState(l1);
        t1.addStates(l2, l3, l4);
        t1.addTransition(new TSTransition(l1,alpha1,l2));
        t1.addTransition(new TSTransition(l2,gama,l3));
        t1.addTransition(new TSTransition(l3,alpha2,l4));

        TransitionSystem t2 = new TransitionSystem();
        Object m1 = "l1";
        Object m2 = "l2";
        Object m3 = "l3";
        Object m4 = "l4";
        Object beta1 = "beta1";
        Object beta2 = "beta2";
        t2.addInitialState(m1);
        t2.addStates(m2, m3, m4);
        t2.addTransition(new TSTransition(m1,beta1,m2));
        t2.addTransition(new TSTransition(m2,gama,m3));
        t2.addTransition(new TSTransition(m3,beta2,m4));


        TransitionSystem t12 = new TransitionSystem();
        Pair<Object,Object>  l1m1  = new Pair<>(l1,m1);
        Pair<Object,Object>  l1m2  = new Pair<>(l1,m2);
        Pair<Object,Object>  l2m1  = new Pair<>(l2,m1);
        Pair<Object,Object>  l2m2  = new Pair<>(l2,m2);
        Pair<Object,Object>  l3m3  = new Pair<>(l3,m3);
        Pair<Object,Object>  l4m3  = new Pair<>(l4,m3);
        Pair<Object,Object>  l3m4  = new Pair<>(l3,m4);
        Pair<Object,Object>  l4m4  = new Pair<>(l4,m4);
        Pair<Object,Object>  l1m3  = new Pair<>(l1,m3);
        Pair<Object,Object>  l1m4  = new Pair<>(l1,m4);
        Pair<Object,Object>  l2m3  = new Pair<>(l2,m3);
        Pair<Object,Object>  l2m4  = new Pair<>(l2,m4);
        Pair<Object,Object>  l3m1  = new Pair<>(l3,m1);
        Pair<Object,Object>  l3m2  = new Pair<>(l3,m2);
        Pair<Object,Object>  l4m1  = new Pair<>(l4,m1);
        Pair<Object,Object>  l4m2  = new Pair<>(l4,m2);
        t12.addInitialState(l1m1);
        t12.addStates(l1m2,l2m1,l2m2,l3m3,l4m3,l3m4,l4m4,l1m3,l1m4,l2m3,l2m4,l3m1,l3m2,l4m1,l4m2);
        t12.addTransition(new TSTransition(l1m1,alpha1,l2m1));
        t12.addTransition(new TSTransition(l1m1,beta1,l1m2));
        t12.addTransition(new TSTransition(l2m1,beta1,l2m2));
        t12.addTransition(new TSTransition(l1m2,alpha1,l2m2));
        t12.addTransition(new TSTransition(l2m2,gama,l3m3));
        t12.addTransition(new TSTransition(l3m3,alpha2,l4m3));
        t12.addTransition(new TSTransition(l3m3,beta2,l3m4));
        t12.addTransition(new TSTransition(l4m3,beta2,l4m4));
        t12.addTransition(new TSTransition(l3m4,alpha2,l4m4));

        t12.addTransition(new TSTransition(l1m3,beta2,l1m4));
        t12.addTransition(new TSTransition(l1m3,alpha1,l2m3));
        t12.addTransition(new TSTransition(l1m4,alpha1,l2m4));
        t12.addTransition(new TSTransition(l2m3,beta2,l2m4));
        t12.addTransition(new TSTransition(l3m1,beta1,l3m2));
        t12.addTransition(new TSTransition(l3m1,alpha2,l4m1));
        t12.addTransition(new TSTransition(l3m2,alpha2,l4m2));
        t12.addTransition(new TSTransition(l4m1,beta1,l4m2));

        Object[] ret = {t1, t2, gama};
        return new Pair<>(ret, t12);
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

    // from lecture 3
    class CircuitImpl implements Circuit{

        private Set<String> inputPortNames;
        private Set<String> registersNames;
        private Set<String> outputsNames;

        public CircuitImpl(Set<String> inputPort, Set<String> registers,Set<String> outputs){
            this.inputPortNames = inputPort;
            this.registersNames = registers;
            this.outputsNames = outputs;
        }
        @Override
        public Set<String> getInputPortNames() {
            return inputPortNames;
        }

        @Override
        public Set<String> getRegisterNames() {
            return registersNames;
        }

        @Override
        public Set<String> getOutputPortNames() {
            return outputsNames;
        }

        @Override
        public Map<String, Boolean> updateRegisters(Map<String, Boolean> inputs, Map<String, Boolean> registers) {
            Map<String, Boolean> updated = new HashMap<>();
            for (Map.Entry<String,Boolean> entry : registers.entrySet()){
                if(entry.getKey().equals("r1"))
                    updated.put("r1", (inputs.get("x") && registers.get("r2")) ^ entry.getValue());
                else
                    updated.put(entry.getKey(), inputs.get("x") ^ entry.getValue());
            }
            return updated;
        }

        @Override
        public Map<String, Boolean> computeOutputs(Map<String, Boolean> inputs, Map<String, Boolean> registers) {
            Map<String, Boolean> updated = new HashMap<>();
            for(String out : outputsNames){
                HashSet<Boolean> all = new HashSet<Boolean>(registers.values()) ;
                all.addAll(inputs.values());
                updated.put(out, all.stream().reduce((a,b)-> a && b).orElse(true));
            }
            return updated;
        }
    }

    private Pair<Circuit, TransitionSystem> circuit(){
        Circuit circuit = new CircuitImpl(Set.of("x"), Set.of("r1", "r2"), Set.of("y"));
        TransitionSystem ts = new TransitionSystem();
        Pair<Map, Map> s000 = new Pair(Map.of("r1", false, "r2", false), Map.of("x", false));
        Pair<Map, Map> s010 = new Pair(Map.of("r1", false, "r2", true), Map.of("x", false));
        Pair<Map, Map> s100 = new Pair(Map.of("r1", true, "r2", false), Map.of("x", false));
        Pair<Map, Map> s110 = new Pair(Map.of("r1", true, "r2", true), Map.of("x", false));
        Pair<Map, Map> s001 = new Pair(Map.of("r1", false, "r2", false), Map.of("x", true));
        Pair<Map, Map> s011 = new Pair(Map.of("r1", false, "r2", true), Map.of("x", true));
        Pair<Map, Map> s101 =  new Pair(Map.of("r1", true, "r2", false), Map.of("x", true));
        Pair<Map, Map> s111 = new Pair(Map.of("r1", true, "r2", true), Map.of("x", true));
        ts.addStates(s010, s100, s110, s011, s101, s111);
        ts.addInitialState(s000);
        ts.addInitialState(s001);
        ts.addTransition(new TSTransition(s000, Map.of("x", false),s000));
        ts.addTransition(new TSTransition(s000, Map.of("x", true),s001));
        ts.addTransition(new TSTransition(s001, Map.of("x", false),s010));
        ts.addTransition(new TSTransition(s001, Map.of("x", true),s011));
        ts.addTransition(new TSTransition(s010, Map.of("x", false),s010));
        ts.addTransition(new TSTransition(s010, Map.of("x", true),s011));
        ts.addTransition(new TSTransition(s011, Map.of("x", false),s100));
        ts.addTransition(new TSTransition(s011, Map.of("x", true),s101));
        ts.addTransition(new TSTransition(s100, Map.of("x", false),s100));
        ts.addTransition(new TSTransition(s100, Map.of("x", true),s101));
        ts.addTransition(new TSTransition(s110, Map.of("x", false),s110));
        ts.addTransition(new TSTransition(s110, Map.of("x", true),s111));
        ts.addTransition(new TSTransition(s111, Map.of("x", false),s000));
        ts.addTransition(new TSTransition(s111, Map.of("x", true),s001));
        ts.addTransition(new TSTransition(s101, Map.of("x", false),s110));
        ts.addTransition(new TSTransition(s101, Map.of("x", true),s111));
        ts.addToLabel(s010, "r2");
        ts.addToLabel(s100, "r1");
        ts.addToLabel(s110, "r1");
        ts.addToLabel(s110, "r2");
        ts.addToLabel(s001, "x");
        ts.addToLabel(s011, "x");
        ts.addToLabel(s011, "r2");
        ts.addToLabel(s101, "r1");
        ts.addToLabel(s101, "x");
        ts.addToLabel(s111, "y");
        ts.addToLabel(s111, "x");
        ts.addToLabel(s111, "r1");
        ts.addToLabel(s111, "r2");


        return new Pair(circuit, ts);

    }
    private interface pgAndts {
        public Pair<ProgramGraph, TransitionSystem> get();
    }

    private interface csAndts {
        public Pair<ChannelSystem, TransitionSystem> get();
    }
    private abstract class Channel{
        Object c;

        public Channel(Object c) {
            this.c = c;
        }

        public abstract boolean isInf();
        public abstract boolean isDataLess();
    }
    private class InfChannel extends Channel{
        public InfChannel(Object c) {
            super(c);
        }

        @Override
        public boolean isInf() {
            return true;
        }

        @Override
        public boolean isDataLess() {
            return false;
        }
    }
    private class DataLess extends Channel{
        public DataLess(Object c) {
            super(c);
        }

        @Override
        public boolean isInf() {
            return false;
        }

        @Override
        public boolean isDataLess() {
            return true;
        }
    }

    private abstract class ChannelAction {
        protected Channel c;

        public ChannelAction(Channel c) {
            this.c = c;
        }

        public abstract boolean canExecute(Map<Object, Object> ksi);
        public abstract Map<String, Object> exectue(Map<String, Object> ksi);
    }

    private class ASyncAction extends ChannelAction {

        public ASyncAction(Channel c) {
            super(c);
        }

        @Override
        public boolean canExecute(Map<Object, Object> ksi) {
            return ((LinkedList)ksi.get(c)).size()==0;
        }

        @Override
        public Map<String, Object> exectue(Map<String, Object> ksi) {
            return ksi;
        }
    }

    private class ReadAction extends ChannelAction {

        public ReadAction(Channel c) {
            super(c);
        }

        @Override
        public boolean canExecute(Map<Object, Object> ksi) {
            return !c.isDataLess() && ((LinkedList)ksi.get(c)).size()>0;
        }

        @Override
        public Map<String, Object> exectue(Map<String, Object> ksi) {
            ((LinkedList)ksi.get(c)).poll();
            return ksi;
        }
    }

    private class WriteAction extends ChannelAction {
        Object message;
        public WriteAction(Channel c, Object message) {
            super(c);
            this.message = message;
        }

        @Override
        public boolean canExecute(Map<Object, Object> ksi) {
            if (c.isInf()){
                return true;
            }else {
                return false;
            }
        }

        @Override
        public Map<String, Object> exectue(Map<String, Object> ksi) {
            ((LinkedList)ksi.get(c)).add(message);
            return ksi;
        }
    }
}