package com.github.misberner.jdtree.multi;

public class MDTLCAInfo<D, O> {
	
	public final MDTNode<D, O> leastCommonAncestor;
	public final O firstOutcome;
	public final O secondOutcome;

	public MDTLCAInfo(MDTNode<D,O> leastCommonAncestor, O firstOutcome, O secondOutcome) {
		this.leastCommonAncestor = leastCommonAncestor;
		this.firstOutcome = firstOutcome;
		this.secondOutcome = secondOutcome;
	}

}
