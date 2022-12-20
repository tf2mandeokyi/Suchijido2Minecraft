package com.mndk.scjdmc.util;

import lombok.Getter;

import java.io.File;

@Getter
public class ScjdFileInformation {
    private final File file;
    private final ScjdParsedType parsedType;

    public ScjdFileInformation(File file, ScjdParsedType parsedType) {
        this.file = file;
        this.parsedType = parsedType;
    }

    public String getNameOrIndex() {
        return this.parsedType == ScjdParsedType.INDEX ?
                ScjdMapIndexUtils.getMapIndexFromFileName(file.getName()) :
                file.getName();
    }

    public String getNameForFile() {
        return parsedType + "_" + this.getNameOrIndex();
    }
}
