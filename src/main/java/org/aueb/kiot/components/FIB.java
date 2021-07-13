package org.aueb.kiot.components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.aueb.kiot.general.Address;

/**
 * FIB class will contain the information of "children" nodes. It contains the
 * BF_catalog and the port of each "child" node
 *
 */
public class FIB {

    private static FIB instance;
    private final Map<BloomFilter, Address> fib;

    public FIB() {
        fib = Collections.synchronizedMap(new HashMap<BloomFilter, Address>());
    }

    /**
     * Returns an instance of FIB.
     *
     * @return instance of FIB
     */
    public static FIB getInstance() {
        if (instance == null) {
            instance = new FIB();
        }
        return instance;
    }

    /**
     * Adds a BloomFilter with address. If the address exists replace it.
     *
     * @param bf
     * @param address
     */
    public void add(BloomFilter bf, Address address) {
        for (Address a : fib.values()) {
            if (a.getPort() == address.getPort() && a.getIp().getHostAddress().equals(address.getIp().getHostAddress())) {
                //this.fib.values().remove(a);
                this.fib.put(bf, a);
                return;
            }
        }
        this.fib.put(bf, address);
    }

    /**
     * Returns FIB
     *
     * @return FIB
     */
    public Map<BloomFilter, Address> getFIB() {
        return this.fib;
    }

    /**
     * If the specified BloomFilter is contained in FIB then it returns the
     * address. If more than one BF_catalogs contain the specified BloomFilter
     * then returns all addresses.
     *
     * @param bf Bloom Filter to ckeck if is contained in FIB
     * @return addresses of FIB
     */
    public ArrayList<Address> getAddresses(BloomFilter bf) {
        ArrayList<Address> addresses = new ArrayList();
        for (BloomFilter bloom : this.fib.keySet()) {
            if (bloom.contains(bf)) {
                addresses.add(this.fib.get(bloom));
            }
        }
        return addresses;
    }

    /**
     * Print FIB
     *
     * @return a string with FIB entries
     */
    public String printFIB() {
        String fib = "FIB: ";
        for (BloomFilter bf : this.fib.keySet()) {
            fib += "BitSet: " + bf.getBitSet() + " Address: " + this.fib.get(bf).getIp().toString() + ":" + this.fib.get(bf).getPort() + "\n";
        }
        return fib;
    }

    //testing alt
    public void add2(BloomFilter bf, Address address) {
        Map<Address, BloomFilter> inverseMap = new HashMap<Address, BloomFilter>();
        //Map
        for (Map.Entry<BloomFilter, Address> entry : this.fib.entrySet()) {
            BloomFilter bloomFilter = entry.getKey();
            Address address1 = entry.getValue();
            if (inverseMap.containsKey(address)) {
                bloomFilter = bloomFilter.union(inverseMap.get(address));
                inverseMap.put(address, bloomFilter);
            } else {
                inverseMap.put(address, bloomFilter);
            }
        }
        //Reduce
        Map<BloomFilter, Address> map = new HashMap<BloomFilter, Address>();
        for (Map.Entry<Address, BloomFilter> im : inverseMap.entrySet()) {
            BloomFilter bloomFilter = im.getValue();
            Address a = im.getKey();
            map.put(bloomFilter, a);
        }
        this.fib.putAll(map);
    }

    //testing alt
    public void add3(BloomFilter bf, Address address) {
        if (fib.containsKey(bf)) {
            Address adress2 = fib.get(bf);
            if (fib.containsValue(address)) {

            }
        }
    }

}
