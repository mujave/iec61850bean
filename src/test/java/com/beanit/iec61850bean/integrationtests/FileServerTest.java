package com.beanit.iec61850bean.integrationtests;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.RandomUtil;

import com.beanit.iec61850bean.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class FileServerTest implements ClientEventListener {

    private static final int PORT = 102;
    private static final String ICD_FILE = "src/test/resources/simple-test.icd";
    private static final Logger log = LoggerFactory.getLogger(FileServerTest.class);
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
        this.clientAssociation = clientSap.associate(InetAddress.getByName("192.168.13.27"), PORT, "", this);
        this.clientModel = this.clientAssociation.retrieveModel();
        this.clientAssociation.enableReporting(clientModel.getUrcb("FKMONT/LLN0.brcb01Ain01"));
    }

    private void startServer() throws SclParseException, IOException {
        serverSap = new ServerSap(PORT, 0, null, SclParser.parse(ICD_FILE).get(0), null);
        serverSap.setFileServiceParentPath("D:\\codeSpeace\\test");

        this.serverSap.startListening(new ServerEventListener() {
            @Override
            public List<ServiceError> write(List<BasicDataAttribute> arg0) {
                return null;
            }

            @Override
            public void serverStoppedListening(ServerSap arg0) {
            }

            @Override
            public int fileDelete(String fileName) {
                log.info("Delete file: {}", fileName);
                return 0;
            }
        });
        this.serverModel = this.serverSap.getModelCopy();
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

    @Test
    public void testGetFile() throws IOException, ServiceError, InterruptedException {
        this.clientAssociation.getFile("/chart.txt", (byte[] fileData, boolean moreFollows) -> {
            log.info("Received {} bytes of file data. More data follows: {}", fileData.length, moreFollows);
            log.info("\n{}", new String(fileData));
            return moreFollows;
        });
    }

    @Test
    public void testDeleteFile() throws ServiceError, IOException {
        this.clientAssociation.deleteFile("1.txt");
    }

    @Test
    public void testReport() throws ServiceError, IOException {
        BdaFloat32 node = (BdaFloat32) serverModel.findModelNode("FKMONT/GGIO2.AnInd1.mag.f", Fc.MX);
        node.getFloat().floatValue();
        System.out.println();
        while (true) {
            node.setFloat(RandomUtil.randomFloat());
            List<BasicDataAttribute> bdas = new ArrayList<>();
            bdas.add(node);
            this.serverSap.setValues(bdas);
            ThreadUtil.sleep(1000);
        }
    }

    @Test
    public void testPutFile() throws ServiceError, IOException {
        this.clientAssociation.writeFile("test.txt", "2.txt");
    }

    @Override
    public void newReport(Report report) {
        System.out.println("newReport: " + report);
    }

    @Override
    public void associationClosed(IOException e) {

    }
}