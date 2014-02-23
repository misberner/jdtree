/*
 * Copyright (c) 2014 by Malte Isberner (https://github.com/misberner).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.misberner.jdtree.multi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import com.github.misberner.jdtree.NodeType;
import com.google.common.base.Function;

/**
 * A flexible multi-valued discrimination tree.
 * 
 * @author Malte Isberner
 *
 * @param <D> discriminator type
 * @param <O> outcome type
 */
@ParametersAreNonnullByDefault
public class MultiDTree<D,O> {

	
	@Nonnull
	private final List<MDTNode<D,O>> nodes;
	@Nonnull
	private final List<MDTNode<D,O>> leaves;
	@Nonnull
	private final List<MDTNode<D,O>> innerNodes;
	
	@Nonnull
	private final MDTNode<D,O> root;
	
	public MultiDTree() {
		this.nodes = new ArrayList<>();
		this.leaves = new ArrayList<>();
		this.innerNodes = new ArrayList<>();
		root = createLeaf(null, null);
	}
	
	private MultiDTree(MDTNode<D,O> root, List<MDTNode<D,O>> nodes, List<MDTNode<D,O>> innerNodes, List<MDTNode<D,O>> leaves) {
		this.root = root;
		this.nodes = nodes;
		this.innerNodes = innerNodes;
		this.leaves = leaves;
	}
	
	@Nonnull
	public MDTNode<D,O> getRoot() {
		return root;
	}
	
	
	public List<? extends MDTNode<D,O>> getNodes() {
		return Collections.unmodifiableList(nodes);
	}
	public List<? extends MDTNode<D,O>> getLeaves() {
		return Collections.unmodifiableList(leaves);
	}
	
	public List<? extends MDTNode<D,O>> getInnerNodes() {
		return Collections.unmodifiableList(innerNodes);
	}
	@Nonnegative
	public int getNumLeaves() {
		return leaves.size();
	}
	
	@Nonnegative
	public int getNumInnerNodes() {
		return innerNodes.size();
	}
	
	@Nonnegative
	public int getNumNodes() {
		return nodes.size();
	}
	
	@Nonnegative
	public int getNumNodes(NodeType type) {
		if(type == NodeType.ANY) {
			return getNumNodes();
		}
		if(type == NodeType.INNER) {
			return getNumInnerNodes();
		}
		return getNumLeaves();
	}
	
	
	@Nonnull
	@SafeVarargs
	public final void split(MDTNode<D,O> leaf, D discriminator, O repOutcome, O... otherOutcomes) {
		split(leaf, discriminator, repOutcome, Arrays.asList(otherOutcomes));
	}
	
	@Nonnull
	public void split(MDTNode<D,O> leaf, D discriminator, O repOutcome, Collection<? extends O> otherOutcomes) {
		int oldLeafId = leaf.getLeafId();
		MDTNode<D,O> repLeaf = replaceLeaf(leaf, repOutcome, oldLeafId);
		leaves.set(oldLeafId, repLeaf);
		
		
		
		Map<O,MDTNode<D,O>> childMap = createChildMap(repOutcome, otherOutcomes);
		
		childMap.put(repOutcome, repLeaf);
		
		for(O otherOutcome : otherOutcomes) {
			MDTNode<D,O> newLeaf = createLeaf(leaf, otherOutcome);
			childMap.put(otherOutcome, newLeaf);
		}
		
		makeInner(leaf, discriminator, childMap);
	}
	
	private void makeInner(MDTNode<D,O> leaf, D discriminator, Map<O,MDTNode<D,O>> childMap) {
		leaf.makeInner(innerNodes.size(), discriminator, childMap);
		innerNodes.add(leaf);
	}
	
	@Nonnull
	public MDTNode<D,O> getNode(int nodeId) {
		return nodes.get(nodeId);
	}
	
	@Nonnull
	public MDTNode<D,O> getLeaf(int leafId) {
		return leaves.get(leafId);
	}
	
	
	
