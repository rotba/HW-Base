package il.ac.bgu.cs.formalmethodsintro.base.ltl_wrapper;

import il.ac.bgu.cs.formalmethodsintro.base.ltl.AP;
import il.ac.bgu.cs.formalmethodsintro.base.ltl.LTL;

import java.util.HashSet;
import java.util.Set;

public class WAP  extends LTLWrapper{
    private AP ltl;
    public WAP(AP ltl) {
        super(ltl);
        this.ltl=ltl;
    }

    @Override
    public Set<LTL> getSub() {
        HashSet ans = new HashSet();
        ans.add(ltl);
        ans.add(not(ltl));
        return ans;
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
