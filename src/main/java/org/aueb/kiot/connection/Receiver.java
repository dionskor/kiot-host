package org.aueb.kiot.connection;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;

import org.aueb.kiot.components.BloomFilter;
import org.aueb.kiot.components.FIB;
import org.aueb.kiot.components.messages.Advertisement;
import org.aueb.kiot.components.messages.Data;
import org.aueb.kiot.components.messages.Interest;
import org.aueb.kiot.components.messages.ParentMessage;
import org.aueb.kiot.general.Address;
import org.aueb.kiot.general.Configuration;
import org.aueb.kiot.logging.Logger;

public class Receiver extends Thread {

    private DatagramSocket serverSocket;

    public Receiver() {
        try {
            serverSocket = new DatagramSocket(Configuration.PORT);
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            long startTime = 0;
            while (true) {

                byte[] receiveData = new byte[50];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);

                ByteBuffer buffer = ByteBuffer.wrap(receiveData);
                String messageId = this.getMessageId(buffer);
                //Logger.log("Packet received: messageId = " + messageId);

                this.extractPacket(messageId, buffer, receivePacket, startTime);

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            serverSocket.close();
        }

    }

    //Check message id in order to process the packet
    private void extractPacket(String messageId, ByteBuffer buffer, DatagramPacket receivePacket, long startTime) {
        if (messageId.equals(Configuration.MSG_ADVERTISEMENT)) {
            Logger.log("Advertisement message came");
            this.extractAdverisement(buffer, receivePacket, messageId);
        } else if (messageId.equals(Configuration.MSG_ADVERTISEMENT_ACK)) {
            Logger.log("Advertisement ACK message came");
            Configuration.ADVERTISEMENT_ACK_LIST.remove(receivePacket.getAddress());
        } else if (messageId.equals(Configuration.MSG_INTEREST)) {
            Logger.log("Interest message came");
            startTime = this.extractInterest(buffer);
        } else if (messageId.equals(Configuration.MSG_DATA)) {
            Logger.log("Data message came");
            this.extractData(startTime, buffer);
        } else if (messageId.equals(Configuration.MSG_PARENT)) {
            Logger.log("Parent message came");
            this.extractParent(buffer, receivePacket);
        } else if (messageId.equals(Configuration.MSG_PARENT_ACK)) {
            Logger.log("Parent ACK message came");
            Configuration.PARENT_MAP.put(receivePacket.getAddress(), this.getChildren(buffer));
        } else if (messageId.equals(Configuration.MSG_PARENT_CHILD)) {
            Logger.log("Parent Children message came");
            Configuration.CHILDREN_NUMBER++;
        } else {
            Logger.log("Unknown packet recieved with ID=" + messageId);
        }
    }

    //Recieve the Interest Packet
    private long extractInterest(ByteBuffer buffer) {
        long startTime;
        startTime = System.currentTimeMillis();
        BloomFilter bf_query = this.getBloomFilter(buffer);
        Logger.log("Interest BF= " + bf_query.getBitSet());
        if (Configuration.type < Configuration.LEAF_NODE) {
            for (BloomFilter bf_cat : FIB.getInstance().getFIB().keySet()) {
                if (bf_cat.contains(bf_query)) {
                    this.sendInterest(bf_cat, bf_query);
                } else {
                    Logger.log("Didn't found the query");
                }
            }
        } else if (Configuration.type == Configuration.LEAF_NODE) {
            String data = "";
            for (BloomFilter bf_path_tag : BloomFilter.getBF_tags()) {
                if (bf_query.contains(bf_path_tag) || bf_path_tag.contains(bf_query)) {
                    this.insertLeafTags(bf_path_tag);
                }
            }
        }
        return startTime;
    }

    //When Interest Arrives at the Leaf Nodes Insert tags to BF
    private void insertLeafTags(BloomFilter bf_path_tag) {
        Logger.log("Inserting Leaf Tags");
        for (String tag : Configuration.tags) {
            this.insertTagtoBF(tag, bf_path_tag);
        }
    }

    private void insertTagtoBF(String tag, BloomFilter bf_path_tag) {
        Logger.log("Inserting tag:" + tag + " to BF");
        BloomFilter bf_tag = new BloomFilter(Configuration.BF_SIZE, Configuration.MAX_NUM_TAGS);
        bf_tag.add(tag);
        if (bf_path_tag.contains(bf_tag)) {
            this.sendData(tag);
        }
    }

    private void sendData(String tag) {
        String data;
        data = tag;
        new Sender(new Data(data), Configuration.PARENT_IP, Configuration.PORT).start();
        Logger.log("Sended Data message: Data = " + data);
    }

