package com.mndk.kvm2m.db.common;


import com.mndk.kvm2m.core.vmap.VMapElementType;
import com.mndk.kvm2m.db.VMapSQLManager;
import lombok.Getter;
import lombok.ToString;

@ToString
public class TableColumns {


    private static final boolean CHECK_PRIMARY_KEY = true;


    private final TableColumn[] columns;
    private final @Getter int primaryKeyIndex;
    private final @Getter int length;
    private @Getter VMapElementType parentType;
    public void setParentType(VMapElementType value) {
        if(parentType != null) throw new RuntimeException(new IllegalAccessException());
        this.parentType = value;
    }



    public TableColumns(TableColumn... columns) {
        int primaryKeyIndex = -1;

        this.columns = new TableColumn[columns.length + 1];
        this.length = columns.length + 1;

        this.columns[0] = new TableColumn("UFID", "UFID", new TableColumn.VarCharType(34),
                TableColumn.PRIMARY_KEY | TableColumn.NOT_NULL);

        for (int i = 0; i < columns.length; ++i) {
            this.columns[i + 1] = columns[i];

            if(CHECK_PRIMARY_KEY) {
                if ((columns[i].getFlag() & TableColumn.PRIMARY_KEY) == TableColumn.PRIMARY_KEY) {
                    if (primaryKeyIndex != -1) throw new RuntimeException("Duplicate Primary Key");
                    primaryKeyIndex = i;
                }
            }
        }

        this.primaryKeyIndex = primaryKeyIndex;
    }



    public TableColumn get(int index) {
        return columns[index];
    }



    public String generateTableCreationSQL() {
        String result = "CREATE TABLE IF NOT EXISTS `" + VMapSQLManager.getElementDataTableName(parentType) + "` (";
        for (int i = 0; i < columns.length; ++i) {
            TableColumn column = columns[i];
            result += "`" + column.getCategoryName() + "` " + column.getDataType();
            if ((column.getFlag() & TableColumn.NOT_NULL) == TableColumn.NOT_NULL) {
                result += " NOT NULL";
            }
            if(i != columns.length - 1 || primaryKeyIndex != -1) {
                result += ",";
            }
        }
        if(primaryKeyIndex != -1) {
            result += " PRIMARY KEY (`" + columns[primaryKeyIndex].getCategoryName() + "`)";
        }
        return result + ") CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;";
    }



    public String generateElementInsertionSQL(int elementCount) {
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
