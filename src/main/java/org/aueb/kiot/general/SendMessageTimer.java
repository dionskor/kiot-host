package org.aueb.kiot.general;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.aueb.kiot.components.BloomFilter;
import org.aueb.kiot.components.messages.Advertisement;
import org.aueb.kiot.components.messages.ParentMessage;
import org.aueb.kiot.connection.Sender;
import org.aueb.kiot.logging.Logger;

public class SendMessageTimer extends Thread {

    private final int time;
    private final String _case;

    public SendMessageTimer(int time, String _case) {
        this.time = time;
        this._case = _case;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(time);
            if (_case.equals(Configuration.MSG_PARENT)) { // parent case
                if (!Configuration.PARENT_MAP.isEmpty()) {
                    if (Configuration.RTT_ENABLED) {
                        chooseParentByRTT();
                    } else {
                        chooseParentByChildren();
                    }
                } else {
                    if (Configuration.type != Configuration.ROOT_NODE) {
                        new Sender(new ParentMessage(), Configuration.BROADCAST_IP, Configuration.PORT).start();
                    }
                }
            } else if (_case.equals(Configuration.MSG_ADVERTISEMENT)) { // advertise case
                if (!Configuration.ADVERTISEMENT_ACK_LIST.isEmpty()) {
                    System.out.println(Configuration.ADVERTISEMENT_ACK_LIST);
                    new Sender(new Advertisement(BloomFilter.getBF_catalog()), Configuration.PARENT_IP, Configuration.PORT).start();
                }
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
            start();
        }

    }

    /**
     * choose parent with less children
     */
    private void chooseParentByChildren() {
        Logger.log("-----Potential Parents-----");
        for (Map.Entry<InetAddress, Integer> entry : Configuration.PARENT_MAP.entrySet()) {
            Logger.log(entry.getKey() + " - " + entry.getValue());
        }
        Logger.log("---------------------------");
        Map.Entry<InetAddress, Integer> minEntry = null;
        for (Map.Entry<InetAddress, Integer> entry : Configuration.PARENT_MAP.entrySet()) {
            if (minEntry == null || entry.getValue().compareTo(minEntry.getValue()) < 0) {
                minEntry = entry;
            }
        }
        Configuration.PARENT_IP = minEntry.getKey();
        // send to parent to increase it's children
        new Sender(new ParentMessage(Configuration.MSG_PARENT_CHILD), Configuration.PARENT_IP, Configuration.PORT).start();
        // send advertisement message after parent initialization
        if (Configuration.type != 1) {
            new Sender(new Advertisement(BloomFilter.getBF_catalog()), Configuration.PARENT_IP, Configuration.PORT).start();
        }
        Logger.log("Parent: " + Configuration.PARENT_IP);
    }

    /**
     * choose parent with less RTT
     */
    private void chooseParentByRTT() {
        Logger.log("-----Potential Parents-----");
        for (Map.Entry<InetAddress, Integer> entry : Configuration.PARENT_MAP.entrySet()) {
            Logger.log(entry.getKey() + " - " + entry.getValue());
        }
        Logger.log("---------------------------");
        HashMap<InetAddress, Long> rtts = new HashMap<InetAddress, Long>();
        for (InetAddress ia : Configuration.PARENT_MAP.keySet()) {
            try {
                if (ia.isReachable(5000)) {
                    if (ia.isReachable(5000)) {
                        long startTime = System.currentTimeMillis();
                        if (ia.isReachable(5000)) {
                            rtts.put(ia, System.currentTimeMillis() - startTime);
                        }
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        Logger.log("----------------- RTT -------------------");
        for (Map.Entry<InetAddress, Long> entry : rtts.entrySet()) {
            Logger.log(entry.getKey() + " - " + entry.getValue());
        }
        Logger.log("-----------------------------------------");
        Map.Entry<InetAddress, Long> minEntry = null;
        for (Map.Entry<InetAddress, Long> entry : rtts.entrySet()) {
            if (minEntry == null || entry.getValue().compareTo(minEntry.getValue()) < 0) {
                minEntry = entry;
            }
        }
        Configuration.PARENT_IP = minEntry.getKey();
        // send to parent to increase it's children
        new Sender(new ParentMessage(Configuration.MSG_PARENT_CHILD), Configuration.PARENT_IP, Configuration.PORT).start();
        // send advertisement message after parent initialization
        if (Configuration.type != Configuration.ROOT_NODE) {
            new Sender(new Advertisement(BloomFilter.getBF_catalog()), Configuration.PARENT_IP, Configuration.PORT).start();
        }
        Logger.log("Parent: " + Configuration.PARENT_IP);
    }

    private class RTT extends Thread {

        public RTT() {

        }

        @Override
        public void run() {
            HashMap<InetAddress, Long> rtts = new HashMap<InetAddress, Long>();
            for (InetAddress ia : Configuration.PARENT_MAP.keySet()) {
                try {
                    long startTime = System.currentTimeMillis();
                    if (ia.isReachable(5000)) {
                        if (ia.isReachable(5000)) {
                            if (ia.isReachable(5000)) {
                                rtts.put(ia, System.currentTimeMillis() - startTime);
                            }
                        }
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            Logger.log("----------------- RTT -------------------");
            for (Map.Entry<InetAddress, Long> entry : rtts.entrySet()) {
                Logger.log(entry.getKey() + " - " + entry.getValue());
            }
            Logger.log("-----------------------------------------");
            Map.Entry<InetAddress, Long> minEntry = null;
            for (Map.Entry<InetAddress, Long> entry : rtts.entrySet()) {
                if (minEntry == null || entry.getValue().compareTo(minEntry.getValue()) < 0) {
                    minEntry = entry;
                }
            }
            Configuration.PARENT_IP = minEntry.getKey();
            // send to parent to increase it's children
            new Sender(new ParentMessage(Configuration.MSG_PARENT_CHILD), Configuration.PARENT_IP, Configuration.PORT).start();
            // send advertisement message after parent initialization
            if (Configuration.type != Configuration.ROOT_NODE) {
                new Sender(new Advertisement(BloomFilter.getBF_catalog()), Configuration.PARENT_IP, Configuration.PORT).start();
            }
            Logger.log("Parent: " + Configuration.PARENT_IP);
        }

    }

}
