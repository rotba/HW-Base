package il.ac.bgu.cs.formalmethodsintro.base;

import java.io.InputStream;
import java.util.*;

import il.ac.bgu.cs.formalmethodsintro.base.automata.Automaton;
import il.ac.bgu.cs.formalmethodsintro.base.automata.MultiColorAutomaton;
import il.ac.bgu.cs.formalmethodsintro.base.channelsystem.ChannelSystem;
import il.ac.bgu.cs.formalmethodsintro.base.circuits.Circuit;
import il.ac.bgu.cs.formalmethodsintro.base.exceptions.StateNotFoundException;
import il.ac.bgu.cs.formalmethodsintro.base.ltl.LTL;
import il.ac.bgu.cs.formalmethodsintro.base.programgraph.ActionDef;
import il.ac.bgu.cs.formalmethodsintro.base.programgraph.ConditionDef;
import il.ac.bgu.cs.formalmethodsintro.base.programgraph.PGTransition;
import il.ac.bgu.cs.formalmethodsintro.base.programgraph.ProgramGraph;
import il.ac.bgu.cs.formalmethodsintro.base.transitionsystem.AlternatingSequence;
import il.ac.bgu.cs.formalmethodsintro.base.transitionsystem.TSTransition;
import il.ac.bgu.cs.formalmethodsintro.base.transitionsystem.TransitionSystem;
import il.ac.bgu.cs.formalmethodsintro.base.util.Pair;
import il.ac.bgu.cs.formalmethodsintro.base.verification.VerificationResult;
import org.antlr.runtime.ANTLRStringStream;

/**
 * Interface for the entry point class to the HW in this class. Our
 * client/testing code interfaces with the student solutions through this
 * interface only. <br>
 * More about facade: {@linkplain http://www.vincehuston.org/dp/facade.html}.
 */
public class FvmFacade {

    private static FvmFacade INSTANCE = null;

    /**
     *
     * @return an instance of this class.
     */
    public static FvmFacade get() {
        if (INSTANCE == null) {
            INSTANCE = new FvmFacade();
        }
        return INSTANCE;
    }

    public static FvmFacade createInstance() {
        return get();
    }

    /**
     * Checks whether a transition system is action deterministic. I.e., if for
     * any given p and α there exists only a single tuple (p,α,q) in →. Note
     * that this must be true even for non-reachable states.
     *
     * @param <S> Type of states.
     * @param <A> Type of actions.
     * @param <P> Type of atomic propositions.
     * @param ts The transition system being tested.
     * @return {@code true} iff the action is deterministic.
     */

    public <S, A, P> boolean isActionDeterministic(TransitionSystem<S, A, P> ts) {
        for(S s : ts.getStates()){
            for(A a : ts.getActions()){
                if(ts.getTransition(s, a).size() > 1)
                    return false;
            }
        }
        return true;
    }

    /**
     * Checks whether an action is ap-deterministic (as defined in class), in
     * the context of a given {@link TransitionSystem}.
     *
     * @param <S> Type of states.
     * @param <A> Type of actions.
     * @param <P> Type of atomic propositions.
     * @param ts The transition system being tested.
     * @return {@code true} iff the action is ap-deterministic.
     */
    public <S, A, P> boolean isAPDeterministic(TransitionSystem<S, A, P> ts) {
        for(S s : ts.getStates()){
            for(S sTag1 : post(ts,s)){
                for(S sTag2 : post(ts,s)){
                    if(!sTag1.equals(sTag2)){
                        if(ts.getLabel(sTag1).equals(ts.getLabel(sTag2))){
                            return false;
                        }
                    }
                }

            }
        }
        return true;
    }

