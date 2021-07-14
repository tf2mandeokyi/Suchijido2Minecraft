package com.mndk.kvm2m.db;

import com.mndk.kvm2m.core.vmap.VMapElementType;
import com.mndk.kvm2m.core.vmap.VMapReaderResult;
import com.mndk.kvm2m.core.vmap.elem.VMapElement;
import com.mndk.kvm2m.core.vmap.elem.VMapLayer;
import com.mndk.kvm2m.db.common.TableColumns;

import java.sql.*;
import java.util.Properties;
import java.util.function.Function;

public class VMapSQLManager {

    private static final VMapSQLManager instance = new VMapSQLManager();
    public static VMapSQLManager getInstance() { return instance; }

    private static final String UFID_TABLE_NAME = "elementufid";
    private static final String GEOMETRY_TABLE_NAME = "elementgeometry";
    private static final String DATA_TABLE_NAME = "elementdata";

    private Connection connection;

    private VMapSQLManager() {}

    public ResultSet executeQuery(String sql) throws SQLException {
        Statement statement = this.connection.createStatement();
        return statement.executeQuery(sql);
    }

    public void connect(String db_url, String user, String password) throws SQLException {
        Properties properties = new Properties();
        properties.put("user", user);
        properties.put("password", password);
        properties.put("serverTimezone", "Asia/Seoul");
        this.connection = DriverManager.getConnection(db_url, properties);
        if(this.connection == null) throw new SQLException("Connection failed");
    }

    public void initializeDataTables() throws SQLException {
        this.initializeDataTables(t -> true);
    }

    public void initializeDataTables(Function<VMapElementType, Boolean> filter) throws SQLException {
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
        final String sql = columns.generateElementInsertionSQL(layer.size());
        try(PreparedStatement statement = this.connection.prepareStatement(sql)) {
            int i = 1;
            for(VMapElement element : layer) {
                for (int j = 0; j < columns.getLength(); ++j) {
                    String columnName = columns.get(j).getCategoryName();
                    statement.setObject(i + j, element.getDataByColumn(columnName));
                }
                i += columns.getLength();
            }
            statement.executeUpdate();
        }
    }

    public static String getElementDataTableName(VMapElementType type) {
        return "elementdata_" + type.name();
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
