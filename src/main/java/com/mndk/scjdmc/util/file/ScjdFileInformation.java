package com.mndk.scjdmc.util.file;

import com.mndk.scjdmc.util.ScjdMapIndexUtils;
import com.mndk.scjdmc.util.ScjdParsedType;
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
        switch(parsedType) {
            case INDEX: return ScjdMapIndexUtils.getMapIndexFromFileName(file.getName());
            case TILE: return file.getParentFile().getName() + "," + file.getName();
            default: return file.getName();
        }
    }

    public String getNameForFile() {
        return parsedType + "_" + this.getNameOrIndex();
    }
}
