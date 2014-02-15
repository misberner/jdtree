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

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.ParametersAreNullableByDefault;

/**
 * Visitor interface for traversing a {@link BinaryDTree}.
 * <p>
 * Note that this is not a <i>visitor</i> in the classical sense
 * of the visitor pattern (i.e., realizing a mere double dispatch). Instead,
 * it controls the behavior of a combined pre-/postorder traversal through the tree.
 * 
 * 
 * @author Malte Isberner
 *
 * @param <D> discriminator type
 * @param <P> type of data transferred from parent to children
 * @param <C> type of data transferred from children to parent
 */
@ParametersAreNonnullByDefault
public interface BDTVisitor<D,P,C> {
	
	@ParametersAreNullableByDefault
	public static class ChildData<P> {
		@Nullable
		private P falseChildData;
		@Nullable
		private P trueChildData;
		
		public void setFalseChildData(P falseChildData) {
			this.falseChildData = falseChildData;
		}
		
		public void setTrueChildData(P trueChildData) {
			this.trueChildData = trueChildData;
		}
		
		public void setChildData(P childData) {
			this.falseChildData = childData;
			this.trueChildData = childData;
		}
		
		@Nullable
		public P getFalseChildData() {
			return falseChildData;
		}
		
		@Nullable
		public P getTrueChildData() {
			return trueChildData;
		}
		
		void reset() {
			setChildData(null);
		}
	}

	/**
	 * This method is called when an inner node is visited for the first time, i.e.,
	 * before its children have been visited.
	 * <p>
	 * The return value indicates whether the children should in fact be visited. Note that
	 * if {@code false} is returned, {@link #visitInnerPost(BDTNode,Object,Object,Object)}
	 * will not be called.
	 * 
	 * @param innerNode the inner node that is being visited
	 * @param parentData the data originating from the parent node (or from the user,
	 * if the node is the root node)
	 * @param childData data structure in which the data to be passed to the children is stored
	 * @return {@code true} if the traversal should continue with the children of this node,
	 * {@code false} otherwise
	 */
	public boolean visitInnerPre(BDTNode<D> innerNode, @Nullable P parentData, ChildData<P> childData);
	
	/**
	 * This method is called when an inner node is visited for the second time, i.e.,
	 * after its children have been visited.
	 * <p>
	 * The return value of this method is passed as child data to the parent (or to the user,
	 * if the node is the root node).
	 * 
	 * @param innerNode the inner node that is being visited
	 * @param parentData the data originating from the parent node (or from the user,
	 * if the node is the root node)
	 * @param falseChildData the data originating from visiting the "false" child
	 * @param trueChildData the data originating from visiting the "true" child
	 * @return the data resulting from visiting the specified node
	 */
	@Nullable
	public C visitInnerPost(BDTNode<D> innerNode, @Nullable P parentData, @Nullable C falseChildData, @Nullable C trueChildData);
	
	/**
	 * This method is called when a leaf is visited.
	 * 
	 * @param leaf the leaf that is being visited
	 * @param parentData the data originating from the parent node (or from the user,
	 * if the node is the root node)
	 * @return the data resulting from visiting the specified node
	 */
	@Nullable
	public C visitLeaf(BDTNode<D> leaf, @Nullable P parentData);
}
