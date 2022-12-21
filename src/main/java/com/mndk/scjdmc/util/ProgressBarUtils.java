package com.mndk.scjdmc.util;

import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;

public class ProgressBarUtils {

    public static ProgressBar createProgressBar(String title, int size) {
        return new ProgressBarBuilder()
                .setTaskName(title)
                .setInitialMax(size)
                .setUpdateIntervalMillis(100)
                .setMaxRenderedLength(150)
                .continuousUpdate()
                .clearDisplayOnFinish()
                .build();
    }

}
