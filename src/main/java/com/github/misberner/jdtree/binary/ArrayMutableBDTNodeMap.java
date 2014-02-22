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

import java.util.Arrays;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import com.github.misberner.jdtree.NodeType;

@ParametersAreNonnullByDefault
public class ArrayMutableBDTNodeMap<V> implements MutableBDTNodeMap<V> {
	
	@Nonnull
	protected final BinaryDTree<?> dtree;
	@Nullable
	private final V initVal;
	@Nonnull
	private Object[] values;
	private int lastNum;
	
	private final NodeType type;
	
	public ArrayMutableBDTNodeMap(BinaryDTree<?> dtree, int num, @Nullable V initVal, NodeType type) {
		this.dtree = dtree;
		this.values = new Object[num];
		if(initVal != null) {
			Arrays.fill(values, initVal);
		}
		this.lastNum = num;
		this.initVal = initVal;
		
		this.type = type;
	}
	
	public ArrayMutableBDTNodeMap(BinaryDTree<?> dtree, V[] values, NodeType type) {
		this.dtree = dtree;
		this.values = values;
		this.lastNum = values.length;
		this.initVal = null;
		
		this.type = type;
	}
	
	public ArrayMutableBDTNodeMap(BinaryDTree<?> dtree, @Nullable V initial, NodeType type) {
		this(dtree, dtree.getNumNodes(type), initial, type);
	}
	
	public ArrayMutableBDTNodeMap(BinaryDTree<?> dtree, @Nullable V initial) {
		this(dtree, initial, NodeType.ANY);
	}
	
	public ArrayMutableBDTNodeMap(BinaryDTree<?> dtree, NodeType type) {
		this(dtree, (V)null, type);
	}

	@Override
	@SuppressWarnings("unchecked")
	public V get(BDTNode<?> node) {
		int id = node.getId(type);
		if(id >= lastNum) {
			lastNum = updateNumNodes();
			
			if(id >= lastNum) {
				throw new IllegalArgumentException();
			}
		}
		if(id >= values.length) {
			return null;
		}
		return (V)values[id];
	}

	@Override
	public void put(BDTNode<?> node, V data) {
		int id = node.getId(type);
		if(id >= lastNum || id >= values.length) {
			lastNum = updateNumNodes();
			if(id >= lastNum) {
				throw new IllegalArgumentException();
			}
			if(lastNum >= values.length) {
				ensureCapacity();
			}
		}
		values[id] = data;
	}
	
	@Override
	public V apply(BDTNode<?> node) {
		return get(node);
	}
	
	private void ensureCapacity() {
		int minNewCapacity = (int)(values.length * 1.5f);
		int newCapacity = Math.max(minNewCapacity, lastNum);
		
		Object[] newValues = new Object[newCapacity];
		System.arraycopy(values, 0, newValues, 0, values.length);
		Arrays.fill(newValues, values.length, newValues.length, initVal);
		this.values = newValues;
	}
	
	private int updateNumNodes() {
		if(dtree == null) {
			return lastNum;
		}
		return dtree.getNumNodes(type);
	}
}