    /**
     * Checks whether an alternating sequence is an execution of a
     * {@link TransitionSystem}, as defined in class.
     *
     * @param <S> Type of states.
     * @param <A> Type of actions.
     * @param <P> Type of atomic propositions.
     * @param ts The transition system being tested.
     * @param e The sequence that may or may not be an execution of {@code ts}.
     * @return {@code true} iff {@code e} is an execution of {@code ts}.
     */
    public <S, A, P> boolean isExecution(TransitionSystem<S, A, P> ts, AlternatingSequence<S, A> e) {
        if(!isExecutionFragment(ts, e))
            return false;
        if(!ts.getInitialStates().contains(e.head()))
            return false;
        Set<TSTransition<S, A>> trans = ts.getTransitions();
        for(TSTransition<S, A> tr : trans){
            if(tr.getFrom().equals(e.last()))
                return false;
        }
        return true;
    }

    /**
     * Checks whether an alternating sequence is an execution fragment of a
     * {@link TransitionSystem}, as defined in class.
     *
     * @param <S> Type of states.
     * @param <A> Type of actions.
     * @param <P> Type of atomic propositions.
     * @param ts The transition system being tested.
     * @param e The sequence that may or may not be an execution fragment of
     * {@code ts}.
     * @return {@code true} iff {@code e} is an execution fragment of
     * {@code ts}.
     */
    public <S, A, P> boolean isExecutionFragment(TransitionSystem<S, A, P> ts, AlternatingSequence<S, A> e) {
        Set<S> to_states_f = ts.getTransition(e.getStateAt(0), e.getActAt(1));
        if(!to_states_f.contains(e.getStateAt(2)))
            return false;
        for(int i = 2; i < e.size()-3; i+=2){
            Set<S> to_states = ts.getTransition(e.getStateAt(i), e.getActAt(i+1));
            if(!to_states.contains(e.getStateAt(i+2)))
                return false;
        }
        return true;
    }

    /**
     * Checks whether an alternating sequence is an initial execution fragment
     * of a {@link TransitionSystem}, as defined in class.
     *
     * @param <S> Type of states.
     * @param <A> Type of actions.
     * @param <P> Type of atomic propositions.
     * @param ts The transition system being tested.
     * @param e The sequence that may or may not be an initial execution
     * fragment of {@code ts}.
     * @return {@code true} iff {@code e} is an execution fragment of
     * {@code ts}.
     */
    public <S, A, P> boolean isInitialExecutionFragment(TransitionSystem<S, A, P> ts, AlternatingSequence<S, A> e) {
        if(isExecutionFragment(ts, e) && ts.getInitialStates().contains(e.head()))
            return true;
        return false;
    }

    /**
     * Checks whether an alternating sequence is a maximal execution fragment of
     * a {@link TransitionSystem}, as defined in class.
     *
     * @param <S> Type of states.
     * @param <A> Type of actions.
     * @param <P> Type of atomic propositions.
     * @param ts The transition system being tested.
     * @param e The sequence that may or may not be a maximal execution fragment
     * of {@code ts}.
     * @return {@code true} iff {@code e} is a maximal fragment of {@code ts}.
     */
    public <S, A, P> boolean isMaximalExecutionFragment(TransitionSystem<S, A, P> ts, AlternatingSequence<S, A> e) {
        Set<TSTransition<S, A>> trans = ts.getTransitions();
        for(TSTransition<S, A> tr : trans){
            if(tr.getFrom().equals(e.last()))
                return false;
        }
        if(isExecutionFragment(ts, e))
            return true;
        return false;
    }

    /**
     * Checks whether a state in {@code ts} is terminal.
     *
     * @param <S> Type of states.
     * @param <A> Type of actions.
     * @param ts Transition system of {@code s}.
     * @param s The state being tested for terminality.
     * @return {@code true} iff state {@code s} is terminal in {@code ts}.
     * @throws StateNotFoundException if {@code s} is not a state of {@code ts}.
     */
    public <S, A> boolean isStateTerminal(TransitionSystem<S, A, ?> ts, S s) {
        return post(ts,s).size()==0;
    }

