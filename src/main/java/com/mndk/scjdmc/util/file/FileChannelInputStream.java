package com.mndk.scjdmc.util.file;

import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream;
import sun.misc.Cleaner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

public class FileChannelInputStream extends InputStream {

    private final FileChannel channel;
    private final MappedByteBuffer buffer;
    private final InputStream stream;

    FileChannelInputStream(File file) throws IOException {
        this.channel = FileChannel.open(file.toPath(), StandardOpenOption.READ);
        this.buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        this.stream = new ByteBufferBackedInputStream(buffer);
    }

    @Override
    public int read() throws IOException {
        return this.stream.read();
    }

    @Override
    public void close() throws IOException {
        Cleaner cleaner = ((sun.nio.ch.DirectBuffer) this.buffer).cleaner();
        if (cleaner != null) cleaner.clean();
        this.stream.close();
        this.channel.close();
    }

}
