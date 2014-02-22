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

import com.github.misberner.jdtree.NodeType;


public class FixedBDTNodeMap<V> extends AbstractBDTNodeMap<V> {

	private final V[] values;
	private final int offset;
	private final NodeType type;
	
	public FixedBDTNodeMap(V[] values, int offset, NodeType type) {
		this.values = values;
		this.offset = offset;
		this.type = type;
	}
	
	public FixedBDTNodeMap(V[] values, int offset) {
		this(values, offset, NodeType.ANY);
	}
	
	public FixedBDTNodeMap(V[] values, NodeType type) {
		this(values, 0, type);
	}
	
	public FixedBDTNodeMap(V[] values) {
		this(values, 0, NodeType.ANY);
	}

	@Override
	public V get(BDTNode<?> node) {
		int id = node.getId(type);
		if(id < offset) {
			return null;
		}
		id -= offset;
		if(id >= values.length) {
			return null;
		}
		return values[id];
	}

}
