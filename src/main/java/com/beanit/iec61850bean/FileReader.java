/*
 * Copyright 2025 The Mujave
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
package com.beanit.iec61850bean;

import cn.hutool.core.io.FileUtil;

import java.io.BufferedInputStream;
import java.io.File;

/**
 * 文件读取
 */
public class FileReader { 

    private File file;

    private String readName;

    private long offset = 0;

    public FileReader(File file,String readName) {
        this.file = file;
        this.readName = readName;
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

    public String getReadName() {
        return readName;
    }
}
