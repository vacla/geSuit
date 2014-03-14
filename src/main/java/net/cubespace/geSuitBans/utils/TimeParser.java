/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.cubespace.geSuitBans.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Chinwe
 */
public class TimeParser
{

    /**
     * Parse a string input into milliseconds, using w(eeks), d(ays), h(ours), m(inutes) and s(econds) For example: 4d8m2s -> 4 days, 8 minutes and 2 seconds
     *
     * @param string String to convert to Seconds
     * @return Seconds
     */
    public static int parseString(String string)
    {
        List<String> list = new ArrayList<String>();

        String c;
        int goBack = 0;
        for (int i = 0; i < string.length(); i++) {
            c = String.valueOf(string.charAt(i));
            if (c.matches("[a-zA-Z]")) {
                list.add(string.substring(goBack, i + 1));
                goBack = i + 1;

            }
        }
        // Cleanse
        int amount;
        int total = 0;
        char ch;
        for (String st : list) {
            ch = st.charAt(st.length() - 1);
            if (st.length() != 1 && String.valueOf(ch).matches("[M,w,d,h,m,s]")) {
                // Total milliseconds
                amount = Math.abs(Integer.parseInt(st.substring(0, st.length() - 1)));
                switch (ch) {
                    case 's':
                        total += (amount);
                        break;
                    case 'm':
                        total += (amount * 60);
                        break;
                    case 'h':
                        total += (amount * 3600);
                        break;
                    case 'd':
                        total += (amount * 3600 * 24);
                        break;
                    case 'w':
                        total += (amount * 3600 * 24 * 7);
                        break;
                }

            }
        }

        if (total == 0) {
            return -1;
        }

        return total;
    }

}