    /**
     * @param <S> Type of states.
     * @param ts Transition system of {@code s}.
     * @param s A state in {@code ts}.
     * @return All the states in {@code Post(s)}, in the context of {@code ts}.
     * @throws StateNotFoundException if {@code s} is not a state of {@code ts}.
     */
    public <S> Set<S> post(TransitionSystem<S, ?, ?> ts, S s) {
        Set<S> ans = new HashSet<S>();
        for (TSTransition<S, ?> tran:ts.getTransitions()) {
            if(tran.getFrom().equals(s)){
                ans.add(tran.getTo());
            }
        }
        return ans;
    }

    /**
     * @param <S> Type of states.
     * @param ts Transition system of {@code s}.
     * @param c States in {@code ts}.
     * @return All the states in {@code Post(s)} where {@code s} is a member of
     * {@code c}, in the context of {@code ts}.
     * @throws StateNotFoundException if {@code s} is not a state of {@code ts}.
     */
    public <S> Set<S> post(TransitionSystem<S, ?, ?> ts, Set<S> c) {
        Set<S> ans = new HashSet<>();
        for (S s: c) {
            for (S sTag: post(ts, s)) {
                ans.add(sTag);
            }
        }
        return ans;
    }

    /**
     * @param <S> Type of states.
     * @param <A> Type of actions.
     * @param ts Transition system of {@code s}.
     * @param s A state in {@code ts}.
     * @param a An action.
     * @return All the states that {@code ts} might transition to from
     * {@code s}, when action {@code a} is selected.
     * @throws StateNotFoundException if {@code s} is not a state of {@code ts}.
     */
    public <S, A> Set<S> post(TransitionSystem<S, A, ?> ts, S s, A a) {
        return ts.getTransition(s,a);
    }

    /**
     * @param <S> Type of states.
     * @param <A> Type of actions.
     * @param ts Transition system of {@code s}.
     * @param c Set of states in {@code ts}.
     * @param a An action.
     * @return All the states that {@code ts} might transition to from any state
     * in {@code c}, when action {@code a} is selected.
     */
    public <S, A> Set<S> post(TransitionSystem<S, A, ?> ts, Set<S> c, A a) {
        Set<S> ans = new HashSet<>();
        for (S s:c) {
            for (S sTag: post(ts,s,a)) {
                ans.add(sTag);
            }
        }
        return ans;
    }

    /**
     * @param <S> Type of states.
     * @param ts Transition system of {@code s}.
     * @param s A state in {@code ts}.
     * @return All the states in {@code Pre(s)}, in the context of {@code ts}.
     */
    public <S> Set<S> pre(TransitionSystem<S, ?, ?> ts, S s) {
        Set<S> ans = new HashSet<S>();
        for (TSTransition<S, ?> tran:ts.getTransitions()) {
            if(tran.getTo().equals(s)){
                ans.add(tran.getFrom());
            }
        }
        return ans;
    }

    /**
     * @param <S> Type of states.
     * @param ts Transition system of {@code s}.
     * @param c States in {@code ts}.
     * @return All the states in {@code Pre(s)} where {@code s} is a member of
     * {@code c}, in the context of {@code ts}.
     * @throws StateNotFoundException if {@code s} is not a state of {@code ts}.
     */
    public <S> Set<S> pre(TransitionSystem<S, ?, ?> ts, Set<S> c) {
        Set<S> ans = new HashSet<>();
        for (S s: c) {
            for (S sTag: pre(ts, s)) {
                ans.add(sTag);
            }
        }
        return ans;
    }

    /**
     * @param <S> Type of states.
     * @param <A> Type of actions.
     * @param ts Transition system of {@code s}.
     * @param s A state in {@code ts}.
     * @param a An action.
     * @return All the states that {@code ts} might transitioned from, when in
     * {@code s}, and the last action was {@code a}.
     * @throws StateNotFoundException if {@code s} is not a state of {@code ts}.
     */
    public <S, A> Set<S> pre(TransitionSystem<S, A, ?> ts, S s, A a) {
        Set<S> ans = new HashSet<>();
        for (S sTag: ts.getStates()) {
            if(post(ts,sTag,a).contains(s))
                ans.add(s);
        }
        return ans;
    }

