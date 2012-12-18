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

package org.opennms.features.topology.plugins.topo.linkd.internal;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.SimpleEdgeProvider;
import org.opennms.features.topology.api.topo.SimpleVertexProvider;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.netmgt.dao.DataLinkInterfaceDao;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.SnmpInterfaceDao;
import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.slf4j.LoggerFactory;

public class LinkdTopologyProvider implements GraphProvider {
    private static final String LINKD_GROUP_ID_PREFIX = "linkdg";
    public static final String GROUP_ICON_KEY = "linkd:group";
    public static final String SERVER_ICON_KEY = "linkd:system";
    
    private static final String HTML_TOOLTIP_TAG_OPEN = "<p>";
    private static final String HTML_TOOLTIP_TAG_END  = "</p>";
    /**
     * Always print at least one digit after the decimal point,
     * and at most three digits after the decimal point.
     */
    private static final DecimalFormat s_oneDigitAfterDecimal = new DecimalFormat("0.0##");
    
    /**
     * Print no digits after the decimal point (heh, nor a decimal point).
     */
    private static final DecimalFormat s_noDigitsAfterDecimal = new DecimalFormat("0");

    /**
     * Do not use directly. Call {@link #getNodeStatusMap 
     * getInterfaceStatusMap} instead.
     */
    private static final Map<Character, String> m_nodeStatusMap;

    static {
        m_nodeStatusMap = new HashMap<Character, String>();
        m_nodeStatusMap.put('A', "Active");
        m_nodeStatusMap.put(' ', "Unknown");
        m_nodeStatusMap.put('D', "Deleted");                        
    }
    
     static final String[] OPER_ADMIN_STATUS = new String[] {
        "&nbsp;",          //0 (not supported)
        "Up",              //1
        "Down",            //2
        "Testing",         //3
        "Unknown",         //4
        "Dormant",         //5
        "NotPresent",      //6
        "LowerLayerDown"   //7
      };

    private boolean addNodeWithoutLink = false;
    
    private DataLinkInterfaceDao m_dataLinkInterfaceDao;
    
    private NodeDao m_nodeDao;
    
    private SnmpInterfaceDao m_snmpInterfaceDao;

    private IpInterfaceDao m_ipInterfaceDao;

    private String m_configurationFile;

//    private TransactionOperations m_transactionTemplate;
    
    public String getConfigurationFile() {
        return m_configurationFile;
    }

    public void setConfigurationFile(String configurationFile) {
        m_configurationFile = configurationFile;
    }

    public SnmpInterfaceDao getSnmpInterfaceDao() {
        return m_snmpInterfaceDao;
    }

