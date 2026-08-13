/**
 * <copyright>
 *
 * Copyright (c) 2015 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *
 * </copyright>
 */
package org.eclipse.sphinx.platform.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.platform.internal.messages.Messages;

public class DirectedGraph<V> {

	private Set<Edge> edges;
	private boolean allowLoops;
	private boolean allowMultipleEdges;
	private DirectedGraphInternal graphInternal;

	public DirectedGraph(boolean allowLoops, boolean allowMultipleEdges) {
		edges = new HashSet<Edge>();
		graphInternal = new DirectedGraphInternal();
		this.allowLoops = allowLoops;
		this.allowMultipleEdges = allowMultipleEdges;
	}

	protected boolean assertVertexExist(V vertex) {
		if (containsVertex(vertex)) {
			return true;
		} else if (vertex == null) {
			throw new NullPointerException();
		} else {
			throw new IllegalArgumentException(NLS.bind(Messages.error_NoSuchVertex, vertex));
		}
	}

	public boolean addVertex(V vertex) {
		if (vertex == null) {
			throw new NullPointerException();
		} else if (containsVertex(vertex)) {
			return false;
		} else {
			graphInternal.addVertex(vertex);
			return true;
		}
	}

	public boolean containsVertex(V vertex) {
		return graphInternal.getVertexSet().contains(vertex);
	}

	public Set<Edge> outgoingEdgesOf(V vertex) {
		return graphInternal.outgoingEdgesOf(vertex);
	}

	public Set<Edge> incomingEdgesOf(V vertex) {
		return graphInternal.incomingEdgesOf(vertex);
	}

	public int inDegreeOf(V vertex) {
		return graphInternal.inDegreeOf(vertex);
	}

	public int outDegreeOf(V vertex) {
		return graphInternal.outDegreeOf(vertex);
	}

	public Edge addEdge(V srcVertex, V trgtVertex) {
		assertVertexExist(srcVertex);
		assertVertexExist(trgtVertex);

		if (!allowMultipleEdges && containsEdge(srcVertex, trgtVertex)) {
			return null;
		}

		if (!allowLoops && srcVertex.equals(trgtVertex)) {
			throw new IllegalArgumentException(Messages.error_LoopsNotAllowed);
		}

		Edge edge = new Edge(srcVertex, trgtVertex);

		if (containsEdge(edge)) {

			return null;
		} else {
			edges.add(edge);
			graphInternal.addEdgeToTouchingVertices(edge);

			return edge;
		}
	}

	public boolean containsEdge(Edge edge) {
		return edges.contains(edge);
	}

	public boolean containsEdge(V srcVertex, V trgtVertex) {
		return getEdge(srcVertex, trgtVertex) != null;
	}

	public Edge getEdge(V srcVertex, V trgtVertex) {
		return graphInternal.getEdge(srcVertex, trgtVertex);
	}

	public void clear() {
		edges.clear();
		graphInternal.clear();
	}

	protected class DirectedGraphInternal {

		private Map<V, DirectedEdgeContainer> verticesMap = new LinkedHashMap<V, DirectedEdgeContainer>();

		protected void addVertex(V v) {
			verticesMap.put(v, null);
		}

		public Set<V> getVertexSet() {
			return verticesMap.keySet();
		}

		public int inDegreeOf(V vertex) {
			return getEdgeContainer(vertex).incomingEdges.size();
		}

		public Set<Edge> incomingEdgesOf(V vertex) {
			return getEdgeContainer(vertex).getUnmodifiableIncomingEdges();
		}

		public int outDegreeOf(V vertex) {
			return getEdgeContainer(vertex).outgoingEdges.size();
		}

		public Set<Edge> outgoingEdgesOf(V vertex) {
			return getEdgeContainer(vertex).getUnmodifiableOutgoingEdges();
		}

		public void addEdgeToTouchingVertices(Edge edge) {
			V source = edge.getSource();
			V target = edge.getTarget();

			getEdgeContainer(source).addOutgoingEdge(edge);
			getEdgeContainer(target).addIncomingEdge(edge);
		}

		public Edge getEdge(V srcVertex, V trgtVertex) {
			if (containsVertex(srcVertex) && containsVertex(trgtVertex)) {
				DirectedEdgeContainer edgeContainer = getEdgeContainer(srcVertex);
				Iterator<Edge> edgesIterator = edgeContainer.outgoingEdges.iterator();

				while (edgesIterator.hasNext()) {
					Edge edge = edgesIterator.next();

					if (edge.getTarget().equals(trgtVertex)) {
						return edge;
					}
				}
			}
			return null;
		}

		private DirectedEdgeContainer getEdgeContainer(V vertex) {
			if (getVertexSet().contains(vertex)) {
				DirectedEdgeContainer edgeContainer = verticesMap.get(vertex);
				if (edgeContainer == null) {
					edgeContainer = new DirectedEdgeContainer();
					verticesMap.put(vertex, edgeContainer);
				}
				return edgeContainer;
			} else if (vertex == null) {
				throw new NullPointerException();
			} else {
				throw new IllegalArgumentException(NLS.bind(Messages.error_NoSuchVertex, vertex));
			}
		}

		public void clear() {
			verticesMap.clear();
		}
	}

	protected class DirectedEdgeContainer {

		Set<Edge> incomingEdges;
		Set<Edge> outgoingEdges;
		private transient Set<Edge> unmodifiableIncoming = null;
		private transient Set<Edge> unmodifiableOutgoing = null;

		protected DirectedEdgeContainer() {
			incomingEdges = new HashSet<Edge>();
			outgoingEdges = new HashSet<Edge>();
		}

		public Set<Edge> getUnmodifiableIncomingEdges() {
			if (unmodifiableIncoming == null) {
				unmodifiableIncoming = Collections.unmodifiableSet(incomingEdges);
			}
			return unmodifiableIncoming;
		}

		public Set<Edge> getUnmodifiableOutgoingEdges() {
			if (unmodifiableOutgoing == null) {
				unmodifiableOutgoing = Collections.unmodifiableSet(outgoingEdges);
			}
			return unmodifiableOutgoing;
		}

		public void addIncomingEdge(Edge edge) {
			incomingEdges.add(edge);
		}

		public void addOutgoingEdge(Edge edge) {
			outgoingEdges.add(edge);
		}

		public void removeIncomingEdge(Edge edge) {
			incomingEdges.remove(edge);
		}

		public void removeOutgoingEdge(Edge edge) {
			outgoingEdges.remove(edge);
		}
	}

	public class Edge {

		private V sourceVertex;
		private V targetVertex;

		public Edge(V srcVertex, V trgtVertex) {
			sourceVertex = srcVertex;
			targetVertex = trgtVertex;
		}

		public V getSource() {
			return sourceVertex;
		}

		public V getTarget() {
			return targetVertex;
		}

		@Override
		public String toString() {
			return NLS.bind(Messages.toString_Edge, sourceVertex, targetVertex);
		}
	}
}
