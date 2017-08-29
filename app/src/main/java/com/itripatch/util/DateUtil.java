/*
 * Copyright (C) 2014 youten
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.itripatch.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Date Util
 */
public class DateUtil {
    private static final ThreadLocal<SimpleDateFormat> mFormater = new ThreadLocal<SimpleDateFormat>() {
        private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";

        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat(DATE_PATTERN, Locale.ENGLISH);
        }
    };

    private static final ThreadLocal<SimpleDateFormat> mFilenameFormater = new ThreadLocal<SimpleDateFormat>() {
        private static final String FILENAME_PATTERN = "yyyy-MMdd-HH-mm-ss";

        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat(FILENAME_PATTERN, Locale.ENGLISH);
        }
    };

    private static final int SECOND = 1000;
    private static final int MINUTE = 60 * SECOND;
    private static final int HOUR = 60 * MINUTE;
    private static final int DAY = 24 * HOUR;

    /**
     * get "yyyy-MM-dd HH:mm:ss.SSS" String
     */
    public static String get_yyyyMMddHHmmssSSS(long ms) {
        return mFormater.get().format(new Date(ms));
    }

    /**
     * get "yyyy-MMdd-HH-mm-ss.csv" String
     */
    public static String get_nowCsvFilename() {
        return mFilenameFormater.get().format(new Date()) + ".csv";
    }

    public static String get_lastTime(long ms) {
        StringBuffer text = new StringBuffer("");
        if (ms >= MINUTE) {
            if (ms > HOUR) {
                text.append(ms / HOUR).append("hr(s) ");
                ms %= HOUR;
            }
            if (ms > MINUTE) {
                text.append(ms / MINUTE).append("min(s) ");
                ms %= MINUTE;
            }
        } else {
            if (ms > SECOND) {
                text.append(ms / SECOND).append("sec(s) ");
                ms %= SECOND;
            }
        }
        return text.toString();
    }

    private DateUtil() {
        // Util class
    }
}
