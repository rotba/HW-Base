package il.ac.bgu.cs.formalmethodsintro.base.ltl_wrapper;

import il.ac.bgu.cs.formalmethodsintro.base.ltl.LTL;
import il.ac.bgu.cs.formalmethodsintro.base.ltl.Next;

import java.util.HashSet;
import java.util.Set;

public class WNext extends LTLWrapper {
    private Next ltl;

    public WNext(Next ltl) {
        super(ltl);
        this.ltl = ltl;
    }


    @Override
    public Set<LTL> getSub() {
        HashSet ans = new HashSet();
        ans.addAll(LTLWrapper.createLTLWrapper(ltl.getInner()).getSub());
        ans.add(ltl);
        ans.add(not(ltl));
        return ans;
    }

    @Override
    public boolean derivesAccepting(Set<LTL> s) {
        return false;
    }

    @Override
    public boolean isNext() {
        return true;
    }

    @Override
    public boolean derivesDeletion(Set<LTL> B, Set<LTL> btag) {
        return !btag.contains(ltl.getInner());
    }

    @Override
    public boolean derivesNotYesodi(Set<LTL> s) {
        return false;
    }
}
