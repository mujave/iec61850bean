package com.beanit.iec61850bean.mms;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beanit.iec61850bean.BasicDataAttribute;
import com.beanit.iec61850bean.BdaBoolean;
import com.beanit.iec61850bean.BdaFloat32;
import com.beanit.iec61850bean.ClientAssociation;
import com.beanit.iec61850bean.ClientEventListener;
import com.beanit.iec61850bean.ClientSap;
import com.beanit.iec61850bean.Fc;
import com.beanit.iec61850bean.FcModelNode;
import com.beanit.iec61850bean.FileInformation;
import com.beanit.iec61850bean.Report;
import com.beanit.iec61850bean.SclParseException;
import com.beanit.iec61850bean.SclParser;
import com.beanit.iec61850bean.ServerEventListener;
import com.beanit.iec61850bean.ServerModel;
import com.beanit.iec61850bean.ServerSap;
import com.beanit.iec61850bean.ServiceError;
import com.beanit.iec61850bean.Urcb;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.thread.ThreadUtil;

public class SimpleServerClientTest implements ClientEventListener {

    private static final Logger log = LoggerFactory.getLogger(SimpleServerClientTest.class);

    ClientAssociation clientAssociation;
    private ServerModel clientModel;

    @BeforeEach
    public void startServerAndClient() throws SclParseException, IOException, ServiceError {
        startServer();
        startClient();
    }

    private void startClient() throws IOException, ServiceError {
        // 创建客户端能力对象
        ClientSap clientSap = new ClientSap();
        // 与服务端建立连接
        this.clientAssociation = clientSap.associate(InetAddress.getByName("localhost"), PORT, "", this);
        // 获取模型文件能力对象
        this.clientModel = this.clientAssociation.retrieveModel();
        // 客户端也可以离线的方式读取本地的模型文件
        // this.clientModel = SclParser.parse(ICD_FILE).get(0);
    }

    private static final int PORT = 102;
    private static final String ICD_FILE = "src/test/resources/simple-test.icd";
    // 服务端能力对象
    private ServerSap serverSap;
    // 模型文件能力对象
    private ServerModel serverModel;

    private void startServer() throws SclParseException, IOException {
        // 从本地加载一个icd文件，并在102端口暴漏一个mms服务端
        serverSap = new ServerSap(PORT, 0, null, SclParser.parse(ICD_FILE).get(0), null);
        // 设置服务端的消息监听器
        this.serverSap.startListening(
                new ServerEventListener() {

                    @Override
                    public List<ServiceError> write(List<BasicDataAttribute> arg0) {
                        return null;
                    }

                    @Override
                    public void serverStoppedListening(ServerSap arg0) {
                    }
                });
        // 设置服务端文件服务的根目录
        // this.serverSap.setFileServiceParentPath("D:\\test\\mms\\");
        this.serverModel = this.serverSap.getModelCopy();
    }

    @Test
    public void testSetValueForServer() throws IOException, ServiceError, InterruptedException {
        List<BasicDataAttribute> writeList = CollUtil.newArrayList();
        // 通过模型中DA的引用名称找到对应的对象
        BdaBoolean v1 = (BdaBoolean) serverModel.findModelNode("FKMONT/GGIO1.Ind1.stVal", Fc.ST);

        // 设置对应要写入的值
        v1.setValue(true);
        // 将DA加入到待写入集合中
        writeList.add(v1);
        BdaFloat32 v2 = (BdaFloat32) serverModel.findModelNode("FKMONT/GGIO2.AnInd1.mag.f", Fc.MX);
        // 读取模型该节点的当前值
        System.out.println(v2.getFloat().floatValue());
        v2.setFloat(1.2f);
        writeList.add(v2);
        // 服务端通过服务端能力对象将数据集写入到模型
        serverSap.setValues(writeList);
    }

    @Test
    public void testSetValueForClient() throws IOException, ServiceError, InterruptedException {
        BdaBoolean v1 = (BdaBoolean) clientModel.findModelNode("FKMONT/GGIO1.Ind1.stVal", Fc.ST);
        System.out.println("1." + v1.getValue());
        BdaFloat32 v2 = (BdaFloat32) clientModel.findModelNode("FKMONT/GGIO2.AnInd1.mag.f", Fc.MX);
        System.out.println("2." + v2.getFloat());
        testSetValueForServer();

        clientAssociation.getDataValues(v1);
        System.out.println("3." + v1.getValue());
        clientAssociation.getDataValues(v2);
        System.out.println("4." + v2.getFloat().floatValue());
    }

    @Test
    public void testGetFileDirectory() throws IOException, ServiceError, InterruptedException {
        List<FileInformation> fileDirectory = this.clientAssociation.getFileDirectory("");
        int i = 0;
        for (FileInformation fileInformation : fileDirectory) {
            log.info("{} - {} sizeof: {} {}", ++i, fileInformation.getFilename(), fileInformation.getFileSize(),
                    DateUtil.formatDateTime(fileInformation.getLastModified().getTime()));
        }
    }

    @Override
    public void newReport(Report report) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'newReport'");
    }

    @Override
    public void associationClosed(IOException e) {
        log.error("Iec61850 mms server has closed");
    }

    @Test
    public void testRcbEnable() {
        while (true) {
            log.info("==================================================");
            Collection<Urcb> urcbs = this.serverModel.getUrcbs();
            for (Urcb urcb : urcbs) {
                log.info("{}:{}", urcb.getName(), urcb.getRptEna().getValue());
            }
            ThreadUtil.sleep(5*1000L);
        }

    }
}
