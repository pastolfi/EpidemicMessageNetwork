package hello.world.emn;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import hello.world.emn.database.DatabaseHelper;

public class DataLocation extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_location);

        final TextView nome = (TextView)findViewById(R.id.nome);
        final TextView lat = (TextView)findViewById(R.id.lat);
        final TextView lon = (TextView)findViewById(R.id.lon);
        final Context context = this;

        Button add = (Button)findViewById(R.id.add);

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
}
