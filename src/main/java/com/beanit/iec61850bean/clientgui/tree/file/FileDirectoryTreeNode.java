package com.beanit.iec61850bean.clientgui.tree.file;

import java.util.Calendar;

import javax.swing.tree.DefaultMutableTreeNode;

import com.beanit.iec61850bean.FileInformation;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;

public class FileDirectoryTreeNode extends DefaultMutableTreeNode {

    public FileDirectoryTreeNode(String name) {
        super(name);
    }

    public void add(FileInformation fileInformation) {
        this.add(StrUtil.splitToArray(fileInformation.getFilename(), "\\"), fileInformation.getFilename(),
                fileInformation.getFileSize(), fileInformation.getLastModified());
    }

    private void add(String[] name, String absPathName, long fileSize, Calendar lastModified) {

        if (name == null || name.length == 0) {
            return; // 或抛出明确的异常，根据业务场景选择
        }
        if (name.length == 1) {
            // 直接添加文件节点
            this.add(new FileTreeNode(name[0], absPathName, fileSize, lastModified));
        } else {
            String currentDirName = name[0];
            boolean isExistingDirFound = false;
            FileDirectoryTreeNode matchedDirNode = null;

            for (int i = 0; i < this.getChildCount(); i++) {
                DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) this.getChildAt(i);

                if (childNode instanceof FileDirectoryTreeNode) {
                    FileDirectoryTreeNode dirNode = (FileDirectoryTreeNode) childNode;
                    Object userObject = dirNode.getUserObject();
                    if (currentDirName.equals(userObject != null ? userObject.toString() : "")) {
                        matchedDirNode = dirNode;
                        isExistingDirFound = true;
                        break; // 4. 找到后立即终止循环，提升性能
                    }
                }
            }

            FileDirectoryTreeNode targetDirNode = isExistingDirFound ? matchedDirNode
                    : new FileDirectoryTreeNode(currentDirName);
            if (name.length > 1) {
                targetDirNode.add(ArrayUtil.sub(name, 1, name.length), absPathName, fileSize, lastModified);
            }

            if (!isExistingDirFound) {
                this.add(targetDirNode);
            }
        }
    }
}
