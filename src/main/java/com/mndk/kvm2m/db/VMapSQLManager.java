package com.mndk.kvm2m.db;

import com.mndk.kvm2m.core.util.math.Vector2DH;
import com.mndk.kvm2m.core.vmap.VMapElementType;
import com.mndk.kvm2m.core.vmap.VMapReaderResult;
import com.mndk.kvm2m.core.vmap.elem.VMapElement;
import com.mndk.kvm2m.core.vmap.elem.VMapLayer;
import com.mndk.kvm2m.core.vmap.elem.line.VMapPolyline;
import com.mndk.kvm2m.core.vmap.elem.point.VMapPoint;
import com.mndk.kvm2m.core.vmap.elem.poly.VMapPolygon;
import com.mndk.kvm2m.db.common.TableColumn;
import com.mndk.kvm2m.db.common.TableColumns;

import java.sql.*;
import java.text.DecimalFormat;
import java.util.Properties;
import java.util.function.Function;

public class VMapSQLManager {



    private static final DecimalFormat decimalFormat = new DecimalFormat("#.0000000");



    private static final VMapSQLManager instance = new VMapSQLManager();
    public static VMapSQLManager getInstance() { return instance; }



    private static final String GEOMETRY_TABLE_NAME = "elementgeometry";
    private static final String DATA_TABLE_NAME = "elementdata";



    private Connection connection;



    private VMapSQLManager() {}



    private ResultSet executeQuery(String sql) throws SQLException {
        try(Statement statement = this.connection.createStatement()) {
            return statement.executeQuery(sql);
        }
    }
    private void executeUpdate(String sql) throws SQLException {
        try(Statement statement = this.connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }



    public void connect(String db_url, String user, String password) throws SQLException {
        Properties properties = new Properties();
        properties.put("user", user);
        properties.put("password", password);
        properties.put("serverTimezone", "Asia/Seoul");
        this.connection = DriverManager.getConnection(db_url, properties);
        if(this.connection == null) throw new SQLException("Connection failed");
    }



    public void initializeTables() throws SQLException {
        this.initializeTables(t -> true);
        this.initializeGeometryTable();
    }



    public void initializeGeometryTable() throws SQLException {
        this.executeUpdate("" +
                "CREATE TABLE IF NOT EXISTS `" + GEOMETRY_TABLE_NAME + "` (" +
                        TableColumns.UFID_COLUMN.toTableCreationSql() + "," +
                        "`geometry_data` MEDIUMTEXT NOT NULL," +
                        "PRIMARY KEY (`" + TableColumns.UFID_COLUMN.getCategoryName() + "`)" +
                ")"
        );
    }



    public void initializeTables(Function<VMapElementType, Boolean> filter) throws SQLException {
        for(VMapElementType type : VMapElementType.values()) {
            if(!filter.apply(type)) continue;
            String query = type.getColumns().generateTableCreationSQL();
            System.out.println(query);
            try(Statement statement = connection.createStatement()){
                statement.executeUpdate(query);
            }
        }
    }



    public void insertVMapData(VMapReaderResult result) throws SQLException {
        for(VMapLayer layer : result.getElementLayers()) {
            insertVMapLayerData(layer);
        }
    }



    public void insertVMapLayerData(VMapLayer layer) throws SQLException {
        if(layer == null) return;

        VMapElementType type = layer.getType();
        TableColumns columns = type.getColumns();

        final String dataSql = columns.generateElementDataInsertionSQL(layer.size());
        final String geometrySql = generateGeometryInsertionSql(layer.size());

        try(PreparedStatement dataStatement = this.connection.prepareStatement(dataSql);
            PreparedStatement geometryStatement = this.connection.prepareStatement(geometrySql)) {

            int dataStatementIndex = 1;
            for(int i = 0 ; i < layer.size(); ++i) {
                VMapElement element = layer.get(i);

                geometryStatement.setObject(2 * i + 1, element.getDataByColumn("UFID"));
                geometryStatement.setString(2 * i + 2, getGeometryDataString(element));

                for (int j = 0; j < columns.getLength(); ++j) {
                    TableColumn column = columns.get(j);
                    String columnName = column.getCategoryName();
                    Object o = element.getDataByColumn(columnName);

                    if(column.getDataType() instanceof TableColumn.VarCharType) {
                        TableColumn.VarCharType varCharType = (TableColumn.VarCharType) column.getDataType();
                        if(o instanceof String && varCharType.getLength() < ((String) o).length()) {
                            o = ((String) o).substring(0, varCharType.getLength());
                        }
                    }

                    dataStatement.setObject(dataStatementIndex + j, o);
                }
                dataStatementIndex += columns.getLength();
            }

            dataStatement.executeUpdate();
            geometryStatement.executeUpdate();
        }
    }



    private static String getGeometryDataString(VMapElement element) {
        if(element instanceof VMapPoint) {
            Vector2DH v = ((VMapPoint) element).getPosition();
            return "POINT(" + decimalFormat.format(v.x) + " " + decimalFormat.format(v.z) + ")";
        }
        else {
            String geometryData;
            System.out.println(element.getClass().toString());
            if (element instanceof VMapPolygon) {
                geometryData = "POLYGON(";
            }
            else if(element instanceof VMapPolyline) {
                geometryData = "POLYLINE(";
            }
            else {
                return "NULL()";
            }
            Vector2DH[][] lines = ((VMapPolyline) element).getVertexList();
            for (Vector2DH[] line : lines) {
                String temp = "";
                for (Vector2DH point : line) {
                    temp += decimalFormat.format(point.x) + " " + decimalFormat.format(point.z) + ",";
                }
                geometryData += "(" + temp.substring(0, temp.length() - 1) + ")";
            }
            geometryData += ")";
            return geometryData;
        }
    }



    private static String generateGeometryInsertionSql(int elementSize) {
        String result = "INSERT INTO `" + GEOMETRY_TABLE_NAME + "` VALUES";

        StringBuilder qmarkString = new StringBuilder();
        for(int i = 0; i < elementSize; ++i) {
            qmarkString.append("(?,?),");
        }
        return result + qmarkString.substring(0, qmarkString.length() - 1) +
            "ON DUPLICATE KEY UPDATE `geometry_data`=values(`geometry_data`);";
    }



    public static String getElementDataTableName(VMapElementType type) {
        return DATA_TABLE_NAME + "_" + type.name();
    }



    public void close() throws SQLException {
        this.connection.close();
    }



    static {
        try {
            DriverManager.registerDriver(new com.mysql.cj.jdbc.Driver());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
