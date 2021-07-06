package com.mndk.kvm2m.db.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@ToString
public class TableColumn {

    public static final int PRIMARY_KEY = 1;

    public static final int NOT_NULL = 2;

    private final @Getter String categoryId;
    private final @Getter String categoryName;
    private final @Getter ColumnType dataType;
    private final @Getter int flag;

    public TableColumn(String categoryId, String categoryName, ColumnType dataType) {
        this(categoryId, categoryName, dataType, 0);
    }



    public static abstract class ColumnType {
        @Override public abstract String toString();
    }

    @RequiredArgsConstructor
    public static class VarCharType extends ColumnType {
        private final @Getter int length;
        @Override public String toString() {
            return "VARCHAR(" + length * 3 + ")";
            // There are some data exceeding their length limit, so I'm multiplying this by 3 for just in case
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

}