    /**
     * @param <S> Type of states.
     * @param <A> Type of actions.
     * @param ts Transition system of {@code s}.
     * @param c Set of states in {@code ts}.
     * @param a An action.
     * @return All the states that {@code ts} might transitioned from, when in
     * any state in {@code c}, and the last action was {@code a}.
     * @throws StateNotFoundException if {@code s} is not a state of {@code ts}.
     */
    public <S, A> Set<S> pre(TransitionSystem<S, A, ?> ts, Set<S> c, A a) {
        Set<S> ans = new HashSet<>();
        for (S s: c) {
            for (S sTag: pre(ts,s,a)) {
                ans.add(sTag);
            }
        }
        return ans;
    }

    /**
     * Implements the {@code reach(TS)} function.
     *
     * @param <S> Type of states.
     * @param <A> Type of actions.
     * @param ts Transition system of {@code s}.
     * @return All states reachable in {@code ts}.
     */
    public <S, A> Set<S> reach(TransitionSystem<S, A, ?> ts) {
        Set<S> reachable = new HashSet<>();
        for(TSTransition<S, A> tr : ts.getTransitions()){
            reachable.add(tr.getTo());
            if(ts.getInitialStates().contains(tr.getFrom()))
                reachable.add(tr.getFrom());
        }
        return reachable;
    }

    /**
     * Compute the synchronous product of two transition systems.
     *
     * @param <S1> Type of states in the first system.
     * @param <S2> Type of states in the first system.
     * @param <A> Type of actions (in both systems).
     * @param <P> Type of atomic propositions (in both systems).
     * @param ts1 The first transition system.
     * @param ts2 The second transition system.
     *
     * @return A transition system that represents the product of the two.
     */
    public <S1, S2, A, P> TransitionSystem<Pair<S1, S2>, A, P> interleave(TransitionSystem<S1, A, P> ts1,
            TransitionSystem<S2, A, P> ts2) {
        TransitionSystem<Pair<S1, S2>, A, P> interleaved = new TransitionSystem();
        // create new states and their labels
        for(S1 state1 : ts1.getStates()) {
            for (S2 state2 : ts2.getStates()) {
                Pair<S1, S2> new_state = new Pair<S1, S2>(state1, state2);
                interleaved.addState(new_state);
                for(P ap : ts1.getLabel(state1))
                    interleaved.addToLabel(new_state, ap);
                for(P ap : ts2.getLabel(state2))
                    interleaved.addToLabel(new_state, ap);
                if(ts1.getInitialStates().contains(state1) && ts2.getInitialStates().contains(state2))
                    // determine if they are initial
                    interleaved.addInitialState(new_state);
            }
        }
        interleaved.addAllActions(ts1.getActions());
        interleaved.addAllActions(ts2.getActions());
        interleaved.addAllAtomicPropositions(ts1.getAtomicPropositions());
        interleaved.addAllAtomicPropositions(ts2.getAtomicPropositions());

        for(TSTransition<S1, A> transition : ts1.getTransitions())
            for(Pair<S1,S2> new_state : interleaved.getStates())
                if(new_state.first.equals(transition.getFrom()))
                    for(Pair<S1,S2> to_state : interleaved.getStates())
                        if(to_state.first.equals(transition.getTo()) && to_state.second.equals(new_state.second))
                            interleaved.addTransition(new TSTransition<>(new_state, transition.getAction(), to_state));

        for(TSTransition<S2, A> transition : ts2.getTransitions())
            for(Pair<S1,S2> new_state : interleaved.getStates())
                if(new_state.second.equals(transition.getFrom()))
                    for(Pair<S1,S2> to_state : interleaved.getStates())
                        if(to_state.second.equals(transition.getTo()) && to_state.first.equals(new_state.first))
                            interleaved.addTransition(new TSTransition<>(new_state, transition.getAction(), to_state));

        return interleaved;
    }

