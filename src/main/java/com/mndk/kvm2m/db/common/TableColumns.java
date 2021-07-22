package com.mndk.kvm2m.db.common;


import com.google.gson.JsonObject;
import com.mndk.kvm2m.core.vmap.VMapElementDataType;
import com.mndk.kvm2m.core.vmap.elem.VMapElement;
import lombok.Getter;
import lombok.ToString;

@ToString
public class TableColumns {


    public static final TableColumn ID_COLUMN = new TableColumn(
            "ID",
            "ID",
            new TableColumn.BigIntType(),
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
        this.columns = columns /*new TableColumn[columns.length + 1]*/;
        this.length = columns.length/* + 1*/;

        /*this.columns[0] = ID_COLUMN;*/
        this.primaryKeyIndex = /* 0 */ -1;

        // System.arraycopy(columns, 0, this.columns, 1, columns.length);
    }



    public TableColumn get(int index) {
        return columns[index];
    }



    public JsonObject convertElementDataToJson(VMapElement element) {
        JsonObject object = new JsonObject();
        for(int i = 0; i < length; ++i) {

            String name = columns[i].getCategoryName();
            Object value = element.getData(name);
            if(value == null) continue;

            if(value instanceof Number) {
                object.addProperty(name, (Number) value);
            }
            if(value instanceof String) {
                if (((String) value).length() == 0) { continue; }
                object.addProperty(name, (String) value);
            }
            if(value instanceof Boolean) {
                object.addProperty(name, (Boolean) value);
            }
            if(value instanceof Character) {
                object.addProperty(name, (Character) value);
            }
        }
        return object;
    }
}
