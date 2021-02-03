// Copied from OATemplate project by OABuilder 11/17/20 12:25 PM
package com.oreillyauto.storepurchaseorder.remote;

import com.viaoa.remote.annotation.OARemoteInterface;

@OARemoteInterface
public interface RemoteSpellCheckInterface {

    public final static String BindName = "RemoteSpellCheck";
    
    public String[] getMatchingWords(String word);
    public String[] getSoundexMatchingWords(String word);
    public void addNewWord(String word);
    public boolean isWordFound(String word);
    
}
