package com.mndk.kvm2m.core.db.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@ToString
@Getter
public class TableColumn {

    private final String id;
    private final String name;
    private final ColumnType dataType;
    private final boolean notNull;

    public TableColumn(String id, String name, ColumnType dataType) {
        this(id, name, dataType, false);
    }


    public String toTableCreationSql() {
        String result = "`" + this.name + "` " + this.dataType;
        if (notNull) {
            result += " NOT NULL";
        }
        return result;
    }



    public static abstract class ColumnType {
        @Override public abstract String toString();
    }

    @RequiredArgsConstructor
    public static class VarCharType extends ColumnType {
        private final @Getter int length;
        @Override public String toString() {
            return "VARCHAR(" + length + ")";
        }
    }

    @RequiredArgsConstructor
    public static class NumericType extends ColumnType {
        private final @Getter int length;
        private final @Getter int decimal;
        public NumericType(int length) {
            this(length, 0);
        }
        @Override public String toString() {
            if(decimal > 0) return "NUMERIC(" + length + "," + decimal + ")";
            else return "NUMERIC(" + length + ")";
        }
    }

    public static class BigIntType extends NumericType {
        public BigIntType() {
            super(0, 0);
        }
        @Override public String toString() {
            return "BIGINT";
        }
    }

}
