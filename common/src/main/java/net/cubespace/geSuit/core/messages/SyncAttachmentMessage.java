package net.cubespace.geSuit.core.messages;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import com.google.common.collect.Maps;

import net.cubespace.geSuit.core.attachments.Attachment;
import net.cubespace.geSuit.core.util.NetworkUtils;

public class SyncAttachmentMessage extends BaseMessage {
    public UUID owner;
    public String className;
    public Map<String, String> values;
    
    public SyncAttachmentMessage() {}
    public SyncAttachmentMessage(UUID owner, Class<? extends Attachment> clazz, Map<String, String> values) {
        this.owner = owner;
        this.className = clazz.getName();
        this.values = values;
    }
    
    @Override
    public void write(DataOutput out) throws IOException {
        NetworkUtils.writeUUID(out, owner);
        out.writeUTF(className);
        out.writeShort(values.size());
        for (Entry<String, String> entry : values.entrySet()) {
            out.writeUTF(entry.getKey());
            out.writeUTF(entry.getValue());
        }
    }

    @Override
    public void read(DataInput in) throws IOException {
        owner = NetworkUtils.readUUID(in);
        className = in.readUTF();
        int size = in.readUnsignedShort();
        values = Maps.newHashMapWithExpectedSize(size);
        for (int i = 0; i < size; ++i) {
            values.put(in.readUTF(), in.readUTF());
        }
    }
}