    /**
     * Compute the synchronous product of two transition systems.
     *
     * @param <S1> Type of states in the first system.
     * @param <S2> Type of states in the first system.
     * @param <A> Type of actions (in both systems).
     * @param <P> Type of atomic propositions (in both systems).
     * @param ts1 The first transition system.
     * @param ts2 The second transition system.
     * @param handShakingActions Set of actions both systems perform together.
     *
     * @return A transition system that represents the product of the two.
     */
    public <S1, S2, A, P> TransitionSystem<Pair<S1, S2>, A, P> interleave(TransitionSystem<S1, A, P> ts1,
            TransitionSystem<S2, A, P> ts2, Set<A> handShakingActions) {
        TransitionSystem<Pair<S1, S2>, A, P> interleaved = new TransitionSystem();
        for(S1 state1 : ts1.getStates()) {
            for (S2 state2 : ts2.getStates()) {
                Pair<S1, S2> new_state = new Pair<S1, S2>(state1, state2);
                interleaved.addState(new_state);
                for(P ap : ts1.getLabel(state1)){
                    interleaved.addToLabel(new_state, ap);
                }
                for(P ap : ts2.getLabel(state2)){
                    interleaved.addToLabel(new_state, ap);
                }
                if(ts1.getInitialStates().contains(state1) && ts2.getInitialStates().contains(state2)) {
                    interleaved.addInitialState(new_state);
                }
            }
        }
        interleaved.addAllActions(ts1.getActions());
        interleaved.addAllActions(ts2.getActions());
        interleaved.addAllAtomicPropositions(ts1.getAtomicPropositions());
        interleaved.addAllAtomicPropositions(ts2.getAtomicPropositions());
        for(TSTransition<S1, A> transition : ts1.getTransitions()){
            for(Pair<S1,S2> new_state : interleaved.getStates()){
                if(new_state.first.equals(transition.getFrom())  && !handShakingActions.contains(transition.getAction())){
                    for(Pair<S1,S2> to_state : interleaved.getStates()){
                        if(to_state.first.equals(transition.getTo()))
                                interleaved.addTransition(new TSTransition<>(new_state, transition.getAction(), to_state));
                    }
                }
            }
        }
        for(TSTransition<S2, A> transition : ts2.getTransitions()){
            for(Pair<S1,S2> new_state : interleaved.getStates()){
                if(new_state.second.equals(transition.getFrom()) && !handShakingActions.contains(transition.getAction())){
                    for(Pair<S1,S2> to_state : interleaved.getStates()){
                        if(to_state.second.equals(transition.getTo()))
                                interleaved.addTransition(new TSTransition<>(new_state, transition.getAction(), to_state));
                    }
                }
            }
        }
        for(A action : handShakingActions){
            List<S1> orig1 = new LinkedList<>();
            List<S2> orig2 = new LinkedList<>();
            Set<S1> post1 = new HashSet<>();
            Set<S2> post2 = new HashSet<>();
            for(S1 state1 : ts1.getStates()){
                post1 = post(ts1, state1, action);
                if(post1.size() > 0)
                    orig1.add(state1);
            }
            for(S2 state2 : ts2.getStates()){
                post2 = post(ts2, state2, action);
                if(post2.size() > 0)
                    orig2.add(state2);
            }
            for(Pair<S1,S2> new_state : interleaved.getStates()){
                if(orig1.contains(new_state.first) && orig2.contains(new_state.second)){
                    for(Pair<S1,S2> to_state : interleaved.getStates()){
                        if(post1.contains(to_state.first) && post2.contains(to_state.second))
                            interleaved.addTransition(new TSTransition<>(new_state, action, to_state));
                    }
                }
            }
            }
        return interleaved;
    }

