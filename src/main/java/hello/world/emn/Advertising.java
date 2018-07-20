package hello.world.emn;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import hello.world.emn.database.DatabaseHelper;
import hello.world.emn.database.model.Location;
import hello.world.emn.util.Endpoint;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class Advertising extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "hello.world.emn.action.SEND";
    private static final String ACTION_BAZ = "hello.world.emn.action.RECEIVE";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "hello.world.emn.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "hello.world.emn.extra.PARAM2";

    private static final String SERVICE_ID = "hello.world.emn";

    private static final int MAX_LENGTH = 16;

    /**
     * The devices we have pending connections to. They will stay pending until we call  or .
     */
    private final Map<String, Endpoint> mPendingConnections = new HashMap<>();
    private boolean mIsConnecting = false;
    private final Map<String, Endpoint> mEstablishedConnections = new HashMap<>();

    private final Map<String, Endpoint> mDiscoveredEndpoints = new HashMap<>();

    private final ConnectionLifecycleCallback mConnectionLifecycleCallback = new ConnectionLifecycleCallback() {
        @Override
        public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
            Endpoint endpoint = new Endpoint(endpointId, connectionInfo.getEndpointName());
            mPendingConnections.put(endpointId, endpoint);
            Advertising.this.onConnectionInitiated(endpoint, connectionInfo);
            Log.i("ConnectionInitiated", "Connection Intited");
        }

        @Override
        public void onConnectionResult(String endpointId, ConnectionResolution result) {
            mIsConnecting = false;

            if (!result.getStatus().isSuccess()) {
                onConnectionFailed(mPendingConnections.remove(endpointId));
                Log.e("Connection Result", "failed");
                return;
            }
            connectedToEndpoint(mPendingConnections.remove(endpointId));
        }

        @Override
        public void onDisconnected(String endpointId) {
            if (!mEstablishedConnections.containsKey(endpointId)) {
                Log.e("Disconnect", "client non presente");
                return;
            }
            disconnectedFromEndpoint(mEstablishedConnections.get(endpointId));
        }
    };

    protected void onConnectionInitiated(Endpoint endpoint, ConnectionInfo connectionInfo) {}

    protected void onConnectionFailed(Endpoint endpoint) {}

    protected void onEndpointConnected(Endpoint endpoint) {}

    private void connectedToEndpoint(Endpoint endpoint) {
        mEstablishedConnections.put(endpoint.getId(), endpoint);
        onEndpointConnected(endpoint);
        Log.i("connectedToEndPoint", "connesso");
    }

    private void disconnectedFromEndpoint(Endpoint endpoint) {
        mEstablishedConnections.remove(endpoint.getId());
        onEndpointDisconnected(endpoint);
        Log.i("diconnectedFromEndPoint", "disconnesso");
    }

    protected void onEndpointDisconnected(Endpoint endpoint) {}

    private final PayloadCallback mPayloadCallback = new PayloadCallback() {
        @Override
        public void onPayloadReceived(String endpointId, Payload payload) {
            onReceive(mEstablishedConnections.get(endpointId), payload);
        }

        @Override
        public void onPayloadTransferUpdate(String s, PayloadTransferUpdate payloadTransferUpdate) {

        }
    };

    private ConnectionsClient mConnectionsClient;

    /** True if we are discovering. */
    private boolean mIsDiscovering = false;

    /** True if we are advertising. */
    private boolean mIsAdvertising = false;

    /**
     * Someone connected to us has sent us data. Override this method to act on the event.
     *
     * @param endpoint The sender.
     * @param payload The data.
     */
    protected void onReceive(Endpoint endpoint, Payload payload) {
        ByteArrayInputStream bis = new ByteArrayInputStream(payload.asBytes());
        DatabaseHelper db = new DatabaseHelper(this);
        try {
            ObjectInputStream ois = new ObjectInputStream(bis);
            List<Location> ll = (List<Location>)ois.readObject();
            Iterator<Location> iterator = ll.iterator();
            while(iterator.hasNext()){
                Location l = iterator.next();
                db.insertLocation(l.getNome(), l.getLat(), l.getLon());
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("onReceive", e.getMessage());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            Log.e("onReceive", e.getMessage());
        }
        db.close();
        Log.i("onReceive", "payload inserito");
    }


    public Advertising() {
        super("Advertising");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFoo(Context context) {
        Intent intent = new Intent(context, Advertising.class);
        intent.setAction(ACTION_FOO);
        Log.i("startActionFoo", "metodo startActionFoo");
        //intent.putExtra(EXTRA_PARAM1, param1);
        //intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionBaz(Context context) {
        Intent intent = new Intent(context, Advertising.class);
        intent.setAction(ACTION_BAZ);
        //intent.putExtra(EXTRA_PARAM1, param1);
        //intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i("onHandleIntent", "metodo onHandleIntent");
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FOO.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionFoo(param1, param2);
            } else if (ACTION_BAZ.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionBaz(param1, param2);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {

        mConnectionsClient = Nearby.getConnectionsClient(this);
        Log.i("handleActionFoo", "metodo handleActionFoo");
        mIsAdvertising = true;
        final String localEndpointName = getName();

        mConnectionsClient
                .startAdvertising(
                    localEndpointName,
                    SERVICE_ID,
                    mConnectionLifecycleCallback,
                    new AdvertisingOptions(Strategy.P2P_CLUSTER))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        onAdvertisingStarted();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        onAdvertisingFailed();
                    }
                });
    }

    protected void onAdvertisingStarted() {
        DatabaseHelper db = new DatabaseHelper(this);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {

            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(db.getLocations());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Advertising", e.getMessage());
        }
        mConnectionsClient
                .sendPayload(new ArrayList<String>(mEstablishedConnections.keySet()),Payload.fromBytes(bos.toByteArray()))
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Send Payload", e.getMessage());
                    }
                });
    }

    protected void onAdvertisingFailed() {}

    protected String getName() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(MAX_LENGTH);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        mConnectionsClient = Nearby.getConnectionsClient(this);
        mIsDiscovering = true;
        mDiscoveredEndpoints.clear();
        mConnectionsClient
                .startDiscovery(
                        SERVICE_ID,
                        new EndpointDiscoveryCallback() {
                            @Override
                            public void onEndpointFound(String endpointId, DiscoveredEndpointInfo discoveredEndpointInfo) {
                                if(SERVICE_ID.equals(discoveredEndpointInfo.getServiceId())){
                                    Endpoint endpoint = new Endpoint(endpointId, discoveredEndpointInfo.getEndpointName());
                                    mDiscoveredEndpoints.put(endpointId, endpoint);
                                    onEndpointDiscovered(endpoint);
                                }
                            }

                            @Override
                            public void onEndpointLost(String s) {

                            }
                        },
                        new DiscoveryOptions(Strategy.P2P_CLUSTER))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        onDiscoveryStarted();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mIsDiscovering = false;
                        onDiscoveryFailed();
                    }
                });
    }

    protected void onEndpointDiscovered(Endpoint endpoint) {
        mConnectionsClient
                .acceptConnection(endpoint.getId(), mPayloadCallback)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    protected void onDiscoveryStarted() {

    }

    protected void onDiscoveryFailed() {}
}
