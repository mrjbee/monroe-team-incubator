package android.util;

/**
 * User: MisterJBee
 * Date: 9/22/13 Time: 12:45 AM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class Log {


    private static int println(String level, String tag, String msg, Throwable tr) {
        System.out.println(level+"   "+tag+": "+msg);
        if (tr != null){
            tr.printStackTrace();
        }
        return 0;
    }

    public static int w(java.lang.String tag, java.lang.String msg) {
        return println("WARN", tag, msg , null);
    }

    public static int w(java.lang.String tag, java.lang.String msg, java.lang.Throwable tr) {
        return println("WARN",tag,msg,tr);
    }

    public static int w(java.lang.String tag, java.lang.Throwable tr) {
        return println("WARN", tag, "'", tr);
    }

    public static int e(java.lang.String tag, java.lang.String msg) {
        return println("ERROR", tag, msg, null);
    }

    public static int e(java.lang.String tag, java.lang.String msg, java.lang.Throwable tr) {
        return println("ERROR", tag, msg, tr);
    }

    public static final int VERBOSE = 2;
    public static final int DEBUG = 3;
    public static final int INFO = 4;
    public static final int WARN = 5;
    public static final int ERROR = 6;
    public static final int ASSERT = 7;


    public static int v(java.lang.String tag, java.lang.String msg) { return 0; }

    public static int v(java.lang.String tag, java.lang.String msg, java.lang.Throwable tr) {  return 0; }

    public static int d(java.lang.String tag, java.lang.String msg) {  return 0;}

    public static int d(java.lang.String tag, java.lang.String msg, java.lang.Throwable tr) {  return 0; }

    public static int i(java.lang.String tag, java.lang.String msg) {  return 0; }

    public static int i(java.lang.String tag, java.lang.String msg, java.lang.Throwable tr) { return 0; }

    public static boolean isLoggable(java.lang.String s, int i){return true; }

    public static java.lang.String getStackTraceString(java.lang.Throwable tr) { return ""; }

    public static int println(int i, java.lang.String s, java.lang.String s1){return 0; }

}