    /**
     * Creates a new {@link ProgramGraph} object.
     *
     * @param <L> Type of locations in the graph.
     * @param <A> Type of actions of the graph.
     * @return A new program graph instance.
     */
    public <L, A> ProgramGraph<L, A> createProgramGraph() {
        return new ProgramGraph<>();
    }

    /**
     * Interleaves two program graphs.
     *
     * @param <L1> Type of locations in the first graph.
     * @param <L2> Type of locations in the second graph.
     * @param <A> Type of actions in BOTH GRAPHS.
     * @param pg1 The first program graph.
     * @param pg2 The second program graph.
     * @return Interleaved program graph.
     */
    // TODO: 12/12/2019 : effect function
    public <L1, L2, A> ProgramGraph<Pair<L1, L2>, A> interleave(ProgramGraph<L1, A> pg1, ProgramGraph<L2, A> pg2) {
        ProgramGraph<Pair<L1, L2>, A> interleaved = createProgramGraph();
        for(L1 loc1 : pg1.getLocations()) {
            for (L2 loc2 : pg2.getLocations()) {
                Pair<L1, L2> new_loc = new Pair<L1,L2>(loc1, loc2);
                if(pg1.getInitialLocations().contains(loc1) && pg2.getInitialLocations().contains(loc2))
                    interleaved.setInitial(new_loc, true);
                else
                    interleaved.addLocation(new_loc);

            }
        }
        Set<List<String>> both_inits= new HashSet<>(pg1.getInitalizations());
        both_inits.addAll(pg2.getInitalizations());
        for(List<String> init_list : both_inits)
            interleaved.addInitalization(init_list);
        for(PGTransition<L1,A> trans : pg1.getTransitions())
            for(Pair<L1,L2> from_loc : interleaved.getLocations())
                if(from_loc.first.equals(trans.getTo()))
                    for(Pair<L1,L2> to_loc : interleaved.getLocations())
                        if(to_loc.first.equals(trans.getFrom()))
                            interleaved.addTransition(new PGTransition<>(from_loc, trans.getCondition(), trans.getAction(),to_loc));
        for(PGTransition<L2,A> trans : pg2.getTransitions())
            for(Pair<L1,L2> from_loc : interleaved.getLocations())
                if(from_loc.second.equals(trans.getTo()))
                    for(Pair<L1,L2> to_loc : interleaved.getLocations())
                        if(to_loc.second.equals(trans.getFrom()))
                            interleaved.addTransition(new PGTransition<>(from_loc, trans.getCondition(), trans.getAction(),to_loc));

        return interleaved;
    }

    /**
     * Creates a {@link TransitionSystem} representing the passed circuit.
     *
     * @param c The circuit to translate into a {@link TransitionSystem}.
     * @return A {@link TransitionSystem} representing {@code c}.
     */
    public TransitionSystem<Pair<Map<String, Boolean>, Map<String, Boolean>>, Map<String, Boolean>, Object> transitionSystemFromCircuit(
            Circuit c) {
        TransitionSystem<Pair<Map<String, Boolean>, Map<String, Boolean>>, Map<String, Boolean>, Object> ts = new TransitionSystem<>();
        for(String in : c.getInputPortNames()){
            for(String reg : c.getRegisterNames()){
                Pair<Map<String, Boolean>, Map<String, Boolean>> intr = new Pair<>(Map.of(reg, false), Map.of(in, true));
                ts.addInitialState(intr);
                ts.addToLabel(intr,in);
                Pair<Map<String, Boolean>, Map<String, Boolean>> infl = new Pair<>(Map.of(reg, false), Map.of(in, false));
                ts.addInitialState(infl);
                ts.addToLabel(intr,in);
                Pair<Map<String, Boolean>, Map<String, Boolean>> refl = new Pair<>(Map.of(reg, true), Map.of(in, false));
                ts.addState(refl);
                ts.addToLabel(refl,reg);
                Pair<Map<String, Boolean>, Map<String, Boolean>> retr = new Pair<>(Map.of(reg, true), Map.of(in, true));
                ts.addState(retr);
                ts.addToLabel(retr,reg);
                ts.addToLabel(retr,in);
            }
            ts.addAction(Map.of(in, false));
            ts.addAction(Map.of(in, true));
        }
        ts.addAllAtomicPropositions(c.getOutputPortNames().toArray());
        ts.addAllAtomicPropositions(c.getInputPortNames().toArray());
        ts.addAllAtomicPropositions(c.getRegisterNames().toArray());
        for(Pair<Map<String, Boolean>, Map<String, Boolean>> new_state: ts.getStates()){
            ts.addTransition(new TSTransition(new_state, new_state.first, c.updateRegisters(new_state.first, new_state.second)));
            Map<String, Boolean> out = c.computeOutputs(new_state.first, new_state.second);
            for(String out_port : c.getOutputPortNames()){
                if(out.get(out_port))
                    ts.addToLabel(new_state, out_port);
            }
        }
        return ts;
    }

