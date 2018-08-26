package io.magics.notethis.viewmodels;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;

import io.magics.notethis.utils.Utils;

// Based on: https://android.jlelse.eu/connectivitylivedata-6861b9591bcc
class ConnectionLiveData extends LiveData<Boolean> {

    private ConnectivityManager connectivityManager;

    ConnectionLiveData(Application application) {
        connectivityManager =
                (ConnectivityManager) application.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    private NetworkCallback networkCallback = new NetworkCallback() {
        @Override
        public void onAvailable(Network network) {
            postValue(true);
        }

        @Override
        public void onLost(Network network) {
            postValue(false);
        }
    };

    @Override
    protected void onActive() {
        super.onActive();
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        postValue(networkInfo != null && networkInfo.isConnectedOrConnecting());

        if (Utils.SDK_V >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(networkCallback);
        } else {
            NetworkRequest.Builder builder = new NetworkRequest.Builder();
            connectivityManager.registerNetworkCallback(builder.build(), networkCallback);
        }
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        connectivityManager.unregisterNetworkCallback(networkCallback);
    }
}
