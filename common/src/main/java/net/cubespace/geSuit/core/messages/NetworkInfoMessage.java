package net.cubespace.geSuit.core.messages;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;

public class NetworkInfoMessage extends BaseMessage {
    public int serverId;
    public Map<Integer, String> servers;
    
    public NetworkInfoMessage() {}
    public NetworkInfoMessage(int serverId, Map<Integer, String> servers) {
        this.serverId = serverId;
        this.servers = servers;
    }
    
    @Override
    public void write(DataOutput out) throws IOException {
        out.writeInt(serverId);
        out.writeShort(servers.size());
        for(Entry<Integer, String> server : servers.entrySet()) {
            out.writeInt(server.getKey());
            out.writeUTF(server.getValue());
        }
    }

    @Override
    public void read(DataInput in) throws IOException {
        serverId = in.readInt();
        int size = in.readUnsignedShort();
        servers = Maps.newHashMapWithExpectedSize(size);
        
        for (int i = 0; i < size; ++i) {
            servers.put(in.readInt(), in.readUTF());
        }
    }
}