    /**
     * Creates a {@link TransitionSystem} from a program graph.
     *
     * @param <L> Type of program graph locations.
     * @param <A> Type of program graph actions.
     * @param pg The program graph to be translated into a transition system.
     * @param actionDefs Defines the effect of each action.
     * @param conditionDefs Defines the conditions (guards) of the program
     * graph.
     * @return A transition system representing {@code pg}.
     */
    public <L, A> TransitionSystem<Pair<L, Map<String, Object>>, A, String> transitionSystemFromProgramGraph(
            ProgramGraph<L, A> pg, Set<ActionDef> actionDefs, Set<ConditionDef> conditionDefs) {
        TransitionSystem<Pair<L, Map<String, Object>>,A, String> ts = createTransitionSystem();
        for (L initLoc:pg.getInitialLocations()) {
            for (List<String> init: pg.getInitalizations()) {
                for (String initialDef: init) {
                    for (ActionDef ad: actionDefs) {
                        if (ad.isMatchingAction(initialDef)){
                            ts.addInitialState(new Pair<>(initLoc, ad.effect(new HashMap<>(),initialDef)));
                        }
                    }
                }
            }

        }

        for (Pair<L, Map<String, Object>> initialState: ts.getInitialStates()) {
            for (Pair<L, Map<String, Object>> reachable:
                    lazyGetReachables(initialState,pg,actionDefs,conditionDefs )) {
                ts.addState(reachable);
            }
        }
        return null;
    }

    private <L, A>  Set<Pair<L, Map<String, Object>>> lazyGetReachables(Pair<L, Map<String, Object>> currState, ProgramGraph<L,A> pg, Set<ActionDef> actionDefs, Set<ConditionDef> conditionDefs) {
        HashSet<Pair<L, Map<String, Object>>> ans = new HashSet<>();
        for (PGTransition tran: pg.getTransitions()) {
            if(tran.getFrom().equals(currState.getFirst())){
                for (ConditionDef cd: conditionDefs) {
                    if(cd.evaluate(currState.getSecond(), tran.getCondition())){
                        for (ActionDef ad: actionDefs) {
                            if(ad.isMatchingAction(tran.getAction())){
                                Pair<L, Map<String, Object>> newState = new Pair(tran.getTo(), ad.effect(currState.getSecond(), tran.getAction()));
                                ans.add(newState);
                                for (Pair<L, Map<String, Object>> reachable: lazyGetReachables(newState,pg,actionDefs,conditionDefs)) {
                                    ans.add(reachable);
                                }
                            }
                        }
                    }
                }
            }
        }
        return ans;
    }

    /**
     * Creates a transition system representing channel system {@code cs}.
     *
     * @param <L> Type of locations in the channel system.
     * @param <A> Type of actions in the channel system.
     * @param cs The channel system to be translated into a transition system.
     * @return A transition system representing {@code cs}.
     */
    public <L, A> TransitionSystem<Pair<List<L>, Map<String, Object>>, A, String> transitionSystemFromChannelSystem(
            ChannelSystem<L, A> cs) {
        throw new java.lang.UnsupportedOperationException();
    }

