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

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ExtractedBDTree<D> {
	
	@Nonnull
	private final BDTNodeMap<BDTNode<D>> origNodeMap;
	@Nonnull
	private final BinaryDTree<D> extractedTree;
	
	ExtractedBDTree(BinaryDTree<D> originalTree, BinaryDTree<D> extractedTree,
			BDTNode<D>[] origNodes) {
		this.extractedTree = extractedTree;
		this.origNodeMap = new FixedBDTNodeMap<>(origNodes);
	}
	
	
	@Nonnull
	public BinaryDTree<D> getExtractedTree() {
		return extractedTree;
	}
	
	
	@Nonnull
	public BDTNode<D> getOriginalNode(BDTNode<D> extractedNode) {
		BDTNode<D> origNode = origNodeMap.get(extractedNode);
		assert origNode != null;
		return origNode;
	}
	
	@Nonnull
	public BDTNodeMap<BDTNode<D>> getOriginalNodeMap() {
		return origNodeMap;
	}

}
