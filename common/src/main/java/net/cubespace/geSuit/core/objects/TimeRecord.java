package net.cubespace.geSuit.core.objects;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;

import net.cubespace.geSuit.core.storage.ByteStorable;
import net.cubespace.geSuit.core.util.NetworkUtils;

public class TimeRecord implements ByteStorable {
    private UUID uuid;

	private long timeTotal;
	private long timeSession;
    private long timeToday;
    private long timeWeek;
    private long timeMonth;
    private long timeYear;

    public TimeRecord(UUID uuid) {
        this.uuid = uuid;
    }
    
    public TimeRecord(UUID uuid, long totalTime) {
        this.uuid = uuid;
        this.timeTotal = totalTime;
    }
    
    public TimeRecord() {}

    public UUID getUniqueId() {
		return uuid;
	}

	public void setUniqueId(UUID uuid) {
		this.uuid = uuid;
	}

	public long getTimeTotal() {
		return timeTotal;
	}

	public void setTimeTotal(long timeTotal) {
		this.timeTotal = timeTotal;
	}

	public long getTimeSession() {
		return timeSession;
	}

	public void setTimeSession(long timeSession) {
		this.timeSession = timeSession;
	}

	public long getTimeToday() {
		return timeToday;
	}

	public void setTimeToday(long timeToday) {
		this.timeToday = timeToday;
	}

	public long getTimeWeek() {
		return timeWeek;
	}

	public void setTimeWeek(long timeWeek) {
		this.timeWeek = timeWeek;
	}

	public long getTimeMonth() {
		return timeMonth;
	}

	public void setTimeMonth(long timeMonth) {
		this.timeMonth = timeMonth;
	}

	public long getTimeYear() {
		return timeYear;
	}

	public void setTimeYear(long timeYear) {
		this.timeYear = timeYear;
	}

    @Override
    public void save(DataOutput out) throws IOException {
        NetworkUtils.writeUUID(out, uuid);
        out.writeLong(timeTotal);
        out.writeLong(timeSession);
        out.writeLong(timeToday);
        out.writeLong(timeWeek);
        out.writeLong(timeMonth);
        out.writeLong(timeYear);
    }

    @Override
    public void load(DataInput in) throws IOException {
        uuid = NetworkUtils.readUUID(in);
        timeTotal = in.readLong();
        timeSession = in.readLong();
        timeToday = in.readLong();
        timeWeek = in.readLong();
        timeMonth = in.readLong();
        timeYear = in.readLong();
    }
}