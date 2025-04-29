package com.beanit.iec61850bean.internal.mms.asn1;

import com.beanit.asn1bean.ber.types.BerNull;

public class FileObtainResponse extends BerNull {

    private static final long serialVersionUID = 1L;

    public FileObtainResponse() {
    }

    public FileObtainResponse(byte[] code) {
        super(code);
    }
}
