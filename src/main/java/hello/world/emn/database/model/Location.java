package hello.world.emn.database.model;

public class Location {
    public static final String TABLE_NAME = "LOCATION";

    public static final String COLUMN_ID_LOCATION = "ID_LOCATION";
    public static final String COLUMN_NOME = "NOME";
    public static final String COLUMN_LAT = "LAT";
    public static final String COLUMN_LON = "LON";

    private String id_location;
    private String nome;
    private double lat;
    private double lon;

    public static final String CREATE_TABLE = "CREATE TABLE "+TABLE_NAME+"("+COLUMN_ID_LOCATION+" NVARCHAR(40) NOT NULL, "
            +COLUMN_NOME+" NVARCHAR(100),"
            +COLUMN_LAT+" DOUBLE CS_DOUBLE,"
            +COLUMN_LON+" DOUBLE CS_DOUBLE,"+
            "PRIMARY KEY("+COLUMN_ID_LOCATION+"))";

    public void ZPOI(){

    }

    public void setLocation(String location){
        this.id_location = location;
    }

    public void setNome(String nome){
        this.nome = nome;
    }

    public void setLat(double lat){
        this.lat = lat;
    }

    public void setLon(double lon){
        this.lon = lon;
    }

    public String getId_location() {
        return id_location;
    }

    public String getNome() {
        return nome;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }
}