    /**
     * Construct a program graph from nanopromela code.
     *
     * @param filename The nanopromela code.
     * @return A program graph for the given code.
     * @throws Exception If the code is invalid.
     */
    public ProgramGraph<String, String> programGraphFromNanoPromela(String filename) throws Exception {
        throw new java.lang.UnsupportedOperationException();
    }

    /**
     * Construct a program graph from nanopromela code.
     *
     * @param nanopromela The nanopromela code.
     * @return A program graph for the given code.
     * @throws Exception If the code is invalid.
     */
    public ProgramGraph<String, String> programGraphFromNanoPromelaString(String nanopromela) throws Exception {
        throw new java.lang.UnsupportedOperationException();
    }

    /**
     * Construct a program graph from nanopromela code.
     *
     * @param inputStream The nanopromela code.
     * @return A program graph for the given code.
     * @throws Exception If the code is invalid.
     */
    public ProgramGraph<String, String> programGraphFromNanoPromela(InputStream inputStream) throws Exception {
        throw new java.lang.UnsupportedOperationException();
    }

    /**
     * Creates a transition system from a transition system and an automaton.
     *
     * @param <Sts> Type of states in the transition system.
     * @param <Saut> Type of states in the automaton.
     * @param <A> Type of actions in the transition system.
     * @param <P> Type of atomic propositions in the transition system, which is
     * also the type of the automaton alphabet.
     * @param ts The transition system.
     * @param aut The automaton.
     * @return The product of {@code ts} with {@code aut}.
     */
    public <Sts, Saut, A, P> TransitionSystem<Pair<Sts, Saut>, A, Saut> product(TransitionSystem<Sts, A, P> ts,
            Automaton<Saut, P> aut) {
        throw new java.lang.UnsupportedOperationException();
    }

    /**
     * Verify that a system satisfies an omega regular property.
     *
     * @param <S> Type of states in the transition system.
     * @param <Saut> Type of states in the automaton.
     * @param <A> Type of actions in the transition system.
     * @param <P> Type of atomic propositions in the transition system, which is
     * also the type of the automaton alphabet.
     * @param ts The transition system.
     * @param aut A Büchi automaton for the words that do not satisfy the
     * property.
     * @return A VerificationSucceeded object or a VerificationFailed object
     * with a counterexample.
     */
    public <S, A, P, Saut> VerificationResult<S> verifyAnOmegaRegularProperty(TransitionSystem<S, A, P> ts,
            Automaton<Saut, P> aut) {
        throw new java.lang.UnsupportedOperationException();
    }

    /**
     * Translation of Linear Temporal Logic (LTL) formula to a Nondeterministic
     * Büchi Automaton (NBA).
     *
     * @param <L> Type of resultant automaton transition alphabet
     * @param ltl The LTL formula represented as a parse-tree.
     * @return An automaton A such that L_\omega(A)=Words(ltl)
     */
    public <L> Automaton<?, L> LTL2NBA(LTL<L> ltl) {
        throw new java.lang.UnsupportedOperationException();
    }

    /**
     * A translation of a Generalized Büchi Automaton (GNBA) to a
     * Nondeterministic Büchi Automaton (NBA).
     *
     * @param <L> Type of resultant automaton transition alphabet
     * @param mulAut An automaton with a set of accepting states (colors).
     * @return An equivalent automaton with a single set of accepting states.
     */
    public <L> Automaton<?, L> GNBA2NBA(MultiColorAutomaton<?, L> mulAut) {
        throw new java.lang.UnsupportedOperationException();
    }

    public <S, A, AP>TransitionSystem<S, A, AP> createTransitionSystem() {
        return new TransitionSystem<>();
    }
}
