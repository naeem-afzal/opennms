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
package org.opennms.netmgt.snmp.joesnmp;

import java.math.BigInteger;
import java.net.InetAddress;

import org.opennms.netmgt.snmp.AbstractSnmpValue;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.protocols.snmp.SnmpCounter32;
import org.opennms.protocols.snmp.SnmpCounter64;
import org.opennms.protocols.snmp.SnmpEndOfMibView;
import org.opennms.protocols.snmp.SnmpIPAddress;
import org.opennms.protocols.snmp.SnmpInt32;
import org.opennms.protocols.snmp.SnmpNoSuchInstance;
import org.opennms.protocols.snmp.SnmpNoSuchObject;
import org.opennms.protocols.snmp.SnmpNull;
import org.opennms.protocols.snmp.SnmpObjectId;
import org.opennms.protocols.snmp.SnmpOctetString;
import org.opennms.protocols.snmp.SnmpOpaque;
import org.opennms.protocols.snmp.SnmpSMI;
import org.opennms.protocols.snmp.SnmpSyntax;
import org.opennms.protocols.snmp.SnmpTimeTicks;
import org.opennms.protocols.snmp.SnmpUInt32;

class JoeSnmpValue extends AbstractSnmpValue {
    SnmpSyntax m_value;
    
    JoeSnmpValue(final SnmpSyntax value) {
        m_value = value;
    }
    
    JoeSnmpValue(final int typeId, final byte[] initialBytes) {
        final byte[] bytes = initialBytes == null? null : initialBytes.clone();
        switch(typeId) {
        case SnmpSMI.SMI_COUNTER64: {
            m_value = new SnmpCounter64(new BigInteger(bytes));
            break;
        }
        case SnmpSMI.SMI_INTEGER: {
            m_value = new SnmpInt32(new BigInteger(bytes).intValue());
            break;
        }
        case SnmpSMI.SMI_COUNTER32: {
            m_value = new SnmpCounter32(new BigInteger(bytes).longValue());
            break;
        }
        case SnmpSMI.SMI_TIMETICKS: {
            m_value = new SnmpTimeTicks(new BigInteger(bytes).longValue());
            break;
        }
        case SnmpSMI.SMI_UNSIGNED32: {
            m_value = new SnmpUInt32(new BigInteger(bytes).longValue());
            break;
        }
        case SnmpSMI.SMI_IPADDRESS: {
            m_value = new SnmpIPAddress(bytes);
            break;
        }
        case SnmpSMI.SMI_OBJECTID: {
            m_value = new SnmpObjectId(new String(bytes));
            break;
        }
        case SnmpSMI.SMI_OPAQUE: {
            m_value = new SnmpOpaque(bytes);
            break;
        }
        case SnmpSMI.SMI_STRING: {
            m_value = new SnmpOctetString(bytes);
            break;
        }
        case SnmpSMI.SMI_ENDOFMIBVIEW: {
        	m_value = new SnmpEndOfMibView();
        	break;
        }
        case SnmpSMI.SMI_NOSUCHINSTANCE: {
        	m_value = new SnmpNoSuchInstance();
        	break;
        }
        case SnmpSMI.SMI_NOSUCHOBJECT: {
        	m_value = new SnmpNoSuchObject();
        	break;
        }
        case SnmpSMI.SMI_NULL: {
            m_value = new SnmpNull();
            break;
        }
        default:
            throw new IllegalArgumentException("invaldi type id "+typeId);
        }    
    }
    
    @Override
    public byte[] getBytes() {
        switch (m_value.typeId()) {
        case SnmpSMI.SMI_COUNTER64:
        case SnmpSMI.SMI_INTEGER:
        case SnmpSMI.SMI_COUNTER32:
        case SnmpSMI.SMI_TIMETICKS:
        case SnmpSMI.SMI_UNSIGNED32:
            return toBigInteger().toByteArray();
        case SnmpSMI.SMI_IPADDRESS:
            return toInetAddress().getAddress();
        case SnmpSMI.SMI_OBJECTID:
            return ((SnmpObjectId)m_value).toString().getBytes();
        case SnmpSMI.SMI_OPAQUE:
        case SnmpSMI.SMI_STRING:
            return ((SnmpOctetString)m_value).getString();
        case SnmpSMI.SMI_ENDOFMIBVIEW:
        case SnmpSMI.SMI_NOSUCHINSTANCE:
        case SnmpSMI.SMI_NOSUCHOBJECT:
        case SnmpSMI.SMI_NULL:
            return new byte[0];
        default:
            throw new IllegalArgumentException("cannot convert "+m_value+" to a byte array");
        }
    }        

    @Override
    public boolean isEndOfMib() {
        return m_value instanceof SnmpEndOfMibView;
    }
    
    @Override
    public boolean isError() {
        switch (getType()) {
        case SnmpValue.SNMP_NO_SUCH_INSTANCE:
        case SnmpValue.SNMP_NO_SUCH_OBJECT:
            return true;
        default:
            return false;
        }
        
    }

