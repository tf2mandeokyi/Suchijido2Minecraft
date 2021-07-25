package com.mndk.scjd2mc.core.db.common;


import com.google.gson.JsonObject;
import com.mndk.scjd2mc.core.scjd.type.ElementDataType;
import com.mndk.scjd2mc.core.scjd.elem.ScjdElement;
import lombok.Getter;
import lombok.ToString;

@ToString
public class TableColumns {


    private final TableColumn[] columns;
    private final @Getter int length;
    private @Getter
    ElementDataType parentType;

    public void setParentType(ElementDataType value) {
        if(parentType != null) throw new RuntimeException(new IllegalAccessException());
        this.parentType = value;
    }



    public TableColumns(TableColumn... columns) {
        this.columns = columns;
        this.length = columns.length;
    }



    public TableColumn get(int index) {
        return columns[index];
    }



    public JsonObject convertElementDataToJson(ScjdElement element) {
        JsonObject object = new JsonObject();
        for(int i = 0; i < length; ++i) {

            String name = columns[i].getName();
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
