package com.progressoft.tools;

import java.nio.file.Path;

public class NormalizerClass implements Normalizer {

    @Override
    public ScoringSummary zscore(Path csvPath, Path destPath, String colToStandardize) {
        return new ZscoreNormalization(csvPath, destPath, colToStandardize);
    }

    @Override
    public ScoringSummary minMaxScaling(Path csvPath, Path destPath, String colToNormalize) {
        return new MinmaxNormalization(csvPath, destPath, colToNormalize);
    }
}
