package net.cubespace.geSuit.core.objects;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DateDiff provides a parser and formatter for datetime differences.
 * 
 * <p>It parses a format like '2w5f10h20s'. Valid units are:</p>
 * <ul>
 *   <li>y - year (365 days)</li>
 *   <li>mo - month (30 days)</li>
 *   <li>w - week</li>
 *   <li>d - day</li>
 *   <li>h - hour</li>
 *   <li>m - minute</li>
 *   <li>s - second</li>
 * </ul>
 * 
 * Whitespace is ignored.
 */
public class DateDiff {
    private long time;
    
    /**
     * Creates a new DateDiff from a value in milliseconds
     * @param time The time difference in milliseconds
     */
    public DateDiff(long time) {
        this.time = time;
    }
    
    /**
     * @return Returns the time difference added to now
     */
    public long fromNow() {
        return System.currentTimeMillis() + time;
    }
    
    /**
     * Returns the time difference added to the date
     * @param date The date to start from
     * @return The resulting time
     */
    public long from(long date) {
        return date + time;
    }
    
    /**
     * @return Returns the value of the time difference in milliseconds
     */
    public long toMillis() {
        return time;
    }
    
    private static String[] unitNames = {"y", "mo", "w", "d", "h", "m", "s"};
    private static int[] unitValues = {365, 30, 7, 1, 1, 1, 1};
    private static TimeUnit[] units = {TimeUnit.DAYS, TimeUnit.DAYS, TimeUnit.DAYS, TimeUnit.DAYS, TimeUnit.HOURS, TimeUnit.MINUTES, TimeUnit.SECONDS};
    
    private static Pattern overallPattern = Pattern.compile("([0-9]+[a-zA-Z]+)(\\s*[0-9]+[a-zA-Z]+)*");
    private static Pattern pattern = Pattern.compile("([0-9]+)([a-zA-Z]+)");
    /**
     * Parses the string as a date difference. See {@link DateDiff} for details on the format expected
     * @param text The formatted text to parse
     * @return An equivalent DateDiff
     * @throws IllegalArgumentException Thrown if the value cannot be parsed
     */
    public static DateDiff valueOf(String text) {
        
        Matcher matcher = overallPattern.matcher(text);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Unable to convert " + text + " into a date diff.");
        }
        
        long time = 0;
        
        matcher = pattern.matcher(text);
        while(matcher.find()) {
            int value = Integer.parseInt(matcher.group(1));
            String name = matcher.group(2);
            for (int i = 0; i < units.length; ++i) {
                if (unitNames[i].equalsIgnoreCase(name)) {
                    time += units[i].toMillis(value * unitValues[i]);
                    break;
                }
            }
        }
        
        return new DateDiff(time);
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        
        long value = time;
        
        while(value >= 1000) {
            for (int i = 0; i < units.length; ++i) {
                long unitValue = units[i].convert(value, TimeUnit.MILLISECONDS) / unitValues[i];
                if (unitValue > 0) {
                    builder.append(unitValue);
                    builder.append(unitNames[i]);
                    
                    value -= units[i].toMillis(unitValue * unitValues[i]);
                }
            }
        }
        
        return builder.toString();
    }
}
