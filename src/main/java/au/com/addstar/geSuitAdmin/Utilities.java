package au.com.addstar.geSuitAdmin;

import java.text.DateFormat;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 7/08/2017.
 */
public class Utilities {

        private static final Pattern TIME_PATTERN = Pattern.compile("([0-9]+)([wdhms])");
        private static final int SECOND = 1;
        private static final int MINUTE = SECOND * 60;
        private static final int HOUR = MINUTE * 60;
        private static final int DAY = HOUR * 24;
        private static final int WEEK = DAY * 7;

        /**
         * Parse a string input into seconds, using w(eeks), d(ays), h(ours), m(inutes) and s(econds) For example: 4d8m2s -> 4 days, 8 minutes and 2 seconds
         *
         * @param string String to convert to Seconds
         * @return Seconds
         */
        public static int parseString(String string)
        {
            Matcher m = TIME_PATTERN.matcher(string);
            int total = 0;
            while (m.find()) {
                int amount = Integer.valueOf(m.group(1));
                switch (m.group(2).charAt(0)) {
                    case 's':
                        total += amount * SECOND;
                        break;
                    case 'm':
                        total += amount * MINUTE;
                        break;
                    case 'h':
                        total += amount * HOUR;
                        break;
                    case 'd':
                        total += amount * DAY;
                        break;
                    case 'w':
                        total += amount * WEEK;
                        break;
                }
            }
            return total;
        }

        public static long parseStringtoMillisecs(String string) {
            int total = parseString(string);
            return total * 1000;
        }


    public static String buildTimeDiffString(long timeDiff, int precision) {
        StringBuilder builder = new StringBuilder();

        int count = 0;
        long amount = timeDiff / TimeUnit.DAYS.toMillis(1);
        if (amount >= 1) {
            builder.append(Long.toString(amount));
            if (amount > 1) {
                builder.append(" Days ");
            } else {
                builder.append(" Day ");
            }
            timeDiff -= amount * TimeUnit.DAYS.toMillis(1);
            ++count;
        }

        amount = timeDiff / TimeUnit.HOURS.toMillis(1);
        if (count < precision && amount >= 1) {
            builder.append(Long.toString(amount));
            if (amount > 1) {
                builder.append(" Hours ");
            } else {
                builder.append(" Hour ");
            }
            timeDiff -= amount * TimeUnit.HOURS.toMillis(1);
            ++count;
        }

        amount = timeDiff / TimeUnit.MINUTES.toMillis(1);
        if (count < precision && amount >= 1) {
            builder.append(Long.toString(amount));
            if (amount > 1) {
                builder.append(" Mins ");
            } else {
                builder.append(" Min ");
            }
            timeDiff -= amount * TimeUnit.MINUTES.toMillis(1);
            ++count;
        }

        amount = timeDiff / TimeUnit.SECONDS.toMillis(1);
        if (count < precision && amount >= 1) {
            builder.append(Long.toString(amount));
            if (amount > 1) {
                builder.append(" Secs ");
            } else {
                builder.append(" Sec ");
            }
            timeDiff -= amount * TimeUnit.SECONDS.toMillis(1);
            ++count;
        }

        if (timeDiff < 1000 && builder.length() == 0) {
            builder.append("0 Secs");
        }

        return builder.toString().trim();
    }

    public static String buildShortTimeDiffString(long timeDiff, int precision) {
        StringBuilder builder = new StringBuilder();

        int count = 0;
        long amount = timeDiff / TimeUnit.DAYS.toMillis(1);
        if (amount >= 1) {
            builder.append(Long.toString(amount));
            builder.append("d ");
            timeDiff -= amount * TimeUnit.DAYS.toMillis(1);
            ++count;
        }

        amount = timeDiff / TimeUnit.HOURS.toMillis(1);
        if (count < precision && amount >= 1) {
            builder.append(Long.toString(amount));
            builder.append("h ");
            timeDiff -= amount * TimeUnit.HOURS.toMillis(1);
            ++count;
        }

        amount = timeDiff / TimeUnit.MINUTES.toMillis(1);
        if (count < precision && amount >= 1) {
            builder.append(Long.toString(amount));
            builder.append("m ");
            timeDiff -= amount * TimeUnit.MINUTES.toMillis(1);
            ++count;
        }

        amount = timeDiff / TimeUnit.SECONDS.toMillis(1);
        if (count < precision && amount >= 1) {
            builder.append(Long.toString(amount));
            builder.append("s ");
            timeDiff -= amount * TimeUnit.SECONDS.toMillis(1);
            ++count;
        }

        if (timeDiff < 1000 && builder.length() == 0) {
            builder.append("0s");
        }

        return builder.toString().trim();
    }

    public static String createTimeStampString(long timeStamp) {
        return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG).format(timeStamp);
    }
    }