    @Override
    public boolean isNumeric() {
        switch (m_value.typeId()) {
        case SnmpSMI.SMI_INTEGER:
        case SnmpSMI.SMI_COUNTER32:
        case SnmpSMI.SMI_COUNTER64:
        case SnmpSMI.SMI_TIMETICKS:
        case SnmpSMI.SMI_UNSIGNED32:
            return true;
        default:
            return false;
        }
    }
    
    @Override
    public int toInt() {
        switch (m_value.typeId()) {
        case SnmpSMI.SMI_COUNTER64:
            return ((SnmpCounter64)m_value).getValue().intValue();
        case SnmpSMI.SMI_INTEGER:
            return ((SnmpInt32)m_value).getValue();
        case SnmpSMI.SMI_COUNTER32:
        case SnmpSMI.SMI_TIMETICKS:
        case SnmpSMI.SMI_UNSIGNED32:
            return (int)((SnmpUInt32)m_value).getValue();
        default:
            return Integer.parseInt(m_value.toString());
        }
    }
    
    @Override
    public long toLong() {
        switch (m_value.typeId()) {
        case SnmpSMI.SMI_COUNTER64:
            return ((SnmpCounter64)m_value).getValue().longValue();
        case SnmpSMI.SMI_INTEGER:
            return ((SnmpInt32)m_value).getValue();
        case SnmpSMI.SMI_COUNTER32:
        case SnmpSMI.SMI_TIMETICKS:
        case SnmpSMI.SMI_UNSIGNED32:
            return ((SnmpUInt32)m_value).getValue();
        case SnmpSMI.SMI_STRING:
	    return (convertStringToLong());
        default:
            return Long.parseLong(m_value.toString());
        }
    }


    private long convertStringToLong() {
        return Double.valueOf(m_value.toString()).longValue();
    }


    
    @Override
    public int getType() {
        return m_value.typeId();
    }

    @Override
    public String toDisplayString() {
        
        switch (m_value.typeId()) {
        case SnmpSMI.SMI_TIMETICKS :
            return Long.toString(toLong());
        case SnmpSMI.SMI_STRING:
            return SnmpOctetString.toDisplayString((SnmpOctetString)m_value);
        default :
            return m_value.toString();
        }
    }

    @Override
    public InetAddress toInetAddress() {
        switch (m_value.typeId()) {
            case SnmpSMI.SMI_IPADDRESS:
                return SnmpIPAddress.toInetAddress((SnmpIPAddress)m_value);
            default:
                throw new IllegalArgumentException("cannot convert "+m_value+" to an InetAddress"); 
        }
    }

    @Override
    public String toHexString() {
        switch (m_value.typeId()) {
        case SnmpSMI.SMI_STRING:
            return SnmpOctetString.toHexString((SnmpOctetString)m_value);
        default:
            throw new IllegalArgumentException("cannt convert "+m_value+" to a HexString");
        }
    }
    
    @Override
    public String toString() {
        return toDisplayString();
    }

    @Override
    public BigInteger toBigInteger() {
        switch (m_value.typeId()) {
        case SnmpSMI.SMI_COUNTER64:
            return ((SnmpCounter64)m_value).getValue();
        case SnmpSMI.SMI_INTEGER:
            return BigInteger.valueOf(((SnmpInt32)m_value).getValue());
        case SnmpSMI.SMI_COUNTER32:
        case SnmpSMI.SMI_TIMETICKS:
        case SnmpSMI.SMI_UNSIGNED32:
            return BigInteger.valueOf(((SnmpUInt32)m_value).getValue());
        default:
            return new BigInteger(m_value.toString());
        }
    }

    @Override
    public SnmpObjId toSnmpObjId() {
        switch (m_value.typeId()) {
        case SnmpSMI.SMI_OBJECTID:
            return SnmpObjId.get(((SnmpObjectId)m_value).getIdentifiers());
        default:
            throw new IllegalArgumentException("cannt convert "+m_value+" to a SnmpObjId");
        }
    }

    @Override
    public boolean isDisplayable() {
        if (isNumeric())
            return true;
        
        if (getType() == SnmpValue.SNMP_OBJECT_IDENTIFIER || getType() == SnmpValue.SNMP_IPADDRESS)
            return true;
        
        if (getType() == SnmpValue.SNMP_OCTET_STRING) {
            return allBytesPlainAscii(getBytes());
        }
        
        return false;
    }

    @Override
    public boolean isNull() {
        return getType() == SnmpValue.SNMP_NULL;
    }

    public SnmpSyntax getSnmpSyntax() {
        return m_value;
    }
    
    @Override
    public int hashCode() {
        if (m_value == null) return 2677;
        return m_value.hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
           if (obj == null) return false;
           if (obj == this) return true;
           if (obj.getClass() != getClass()) return false;

           final JoeSnmpValue that = (JoeSnmpValue)obj;
           return m_value == null ? that.m_value == null : m_value.equals(that.m_value);

    }
    
}
