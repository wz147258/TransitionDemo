package com.sflin.transitiondemo.utis;

import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import com.sflin.transitiondemo.BuildConfig;

import java.util.Locale;

/**
 * @author puppet
 * @date 2017/4/8 0008
 */
public class LogUtils {
    /**
     * 总开关，是否显示log
     */
    private static final boolean showLog = BuildConfig.DEBUG;

    public static final String DEFAULT_TAG = "wangzheng";

    public enum LogLevel {
        INFO, DEBUG, ERROR
    }

    public static void simplePrint(String tag, String info, LogLevel logLevel) {
        if (!showLog) {
            return;
        }
        switch (logLevel) {
            case INFO: {
                Log.i(tag, info);
                break;
            }
            case DEBUG: {
                Log.d(tag, info);
                break;
            }
            case ERROR: {
                Log.e(tag, info);
                break;
            }
        }
    }

    public static void printInfoWithDefaultTag(String msg) {
        print(DEFAULT_TAG, msg, LogLevel.INFO);
    }

    /**
     * 会打印调用print的方法的信息，比如类名，方法名，行号
     */
    public static void print(String tag, String msg, LogLevel logLevel) {
        printByDepth(tag, msg, logLevel, 0);
    }

    public static void printInfo(String tag, Object msg) {
        printByDepth(tag, String.valueOf(msg), LogLevel.INFO, 0);
    }

    /**
     * @param depth 0表示调用printByDepth的方法（记做A），不包含当前类方法的调用，1表示调用A的方法
     */
    public static void printByDepth(String tag, String msg, LogLevel logLevel, int depth) {
        if (!showLog) {
            return;
        }
        if (tag == null) {
            tag = "null";
        }
        // getStackTrace的数组0为当前线程的栈顶，3为调用printByDepth的方法，
        StackTraceElement caller = null;
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        for (int i = depth + 3; i < stackTraceElements.length; i++) {
            StackTraceElement stackTraceElement = stackTraceElements[i];
            if (!TextUtils.equals(LogUtils.class.getName(), stackTraceElement.getClassName())) {
                caller = stackTraceElement;
                break;
            }
        }
        if (caller == null) {
            return;
        }
        switch (logLevel) {
            case INFO: {
                Log.i(tag, generateMsg(caller, msg));
                break;
            }
            case DEBUG: {
                Log.d(tag, generateMsg(caller, msg));
                break;
            }
            case ERROR: {
                Log.e(tag, generateMsg(caller, msg));
                break;
            }
        }
    }

    /**
     * 打印调用堆栈，不包含WZLogUtils的方法调用
     *
     * @param size 表示要打印多少方法堆栈，大于0表示指定多少方法堆栈，小于等于0表示打印全部，
     *             比如1表示仅打印调用printContext的方法
     */
    public static void printContext(String tag, String msg, LogLevel logLevel, int size) {
        if (!showLog) {
            return;
        }
        if (tag == null) {
            tag = "null";
        }

        // getStackTrace的数组：0为当前线程的栈顶，3为调用printContext的方法
        StackTraceElement[] traceElements = Thread.currentThread().getStackTrace();
        final int startIndex = 3;
        if (size <= 0) {
            size = traceElements.length;
        }
        int validCount = 0;
        switch (logLevel) {
            case INFO: {
                for (int i = startIndex; i < traceElements.length; i++) {
                    StackTraceElement traceElement = traceElements[i];
                    if (TextUtils.equals(LogUtils.class.getName(), traceElement.getClassName())) {
                        continue;
                    }
                    Log.i(tag, generateMsg(traceElement, msg));
                    validCount++;
                    if (validCount >= size) {
                        break;
                    }
                }
                break;
            }
            case DEBUG: {
                for (int i = startIndex; i < traceElements.length; i++) {
                    StackTraceElement traceElement = traceElements[i];
                    if (TextUtils.equals(LogUtils.class.getName(), traceElement.getClassName())) {
                        continue;
                    }
                    Log.d(tag, generateMsg(traceElement, msg));
                    validCount++;
                    if (validCount >= size) {
                        break;
                    }
                }
                break;
            }
            case ERROR: {
                for (int i = startIndex; i < traceElements.length; i++) {
                    StackTraceElement traceElement = traceElements[i];
                    if (TextUtils.equals(LogUtils.class.getName(), traceElement.getClassName())) {
                        continue;
                    }
                    Log.e(tag, generateMsg(traceElement, msg));
                    validCount++;
                    if (validCount >= size) {
                        break;
                    }
                }
                break;
            }
        }
    }

    private static String generateMsg(StackTraceElement caller, String msg) {
        if (caller == null) {
            return msg == null ? "" : msg;
        }
        StringBuilder sb = new StringBuilder();

        String callerClazzName = caller.getClassName();
        // 只打印方法名，不包含包名
        callerClazzName = callerClazzName.substring(callerClazzName.lastIndexOf(".") + 1);
        String format = "%s.%s:%d";
        String msgPrefix = String.format(Locale.US, format, callerClazzName, caller.getMethodName(), caller.getLineNumber());
        sb.append(msgPrefix);
        // 快速定位，可点击，只对app的代码有效，系统代码无效
        sb.append("(").append(caller.getFileName()).append(":").append(caller.getLineNumber()).append(")");
        if (!TextUtils.isEmpty(msg)) {
            sb.append(", msg : " + msg);
        }
        return sb.toString();
    }

    /**
     * @param action 通过getAction()获取
     * @return 如果ACTION_POINTER_XX则会加入index
     */
    public static String actionToString(int action) {
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                return "MotionEvent.ACTION_DOWN";
            case MotionEvent.ACTION_UP:
                return "MotionEvent.ACTION_UP";
            case MotionEvent.ACTION_CANCEL:
                return "MotionEvent.ACTION_CANCEL";
            case MotionEvent.ACTION_OUTSIDE:
                return "MotionEvent.ACTION_OUTSIDE";
            case MotionEvent.ACTION_MOVE:
                return "MotionEvent.ACTION_MOVE";
            case MotionEvent.ACTION_HOVER_MOVE:
                return "MotionEvent.ACTION_HOVER_MOVE";
            case MotionEvent.ACTION_SCROLL:
                return "MotionEvent.ACTION_SCROLL";
            case MotionEvent.ACTION_HOVER_ENTER:
                return "MotionEvent.ACTION_HOVER_ENTER";
            case MotionEvent.ACTION_HOVER_EXIT:
                return "MotionEvent.ACTION_HOVER_EXIT";
            case MotionEvent.ACTION_BUTTON_PRESS:
                return "MotionEvent.ACTION_BUTTON_PRESS";
            case MotionEvent.ACTION_BUTTON_RELEASE:
                return "MotionEvent.ACTION_BUTTON_RELEASE";
        }
        int index = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_POINTER_DOWN:
                return "MotionEvent.ACTION_POINTER_DOWN(" + index + ")";
            case MotionEvent.ACTION_POINTER_UP:
                return "MotionEvent.ACTION_POINTER_UP(" + index + ")";
            default:
                return Integer.toString(action);
        }
    }
}
