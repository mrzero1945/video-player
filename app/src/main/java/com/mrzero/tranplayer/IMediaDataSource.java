package com.mrzero.tranplayer;

import java.io.IOException;

/* loaded from: classes3.dex */
public interface IMediaDataSource {
    void close() throws IOException;

    long getSize() throws IOException;

    int readAt(long j10, byte[] bArr, int i10, int i11) throws IOException;
}
