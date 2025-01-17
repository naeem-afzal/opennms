/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.features.topology.netutils.internal.operations;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.features.topology.api.AbstractOperation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.netutils.internal.ping.PingWindow;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.icmp.proxy.LocationAwarePingClient;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class PingOperation extends AbstractOperation {

    private LocationAwarePingClient pingClient;

    private MonitoringLocationDao monitoringLocationDao;

    private NodeDao nodeDao;

    public PingOperation(LocationAwarePingClient pingClient, MonitoringLocationDao monitoringLocationDao, NodeDao nodeDao) {
        this.pingClient = Objects.requireNonNull(pingClient);
        this.monitoringLocationDao = Objects.requireNonNull(monitoringLocationDao);
        this.nodeDao = Objects.requireNonNull(nodeDao);
    }

    @Override
    public void execute(final List<VertexRef> targets, final OperationContext operationContext) {
        final VertexRef target = targets.get(0);
        final Vertex vertex = getVertexItem(operationContext, target);
        final Optional<OnmsNode> node = getNodeIfAvailable(vertex);

        final List<String> locations = monitoringLocationDao.findAll().stream().map(OnmsMonitoringLocation::getLocationName).collect(Collectors.toList());
        final String defaultLocation = node.isPresent()
                ? node.get().getLocation().getLocationName()
                : MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID;

        final List<String> ipAddresses = node.isPresent()
                ? Lists.newArrayList(node.get().getIpInterfaces()).stream().map(eachInterface -> InetAddressUtils.str(eachInterface.getIpAddress())).collect(Collectors.toList())
                : Lists.newArrayList(vertex.getIpAddress());
        final String defaultIp = getDefaultIp(vertex, node);

        final String caption = String.format("Ping - %s (%s)", vertex.getLabel(), vertex.getIpAddress());
        new PingWindow(pingClient,
                locations, ipAddresses,
                defaultLocation, defaultIp,
                caption)
                .open();
    }

    private String getDefaultIp(Vertex vertex, Optional<OnmsNode> node) {
        if (hasValidIpAddress(vertex)) {
            return vertex.getIpAddress();
        }
        if (node.isPresent() && node.get().getPrimaryInterface() != null) {
            return InetAddressUtils.str(node.get().getPrimaryInterface().getIpAddress());
        }
        if (node.isPresent()) {
            return InetAddressUtils.str(node.get().getIpInterfaces().iterator().next().getIpAddress());
        }
        throw new IllegalStateException("The vertex does not have a ip address or a node assigned.");
    }

    @Override
    public boolean enabled(List<VertexRef> targets, OperationContext operationContext) {
        if (targets.size() == 1) {
            final Vertex vertexItem = getVertexItem(operationContext, targets.get(0));
            if (vertexItem != null) {
                return hasValidIpAddress(vertexItem) || hasValidNodeId(vertexItem);
            }
        }
        return false;
    }

    @Override
    public boolean display(final List<VertexRef> targets, final OperationContext operationContext) {
        return targets != null && targets.size() > 0;
    }

    @Override
    public String getId() {
        return "ping";
    }


    /**
     * Verifies that the provided vertex has a valid node assigned and the node has at least one ip address.
     *
     * @param vertex The vertex to check
     * @return True if a node with at least one ip address is assigned, false otherwise.
     */
    private boolean hasValidNodeId(Vertex vertex) {
        return vertex.getNodeID() != null && getNodeIfAvailable(vertex).isPresent();
    }

    private boolean hasValidIpAddress(Vertex vertexItem) {
        // Only enable if we actually have something to ping
        // Be aware that originally the ip address was also resolved via InetAddressUtils.getInetAddress(ipAddress)
        // but in cases were the ipAddress is not reachable, the response thread would block and causing
        // the ui to build the menubar very slowly. Therefore this was changed. See NMS-10452
        return !Strings.isNullOrEmpty(vertexItem.getIpAddress());
    }

    /**
     * Returns an Optional containing a node id {@link Vertex#getNodeID()} exists in the OpenNMS Database and that the node
     * has at least one ip interface.
     *
     * @param   vertex The vertex to verify.
     * @return  A non empty optional if a node with node id {@link Vertex#getNodeID()} exists and
     *          that node has at least one ip interface defined.
     */
    private Optional<OnmsNode> getNodeIfAvailable(Vertex vertex) {
        Objects.requireNonNull(vertex);
        Objects.requireNonNull(vertex.getNodeID());

        final OnmsNode node = nodeDao.get(vertex.getNodeID());
        if (node != null && !node.getIpInterfaces().isEmpty()) {
            return Optional.of(node);
        }
        return Optional.empty();
    }
}
