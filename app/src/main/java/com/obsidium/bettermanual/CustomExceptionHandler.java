package com.obsidium.bettermanual;

import com.github.ma1co.pmcademo.app.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class CustomExceptionHandler implements Thread.UncaughtExceptionHandler
{
    private final Thread.UncaughtExceptionHandler defaultUEH;

    public CustomExceptionHandler()
    {
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
    }

    public static String stacktraceToString(Throwable e) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        e.printStackTrace(printWriter);
        printWriter.close();

        return result.toString();
    }

    public void uncaughtException(Thread t, Throwable e) {
        e.printStackTrace();

        Logger.error(stacktraceToString(e));
        defaultUEH.uncaughtException(t, e);
    }
}