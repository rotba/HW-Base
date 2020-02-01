package il.ac.bgu.cs.formalmethodsintro.base.ltl_wrapper;

import il.ac.bgu.cs.formalmethodsintro.base.ltl.And;
import il.ac.bgu.cs.formalmethodsintro.base.ltl.LTL;

import java.util.HashSet;
import java.util.Set;

public class WAnd extends LTLWrapper {
    private And ltl;
    public WAnd(And ltl) {
        super(ltl);
        this.ltl = ltl;
    }

    @Override
    public boolean derivesDeletion(Set<LTL> B, Set<LTL> btag) {
        return false;
    }

    @Override
    public Set<LTL> getSub() {
        Set ans = new HashSet();
        ans.addAll(createLTLWrapper(ltl.getLeft()).getSub());
        ans.addAll(createLTLWrapper(ltl.getRight()).getSub());
        ans.add(ltl);
        ans.add(not(ltl));
        return ans;
    }

    @Override
    public boolean derivesNotYesodi(Set<LTL> s) {
        return !(s.contains(ltl.getLeft()) && s.contains(ltl.getRight()));
    }

    @Override
    public boolean derivesAccepting(Set<LTL> s) {
        return false;
    }
}
