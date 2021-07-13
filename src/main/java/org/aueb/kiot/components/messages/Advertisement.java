package org.aueb.kiot.components.messages;

import java.net.InetAddress;
import java.nio.ByteBuffer;

import org.aueb.kiot.components.BloomFilter;
import org.aueb.kiot.general.Configuration;
import org.aueb.kiot.general.SendMessageTimer;

public class Advertisement extends Packet {

    public Advertisement(BloomFilter bf) {
        super(bf, Configuration.MSG_ADVERTISEMENT);
        new SendMessageTimer(Configuration.TIME, Configuration.MSG_ADVERTISEMENT).start();
    }

    public Advertisement(String messageId) {
        super(messageId);
    }

    public byte[] toByteArray() {
        int buff_size;
        ByteBuffer buffer;
        if (this.messageId.equals(Configuration.MSG_ADVERTISEMENT)) {
            buff_size = 2 /*
                     * size of message id
                     */ + Configuration.BF_SIZE / 8 /*
                     * size of bloom filter in bytes
                     */;
            buffer = ByteBuffer.allocate(buff_size);
            byte[] bs = this.bf.getBitSet().toByteArray();
            buffer.put(this.messageId.getBytes());
            buffer.put(bs);
        } else { // else if MSG_ADVERTISEMENT_ACK
            buff_size = 2 /*
                     * size of message id
                     */;
            buffer = ByteBuffer.allocate(buff_size);
            buffer.put(this.messageId.getBytes());
        }
        return buffer.array();
    }

    public byte[] toByteArray(InetAddress dstIP) {
        int buff_size;
        ByteBuffer buffer;
        if (this.messageId.equals(Configuration.MSG_ADVERTISEMENT)) {
            initializeAdvertisementAckList(dstIP);
            buff_size = 2 /*
                     * size of message id
                     */ + Configuration.BF_SIZE / 8 /*
                     * size of bloom filter in bytes
                     */;
            buffer = ByteBuffer.allocate(buff_size);
            byte[] bs = this.bf.getBitSet().toByteArray();
            buffer.put(this.messageId.getBytes());
            buffer.put(bs);
        } else { // else if MSG_ADVERTISEMENT_ACK
            buff_size = 2 /*
                     * size of message id
                     */;
            buffer = ByteBuffer.allocate(buff_size);
            buffer.put(this.messageId.getBytes());
        }
        return buffer.array();
    }

    private void initializeAdvertisementAckList(InetAddress dstIP) {
        Configuration.ADVERTISEMENT_ACK_LIST.add(dstIP);
    }

}
