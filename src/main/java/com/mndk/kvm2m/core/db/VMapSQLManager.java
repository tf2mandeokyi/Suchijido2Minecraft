package com.mndk.kvm2m.core.db;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.mndk.kvm2m.core.util.shape.BoundingBoxDouble;
import com.mndk.kvm2m.core.vmap.*;
import com.mndk.kvm2m.core.vmap.elem.VMapElement;
import com.mndk.kvm2m.core.vmap.elem.VMapLayer;
import com.mndk.kvm2m.core.db.common.TableColumn;
import com.mndk.kvm2m.core.db.common.TableColumns;
import com.mndk.kvm2m.core.vmap.type.VMapElementDataType;
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

    private static final JsonParser JSON_PARSER = new JsonParser();



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
                "    `" + TableColumns.ID_COLUMN.getName() + "` BIGINT(10) NOT NULL AUTO_INCREMENT PRIMARY KEY,\n" +
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



    public VMapReaderResult getVMapData(
            String map_index, GeographicProjection projection, Map<String, String> options) throws Exception {

        if(connection == null) throw new SQLException("SQL Connection not initialized");

        String idColumn = TableColumns.ID_COLUMN.getName();

        String sql =
                "SELECT " +
                        "`" + idColumn + "`, " +
                        "`geom`, " +
                        "`data_type`, " +
                        "`data` " +
                "FROM `" + MAIN_TABLE_NAME + "` WHERE `map_index` = ?;";

        VMapPayload.Geometry geometryPayload = new VMapPayload.Geometry();
        VMapPayload.Data dataPayload = new VMapPayload.Data();

        try(PreparedStatement statement = this.connection.prepareStatement(sql)) {

            statement.setString(1, map_index);
            ResultSet resultSet = statement.executeQuery();

            while(resultSet.next()) {

                long id = resultSet.getLong(idColumn);
                InputStream stream = resultSet.getBlob("geom").getBinaryStream();
                geometryPayload.put(id, VMapUtils.parseGeometryDataString(stream, projection));

                VMapElementDataType type = VMapElementDataType.fromLayerName(
                        resultSet.getString("data_type"));
                TableColumns columns = type.getColumns();

                JsonObject jsonObject = JSON_PARSER.parse(resultSet.getString("data")).getAsJsonObject();
                Object[] dataRow = new Object[columns.getLength()];
                for(int i = 0; i < columns.getLength(); ++i) {
                    TableColumn column = columns.get(i);
                    JsonElement element = jsonObject.get(column.getName());

                    if (column.getDataType() instanceof TableColumn.VarCharType) {
                        if(element == null) {
                            dataRow[i] = "";
                        }
                        else {
                            JsonPrimitive primitive = element.getAsJsonPrimitive();
                            dataRow[i] = primitive.getAsString();
                        }
                    } else if (column.getDataType() instanceof TableColumn.NumericType) {
                        if(element == null) dataRow[i] = null;
                        else {
                            JsonPrimitive primitive = element.getAsJsonPrimitive();
                            if(column.getDataType() instanceof TableColumn.BigIntType) {
                                dataRow[i] = primitive.getAsLong();
                            }
                            else {
                                dataRow[i] = primitive.getAsDouble();
                            }
                        }
                    }
                }
                dataPayload.put(id, new VMapPayload.Data.Record(type, dataRow));
            }
        }

        return VMapPayload.combineVMapPayloads(geometryPayload, dataPayload, options);
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
