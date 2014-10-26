package com.example.natha_000.atlasiptvinterface;

import java.util.ArrayList;
import java.util.List;


public class IPTVHandler {
    public List<IPTVChannels> getAllChannels() {
        List<IPTVChannels> ChannelList = new ArrayList<IPTVChannels>();
        // Select All Query
                IPTVChannels iptvchannel = new IPTVChannels();
                iptvchannel.setAll(2,"AE","239.0.0.20:10005");
                ChannelList.add(iptvchannel);
        // return contact list
        return ChannelList;
    }
}
