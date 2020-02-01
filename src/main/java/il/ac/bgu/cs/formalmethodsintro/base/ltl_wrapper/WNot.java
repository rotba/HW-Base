package il.ac.bgu.cs.formalmethodsintro.base.ltl_wrapper;

import il.ac.bgu.cs.formalmethodsintro.base.ltl.LTL;
import il.ac.bgu.cs.formalmethodsintro.base.ltl.Not;
import il.ac.bgu.cs.formalmethodsintro.base.ltl.Until;

import java.util.HashSet;
import java.util.Set;

public class WNot extends LTLWrapper {
    private Not ltl;
    public WNot(Not ltl) {
        super(ltl);
        this.ltl=ltl;
    }

    @Override
    public Set<LTL> getSub() {
        HashSet ans = new HashSet();
        ans.addAll(LTLWrapper.createLTLWrapper(ltl.getInner()).getSub());
        ans.add(ltl);
        ans.add(ltl.getInner());
        return ans;
    }

    @Override
    public boolean derivesAccepting(Set<LTL> s) {
        if(ltl.getInner() instanceof Until){
            return true;
        }else{
            return false;
        }
    }

    @Override
    public boolean derivesDeletion(Set<LTL> B, Set<LTL> btag) {
        return false;
    }

    @Override
    public boolean derivesNotYesodi(Set<LTL> s) {
        if(s.contains(ltl.getInner())){
            return true;
        }
        if(ltl.getInner() instanceof Until){
            Until u = ((Until) ltl.getInner());
            if(s.contains(u.getRight())){
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean derivesNotMaximal(Set<LTL> s) {
        if(!s.contains(ltl)){
            if(!s.contains(ltl.getInner())){
                return true;
            }
        }
        return false;
    }


}
