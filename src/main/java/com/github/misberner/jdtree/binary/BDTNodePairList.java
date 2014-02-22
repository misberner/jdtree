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

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import com.github.misberner.jdtree.NodeType;

public class BDTNodePairList<D> {
	
	private static final float DENSE_THRESHOLD = 0.75f;
	
	private final List<BDTNodePair<D>> nodePairs = new ArrayList<>();

	private int minId = Integer.MAX_VALUE;
	private int maxId = 0;
	
	private final NodeType type;
	
	public BDTNodePairList(NodeType forType) {
		this.type = forType;
	}
	
	public BDTNodePairList() {
		this(NodeType.ANY);
	}
	
	public void addPair(BDTNode<D> node1, BDTNode<D> node2) {
		BDTNodePair<D> pair = new BDTNodePair<>(node1, node2);
		nodePairs.add(pair);
		int id1 = node1.getId(type);
		
		if(id1 < minId) {
			this.minId = id1;
		}
		if(id1 > maxId) {
			this.maxId = id1;
		}
	}
	
	public boolean isDense() {
		return getDensity() >= DENSE_THRESHOLD;
	}
	
	public boolean isEmpty() {
		return nodePairs.isEmpty();
	}
	
	public int size() {
		return nodePairs.size();
	}
	
	public float getDensity() {
		if(isEmpty()) {
			return 1.0f;
		}
		
		int span = maxId - minId + 1;
		
		return span/(float)nodePairs.size();
	}
	
	
	public BDTNodeMap<BDTNode<D>> toNodeMap() {
		if(!isDense()) {
			BDTNode<D>[] array = toArray();
			return new FixedBDTNodeMap<>(array, minId, type);
		}
		Map<BDTNode<D>,BDTNode<D>> map = toMap();
		return new MapBDTNodeMap<D,BDTNode<D>>(map, type);
	}
	
	@SuppressWarnings("unchecked")
	private BDTNode<D>[] toArray() {
		int span = maxId - minId + 1;
		
		BDTNode<D>[] array = new BDTNode[span];
		
		for(BDTNodePair<D> pair : nodePairs) {
			int id = pair.node1.getId(type);
			array[id] = pair.node2;
		}
		
		return array;
	}
	
	private Map<BDTNode<D>,BDTNode<D>> toMap() {
		Map<BDTNode<D>,BDTNode<D>> result = new IdentityHashMap<>();
		
		for(BDTNodePair<D> pair : nodePairs) {
			result.put(pair.node1, pair.node2);
		}
		
		return result;
	}
}
