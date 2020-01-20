package il.ac.bgu.cs.formalmethodsintro.base.ltl_wrapper;

import il.ac.bgu.cs.formalmethodsintro.base.ltl.*;

import java.util.Set;

public abstract class LTLWrapper extends LTL {
    public static LTLWrapper createLTLWrapper(LTL ltl){
        if(ltl instanceof And)
            return new WAnd((And) ltl);
        if(ltl instanceof AP)
            return new WAP((AP) ltl);
        if(ltl instanceof Next)
            return new WNext((Next) ltl);
        if(ltl instanceof Not)
            return new WNot((Not) ltl);
        if(ltl instanceof TRUE)
            return new WTRUE((TRUE) ltl);
        if(ltl instanceof Until)
            return new WUntil((Until) ltl);
        return null;
    }


    public abstract Set<LTL> getSub();
    public abstract boolean derivesAccepting(Set<LTL> s);
    public abstract boolean derivesDeletion(Set<LTL> btag);
    public abstract boolean derivesNotYesodi(Set<LTL> s);
//    public abstract boolean isNotYesodiWith(WAnd l);
//    public abstract boolean isNotYesodiWith(WAP l);
//    public abstract boolean isNotYesodiWith(WNext l);
//    public abstract boolean isNotYesodiWith(WNot l);
//    public abstract boolean isNotYesodiWith(WTRUE l);
//    public abstract boolean isNotYesodiWith(WUntil l);

}


