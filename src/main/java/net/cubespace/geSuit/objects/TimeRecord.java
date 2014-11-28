package net.cubespace.geSuit.objects;

public class TimeRecord {
    private String uuid;

	private long timeTotal;
    private long timeDaily;
    private long timeWeekly;
    private long timeMonthly;

    public TimeRecord(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public long getTimeTotal() {
		return timeTotal;
	}

	public void setTimeTotal(long timeTotal) {
		this.timeTotal = timeTotal;
	}

	public long getTimeDaily() {
		return timeDaily;
	}

	public void setTimeDaily(long timeDaily) {
		this.timeDaily = timeDaily;
	}

	public long getTimeWeekly() {
		return timeWeekly;
	}

	public void setTimeWeekly(long timeWeekly) {
		this.timeWeekly = timeWeekly;
	}

	public long getTimeMonthly() {
		return timeMonthly;
	}

	public void setTimeMonthly(long timeMonthly) {
		this.timeMonthly = timeMonthly;
	}
}