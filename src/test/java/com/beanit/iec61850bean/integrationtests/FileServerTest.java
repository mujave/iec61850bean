package com.beanit.iec61850bean.integrationtests;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beanit.iec61850bean.BasicDataAttribute;
import com.beanit.iec61850bean.ClientAssociation;
import com.beanit.iec61850bean.ClientEventListener;
import com.beanit.iec61850bean.ClientSap;
import com.beanit.iec61850bean.FileInformation;
import com.beanit.iec61850bean.GetFileListener;
import com.beanit.iec61850bean.Report;
import com.beanit.iec61850bean.SclParseException;
import com.beanit.iec61850bean.SclParser;
import com.beanit.iec61850bean.ServerEventListener;
import com.beanit.iec61850bean.ServerModel;
import com.beanit.iec61850bean.ServerSap;
import com.beanit.iec61850bean.ServiceError;
 
import cn.hutool.core.date.DateUtil;
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
        List<FileInformation> fileDirectory = this.clientAssociation.getFileDirectory("/");
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

    @Override
    public void newReport(Report report) {

    }

    @Override
    public void associationClosed(IOException e) {

    }
}