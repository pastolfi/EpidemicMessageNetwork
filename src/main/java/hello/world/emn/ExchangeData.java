package hello.world.emn;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.util.SimpleArrayMap;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.messages.Strategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.List;
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
public class ExchangeData extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_DISCOVERY = "hello.world.emn.action.DISCOVERY";
    private static final String ACTION_STOP_DISCOVERY = "hello.world.emn.action.STOP_DISCOVERY";
    private static final String ACTION_ADVERTISING = "hello.world.emn.action.ADVERTISING";
    private static final String ACTION_STOP_ADVERTISING = "hello.world.emn.action.STOP_ADVERTISING";
    private static final String SERVICE_ID = "hello.world.emn";
    private NotificationManager mNotificationManager; // =  (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    private final SimpleArrayMap<Long, NotificationCompat.Builder> incomingPayloads = new SimpleArrayMap<>();
    private final SimpleArrayMap<Long, NotificationCompat.Builder> outgoingPayloads = new SimpleArrayMap<>();
    private Context cont;
    public static final String NOTIFICATION_CHANNEL_ID = "10001";


    // TODO: Rename parameters
    /*private static final String EXTRA_PARAM1 = "hello.world.emn.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "hello.world.emn.extra.PARAM2";*/

    public ExchangeData() {
        super("ExchangeData");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionDiscovery(Context context){ //, String param1, String param2) {
        Intent intent = new Intent(context, ExchangeData.class);
        intent.setAction(ACTION_DISCOVERY);
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
    public static void startActionAdvertising(Context context){ //, String param1, String param2) {
        Intent intent = new Intent(context, ExchangeData.class);
        intent.setAction(ACTION_ADVERTISING);
        //intent.putExtra(EXTRA_PARAM1, param1);
        //intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    public static void setActionStopAdvertising(Context context){
        Log.i("ActionStopAdvertising", "setActionStopAdvertising");
        Intent intent = new Intent(context, ExchangeData.class);
        intent.setAction(ACTION_STOP_ADVERTISING);
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
    public static void startActionStopDiscovery(Context context){ //, String param1, String param2) {
        Log.i("ActionStopDiscovery", "startActionStopDiscovery");
        Intent intent = new Intent(context, ExchangeData.class);
        intent.setAction(ACTION_STOP_DISCOVERY);
        //intent.putExtra(EXTRA_PARAM1, param1);
        //intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mNotificationManager =  (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        this.cont = getApplicationContext();
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_DISCOVERY.equals(action)) {
                /*final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);*/
                handleActionDiscovery(); //(param1, param2);
            } else if (ACTION_ADVERTISING.equals(action)) {
                /*final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);*/
                handleActionAdvertising(); //(param1, param2);
            }else if (ACTION_STOP_DISCOVERY.equals(action)){
                Log.i("onHandleEvent", ACTION_STOP_DISCOVERY);
                handleActionStopDiscovery();
            }else if (ACTION_STOP_ADVERTISING.equals(action)){
                Log.i("onHandleEvent", ACTION_STOP_ADVERTISING);
                handleActionStopAdvertising();
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionDiscovery(){ //(String param1, String param2) {
        //Nearby.getConnectionsClient(this).stopAdvertising();
        final Context context = this;
        Nearby.getConnectionsClient(this)
                .startDiscovery(
                SERVICE_ID,
                mEndpointDiscoveryCallback,
                new DiscoveryOptions(com.google.android.gms.nearby.connection.Strategy.P2P_CLUSTER))
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unusedResult) {
                                // We're discovering!
                                Log.i("success", "discovering started");
                                buildNotification(true);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // We were unable to start discovering.
                            }
                        });
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionAdvertising(){ //(String param1, String param2) {
        //Nearby.getConnectionsClient(this).stopDiscovery();
        final Context context = this;
        Nearby.getConnectionsClient(this).startAdvertising(
                        getName(),
                        SERVICE_ID,
                        mConnectionLifecycleCallback,
                        new AdvertisingOptions(com.google.android.gms.nearby.connection.Strategy.P2P_CLUSTER))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        buildNotification(false);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    private void handleActionStopDiscovery(){ //(String param1, String param2) {
        Log.i("Stop discovery", "Stop discovery");
        Nearby.getConnectionsClient(this).stopDiscovery();
        mNotificationManager.cancel(001);
    }

    private void handleActionStopAdvertising(){
        Log.i("Stop advertising", "Stop advertising");
        Nearby.getConnectionsClient(this).stopAdvertising();
        mNotificationManager.cancel(002);
    }

    @Override
    public boolean stopService(Intent intent){
        final String action = intent.getAction();
        Log.i("stop service", action);
        if (ACTION_STOP_DISCOVERY.equals(action)){
            Log.i("stopService", ACTION_STOP_DISCOVERY);
            handleActionStopDiscovery();
        }else if (ACTION_STOP_ADVERTISING.equals(action)){
            Log.i("stopService", ACTION_STOP_ADVERTISING);
            handleActionStopAdvertising();
        }
        return super.stopService(intent);
    }

    protected String getName() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(16);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }

    private void buildNotification(boolean isIncoming) {

        Intent contentIntent = new Intent(this, Receiver.class);
        contentIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        contentIntent.setAction(isIncoming ? ACTION_STOP_DISCOVERY : ACTION_STOP_ADVERTISING);
        PendingIntent intent = PendingIntent.getBroadcast(this, 1,
                contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent reopenActivity = new Intent(this, MainActivity.class);
        reopenActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent reopenIntent = PendingIntent.getActivity(this, 0, reopenActivity, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_icon_notif)
                .setContentTitle("Epidemic Message Network")
                .setContentText(isIncoming ? "Discovery started..." : "Advertising started...")
                .addAction(R.drawable.ic_icon_stop, "STOP", intent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(reopenIntent);
        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "EMN_CHANNEL", importance);
            assert mNotificationManager != null;
            mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
            mNotificationManager.createNotificationChannel(notificationChannel);
        }
        assert mNotificationManager != null;
        mNotificationManager.notify(isIncoming ? 001 : 002, mBuilder.build());
    }

    protected void onReceive(Payload payload) {
        ByteArrayInputStream bis = new ByteArrayInputStream(payload.asBytes());
        DatabaseHelper db = new DatabaseHelper(this);
        try {
            ObjectInputStream ois = new ObjectInputStream(bis);
            //List<Location> ll = (List<Location>)ois.readObject();
            String jsonString = ois.readObject().toString();
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jArray = jsonObject.getJSONArray("Locations");
            for (int i = 0; i < jArray.length(); i++){
                db.insertReceivedLocation(cont,jArray.getJSONObject(i).getString("ID_LOCATION"),jArray.getJSONObject(i).getString("NOME"), jArray.getJSONObject(i).getDouble("LAT"), jArray.getJSONObject(i).getDouble("LON"));
            }
            Log.i("messaggio", jsonString);
            Toast.makeText(this,"Payload Ricevuto", Toast.LENGTH_LONG ).show();
            /*Iterator<Location> iterator = ll.iterator();
            while(iterator.hasNext()){
                Location l = iterator.next();
                db.insertLocation(l.getNome(), l.getLat(), l.getLon());
            }*/
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("onReceive", e.getMessage());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            Log.e("onReceive", e.getMessage());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        db.close();
        Log.i("onReceive", "payload inserito");
    }

    private final PayloadCallback mPayloadCallback = new PayloadCallback() {
        @Override
        public void onPayloadReceived(String s, Payload payload) {
            // Build and start showing the notification.
            //NotificationCompat.Builder notification = buildNotification(payload, true /*isIncoming*/);
            /*mNotificationManager.notify(payload.getId()., notification.build());
            incomingPayloads.put(payload.getId(), notification);*/
            onReceive(payload);
        }

        @Override
        public void onPayloadTransferUpdate(String s, PayloadTransferUpdate payloadTransferUpdate) {

        }
    };

    private final ConnectionLifecycleCallback mConnectionLifecycleCallback =
            new ConnectionLifecycleCallback() {

                @Override
                public void onConnectionInitiated(
                        String endpointId, ConnectionInfo connectionInfo) {
                    // Automatically accept the connection on both sides.
                    Nearby.getConnectionsClient(cont).acceptConnection(endpointId, mPayloadCallback);
                }

                @Override
                public void onConnectionResult(String endpointId, ConnectionResolution result) {
                    switch (result.getStatus().getStatusCode()) {
                        case ConnectionsStatusCodes.STATUS_OK:
                            // We're connected! Can now start sending and receiving data.
                            Log.i("ConnectionResult", "connesso");
                            sendPayload(endpointId);
                            break;
                        case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                            // The connection was rejected by one or both sides.
                            break;
                        case ConnectionsStatusCodes.STATUS_ERROR:
                            // The connection broke before it was able to be accepted.
                            break;
                    }
                }

                @Override
                public void onDisconnected(String endpointId) {
                    // We've been disconnected from this endpoint. No more data can be
                    // sent or received.
                }
            };

    private final EndpointDiscoveryCallback mEndpointDiscoveryCallback =
            new EndpointDiscoveryCallback() {
                @Override
                public void onEndpointFound(String endpointId, DiscoveredEndpointInfo discoveredEndpointInfo) {
                    // An endpoint was found!
                    Nearby.getConnectionsClient(cont).requestConnection(
                            getName(),
                            endpointId,
                            mConnectionLifecycleCallback)
                           .addOnSuccessListener(new OnSuccessListener<Void>() {
                               @Override
                               public void onSuccess(Void aVoid) {
                                    Log.i("EndPointFound", "endpoint succefully found");
                               }
                           })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                }
                            });
                }

                @Override
                public void onEndpointLost(String endpointId) {
                    // A previously discovered endpoint has gone away.
                }
            };

    private void sendPayload(String endpointId){
        DatabaseHelper db = new DatabaseHelper(this);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(db.getJsonLocations().toString());
            oos.close();
            Nearby.getConnectionsClient(cont).sendPayload(endpointId, Payload.fromBytes(bos.toByteArray()))
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.i("sendPayload", "inviato");
                            Toast.makeText(getApplicationContext(), "Payload inviato", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("sendPayload", e.getMessage());
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Advertising", e.getMessage());
        }

        db.close();
    }
}
