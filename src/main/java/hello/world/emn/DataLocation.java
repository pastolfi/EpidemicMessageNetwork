package hello.world.emn;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import hello.world.emn.database.DatabaseHelper;

public class DataLocation extends AppCompatActivity implements LocationListener {

    private TextView lat;
    private TextView lon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_location);

        final TextView nome = (TextView) findViewById(R.id.nome);
        lat = (TextView) findViewById(R.id.lat);
        lon = (TextView) findViewById(R.id.lon);
        lat.setKeyListener(null);
        lon.setKeyListener(null);
        final Context context = this;

        Button add = (Button) findViewById(R.id.add);

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            Log.i("PermessiKO", "richiedo permessi");
            ActivityCompat.requestPermissions(DataLocation.this, new String[]{
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    1);

            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        }else{

            Log.i("PermessiOK","Richiedo update della location");
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }



        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (nome.getText().toString().isEmpty()){
                    Toast.makeText(context, "Inserire nome", Toast.LENGTH_LONG).show();
                    return;
                }else if(lat.getText().toString().isEmpty()) {
                    Toast.makeText(context, "Inserire latituidine", Toast.LENGTH_LONG).show();
                    return;
                }else if(lon.getText().toString().isEmpty()) {
                    Toast.makeText(context, "Inserire longitudine", Toast.LENGTH_LONG).show();
                    return;
                }else {
                    DatabaseHelper db = new DatabaseHelper(context);
                    db.insertLocation(nome.getText().toString(), Double.parseDouble(lat.getText().toString()), Double.parseDouble(lon.getText().toString()));
                    db.close();
                    Intent intent = new Intent(context, MainActivity.class);
                    startActivity(intent);
                }
            }
        });

    }

    private void updateMyLocation(Location location){
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        Log.i("Location", Double.toString(latitude)+Double.toString(longitude));
        lat.setText(Double.toString(latitude));
        lon.setText(Double.toString(longitude));
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i("onLocationChanged", "onLocationChanged");
        updateMyLocation(location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
