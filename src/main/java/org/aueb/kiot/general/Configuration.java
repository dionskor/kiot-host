package org.aueb.kiot.general;

import java.io.FileInputStream;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

/**
 * This class stores configuration parameters and types of messages.
 */
public class Configuration {

    public static int TOTAL_ADVERTISEMENTS = 0;

    public static final int ROOT_NODE = 1;
    public static final int MIDDLE_NODE = 2;
    public static final int LEAF_NODE = 3;
    /**
     * enable rtt measurement
     */
    public static boolean RTT_ENABLED = false;

    /**
     * Advertisement message
     */
    public static String MSG_ADVERTISEMENT = "1";
    public static String MSG_ADVERTISEMENT_ACK = "2";

    /**
     * Interest message
     */
    public static String MSG_INTEREST = "3";

    /**
     * Data message
     */
    public static String MSG_DATA = "4";

    /**
     * Parent messages
     */
    public static String MSG_PARENT = "5";
    public static String MSG_PARENT_ACK = "6";
    public static String MSG_PARENT_BF = "7";
    public static String MSG_PARENT_CHILD = "8";

    /**
     * RTT message
     */
    public static String RTT_MESSAGE = "9";

    /**
     * Bloom Filter (BitSet) size
     */
    public static int BF_SIZE = 160;

    /**
     * Size of data
     */
    public static int DATA_SIZE = 49;

    /**
     * Maximum number of tags that we can add in Bloom Filter
     */
    public static int MAX_NUM_TAGS = 20;

    /**
     * Maximum wait time for Timer
     */
    public static int TIME = 3000;

    /**
     * The port that every nodes listens
     */
    public static int PORT = 9000;

    /**
     * Number of node's children
     */
    public static int CHILDREN_NUMBER = 0;

    /**
     * Map of potential parents (as key) with number of children (as value)
     */
    public static HashMap<InetAddress, Integer> PARENT_MAP;

    /**
     * List for advertise ACKs
     */
    public static HashSet<InetAddress> ADVERTISEMENT_ACK_LIST = new HashSet<InetAddress>();

    /**
     * Broadcast IP
     */
    public static InetAddress BROADCAST_IP;

    public static InetAddress /*
             * IP = null,
             */ PARENT_IP = null;
    public static int type;
    public static List<String> tags, path;
    public static short logMode;
    public static String outputFile, warningFile, nodeId;
    public static int level;

    /**
     * Reads the configuration file (hard-coded - not given as an argument) and
     * stores the read values to the corresponding variables. Also, parse some
     * other arguments that we will need.
     */
    public static void configure(String config_file) {

        Properties prop = new Properties();

        try {
            prop.load(new FileInputStream(config_file));

            BROADCAST_IP = InetAddress.getByName("10.255.255.255");
            nodeId = prop.getProperty("NodeID");
            level = Integer.parseInt(prop.getProperty("Level"));
            type = Integer.parseInt(prop.getProperty("Type"));
            outputFile = prop.getProperty("OutputFile");
            warningFile = prop.getProperty("WarningFile");
            logMode = Short.parseShort(prop.getProperty("LoggingMode"));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            path = Arrays.asList(prop.getProperty("Path").split(","));
            tags = Arrays.asList(prop.getProperty("Tags").split(","));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

}
