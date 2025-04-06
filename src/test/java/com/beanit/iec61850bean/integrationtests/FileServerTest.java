package com.beanit.iec61850bean.integrationtests;

import cn.hutool.core.date.DateUtil;
import com.beanit.iec61850bean.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
public class FileServerTest implements ClientEventListener {

    private static final int PORT = 102;
    private static final String ICD_FILE = "src/test/resources/iec61850bean-sample01.icd";
    private static final String URCB1_REFERENCE = "ied1lDevice1/LLN0.urcb101";
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
        this.clientAssociation = clientSap.associate(InetAddress.getByName("localhost"), PORT, "", this);
        this.clientModel = this.clientAssociation.retrieveModel();
    }

    private void startServer() throws SclParseException, IOException {
        serverSap = new ServerSap(PORT, 0, null, SclParser.parse(ICD_FILE).get(0), null);
        //serverSap.setFileServiceParentPath("D:\\codeSpeace\\test");
        //serverSap.setReportFileDirectory(false);
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
    public void testGetFileDirectory() throws IOException, ServiceError, InterruptedException {
        List<FileInformation> fileDirectory = this.clientAssociation.getFileDirectory("COMTRADE");
        int i = 0;
        for (FileInformation fileInformation : fileDirectory) {
            log.info("{} - {} sizeof: {} {}", ++i, fileInformation.getFilename(), fileInformation.getFileSize(),
                    DateUtil.formatDateTime(fileInformation.getLastModified().getTime()));
        }
    }

    @Test
    public void testGetFile() throws IOException, ServiceError, InterruptedException {
         this.clientAssociation.getFile("/chart.txt", new GetFileListener() {

            @Override
            public boolean dataReceived(byte[] fileData, boolean moreFollows) {
                log.info("Received {} bytes of file data. More data follows: {}", fileData.length, moreFollows);
                log.info("\n{}", new String(fileData));
                return moreFollows;
            }
        });
    }

    @Test
    public void testDeleteFile() throws ServiceError, IOException {
        this.clientAssociation.deleteFile("0312B12000042A3840001_001_01_20250404220705.dat");
    }

    @Override
    public void newReport(Report report) {

    }

    @Override
    public void associationClosed(IOException e) {

    }
}