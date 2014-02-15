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
package com.github.misberner.jdtree.binary;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import com.github.misberner.jdtree.binary.BDTVisitor.ChildData;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicate;

/**
 * A flexible binary discrimination tree.
 * 
 * @author Malte Isberner
 *
 * @param <D> discriminator type
 */
@ParametersAreNonnullByDefault
public class BinaryDTree<D> {

	
	@Nonnull
	private final List<BDTNode<D>> nodes;
	@Nonnull
	private final List<BDTNode<D>> leaves;
	@Nonnull
	private final List<BDTNode<D>> innerNodes;
	
	@Nonnull
	private final BDTNode<D> root;
	
	public BinaryDTree() {
		this.nodes = new ArrayList<>();
		this.leaves = new ArrayList<>();
		this.innerNodes = new ArrayList<>();
		root = createLeaf(null);
	}
	
	private BinaryDTree(BDTNode<D> root, List<BDTNode<D>> nodes, List<BDTNode<D>> innerNodes, List<BDTNode<D>> leaves) {
		this.root = root;
		this.nodes = nodes;
		this.innerNodes = innerNodes;
		this.leaves = leaves;
	}
	
	@Nonnull
	public BDTNode<D> getRoot() {
		return root;
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
	
	private static class SplitRecord<D,E> {
		private final BDTNode<D> thisTreeNode;
		private final BDTNode<E> splitTreeNode;
		
		public SplitRecord(BDTNode<D> thisTreeNode, BDTNode<E> splitTreeNode) {
			this.thisTreeNode = thisTreeNode;
			this.splitTreeNode = splitTreeNode;
		}
	}
	
	/**
	 * Splits a leaf according to another discrimination tree.
	 * <p>
	 * This method is aliasing-safe: if {@code leaf} is a leaf of {@code splitTree}, this method
	 * performs as if working on a copy of {@code splitTree} nade <i>before</i> any split
	 * operation has been performed (even in the case of aliasing, no such copy
	 * has to be made).
	 * <p>
	 * The transformer {@link discTransformer} is invoked exactly once per inner node in
	 * {@code splitTree}.
	 * 
	 * @param leaf the leaf to split
	 * @param splitTree the tree according to which to split
	 * @param discTransformer the transformer for transforming the discriminators.
	 * @return an array containing a mapping from the node ids of splitTree to the ids of
	 * the newly created/split nodes. 
	 */
	@Nonnull
	public <E> int[] split(BDTNode<D> leaf, BinaryDTree<E> splitTree, Function<? super E,? extends D> discTransformer) {
		Deque<SplitRecord<D,E>> stack = new ArrayDeque<>();
		int[] result = new int[splitTree.getNumNodes()];
		
		stack.push(new SplitRecord<>(leaf, splitTree.getRoot()));
		
		while(!stack.isEmpty()) {
			SplitRecord<D,E> rec = stack.pop();
			BDTNode<D> thisNode = rec.thisTreeNode;
			BDTNode<E> splitNode = rec.splitTreeNode;
			result[splitNode.getNodeId()] = thisNode.getNodeId();
			
			if(splitNode.isInner() && splitNode != leaf) {
				D newDiscriminator = discTransformer.apply(splitNode.getDiscriminator());
				BDTNode<D>[] children = split(thisNode, newDiscriminator);
				
				stack.push(new SplitRecord<>(children[0], splitNode.getFalseChild()));
				stack.push(new SplitRecord<>(children[1], splitNode.getTrueChild()));
			}
		}
		
		return result;
	}
	
	@Nonnull
	public int[] split(BDTNode<D> leaf, BinaryDTree<? extends D> splitTree) {
		return split(leaf, splitTree, Functions.<D>identity());
	}
	
	
	@Nonnull
	public BDTNode<D>[] split(BDTNode<D> leaf, D discriminator) {
		int oldLeafId = leaf.getLeafId();
		BDTNode<D> repLeaf = createLeaf(leaf, oldLeafId);
		leaves.set(oldLeafId, repLeaf);
		BDTNode<D> newLeaf = createLeaf(leaf);
		leaf.makeInner(innerNodes.size(), discriminator, repLeaf, newLeaf);
		innerNodes.add(leaf);
		return leaf.getChildren();
	}
	
	@Nonnull
	public BDTNode<D> getNode(int nodeId) {
		return nodes.get(nodeId);
	}
	
	@Nonnull
	public BDTNode<D> getLeaf(int leafId) {
		return leaves.get(leafId);
	}
	
	@Nullable
	public ExtractedBDTree<D> extract(Predicate<? super BDTNode<D>> pred) {
		return extract(root, pred);
	}
	
	private static class ExtractRecord<D> {
		private BDTNode<D> thisTreeNode;
		private final BDTNode<D> extractedTreeNode;
		
		public ExtractRecord(BDTNode<D> thisTreeNode, BDTNode<D> extractedTreeNode) {
			this.thisTreeNode = thisTreeNode;
			this.extractedTreeNode = extractedTreeNode;
		}
	}
	
	@SuppressWarnings("unchecked")
	@Nullable
	public ExtractedBDTree<D> extract(BDTNode<D> node, Predicate<? super BDTNode<D>> pred) {
		if(!pred.apply(node)) {
			return null;
		}
		List<BDTNode<D>> originalNodes = new ArrayList<>();
		BinaryDTree<D> extractedDTree = new BinaryDTree<>();
		originalNodes.add(null);
		
		Deque<ExtractRecord<D>> stack = new ArrayDeque<>();
		
		stack.push(new ExtractRecord<>(node, extractedDTree.getRoot()));
		
		while(!stack.isEmpty()) {
			ExtractRecord<D> rec = stack.pop();
			
			BDTNode<D> thisNode = rec.thisTreeNode;
			BDTNode<D> falseChild = null, trueChild = null;
			
			if(thisNode.isInner()) {
				falseChild = thisNode.getFalseChild();
				if(!pred.apply(falseChild)) {
					falseChild = null;
				}
				trueChild = thisNode.getTrueChild();
				if(!pred.apply(trueChild)) {
					trueChild = null;
				}
			}
			
			if(falseChild == null) {
				if(trueChild == null) {
					originalNodes.set(rec.extractedTreeNode.getNodeId(), thisNode);
				}
				else {
					rec.thisTreeNode = trueChild;
					stack.push(rec);
				}
			}
			else if(trueChild == null) {
				rec.thisTreeNode = falseChild;
				stack.push(rec);
			}
			else { // falseChild != null && trueChild != null
				BDTNode<D> extractedNode = rec.extractedTreeNode;
				originalNodes.set(extractedNode.getNodeId(), thisNode);
				BDTNode<D>[] children = extractedDTree.split(extractedNode, thisNode.getDiscriminator());
				// Placeholders for the newly created nodes
				originalNodes.add(null);
				originalNodes.add(null);
				
				stack.push(new ExtractRecord<>(falseChild, children[0]));
				stack.push(new ExtractRecord<>(trueChild, children[1]));
			}
		}
		
		BDTNode<D>[] origArray = new BDTNode[originalNodes.size()];
		originalNodes.toArray(origArray);
		return new ExtractedBDTree<>(this, extractedDTree, origArray);
	}
	
	@Nullable
	public BinaryDTree<D> extractTree(Predicate<? super BDTNode<D>> pred) {
		ExtractedBDTree<D> extractInfo = extract(pred);
		if(extractInfo != null) {
			return extractInfo.getExtractedTree();
		}
		return null;
	}
	
	@Nonnull
	public BDTNode<D> leastCommonAncestor(BDTNode<D> n1, BDTNode<D> n2) {
		int d1 = n1.getDepth();
		int d2 = n2.getDepth();
		
		int ddiff = d1 - d2;
		
		BDTNode<D> curr1, curr2;
		if(ddiff < 0) {
			curr1 = n2;
			curr2 = n1;
			ddiff = -ddiff;
		}
		else {
			curr1 = n1;
			curr2 = n2;
		}
		
		for(int i = 0; i < ddiff; i++) {
			curr1 = curr1.getParent();
		}
		
		while(curr1 != curr2) {
			curr1 = curr1.getParent();
			curr2 = curr2.getParent();
		}
		
		return curr1;
	}
	
	@Nonnull
	public D separator(BDTNode<D> n1, BDTNode<D> n2) {
		if(n1 == n2) {
			throw new IllegalArgumentException("Identical nodes cannot be separated");
		}
		BDTNode<D> lca = leastCommonAncestor(n1, n2);
		return lca.getDiscriminator();
	}
	
	
	
	@Nullable
	public BinaryDTree<D> extractTree(BDTNode<D> node, Predicate<? super BDTNode<D>> pred) {
		return extractTree(root, pred);
	}
	
	
	public <E> BinaryDTree<E> transform(Function<? super D,? extends E> discTransformer) {
		List<BDTNode<E>> newNodes = new ArrayList<>(nodes.size());
		newNodes.addAll(Collections.<BDTNode<E>>nCopies(nodes.size(), null));
		List<BDTNode<E>> newLeaves = new ArrayList<>(leaves.size());
		newLeaves.addAll(Collections.<BDTNode<E>>nCopies(leaves.size(), null));
		List<BDTNode<E>> newInnerNodes = new ArrayList<>(innerNodes.size());
		newInnerNodes.addAll(Collections.<BDTNode<E>>nCopies(innerNodes.size(), null));
		
		BDTNode<D> origRoot = root;
		
		BDTNode<E> newRoot = new BDTNode<>(null, origRoot.nodeId, origRoot.typeId);
		
		Deque<BDTNode<E>> stack = new ArrayDeque<>();
		stack.push(newRoot);
		
		while(!stack.isEmpty()) {
			BDTNode<E> newNode = stack.pop();
			int id = newNode.nodeId;
			newNodes.set(id, newNode);
			BDTNode<D> origNode = nodes.get(id);
			
			if(origNode.isInner()) {
				int tid = newNode.typeId;
				E newDiscr = discTransformer.apply(origNode.getDiscriminator());
				
				BDTNode<D> origFalseChild = origNode.getFalseChild();
				BDTNode<D> origTrueChild = origNode.getTrueChild();
				
				BDTNode<E> newFalseChild = new BDTNode<>(newNode, origFalseChild.nodeId, origFalseChild.typeId);
				BDTNode<E> newTrueChild = new BDTNode<>(newNode, origTrueChild.nodeId, origTrueChild.typeId);
				newNode.makeInner(tid, newDiscr, newFalseChild, newTrueChild);
				
				stack.push(newFalseChild);
				stack.push(newTrueChild);
				
				newInnerNodes.set(newNode.typeId, newNode);
			}
			else {
				newLeaves.set(newNode.typeId, newNode);
			}
		}
		
		return new BinaryDTree<>(newRoot, newNodes, newInnerNodes, newLeaves);
	}
	
	@Nonnull
	private BDTNode<D> createLeaf(BDTNode<D> parent, int leafId) {
		BDTNode<D> leaf = new BDTNode<>(parent, nodes.size(), leafId);
		nodes.add(leaf);
		return leaf;
	}
	
	@Nonnull
	private BDTNode<D> createLeaf(BDTNode<D> parent) {
		BDTNode<D> leaf = createLeaf(parent, leaves.size());
		leaves.add(leaf);
		return leaf;
	}
	
	
	private static final class VisitRecord<D,P,C> {
		private final BDTNode<D> node;
		private final P parentData;
		private final VisitRecord<D,P,C> parentRec;
		
		private int state = 0;
		private C falseChildData;
		private C trueChildData;
		
		public VisitRecord(BDTNode<D> node, P parentData, VisitRecord<D,P,C> parentRec) {
			this.node = node;
			this.parentData = parentData;
			this.parentRec = parentRec;
		}
	}
	
	public <P,C> C visit(BDTVisitor<D,P,C> visitor, BDTNode<D> subTreeRoot, P rootData) {
		Deque<VisitRecord<D,P,C>> stack = new ArrayDeque<>();
		stack.push(new VisitRecord<D,P,C>(subTreeRoot, rootData, null));
		
		ChildData<P> childrenData = new ChildData<>();
		
		while(!stack.isEmpty()) {
			VisitRecord<D, P, C> rec = stack.peek();
			
			BDTNode<D> node = rec.node;
			if(rec.state == 0 && node.isInner()) {
				childrenData.reset();
				if(visitor.visitInnerPre(node, rec.parentData, childrenData)) {
					stack.push(new VisitRecord<D,P,C>(node.getFalseChild(), childrenData.getFalseChildData(), rec));
					stack.push(new VisitRecord<D,P,C>(node.getTrueChild(), childrenData.getTrueChildData(), rec));
					rec.state++;
				}
				else {
					stack.pop();
				}
			}
			else {
				stack.pop();
				
				C childData;
				if(rec.state == 0) { // && !node.isInner
					childData = visitor.visitLeaf(node, rec.parentData);
				}
				else { // rec.state == 3
					childData = visitor.visitInnerPost(node, rec.parentData, rec.falseChildData, rec.trueChildData);
				}
				
				VisitRecord<D,P,C> parentRec = rec.parentRec;
				
				if(parentRec == null) {
					return childData; // sub tree root
				}
				if(parentRec.state == 1) {
					parentRec.trueChildData = childData;
				}
				else { // parentRec.state == 2
					assert parentRec.state == 2;
					parentRec.falseChildData = childData;
				}
				parentRec.state++;
			}
		}
		
		throw new AssertionError("This line should not be reached");
	}
}
