package com.mndk.kvm2m.db;

import com.mndk.kvm2m.core.util.shape.BoundingBoxDouble;
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
import java.util.Map;
import java.util.Properties;

public class VMapSQLManager {


    private static final VMapSQLManager instance = new VMapSQLManager();
    public static VMapSQLManager getInstance() { return instance; }



    private static final String MAIN_TABLE_NAME = "elementdata";



    private Connection connection;



    private VMapSQLManager() {}



    private ResultSet executeQuery(String sql) throws SQLException {
        if(connection == null) throw new SQLException("SQL Connection not established");
        try(Statement statement = this.connection.createStatement()) {
            return statement.executeQuery(sql);
        }
    }
    private void executeUpdate(String sql) throws SQLException {
        if(connection == null) throw new SQLException("SQL Connection not established");
        try(Statement statement = this.connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }



    public void connect(String db_url, String user, String password) throws SQLException {
        Properties properties = new Properties();
        properties.put("user", user);
        properties.put("password", password);
        properties.put("serverTimezone", "Asia/Seoul");
        properties.put("allowMultiQueries", "true");
        this.connection = DriverManager.getConnection(db_url, properties);
        if(this.connection == null) throw new SQLException("SQL Connection failed");
        if(KVectorMap2MinecraftMod.logger != null) {
            KVectorMap2MinecraftMod.logger.info("SQL Connection established");
        }
    }



    public void initializeDataTables() throws SQLException {
        this.initializeGeometryTable();
    }



    private void initializeGeometryTable() throws SQLException {
        this.executeUpdate("" +
                "CREATE TABLE IF NOT EXISTS `" + MAIN_TABLE_NAME + "` (" +
                "    `" + TableColumns.ID_COLUMN.getCategoryName() + "` BIGINT(10) NOT NULL AUTO_INCREMENT PRIMARY KEY,\n" +
                "    `map_index` VARCHAR(10) NOT NULL,\n" +
                "    `geom_type` TINYINT UNSIGNED NOT NULL,\n" +
                "    `geom` MEDIUMBLOB NOT NULL,\n" +
                "    `min_lon` NUMERIC(10, 7) NOT NULL,\n" +
                "    `max_lon` NUMERIC(10, 7) NOT NULL,\n" +
                "    `min_lat` NUMERIC(9, 7) NOT NULL,\n" +
                "    `max_lat` NUMERIC(9, 7) NOT NULL,\n" +
                "    `data_type` VARCHAR(4) NOT NULL,\n" +
                "    `data` JSON NOT NULL\n" +
                ")"
        );
    }



    public void refreshVMapData(VMapReaderResult result, String mapIndex) throws SQLException, IOException {
        try(PreparedStatement statement = this.connection.prepareStatement(
                "DELETE FROM `" + MAIN_TABLE_NAME + "` WHERE `map_index`=?")) {
            statement.setString(1, mapIndex);
            statement.executeUpdate();
        }

        for(VMapLayer layer : result.getLayers()) {
            insertVMapLayerData(layer, mapIndex);
        }
    }



    private void insertVMapLayerData(VMapLayer layer, String mapIndex) throws SQLException, IOException {
        if(layer == null) return;
        if(layer.size() == 0) return;

        if(connection == null) throw new SQLException("SQL Connection not initialized");

        VMapElementDataType type = layer.getType();
        TableColumns columns = type.getColumns();

        final String dataInsertionQuery = generateDataInsertionSql(layer.size());
        PreparedStatement statement = this.connection.prepareStatement(dataInsertionQuery);

        for(int i = 0 ; i < layer.size(); ++i) {
            VMapElement element = layer.get(i);

            Blob geometryBlob = this.connection.createBlob();
            geometryBlob.setBytes(1, VMapUtils.generateGeometryDataBytes(element));
            BoundingBoxDouble bbox = element.getBoundingBoxDouble();

            statement.setString(9 * i + 1, mapIndex);
            statement.setDouble(9 * i + 2, element.getGeometryType().ordinal());
            statement.setBlob(9 * i + 3, geometryBlob);
            statement.setDouble(9 * i + 4, bbox.xmin);
            statement.setDouble(9 * i + 5, bbox.xmax);
            statement.setDouble(9 * i + 6, bbox.zmin);
            statement.setDouble(9 * i + 7, bbox.zmax);
            statement.setString(9 * i + 8, type.getLayerNameHeader());
            statement.setString(9 * i + 9, columns.convertElementDataToJson(element).toString());
        }

        statement.executeUpdate();
    }



    private static String generateDataInsertionSql(int elementSize) {

        String queryString =
                "INSERT INTO `" + MAIN_TABLE_NAME + "` (" +
                        "map_index,geom_type,geom,min_lon,max_lon,min_lat,max_lat,data_type,data" +
                ") VALUES ";

        StringBuilder qmarkString = new StringBuilder();
        for(int i = 0; i < elementSize; ++i) {
            qmarkString.append("(?,?,?,?,?,?,?,?,?)");
            if(i != elementSize - 1) qmarkString.append(",");
        }

        return queryString + qmarkString.toString();

    }



    private VMapGeometryPayload getVMapGeometry(String mapNumber, GeographicProjection projection)
            throws Exception {

        if(connection == null) throw new SQLException("SQL Connection not initialized");

        String sql = "SELECT `UFID`, `geometry_data` FROM `" + MAIN_TABLE_NAME + "` WHERE " +
                "`UFID` REGEXP(CONCAT('^1000', ?, '[A-H]'));";
        VMapGeometryPayload result = new VMapGeometryPayload();

        try(PreparedStatement statement = this.connection.prepareStatement(sql)) {

            statement.setString(1, mapNumber);
            ResultSet resultSet = statement.executeQuery();

            while(resultSet.next()) {
                InputStream stream = resultSet.getBlob("geometry_data").getBinaryStream();
                /*result.put(resultSet.getString("UFID"), VMapUtils.parseGeometryDataString(
                        stream, projection)); // TODO finish this*/
            }
        }
        return result;
    }



    private VMapDataPayload getVMapData(String mapNumber) throws SQLException {

        if(connection == null) throw new SQLException("SQL Connection not initialized");

        VMapDataPayload result = new VMapDataPayload();

        for(VMapElementDataType type : VMapElementDataType.values()) {
            TableColumns columns = type.getColumns();

            String tableName = "a"; // getDataTableName(type);
            String sql = "SELECT * FROM `" + tableName + "` " +
                    "WHERE `UFID` LIKE CONCAT('1000', ?, '" + type.getLayerNameHeader() + "%');";

            PreparedStatement statement = this.connection.prepareStatement(sql);

            statement.setString(1, mapNumber);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Object[] dataRow = new Object[columns.getLength()];
                for (int i = 0; i < columns.getLength(); ++i) {
                    TableColumn column = columns.get(i);
                    if (column.getDataType() instanceof TableColumn.VarCharType) {
                        dataRow[i] = resultSet.getString(column.getCategoryName());
                    } else if (column.getDataType() instanceof TableColumn.BigIntType) {
                        dataRow[i] = resultSet.getLong(column.getCategoryName());
                    } else if (column.getDataType() instanceof TableColumn.NumericType) {
                        dataRow[i] = resultSet.getDouble(column.getCategoryName());
                    }
                }
                /*result.put(ufid, new VMapDataPayload.Record(type, dataRow)); // TODO finish this*/
            }
            statement.close();

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
