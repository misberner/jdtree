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

import java.util.BitSet;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.github.misberner.jdtree.NodeType;
import com.google.common.base.Predicate;


@ParametersAreNonnullByDefault
public class BDTMarking implements Predicate<BDTNode<?>> {
	
	@Nonnull
	private final BitSet marking;
	private int offset = 0;
	private final NodeType type;
	
	public BDTMarking(BinaryDTree<?> dt, NodeType type) {
		this(dt, type, 0);
	}
	
	public BDTMarking(BinaryDTree<?> dt, int offset) {
		this(dt, NodeType.ANY, offset);
	}
	
	public BDTMarking(BinaryDTree<?> dt) {
		this(dt, NodeType.ANY, 0);
	}
	
	public BDTMarking(BinaryDTree<?> dt, NodeType type, int offset) {
		this.marking = new BitSet(dt.getNumNodes(type) - offset);
		this.type = type;
		this.offset = offset;
	}
	
	public void clear() {
		marking.clear();
	}
	
	
	public void markAndPropagate(BDTNode<?> node) {
		BDTNode<?> curr = node;
		
		while(curr != null && mark(curr)) {
			curr = curr.getParent();
		}
	}
	
	public boolean isMarked(BDTNode<?> node) {
		int id = nodeId(node);
		if(id < 0) {
			// shift offset & markings
			int shiftAmt = -id;
			offset -= shiftAmt;
			
			// FIXME this should be more efficient
			for(int idx = marking.length(); (idx = marking.previousSetBit(idx - 1)) >= 0;) {
				marking.set(idx + offset);
				marking.clear(idx);
			}
			id = 0;
		}
		return marking.get(id);
	}
	
	public boolean mark(BDTNode<?> node) {
		int id = node.getNodeId();
		boolean wasMarked = marking.get(id);
		if(!wasMarked) {
			marking.set(id);
			return true;
		}
		return false;
	}
	
	public boolean unmark(BDTNode<?> node) {
		int id = node.getNodeId();
		boolean wasMarked = marking.get(id);
		if(wasMarked) {
			marking.set(id, false);
		}
		return false;
	}
	
	@Override
	public boolean apply(BDTNode<?> node) {
		return isMarked(node);
	}
	
	private int nodeId(BDTNode<?> node) {
		int nodeId = node.getId(type);
		return nodeId - offset;
	}
	

}
