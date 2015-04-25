package net.cubespace.geSuit.objects;

import java.util.UUID;

public class TimeRecord {
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
}