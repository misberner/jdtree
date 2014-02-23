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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Information about the least common ancestor (LCA) in a {@link MultiDTree}.
 * 
 * @author Malte Isberner
 *
 * @param <D> discriminator type
 * @param <O> outcome type
 */
public class MDTLCAInfo<D, O> {
	
	/**
	 * The least common ancestor node.
	 */
	@Nonnull
	public final MDTNode<D, O> leastCommonAncestor;
	
	/**
	 * The outcome of the LCA leading to the subtree containing the
	 * first node of the LCA query.
	 */
	@Nullable
	public final O firstOutcome;
	/**
	 * The outcome of the LCA leading to the subtree containing the
	 * second node of the LCA query.
	 */
	@Nullable
	public final O secondOutcome;

	/**
	 * Constructor.
	 * @param leastCommonAncestor the least common ancestor node
	 * @param firstOutcome the outcome for the subtree containing the first node
	 * @param secondOutcome the outcome for the subtree containing the second node
	 */
	public MDTLCAInfo(MDTNode<D,O> leastCommonAncestor, O firstOutcome, O secondOutcome) {
		this.leastCommonAncestor = leastCommonAncestor;
		this.firstOutcome = firstOutcome;
		this.secondOutcome = secondOutcome;
	}

}
