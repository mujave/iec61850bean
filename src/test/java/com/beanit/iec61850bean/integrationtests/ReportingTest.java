/*
 * Copyright 2011 The IEC61850bean Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.beanit.iec61850bean.integrationtests;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.RandomUtil;
import com.beanit.iec61850bean.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class ReportingTest implements ClientEventListener {

    private Logger log = LoggerFactory.getLogger(ReportingTest.class);
    private static final String PREEXISTING_DATASET_REFERENCE = "ied1lDevice1/LLN0$dataset1";
    private static final String CREATED_DATASET_REFERENCE = "ied1lDevice1/LLN0$datasetnew";
    private static final String CHANGING_SERVER_DA_REFERENCE_1 =
            "ied1lDevice1/MMXU1.W.phsA.cVal.mag.f";
    private static final int PORT = 54321;
    private static final String ICD_FILE = "src/test/resources/iec61850bean-sample01.icd";
    private static final String URCB1_REFERENCE = "ied1lDevice1/LLN0.urcb101";
    ClientAssociation clientAssociation;
    private ServerSap serverSap;
    private ServerModel serverModel;
    private ServerModel clientModel;
    private int reportCounter = 0;

    @BeforeEach
    public void startServerAndClient() throws SclParseException, IOException, ServiceError {
        startServer();
        startClient();
    }

    private void startClient() throws IOException, ServiceError {
        ClientSap clientSap = new ClientSap();
        this.clientAssociation = clientSap.associate(InetAddress.getByName("127.0.0.1"), PORT, "", this);
        this.clientModel = this.clientAssociation.retrieveModel();
        Collection<Urcb> urcb = clientModel.getUrcbs();
        for (Urcb u : urcb) {
            //从模型里面读取最新状态
            clientAssociation.getRcbValues(u);
            System.out.println("Urcb enable?: " + u.getRptEna());
            if (!u.getRptEna().getValue()) {
                System.out.println(u.getRptId().getName());
                clientAssociation.enableReporting(u);
            }

        }
    }


    private void startServer() throws SclParseException, IOException {
        serverSap = new ServerSap(PORT, 0, null, SclParser.parse(ICD_FILE).get(0), null);
        this.serverSap.startListening(new ServerEventListener() {

            @Override
            public List<ServiceError> write(List<BasicDataAttribute> arg0) {
                return null;
            }

            @Override
            public void serverStoppedListening(ServerSap arg0) {
            }
        });
        this.serverModel = this.serverSap.getModelCopy();
    }

    @Test
    public void reportEnableTest() throws ServiceError, IOException {
        HashSet<Object> enableReportNamees = new HashSet<>();
        Collection<Urcb> urcbs = this.clientModel.getUrcbs();
        for (Urcb urcb : urcbs) {
            clientAssociation.getRcbValues(urcb);
            String rptId = urcb.getRptId().getStringValue();
            log.info("1.{}(rptID:{}) {}", urcb.getName(), rptId, urcb.getRptEna().getValue());
            if (!enableReportNamees.contains(rptId)) {
                clientAssociation.enableReporting(urcb);
                enableReportNamees.add(rptId);
            }
        }
        for (Urcb urcb : urcbs) {
            clientAssociation.getRcbValues(urcb);
            log.info("2.{}(rptID:{}) {}", urcb.getName(), urcb.getRptId().getStringValue(), urcb.getRptEna().getValue());
        }
    }

    public void testSetValueForServer() throws IOException, ServiceError, InterruptedException {
        List<BasicDataAttribute> writeList = CollUtil.newArrayList();
        BdaFloat32 v2 = (BdaFloat32) serverModel.findModelNode("FKMONT/GGIO2.AnInd1.mag.f", Fc.MX);
        //读取模型该节点的当前值
        System.out.println(v2.getFloat().floatValue());
        v2.setFloat(RandomUtil.randomFloat());
        writeList.add(v2);
        //服务端通过服务端能力对象将数据集写入到模型
        serverSap.setValues(writeList);
    }


    @Test
    public void reportTest() throws ServiceError, IOException, InterruptedException {
        //让客户端开启报告
        reportEnableTest();
        //调用服务端发送数据
        while (true){
        testSetValueForServer();
            ThreadUtil.sleep(1000l);
        }
    }

    @Test
    public void syso(){
        System.out.println(this.clientModel);//
        BdaUnicodeString modelNode = (BdaUnicodeString)serverModel.findModelNode("FKMONT/GGIO4.AnInd1.dU", Fc.DC);
         System.out.println(modelNode.getValStr());
    }

    @Override
    public void newReport(Report report) {
        System.out.println("got a report.");
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println(report);
    }


    @Override
    public void associationClosed(IOException arg0) {
    }
}