	@Nonnull
	public MDTLCAInfo<D,O> leastCommonAncestor(MDTNode<D,O> n1, MDTNode<D,O> n2) {
		int d1 = n1.getDepth();
		int d2 = n2.getDepth();
		
		int ddiff = d1 - d2;
		
		MDTNode<D,O> curr1, curr2;
		boolean swapped = false;
		if(ddiff < 0) {
			curr1 = n2;
			curr2 = n1;
			ddiff = -ddiff;
			swapped = true;
		}
		else {
			curr1 = n1;
			curr2 = n2;
		}
		
		for(int i = 0; i < ddiff; i++) {
			curr1 = curr1.getParent();
		}
		
		MDTNode<D, O> prev1 = null;
		MDTNode<D, O> prev2 = null; 
		while(curr1 != curr2) {
			prev1 = curr1;
			prev2 = curr2;
			curr1 = curr1.getParent();
			curr2 = curr2.getParent();
		}
		
		
		O firstOutcome = (prev1 != null) ? prev1.parentOutcome : null;
		O secondOutcome = (prev2 != null) ? prev2.parentOutcome : null;
		
		if(swapped) {
			O tmp = firstOutcome;
			firstOutcome = secondOutcome;
			secondOutcome = tmp;
		}

		return new MDTLCAInfo<>(curr1, firstOutcome, secondOutcome);
	}
	
	@Nonnull
	public D separator(MDTNode<D,O> n1, MDTNode<D,O> n2) {
		if(n1 == n2) {
			throw new IllegalArgumentException("Identical nodes cannot be separated");
		}
		MDTNode<D,O> lca = leastCommonAncestor(n1, n2).leastCommonAncestor;
		return lca.getDiscriminator();
	}
	
	@Nonnull
	public MDTNode<D,O> sift(Function<? super D,? extends O> evalFunc) {
		return sift(root, evalFunc);
	}
	
	@Nonnull
	public MDTNode<D,O> sift(MDTNode<D,O> start, Function<? super D,? extends O> evalFunc) {
		MDTNode<D,O> curr = start;
		
		while(curr.isInner()) {
			D discr = curr.getDiscriminator();
			O outcome = evalFunc.apply(discr);
			curr = child(curr, outcome);
		}
		
		return curr;
	}
	
	public MDTNode<D,O> child(MDTNode<D,O> inner, O outcome) {
		MDTNode<D,O> child = inner.getChild(outcome);
		if(child == null) {
			child = createLeaf(inner, outcome);
			inner.putChild(outcome, child);
		}
		return child;
	}
	
	@Nonnull
	public <X> MDTNode<D,O> sift(@Nullable X object, MDTEvaluator<? super X, ? super D,? extends O> evaluator) {
		return sift(root, object, evaluator);
	}
	
	@Nonnull
	public <X> MDTNode<D,O> sift(MDTNode<D,O> start, @Nullable X object, MDTEvaluator<? super X,? super D,? extends O> evaluator) {
		MDTNode<D,O> curr = start;
		
		while(curr.isInner()) {
			D discr = curr.getDiscriminator();
			O outcome = evaluator.evaluate(object, discr);
			curr = child(curr, outcome);
		}
		
		return curr;
	}
	
	@Nonnull
	private MDTNode<D,O> replaceLeaf(MDTNode<D,O> parent, O parentOutcome, int leafId) {
		MDTNode<D,O> leaf = new MDTNode<>(parent, parentOutcome, nodes.size(), leafId);
		nodes.add(leaf);
		leaves.set(leafId, leaf);
		return leaf;
	}
	
	@Nonnull
	private MDTNode<D,O> createLeaf(MDTNode<D,O> parent, O parentOutcome) {
		MDTNode<D,O> leaf = new MDTNode<>(parent, parentOutcome, nodes.size(), leaves.size());
		nodes.add(leaf);
		leaves.add(leaf);
		return leaf;
	}
	
	
	
	protected Map<O,MDTNode<D,O>> createChildMap(O repOutcome, Collection<? extends O> otherOutcomes) {
		return new HashMap<>();
	}
	
}
