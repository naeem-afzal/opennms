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
package org.opennms.netmgt.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.discovery.*;
import org.opennms.netmgt.model.discovery.IPPollAddress;

public class DiscoveryConfigFactoryTest {
    @Test
    public void testAddToSpecificsFromURLViaURL() throws Exception {
        final List<IPPollAddress> specifics = new ArrayList<>();
        final URL in = this.getClass().getResource("validDiscoveryIncludeFile.txt");
        final long timeout = 100;
        final int retries = 1;
        DiscoveryConfigFactory.addToSpecificsFromURL(specifics, in.toString(), null, null, timeout, retries);
        assertEquals(8, specifics.size());
        assertEquals("127.0.0.1", InetAddressUtils.str(specifics.get(0).getAddress()));
        assertEquals("10.1.1.1", InetAddressUtils.str(specifics.get(1).getAddress()));
        assertEquals("10.2.1.1", InetAddressUtils.str(specifics.get(2).getAddress()));
        assertEquals("8.8.8.8", InetAddressUtils.str(specifics.get(3).getAddress()));
        assertEquals("fe80:0000:0000:0000:ffff:eeee:dddd:cccc", InetAddressUtils.str(specifics.get(4).getAddress()));
        assertEquals("0000:0000:0000:0000:0000:0000:0000:0001", InetAddressUtils.str(specifics.get(5).getAddress()));
        assertEquals("fe80:0000:0000:0000:ffff:eeee:dddd:cccd", InetAddressUtils.str(specifics.get(6).getAddress()));
        assertEquals("fe80:0000:0000:0000:ffff:eeee:dddd:cccc", InetAddressUtils.str(specifics.get(7).getAddress()));
    }
    
    @Test
    public void testAddToSpecificsFromURLViaStream() throws Exception {
        final List<IPPollAddress> specifics = new ArrayList<>();
        final InputStream in = this.getClass().getResourceAsStream("validDiscoveryIncludeFile.txt");
        final long timeout = 100;
        final int retries = 1;
        DiscoveryConfigFactory.addToSpecificsFromURL(specifics, in, null, null, timeout, retries, null);
        assertEquals(8, specifics.size());
        assertEquals("127.0.0.1", InetAddressUtils.str(specifics.get(0).getAddress()));
        assertEquals("10.1.1.1", InetAddressUtils.str(specifics.get(1).getAddress()));
        assertEquals("10.2.1.1", InetAddressUtils.str(specifics.get(2).getAddress()));
        assertEquals("8.8.8.8", InetAddressUtils.str(specifics.get(3).getAddress()));
        assertEquals("fe80:0000:0000:0000:ffff:eeee:dddd:cccc", InetAddressUtils.str(specifics.get(4).getAddress()));
        assertEquals("0000:0000:0000:0000:0000:0000:0000:0001", InetAddressUtils.str(specifics.get(5).getAddress()));
        assertEquals("fe80:0000:0000:0000:ffff:eeee:dddd:cccd", InetAddressUtils.str(specifics.get(6).getAddress()));
        assertEquals("fe80:0000:0000:0000:ffff:eeee:dddd:cccc", InetAddressUtils.str(specifics.get(7).getAddress()));
    }
    
