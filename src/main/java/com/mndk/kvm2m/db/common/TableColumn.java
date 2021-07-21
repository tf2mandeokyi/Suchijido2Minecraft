package com.mndk.kvm2m.db.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@ToString
@Getter
public class TableColumn {

    public static final int NOT_NULL = 1;

    private final String categoryId;
    private final String categoryName;
    private final ColumnType dataType;
    private final int flag;

    public TableColumn(String categoryId, String categoryName, ColumnType dataType) {
        this(categoryId, categoryName, dataType, 0);
    }


    public String toTableCreationSql() {
        String result = "`" + this.categoryName + "` " + this.dataType;
        if ((this.flag & TableColumn.NOT_NULL) == TableColumn.NOT_NULL) {
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
