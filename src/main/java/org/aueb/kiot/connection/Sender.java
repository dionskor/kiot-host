package org.aueb.kiot.connection;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;

import org.aueb.kiot.components.BloomFilter;
import org.aueb.kiot.components.messages.Advertisement;
import org.aueb.kiot.components.messages.Data;
import org.aueb.kiot.components.messages.Interest;
import org.aueb.kiot.components.messages.ParentMessage;
import org.aueb.kiot.general.Configuration;
import org.aueb.kiot.logging.Logger;

/**
 * Sender class is for sending UDP packets
 *
 * @author kiot
 */
public class Sender extends Thread {

	private DatagramSocket ctrlSocket;
	private Object sendDataObject;
	private InetAddress dstIP;
	private int dstPort;

	/**
	 * Constructor
	 *
	 * @param obj  advertisement, interest, data or parent message
	 * @param ip   ip to send
	 * @param port port to send
	 */
	public Sender(Object obj, InetAddress ip, int port) {
		try {
			this.ctrlSocket = new DatagramSocket();
			this.sendDataObject = obj;
			this.dstIP = ip;
			this.dstPort = port;
		} catch (SocketException e) {
			e.printStackTrace();
			Logger.log2(e.toString());
		}
	}

	@Override
	public void run() {
		try {
			byte[] sendData = null;
			sendData = processPacket(sendData);
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, dstIP, dstPort);
			ctrlSocket.send(sendPacket);
			Logger.log("Packet sent");
			ctrlSocket = null;
			sendPacket = null;
		} catch (NullPointerException e) {
			Logger.log2("Packet not sent. Unknown address or null.");
		} catch (IOException e) {
			e.printStackTrace();
			Logger.log2(e.getMessage());
		} catch (Exception e) {
			Logger.log2(e.getMessage());
			e.printStackTrace();
		}
	}

	private byte[] processPacket(byte[] sendData) {
		if (this.sendDataObject instanceof Advertisement) {
			sendData = ((Advertisement) this.sendDataObject).toByteArray(this.dstIP);
			Logger.log("Advertisement packet sending...");
			testBF(sendData);
		} else if (this.sendDataObject instanceof Interest) {
			sendData = ((Interest) this.sendDataObject).toByteArray();
			Logger.log("Interest packet sending...");
			testBF(sendData);
		} else if (this.sendDataObject instanceof Data) {
			sendData = ((Data) this.sendDataObject).toByteArray();
			Logger.log("Data packet sending...");
		} else if (this.sendDataObject instanceof ParentMessage) {
			sendData = ((ParentMessage) this.sendDataObject).toByteArray();
			Logger.log("ParentMessage packet sending...");
		}
		Logger.log("Bloom Filter is: " + BloomFilter.getBF_catalog().getBitSet());

		return sendData;
	}

	private void testBF(byte[] sendData) {
		ByteBuffer buffer = ByteBuffer.wrap(sendData);
		Logger.log("Packet Id=" + getMessageId(buffer));
		Logger.log("Packet BF=" + getBloomFilter(buffer).getBitSet());
	}

	private String getMessageId(ByteBuffer buffer) {
		try {
			byte[] messageIdBytes = new byte[1];
			buffer.get(messageIdBytes, 0, 1);
			return new String(messageIdBytes, "UTF-8");
		} catch (UnsupportedEncodingException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	private BloomFilter getBloomFilter(ByteBuffer buffer) {
		byte[] finalBfBytes = new byte[Configuration.BF_SIZE / 8];
		buffer.get(finalBfBytes, 0, Configuration.BF_SIZE / 8);
		BloomFilter bloomFilter = new BloomFilter(Configuration.BF_SIZE, Configuration.MAX_NUM_TAGS);
		bloomFilter.add(finalBfBytes);
		// BloomFilter[187]
		return bloomFilter;
	}

}
