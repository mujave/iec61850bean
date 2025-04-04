package com.beanit.iec61850bean;

import cn.hutool.core.io.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;

public class FileReader {

    private static final Logger logger = LoggerFactory.getLogger(FileReader.class);

    private File file;

    private long offset = 0;

    public FileReader(File file) {
        this.file = file;
    }

    public byte[] read(int length) {
        if (length + offset > file.length()) {
            length = (int) (file.length() - offset);
        }
        byte[] bytes = new byte[length];
        try (BufferedInputStream inputStream = FileUtil.getInputStream(file);) {
            inputStream.skip(offset);
            inputStream.read(bytes);
            offset += length;
            return bytes;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isEndOfFile() {
        return offset >= file.length();
    }

}
