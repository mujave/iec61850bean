package com.beanit.iec61850bean.integrationtests;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
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
    }

    private void startServer() throws SclParseException, IOException {
        serverSap = new ServerSap(PORT, 0, null, SclParser.parse(ICD_FILE).get(0), null);
        serverSap.setFileServiceParentPath("D:\\codeSpeace\\myTest");

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
        List<FileInformation> fileDirectory = this.clientAssociation.getFileDirectory("重要文档");
        int i = 0;
        for (FileInformation fileInformation : fileDirectory) {
            log.info("{} - {} sizeof: {} {}", ++i, fileInformation.getFilename(), fileInformation.getFileSize(),
                    DateUtil.formatDateTime(fileInformation.getLastModified().getTime()));
        }
    }

    @Test
    public void testGetFile() throws IOException, ServiceError, InterruptedException {
        this.clientAssociation.getFile("/push.bat", (byte[] fileData, boolean moreFollows) -> {
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
    public void testPutFile() throws Exception {
        this.clientAssociation.writeFile("test.txt", FileUtil.file("D:\\codeSpeace\\myTest\\2.txt"));
        ThreadUtil.sleep(10 * 60 * 1000);
    }

    @Override
    public void associationClosed(IOException e) {

    }

    @Override
    public void newReport(Report report) {
        System.out.println("Unimplemented method 'newReport'");
    }
}