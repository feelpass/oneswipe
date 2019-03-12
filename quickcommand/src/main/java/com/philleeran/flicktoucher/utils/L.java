
package com.philleeran.flicktoucher.utils;

import android.util.Log;


/**
 * Logger class
 *
 * @author senicy
 */
public class L {

    private static LogConfig config = LogConfig.createDefaultConfig();

    public static void setLogConfig(LogConfig logConfig) {
        L.config = logConfig;
    }

    public static void setEnabled(boolean enabled) {
        L.config.enabled = enabled;
    }

    public static boolean isEnabled() {
        return L.config.enabled;
    }

    // verbose
    public static void v(String text) {
        if (config.enabled) {
            if (config.lineFeed) {
                for (String line : text.split("\n")) {
                    verboseOutput(convertText(line, config.stackDepth, config.stackDepth));
                }
            } else {
                verboseOutput(convertText(text, config.stackDepth, config.stackDepth));
            }
        }
    }

    public static void vv(String text) {
        if (config.enabled) {
            if (config.lineFeed) {
                for (String line : text.split("\n")) {
                    verboseOutput(convertText(line, config.stackDepth + 1, config.stackDepth));
                }
            } else {
                verboseOutput(convertText(text, config.stackDepth + 1, config.stackDepth));
            }
        }
    }

    public static void v(String text, Throwable e) {
        if (config.enabled) {
            if (config.lineFeed) {
                for (String line : text.split("\n")) {
                    verboseOutput(convertText(line, config.stackDepth, config.stackDepth), e);
                }
            } else {
                verboseOutput(convertText(text, config.stackDepth, config.stackDepth), e);
            }
        }
    }

    public static void vv(String text, Throwable e) {
        if (config.enabled) {
            if (config.lineFeed) {
                for (String line : text.split("\n")) {
                    verboseOutput(convertText(line, config.stackDepth + 1, config.stackDepth), e);
                }
            } else {
                verboseOutput(convertText(text, config.stackDepth + 1, config.stackDepth), e);
            }
        }
    }

    public static void v(Throwable e) {
        if (config.enabled) {
            if (config.lineFeed) {
                for (String line : e.getMessage().split("\n")) {
                    verboseOutput(convertText(line, config.stackDepth + 1, config.stackDepth), e);
                }
            } else {
                verboseOutput(convertText(e.getMessage(), config.stackDepth + 1, config.stackDepth), e);
            }
        }
    }

    private static void verboseOutput(String log) {
        if (config.android) {
            Log.v(config.tag, log);
        } else {
            System.out.println(log);
        }
    }

    private static void verboseOutput(String log, Throwable e) {
        if (config.android) {
            Log.v(config.tag, log);
        } else {
            System.out.println(log);
            e.printStackTrace();
        }
    }

    // debug

    public static void d(String text) {
        if (config.enabled) {
            Log.d(config.tag, convertText(text, config.stackDepth, config.stackDepth));
        }
    }

    public static void dd(String text) {
        if (config.enabled) {
            Log.d(config.tag, convertText(text, config.stackDepth, config.stackDepth));
            Log.d(config.tag, convertTextForStack(text, config.stackDepth + 1, config.stackDepth + 1));
        }
    }

    public static void dd() {
        if (config.enabled) {
            Log.d(config.tag, convertText("", config.stackDepth, config.stackDepth));
            Log.d(config.tag, convertTextForStack("", config.stackDepth + 1, config.stackDepth + 1));
        }
    }

    public static void d(String text, Throwable e) {
        if (config.enabled) {
            Log.d(config.tag, convertText(text, config.stackDepth, config.stackDepth), e);
        }
    }

    public static void dd(String text, Throwable e) {
        if (config.enabled) {
            Log.d(config.tag, convertText(text, config.stackDepth, config.stackDepth), e);
            Log.d(config.tag, convertTextForStack(text, config.stackDepth + 1, config.stackDepth), e);
        }
    }

    public static void d(Throwable e) {
        if (config.enabled) {
            Log.d(config.tag, e.getMessage(), e);
        }
    }

