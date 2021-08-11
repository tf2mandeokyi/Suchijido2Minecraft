package com.mndk.scjd2mc.core.db;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.mndk.scjd2mc.core.db.common.TableColumn;
import com.mndk.scjd2mc.core.db.common.TableColumns;
import com.mndk.scjd2mc.core.scjd.SuchijidoData;
import com.mndk.scjd2mc.core.scjd.SuchijidoUtils;
import com.mndk.scjd2mc.core.scjd.elem.ScjdElement;
import com.mndk.scjd2mc.core.scjd.elem.ScjdLayer;
import com.mndk.scjd2mc.core.scjd.geometry.GeometryShape;
import com.mndk.scjd2mc.core.scjd.type.ElementDataType;
import com.mndk.scjd2mc.core.util.shape.BoundingBoxDouble;
import com.mndk.scjd2mc.mod.Suchijido2MinecraftMod;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Map;
import java.util.Properties;

public class MySQLManager {


    private static final String MAIN_TABLE_NAME = "elementdata";

    public static final TableColumn ID_COLUMN = new TableColumn(
            "ID",
            "ID",
            new TableColumn.BigIntType(),
            true
    );



    private static Connection connection;



    private MySQLManager() {}



    public static void connect(String db_url, String user, String password) throws SQLException {
        Properties properties = new Properties();
        properties.put("user", user);
        properties.put("password", password);
        properties.put("serverTimezone", "Asia/Seoul");
        properties.put("allowMultiQueries", "true");
        connection = DriverManager.getConnection(db_url, properties);
        if(connection == null) throw new SQLException("SQL Connection failed");
        if(Suchijido2MinecraftMod.logger != null) {
            Suchijido2MinecraftMod.logger.info("SQL Connection established");
        }
    }



    private static ResultSet executeQuery(String sql) throws SQLException {
        if(connection == null) throw new SQLException("SQL Connection not established");
        try(Statement statement = connection.createStatement()) {
            return statement.executeQuery(sql);
        }
    }
    private static void executeUpdate(String sql) throws SQLException {
        if(connection == null) throw new SQLException("SQL Connection not established");
        try(Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }



    public static void initializeDataTables() throws SQLException {
        initializeGeometryTable();
    }



    private static void initializeGeometryTable() throws SQLException {
        executeUpdate("" +
                "CREATE TABLE IF NOT EXISTS `" + MAIN_TABLE_NAME + "` (" +
                "    `" + ID_COLUMN.getName() + "` BIGINT(10) NOT NULL AUTO_INCREMENT PRIMARY KEY,\n" +
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



    public static void refreshVMapData(SuchijidoData result, String mapIndex) throws SQLException, IOException {
        try(PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM `" + MAIN_TABLE_NAME + "` WHERE `map_index`=?")) {
            statement.setString(1, mapIndex);
            statement.executeUpdate();
        }

        for(ScjdLayer layer : result.getLayers()) {
            insertVMapLayerData(layer, mapIndex);
        }
    }



    private static void insertVMapLayerData(ScjdLayer layer, String mapIndex) throws SQLException, IOException {
        if(layer == null) return;
        if(layer.size() == 0) return;

        if(connection == null) throw new SQLException("SQL Connection not initialized");

        ElementDataType type = layer.getType();
        TableColumns columns = type.getColumns();

        final String dataInsertionQuery = generateDataInsertionSql(layer.size());
        PreparedStatement statement = connection.prepareStatement(dataInsertionQuery);

        for(int i = 0 ; i < layer.size(); ++i) {
            ScjdElement<?> element = layer.get(i);
            GeometryShape<?> geometryShape = element.getShape();

            statement.setString(9 * i + 1, mapIndex);
            statement.setDouble(9 * i + 2, geometryShape.getType().ordinal());

            Blob geometryBlob = connection.createBlob();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            geometryShape.toGeometryBytes(dos);
            geometryBlob.setBytes(1, bos.toByteArray());
            statement.setBlob(9 * i + 3, geometryBlob);

            BoundingBoxDouble bbox = geometryShape.getBoundingBox();
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

        return queryString + qmarkString;

    }



    public static SuchijidoData getVMapData(
            String map_index, GeographicProjection targetProjection, Map<String, String> options) throws Exception {

        if(connection == null) throw new SQLException("SQL Connection not initialized");

        String idColumn = ID_COLUMN.getName();

        String query = "SELECT `" + idColumn + "`, `geom`, `data_type`, `data` FROM `" + MAIN_TABLE_NAME + "` " +
                "WHERE `map_index` = ?;";

        try(PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, map_index);
            ResultSet resultSet = statement.executeQuery();
            return resultSetToScjdData(resultSet, targetProjection, options);
        }
    }



    public static SuchijidoData getVMapData(
            BoundingBoxDouble bbox, GeographicProjection targetProjection, Map<String, String> options
    ) throws Exception {

        String idColumn = ID_COLUMN.getName();

        String query = "SELECT `" + idColumn + "`, `geom`, `data_type`, `data` FROM `" + MAIN_TABLE_NAME + "` " +
                "WHERE `max_lon` >= ? AND ? >= `min_lon` AND `max_lat` >= ? AND ? >= `min_lat`;";

        try(PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setDouble(1, bbox.xmin);
            statement.setDouble(2, bbox.xmax);
            statement.setDouble(3, bbox.zmin);
            statement.setDouble(4, bbox.zmax);
            ResultSet resultSet = statement.executeQuery();
            return resultSetToScjdData(resultSet, targetProjection, options);
        }
    }



    public static SuchijidoData getVMapData(
            int x, int y, double zFactor, GeographicProjection targetProjection, Map<String, String> options
    ) throws Exception {
        return getVMapData(
                new BoundingBoxDouble(x / zFactor, y / zFactor, (x + 1) / zFactor, (y + 1) / zFactor),
                targetProjection, options
        );
    }



    public static SuchijidoData getVMapData(
            int x, int y, GeographicProjection targetProjection, Map<String, String> options
    ) throws Exception {
        return getVMapData(
                new BoundingBoxDouble(x / 64., y / 64., (x + 1) / 64., (y + 1) / 64.),
                targetProjection, options
        );
    }



    private static SuchijidoData resultSetToScjdData(
            ResultSet resultSet, GeographicProjection targetProjection, Map<String, String> options)
            throws Exception {

        SuchijidoData result = new SuchijidoData();

        while(resultSet.next()) {

            ElementDataType type = ElementDataType.fromLayerName(
                    resultSet.getString("data_type"));
            TableColumns columns = type.getColumns();
            ScjdLayer layer = result.getLayer(type);

            long id = resultSet.getLong(ID_COLUMN.getName());
            DataInputStream geometryStream = new DataInputStream(resultSet.getBlob("geom").getBinaryStream());
            GeometryShape<?> geometry = GeometryShape.fromGeometryBytes(geometryStream, targetProjection);

            JsonObject jsonObject = JsonParser.parseString(resultSet.getString("data")).getAsJsonObject();
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

            layer.addAll(SuchijidoUtils.combineGeometryAndData(layer, geometry, type, dataRow, Long.toString(id), options));
        }

        return result;
    }



    public static void close() throws SQLException {
        connection.close();
    }



    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
