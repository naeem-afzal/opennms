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
package org.opennms.netmgt.enlinkd.persistence.api;

import java.util.Date;
import java.util.List;

import org.opennms.netmgt.dao.api.OnmsDao;
import org.opennms.netmgt.enlinkd.model.BridgeBridgeLink;





/**
 * <p>BridgeBridgeLinkDao interface.</p>
 */
public interface BridgeBridgeLinkDao extends OnmsDao<BridgeBridgeLink, Integer> {

    List<BridgeBridgeLink> findByNodeId(Integer id);

    List<BridgeBridgeLink> findByDesignatedNodeId(Integer id);

    BridgeBridgeLink getByNodeIdBridgePort(Integer id, Integer port);

    BridgeBridgeLink getByNodeIdBridgePortIfIndex(Integer id, Integer ifindex);

    List<BridgeBridgeLink> getByDesignatedNodeIdBridgePort(Integer id, Integer port);

    List<BridgeBridgeLink> getByDesignatedNodeIdBridgePortIfIndex(Integer id, Integer ifindex);

    void deleteByNodeIdOlderThen(Integer nodeiId, Date now);

    void deleteByDesignatedNodeIdOlderThen(Integer nodeiId, Date now);

    void deleteByNodeId(Integer nodeiId);

    void deleteByDesignatedNodeId(Integer nodeiId);

    void deleteAll();

}