    public void setSnmpInterfaceDao(SnmpInterfaceDao snmpInterfaceDao) {
        m_snmpInterfaceDao = snmpInterfaceDao;
    }

    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }
    
    public boolean isAddNodeWithoutLink() {
        return addNodeWithoutLink;
    }

    public void setAddNodeWithoutLink(boolean addNodeWithoutLink) {
        this.addNodeWithoutLink = addNodeWithoutLink;
    }

    private final SimpleVertexProvider m_vertexContainer;
    private final SimpleEdgeProvider m_edgeContainer;

    private int m_groupCounter = 0;
    
    public DataLinkInterfaceDao getDataLinkInterfaceDao() {
        return m_dataLinkInterfaceDao;
    }

    public void setDataLinkInterfaceDao(DataLinkInterfaceDao dataLinkInterfaceDao) {
        m_dataLinkInterfaceDao = dataLinkInterfaceDao;
    }

    @Override
    public void onInit() {
        log("init: loading topology v1.3");
        loadtopology();
    }
    
    public LinkdTopologyProvider() {
        m_vertexContainer = new SimpleVertexProvider("linkd");
        m_edgeContainer = new SimpleEdgeProvider("linkd");
    }

    @Override
    public Vertex addGroup(String groupName, String groupIconKey) {
        String nextGroupId = getNextGroupId();
        return addGroup(nextGroupId, groupIconKey, groupName);
    }

    private Vertex addGroup(String groupId, String iconKey, String label) {
        if (m_vertexContainer.containsId(groupId)) {
            throw new IllegalArgumentException("A vertex or group with id " + groupId + " already exists!");
        }
        log("Adding a group: " + groupId);
        LinkdVertex vertex = new LinkdGroup(groupId, label);
        vertex.setIconKey(iconKey);
        m_vertexContainer.add(vertex);
        return vertex;
    }
    
    private String getNextGroupId() {
        return LINKD_GROUP_ID_PREFIX + m_groupCounter++;
    }

    @Override
    public void load(String filename) {
        if (filename == null) {
            loadtopology();
        } else {
            loadfromfile(filename);
        }
    }

    @XmlRootElement(name="graph")
    @XmlAccessorType(XmlAccessType.FIELD)
    private static class SimpleGraph {
        
        @XmlElements({
                @XmlElement(name="vertex", type=Vertex.class),
                @XmlElement(name="group", type=Vertex.class)
        })
        List<Vertex> m_vertices = new ArrayList<Vertex>();
        
        @XmlElement(name="edge")
        List<Edge> m_edges = new ArrayList<Edge>();
        
        public SimpleGraph(List<Vertex> vertices, List<Edge> edges) {
            m_vertices = vertices;
            m_edges = edges;
        }

    }

    private SimpleGraph getGraphFromFile(File file) {
        return JAXB.unmarshal(file, SimpleGraph.class);
    }

    private void loadfromfile(String filename) {
        File file = new File(filename);
        if (file.exists() && file.canRead()) {
            SimpleGraph graph = getGraphFromFile(file);        
            m_vertexContainer.add(graph.m_vertices);        
            m_edgeContainer.add(graph.m_edges);
        }
    }

    //@Transactional
    private void loadtopology() {
        log("loadtopology: loading topology: configFile:" + m_configurationFile);
        
        log("loadtopology: Clear " + m_vertexContainer.getClass().getSimpleName());
        m_vertexContainer.clear();
        log("loadtopology: Clear " + m_edgeContainer.getClass().getSimpleName());
        m_edgeContainer.clear();

        Map<String, Vertex> vertexes = new HashMap<String,Vertex>();
        List<Edge> edges = new ArrayList<Edge>();
        for (DataLinkInterface link: m_dataLinkInterfaceDao.findAll()) {
            log("loadtopology: parsing link: " + link.getDataLinkInterfaceId());

            OnmsNode node = m_nodeDao.get(link.getNode().getId());
            //OnmsNode node = link.getNode();
            log("loadtopology: found node: " + node.getLabel());
            String sourceId = node.getNodeId();
            Vertex source;
            if ( vertexes.containsKey(sourceId)) {
                source = vertexes.get(sourceId);
            } else {
                log("loadtopology: adding source as vertex: " + node.getLabel());
                source = getVertex(node);
                vertexes.put(sourceId, source);
            }

            OnmsNode parentNode = m_nodeDao.get(link.getNodeParentId());
            log("loadtopology: found parentnode: " + parentNode.getLabel());
                       String targetId = parentNode.getNodeId();
            Vertex target;
            if (vertexes.containsKey(targetId)) {
                target = vertexes.get(targetId);
            } else {
                log("loadtopology: adding target as vertex: " + parentNode.getLabel());
                target = getVertex(parentNode);
                vertexes.put(targetId, target);
            }
            Edge edge = new LinkdEdge(link.getDataLinkInterfaceId(),source,target); 
            edge.setTooltipText(getEdgeTooltipText(link,source,target));
            edges.add(edge);
        }
        
        log("loadtopology: isAddNodeWithoutLink: " + isAddNodeWithoutLink());
        if (isAddNodeWithoutLink()) {
            for (OnmsNode onmsnode: m_nodeDao.findAll()) {
                log("loadtopology: parsing link less node: " + onmsnode.getLabel());
                String nodeId = onmsnode.getNodeId();
                if (!vertexes.containsKey(nodeId)) {
                    log("loadtopology: adding link less node: " + onmsnode.getLabel());
                    vertexes.put(nodeId,getVertex(onmsnode));
                }                
            }
        }
        
        log("Found Vertexes: #" + vertexes.size());        
        log("Found Edges: #" + edges.size());

                
        m_vertexContainer.add(vertexes.values());
        m_edgeContainer.add(edges);
 
        File configFile = new File(m_configurationFile);

        if (configFile.exists() && configFile.canRead()) {
            log("loadtopology: loading topology from configuration file: " + m_configurationFile);
            m_groupCounter = 0;
            SimpleGraph graph = getGraphFromFile(configFile);
            for (Vertex vertex: graph.m_vertices) {
                if (!vertex.isLeaf()) {
                    log("loadtopology: adding group to topology: " + vertex.getId());
                    // Find the highest index group number and start the index for new groups above it
                    int groupNumber = Integer.parseInt(vertex.getId().substring(LINKD_GROUP_ID_PREFIX.length()));
                    if (m_groupCounter <= groupNumber) {
                        m_groupCounter = groupNumber + 1;
                    }
                    addGroup(vertex.getId(), vertex.getIconKey(), vertex.getLabel());
                }
            }
            
            for (Vertex vertex: graph.m_vertices) {
                log("loadtopology: found vertex: " + vertex.getId());
                if (vertex.isRoot()) {
                    if (!vertex.isLeaf())
                        setParent(vertex, null);
                } else {
                    setParent(vertex, vertex.getParent());
                }
            }

        }
        log("Found Groups: #" + m_groupCounter);


    }

    private LinkdVertex getVertex(OnmsNode onmsnode) {
        OnmsIpInterface ip = getAddress(onmsnode);
        LinkdVertex vertex = new LinkdVertex(onmsnode.getNodeId(), 0, 0, getIconName(onmsnode), onmsnode.getLabel(), ( ip == null ? null : ip.getIpAddress().getHostAddress()));
        vertex.setNodeID(Integer.parseInt(onmsnode.getNodeId()));
        vertex.setTooltipText(getNodeTooltipText(onmsnode, vertex, ip));
        return vertex;
    }

    private OnmsIpInterface getAddress(OnmsNode node) {
        //OnmsIpInterface ip = node.getPrimaryInterface();
        OnmsIpInterface ip = m_ipInterfaceDao.findPrimaryInterfaceByNodeId(node.getId());
        if ( ip == null) {
//            for (OnmsIpInterface iterip: node.getIpInterfaces()) {
            for (OnmsIpInterface iterip: m_ipInterfaceDao.findByNodeId(node.getId())) {
                ip = iterip;
                break;
            }
        }
        return ip;
    }
    

    private String getEdgeTooltipText(DataLinkInterface link,
            Vertex source, Vertex target) {
        StringBuffer tooltipText = new StringBuffer();

        OnmsSnmpInterface sourceInterface = m_snmpInterfaceDao.findByNodeIdAndIfIndex(Integer.parseInt(source.getId()), link.getIfIndex());
        OnmsSnmpInterface targetInterface = m_snmpInterfaceDao.findByNodeIdAndIfIndex(Integer.parseInt(target.getId()), link.getParentIfIndex());
        
        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
        if (sourceInterface != null && targetInterface != null
         && sourceInterface.getNetMask() != null && !sourceInterface.getNetMask().isLoopbackAddress() 
         && targetInterface.getNetMask() != null && !targetInterface.getNetMask().isLoopbackAddress()) {
            tooltipText.append("Type of Link: Layer3/Layer2");
        } else {
            tooltipText.append("Type of Link: Layer2");
        }
        tooltipText.append(HTML_TOOLTIP_TAG_END);

        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
        tooltipText.append( "Name: &lt;endpoint1 " + source.getLabel());
        if (sourceInterface != null ) 
            tooltipText.append( ":"+sourceInterface.getIfName());
        tooltipText.append( " ---- endpoint2 " + target.getLabel());
        if (targetInterface != null) 
            tooltipText.append( ":"+targetInterface.getIfName());
        tooltipText.append("&gt;");
        tooltipText.append(HTML_TOOLTIP_TAG_END);
        
        if ( targetInterface != null) {
            if (targetInterface.getIfSpeed() != null) {
                tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
                tooltipText.append( "Bandwidth: " + getHumanReadableIfSpeed(targetInterface.getIfSpeed()));
                tooltipText.append(HTML_TOOLTIP_TAG_END);
            }
            if (targetInterface.getIfOperStatus() != null) {
                tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
                tooltipText.append( "Link status: " + getIfStatusString(targetInterface.getIfOperStatus()));
                tooltipText.append(HTML_TOOLTIP_TAG_END);
            }
        } else if (sourceInterface != null) {
            if (sourceInterface.getIfSpeed() != null) {
                tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
                tooltipText.append( "Bandwidth: " + getHumanReadableIfSpeed(sourceInterface.getIfSpeed()));
                tooltipText.append(HTML_TOOLTIP_TAG_END);
            }
            if (sourceInterface.getIfOperStatus() != null) {
                tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
                tooltipText.append( "Link status: " + getIfStatusString(sourceInterface.getIfOperStatus()));
                tooltipText.append(HTML_TOOLTIP_TAG_END);
            }
        }

        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
        tooltipText.append( "End Point 1: " + source.getLabel() + ", " + source.getIpAddress());
        tooltipText.append(HTML_TOOLTIP_TAG_END);
        
        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
        tooltipText.append( "End Point 2: " + target.getLabel() + ", " + target.getIpAddress());
        tooltipText.append(HTML_TOOLTIP_TAG_END);

        log("getEdgeTooltipText\n" + tooltipText);
        return tooltipText.toString();
    }

    private String getNodeTooltipText(OnmsNode node, LinkdVertex vertex, OnmsIpInterface ip) {
        StringBuffer tooltipText = new StringBuffer();

        /*
        if (node.getSysDescription() != null && node.getSysDescription().length() >0) {
            tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
            tooltipText.append("Description: " + node.getSysDescription());
            tooltipText.append(HTML_TOOLTIP_TAG_END);
        }
        */

        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
        tooltipText.append( "Management IP and Name: " + vertex.getIpAddress() + " (" + vertex.getLabel() + ")");
        tooltipText.append(HTML_TOOLTIP_TAG_END);
        
        if (node.getSysLocation() != null && node.getSysLocation().length() >0) {
            tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
            tooltipText.append("Location: " + node.getSysLocation());
            tooltipText.append(HTML_TOOLTIP_TAG_END);
        }
        
        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
        tooltipText.append( "Status: " +getNodeStatusString(node.getType().charAt(0)));
        if (ip != null && ip.isManaged()) {
            tooltipText.append( " / Managed");
        } else {
            tooltipText.append( " / Unmanaged");
        }
        tooltipText.append(HTML_TOOLTIP_TAG_END);

        log("getNodeTooltipText:\n" + tooltipText);
        
        return tooltipText.toString();

    }
    
    protected String getIconName(OnmsNode node) {
        return node.getSysObjectId() == null ? "linkd:system" : "linkd:system:snmp:"+node.getSysObjectId();
    }
    
    @Override
    public void save(String filename) {
        if (filename == null) 
            filename=m_configurationFile;
        List<Vertex> vertices = m_vertexContainer.getVertices();
        List<Edge> edges = m_edgeContainer.getEdges();

        SimpleGraph graph = new SimpleGraph(vertices, edges);
        
        JAXB.marshal(graph, new File(filename));
    }

    @Override
    public boolean setParent(VertexRef vertexId, VertexRef parentId) {
        boolean addedparent = m_vertexContainer.setParent(vertexId, parentId);
        log("setParent() for vertex: " + vertexId + ", parent: " + parentId + ", result: " + (addedparent ? "SUCCESS" : "FAILED"));
        return addedparent;
    }
    
      private static String getIfStatusString(int ifStatusNum) {
          if (ifStatusNum < OPER_ADMIN_STATUS.length) {
              return OPER_ADMIN_STATUS[ifStatusNum];
          } else {
              return "Unknown (" + ifStatusNum + ")";
          }
      }
      
    /**
     * Return the human-readable name for a interface status character, may be
     * null.
     *
     * @param c a char.
     * @return a {@link java.lang.String} object.
     */
    private String getNodeStatusString(char c) {
        return m_nodeStatusMap.get(c);
    }
    
    /**
     * Method used to convert an integer bits-per-second value to a more
     * readable vale using commonly recognized abbreviation for network
     * interface speeds. Feel free to expand it as necessary to accomodate
     * different values.
     *
     * @param ifSpeed
     *            The bits-per-second value to be converted into a string
     *            description
     * @return A string representation of the speed (&quot;100 Mbps&quot; for
     *         example)
     */
    private static String getHumanReadableIfSpeed(long ifSpeed) {
        DecimalFormat formatter;
        double displaySpeed;
        String units;
        
        if (ifSpeed >= 1000000000L) {
            if ((ifSpeed % 1000000000L) == 0) {
                formatter = s_noDigitsAfterDecimal;
            } else {
                formatter = s_oneDigitAfterDecimal;
            }
            displaySpeed = ((double) ifSpeed) / 1000000000;
            units = "Gbps";
        } else if (ifSpeed >= 1000000L) {
            if ((ifSpeed % 1000000L) == 0) {
                formatter = s_noDigitsAfterDecimal;
            } else {
                formatter = s_oneDigitAfterDecimal;
            }
            displaySpeed = ((double) ifSpeed) / 1000000;
            units = "Mbps";
        } else if (ifSpeed >= 1000L) {
            if ((ifSpeed % 1000L) == 0) {
                formatter = s_noDigitsAfterDecimal;
            } else {
                formatter = s_oneDigitAfterDecimal;
            }
            displaySpeed = ((double) ifSpeed) / 1000;
            units = "kbps";
        } else {
            formatter = s_noDigitsAfterDecimal;
            displaySpeed = (double) ifSpeed;
            units = "bps";
        }
        
        return formatter.format(displaySpeed) + " " + units;
    }

    private void log(final String string) {
        LoggerFactory.getLogger(getClass()).debug(string);
    }
/*
    public TransactionOperations getTransactionTemplate() {
        return m_transactionTemplate;
    }

    public void setTransactionTemplate(TransactionOperations transactionTemplate) {
        m_transactionTemplate = transactionTemplate;
    }
*/

    public IpInterfaceDao getIpInterfaceDao() {
        return m_ipInterfaceDao;
    }

    public void setIpInterfaceDao(IpInterfaceDao ipInterfaceDao) {
        m_ipInterfaceDao = ipInterfaceDao;
    }

	@Override
	public String getNamespace() {
		return "nodes";
	}
}
