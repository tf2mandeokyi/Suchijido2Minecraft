package com.mndk.scjdmc.util;

import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;

public class ProgressBarUtils {

    public static ProgressBar createProgressBar(String title, int size) {
        return new ProgressBarBuilder()
                .setTaskName(title)
                .setInitialMax(size)
                .setUpdateIntervalMillis(100)
                .setMaxRenderedLength(150)
                .continuousUpdate()
                .clearDisplayOnFinish()
                .setStyle(ProgressBarStyle.ASCII)
                .build();
    }

}
