package net.cubespace.geSuit.core.messages;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Sets;

import net.cubespace.geSuit.core.util.NetworkUtils;

public class SyncAttachmentMessage extends BaseMessage {
    public UUID owner;
    public Set<String> updatedAttachments;
    
    public SyncAttachmentMessage() {}
    public SyncAttachmentMessage(UUID owner, Set<String> updatedAttachments) {
        this.owner = owner;
        this.updatedAttachments = updatedAttachments;
    }
    
    @Override
    public void write(DataOutput out) throws IOException {
        NetworkUtils.writeUUID(out, owner);
        out.writeShort(updatedAttachments.size());
        for (String id : updatedAttachments) {
            out.writeUTF(id);
        }
    }

    @Override
    public void read(DataInput in) throws IOException {
        owner = NetworkUtils.readUUID(in);
        int size = in.readUnsignedShort();
        updatedAttachments = Sets.newHashSetWithExpectedSize(size);
        for (int i = 0; i < size; ++i) {
            updatedAttachments.add(in.readUTF());
        }
    }
}
