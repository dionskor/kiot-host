package org.aueb.kiot.logging;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.aueb.kiot.general.Configuration;

/**
 * This class outputs a text log message to the predefined output streams.
 */
public class TxtLogger {

    public static int outputType;
    private PrintWriter outFile, outFile2;
    private static final String LINE_SEPARATOR = "\r\n";
    private static long beginTime = System.currentTimeMillis();

    public TxtLogger(int ot) {
        //System.out.println("[Warning]:Creating new TXTLogger.");
        outputType = ot;

        if (outputType > 1) {
            //System.out.println("[Warning]:[OutputType > 1]");
            File logfile = new File(Configuration.outputFile);
            File warningfile = new File(Configuration.warningFile);
            if (!logfile.exists()) {
                createNewLogfile(logfile);
            } else {
                appendLogfile(logfile);
            }
            if (!warningfile.exists()) {
                createWarningFile(warningfile);
            } else {
                appendWarningFile(warningfile);
            }
        }
    }

    private void appendWarningFile(File warningfile) {
        try {
            outFile2 = new PrintWriter(new FileOutputStream(warningfile, true));
        } catch (FileNotFoundException e) {
            System.out.println("Txt Warning Logger FileNotFound:" + e.getMessage());
        }
    }

    private void createWarningFile(File warningfile) {
        System.out.println("Creating new Warning File");
        try {
            warningfile.createNewFile();
            outFile2 = new PrintWriter(new FileOutputStream(warningfile, true));
        } catch (FileNotFoundException e) {
            System.out.println("Txt Warning Logger FileNotFoundException:" + e.getMessage());
        } catch (IOException e) {
            System.out.println("Txt Warning Logger IOException:" + e.getMessage());
        }
    }

    private void appendLogfile(File logfile) {
        try {
            outFile = new PrintWriter(new FileOutputStream(logfile, true));
        } catch (FileNotFoundException e) {
            System.out.println("Txt Logger FileNotFoundException:" + e.getMessage());
        }
    }

    private void createNewLogfile(File logfile) {
        System.out.println("Creating new Log File");
        try {
            logfile.createNewFile();
            outFile = new PrintWriter(new FileOutputStream(logfile));
        } catch (FileNotFoundException e) {
            System.out.println("Txt Logger FileNotFound Exception:" + e.getMessage());
        } catch (IOException e) {
            System.out.println("Txt Logger IOException:" + e.getMessage());
        }
    }

    public void logold(String msg) {
        //System.out.println("[Warning]: Calling log.");
        Date date = new Date();
        date.toString();
        String timeStamp = String.valueOf((System.currentTimeMillis() - beginTime) / 1000);

        if (outputType == 1) {
            System.out.println(timeStamp + " / " + Configuration.nodeId + " > " + msg);
        } else if (outputType == 2) {
            outFile.append(timeStamp + " / " + Configuration.nodeId + " > " + msg + LINE_SEPARATOR);
            outFile.flush();
        } else {
            System.out.println(timeStamp + " / " + Configuration.nodeId + " > " + msg);
            outFile.append(timeStamp + " / " + Configuration.nodeId + " > " + msg + LINE_SEPARATOR);
            outFile.flush();
        }

    }

    public void log(String msg) {
        if (outputType == 1) {
            System.out.println(dateStamp() + "[h" + Configuration.nodeId + "]:" + msg);
        } else if (outputType == 2) {
            outFile.append(dateStamp() + "[h" + Configuration.nodeId + "]:" + msg + LINE_SEPARATOR);
            outFile.flush();
        } else {
            System.out.println(dateStamp() + "[h" + Configuration.nodeId + "]:" + msg);
            outFile.append(dateStamp() + "[h" + Configuration.nodeId + "]:" + msg + LINE_SEPARATOR);
            outFile.flush();
        }

    }

    public void log2old(String msg) {

        outFile2.append(msg + LINE_SEPARATOR);
        outFile2.flush();

    }

    public void log2(String msg) {
        outFile2.append(dateStamp() + "[h" + Configuration.nodeId + "]:" + msg + LINE_SEPARATOR);
        outFile2.flush();
    }

    private String dateStamp() {
        String info = new String();
        String stringFormat = "[yyyy-MM-dd][HH:mm:ss:SSS]";
        DateFormat sdf = new SimpleDateFormat(stringFormat);
        Date date = new Date();
        info = sdf.format(date);
        return info;
    }

    public void close() {
        try {
            outFile.close();
            outFile2.close();
        } catch (Exception e) {
            System.out.println("Txt Logger Close Exception:" + e.getMessage());
        }
    }

}
