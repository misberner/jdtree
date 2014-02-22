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

import java.util.Map;

import com.github.misberner.jdtree.NodeType;

public class MapBDTNodeMap<D,V> extends AbstractBDTNodeMap<V> {
	
	private final Map<? super BDTNode<D>,? extends V> map;
	private final NodeType type;

	public MapBDTNodeMap(Map<? super BDTNode<D>,? extends V> map, NodeType type) {
		this.map = map;
		this.type = type;
	}
	
	public MapBDTNodeMap(Map<? super BDTNode<D>,? extends V> map) {
		this(map, NodeType.ANY);
	}

	@Override
	public V get(BDTNode<?> node) {
		if(!node.isOfType(type)) {
			throw new IllegalArgumentException(); // TODO message
		}
		return map.get(node);
	}

}
