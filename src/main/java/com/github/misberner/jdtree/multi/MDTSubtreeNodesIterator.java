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

import java.util.ArrayDeque;
import java.util.Deque;

import com.github.misberner.jdtree.NodeType;
import com.google.common.collect.AbstractIterator;


final class MDTSubtreeNodesIterator<D,O> extends AbstractIterator<MDTNode<D,O>> {
	
	private final Deque<MDTNode<D,O>> stack = new ArrayDeque<>();
	private final NodeType type;
	
	
	public MDTSubtreeNodesIterator(MDTNode<D,O> subtreeRoot, NodeType type) {
		stack.push(subtreeRoot);
		this.type = type;
	}


	@Override
	protected MDTNode<D,O> computeNext() {
		while(!stack.isEmpty()) {
			MDTNode<D,O> current = stack.pop();
			
			if(current.isInner()) {
				for(MDTNode<D,O> child : current.getChildren()) {
					stack.push(child);
				}
			}
			
			if(current.isOfType(type)) {
				return current;
			}
		}
		
		return endOfData();
	}
	
}
