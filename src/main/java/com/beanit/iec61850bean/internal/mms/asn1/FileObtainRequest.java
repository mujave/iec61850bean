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
package com.beanit.iec61850bean.internal.mms.asn1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import com.beanit.asn1bean.ber.BerLength;
import com.beanit.asn1bean.ber.BerTag;
import com.beanit.asn1bean.ber.ReverseByteArrayOutputStream;
import com.beanit.asn1bean.ber.types.BerType;

public class FileObtainRequest implements BerType, Serializable {

    public static final BerTag tag = new BerTag(BerTag.UNIVERSAL_CLASS, BerTag.CONSTRUCTED, 16);
    private static final long serialVersionUID = 1L;
    private byte[] code = null;
    private FileName sourceFile = null;
    private FileName destinationFile = null;

    public FileObtainRequest() {
    }

    public FileObtainRequest(byte[] code) {
        this.code = code;
    }

    public FileName getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(FileName sourceFile) {
        this.sourceFile = sourceFile;
    }

    public FileName getDestinationFile() {
        return destinationFile;
    }

    public void setDestinationFile(FileName destinationFile) {
        this.destinationFile = destinationFile;
    }

    @Override
    public int encode(OutputStream reverseOS) throws IOException {
        return encode(reverseOS, true);
    }

    public int encode(OutputStream reverseOS, boolean withTag) throws IOException {

        if (code != null) {
            reverseOS.write(code);
            if (withTag) {
                return tag.encode(reverseOS) + code.length;
            }
            return code.length;
        }

        int codeLength = 0;
        if (destinationFile != null) {
            codeLength += destinationFile.encode(reverseOS, false);
            // write tag: CONTEXT_CLASS, CONSTRUCTED, 1
            reverseOS.write(0xA2);
            codeLength += 1;
        }

        if (sourceFile != null) {
            codeLength += sourceFile.encode(reverseOS, false);
            // write tag: CONTEXT_CLASS, CONSTRUCTED, 0
            reverseOS.write(0xA1);
            codeLength += 1;
        }

        codeLength += BerLength.encodeLength(reverseOS, codeLength);

        if (withTag) {
            codeLength += tag.encode(reverseOS);
        }

        return codeLength;
    }

    @Override
    public int decode(InputStream is) throws IOException {
        return decode(is, true);
    }

    public int decode(InputStream is, boolean withTag) throws IOException {
        int tlByteCount = 0;
        int vByteCount = 0;
        BerTag berTag = new BerTag();

        if (withTag) {
            tlByteCount += tag.decodeAndCheck(is);
        }

        BerLength length = new BerLength();
        tlByteCount += length.decode(is);
        int lengthVal = length.val;
        if (lengthVal == 0) {
            return tlByteCount;
        }
        vByteCount += berTag.decode(is);

        if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.CONSTRUCTED, 1)) {
            sourceFile = new FileName();
            vByteCount += sourceFile.decode(is, false);
            if (lengthVal >= 0 && vByteCount == lengthVal) {
                return tlByteCount + vByteCount;
            }
            vByteCount += berTag.decode(is);
        }

        if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.CONSTRUCTED, 2)) {
            destinationFile = new FileName();
            vByteCount += destinationFile.decode(is, false);
            if (lengthVal >= 0 && vByteCount == lengthVal) {
                return tlByteCount + vByteCount;
            }
            vByteCount += berTag.decode(is);
        }

        if (lengthVal < 0) {
            if (!berTag.equals(0, 0, 0)) {
                throw new IOException("Decoded sequence has wrong end of contents octets");
            }
            vByteCount += BerLength.readEocByte(is);
            return tlByteCount + vByteCount;
        }

        throw new IOException(
                "Unexpected end of sequence, length tag: " + lengthVal + ", bytes decoded: " + vByteCount);
    }

    public void encodeAndSave(int encodingSizeGuess) throws IOException {
        ReverseByteArrayOutputStream reverseOS = new ReverseByteArrayOutputStream(encodingSizeGuess);
        encode(reverseOS, false);
        code = reverseOS.getArray();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        appendAsString(sb, 0);
        return sb.toString();
    }

    public void appendAsString(StringBuilder sb, int indentLevel) {

        sb.append("{");
        boolean firstSelectedElement = true;
        if (sourceFile != null) {
            sb.append("\n");
            for (int i = 0; i < indentLevel + 1; i++) {
                sb.append("\t");
            }
            sb.append("sourceFile: ");
            sourceFile.appendAsString(sb, indentLevel + 1);
            firstSelectedElement = false;
        }

        if (destinationFile != null) {
            if (!firstSelectedElement) {
                sb.append(",\n");
            }
            for (int i = 0; i < indentLevel + 1; i++) {
                sb.append("\t");
            }
            sb.append("destinationFile: ");
            destinationFile.appendAsString(sb, indentLevel + 1);
            firstSelectedElement = false;
        }

        sb.append("\n");
        for (int i = 0; i < indentLevel; i++) {
            sb.append("\t");
        }
        sb.append("}");
    }
}
