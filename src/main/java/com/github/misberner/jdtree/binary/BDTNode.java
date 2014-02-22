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

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import com.github.misberner.jdtree.NodeType;


/**
 * A node in a {@link BinaryDTree}.
 * 
 * @author Malte Isberner
 *
 * @param <D> discriminator type
 */
@ParametersAreNonnullByDefault
public class BDTNode<D> {
	
	private final BDTNode<D> parent;
	final int nodeId;
	private final int depth;
	int typeId;
	D discriminator;
	
	private BDTNode<D>[] children = null;

	/**
	 * Constructor. Constructs a new leaf.
	 * 
	 * @param parent the parent of this node, may be {@code null} if this is a root node
	 * @param nodeId the node id of this node
	 * @param leafId the leaf id of this (leaf) node
	 */
	public BDTNode(@Nullable BDTNode<D> parent, @Nonnegative int nodeId, @Nonnegative int leafId) {
		this.parent = parent;
		this.nodeId = nodeId;
		this.typeId = leafId;
		this.depth = (parent == null) ? 0 : parent.depth + 1;
	}
	
	/**
	 * Turns a leaf node into an inner node.
	 * 
	 * @param innerId the "inner node" id of this new inner node
	 * @param discriminator the discriminator to use at this new inner node
	 * @param falseChild the "false" child of this new inner node
	 * @param trueChild the "true" child of this new inner node
	 */
	@SuppressWarnings("unchecked")
	void makeInner(@Nonnegative int innerId, @Nullable D discriminator, BDTNode<D> falseChild, BDTNode<D> trueChild) {
		assert isLeaf() : "Can only turn a leaf node into an inner node";
		
		this.typeId = innerId;
		this.discriminator = discriminator;
		this.children = new BDTNode[2];
		this.children[0] = falseChild;
		this.children[1] = trueChild;
	}
	
	/**
	 * Retrieves the parent of this node.
	 * @return the parent of this node, or {@code null} if this is the root node.
	 */
	@Nullable
	public BDTNode<D> getParent() {
		return parent;
	}
	
	/**
	 * Retrieves the (global) node id of this node.
	 * @return the node id of this node
	 */
	@Nonnegative
	public int getNodeId() {
		return nodeId;
	}
	
	public int getId(NodeType type) {
		if(type == NodeType.ANY) {
			return nodeId;
		}
		if(type != getType()) {
			throw new IllegalArgumentException();
		}
		return typeId;
	}
	
	public boolean isOfType(NodeType type) {
		if(type == NodeType.ANY) {
			return true;
		}
		return (type == getType());
	}
	
	/**
	 * Retrieves the leaf id of this node.
	 * @return the leaf id of this node.
	 */
	@Nonnegative
	public int getLeafId() {
		assert isLeaf() : "Only leaves have valid leaf ids";
		return typeId;
	}
	
	/**
	 * Retrieves the "inner node" id of this node.
	 * @return the inner node id of this node
	 */
	@Nonnegative
	public int getInnerId() {
		assert isInner() : "Only inner nodes have valid inner node ids";
		return typeId;
	}
	
	/**
	 * Retrieves the depth of this node
	 * @return the depth of this node
	 */
	public int getDepth() {
		return depth;
	}
	
	/**
	 * Checks if this node is a leaf.
	 * @return {@code true} if this node is a leaf, {@code false} otherwise
	 */
	public boolean isLeaf() {
		return (children == null);
	}
	
	/**
	 * Checks if this node is an inner node.
	 * @return {@code true} if this node is an inner node, {@code false} otherwise
	 */
	public boolean isInner() {
		return (children != null);
	}
	
	public NodeType getType() {
		return (children != null) ? NodeType.INNER : NodeType.LEAF;
	}
	
	/**
	 * Retrieves the discriminator of this node. Calling this method is illegal
	 * if this node is not an inner node.
	 * @return the discriminator of this inner node
	 */
	@Nullable
	public D getDiscriminator() {
		assert isInner() : "Only inner nodes have discriminators";
		return discriminator;
	}
	
	/**
	 * Retrieves the "false" child of this node. Calling this method is illegal
	 * if this node is not an inner node.
	 * @return the "false" child of this node
	 */
	@Nonnull
	public BDTNode<D> getFalseChild() {
		assert isInner() : "Only inner nodes have children";
		return children[0];
	}
	
	/**
	 * Retrieves the "true" child of this node. Calling this method is illegal
	 * if this node is not an inner node.
	 * @return the "true" child of this inner node
	 */
	@Nonnull
	public BDTNode<D> getTrueChild() {
		assert isInner() : "Only inner nodes have children";
		return children[1];
	}

	/**
	 * Retrieves the children of this node. The result is an array of length {@code 2} in which
	 * the "false" child is stored at index {@code 0}, and the "true" child is stored at index
	 * {@code 1}. Calling this method is illegal if this node is not an inner node.
	 * <p>
	 * The result is returned as a defensive copy, thus modifying it will not change the tree.
	 * @return the children of this inner node
	 */
	@Nonnull
	public BDTNode<D>[] getChildren() {
		assert isInner() : "Only inner nodes have children";
		return children.clone();
	}
	
	/**
	 * Retrieves the child with the specified label. Calling this method is illegal if this
	 * node is not an inner node.
	 * 
	 * @param label the label specifying which child to retrieve
	 * @return the child for the respective label
	 */
	@Nonnull
	public BDTNode<D> getChild(boolean label) {
		assert isInner() : "Only inner nodes have children";
		return children[label ? 1 : 0];
	}
	
	void setFalseChild(BDTNode<D> newFalseChild) {
		assert newFalseChild.parent == this;
		children[0] = newFalseChild;
	}
	
	void setTrueChild(BDTNode<D> newTrueChild) {
		assert newTrueChild.parent == this;
		children[1] = newTrueChild;
	}
}
