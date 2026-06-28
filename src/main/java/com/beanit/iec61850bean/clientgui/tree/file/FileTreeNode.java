package com.beanit.iec61850bean.clientgui.tree.file;

import java.util.Calendar;

import javax.swing.tree.DefaultMutableTreeNode;

public class FileTreeNode extends DefaultMutableTreeNode {

    private String absPathName;

    private Long fileSize;

    private Calendar lastModified;

    public FileTreeNode(String name, String absPathName, Long fileSize, Calendar lastModified) {
        super(name);
        if (absPathName.charAt(0) != '\\' || absPathName.charAt(0) != '/') {
            absPathName = "\\" + absPathName;
        }
        this.absPathName = absPathName;
        this.fileSize = fileSize;
        this.lastModified = lastModified;
    }

    public String getAbsPathName() {
        return absPathName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public Calendar getLastModified() {
        return lastModified;
    }
}
