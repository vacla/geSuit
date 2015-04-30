package net.cubespace.geSuit.core.messages;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.cubespace.geSuit.core.objects.WarnAction;
import net.cubespace.geSuit.core.objects.WarnInfo;
import net.cubespace.geSuit.core.objects.WarnAction.ActionType;
import net.cubespace.geSuit.core.util.NetworkUtils;

public class FireWarnEventMessage extends BaseMessage {
    public WarnInfo warn;
    public WarnAction action;
    public int number;
    
    public FireWarnEventMessage() {}
    
    public FireWarnEventMessage(WarnInfo warn, WarnAction action, int number) {
        this.warn = warn;
        this.action = action;
        this.number = number;
    }
    
    @Override
    public void write(DataOutput out) throws IOException {
        warn.save(out);
        
        out.writeShort(number);
        NetworkUtils.writeEnum(out, action.getType());
        out.writeLong(action.getTime());
    }

    @Override
    public void read(DataInput in) throws IOException {
        warn = new WarnInfo();
        warn.load(in);
        number = in.readUnsignedShort();
        action = new WarnAction(NetworkUtils.readEnum(in, ActionType.class), in.readLong());
    }

}
