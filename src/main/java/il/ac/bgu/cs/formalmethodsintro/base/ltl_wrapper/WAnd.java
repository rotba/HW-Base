package il.ac.bgu.cs.formalmethodsintro.base.ltl_wrapper;

import il.ac.bgu.cs.formalmethodsintro.base.ltl.And;
import il.ac.bgu.cs.formalmethodsintro.base.ltl.LTL;

import java.util.HashSet;
import java.util.Set;

public class WAnd extends LTLWrapper {
    private And ltl;
    public WAnd(And ltl) {
        this.ltl = ltl;
    }

    @Override
    public boolean derivesDeletion(Set<LTL> btag) {
        return false;
    }

    @Override
    public Set<LTL> getSub() {
        Set ans = new HashSet();
        Set s1 = createLTLWrapper(ltl.getLeft()).getSub();
        Set s2 = createLTLWrapper(ltl.getRight()).getSub();
        Set s3 = new HashSet();
        s3.add(ltl);
        s3.add(LTL.not(ltl));
        ans.addAll(s1);
        ans.addAll(s2);
        ans.addAll(s3);
        return ans;
    }

    @Override
    public boolean derivesNotYesodi(Set<LTL> s) {
        return false;
    }

    @Override
    public boolean derivesAccepting(Set<LTL> s) {
        return false;
    }
}