    private void sendInterest(BloomFilter bf_cat, BloomFilter bf_query) {
        Logger.log2(Configuration.nodeId);
        Address address = FIB.getInstance().getFIB().get(bf_cat);
        new Sender(new Interest(bf_query), address.getIp(), address.getPort()).start();
        Logger.log("Sended Interest message: BloomFilter (BitSet) = " + bf_query.getBitSet() + " to " + address.getIp());
    }

    private void extractParent(ByteBuffer buffer, DatagramPacket receivePacket) {
        int level = getLevel(buffer);

        if (Configuration.level == level - 1) {
            this.sendParentACK(receivePacket);
        }
    }

    private void sendParentACK(DatagramPacket receivePacket) {
        new Sender(new ParentMessage(Configuration.MSG_PARENT_ACK, Configuration.CHILDREN_NUMBER), receivePacket.getAddress(), Configuration.PORT).start();
        Logger.log("Sended Parent ACK message.");
    }

    private void extractData(long startTime, ByteBuffer buffer) {
        Logger.log("Query time: " + (System.currentTimeMillis() - startTime));
        String data = getData(buffer);

        if (Configuration.type > 1) {
            new Sender(new Data(data), Configuration.PARENT_IP, Configuration.PORT).start();
            Logger.log("Sended Data message: Data = " + data);
        } else if (Configuration.type == 1) {
            Logger.log("Data message is: " + data);
        }
    }

    private void extractAdverisement(ByteBuffer buffer, DatagramPacket receivePacket, String messageId) {
        Configuration.TOTAL_ADVERTISEMENTS++;
        BloomFilter bf_cat = this.getBloomFilter(buffer);

        new Sender(new Advertisement(Configuration.MSG_ADVERTISEMENT_ACK), receivePacket.getAddress(), Configuration.PORT).start();
        // Add child BF to FIB<BF,Address>
        FIB.getInstance().add(bf_cat, new Address(receivePacket.getAddress(), Configuration.PORT));
        Logger.log("FIB entry received: BloomFilter (BitSet) = " + bf_cat.getBitSet() + " and messageId = " + messageId);
        Logger.log(FIB.getInstance().printFIB());
        //Unify the packet BF with the curent bf
        BloomFilter.setBF_catalog(BloomFilter.getBF_catalog().union(bf_cat));
        Logger.log("Setted new BF catalog (BitSet): " + BloomFilter.getBF_catalog().getBitSet());

        if (Configuration.type == Configuration.ROOT_NODE) {
            Logger.log("Bloom Filter cardinality: " + BloomFilter.getBF_catalog().getBitSet().cardinality());
            Logger.log("Total Advertisements: " + Configuration.TOTAL_ADVERTISEMENTS);

        }

        if (Configuration.type > Configuration.ROOT_NODE && Configuration.PARENT_IP != null) {
            Logger.log("Transfering Advertisement to Parent node");
            new Sender(new Advertisement(BloomFilter.getBF_catalog()), Configuration.PARENT_IP, Configuration.PORT).start();
        }
    }

    private String getMessageId(ByteBuffer buffer) {
        try {
            byte[] messageIdBytes = new byte[1];
            buffer.get(messageIdBytes, 0, 1);
            return new String(messageIdBytes, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
            Logger.log2("Message ID UnsuportedEncoding Exception" + ex.toString());
        } catch (Exception e) {
            Logger.log2("Message ID Exception:" + e.toString());
        }
        return null;
    }

    private BloomFilter getBloomFilter(ByteBuffer buffer) {
        BloomFilter bloomFilter = new BloomFilter(Configuration.BF_SIZE, Configuration.MAX_NUM_TAGS);
        try {
            byte[] finalBfBytes = new byte[Configuration.BF_SIZE / 8];
            buffer.get(finalBfBytes, 2, Configuration.BF_SIZE / 8);
            bloomFilter.add(finalBfBytes);

        } catch (Exception e) {
            Logger.log2("Get BloomFilter Exception:" + e.toString());
        }
        //BloomFilter[187]
        return bloomFilter;
    }

    private String getData(ByteBuffer buffer) {
        try {
            byte[] dataBytes = new byte[Configuration.DATA_SIZE];
            buffer.get(dataBytes, 0, Configuration.DATA_SIZE);
            return new String(dataBytes, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            Logger.log2("getDataException:" + e.toString());
        }
        return null;
    }

    private int getLevel(ByteBuffer buffer) {
        return buffer.getInt();
    }

    private int getChildren(ByteBuffer buffer) {
        return buffer.getInt();
    }

}
