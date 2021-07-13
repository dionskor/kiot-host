package org.aueb.kiot.components.messages;

import java.nio.ByteBuffer;

import org.aueb.kiot.components.BloomFilter;
import org.aueb.kiot.general.Configuration;

public class Interest extends Packet {

    public Interest(BloomFilter bf) {
        super(bf, Configuration.MSG_INTEREST);
    }

    public byte[] toByteArray() {
        int buff_size = 2 /*
                 * size of message id
                 */ + Configuration.BF_SIZE / 8 /*
                 * size of bloom filter in bytes
                 */;
        ByteBuffer buffer = ByteBuffer.allocate(buff_size);
        byte[] bs = this.bf.getBitSet().toByteArray();
        buffer.put(this.messageId.getBytes());
        buffer.put(bs);
        return buffer.array();
    }

}
