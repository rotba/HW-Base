package il.ac.bgu.cs.formalmethodsintro.base.ltl_wrapper;

import il.ac.bgu.cs.formalmethodsintro.base.ltl.LTL;
import il.ac.bgu.cs.formalmethodsintro.base.ltl.Next;

import java.util.Set;

public class WNext extends LTLWrapper {
    private Next ltl;
    public WNext(Next ltl) {
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
