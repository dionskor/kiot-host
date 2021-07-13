package org.aueb.kiot.Main;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

import org.aueb.kiot.components.BloomFilter;
import org.aueb.kiot.components.messages.Advertisement;
import org.aueb.kiot.components.messages.ParentMessage;
import org.aueb.kiot.connection.Receiver;
import org.aueb.kiot.connection.Sender;
import org.aueb.kiot.general.Configuration;
import org.aueb.kiot.logging.Logger;

public class Main {

	public static void main(String[] args) throws InterruptedException, SocketException {
		/*
		 * Reads The Configuration File as an argument given by the path
		 */
		if (args[0] == null) {
			Logger.log("Forgot configuration file as parameter");
			return;
		}
		// System.out.println("Creating Logger");
		Configuration.configure(args[0]);
		Logger.log2("Host initialized");
		// Logger.logger = new TxtLogger(Configuration.logMode);
		System.out.println("Creating Reciever");
		new Receiver().start();
		Logger.log("Receiver started...");

		Thread.sleep(1000);
		try {
			/**
			 * Send broadcast message to find parent
			 */
			if (Configuration.type != Configuration.ROOT_NODE && Configuration.PARENT_IP == null) {
				System.out.println("Creating new Sender with Parent Message");
				new Sender(new ParentMessage(), Configuration.BROADCAST_IP, Configuration.PORT).start();
			}

			/**
			 * Send advertisement to parent (if PARENT_IP is predefined)
			 */
			if (Configuration.type == Configuration.LEAF_NODE && Configuration.PARENT_IP != null) {
				Logger.log("Sending Advertisement to Parent Node");
				new Sender(new Advertisement(BloomFilter.getBF_catalog()), Configuration.PARENT_IP, Configuration.PORT)
						.start();
			}
		} catch (Exception e) {
			Logger.log2(e.getMessage());
		}

	}

	static List<InetAddress> listAllBroadcastAddresses() throws SocketException {
		List<InetAddress> broadcastList = new ArrayList<>();
		Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
		while (interfaces.hasMoreElements()) {
			NetworkInterface networkInterface = interfaces.nextElement();

			if (networkInterface.isLoopback() || !networkInterface.isUp()) {
				continue;
			}

			networkInterface.getInterfaceAddresses().stream().map(a -> a.getBroadcast()).filter(Objects::nonNull)
					.forEach(broadcastList::add);
		}
		return broadcastList;
	}

}