    // info

    public static void i(String text) {
        if (config.enabled) {
            Log.i(config.tag, convertText(text, config.stackDepth, config.stackDepth));
        }
    }

    public static void ii(String text) {
        if (config.enabled) {
            Log.i(config.tag, convertText(text, config.stackDepth, config.stackDepth));
            Log.i(config.tag, convertTextForStack(text, config.stackDepth + 1, config.stackDepth + 1));
        }
    }

    public static void i(String text, Throwable e) {
        if (config.enabled) {
            Log.i(config.tag, convertText(text, config.stackDepth, config.stackDepth), e);
        }
    }

    public static void ii(String text, Throwable e) {
        if (config.enabled) {
            Log.i(config.tag, convertText(text, config.stackDepth, config.stackDepth), e);
            Log.i(config.tag, convertTextForStack(text, config.stackDepth + 1, config.stackDepth + 1), e);
        }
    }

    public static void i(Throwable e) {
        if (config.enabled) {
            Log.i(config.tag, e.getMessage(), e);
        }
    }

    // warn

    public static void w(String text) {
        if (config.enabled) {
            Log.w(config.tag, convertText(text, config.stackDepth, config.stackDepth));
        }
    }

    public static void ww(String text) {
        if (config.enabled) {
            Log.w(config.tag, convertText(text, config.stackDepth, config.stackDepth));
            Log.w(config.tag, convertTextForStack(text, config.stackDepth + 1, config.stackDepth + 1));
        }
    }

    public static void w(String text, Throwable e) {
        if (config.enabled) {
            Log.w(config.tag, convertText(text, config.stackDepth, config.stackDepth), e);
        }
    }

    public static void ww(String text, Throwable e) {
        if (config.enabled) {
            Log.w(config.tag, convertText(text, config.stackDepth, config.stackDepth), e);
            Log.w(config.tag, convertTextForStack(text, config.stackDepth + 1, config.stackDepth + 1), e);
        }
    }

    public static void w(Throwable e) {
        if (config.enabled) {
            Log.w(config.tag, e.getMessage(), e);
        }
    }

    // error

    public static void e(String text) {
        if (config.enabled) {
            Log.e(config.tag, convertText(text, config.stackDepth, config.stackDepth));
        }
    }

    public static void ee(String text) {
        if (config.enabled) {
            Log.e(config.tag, convertText(text, config.stackDepth + 1, config.stackDepth));
        }
    }

    public static void e(String text, Throwable e) {
        if (config.enabled) {
            Log.e(config.tag, convertText(text, config.stackDepth, config.stackDepth), e);
        }
    }

    public static void e(String format, Object... args) {
        Log.e(config.tag, convertText(String.format(format, args), config.stackDepth, config.stackDepth));
    }
    public static void ee(String text, Throwable e) {
        if (config.enabled) {
            Log.e(config.tag, convertText(text, config.stackDepth, config.stackDepth), e);
            Log.e(config.tag, convertTextForStack(text, config.stackDepth + 1, config.stackDepth + 1), e);
        }
    }

    public static void e(Throwable e) {
        if (config.enabled) {
            Log.e(config.tag, e.getMessage(), e);
        }
    }

    private static String convertText(String text, int fileDepth, int methodDepth) {
        StackTraceElement stackTrace = Thread.currentThread().getStackTrace()[fileDepth];
        StackTraceElement methodStackTrace = Thread.currentThread().getStackTrace()[methodDepth];
        return String.format(config.logFormat, stackTrace.getFileName(), stackTrace.getLineNumber(), methodStackTrace.getMethodName(), text);
    }
    private static String convertTextForStack(String text, int fileDepth, int methodDepth) {
        StackTraceElement stackTrace = Thread.currentThread().getStackTrace()[fileDepth];
        StackTraceElement methodStackTrace = Thread.currentThread().getStackTrace()[methodDepth];
        return String.format(config.logFormatForStack, stackTrace.getFileName(), stackTrace.getLineNumber(), methodStackTrace.getMethodName(), text);
    }
}