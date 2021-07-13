package org.aueb.kiot.components.messages;

import org.aueb.kiot.components.BloomFilter;

public abstract class Packet {

    protected BloomFilter bf;
    protected String messageId;
    protected String data;
    protected int level;

    public Packet(BloomFilter bf, String mid) {
        this.bf = bf;
        this.messageId = mid;
    }

    public Packet(String data, String mid) {
        this.messageId = mid;
        this.data = data;
    }

    public Packet(int level, String mid) {
        this.messageId = mid;
        this.level = level;
    }

    public Packet(String mid) {
        this.messageId = mid;
    }

    protected abstract byte[] toByteArray();

}
