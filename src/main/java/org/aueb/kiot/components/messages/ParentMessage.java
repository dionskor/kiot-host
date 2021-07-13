package org.aueb.kiot.components.messages;

import java.nio.ByteBuffer;
import java.util.HashMap;

import org.aueb.kiot.general.Configuration;
import org.aueb.kiot.general.SendMessageTimer;

public class ParentMessage extends Packet {

    private int numOfChildren;

    public ParentMessage() {
        super(Configuration.level, Configuration.MSG_PARENT);
        new SendMessageTimer(Configuration.TIME, Configuration.MSG_PARENT).start();
    }

    public ParentMessage(String messageId) {
        super(messageId);
    }

    public ParentMessage(String messageId, int numOfChildren) {
        super(messageId);
        this.numOfChildren = numOfChildren;
    }

//    public ParentMessage(BloomFilter bf) {
//        super(bf, Configuration.MSG_PARENT_BF);
//    }
    public byte[] toByteArray() {
        int buff_size;
        ByteBuffer buffer = null;
        if (this.messageId.equals(Configuration.MSG_PARENT)) {
            buffer = processParent(buffer);
        } else if (this.messageId.equals(Configuration.MSG_PARENT_ACK)) {
            buffer = processParentACK(buffer);
        } else if (this.messageId.equals(Configuration.MSG_PARENT_CHILD)) {
            buffer = processParentChild(buffer);
        } else if (this.messageId.equals(Configuration.MSG_PARENT_BF)) {
            buffer = processParentBloomFIlter(buffer);
        }
        return buffer.array();
    }

    private ByteBuffer processParent(ByteBuffer buffer) {
        int buff_size;
        initializeParentMap();
        buff_size = 2 /*
                 * size of message id
                 */ + 4 /*
                 * level size (integer)
                 */;
        buffer = ByteBuffer.allocate(buff_size);
        buffer.put(this.messageId.getBytes());
        buffer.putInt(this.level);
        return buffer;
    }

    private ByteBuffer processParentACK(ByteBuffer buffer) {
        int buff_size = 2 /*
                 * size of message id
                 */ + 4 /*
                 * children parameter size (integer)
                 */;
        buffer = ByteBuffer.allocate(buff_size);
        buffer.put(this.messageId.getBytes());
        buffer.putInt(this.numOfChildren);
        return buffer;
    }

    private ByteBuffer processParentChild(ByteBuffer buffer) {
        int buff_size = 2 /*
                 * size of message id
                 */;
        buffer = ByteBuffer.allocate(buff_size);
        buffer.put(this.messageId.getBytes());
        return buffer;
    }

    private ByteBuffer processParentBloomFIlter(ByteBuffer buffer) {
        int buff_size = 2 /*
                 * size of message id
                 */ + Configuration.BF_SIZE / 8 /*
                 * size of bloom filter in bytes
                 */;
        buffer = ByteBuffer.allocate(buff_size);
        byte[] bs = this.bf.getBitSet().toByteArray();
        buffer.put(this.messageId.getBytes());
        buffer.put(bs);
        return buffer;
    }

    private void initializeParentMap() {
        Configuration.PARENT_MAP = new HashMap();
    }

}
