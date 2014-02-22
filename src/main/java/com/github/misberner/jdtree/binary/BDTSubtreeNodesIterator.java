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
import java.util.Deque;

import com.github.misberner.jdtree.NodeType;
import com.google.common.collect.AbstractIterator;


final class BDTSubtreeNodesIterator<D> extends AbstractIterator<BDTNode<D>> {
	
	private final Deque<BDTNode<D>> stack = new ArrayDeque<>();
	private final NodeType type;
	private final boolean reverse;
	
	public BDTSubtreeNodesIterator(BDTNode<D> subtreeRoot, NodeType type) {
		this(subtreeRoot, type, false);
	}
	
	public BDTSubtreeNodesIterator(BDTNode<D> subtreeRoot, NodeType type, boolean reverse) {
		stack.push(subtreeRoot);
		this.type = type;
		this.reverse = reverse;
	}


	@Override
	protected BDTNode<D> computeNext() {
		while(!stack.isEmpty()) {
			BDTNode<D> current = stack.pop();
			
			if(current.isInner()) {
				BDTNode<D> falseChild = current.getFalseChild();
				BDTNode<D> trueChild = current.getTrueChild();
				
				if(reverse) {
					stack.push(falseChild);
					stack.push(trueChild);
				}
				else {
					stack.push(trueChild);
					stack.push(falseChild);
				}
			}
			
			if(current.isOfType(type)) {
				return current;
			}
		}
		
		return endOfData();
	}
	
	

}
