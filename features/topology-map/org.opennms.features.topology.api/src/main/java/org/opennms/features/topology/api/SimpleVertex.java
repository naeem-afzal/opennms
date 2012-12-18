/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.api;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlTransient;

import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.Vertex;

public abstract class SimpleVertex extends AbstractVertex {

	private List<Edge> m_edges = new ArrayList<Edge>();

	/**
	 * @param namespace
	 * @param id
	 */
	public SimpleVertex(String namespace, String id) {
		super(namespace, id);
	}

	/**
	 * @param namespace
	 * @param id
	 * @param x
	 * @param y
	 */
	public SimpleVertex(String namespace, String id, int x, int y) {
		this(namespace, id);
		setX(x);
		setY(y);
	}

	@XmlTransient
	@Override
	public List<Edge> getEdges() {
		return m_edges;
	}

	@Override
	public void addEdge(Edge edge) {
		m_edges.add(edge);
	}

	@Override
	public void removeEdge(Edge edge) {
		m_edges.remove(edge);
	}
}
