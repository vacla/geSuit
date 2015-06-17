package net.cubespace.geSuit.core.messages;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Properties;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.google.common.base.Charsets;

public class LangUpdateMessage extends BaseMessage {
    public Properties messages;
    
    public LangUpdateMessage() {}
    public LangUpdateMessage(Properties messages, Properties defaults) {
        // Combine defaults with messages so we can save the result
        Properties props = new Properties();
        props.putAll(defaults);
        props.putAll(messages);
        
        this.messages = props;
    }
    
    @Override
    public void write(DataOutput out) throws IOException {
        // Compress the result
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        GZIPOutputStream compressed = new GZIPOutputStream(stream);
        OutputStreamWriter writer = new OutputStreamWriter(compressed, Charsets.UTF_8);
        messages.store(writer, "");
        compressed.finish();
        
        // Write the result
        byte[] data = stream.toByteArray();
        
        out.writeShort(data.length);
        out.write(data);
    }

    @Override
    public void read(DataInput in) throws IOException {
        int size = in.readUnsignedShort();
        
        byte[] data = new byte[size];
        in.readFully(data);
        
        // Decompress
        ByteArrayInputStream stream = new ByteArrayInputStream(data);
        GZIPInputStream compressed = new GZIPInputStream(stream);
        InputStreamReader reader = new InputStreamReader(compressed);
        
        messages = new Properties();
        messages.load(reader);
    }

}
