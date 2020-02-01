package il.ac.bgu.cs.formalmethodsintro.base.ltl_wrapper;

import il.ac.bgu.cs.formalmethodsintro.base.ltl.LTL;
import il.ac.bgu.cs.formalmethodsintro.base.ltl.Until;

import java.util.HashSet;
import java.util.Set;

public class WUntil extends LTLWrapper {
    private Until ltl;
    public WUntil(Until ltl) {
        super(ltl);
        this.ltl=ltl;
    }

    @Override
    public Set<LTL> getSub() {
        HashSet ans  = new HashSet();
        ans.addAll(LTLWrapper.createLTLWrapper(ltl.getLeft()).getSub());
        ans.addAll(LTLWrapper.createLTLWrapper(ltl.getRight()).getSub());
        ans.add(ltl);
        ans.add(not(ltl));
        return ans;
    }

    @Override
    public boolean derivesAccepting(Set<LTL> s) {
        return s.contains(ltl.getRight());
    }

    @Override
    public boolean derivesDeletion(Set<LTL> B, Set<LTL> btag) {
        return !B.contains(ltl.getRight()) && !btag.contains(ltl);
    }

    @Override
    public boolean derivesNotYesodi(Set<LTL> s) {
        return !(s.contains(ltl.getLeft()) || s.contains(ltl.getRight()));
    }

    @Override
    public boolean isUntil() {
        return true;
    }
}