    @Test
    public void testMultipleExcludes() throws Exception {

        final DiscoveryConfiguration conf = new DiscoveryConfiguration();

        IncludeRange ir = new IncludeRange();
        ir.setBegin("192.168.0.1");
        ir.setEnd("192.168.0.254");
        conf.addIncludeRange(ir);

        ir = new IncludeRange();
        ir.setBegin("192.168.2.1");
        ir.setEnd("192.168.2.255");
        conf.addIncludeRange(ir);

        Specific s = new Specific();
        s.setAddress("192.168.1.1");
        conf.addSpecific(s);

        s = new Specific();
        s.setAddress("192.168.4.1");
        conf.addSpecific(s);

        ExcludeRange er = new ExcludeRange();
        er.setBegin("192.168.0.100");
        er.setEnd("192.168.0.150");
        conf.addExcludeRange(er);

        er = new ExcludeRange();
        er.setBegin("192.168.2.200");
        er.setEnd("192.168.4.254");
        conf.addExcludeRange(er);

        final DiscoveryConfigFactory factory = new DiscoveryConfigFactory(conf);
        factory.initializeExcludeRanges();

        assertFalse(factory.isExcluded(InetAddressUtils.addr("192.168.0.1"), null));
        assertFalse(factory.isExcluded(InetAddressUtils.addr("192.168.0.2"), null));
        assertFalse(factory.isExcluded(InetAddressUtils.addr("192.168.0.99"), null));
        assertTrue(factory.isExcluded(InetAddressUtils.addr("192.168.0.100"), null));
        assertTrue(factory.isExcluded(InetAddressUtils.addr("192.168.0.140"), null));
        assertTrue(factory.isExcluded(InetAddressUtils.addr("192.168.0.150"), null));
        assertFalse(factory.isExcluded(InetAddressUtils.addr("192.168.0.151"), null));
        assertFalse(factory.isExcluded(InetAddressUtils.addr("192.168.1.1"), null));
        assertFalse(factory.isExcluded(InetAddressUtils.addr("192.168.2.1"), null));
        assertFalse(factory.isExcluded(InetAddressUtils.addr("192.168.2.100"), null));
        assertTrue(factory.isExcluded(InetAddressUtils.addr("192.168.2.200"), null));
        assertTrue(factory.isExcluded(InetAddressUtils.addr("192.168.2.220"), null));
        assertTrue(factory.isExcluded(InetAddressUtils.addr("192.168.2.255"), null));
        assertTrue(factory.isExcluded(InetAddressUtils.addr("192.168.4.1"), null));

    }

    @Test
    public void testMultipleExcludeFromUrl() throws Exception {
        final DiscoveryConfiguration conf = new DiscoveryConfiguration();
        ExcludeUrl excludeUrl = new ExcludeUrl();
        URL url = this.getClass().getResource("validDiscoveryExcludeFile.txt");
        excludeUrl.setUrl(url.toString());

        conf.addExcludeUrl(excludeUrl);

        excludeUrl = new ExcludeUrl();
        url = this.getClass().getResource("validDiscoveryExcludeFile1.txt");
        excludeUrl.setUrl(url.toString());
        excludeUrl.setLocation("Anywhere");
        conf.addExcludeUrl(excludeUrl);

        final DiscoveryConfigFactory factory = new DiscoveryConfigFactory(conf);
        factory.initializeExcludeUrls();

        assertTrue(factory.isExcluded(InetAddressUtils.addr("127.0.0.1"), null));
        assertTrue(factory.isExcluded(InetAddressUtils.addr("127.0.0.2"), "Anywhere"));
        assertFalse(factory.isExcluded(InetAddressUtils.addr("127.0.0.3"), null));
        assertFalse(factory.isExcluded(InetAddressUtils.addr("127.0.0.3"), "Anywhere"));
        assertTrue(factory.isExcluded(InetAddressUtils.addr("8.8.8.2"), "Anywhere"));
        assertFalse(factory.isExcluded(InetAddressUtils.addr("8.8.8.2"), null));
        assertTrue(factory.isExcluded(InetAddressUtils.addr("fe80:0000:0000:0000:ffff:eeee:dddd:dddd"), "Anywhere"));
        assertTrue(factory.isExcluded(InetAddressUtils.addr("fe80:0000:0000:0000:ffff:eeee:dddd:dddd"), "Anywhere"));
    }

