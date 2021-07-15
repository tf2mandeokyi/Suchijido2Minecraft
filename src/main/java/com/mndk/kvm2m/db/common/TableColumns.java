package com.mndk.kvm2m.db.common;


import com.mndk.kvm2m.core.vmap.VMapElementDataType;
import com.mndk.kvm2m.db.VMapSQLManager;
import lombok.Getter;
import lombok.ToString;

@ToString
public class TableColumns {


    public static final TableColumn UFID_COLUMN = new TableColumn(
            "UFID",
            "UFID",
            new TableColumn.VarCharType(34),
            TableColumn.NOT_NULL
    );


    private final TableColumn[] columns;
    private final @Getter int primaryKeyIndex;
    private final @Getter int length;
    private @Getter VMapElementDataType parentType;

    public void setParentType(VMapElementDataType value) {
        if(parentType != null) throw new RuntimeException(new IllegalAccessException());
        this.parentType = value;
    }



    public TableColumns(TableColumn... columns) {
        this.columns = new TableColumn[columns.length + 1];
        this.length = columns.length + 1;

        this.columns[0] = UFID_COLUMN;
        this.primaryKeyIndex = 0;

        System.arraycopy(columns, 0, this.columns, 1, columns.length);
    }



    public TableColumn get(int index) {
        return columns[index];
    }



    public boolean hasTable() {
        return /* this.length > 1 */ true;
    }



    public String generateTableCreationSQL() {
        if(!this.hasTable()) return null;

        StringBuilder result = new StringBuilder(
                "CREATE TABLE IF NOT EXISTS `" + VMapSQLManager.getElementDataTableName(parentType) + "` (");

        for (int i = 0; i < columns.length; ++i) {
            TableColumn column = columns[i];
            result.append(column.toTableCreationSql());
            if(i != columns.length - 1) {
                result.append(",");
            }
        }

        if(primaryKeyIndex != -1) {
            result.append(", PRIMARY KEY (`").append(columns[primaryKeyIndex].getCategoryName()).append("`)");
        }
        return result + ") CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;";
    }



    public String generateElementDataInsertionSQL(int elementCount) {
        if(!this.hasTable()) return null;

        String columnString = "", qmarkString = "", dataUpdateString = "";

        for (TableColumn column : columns) {
            String name = column.getCategoryName();
            columnString += "`" + name + "`,";
            qmarkString += "?,";
            dataUpdateString += "`" + name + "`=values(`" + name + "`),";
        }
        columnString = columnString.substring(0, columnString.length() - 1);
        qmarkString = "(" + qmarkString.substring(0, qmarkString.length() - 1) + ")";
        dataUpdateString = dataUpdateString.substring(0, dataUpdateString.length() - 1);

        String qmarkStrings = "";
        for(int i = 0; i < elementCount; ++i) {
            qmarkStrings += qmarkString + ",";
        }
        qmarkStrings = qmarkStrings.substring(0, qmarkStrings.length() - 1);

        return "INSERT INTO `" + VMapSQLManager.getElementDataTableName(parentType) + "`" +
                " (" + columnString + ") VALUES " + qmarkStrings +
                (primaryKeyIndex != -1 ? " ON DUPLICATE KEY UPDATE " + dataUpdateString : "") + ";";
    }
}
