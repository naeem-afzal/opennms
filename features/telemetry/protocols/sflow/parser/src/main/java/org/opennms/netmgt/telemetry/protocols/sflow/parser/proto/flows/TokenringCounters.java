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
package org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows;

import org.bson.BsonWriter;
import org.opennms.netmgt.telemetry.listeners.utils.BufferUtils;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.SampleDatagramEnrichment;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.InvalidPacketException;

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;

// struct tokenring_counters {
//   unsigned int dot5StatsLineErrors;
//   unsigned int dot5StatsBurstErrors;
//   unsigned int dot5StatsACErrors;
//   unsigned int dot5StatsAbortTransErrors;
//   unsigned int dot5StatsInternalErrors;
//   unsigned int dot5StatsLostFrameErrors;
//   unsigned int dot5StatsReceiveCongestions;
//   unsigned int dot5StatsFrameCopiedErrors;
//   unsigned int dot5StatsTokenErrors;
//   unsigned int dot5StatsSoftErrors;
//   unsigned int dot5StatsHardErrors;
//   unsigned int dot5StatsSignalLoss;
//   unsigned int dot5StatsTransmitBeacons;
//   unsigned int dot5StatsRecoverys;
//   unsigned int dot5StatsLobeWires;
//   unsigned int dot5StatsRemoves;
//   unsigned int dot5StatsSingles;
//   unsigned int dot5StatsFreqErrors;
// };

public class TokenringCounters implements CounterData {
    public final long dot5StatsLineErrors;
    public final long dot5StatsBurstErrors;
    public final long dot5StatsACErrors;
    public final long dot5StatsAbortTransErrors;
    public final long dot5StatsInternalErrors;
    public final long dot5StatsLostFrameErrors;
    public final long dot5StatsReceiveCongestions;
    public final long dot5StatsFrameCopiedErrors;
    public final long dot5StatsTokenErrors;
    public final long dot5StatsSoftErrors;
    public final long dot5StatsHardErrors;
    public final long dot5StatsSignalLoss;
    public final long dot5StatsTransmitBeacons;
    public final long dot5StatsRecoverys;
    public final long dot5StatsLobeWires;
    public final long dot5StatsRemoves;
    public final long dot5StatsSingles;
    public final long dot5StatsFreqErrors;

    public TokenringCounters(final ByteBuf buffer) throws InvalidPacketException {
        this.dot5StatsLineErrors = BufferUtils.uint32(buffer);
        this.dot5StatsBurstErrors = BufferUtils.uint32(buffer);
        this.dot5StatsACErrors = BufferUtils.uint32(buffer);
        this.dot5StatsAbortTransErrors = BufferUtils.uint32(buffer);
        this.dot5StatsInternalErrors = BufferUtils.uint32(buffer);
        this.dot5StatsLostFrameErrors = BufferUtils.uint32(buffer);
        this.dot5StatsReceiveCongestions = BufferUtils.uint32(buffer);
        this.dot5StatsFrameCopiedErrors = BufferUtils.uint32(buffer);
        this.dot5StatsTokenErrors = BufferUtils.uint32(buffer);
        this.dot5StatsSoftErrors = BufferUtils.uint32(buffer);
        this.dot5StatsHardErrors = BufferUtils.uint32(buffer);
        this.dot5StatsSignalLoss = BufferUtils.uint32(buffer);
        this.dot5StatsTransmitBeacons = BufferUtils.uint32(buffer);
        this.dot5StatsRecoverys = BufferUtils.uint32(buffer);
        this.dot5StatsLobeWires = BufferUtils.uint32(buffer);
        this.dot5StatsRemoves = BufferUtils.uint32(buffer);
        this.dot5StatsSingles = BufferUtils.uint32(buffer);
        this.dot5StatsFreqErrors = BufferUtils.uint32(buffer);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("dot5StatsLineErrors", this.dot5StatsLineErrors)
                .add("dot5StatsBurstErrors", this.dot5StatsBurstErrors)
                .add("dot5StatsACErrors", this.dot5StatsACErrors)
                .add("dot5StatsAbortTransErrors", this.dot5StatsAbortTransErrors)
                .add("dot5StatsInternalErrors", this.dot5StatsInternalErrors)
                .add("dot5StatsLostFrameErrors", this.dot5StatsLostFrameErrors)
                .add("dot5StatsReceiveCongestions", this.dot5StatsReceiveCongestions)
                .add("dot5StatsFrameCopiedErrors", this.dot5StatsFrameCopiedErrors)
                .add("dot5StatsTokenErrors", this.dot5StatsTokenErrors)
                .add("dot5StatsSoftErrors", this.dot5StatsSoftErrors)
                .add("dot5StatsHardErrors", this.dot5StatsHardErrors)
                .add("dot5StatsSignalLoss", this.dot5StatsSignalLoss)
                .add("dot5StatsTransmitBeacons", this.dot5StatsTransmitBeacons)
                .add("dot5StatsRecoverys", this.dot5StatsRecoverys)
                .add("dot5StatsLobeWires", this.dot5StatsLobeWires)
                .add("dot5StatsRemoves", this.dot5StatsRemoves)
                .add("dot5StatsSingles", this.dot5StatsSingles)
                .add("dot5StatsFreqErrors", this.dot5StatsFreqErrors)
                .toString();
    }

    public void writeBson(final BsonWriter bsonWriter, final SampleDatagramEnrichment enr) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeInt64("dot5StatsLineErrors", this.dot5StatsLineErrors);
        bsonWriter.writeInt64("dot5StatsBurstErrors", this.dot5StatsBurstErrors);
        bsonWriter.writeInt64("dot5StatsACErrors", this.dot5StatsACErrors);
        bsonWriter.writeInt64("dot5StatsAbortTransErrors", this.dot5StatsAbortTransErrors);
        bsonWriter.writeInt64("dot5StatsInternalErrors", this.dot5StatsInternalErrors);
        bsonWriter.writeInt64("dot5StatsLostFrameErrors", this.dot5StatsLostFrameErrors);
        bsonWriter.writeInt64("dot5StatsReceiveCongestions", this.dot5StatsReceiveCongestions);
        bsonWriter.writeInt64("dot5StatsFrameCopiedErrors", this.dot5StatsFrameCopiedErrors);
        bsonWriter.writeInt64("dot5StatsTokenErrors", this.dot5StatsTokenErrors);
        bsonWriter.writeInt64("dot5StatsSoftErrors", this.dot5StatsSoftErrors);
        bsonWriter.writeInt64("dot5StatsHardErrors", this.dot5StatsHardErrors);
        bsonWriter.writeInt64("dot5StatsSignalLoss", this.dot5StatsSignalLoss);
        bsonWriter.writeInt64("dot5StatsTransmitBeacons", this.dot5StatsTransmitBeacons);
        bsonWriter.writeInt64("dot5StatsRecoverys", this.dot5StatsRecoverys);
        bsonWriter.writeInt64("dot5StatsLobeWires", this.dot5StatsLobeWires);
        bsonWriter.writeInt64("dot5StatsRemoves", this.dot5StatsRemoves);
        bsonWriter.writeInt64("dot5StatsSingles", this.dot5StatsSingles);
        bsonWriter.writeInt64("dot5StatsFreqErrors", this.dot5StatsFreqErrors);
        bsonWriter.writeEndDocument();
    }
}
