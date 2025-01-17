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
package org.opennms.features.topology.plugins.topo.bsm.info;

import static org.opennms.netmgt.vaadin.core.UIHelper.createLabel;

import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.info.VertexInfoPanelItemProvider;
import org.opennms.features.topology.api.info.item.DefaultInfoPanelItem;
import org.opennms.features.topology.api.info.item.InfoPanelItem;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.plugins.topo.bsm.ApplicationVertex;
import org.opennms.features.topology.plugins.topo.bsm.BusinessServiceVertex;
import org.opennms.features.topology.plugins.topo.bsm.BusinessServicesStatusProvider;
import org.opennms.features.topology.plugins.topo.bsm.BusinessServicesTopologyProvider;
import org.opennms.features.topology.plugins.topo.bsm.IpServiceVertex;
import org.opennms.features.topology.plugins.topo.bsm.ReductionKeyVertex;
import org.opennms.features.topology.plugins.topo.bsm.simulate.SimulationAwareStateMachineFactory;
import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.BusinessServiceStateMachine;
import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.netmgt.bsm.service.model.edge.ApplicationEdge;
import org.opennms.netmgt.bsm.service.model.edge.ChildEdge;
import org.opennms.netmgt.bsm.service.model.edge.Edge;
import org.opennms.netmgt.bsm.service.model.edge.EdgeVisitor;
import org.opennms.netmgt.bsm.service.model.edge.IpServiceEdge;
import org.opennms.netmgt.bsm.service.model.edge.ReductionKeyEdge;
import org.opennms.netmgt.bsm.service.model.graph.BusinessServiceGraph;
import org.opennms.netmgt.bsm.service.model.graph.GraphVertex;
import org.opennms.netmgt.vaadin.core.TransactionAwareBeanProxyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.vaadin.v7.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.v7.ui.Label;

public class BusinessServiceVertexStatusInfoPanelItemProvider extends VertexInfoPanelItemProvider {

    private static final Logger LOG = LoggerFactory.getLogger(BusinessServiceVertexStatusInfoPanelItemProvider.class);

    /**
     * The number of edges the vertex may have to perform the impact analysis.
     * See NMS-8527 for details.
     */
    private static final int MAX_EDGES_FOR_IMPACTING = 10;

    private BusinessServiceManager businessServiceManager;
    private BusinessServicesTopologyProvider businessServicesTopologyProvider;

    private final TransactionAwareBeanProxyFactory transactionAwareBeanProxyFactory;

    public BusinessServiceVertexStatusInfoPanelItemProvider(TransactionAwareBeanProxyFactory transactionAwareBeanProxyFactory) {
        this.transactionAwareBeanProxyFactory = transactionAwareBeanProxyFactory;
    }

    public void setBusinessServiceManager(BusinessServiceManager businessServiceManager) {
        this.businessServiceManager = transactionAwareBeanProxyFactory.createProxy(businessServiceManager);
    }

    public void setBusinessServicesTopologyProvider(final BusinessServicesTopologyProvider businessServicesTopologyProvider) {
        this.businessServicesTopologyProvider = businessServicesTopologyProvider;
    }

    private Component createComponent(BusinessServiceVertex vertex, GraphContainer container) {
        final FormLayout rootLayout = new FormLayout();
        rootLayout.setSizeFull();
        rootLayout.setSpacing(false);
        rootLayout.setMargin(false);
        rootLayout.addStyleName("severity");

        final BusinessServiceStateMachine stateMachine = SimulationAwareStateMachineFactory.createStateMachine(businessServiceManager,
                                                                                                               container.getCriteria());
        final Status overallStatus = BusinessServicesStatusProvider.getStatus(stateMachine, vertex);
        rootLayout.addComponent(createStatusLabel("Overall", overallStatus));
        rootLayout.addComponent(new Label());

        final BusinessServiceGraph graph = stateMachine.getGraph();
        final BusinessService businessService = businessServiceManager.getBusinessServiceById(vertex.getServiceId());
        final Set<GraphVertex> impactingVertices = getImpactingVertices(stateMachine, graph, businessService);
        for (final Edge edge : businessService.getEdges()) {
            // Get the topology vertex for the child to determine the display label
            final Vertex childVertex = businessServicesTopologyProvider.getCurrentGraph().getVertex(edge.accept(new EdgeVisitor<VertexRef>() {
                @Override
                public VertexRef visit(final IpServiceEdge edge) {
                    return new IpServiceVertex(edge.getIpService(), 0);
                }

                @Override
                public VertexRef visit(final ReductionKeyEdge edge) {
                    return new ReductionKeyVertex(edge.getReductionKey(), 0);
                }

                @Override
                public VertexRef visit(final ChildEdge edge) {
                    return new BusinessServiceVertex(edge.getChild(), 0);
                }

                @Override
                public VertexRef visit(final ApplicationEdge edge) {
                    return new ApplicationVertex(edge.getApplication(), 0);
                }
            }));
            final Status edgeStatus = stateMachine.getOperationalStatus(edge);

            rootLayout.addComponent(createStatusLabel(childVertex.getLabel(),
                                                      edgeStatus,
                                                      String.format("%s &times; %d <i class=\"pull-right fa %s\"></i>",
                                                                    edgeStatus.getLabel(),
                                                                    edge.getWeight(),
                                                                    impactingVertices.contains(graph.getVertexByEdgeId(edge.getId()))
                                                                    ? "fa-bolt"
                                                                    : "")));
        }

        return rootLayout;
    }

    @Override
    protected boolean contributeTo(VertexRef vertexRef, GraphContainer container) {
        return vertexRef instanceof BusinessServiceVertex;
    }

    @Override
    protected InfoPanelItem createInfoPanelItem(VertexRef ref, GraphContainer graphContainer) {
        return new DefaultInfoPanelItem()
                .withTitle("Business Service Status")
                .withOrder(100)
                .withComponent(createComponent((BusinessServiceVertex) ref, graphContainer));
    }

    public static Label createStatusLabel(final String caption, final Status status) {
        return createStatusLabel(caption, status, status.getLabel());
    }

    public static Label createStatusLabel(final String caption, final Status status, final String text) {
        final Label label = createLabel(caption, text);
        label.setContentMode(ContentMode.HTML);
        label.addStyleName("severity-" + status.toString().toLowerCase());
        label.addStyleName("bright");

        return label;
    }

    private static Set<GraphVertex> getImpactingVertices(BusinessServiceStateMachine stateMachine, BusinessServiceGraph graph, BusinessService businessService) {
        // Only consider a feasible amount of edges. See NMS-8501 and NMS-8527 for more details
        if (graph.getOutEdges(graph.getVertexByBusinessServiceId(businessService.getId())).size() <= MAX_EDGES_FOR_IMPACTING) {
            return stateMachine.calculateImpacting(businessService)
                    .stream()
                    .map(edge -> graph.getDest(edge)) // DO NOT CONVERT to lamda method reference. See NMS-10529
                    .collect(Collectors.toSet());
        }
        LOG.warn("Try to calculate impacting vertices for more than {} edges. This is currently not supported. See http://http://issues.opennms.org/browse/NMS-8527.", MAX_EDGES_FOR_IMPACTING);
        return Sets.newHashSet();
    }
}


