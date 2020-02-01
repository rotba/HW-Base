package il.ac.bgu.cs.formalmethodsintro.base.ltl_wrapper;

import il.ac.bgu.cs.formalmethodsintro.base.ltl.LTL;
import il.ac.bgu.cs.formalmethodsintro.base.ltl.TRUE;

import java.util.Set;

public class WTRUE extends LTLWrapper {
    private final TRUE ltl;

    public WTRUE(TRUE ltl) {
        super(ltl);
        this.ltl = ltl;
    }

    @Override
    public Set<LTL> getSub() {
        return Set.of(ltl);
    }

    @Override
    public boolean derivesAccepting(Set<LTL> s) {
        return false;
    }

    @Override
    public boolean derivesDeletion(Set<LTL> B, Set<LTL> btag) {
        return false;
    }

    @Override
    public boolean derivesNotYesodi(Set<LTL> s) {
        return false;
    }
}
