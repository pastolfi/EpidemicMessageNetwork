package hello.world.emn;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;

import hello.world.emn.database.DatabaseHelper;
import hello.world.emn.database.model.Location;

public class MainActivity extends AppCompatActivity {

    private static SimpleAdapter mAdapter;
    private static ArrayList<HashMap<String,String>> listHashMap;
    private DatabaseHelper db;
    private Context context;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.ACCESS_WIFI_STATE,
                        Manifest.permission.CHANGE_WIFI_STATE,
                        Manifest.permission.ACCESS_COARSE_LOCATION},
                1);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                Intent intent = new Intent(context, DataLocation.class);
                startActivity(intent);
            }
        });

        db = new DatabaseHelper(this);

        final String[] from = new String[]{"NOME", "LAT", "LON"};
        final int[] to = new int[]{R.id.nome, R.id.lat, R.id.lon};
        listHashMap = db.getLocationsHashMap();
        mAdapter = new SimpleAdapter(this, listHashMap, R.layout.list_layout, from, to);
        listView = (ListView)findViewById(R.id.listLocation);
        listView.setAdapter(mAdapter);

        final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.i("onRefresh", "onRefresh called from SwipeRefreshLayout");
                listHashMap = (new DatabaseHelper(context)).getLocationsHashMap();
                Log.i("listhashmap", listHashMap.toString());
                SimpleAdapter mAdapter1 = new SimpleAdapter(context, listHashMap, R.layout.list_layout, from, to);
                listView.setAdapter(mAdapter1);
                //mAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        BroadcastReceiver MyReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.i("In Receive", "in Receive");
                if(action.equals("hello.world.emn.action.LOCATION_INSERT")){
                    Log.i("In Receive Action OK", "in Receive Action OK");
                    listHashMap = db.getLocationsHashMap();
                    SimpleAdapter mAdapter1 = new SimpleAdapter(context, listHashMap, R.layout.list_layout, from, to);
                    listView.setAdapter(mAdapter1);
                   // mAdapter.notifyDataSetChanged();
                }
            }
        };

        this.registerReceiver(MyReceiver, new IntentFilter("hello.world.emn.action.LOCATION_INSERT"));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_discovery) {
            //Advertising.startActionBaz(this);
            ExchangeData.startActionDiscovery(this);
            return true;
        }

        if (id == R.id.action_advertising){
            //Advertising.startActionFoo(this);
            ExchangeData.startActionAdvertising(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
