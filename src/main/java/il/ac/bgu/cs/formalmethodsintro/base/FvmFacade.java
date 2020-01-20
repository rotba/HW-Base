package il.ac.bgu.cs.formalmethodsintro.base;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import il.ac.bgu.cs.formalmethodsintro.base.automata.Automaton;
import il.ac.bgu.cs.formalmethodsintro.base.automata.MultiColorAutomaton;
import il.ac.bgu.cs.formalmethodsintro.base.channelsystem.ChannelSystem;
import il.ac.bgu.cs.formalmethodsintro.base.channelsystem.InterleavingActDef;
import il.ac.bgu.cs.formalmethodsintro.base.channelsystem.ParserBasedInterleavingActDef;
import il.ac.bgu.cs.formalmethodsintro.base.circuits.Circuit;
import il.ac.bgu.cs.formalmethodsintro.base.exceptions.StateNotFoundException;
import il.ac.bgu.cs.formalmethodsintro.base.goal.GoalStructure;
import il.ac.bgu.cs.formalmethodsintro.base.ltl.AP;
import il.ac.bgu.cs.formalmethodsintro.base.ltl.LTL;
import il.ac.bgu.cs.formalmethodsintro.base.ltl_wrapper.LTLWrapper;
import il.ac.bgu.cs.formalmethodsintro.base.nanopromela.NanoPromelaBaseListener;
import il.ac.bgu.cs.formalmethodsintro.base.nanopromela.NanoPromelaFileReader;
import il.ac.bgu.cs.formalmethodsintro.base.nanopromela.NanoPromelaParser;
import il.ac.bgu.cs.formalmethodsintro.base.programgraph.ActionDef;
import il.ac.bgu.cs.formalmethodsintro.base.programgraph.ConditionDef;
import il.ac.bgu.cs.formalmethodsintro.base.programgraph.PGTransition;
import il.ac.bgu.cs.formalmethodsintro.base.programgraph.ProgramGraph;
import il.ac.bgu.cs.formalmethodsintro.base.transitionsystem.AlternatingSequence;
import il.ac.bgu.cs.formalmethodsintro.base.transitionsystem.TSTransition;
import il.ac.bgu.cs.formalmethodsintro.base.transitionsystem.TransitionSystem;
import il.ac.bgu.cs.formalmethodsintro.base.util.GraphvizPainter;
import il.ac.bgu.cs.formalmethodsintro.base.util.Pair;
import il.ac.bgu.cs.formalmethodsintro.base.util.Util;
import il.ac.bgu.cs.formalmethodsintro.base.verification.VeficationSucceeded;
import il.ac.bgu.cs.formalmethodsintro.base.verification.VerificationFailed;
import il.ac.bgu.cs.formalmethodsintro.base.verification.VerificationResult;
import il.ac.bgu.cs.formalmethodsintro.base.verification.VerificationSucceeded;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.misc.Utils;
import org.svvrl.goal.core.util.Sets;

/**
 * Interface for the entry point class to the HW in this class. Our
 * client/testing code interfaces with the student solutions through this
 * interface only. <br>
 * More about facade: {@linkplain http://www.vincehuston.org/dp/facade.html}.
 */
public class FvmFacade {

    private static FvmFacade INSTANCE = null;

    /**
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
     * @param ts  The transition system being tested.
     * @return {@code true} iff the action is deterministic.
     */

