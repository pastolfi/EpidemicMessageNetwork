package hello.world.emn;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class Receiver extends BroadcastReceiver {

    private static final String ACTION_STOP_DISCOVERY = "hello.world.emn.action.STOP_DISCOVERY";
    private static final String ACTION_STOP_ADVERTISING = "hello.world.emn.action.STOP_ADVERTISING";

    @Override
    public void onReceive(Context context, Intent intent) {
        String Action = intent.getAction();
        Log.i("Receiver", Action);
        if (Action.equals(ACTION_STOP_ADVERTISING)){
            ExchangeData.setActionStopAdvertising(context);
        }else if(Action.equals(ACTION_STOP_DISCOVERY)){
            ExchangeData.startActionStopDiscovery(context);
        }
    }
}
