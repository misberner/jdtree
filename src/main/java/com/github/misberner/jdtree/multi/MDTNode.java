package com.github.misberner.jdtree.multi;

import java.util.Collection;
import java.util.Map;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import com.github.misberner.jdtree.NodeType;


@ParametersAreNonnullByDefault
public class MDTNode<D, O> {
	
	@Nullable
	final MDTNode<D,O> parent;
	final O parentOutcome;
	
	final int nodeId;
	@Nonnegative
	private final int depth;
	int typeId;
	@Nullable
	D discriminator;
	
	@Nullable
	private Map<O,MDTNode<D,O>> children;
	
	
	public MDTNode(@Nullable MDTNode<D,O> parent, @Nullable O parentOutcome, @Nonnegative int nodeId, @Nonnegative int leafId) {
		this.parent = parent;
		this.nodeId = nodeId;
		this.typeId = leafId;
		if(parent == null) {
			this.depth = 0;
			this.parentOutcome = null;
		}
		else {
			this.depth = parent.depth + 1;
			this.parentOutcome = parentOutcome;
		}
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
	void makeInner(@Nonnegative int innerId, @Nullable D discriminator, Map<O,MDTNode<D, O>> children) {
		assert isLeaf() : "Can only turn a leaf node into an inner node";
		
		this.typeId = innerId;
		this.discriminator = discriminator;
		this.children = children;
	}
	
	/**
	 * Retrieves the parent of this node.
	 * @return the parent of this node, or {@code null} if this is the root node.
	 */
	@Nullable
	public MDTNode<D,O> getParent() {
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
	
	@Nonnull
	public Collection<MDTNode<D, O>> getChildren() {
		assert isInner() : "Only inner nodes have children";
		return children.values();
	}
	
	@Nonnull
	public MDTNode<D,O> getChild(O outcome) {
		assert isInner() : "Only inner nodes have children";
		return children.get(outcome);
	}

	
	public void putChild(O outcome, MDTNode<D,O> newChild) {
		assert isInner();
		children.put(outcome, newChild);
	}
}
