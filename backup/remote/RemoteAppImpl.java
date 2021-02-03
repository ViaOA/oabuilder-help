// Copied from OATemplate project by OABuilder 11/17/20 12:25 PM
package com.oreillyauto.storepurchaseorder.remote;

import java.util.ArrayList;

import com.oreillyauto.storepurchaseorder.model.oa.AppUser;
import com.oreillyauto.storepurchaseorder.model.oa.AppUserLogin;
import com.oreillyauto.storepurchaseorder.model.oa.cs.ClientRoot;
import com.oreillyauto.storepurchaseorder.model.oa.cs.ServerRoot;
import com.oreillyauto.storepurchaseorder.resource.Resource;
import com.viaoa.util.OAProperties;

public abstract class RemoteAppImpl implements RemoteAppInterface {

    @Override
    public abstract void saveData();

    @Override
    public abstract AppUserLogin getUserLogin(int clientId, String userId, String password, String location, String userComputerName);

    @Override
    public abstract ServerRoot getServerRoot();

    @Override
    public abstract ClientRoot getClientRoot(int clientId);

    @Override
    public String getRelease() {
        int release = Resource.getInt(Resource.APP_Release);
        return release+""; // expecting a String
    }

    @Override
    public abstract boolean isRunningAsDemo();

    @Override
    public Object testBandwidth(Object data) {
        return data;
    }

    @Override
    public long getServerTime() {
        return System.currentTimeMillis();
    }

    @Override
    public abstract boolean disconnectDatabase();

    @Override
    public abstract OAProperties getServerProperties();

    @Override
    public String getResourceValue(String name) {
        return Resource.getValue(name);
    }

    @Override
    public abstract boolean writeToClientLogFile(int clientId, ArrayList al);

}

