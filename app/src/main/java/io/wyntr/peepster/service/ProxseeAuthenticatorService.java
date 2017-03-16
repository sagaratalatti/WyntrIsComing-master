package io.wyntr.peepster.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by sagar on 13-02-2017.
 */

public class ProxseeAuthenticatorService extends Service {

    private ProxseeAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        mAuthenticator = new ProxseeAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