    @Test
    public void testExcludeFromDefinition() {
        final DiscoveryConfiguration conf = new DiscoveryConfiguration();

        // Add definition with exclude range.
        Definition definition1 = new Definition();
        definition1.setLocation("MINION");
        IncludeRange ir = new IncludeRange();
        ir.setBegin("192.168.0.1");
        ir.setEnd("192.168.0.254");
        ExcludeRange er = new ExcludeRange();
        er.setBegin("192.168.0.100");
        er.setEnd("192.168.0.150");
        definition1.addExcludeRange(er);
        definition1.addIncludeRange(ir);

        Specific s = new Specific();
        s.setAddress("192.168.1.1");
        definition1.addSpecific(s);
        conf.addDefinition(definition1);

        //Add include ranges and exclude ranges with default location
        Definition definition2 = new Definition();
        ir = new IncludeRange();
        ir.setBegin("192.168.2.1");
        ir.setEnd("192.168.2.255");
        definition2.addIncludeRange(ir);

        s = new Specific();
        s.setAddress("192.168.4.1");
        definition2.addSpecific(s);

        er = new ExcludeRange();
        er.setBegin("192.168.2.200");
        er.setEnd("192.168.4.254");
        definition2.addExcludeRange(er);
        conf.addDefinition(definition2);

        final DiscoveryConfigFactory factory = new DiscoveryConfigFactory(conf);
        factory.initializeExcludeRanges();

        assertFalse(factory.isExcluded(InetAddressUtils.addr("192.168.0.1"), "MINION"));
        assertFalse(factory.isExcluded(InetAddressUtils.addr("192.168.0.2"), "MINION"));
        assertFalse(factory.isExcluded(InetAddressUtils.addr("192.168.0.99"), "MINION"));
        assertTrue(factory.isExcluded(InetAddressUtils.addr("192.168.0.100"), "MINION"));
        assertTrue(factory.isExcluded(InetAddressUtils.addr("192.168.0.140"), "MINION"));
        assertTrue(factory.isExcluded(InetAddressUtils.addr("192.168.0.150"), "MINION"));
        // Verify that default location won't exclude
        assertFalse(factory.isExcluded(InetAddressUtils.addr("192.168.0.140"), "Default"));
        assertFalse(factory.isExcluded(InetAddressUtils.addr("192.168.0.150"), "Default"));

        assertFalse(factory.isExcluded(InetAddressUtils.addr("192.168.0.151"), "MINION"));
        assertFalse(factory.isExcluded(InetAddressUtils.addr("192.168.1.1"), "MINION"));
        assertFalse(factory.isExcluded(InetAddressUtils.addr("192.168.2.1"), null));
        assertFalse(factory.isExcluded(InetAddressUtils.addr("192.168.2.100"), null));
        assertTrue(factory.isExcluded(InetAddressUtils.addr("192.168.2.200"), null));
        assertTrue(factory.isExcluded(InetAddressUtils.addr("192.168.2.220"), null));
        assertTrue(factory.isExcluded(InetAddressUtils.addr("192.168.2.255"), null));
        assertTrue(factory.isExcluded(InetAddressUtils.addr("192.168.4.1"), null));
        // Verify that address at different location won't be excluded.
        assertFalse(factory.isExcluded(InetAddressUtils.addr("192.168.4.1"), "MINION"));


    }

    @Test
    public void testSingleIPExclude() throws Exception {
        final DiscoveryConfiguration conf = new DiscoveryConfiguration();

        IncludeRange ir = new IncludeRange();
        ir.setBegin("192.168.0.1");
        ir.setEnd("192.168.0.254");
        conf.addIncludeRange(ir);

        ExcludeRange er = new ExcludeRange();
        er.setBegin("192.168.0.100");
        er.setEnd("192.168.0.100");
        conf.addExcludeRange(er);

        DiscoveryConfigFactory factory = new DiscoveryConfigFactory(conf);
        factory.initializeExcludeRanges();

        assertFalse(factory.isExcluded(InetAddressUtils.addr("192.168.0.1"), null));
        assertFalse(factory.isExcluded(InetAddressUtils.addr("192.168.0.2"), null));
        assertFalse(factory.isExcluded(InetAddressUtils.addr("192.168.0.99"), null));
        assertTrue(factory.isExcluded(InetAddressUtils.addr("192.168.0.100"), null));
        assertFalse(factory.isExcluded(InetAddressUtils.addr("192.168.0.101"), null));
        assertFalse(factory.isExcluded(InetAddressUtils.addr("192.168.0.151"), null));
    }
}
