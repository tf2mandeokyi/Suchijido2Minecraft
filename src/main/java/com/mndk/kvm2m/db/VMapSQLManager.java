package com.mndk.kvm2m.db;

import com.mndk.kvm2m.core.vmap.*;
import com.mndk.kvm2m.core.vmap.elem.VMapElement;
import com.mndk.kvm2m.core.vmap.elem.VMapLayer;
import com.mndk.kvm2m.db.common.TableColumn;
import com.mndk.kvm2m.db.common.TableColumns;
import com.mndk.kvm2m.mod.KVectorMap2MinecraftMod;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

public class VMapSQLManager {


    private static final VMapSQLManager instance = new VMapSQLManager();
    public static VMapSQLManager getInstance() { return instance; }



    private static final String GEOMETRY_TABLE_NAME = "elementgeometry";
    private static final String DATA_TABLE_NAME = "elementdata";



    private Connection connection;



    private VMapSQLManager() {}



    private ResultSet executeQuery(String sql) throws SQLException {
        if(connection == null) throw new SQLException("SQL Connection not initialized");
        try(Statement statement = this.connection.createStatement()) {
            return statement.executeQuery(sql);
        }
    }
    private void executeUpdate(String sql) throws SQLException {
        if(connection == null) throw new SQLException("SQL Connection not initialized");
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
        if(this.connection == null) throw new SQLException("SQL Connection failed");
        if(KVectorMap2MinecraftMod.logger != null) {
            KVectorMap2MinecraftMod.logger.info("SQL Connection established");
        }
    }



    public void initializeDataTables() throws SQLException {
        this.initializeDataTables(t -> true);
        this.initializeGeometryTable();
    }



    public void initializeDataTables(Function<VMapElementDataType, Boolean> filter) throws SQLException {
        for(VMapElementDataType type : VMapElementDataType.values()) {
            if(!filter.apply(type)) continue;
            String query = type.getColumns().generateTableCreationSQL();
            if(query == null) continue;
            try(Statement statement = connection.createStatement()){
                statement.executeUpdate(query);
            }
        }
    }



    public void initializeGeometryTable() throws SQLException {
        this.executeUpdate("" +
                "CREATE TABLE IF NOT EXISTS `" + GEOMETRY_TABLE_NAME + "` (" +
                        TableColumns.UFID_COLUMN.toTableCreationSql() + "," +
                        "`geometry_data` MEDIUMBLOB NOT NULL," +
                        "PRIMARY KEY (`" + TableColumns.UFID_COLUMN.getCategoryName() + "`)" +
                ")"
        );
    }



    public void insertVMapData(VMapReaderResult result) throws SQLException, IOException {
        for(VMapLayer layer : result.getElementLayers()) {
            insertVMapLayerData(layer);
        }
    }



    public void insertVMapLayerData(VMapLayer layer) throws SQLException, IOException {
        if(layer == null) return;
        if(layer.size() == 0) return;

        if(connection == null) throw new SQLException("SQL Connection not initialized");

        VMapElementDataType type = layer.getType();
        TableColumns columns = type.getColumns();

        final String dataSql = columns.generateElementDataInsertionSQL(layer.size());
        final String geometrySql = generateGeometryInsertionSql(layer.size());

        PreparedStatement dataStatement = dataSql == null ? null : this.connection.prepareStatement(dataSql);
        PreparedStatement geometryStatement = this.connection.prepareStatement(geometrySql);

        int dataStatementIndex = 1;
        for(int i = 0 ; i < layer.size(); ++i) {
            VMapElement element = layer.get(i);

            Blob geometryBlob = this.connection.createBlob();
            geometryBlob.setBytes(1, VMapUtils.generateGeometryDataBytes(element));
            geometryStatement.setObject(2 * i + 1, element.getDataByColumn("UFID"));
            geometryStatement.setBlob(2 * i + 2, geometryBlob);

            if(dataStatement != null) {
                for (int j = 0; j < columns.getLength(); ++j) {
                    TableColumn column = columns.get(j);
                    String columnName = column.getCategoryName();
                    Object o = element.getDataByColumn(columnName);

                    if (column.getDataType() instanceof TableColumn.VarCharType) {
                        TableColumn.VarCharType varCharType = (TableColumn.VarCharType) column.getDataType();
                        if (o instanceof String && varCharType.getLength() < ((String) o).length()) {
                            o = ((String) o).substring(0, varCharType.getLength());
                        }
                    }

                    dataStatement.setObject(dataStatementIndex + j, o);
                }
                dataStatementIndex += columns.getLength();
            }
        }

        if(dataStatement != null) dataStatement.executeUpdate();
        geometryStatement.executeUpdate();
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



    private Map<String, VMapGeometryPayload<?>> getVMapGeometry(String mapNumber, GeographicProjection projection)
            throws Exception {

        if(connection == null) throw new SQLException("SQL Connection not initialized");

        String sql = "SELECT * FROM `" + GEOMETRY_TABLE_NAME + "` WHERE `UFID` REGEXP(CONCAT('^1000', ?, '[A-Z]'));";
        Map<String, VMapGeometryPayload<?>> result = new HashMap<>();

        try(PreparedStatement statement = this.connection.prepareStatement(sql)) {

            statement.setString(1, mapNumber);
            ResultSet resultSet = statement.executeQuery();

            while(resultSet.next()) {
                InputStream stream = resultSet.getBlob("geometry_data").getBinaryStream();
                result.put(resultSet.getString("UFID"), VMapUtils.parseGeometryDataString(
                        stream, projection));
            }
        }
        return result;
    }



    private Map<String, VMapDataPayload> getVMapData(String mapNumber) throws SQLException {

        if(connection == null) throw new SQLException("SQL Connection not initialized");

        Map<String, VMapDataPayload> result = new HashMap<>();

        for(VMapElementDataType type : VMapElementDataType.values()) {
            TableColumns columns = type.getColumns();

            String tableName = getElementDataTableName(type);
            String sql = "SELECT * FROM `" + tableName + "` WHERE `UFID` REGEXP(CONCAT('^1000', ?, '[A-Z]'));";

            if(columns.hasTable()) {
                PreparedStatement statement = this.connection.prepareStatement(sql);

                statement.setString(1, mapNumber);
                ResultSet resultSet = statement.executeQuery();

                while (resultSet.next()) {
                    String ufid = resultSet.getString("UFID");
                    Object[] dataRow = new Object[columns.getLength()];
                    for (int i = 0; i < columns.getLength(); ++i) {
                        TableColumn column = columns.get(i);
                        if (column.getDataType() instanceof TableColumn.VarCharType) {
                            dataRow[i] = resultSet.getString(column.getCategoryName());
                        } else if (column.getDataType() instanceof TableColumn.NumericType) {
                            dataRow[i] = resultSet.getDouble(column.getCategoryName());
                        }
                    }
                    result.put(ufid, new VMapDataPayload(type, dataRow));
                }
                statement.close();
            }

        }

        return result;
    }



    public VMapReaderResult getVMapResult(
            String mapNumber, GeographicProjection projection, Map<String, String> options) throws Exception {

        synchronized (this) {
            if (connection == null) throw new SQLException("SQL Connection not initialized");

            return VMapUtils.combineVMapPayloads(
                    this.getVMapGeometry(mapNumber, projection),
                    this.getVMapData(mapNumber),
                    options
            );
        }
    }



    public static String getElementDataTableName(VMapElementDataType type) {
        return DATA_TABLE_NAME + "_" + type.name();
    }



    public void close() throws SQLException {
        this.connection.close();
    }



    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
