package hello.world.emn.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import hello.world.emn.MainActivity;
import hello.world.emn.database.model.Location;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "EMN";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(Location.CREATE_TABLE);
    }

    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+Location.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public long insertLocation(String nome, double lat, double lon){
        String uuid = UUID.randomUUID().toString();
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Location.COLUMN_ID_LOCATION, uuid);
        values.put(Location.COLUMN_NOME, nome);
        values.put(Location.COLUMN_LAT, lat);
        values.put(Location.COLUMN_LON, lon);

        long id = db.insert(Location.TABLE_NAME, null, values);

        db.close();

        return id;
    }

    public long insertReceivedLocation(Context context, String uuid, String nome, double lat, double lon){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Location.COLUMN_ID_LOCATION, uuid);
        values.put(Location.COLUMN_NOME, nome);
        values.put(Location.COLUMN_LAT, lat);
        values.put(Location.COLUMN_LON, lon);

        long id = db.insertWithOnConflict(Location.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        if(id == -1){
            id = db.update(Location.TABLE_NAME, values, Location.COLUMN_ID_LOCATION+"=?", new String[]{uuid});
        }

        db.close();
        Intent intent = new Intent();
        intent.setAction("hello.world.emn.action.LOCATION_INSERT");
        context.sendBroadcast(intent);
        return id;
    }

    public List<Location> getLocations(){
        List<Location> locations = new ArrayList<>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + Location.TABLE_NAME;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Location loc = new Location();
                loc.setLocation(cursor.getString(cursor.getColumnIndex(Location.COLUMN_ID_LOCATION)));
                loc.setNome(cursor.getString(cursor.getColumnIndex(Location.COLUMN_NOME)));
                loc.setLat(cursor.getDouble(cursor.getColumnIndex(Location.COLUMN_LAT)));
                loc.setLon(cursor.getDouble(cursor.getColumnIndex(Location.COLUMN_LON)));
                locations.add(loc);
            } while (cursor.moveToNext());
        }
        db.close();
        return locations;
    }

    public ArrayList<HashMap<String,String>> getLocationsHashMap(){
        ArrayList<HashMap<String, String>> listHashMap = new ArrayList<HashMap<String, String>>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + Location.TABLE_NAME;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("NOME", cursor.getString(cursor.getColumnIndex(Location.COLUMN_NOME)));
                map.put("LAT", cursor.getString(cursor.getColumnIndex(Location.COLUMN_LAT)));
                map.put("LON", cursor.getString(cursor.getColumnIndex(Location.COLUMN_LON)));
                listHashMap.add(map);
            } while (cursor.moveToNext());
        }
            db.close();
            return listHashMap;
    }

    public JSONObject getJsonLocations(){
        JSONArray jsonArray = new JSONArray();
        JSONObject json = new JSONObject();
        String selectQuery = "SELECT  * FROM " + Location.TABLE_NAME;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                try {
                    JSONObject json_item = new JSONObject();
                    jsonArray.put(json_item.put(Location.COLUMN_ID_LOCATION,cursor.getString(cursor.getColumnIndex(Location.COLUMN_ID_LOCATION)) ));
                    jsonArray.put(json_item.put(Location.COLUMN_NOME,cursor.getString(cursor.getColumnIndex(Location.COLUMN_NOME)) ));
                    jsonArray.put(json_item.put(Location.COLUMN_LAT,cursor.getDouble(cursor.getColumnIndex(Location.COLUMN_LAT))));
                    jsonArray.put(json_item.put(Location.COLUMN_LON,cursor.getDouble(cursor.getColumnIndex(Location.COLUMN_LON))));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        }
        db.close();
        try {
            json.put("Locations", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }
}