    public <S, A, P> boolean isActionDeterministic(TransitionSystem<S, A, P> ts) {
        if (ts.getInitialStates().size() > 1)
            return false;
        for (S s : ts.getStates()) {
            for (A a : ts.getActions()) {
                // getTransition returns all the successors of s considering action a
                if (ts.getTransition(s, a).size() > 1)
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
     * @param ts  The transition system being tested.
     * @return {@code true} iff the action is ap-deterministic.
     */
    public <S, A, P> boolean isAPDeterministic(TransitionSystem<S, A, P> ts) {
        if (ts.getInitialStates().size() > 1)
            return false;
        for (S s : ts.getStates()) {
            for (S sTag1 : post(ts, s)) {
                for (S sTag2 : post(ts, s)) {
                    if (!sTag1.equals(sTag2)) {
                        if (ts.getLabel(sTag1).equals(ts.getLabel(sTag2))) {
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
     * @param ts  The transition system being tested.
     * @param e   The sequence that may or may not be an execution of {@code ts}.
     * @return {@code true} iff {@code e} is an execution of {@code ts}.
     */
    public <S, A, P> boolean isExecution(TransitionSystem<S, A, P> ts, AlternatingSequence<S, A> e) {
        if (!isMaximalExecutionFragment(ts, e))
            return false;
        return ts.getInitialStates().contains(e.head());
    }

    /**
     * Checks whether an alternating sequence is an execution fragment of a
     * {@link TransitionSystem}, as defined in class.
     *
     * @param <S> Type of states.
     * @param <A> Type of actions.
     * @param <P> Type of atomic propositions.
     * @param ts  The transition system being tested.
     * @param e   The sequence that may or may not be an execution fragment of
     *            {@code ts}.
     * @return {@code true} iff {@code e} is an execution fragment of
     * {@code ts}.
     */
    public <S, A, P> boolean isExecutionFragment(TransitionSystem<S, A, P> ts, AlternatingSequence<S, A> e) {
        if (e.size() % 2 == 0)
            return false;
        else if (e.size() == 1) {
            return ts.getStates().contains(e.head());
        }
        // check if first transition is valid
        Set<S> to_states_f = ts.getTransition(e.getStateAt(0), e.getActAt(1));
        if (!to_states_f.contains(e.getStateAt(2)))
            return false;
        //check for the rest of the transitions if they are valid
        for (int i = 2; i < e.size() - 3; i += 2) {
            Set<S> to_states = ts.getTransition(e.getStateAt(i), e.getActAt(i + 1));
            if (!to_states.contains(e.getStateAt(i + 2)))
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
     * @param ts  The transition system being tested.
     * @param e   The sequence that may or may not be an initial execution
     *            fragment of {@code ts}.
     * @return {@code true} iff {@code e} is an execution fragment of
     * {@code ts}.
     */
    public <S, A, P> boolean isInitialExecutionFragment(TransitionSystem<S, A, P> ts, AlternatingSequence<S, A> e) {
        return (isExecutionFragment(ts, e) && ts.getInitialStates().contains(e.head()));
    }

    /**
     * Checks whether an alternating sequence is a maximal execution fragment of
     * a {@link TransitionSystem}, as defined in class.
     *
     * @param <S> Type of states.
     * @param <A> Type of actions.
     * @param <P> Type of atomic propositions.
     * @param ts  The transition system being tested.
     * @param e   The sequence that may or may not be a maximal execution fragment
     *            of {@code ts}.
     * @return {@code true} iff {@code e} is a maximal fragment of {@code ts}.
     */
    public <S, A, P> boolean isMaximalExecutionFragment(TransitionSystem<S, A, P> ts, AlternatingSequence<S, A> e) {
        Set<TSTransition<S, A>> trans = ts.getTransitions();
        // check if there are transitions in ts that include e's last state as it predecessor
        for (TSTransition<S, A> tr : trans) {
            if (tr.getFrom().equals(e.last()))
                return false;
        }
        return (isExecutionFragment(ts, e));
    }

    /**
     * Checks whether a state in {@code ts} is terminal.
     *
     * @param <S> Type of states.
     * @param <A> Type of actions.
     * @param ts  Transition system of {@code s}.
     * @param s   The state being tested for terminality.
     * @return {@code true} iff state {@code s} is terminal in {@code ts}.
     * @throws StateNotFoundException if {@code s} is not a state of {@code ts}.
     */
    public <S, A> boolean isStateTerminal(TransitionSystem<S, A, ?> ts, S s) {
        return post(ts, s).size() == 0;
    }

    /**
     * @param <S> Type of states.
     * @param ts  Transition system of {@code s}.
     * @param s   A state in {@code ts}.
     * @return All the states in {@code Post(s)}, in the context of {@code ts}.
     * @throws StateNotFoundException if {@code s} is not a state of {@code ts}.
     */
    public <S> Set<S> post(TransitionSystem<S, ?, ?> ts, S s) {
        if (!ts.getStates().contains(s))
            throw new StateNotFoundException("s is not a state of ts");
        Set<S> ans = new HashSet<S>();
        for (TSTransition<S, ?> tran : ts.getTransitions()) {
            if (tran.getFrom().equals(s)) {
                ans.add(tran.getTo());
            }
        }
        return ans;
    }

    /**
     * @param <S> Type of states.
     * @param ts  Transition system of {@code s}.
     * @param c   States in {@code ts}.
     * @return All the states in {@code Post(s)} where {@code s} is a member of
     * {@code c}, in the context of {@code ts}.
     * @throws StateNotFoundException if {@code s} is not a state of {@code ts}.
     */
    public <S> Set<S> post(TransitionSystem<S, ?, ?> ts, Set<S> c) {
        Set<S> ans = new HashSet<>();
        for (S s : c) {
            for (S sTag : post(ts, s)) {
                ans.add(sTag);
            }
        }
        return ans;
    }

    /**
     * @param <S> Type of states.
     * @param <A> Type of actions.
     * @param ts  Transition system of {@code s}.
     * @param s   A state in {@code ts}.
     * @param a   An action.
     * @return All the states that {@code ts} might transition to from
     * {@code s}, when action {@code a} is selected.
     * @throws StateNotFoundException if {@code s} is not a state of {@code ts}.
     */
    public <S, A> Set<S> post(TransitionSystem<S, A, ?> ts, S s, A a) {
        if (!ts.getStates().contains(s))
            throw new StateNotFoundException("s is not a state of ts");
        return ts.getTransition(s, a);
    }

    /**
     * @param <S> Type of states.
     * @param <A> Type of actions.
     * @param ts  Transition system of {@code s}.
     * @param c   Set of states in {@code ts}.
     * @param a   An action.
     * @return All the states that {@code ts} might transition to from any state
     * in {@code c}, when action {@code a} is selected.
     */
    public <S, A> Set<S> post(TransitionSystem<S, A, ?> ts, Set<S> c, A a) {
        Set<S> ans = new HashSet<>();
        for (S s : c) {
            try {
                for (S sTag : post(ts, s, a)) {
                    ans.add(sTag);
                }
            } catch (StateNotFoundException e) {
            }
        }
        return ans;
    }

    /**
     * @param <S> Type of states.
     * @param ts  Transition system of {@code s}.
     * @param s   A state in {@code ts}.
     * @return All the states in {@code Pre(s)}, in the context of {@code ts}.
     */
    public <S> Set<S> pre(TransitionSystem<S, ?, ?> ts, S s) {
        Set<S> ans = new HashSet<S>();
        for (TSTransition<S, ?> tran : ts.getTransitions()) {
            if (tran.getTo().equals(s)) {
                ans.add(tran.getFrom());
            }
        }
        return ans;
    }

    /**
     * @param <S> Type of states.
     * @param ts  Transition system of {@code s}.
     * @param c   States in {@code ts}.
     * @return All the states in {@code Pre(s)} where {@code s} is a member of
     * {@code c}, in the context of {@code ts}.
     * @throws StateNotFoundException if {@code s} is not a state of {@code ts}.
     */
    public <S> Set<S> pre(TransitionSystem<S, ?, ?> ts, Set<S> c) {
        Set<S> ans = new HashSet<>();
        for (S s : c) {
            if (!ts.getStates().contains(s))
                throw new StateNotFoundException("s is not a state of ts");
            for (S sTag : pre(ts, s)) {
                ans.add(sTag);
            }
        }
        return ans;
    }

    /**
     * @param <S> Type of states.
     * @param <A> Type of actions.
     * @param ts  Transition system of {@code s}.
     * @param s   A state in {@code ts}.
     * @param a   An action.
     * @return All the states that {@code ts} might transitioned from, when in
     * {@code s}, and the last action was {@code a}.
     * @throws StateNotFoundException if {@code s} is not a state of {@code ts}.
     */
    public <S, A> Set<S> pre(TransitionSystem<S, A, ?> ts, S s, A a) {
        if (!ts.getStates().contains(s))
            throw new StateNotFoundException("s is not a state of ts");
        Set<S> ans = new HashSet<>();
        for (S sTag : ts.getStates()) {
            if (post(ts, sTag, a).contains(s))
                ans.add(sTag);
        }
        return ans;
    }

    /**
     * @param <S> Type of states.
     * @param <A> Type of actions.
     * @param ts  Transition system of {@code s}.
     * @param c   Set of states in {@code ts}.
     * @param a   An action.
     * @return All the states that {@code ts} might transitioned from, when in
     * any state in {@code c}, and the last action was {@code a}.
     * @throws StateNotFoundException if {@code s} is not a state of {@code ts}.
     */
    public <S, A> Set<S> pre(TransitionSystem<S, A, ?> ts, Set<S> c, A a) {
        Set<S> ans = new HashSet<>();
        for (S s : c) {
            for (S sTag : pre(ts, s, a)) {
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
     * @param ts  Transition system of {@code s}.
     * @return All states reachable in {@code ts}.
     */
    public <S, A> Set<S> reach(TransitionSystem<S, A, ?> ts) {
        Set<S> reachable = new HashSet<>();
        for (S initial_state : ts.getInitialStates()) {
            reachable.add(initial_state);
            reachable = rec_reach(ts, initial_state, reachable);
        }
        return reachable;
    }

    private <S, A> Set<S> rec_reach(TransitionSystem<S, A, ?> ts, S s, Set<S> reachable) {
        for (S post_state : post(ts, s)) {
            if (!reachable.contains(post_state)) {
                reachable.add(post_state);
                reachable = rec_reach(ts, post_state, reachable);
            }
        }
        return reachable;
    }

    private <S1, S2, A, P> TransitionSystem<Pair<S1, S2>, A, P> createInterleavedBase(TransitionSystem<S1, A, P> ts1,
                                                                                      TransitionSystem<S2, A, P> ts2, Set<A> handShakingActions) {
        TransitionSystem<Pair<S1, S2>, A, P> interleaved = new TransitionSystem();
        // create new states and their labels
        for (S1 state1 : ts1.getStates()) {
            for (S2 state2 : ts2.getStates()) {
                Pair<S1, S2> new_state = new Pair<S1, S2>(state1, state2);
                interleaved.addState(new_state);
                for (P ap : ts1.getLabel(state1))
                    interleaved.addToLabel(new_state, ap);
                for (P ap : ts2.getLabel(state2))
                    interleaved.addToLabel(new_state, ap);
                if (ts1.getInitialStates().contains(state1) && ts2.getInitialStates().contains(state2))
                    // determine if they are initial
                    interleaved.addInitialState(new_state);
            }
        }

        // add both ts systems' actions and AP
        interleaved.addAllActions(ts1.getActions());
        interleaved.addAllActions(ts2.getActions());
        interleaved.addAllAtomicPropositions(ts1.getAtomicPropositions());
        interleaved.addAllAtomicPropositions(ts2.getAtomicPropositions());

        // for every transition which not included in handShakingActions we create a new transition in
        // the interleaved system
        for (TSTransition<S1, A> transition : ts1.getTransitions())
            if (!handShakingActions.contains(transition.getAction()))
                for (Pair<S1, S2> new_state : interleaved.getStates())
                    if (new_state.first.equals(transition.getFrom()))
                        for (Pair<S1, S2> to_state : interleaved.getStates())
                            if (to_state.first.equals(transition.getTo()) && to_state.second.equals(new_state.second))
                                interleaved.addTransition(new TSTransition<>(new_state, transition.getAction(), to_state));

        for (TSTransition<S2, A> transition : ts2.getTransitions())
            if (!handShakingActions.contains(transition.getAction()))
                for (Pair<S1, S2> new_state : interleaved.getStates())
                    if (new_state.second.equals(transition.getFrom()))
                        for (Pair<S1, S2> to_state : interleaved.getStates())
                            if (to_state.second.equals(transition.getTo()) && to_state.first.equals(new_state.first))
                                interleaved.addTransition(new TSTransition<>(new_state, transition.getAction(), to_state));

        return interleaved;
    }

    /**
     * Compute the synchronous product of two transition systems.
     *
     * @param <S1> Type of states in the first system.
     * @param <S2> Type of states in the first system.
     * @param <A>  Type of actions (in both systems).
     * @param <P>  Type of atomic propositions (in both systems).
     * @param ts1  The first transition system.
     * @param ts2  The second transition system.
     * @return A transition system that represents the product of the two.
     */
    public <S1, S2, A, P> TransitionSystem<Pair<S1, S2>, A, P> interleave(TransitionSystem<S1, A, P> ts1,
                                                                          TransitionSystem<S2, A, P> ts2) {

        TransitionSystem<Pair<S1, S2>, A, P> interleaved = createInterleavedBase(ts1, ts2, new HashSet<>());
        removeUnreachableStates(interleaved);
        return interleaved;
    }

    /**
     * Compute the synchronous product of two transition systems.
     *
     * @param <S1>               Type of states in the first system.
     * @param <S2>               Type of states in the first system.
     * @param <A>                Type of actions (in both systems).
     * @param <P>                Type of atomic propositions (in both systems).
     * @param ts1                The first transition system.
     * @param ts2                The second transition system.
     * @param handShakingActions Set of actions both systems perform together.
     * @return A transition system that represents the product of the two.
     */
    public <S1, S2, A, P> TransitionSystem<Pair<S1, S2>, A, P> interleave(TransitionSystem<S1, A, P> ts1,
                                                                          TransitionSystem<S2, A, P> ts2, Set<A> handShakingActions) {
        TransitionSystem<Pair<S1, S2>, A, P> interleaved = createInterleavedBase(ts1, ts2, handShakingActions);

        for (TSTransition ts1_trans : ts1.getTransitions()) {
            if (handShakingActions.contains(ts1_trans.getAction())) {
                S1 ts1_from = (S1) ts1_trans.getFrom();
                S1 ts1_to = (S1) ts1_trans.getTo();
                for (S2 state_of_2 : ts2.getStates()) {
                    Set<S2> post2 = post(ts2, state_of_2, (A) ts1_trans.getAction());
                    for (Pair<S1, S2> new_state : interleaved.getStates()) {
                        if (new_state.first.equals(ts1_from) && new_state.second.equals(state_of_2)) {
                            for (S2 p2 : post2) {
                                for (Pair<S1, S2> to_state : interleaved.getStates()) {
                                    if (to_state.first.equals(ts1_to) && to_state.second.equals(p2)) {
                                        interleaved.addTransition(new TSTransition<>(new_state, (A) ts1_trans.getAction(), to_state));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        /*for (A action : handShakingActions) {
            List<S1> orig1 = new LinkedList<>();
            List<S2> orig2 = new LinkedList<>();
            Set<S1> allpost1 = new HashSet<>();
            Set<S2> allpost2 = new HashSet<>();
            Set<S1> post1 = new HashSet<>();
            Set<S2> post2 = new HashSet<>();
            for (S1 state1 : ts1.getStates()) {
                post1 = post(ts1, state1, action);
                if (post1.size() > 0) {
                    orig1.add(state1);
                    allpost1.addAll(post1);
                }
            }
            for (S2 state2 : ts2.getStates()) {
                post2 = post(ts2, state2, action);
                if (post2.size() > 0) {
                    orig2.add(state2);
                    allpost2.addAll(post2);
                }
            }
            for (Pair<S1, S2> new_state : interleaved.getStates()) {
                //check if sub states of new_states both has transition identified by action
                if (orig1.contains(new_state.first) && orig2.contains(new_state.second)) {
                    for (Pair<S1, S2> to_state : interleaved.getStates()) {
                        if (allpost1.contains(to_state.first) && allpost2.contains(to_state.second))
                            interleaved.addTransition(new TSTransition<>(new_state, action, to_state));
                    }
                }
            }
        }*/
        removeUnreachableStates(interleaved);
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
     * @param <A>  Type of actions in BOTH GRAPHS.
     * @param pg1  The first program graph.
     * @param pg2  The second program graph.
     * @return Interleaved program graph.
     */
    public <L1, L2, A> ProgramGraph<Pair<L1, L2>, A> interleave(ProgramGraph<L1, A> pg1, ProgramGraph<L2, A> pg2) {
        ProgramGraph<Pair<L1, L2>, A> interleaved = createProgramGraph();
        // create all new states and set initials
        for (L1 loc1 : pg1.getLocations()) {
            for (L2 loc2 : pg2.getLocations()) {
                Pair<L1, L2> new_loc = new Pair<L1, L2>(loc1, loc2);
                if (pg1.getInitialLocations().contains(loc1) && pg2.getInitialLocations().contains(loc2))
                    interleaved.setInitial(new_loc, true);
                else
                    interleaved.addLocation(new_loc);
            }
        }
//        Set<List<String>> both_inits = new HashSet<>(pg1.getInitalizations());
//        both_inits.addAll(pg2.getInitalizations());
//        // set the initialization for the new PG
//        for (List<String> init_list : both_inits)
//            interleaved.addInitalization(init_list);
        Set<List<String>> inits1 = pg1.getInitalizations();
        Set<List<String>> inits2 = pg2.getInitalizations();
        for (List<String> in1 : inits1) {
            for (List<String> in2 : inits2) {
                List<String> toadd = new ArrayList(in1);
                toadd.addAll(in2);
                interleaved.addInitalization(toadd);
            }
        }
        //
        for (PGTransition<L1, A> trans : pg1.getTransitions())
            for (Pair<L1, L2> from_loc : interleaved.getLocations())
                if (from_loc.first.equals(trans.getFrom()))
                    for (Pair<L1, L2> to_loc : interleaved.getLocations())
                        if (to_loc.first.equals(trans.getTo()) && to_loc.second.equals(from_loc.second))
                            interleaved.addTransition(new PGTransition<>(from_loc, trans.getCondition(), trans.getAction(), to_loc));
        for (PGTransition<L2, A> trans : pg2.getTransitions())
            for (Pair<L1, L2> from_loc : interleaved.getLocations())
                if (from_loc.second.equals(trans.getFrom()))
                    for (Pair<L1, L2> to_loc : interleaved.getLocations())
                        if (to_loc.second.equals(trans.getTo()) && to_loc.first.equals(from_loc.first))
                            interleaved.addTransition(new PGTransition<>(from_loc, trans.getCondition(), trans.getAction(), to_loc));
        removeUnreachableLocations(interleaved);
        return interleaved;
    }

    private ArrayList<ArrayList<Boolean>> createBoolCombinations(int n) {
        ArrayList ret = new ArrayList();
        for (int i = 0; i < Math.pow(2, n); i++) {
            String bin = Integer.toBinaryString(i);
            while (bin.length() < n)
                bin = "0" + bin;
            char[] chars = bin.toCharArray();
            ArrayList<Boolean> boolArray = new ArrayList<>(n);
            for (int j = 0; j < chars.length; j++) {
                if (chars[j] == '0')
                    boolArray.add(j, true);
                else
                    boolArray.add(j, false);
            }
            ret.add(boolArray);
        }
        return ret;
    }

    private <S, A, P> void removeUnreachableStates(TransitionSystem<S, A, P> ts) {
        Set<S> reachable = this.reach(ts);
        Set<S> unreachable = new HashSet<>();
        Set<TSTransition<S, A>> unNeededTrans = new HashSet<>();
        for (S state : ts.getStates()) {
            if (!reachable.contains(state)) {
                unreachable.add(state);
                for (TSTransition<S, A> trans : ts.getTransitions()) {
                    if (trans.getFrom().equals(state))
                        unNeededTrans.add(trans);
                }
            }
        }
        for (S toRemove : unreachable)
            ts.forceRemoveState(toRemove);
        for (TSTransition<S, A> trans : unNeededTrans)
            ts.removeTransition(trans);
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
        // create all bool combinations for ts states
        ArrayList combinations = createBoolCombinations((int) (Math.pow(2, c.getRegisterNames().size()) * Math.pow(2, c.getInputPortNames().size())));
        // create a new state for every combination
        for (Object boolarr : combinations) {
            //split the combination to get the registers and inputs separated
            List<Boolean> reg = ((ArrayList) boolarr).subList(0, c.getRegisterNames().size());
            Map<String, Boolean> regMap = new HashMap<>();
            int i = 0;
            for (String regname : c.getRegisterNames()) {
                regMap.put(regname, reg.get(i));
                i++;
            }
            List<Boolean> in = ((ArrayList) boolarr).subList(c.getRegisterNames().size(), c.getRegisterNames().size() + c.getInputPortNames().size());
            Map<String, Boolean> inpMap = new HashMap<>();
            i = 0;
            for (String inpname : c.getInputPortNames()) {
                inpMap.put(inpname, in.get(i));
                i++;
            }
            //all combinations of input is also the ts actions
            ts.addAction(inpMap);
            Pair<Map<String, Boolean>, Map<String, Boolean>> new_state = new Pair<>(regMap, inpMap);
            // check if all registers are false -> initial state
            if (reg.stream().reduce(false, (a, b) -> a || b))
                ts.addState(new_state);
            else
                ts.addInitialState(new_state);
        }
        ts.addAllAtomicPropositions(c.getOutputPortNames().toArray());
        ts.addAllAtomicPropositions(c.getInputPortNames().toArray());
        ts.addAllAtomicPropositions(c.getRegisterNames().toArray());
        // for every state and every action add transition
        for (Pair<Map<String, Boolean>, Map<String, Boolean>> from_state : ts.getStates()) {
            for (Map<String, Boolean> action : ts.getActions()) {
                Map<String, Boolean> upd = c.updateRegisters(from_state.second, from_state.first);
                ts.addTransition(new TSTransition(from_state, action, new Pair<>(upd, action)));
            }
            // add labels for output
            for (Map.Entry entry : c.computeOutputs(from_state.second, from_state.first).entrySet()) {
                if ((Boolean) entry.getValue())
                    ts.addToLabel(from_state, entry.getKey());
            }
            // add labels for registers
            for (Map.Entry entry : from_state.first.entrySet()) {
                if ((Boolean) entry.getValue())
                    ts.addToLabel(from_state, entry.getKey());
            }
            // add labels for input
            for (Map.Entry entry : from_state.second.entrySet()) {
                if ((Boolean) entry.getValue())
                    ts.addToLabel(from_state, entry.getKey());
            }

        }
        removeUnreachableStates(ts);
        return ts;
    }

    /**
     * Creates a {@link TransitionSystem} from a program graph.
     *
     * @param <L>           Type of program graph locations.
     * @param <A>           Type of program graph actions.
     * @param pg            The program graph to be translated into a transition system.
     * @param actionDefs    Defines the effect of each action.
     * @param conditionDefs Defines the conditions (guards) of the program
     *                      graph.
     * @return A transition system representing {@code pg}.
     */
    public <L, A> TransitionSystem<Pair<L, Map<String, Object>>, A, String> transitionSystemFromProgramGraph(
            ProgramGraph<L, A> pg, Set<ActionDef> actionDefs, Set<ConditionDef> conditionDefs) {
        TransitionSystem<Pair<L, Map<String, Object>>, A, String> ts = createTransitionSystem();
        for (Pair<L, Map<String, Object>> initialState : findInitialStates(pg, actionDefs)) {
            ts.addInitialState(initialState);
            tagNewState(ts, initialState);
        }
        spreadReachables(pg, actionDefs, conditionDefs, ts);
        return ts;
    }

    public <L, A> TransitionSystem<Pair<List<L>, Map<String, Object>>, A, String> transitionSystemFromChannelSystem(
            ChannelSystem<L, A> cs, Set<ActionDef> actions, Set<ConditionDef> conditions) {
        TransitionSystem<Pair<List<L>, Map<String, Object>>, A, String> ts = createTransitionSystem();
        findInitialStates(cs, actions, ts);
        spreadReachables(cs, actions, conditions, ts);
        return ts;
    }

    private <A, L> void spreadReachables(ChannelSystem<L, A> cs, Set<ActionDef> actions, Set<ConditionDef> conditions, TransitionSystem<Pair<List<L>, Map<String, Object>>, A, String> ts) {
        for (Pair<List<L>, Map<String, Object>> initialState : ts.getInitialStates()) {
            Queue<Pair<List<L>, Map<String, Object>>> q = new LinkedList<>();
            q.add(initialState);
            spreadToReachables(q, cs, actions, conditions, ts);
        }
    }

    private <L, A> void spreadToReachables(Queue<Pair<List<L>, Map<String, Object>>> q, ChannelSystem<L, A> cs, Set<ActionDef> actions, Set<ConditionDef> conditions, TransitionSystem<Pair<List<L>, Map<String, Object>>, A, String> ts) {
        while (!q.isEmpty()) {
            Pair<List<L>, Map<String, Object>> currState = q.poll();
            for (ProgramGraph<L, A> p1 : cs.getProgramGraphs()) {
                for (ProgramGraph<L, A> p2 : cs.getProgramGraphs()) {
                    if (!p1.equals(p2)) {
                        for (PGTransition<L, A> tran1 : p1.getTransitions().stream().filter(x -> currState.first.contains(x.getFrom())).collect(Collectors.toList())) {
                            for (PGTransition<L, A> tran2 : p2.getTransitions().stream().filter(x -> currState.first.contains(x.getFrom())).collect(Collectors.toList())) {
                                if (currState.getFirst().contains(tran1.getFrom())) {
                                    for (ActionDef ad1 : actions) {
                                        for (ActionDef ad2 : actions) {
                                            for (ConditionDef cd1 : conditions) {
                                                for (ConditionDef cd2 : conditions) {
                                                    if (canExecuteASync(currState, tran1, tran2, cd1, cd2, ad1, ad2)) {
                                                        List<L> locations = new ArrayList<>(currState.first);
                                                        locations.remove(tran1.getFrom());
                                                        locations.remove(tran2.getFrom());
                                                        locations.add(tran1.getTo());
                                                        locations.add(tran2.getTo());
                                                        locations.sort(new Comparator<L>() {
                                                            @Override
                                                            public int compare(L o1, L o2) {
                                                                return o1.toString().compareTo(o2.toString());
                                                            }
                                                        });
                                                        String action = tran1.getAction() + "|" + tran2.getAction();
                                                        Pair<List<L>, Map<String, Object>> newState =
                                                                new Pair<>(
                                                                        locations,
                                                                        ad1.effect(currState.getSecond(), action)
                                                                );
                                                        boolean needToSpread = !ts.getStates().contains(newState);
                                                        ts.addTransition(new TSTransition<>(currState, null, newState));
                                                        tagCSNewState(ts, newState);
                                                        ts.addState(newState);
                                                        if (needToSpread) {
                                                            q.add(newState);
                                                        }
                                                    } else if (canExecute(currState, tran1, cd1, ad1)) {
                                                        List<L> locations = new ArrayList<>(currState.first);
                                                        locations.remove(tran1.getFrom());
                                                        locations.add(tran1.getTo());
                                                        locations.sort(new Comparator<L>() {
                                                            @Override
                                                            public int compare(L o1, L o2) {
                                                                return o1.toString().compareTo(o2.toString());
                                                            }
                                                        });
                                                        Pair<List<L>, Map<String, Object>> newState =
                                                                new Pair<>(
                                                                        locations,
                                                                        ad1.effect(currState.getSecond(), tran1.getAction())
                                                                );
                                                        boolean needToSpread = !ts.getStates().contains(newState);
                                                        if (isChannelAction(tran1.getAction())) {
                                                            ts.addTransition(new TSTransition<>(currState, null, newState));
                                                        } else {
                                                            ts.addTransition(new TSTransition<>(currState, tran1.getAction(), newState));
                                                        }
                                                        tagCSNewState(ts, newState);
                                                        ts.addState(newState);
                                                        if (needToSpread) {
                                                            q.add(newState);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                    }
                }
            }
        }
    }

    private <L, A> void tagCSNewState(TransitionSystem<Pair<List<L>, Map<String, Object>>, A, String> ts, Pair<List<L>, Map<String, Object>> newState) {
        for (L l : newState.first) {
            ts.addToLabel(newState, l.toString());
        }
        for (String key : newState.second.keySet()) {
            ts.addToLabel(newState, key + "=" + newState.second.get(key).toString());
        }
    }

    private <A> boolean isChannelAction(A action) {
        return NanoPromelaFileReader.pareseNanoPromelaString(action.toString()).chanreadstmt() != null ||
                NanoPromelaFileReader.pareseNanoPromelaString(action.toString()).chanwritestmt() != null;

    }

    private <L, A> boolean canExecute(Pair<List<L>, Map<String, Object>> currState, PGTransition<L, A> tran1, ConditionDef cd1, ActionDef ad1) {
        try {
            return currState.first.contains(tran1.getFrom()) &&
                    ad1.isMatchingAction(tran1.getAction()) &&
                    cd1.evaluate(currState.getSecond(), tran1.getCondition()) &&
                    ad1.effect(currState.getSecond(), tran1.getAction()) != null;
        } catch (ParseCancellationException e) {
            return false;
        }

    }

    private <L> boolean canExecuteASync(Pair<List<L>, Map<String, Object>> currState, PGTransition tran1, PGTransition tran2, ConditionDef cd1, ConditionDef cd2, ActionDef ad1, ActionDef ad2) {
        if (
                ad1 instanceof InterleavingActDef
                        && ad2 instanceof InterleavingActDef
                        && new ParserBasedInterleavingActDef().isOneSidedAction(tran1.getAction().toString())
        ) {
            InterleavingActDef iad1 = (InterleavingActDef) ad1;
            String cond1 = tran1.getCondition().equals("") ? "true" : "(" + tran1.getCondition() + ")";
            String cond2 = tran2.getCondition().equals("") ? "true" : "(" + tran2.getCondition() + ")";
            String cond = cond1 + "&&" + cond2;
            String action = tran1.getAction() + "|" + tran2.getAction();
            Set<ConditionDef> set = new HashSet<>();
            set.add(cd1);
            set.add(cd2);
            try {
                return ConditionDef.evaluate(set, currState.getSecond(), cond)
                        && iad1.isMatchingAction(action)
                        && ad1.effect(currState.getSecond(), action) != null
                        && currState.first.contains(tran1.getFrom())
                        && currState.first.contains(tran2.getFrom());
            } catch (java.lang.Exception e) {
                if (e.getMessage().contains("Incompatible hanshaking statements")) {
                    return false;
                } else {
                    throw e;
                }
            }

        } else {
            return false;
        }
    }


    private <A, L> void findInitialStates(ChannelSystem<L, A> cs, Set<ActionDef> actions, TransitionSystem<Pair<List<L>, Map<String, Object>>, A, String> ts) {
        for (List<L> candidate : genInitialCandidates(cs, actions)) {
            for (Map<String, Object> initialEval : getInitialEvals(cs, actions)) {
                Pair<List<L>, Map<String, Object>> newState = new Pair<>(candidate, initialEval);
                tagCSNewState(ts, newState);
                ts.addInitialState(newState);
            }
        }
    }

    private <L, A> Iterable<? extends Map<String, Object>> getInitialEvals(ChannelSystem<L, A> cs, Set<ActionDef> actions) {
        Set<Map<String, Object>> ans = new HashSet<>();
        ans.add(new HashMap<>());
        for (ProgramGraph<L, A> p : cs.getProgramGraphs()) {
            boolean hasRep = p.getInitalizations().size() == 0 ? true : false;
            for (List<String> g : p.getInitalizations()) {
                Map<String, Object> currEval = new HashMap<>();
                for (ActionDef ad : actions) {
                    for (String s : g) {
                        if (ad.isMatchingAction(s))
                            currEval = ad.effect(new HashMap<>(), s);
                    }
                }
                for (Map<String, Object> existingInitial : ans) {
                    if (!hasConflict(currEval, existingInitial)) {
                        Map<String, Object> newEval = new HashMap<>();
                        newEval.putAll(currEval);
                        newEval.putAll(existingInitial);
                        ans.remove(existingInitial);
                        ans.add(newEval);
                        hasRep = true;
                    }
                }
            }
            if (!hasRep) {
                return new HashSet<>();
            }
        }
        return ans;
    }

    private boolean hasConflict(Map<String, Object> g, Map<String, Object> existingInitial) {
        for (String x : g.keySet()
        ) {
            for (String y : existingInitial.keySet()
            ) {
                if (x.equals(y)) {
                    if (g.get(x).equals(existingInitial.get(y))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    private <L, A> Set<List<L>> genInitialCandidates(ChannelSystem<L, A> cs, Set<ActionDef> actions) {
        List<List<L>> ans = new ArrayList<>();
        ProgramGraph<List<L>, A> init = createProgramGraph();
        ProgramGraph<List<L>, A> acc = init;
        for (ProgramGraph<L, A> curr : cs.getProgramGraphs()) {
            acc = cross(curr, acc);
        }
        return acc.getLocations();
    }

    private <L, A> ProgramGraph<List<L>, A> cross(ProgramGraph<L, A> curr, ProgramGraph<List<L>, ?> acc) {
        ProgramGraph<List<L>, A> ans = createProgramGraph();
        if (acc.getLocations().size() == 0) {
            for (L l2 : curr.getInitialLocations()) {
                List<L> newState = new ArrayList<>();
                newState.add(l2);
                ans.addLocation(newState);
            }
            return ans;
        }
        for (List<L> l1 : acc.getLocations()) {
            for (L l2 : curr.getInitialLocations()) {
                List<L> newState = new ArrayList<>(l1);
                newState.add(l2);
                ans.addLocation(newState);
            }
        }
        return ans;
    }


    private <L, A> void spreadReachables(ProgramGraph<L, A> pg, Set<ActionDef> actionDefs, Set<ConditionDef> conditionDefs, TransitionSystem<Pair<L, Map<String, Object>>, A, String> ts) {
        for (Pair<L, Map<String, Object>> initialState : ts.getInitialStates()) {
            spreadToReachables(initialState, pg, actionDefs, conditionDefs, ts);
        }
    }


    private <L, A> List<Pair<L, Map<String, Object>>> findInitialStates(ProgramGraph<L, A> pg, Set<ActionDef> actionDefs) {
        List<Pair<L, Map<String, Object>>> ans = new ArrayList<>();
        for (L initLoc : pg.getInitialLocations()) {
            for (List<String> init : pg.getInitalizations()) {
                for (String initialDef : init) {
                    for (ActionDef ad : actionDefs) {
                        if (ad.isMatchingAction(initialDef)) {
                            Pair<L, Map<String, Object>> newState = new Pair<>(initLoc, ad.effect(new HashMap<>(), initialDef));
                            ans.add(newState);
                        }
                    }
                }
            }
        }
        return ans;
    }

    private <L, A> void tagNewState(TransitionSystem<Pair<L, Map<String, Object>>, A, String> ts, Pair<L, Map<String, Object>> newState) {
        ts.addToLabel(newState, newState.first.toString());
        for (String key : newState.second.keySet()) {
            ts.addToLabel(newState, key + "=" + newState.second.get(key).toString());
        }
    }

    private <L, A> void spreadToReachables(Pair<L, Map<String, Object>> state, ProgramGraph<L, A> pg, Set<ActionDef> actionDefs, Set<ConditionDef> conditionDefs, TransitionSystem ts) {
        Queue<Pair<L, Map<String, Object>>> q = new LinkedList();
        q.add(state);
        while (!q.isEmpty()){
            Pair<L, Map<String, Object>> currState= q.poll();
            for (PGTransition tran : pg.getTransitions()) {
                if (tran.getFrom().equals(currState.getFirst())) {
                    for (ConditionDef cd : conditionDefs) {
                        if (cd.evaluate(currState.getSecond(), tran.getCondition())) {
                            for (ActionDef ad : actionDefs) {
                                if (ad.isMatchingAction(tran.getAction())) {
                                    Pair<L, Map<String, Object>> newState = new Pair(tran.getTo(), ad.effect(currState.getSecond(), tran.getAction()));
                                    if(!ts.getStates().contains(newState)){
                                        ts.addState(newState);
                                        ts.addTransition(new TSTransition(currState, tran.getAction(), newState));
                                        tagNewState(ts, newState);
                                        q.add(newState);
                                    }

//                                    spreadToReachables(newState, pg, actionDefs, conditionDefs, ts);
                                }
                            }
                        }
                    }
                }
            }
        }

    }


    private String padSpaces(String code) {
        code = code.replace("atomic", "atomic ");
        code = code.replace("->", " -> ");
        code = code.replace("::", ":: ");
        code = code.replace("skip", " skip");
//        code = code.replace(":=", " := ");
//        code = code.replace("-", " - ");
//        code = code.replace("+", " + ");
//        code = code.replace("*", " * ");
//        code = code.replace("/", " / ");
//        code = code.replace("%", " % ");
        code = code.replace(";", "; ");
        code = code.replace("fi", " fi");
        code = code.replace("if", "if ");
        code = code.replace("od", " od");
        code = code.replace("do", "do ");
        code = code.replace("{", "{ ");
        code = code.replace("}", "} ");
        return code;
    }

    private <L, A> Set<L> PGreach(ProgramGraph<L, A> pg) {
        Set<L> reachable = new HashSet<>();
        for (L initial_loc : pg.getInitialLocations()) {
            reachable.add(initial_loc);
            reachable = rec_PGreach(pg, initial_loc, reachable);
        }
        return reachable;
    }

    private <L, A> Set<L> rec_PGreach(ProgramGraph<L, A> pg, L l, Set<L> reachable) {
        for (L post_loc : postPG(pg, l)) {
            if (!reachable.contains(post_loc)) {
                reachable.add(post_loc);
                reachable = rec_PGreach(pg, post_loc, reachable);
            }
        }
        return reachable;
    }

    private <L, A> Set<L> postPG(ProgramGraph<L, A> pg, L l) {
        Set<L> ans = new HashSet<>();
        for (PGTransition<L, A> trans : pg.getTransitions()) {
            if (trans.getFrom().equals(l))
                ans.add(trans.getTo());
        }
        return ans;
    }

    public <L, A> void removeUnreachableLocations(ProgramGraph<L, A> pg) {
        Set<L> reachable = this.PGreach(pg);
        Set<L> unreachable = new HashSet<>();
        Set<PGTransition<L, A>> unNeededTrans = new HashSet<>();
        for (L location : pg.getLocations()) {
            if (!reachable.contains(location)) {
                unreachable.add(location);
                for (PGTransition<L, A> trans : pg.getTransitions()) {
                    if (trans.getFrom().equals(location))
                        unNeededTrans.add(trans);
                }
            }
        }
        for (L toRemove : unreachable)
            pg.removeLocation(toRemove);
        for (PGTransition<L, A> trans : unNeededTrans)
            pg.removeTransition(trans);
    }

    /**
     * Construct a program graph from nanopromela code.
     *
     * @param filename The nanopromela code.
     * @return A program graph for the given code.
     * @throws Exception If the code is invalid.
     */
    public ProgramGraph<String, String> programGraphFromNanoPromela(String filename) throws Exception {
        NanoPromelaParser.StmtContext root = NanoPromelaFileReader.pareseNanoPromelaFile(filename);
        return createPGFromNP(root);
    }

    /**
     * Construct a program graph from nanopromela code.
     *
     * @param nanopromela The nanopromela code.
     * @return A program graph for the given code.
     * @throws Exception If the code is invalid.
     */
    public ProgramGraph<String, String> programGraphFromNanoPromelaString(String nanopromela) throws Exception {
        NanoPromelaParser.StmtContext root = NanoPromelaFileReader.pareseNanoPromelaString(padSpaces(nanopromela));
        return createPGFromNP(root);
    }


    /**
     * Construct a program graph from nanopromela code.
     *
     * @param inputStream The nanopromela code.
     * @return A program graph for the given code.
     * @throws Exception If the code is invalid.
     */
    public ProgramGraph<String, String> programGraphFromNanoPromela(InputStream inputStream) throws Exception {
        NanoPromelaParser.StmtContext root = NanoPromelaFileReader.parseNanoPromelaStream(inputStream);
        return createPGFromNP(root);
    }


    private ProgramGraph<String, String> createPGFromNP(NanoPromelaParser.StmtContext root) {
        ProgramGraph<String, String> pg = createProgramGraph();
        Set<NanoPromelaParser.StmtContext> locations = new HashSet<>();
        createLocationsFromNP(root, locations);
        // the whole program is the initial location of the pg
        for (NanoPromelaParser.StmtContext loc : locations)
            pg.addLocation(loc.getText());
        pg.setInitial(root.getText(), true);
        Set<PGTransition> transitions = new HashSet<>();
        for (String string_loc : pg.getLocations()) {
            if (!string_loc.equals("exit")) {
                Set<PGTransition<String, String>> trans = createTransitionsFromNP(string_loc);
                for (PGTransition<String, String> transition : trans)
                    transitions.add(transition);
            }
        }
        for (PGTransition trans : transitions)
            pg.addTransition(trans);
        this.removeUnreachableLocations(pg);
        return pg;
    }


    private void createLocationsFromNP(NanoPromelaParser.StmtContext root, Set<NanoPromelaParser.StmtContext> locations) {
        if (root.assstmt() != null || root.chanreadstmt() != null || root.chanwritestmt() != null || root.atomicstmt() != null || root.skipstmt() != null) {
            /* The sub-statements are only [root] and [exit]                    */
            locations.add(root);
            locations.add(NanoPromelaFileReader.pareseNanoPromelaString("exit"));
        } else if (root.ifstmt() != null) {
        /* The sub-statements are [root], [exit], and the sub-statements of
        all op.stmt() where op is a member of root.ifstmt().option() */
            locations.add(NanoPromelaFileReader.pareseNanoPromelaString("exit"));
            locations.add(root);
            List<NanoPromelaParser.OptionContext> options = root.ifstmt().option();
            for (NanoPromelaParser.OptionContext option : options) {
                createLocationsFromNP(option.stmt(), locations);
            }
        } else if (root.dostmt() != null) {
        /* The sub-statements are [root], [exit], and locations [𝑠𝑢𝑏;root]
        where 𝑠𝑢𝑏 is a sub-statement of some op in root.dostmt().option() */
            locations.add(NanoPromelaFileReader.pareseNanoPromelaString("exit"));
            locations.add(root);
            List<NanoPromelaParser.OptionContext> options = root.dostmt().option();
            for (NanoPromelaParser.OptionContext option : options) {
                Set<NanoPromelaParser.StmtContext> subs = new HashSet<>();
                createLocationsFromNP(option.stmt(), subs);
                for (NanoPromelaParser.StmtContext sub : subs) {
                    if (!sub.getText().equals("exit"))
                        locations.add(NanoPromelaFileReader.pareseNanoPromelaString(padSpaces(sub.getText() + ";" + root.getText())));
                }
            }
        } else { // ;
            /* The sub-statements are locations of the form [𝑠𝑢𝑏;root.stmt(1)] where 𝑠𝑢𝑏 is a sub-statement of root.stmt(0) plus all the substatements of root.stmt(1) */
            NanoPromelaParser.StmtContext first = root.stmt(0);
            NanoPromelaParser.StmtContext second = root.stmt(1);
            Set<NanoPromelaParser.StmtContext> sub_of_first = new HashSet<>();
            createLocationsFromNP(first, sub_of_first);
            for (NanoPromelaParser.StmtContext sub : sub_of_first) {
                if (!sub.getText().equals("exit")) {
                    locations.add(NanoPromelaFileReader.pareseNanoPromelaString(padSpaces(sub.getText() + ";" + second.getText())));
                }
            }
            Set<NanoPromelaParser.StmtContext> sub_of_second = new HashSet<>();
            createLocationsFromNP(second, sub_of_second);
            locations.addAll(sub_of_second);
        }
    }

    private Set<PGTransition<String, String>> createTransitionsFromNP(String loc) {
        NanoPromelaParser.StmtContext location = NanoPromelaFileReader.pareseNanoPromelaString(padSpaces(loc));
        if (location.assstmt() != null || location.chanreadstmt() != null || location.chanwritestmt() != null || location.atomicstmt() != null || location.skipstmt() != null) {
            return Set.of(handleBaseOperation(location));
        } else if (location.ifstmt() != null) {
            return handleIfOperation(location);
        } else if (location.dostmt() != null) {
            return handleDoOperation(location);
        } else { // ;
            return handleJoinOperation(location);
        }
    }

    private Set<PGTransition<String, String>> handleDoOperation(NanoPromelaParser.StmtContext location) {
        Set<PGTransition<String, String>> transitions = new HashSet<>();
        String do_loop = location.getText();
        List<NanoPromelaParser.OptionContext> options = location.dostmt().option();
        String none_of_conds = "";
        for (NanoPromelaParser.OptionContext option : options)
            none_of_conds = none_of_conds + "!" + option.boolexpr().getText() + " && ";
        none_of_conds = none_of_conds.substring(0, none_of_conds.length() - 4);
        transitions.add(new PGTransition<String, String>(do_loop, none_of_conds, "nothing", "exit"));
        for (NanoPromelaParser.OptionContext option : options) {
            Set<PGTransition<String, String>> optTrans = createTransitionsFromNP(option.stmt().getText());
            for (PGTransition<String, String> trans : optTrans) {
                if (trans.getFrom().equals(option.stmt().getText()) && trans.getTo().equals("exit"))
                    transitions.add(new PGTransition<>(do_loop, "(" + option.boolexpr().getText() + " && " + trans.getCondition() + ")", trans.getAction(), do_loop));
                else if (trans.getFrom().equals(option.stmt().getText()))
                    transitions.add(new PGTransition<>(do_loop, "(" + option.boolexpr().getText() + " && " + trans.getCondition() + ")", trans.getAction(), trans.getTo() + ";" + do_loop));
            }
        }
        return transitions;
    }

    private Set<PGTransition<String, String>> handleIfOperation(NanoPromelaParser.StmtContext location) {
        List<NanoPromelaParser.OptionContext> options = location.ifstmt().option();
        Set<PGTransition<String, String>> transitions = new HashSet<>();
        for (NanoPromelaParser.OptionContext option : options) {
            String stmt = option.stmt().getText();
            Set<PGTransition<String, String>> transOfOpt = createTransitionsFromNP(stmt);
            for (PGTransition trans_of_opt : transOfOpt) {
                if (trans_of_opt.getFrom().equals(stmt))
                    transitions.add(new PGTransition<>(location.getText(), "(" + option.boolexpr().getText() + " && " + trans_of_opt.getCondition() + ")", (String) trans_of_opt.getAction(), (String) trans_of_opt.getTo()));
            }
        }
        return transitions;
    }

    private Set<PGTransition<String, String>> handleJoinOperation(NanoPromelaParser.StmtContext joined) {
        NanoPromelaParser.StmtContext first = joined.stmt(0);
        NanoPromelaParser.StmtContext second = joined.stmt(1);
        Set<PGTransition<String, String>> trans_of_first = createTransitionsFromNP(first.getText());
        Set<PGTransition<String, String>> ret = new HashSet<>();
        for (PGTransition<String, String> transition : trans_of_first) {
            if (transition.getFrom().equals(first.getText()) && transition.getTo().equals("exit"))
                ret.add(new PGTransition<>(joined.getText(), transition.getCondition(), transition.getAction(), second.getText()));
            else if (transition.getFrom().equals(first.getText()))
                ret.add(new PGTransition<>(joined.getText(), transition.getCondition(), transition.getAction(), transition.getTo() + ";" + second.getText()));
        }
        return ret;
    }


    private PGTransition handleBaseOperation(NanoPromelaParser.StmtContext location) {
        if (location.skipstmt() != null)
            return new PGTransition<>(location.getText(), "true", "nothing", "exit");
        else if (location.atomicstmt() != null) {
            String atomic = "";
            List<NanoPromelaParser.IntexprContext> basics = location.atomicstmt().intexpr();
            for (int i = 0; i < basics.size(); i++) {
                atomic += basics.get(i).getText();
                if (i != basics.size() - 1)
                    atomic += ";";
            }
            return new PGTransition(location.getText(), "true", atomic, "exit");
        } else
            return new PGTransition<>(location.getText(), "true", location.getText(), "exit");
    }

//    private void createLocationsFromNP2(NanoPromelaParser.StmtContext root, Set<String> locations) {
//        if (root.assstmt() != null || root.chanreadstmt() != null || root.chanwritestmt() != null || root.atomicstmt() != null || root.skipstmt() != null) {
//            /* The sub-statements are only [root] and [exit]                    */
//            String exit = "exit";
//            String base = root.getText();
//            locations.add(exit);
//            locations.add(base);
//        }
//        else if (root.ifstmt() != null) {
//        /* The sub-statements are [root], [exit], and the sub-statements of
//        all op.stmt() where op is a member of root.ifstmt().option() */
//            String exit = "exit";
//            String base = root.getText();
//            locations.add(exit);
//            locations.add(base);
//            List<NanoPromelaParser.OptionContext> options = root.ifstmt().option();
//            for(NanoPromelaParser.OptionContext option : options){
//                Set<String> locOfOpt = new HashSet<>();
//                Set<PGTransition<String, String>> transOfOpt = new HashSet<>();
//                createLocationsFromNP2(option.stmt(), locOfOpt);
//                locations.addAll(locOfOpt);
//            }
//        }
//        else if (root.dostmt() != null) {
//        /* The sub-statements are [root], [exit], and locations [𝑠𝑢𝑏;root]
//        where 𝑠𝑢𝑏 is a sub-statement of some op in root.dostmt().option() */
//            String exit = "exit";
//            String base = root.getText();
//            locations.add(exit);
//            locations.add(base);
//            List<NanoPromelaParser.OptionContext> options = root.dostmt().option();
//            for(NanoPromelaParser.OptionContext option : options){
//                Set<String> locOfOpt = new HashSet<>();
//                createLocationsFromNP2(option.stmt(), locOfOpt);
//                for(String loc : locOfOpt)
//                    if(!loc.equals("exit")) {
//                        locations.add(loc + ";" + base);
//                    }
//            }
//        }
//        else { // ;
//            /* The sub-statements are locations of the form [𝑠𝑢𝑏;root.stmt(1)] where 𝑠𝑢𝑏 is a sub-statement of root.stmt(0) plus all the substatements of root.stmt(1) */
//            NanoPromelaParser.StmtContext first = root.stmt(0);
//            NanoPromelaParser.StmtContext second = root.stmt(1);
//            Set<String> locOfFirst = new HashSet<>();
//            Set<String> locOfSecond = new HashSet<>();
//            createLocationsFromNP2(second, locOfSecond);
//            createLocationsFromNP2(first, locOfFirst);
//            locations.addAll(locOfSecond);
//            for(String loc : locOfFirst)
//                if (!loc.equals("exit")) {
//                    String new_loc = loc + ";" + second.getText();
//                    locations.add(new_loc);
//                }
//        }
//    }

//    private void createPGFromNP(NanoPromelaParser.StmtContext root, Set<String> locations, Set<PGTransition<String, String>> transitions) {
//        if (root.assstmt() != null || root.chanreadstmt() != null || root.chanwritestmt() != null || root.atomicstmt() != null || root.skipstmt() != null) {
//            /* The sub-statements are only [root] and [exit]                    */
//            String exit = "exit";
//            String base = root.getText();
//            locations.add(exit);
//            locations.add(base);
//            if(base.equals("skip"))
//                transitions.add(new PGTransition<>(base, "true", "nothing", exit));
//            else
//                transitions.add(new PGTransition<>(base, "true", base, exit));
//        } else if (root.ifstmt() != null) {
//        /* The sub-statements are [root], [exit], and the sub-statements of
//        all op.stmt() where op is a member of root.ifstmt().option() */
//            String exit = "exit";
//            String base = root.getText();
//            locations.add(exit);
//            locations.add(base);
//            List<NanoPromelaParser.OptionContext> options = root.ifstmt().option();
//            for(NanoPromelaParser.OptionContext option : options){
//                Set<String> locOfOpt = new HashSet<>();
//                Set<PGTransition<String, String>> transOfOpt = new HashSet<>();
//                createPGFromNP(option.stmt(), locOfOpt, transOfOpt);
//                locations.addAll(locOfOpt);
//                transitions.addAll(transOfOpt);
//                for(PGTransition trans_of_opt : transOfOpt){
//                    if(trans_of_opt.getFrom().equals(option.stmt().getText()))
//                        transitions.add(new PGTransition<String, String>(base, option.boolexpr().getText() + " && " + trans_of_opt.getCondition(), (String)trans_of_opt.getAction(), (String)trans_of_opt.getTo()));
//                }
//            }
//        } else if (root.dostmt() != null) {
//        /* The sub-statements are [root], [exit], and locations [𝑠𝑢𝑏;root]
//        where 𝑠𝑢𝑏 is a sub-statement of some op in root.dostmt().option() */
//            String exit = "exit";
//            String base = root.getText();
//            locations.add(exit);
//            locations.add(base);
//            List<NanoPromelaParser.OptionContext> options = root.dostmt().option();
//            String none_of_conds = "";
//            for(NanoPromelaParser.OptionContext option : options){
//                none_of_conds = none_of_conds + "!" + option.boolexpr().getText() + " && ";
//                Set<String> locOfOpt = new HashSet<>();
//                Set<PGTransition<String, String>> transOfOpt = new HashSet<>();
//                createPGFromNP(option.stmt(), locOfOpt, transOfOpt);
//                for(String loc : locOfOpt)
//                    if(!loc.equals("exit")) {
//                        locations.add(loc + ";" + base);
//                        helper(NanoPromelaFileReader.pareseNanoPromelaString(loc + ";" + base), transitions);
//                    }
//                for(PGTransition trans :transOfOpt) {
//                    if (trans.getFrom().equals(option.stmt().getText()) && trans.getTo().equals(exit))
//                        transitions.add(new PGTransition<>(base, option.boolexpr().getText() + " && " + trans.getCondition(), (String) trans.getAction(), base));
//                    else if(trans.getFrom().equals(option.stmt().getText()))
//                        transitions.add(new PGTransition<>(base, option.boolexpr().getText() + " && " + trans.getCondition(), (String) trans.getAction(), trans.getTo() + ";" +base));
//                }
//            }
//            none_of_conds.substring(0, none_of_conds.length()-4);
//            // transition for if all the conditions od the do statement are false
//            transitions.add(new PGTransition<>(base, none_of_conds, "nothing", exit));
//        } else { // ;
//            /* The sub-statements are locations of the form [𝑠𝑢𝑏;root.stmt(1)] where 𝑠𝑢𝑏 is a sub-statement of root.stmt(0) plus all the substatements of root.stmt(1) */
//            NanoPromelaParser.StmtContext first = root.stmt(0);
//            NanoPromelaParser.StmtContext second = root.stmt(1);
//            Set<String> locOfFirst = new HashSet<>();
//            Set<String> locOfSecond = new HashSet<>();
//            Set<PGTransition<String, String>> transOfFirst = new HashSet<>();
//            Set<PGTransition<String, String>> transOfSecond = new HashSet<>();
//            createPGFromNP(second, locOfSecond, transOfSecond);
//            createPGFromNP(first, locOfFirst, transOfFirst);
//            locations.addAll(locOfSecond);
//            transitions.addAll(transOfSecond);
//            for(String loc : locOfFirst)
//                if (!loc.equals("exit")) {
//                    String new_loc = loc + ";" + second.getText();
//                    locations.add(new_loc);
//                    for (PGTransition trans_of_first : transOfFirst)
//                        if (trans_of_first.getFrom().equals(loc) && trans_of_first.getTo().equals("exit"))
//                            transitions.add(new PGTransition<>(new_loc, trans_of_first.getCondition(), (String) trans_of_first.getAction(), second.getText()));
//                        else if(trans_of_first.getFrom().equals(loc))
//                            transitions.add(new PGTransition<>(new_loc, trans_of_first.getCondition(), (String) trans_of_first.getAction(), trans_of_first.getTo() + ";" + second.getText()));
//                    }
//        }
//    }

//    private void helper(NanoPromelaParser.StmtContext root, Set<PGTransition<String,String>> transitions){
//        NanoPromelaParser.StmtContext first = root.stmt(0);
//        NanoPromelaParser.StmtContext second = root.stmt(1);
//        Set<String> locOfFirst = new HashSet<>();
//        Set<PGTransition<String, String>> transOfFirst = new HashSet<>();
//        createPGFromNP(first, locOfFirst, transOfFirst);
//        for(String loc : locOfFirst)
//            if (!loc.equals("exit")) {
//                String new_loc = loc + ";" + second.getText();
//                for (PGTransition trans_of_first : transOfFirst)
//                    if (trans_of_first.getFrom().equals(loc) && trans_of_first.getTo().equals("exit"))
//                        transitions.add(new PGTransition<>(new_loc, trans_of_first.getCondition(), (String) trans_of_first.getAction(), second.getText()));
//                    else if(trans_of_first.getFrom().equals(loc))
//                        transitions.add(new PGTransition<>(new_loc, trans_of_first.getCondition(), (String) trans_of_first.getAction(), trans_of_first.getTo() + ";" + second.getText()));
//            }
//    }

    private TransitionSystem<Pair<Integer, Integer>, Pair<String, Pair<Integer, Integer>>, String> boxTS(int i0, int j0) {
        TransitionSystem<Pair<Integer, Integer>, Pair<String, Pair<Integer, Integer>>, String> ts = new TransitionSystem();
        ts.addAtomicProposition("not_solved");
        for (int i = 1; i <= 7; i++) {
            for (int j = 1; i <= 7; i++) {
                Pair<Integer, Integer> new_state = new Pair<>(i, j);
                if (i == i0 && j == j0)
                    ts.addInitialState(new_state);
                else
                    ts.addState(new_state);
                if (!((i == 2 && j == 2) || (i == 3 && j == 3) || (i == 5 && j == 3) || (i == 5 && j == 4) || (i == 6 && j == 4)))
                    ts.addToLabel(new_state, "not_solved");
                ts.addAction(new Pair("R", new Pair(i, j)));
                ts.addAction(new Pair("L", new Pair(i, j)));
                ts.addAction(new Pair("U", new Pair(i, j)));
                ts.addAction(new Pair("D", new Pair(i, j)));
            }
        }
        Set<Pair<Integer, Integer>> walls = new HashSet<>();
        for (int i = 1; i <= 7; i++) {
            walls.add(new Pair<>(i, 1));
            walls.add(new Pair<>(i, 7));
            walls.add(new Pair<>(1, i));
            walls.add(new Pair<>(7, i));
        }
        walls.add(new Pair<>(2, 5));
        for (Pair<Integer, Integer> state : ts.getStates()) {
            if (!walls.contains(new Pair<>(state.first, state.second + 1)))
                ts.addTransition(new TSTransition<>(state, new Pair("R", new Pair(state.first, state.second - 1)), new Pair<>(state.first, state.second + 1)));
            if (!walls.contains(new Pair<>(state.first, state.second - 1)))
                ts.addTransition(new TSTransition<>(state, new Pair("L", new Pair(state.first, state.second + 1)), new Pair<>(state.first, state.second - 1)));
            if (!walls.contains(new Pair<>(state.first - 1, state.second)))
                ts.addTransition(new TSTransition<>(state, new Pair("U", new Pair(state.first + 1, state.second)), new Pair<>(state.first - 1, state.second)));
            if (!walls.contains(new Pair<>(state.first + 1, state.second)))
                ts.addTransition(new TSTransition<>(state, new Pair("D", new Pair(state.first - 1, state.second)), new Pair<>(state.first + 1, state.second)));
            for (Pair<Integer, Integer> other_state : ts.getStates()) {
                if (!state.equals(other_state) && !other_state.equals(new Pair(state.first, state.second - 1)))
                    ts.addTransition(new TSTransition<>(state, new Pair<>("R", other_state), state));
                if (!state.equals(other_state) && !other_state.equals(new Pair(state.first, state.second + 1)))
                    ts.addTransition(new TSTransition<>(state, new Pair<>("L", other_state), state));
                if (!state.equals(other_state) && !other_state.equals(new Pair(state.first + 1, state.second)))
                    ts.addTransition(new TSTransition<>(state, new Pair<>("U", other_state), state));
                if (!state.equals(other_state) && !other_state.equals(new Pair(state.first - 1, state.second)))
                    ts.addTransition(new TSTransition<>(state, new Pair<>("D", other_state), state));
            }
        }
        return ts;
    }

    private TransitionSystem<Pair<Integer, Integer>, Pair<String, Pair<Integer, Integer>>, String> playerTS(int i0, int j0) {
        TransitionSystem<Pair<Integer, Integer>, Pair<String, Pair<Integer, Integer>>, String> ts = new TransitionSystem();
        for (int i = 1; i <= 7; i++) {
            for (int j = 1; i <= 7; i++) {
                Pair<Integer, Integer> new_state = new Pair<>(i, j);
                if (i == i0 && j == j0)
                    ts.addInitialState(new_state);
                else
                    ts.addState(new_state);
                ts.addAction(new Pair("R", new Pair(i, j)));
                ts.addAction(new Pair("L", new Pair(i, j)));
                ts.addAction(new Pair("U", new Pair(i, j)));
                ts.addAction(new Pair("D", new Pair(i, j)));
            }
        }
        Set<Pair<Integer, Integer>> walls = new HashSet<>();
        for (int i = 1; i <= 7; i++) {
            walls.add(new Pair<>(i, 1));
            walls.add(new Pair<>(i, 7));
            walls.add(new Pair<>(1, i));
            walls.add(new Pair<>(7, i));
        }
        walls.add(new Pair<>(2, 5));
        for (Pair<Integer, Integer> state : ts.getStates()) {
            if (!walls.contains(new Pair<>(state.first, state.second + 1)))
                ts.addTransition(new TSTransition<>(state, new Pair("R", new Pair(state.first, state.second)), new Pair<>(state.first, state.second + 1)));
            if (!walls.contains(new Pair<>(state.first, state.second - 1)))
                ts.addTransition(new TSTransition<>(state, new Pair("L", new Pair(state.first, state.second)), new Pair<>(state.first, state.second - 1)));
            if (!walls.contains(new Pair<>(state.first - 1, state.second)))
                ts.addTransition(new TSTransition<>(state, new Pair("U", new Pair(state.first, state.second)), new Pair<>(state.first - 1, state.second)));
            if (!walls.contains(new Pair<>(state.first + 1, state.second)))
                ts.addTransition(new TSTransition<>(state, new Pair("D", new Pair(state.first, state.second)), new Pair<>(state.first + 1, state.second)));
        }
        return ts;
    }

    public TransitionSystem sokobanTS() {
        TransitionSystem<Pair<Integer, Integer>, Pair<String, Pair<Integer, Integer>>, String> box3_2 = boxTS(3, 2);
        TransitionSystem<Pair<Integer, Integer>, Pair<String, Pair<Integer, Integer>>, String> box3_3 = boxTS(3, 3);
        TransitionSystem<Pair<Integer, Integer>, Pair<String, Pair<Integer, Integer>>, String> box3_5 = boxTS(3, 5);
        TransitionSystem<Pair<Integer, Integer>, Pair<String, Pair<Integer, Integer>>, String> box4_5 = boxTS(4, 5);
        TransitionSystem<Pair<Integer, Integer>, Pair<String, Pair<Integer, Integer>>, String> box5_4 = boxTS(5, 4);
        TransitionSystem<Pair<Integer, Integer>, Pair<String, Pair<Integer, Integer>>, String> player = playerTS(2, 3);
        Set<Pair<String, Pair<Integer, Integer>>> handshaking = box3_2.getActions();
        return interleave(interleave(interleave(interleave(interleave(box3_2, box3_3, handshaking), box3_5, handshaking), box4_5, handshaking), box5_4, handshaking), player, handshaking);
    }


    /**
     * Creates a transition system from a transition system and an automaton.
     *
     * @param <Sts>  Type of states in the transition system.
     * @param <Saut> Type of states in the automaton.
     * @param <A>    Type of actions in the transition system.
     * @param <P>    Type of atomic propositions in the transition system, which is
     *               also the type of the automaton alphabet.
     * @param ts     The transition system.
     * @param aut    The automaton.
     * @return The product of {@code ts} with {@code aut}.
     */

    public <Sts, Saut, A, P> TransitionSystem<Pair<Sts, Saut>, A, Saut> product(TransitionSystem<Sts, A, P> ts,
                                                                                Automaton<Saut, P> aut) {
        TransitionSystem<Pair<Sts, Saut>, A, Saut> ts_a = new TransitionSystem();
        Set<Saut> a_states = aut.getStates();
        for (Sts ts_state : ts.getStates()) {
            for (Saut a_state : a_states) {
                Pair<Sts, Saut> new_state = new Pair(ts_state, a_state);
                if(ts.getInitialStates().contains(ts_state)){
                    if(aut.isSecondState(a_state, ts.getLabel(ts_state)))
                        ts_a.addInitialState(new_state);
                }
                ts_a.addState(new_state);
                ts_a.addToLabel(new_state, a_state);
            }
        }
        //ts_a.addAllAtomicPropositions(aut.getStates());
        ts_a.addAllActions(ts.getActions());
        for (TSTransition<Sts, A> trans : ts.getTransitions()) {
            for (Saut a_from_state : a_states) {
                Map<Set<P>, Set<Saut>> a_trans = aut.getTransitions().get(a_from_state);
                for (Set<P> l_set : a_trans.keySet()) {
                        if(ts.getLabel(trans.getTo()).equals(l_set)) {
                            for (Saut to_a_state : a_trans.get(l_set)) {
                                Pair<Sts, Saut> from = new Pair(trans.getFrom(), a_from_state);
                                Pair<Sts, Saut> to = new Pair(trans.getTo(), to_a_state);
                                ts_a.addTransition(new TSTransition<>(from, trans.getAction(), to));
                            }
                        }
                }
            }
        }
        removeUnreachableStates(ts_a);
        return ts_a;
    }

    /**
     * Verify that a system satisfies an omega regular property.
     *
     * @param <S>    Type of states in the transition system.
     * @param <Saut> Type of states in the automaton.
     * @param <A>    Type of actions in the transition system.
     * @param <P>    Type of atomic propositions in the transition system, which is
     *               also the type of the automaton alphabet.
     * @param ts     The transition system.
     * @param aut    A Büchi automaton for the words that do not satisfy the
     *               property.
     * @return A VerificationSucceeded object or a VerificationFailed object
     * with a counterexample.
     */
    public <S, A, P, Saut> VerificationResult<S> verifyAnOmegaRegularProperty(TransitionSystem<S, A, P> ts,
                                                                              Automaton<Saut, P> aut) {
        TransitionSystem<Pair<S, Saut>, A, Saut> ts_a = product(ts, aut);
        Set<Pair<S, Saut>> r = new HashSet<>(); // set of visited states in outer DFS
        List<Pair<S, Saut>> u = new LinkedList<>(); // stack for outer DFS
        Set<Pair<S, Saut>> t = new HashSet<>(); // set of visited states in inner DFS
        List<Pair<S, Saut>> v = new LinkedList<>(); // stack for inner DFS
        boolean cycle_found = false;
        Pair<S, Saut> s = getsNotInSet(ts_a.getInitialStates(), r);
        while(s!=null && !cycle_found){
            // reachable cycle
            u.add(0, s);
            r.add(s);
            boolean con = true;
            while(con){
                Pair<S,Saut> s_tag = u.get(0);
                if(s_tag == null)
                    con = false;
                else {
                    Set<Pair<S, Saut>> s_tag_post = post(ts_a, s_tag);
                    Pair<S, Saut> s_tag_tag = getsNotInSet(s_tag_post, r);
                    if (s_tag_tag != null) {
                        u.add(0, s_tag_tag);
                        r.add(s_tag_tag);
                    } else {
                        u.remove(0);
                        if (aut.getAcceptingStates().contains(s_tag.second)) {
                            cycle_found = cycle_check(s_tag, t, v, ts_a);
                        }
                    }
                    con = !u.isEmpty() && !cycle_found;
                }
            }
            s = getsNotInSet(ts_a.getInitialStates(), r);
        }
        if(!cycle_found)
            return new VerificationSucceeded<S>();
        else{
            VerificationFailed<S> fail = new VerificationFailed<>();
            Collections.reverse(v);
            Collections.reverse(u);
            fail.setCycle(v.stream().map(state -> state.first).collect(Collectors.toList()));
            fail.setPrefix(u.stream().map(state -> state.first).collect(Collectors.toList()));
            return fail;
        }
    }


    private <Saut, S, A> boolean cycle_check(Pair<S,Saut> s, Set<Pair<S,Saut>> t, List<Pair<S,Saut>> v, TransitionSystem<Pair<S, Saut>, A, Saut> ts_a) {
        boolean cycle_found = false;
        v.add(0, s);
        t.add(s);
        boolean con = true;
        while(con){
            Pair<S,Saut> s_tag = v.get(0);
            if(s_tag == null)
                con = false;
            else {
                Set<Pair<S, Saut>> post_s_tag = post(ts_a, s_tag);
                if (post_s_tag.contains(s))
                    cycle_found = true;
                else {
                    Pair<S, Saut> s_tag_tag = getsNotInSet(post_s_tag, t);
                    if (s_tag_tag != null) {
                        v.add(0, s_tag_tag);
                        t.add(s_tag_tag);
                    } else
                        v.remove(0);
                }
                con = !v.isEmpty() && !cycle_found;
            }
        }
        return cycle_found;
    }

    private <Saut, S> Pair<S,Saut> getsNotInSet(Set<Pair<S,Saut>> initialStates, Set<Pair<S,Saut>> r) {
        for(Pair<S,Saut> state : initialStates)
            if(!r.contains(state))
                return state;
        return null;
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
        MultiColorAutomaton<?, L> gnba = toGNBA(ltl);
        return GNBA2NBA(gnba);
    }

    public <L> MultiColorAutomaton toGNBA(LTL<L> ltl) {
        Set<LTL> closure = getClosure(ltl);
        Set<Set<LTL>> states = filterNotYesodi(closure);
        Set initials = getLTLGNBAInitials(states, ltl);
        Set accepting = getLTLGNBAAcceptings(states);
        Map<Set<LTL>, Integer> colorsMap = getColorsMap(states);
        Set deletionSet = createDeletionSet(states);
        Set<Pair> connectableStates = filterNotConnectable(states, deletionSet);
        MultiColorAutomaton gnba = new MultiColorAutomaton();
        for (Set<LTL> B:states) {
            gnba.addState(B);
            if(initials.contains(B))
                gnba.setInitial(B);
            if(accepting.contains(B))
                gnba.setAccepting(B, colorsMap.get(B));
            for(Set<LTL> Btag: states){
                if(!deletionSet.contains(new Pair<>(B, Btag))){
                    gnba.addTransition(
                            B,
                            new HashSet(Arrays.asList(
                                    getAPOfState(B)
                            )),
                            Btag
                    );
                }
            }
        }
        return gnba;
    }

    private <L> List<AP<L>> getAPOfState(Set<LTL> B) {
        List ans  = new ArrayList();
        for (LTL l: B) {
            if(l instanceof AP){
                ans.add((AP)l);
            }
        }
        return ans;
    }

    private Set<Pair> filterNotConnectable(Set<Set<LTL>> states, Set deletionSet) {
        Set ans = new HashSet();
        for (Set<LTL> B: states) {
            for (Set<LTL> Btag: states) {
                Pair candidate = new Pair<>(B,Btag);
                if(!deletionSet.contains(candidate)){
                    ans.add(candidate);
                }
            }
        }
        return ans;
    }

    private Set createDeletionSet(Set<Set<LTL>> states) {
        Set ans = new HashSet();
        for (Set<LTL> B: states) {
            for (Set<LTL> Btag: states) {
                for (LTL l: B) {
                    if(LTLWrapper.createLTLWrapper(l).derivesDeletion(Btag)){
                        ans.add(new Pair<>(B, Btag));
                    }
                }
            }
        }
        return ans;
    }

    private Map<Set<LTL>, Integer> getColorsMap(Set<Set<LTL>> states) {
        int counter = 0;
        Map ans = new HashMap();
        for (Set<LTL> s: states){
            for (LTL l: s) {
                if(LTLWrapper.createLTLWrapper(l).derivesAccepting(s)){
                    if(!ans.keySet().contains(l)){
                        ans.put(l, counter);
                        counter++;                    }
                }
            }
        }
        return ans;
    }

    private Set getLTLGNBAAcceptings(Set<Set<LTL>> states) {
        Set ans = new HashSet();
        for (Set<LTL> s: states){
            for (LTL l: s) {
                if(LTLWrapper.createLTLWrapper(l).derivesAccepting(s)){
                    ans.add(s);
                }
            }
        }
        return ans;
    }

    private <L> Set getLTLGNBAInitials(Set<Set<LTL>> states, LTL<L> ltl) {
        Set ans = new HashSet();
        for (Set<LTL> s: states) {
            if(s.contains(ltl))
                ans.add(s);
        }
        return ans;
    }

    private Set<Set<LTL>> filterNotYesodi(Set<LTL> closure) {
        Set ans = new HashSet();
        for (Set<LTL> s: Util.powerSet(closure)) {
            for (LTL l: s) {
                if(!LTLWrapper.createLTLWrapper(l).derivesNotYesodi(s)){
                    ans.add(s);
                }
            }
        }
        return ans;
    }

    private <L> Set getClosure(LTL<L> ltl) {
        LTLWrapper wrapper = LTLWrapper.createLTLWrapper(ltl);
        return wrapper.getSub();
    }

    /**
     * A translation of a Generalized Büchi Automaton (GNBA) to a
     * Nondeterministic Büchi Automaton (NBA).
     *
     * @param <L>    Type of resultant automaton transition alphabet
     * @param mulAut An automaton with a set of accepting states (colors).
     * @return An equivalent automaton with a single set of accepting states.
     */
    public <L> Automaton<?, L> GNBA2NBA(MultiColorAutomaton<?, L> mulAut) {
        Automaton ans = new Automaton();
        createReplications(ans, mulAut);
        defineTransitions(ans, mulAut);
        defineAcceptingStates(ans, mulAut);
        defineInitialStates(ans, mulAut);
        return ans;
    }

    private <L> void defineInitialStates(Automaton ans, MultiColorAutomaton<?, L> mulAut) {
        int minColor = Integer.MAX_VALUE;
        for (int color : mulAut.getColors()) {
            minColor = Math.min(minColor, color);
        }
        for (Object state : mulAut.getInitialStates()) {
            ans.setInitial(new Pair<>(state, minColor));
        }
    }

    private <L> void defineAcceptingStates(Automaton ans, MultiColorAutomaton<?, L> mulAut) {
        int minColor = Integer.MAX_VALUE;
        for (int color : mulAut.getColors()) {
            minColor = Math.min(minColor, color);
        }
        for (Object state : mulAut.getAcceptingStates(minColor)) {
            ans.setAccepting(new Pair<>(state, minColor));
        }
    }

    private <L> void defineTransitions(Automaton ans, MultiColorAutomaton<?, L> mulAut) {
        int k = mulAut.getColors().size();
        for (int color : mulAut.getColors()) {
            defineTransitions(color, (color + 1) % k, ans, mulAut);
        }
    }

    private <L> void defineTransitions(int color, int nextColor, Automaton ans, MultiColorAutomaton<?, L> mulAut) {
        for (Object state : mulAut.getStates()) {
            for (Set<L> symbol : mulAut.getTransitions().get(state).keySet()) {
                for (Object stateTag : mulAut.getTransitions().get(state).get(symbol)) {
                    if (mulAut.getAcceptingStates(color).contains(state)) {
                        ans.addTransition(
                                new Pair<>(state, color),
                                symbol,
                                new Pair<>(stateTag, nextColor)
                        );
                    } else {
                        ans.addTransition(
                                new Pair<>(state, color),
                                symbol,
                                new Pair<>(stateTag, color)
                        );
                    }
                }
            }
        }
    }

    private <L> Automaton createReplications(Automaton ans, MultiColorAutomaton<?, L> mulAut) {
        for (int color : mulAut.getColors()) {
            addStates(ans, generateStates(color, mulAut));
        }
        return ans;
    }

    private <L> List<Pair<?, Integer>> generateStates(int color, MultiColorAutomaton<?, L> mulAut) {
        List<Pair<?, Integer>> ans = new ArrayList<>();
        for (Object state : mulAut.getStates()) {
            ans.add(new Pair<>(state, color));
        }
        return ans;
    }

    private static <State> void addStates(Automaton<State, ?> a, List<State> states) {
        for (State s : states) {
            a.addState(s);
        }
    }

    public <S, A, AP> TransitionSystem<S, A, AP> createTransitionSystem() {
        return new TransitionSystem<>();
    }
}
