// Copied from OATemplate project by OABuilder 02/13/19 10:11 AM
package com.oreillyauto.storepurchaseorder.remote;

import java.util.ArrayList;

import com.oreillyauto.storepurchaseorder.model.oa.AppUserLogin;
import com.oreillyauto.storepurchaseorder.model.oa.cs.ClientRoot;
import com.oreillyauto.storepurchaseorder.model.oa.cs.ServerRoot;
import com.viaoa.remote.annotation.OARemoteInterface;
import com.viaoa.util.OAProperties;

@OARemoteInterface
public interface RemoteAppInterface {

    public final static String BindName = "RemoteApp";

    public void saveData();
    
    public AppUserLogin getUserLogin(int clientId, String userId, String password, String location, String userComputerName);
    public ServerRoot getServerRoot();
    public ClientRoot getClientRoot(int clientId);

    public Object testBandwidth(Object data);
    public long getServerTime();
    public boolean disconnectDatabase();

    public String getRelease();
    
    public OAProperties getServerProperties();
    public String getResourceValue(String name);
    
    public boolean writeToClientLogFile(int clientId, ArrayList al);

    public boolean isRunningAsDemo();

    
    // remote clients
    /*$$Start: RemoteAppInterface.remoteClient $$*/
    /*$$End: RemoteAppInterface.remoteClient $$*/
}
