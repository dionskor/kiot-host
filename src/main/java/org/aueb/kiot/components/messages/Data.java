package org.aueb.kiot.components.messages;

import java.nio.ByteBuffer;

import org.aueb.kiot.general.Configuration;

public class Data extends Packet {

    public Data(String data) {
        super(data, Configuration.MSG_DATA);
    }

    public byte[] toByteArray() {
        int buff_size = 2 /*
                 * size of message id
                 */ + Configuration.DATA_SIZE /*
                 * size of data
                 */;
        ByteBuffer buffer = ByteBuffer.allocate(buff_size);
        buffer.put(this.messageId.getBytes());
        buffer.put(this.data.getBytes());
        return buffer.array();
    }

}
