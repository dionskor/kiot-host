package org.aueb.kiot.logging;

import org.aueb.kiot.general.Configuration;

/**
 * This class provides a text logger to other classes.
 *
 * @author Ilias Paidakakos (paidakili@aueb.gr)
 * @version 1.0
 */
public class Logger {

    /**
     * Outputs the message in argument to the preselected stream (console and/or
     * file)
     * <p>
     * @param msg A String containing the org.aueb.kiot.logging message</p>
     */
    public static void log(String msg) {
        TxtLogger logger = null;
        try {
            logger = new TxtLogger(Configuration.logMode);
            logger.log(msg);
        } catch (NullPointerException e) {
            System.out.println("Log NullPointerException:" + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (logger != null) {
                logger.close();
                logger = null;
            }
        }
    }

    public static void log2(String msg) {
        TxtLogger logger = null;
        try {
            logger = new TxtLogger(Configuration.logMode);
            logger.log2(msg);
        } catch (NullPointerException e) {
            System.out.println("Warning Log NullPointerException:" + e.getMessage());
        } catch (Exception e) {
            System.out.println("Warning Log Exception:" + e.getMessage());
        } finally {
            if (logger != null) {
                logger.close();
                logger = null;
            }
        }
    }

}
