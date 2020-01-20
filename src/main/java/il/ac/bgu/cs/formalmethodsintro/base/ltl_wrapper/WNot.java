package il.ac.bgu.cs.formalmethodsintro.base.ltl_wrapper;

import il.ac.bgu.cs.formalmethodsintro.base.ltl.LTL;
import il.ac.bgu.cs.formalmethodsintro.base.ltl.Not;

import java.util.Set;

public class WNot extends LTLWrapper {
    private Not ltl;
    public WNot(Not ltl) {
        this.ltl=ltl;
    }

    @Override
    public Set<LTL> getSub() {
        return null;
    }

    @Override
    public boolean derivesAccepting(Set<LTL> s) {
        return false;
    }

    @Override
    public boolean derivesDeletion(Set<LTL> btag) {
        return false;
    }

    @Override
    public boolean derivesNotYesodi(Set<LTL> s) {
        return false;
    }
}
