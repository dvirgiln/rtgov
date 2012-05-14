/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008-11, Red Hat Middleware LLC, and others contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.savara.bam.epn.cep;

import static org.junit.Assert.*;

import org.junit.Test;
import org.savara.bam.activity.model.ActivityUnit;
import org.savara.bam.activity.model.soa.RequestReceived;
import org.savara.bam.activity.model.soa.ResponseSent;
import org.savara.bam.epn.cep.CEPEventProcessor;

public class CEPEventProcessorTest {

    private static final int TIME_INTERVAL = 2000;
    private static final int GAP_INTERVAL = 3*60*1000;

    @Test
    public void testPurchasingResponseTime() {
        CEPEventProcessor ep=new CEPEventProcessor();
        ep.setRuleName("PurchasingResponseTime");
        
        ActivityUnit e1=new ActivityUnit();
        e1.setId("e1");
        RequestReceived me1=new RequestReceived();
        me1.setTimestamp(System.currentTimeMillis());
        me1.setMessageId("me1");
        e1.getActivityTypes().add(me1);
        
        ActivityUnit e2=new ActivityUnit();
        e2.setId("e2");
        ResponseSent me2=new ResponseSent();
        me2.setMessageId("me2");
        me2.setReplyToId("me0");
        e2.getActivityTypes().add(me2);
        
        ActivityUnit e3=new ActivityUnit();
        e3.setId("e3");
        ResponseSent me3=new ResponseSent();
        me3.setTimestamp(me1.getTimestamp()+TIME_INTERVAL);
        me3.setMessageId("me3");
        me3.setReplyToId("me1");
        e3.getActivityTypes().add(me3);
        
        try {            
            ep.init();
            
            java.util.Properties props1=(java.util.Properties)ep.process("Purchasing", me1, 0);
            
            if (props1 != null) {
                fail("Should be no result 1");
            }
            
            java.util.Properties props2=(java.util.Properties)ep.process("Purchasing", me2, 0);
            
            if (props2 != null) {
                fail("Should be no result 2");
            }
            
            java.util.Properties props3=(java.util.Properties)ep.process("Purchasing", me3, 0);
            
            if (props3 == null) {
                fail("Result should not be null");
            }
            
            String reqId=(String)props3.get("requestId");
            String respId=(String)props3.get("responseId");
            
            if (!reqId.equals(me1.getMessageId())) {
                fail("Request id incorrect");
            }
            
            if (!respId.equals(me3.getMessageId())) {
                fail("Response id incorrect");
            }
            
            if (!props3.containsKey("responseTime")) {
                fail("Response time not found");
            }
            
            long responseTime=(Long)props3.get("responseTime");
            
            if (responseTime != TIME_INTERVAL) {
                fail("Response time should be "+TIME_INTERVAL+": "+responseTime);
            }
            
        } catch(Exception e) {
            e.printStackTrace();
            fail("Exception: "+e);
        }
    }

    @Test
    public void testPurchasingResponseTimeOutOfOrder() {
        CEPEventProcessor ep=new CEPEventProcessor();
        ep.setRuleName("PurchasingResponseTime");
        
        ActivityUnit e1=new ActivityUnit();
        e1.setId("e1");
        RequestReceived me1=new RequestReceived();
        me1.setTimestamp(System.currentTimeMillis()+GAP_INTERVAL);
        me1.setMessageId("me1");
        e1.getActivityTypes().add(me1);
        
        ActivityUnit e3=new ActivityUnit();
        e3.setId("e3");
        ResponseSent me3=new ResponseSent();
        me3.setTimestamp(System.currentTimeMillis()+GAP_INTERVAL+TIME_INTERVAL);
        me3.setMessageId("me3");
        me3.setReplyToId("me1");
        e3.getActivityTypes().add(me3);
        
        try {            
            ep.init();
            
            java.util.Properties props3=(java.util.Properties)ep.process("Purchasing", me3, 0);
            
            if (props3 != null) {
                fail("Should be no result 1");
            }
            
            java.util.Properties props1=(java.util.Properties)ep.process("Purchasing", me1, 0);
            
            if (props1 == null) {
                fail("Result should not be null");
            }
            
            String reqId=(String)props1.get("requestId");
            String respId=(String)props1.get("responseId");
            
            if (!reqId.equals(me1.getMessageId())) {
                fail("Request id incorrect");
            }
            
            if (!respId.equals(me3.getMessageId())) {
                fail("Response id incorrect");
            }
            
            if (!props1.containsKey("responseTime")) {
                fail("Response time not found");
            }
            
            long responseTime=(Long)props1.get("responseTime");
            
            if (responseTime != TIME_INTERVAL) {
                fail("Response time should be "+TIME_INTERVAL+": "+responseTime);
            }
            
        } catch(Exception e) {
            e.printStackTrace();
            fail("Exception: "+e);
        }
    }
}
