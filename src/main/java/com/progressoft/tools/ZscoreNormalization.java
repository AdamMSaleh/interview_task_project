package com.progressoft.tools;

import java.io.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;

public class ZscoreNormalization implements ScoringSummary {

    private BigDecimal mean, sd, var, median, min, max;

    private ArrayList<BigDecimal> n = new ArrayList<>();

    public ZscoreNormalization(Path csvPath, Path destPath, String colToStandardize) {
        if (!Files.exists(csvPath)) {
            throw new IllegalArgumentException("source file not found");
        }

        BufferedReader br = getBF(csvPath);

        String[] columns = null;
        try {
            columns = br.readLine().split(",");
        } catch (IOException e) {
            e.printStackTrace();
        }

        int cIndex = -1;
        for (int i = 0; i < columns.length; i++) {
            if (columns[i].equals(colToStandardize)) {
                cIndex = i;
                break;
            }
        }

        if (cIndex == -1) {
            String format = String.format("column %s not found", colToStandardize);
            throw new IllegalArgumentException(format);
        }

        try {
            String line;
            while ((line = br.readLine()) != null) {
                String[] spLine = line.split(",");
                n.add(new BigDecimal(spLine[cIndex]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        //find mean
        mean = new BigDecimal(0);
        for (int i = 0; i < n.size(); i++) {
            mean = mean.add(n.get(i));
        }
        mean = mean.divide(new BigDecimal(n.size()), RoundingMode.HALF_EVEN);
        mean = mean.setScale(2, RoundingMode.HALF_EVEN);

        //find sd and var
        BigDecimal sum = new BigDecimal(0);
        for (int i = 0; i < n.size(); i++) {
            BigDecimal num = n.get(i);
            num = num.subtract(mean);
            num = num.pow(2);
            sum = sum.add(num);
        }

        var = sum.divide(new BigDecimal(n.size()), RoundingMode.HALF_EVEN);
        var = var.setScale(0, RoundingMode.HALF_EVEN);
        var = var.setScale(2, RoundingMode.HALF_EVEN);
        sd = BigDecimal.valueOf(Math.sqrt(var.doubleValue()));
        sd = sd.setScale(2, RoundingMode.HALF_EVEN);

        //find median, min, max
        Collections.sort(n, (o1, o2) -> o1.compareTo(o2));
        median = n.get(n.size() / 2).setScale(2, RoundingMode.HALF_EVEN);
        min = n.get(0).setScale(2, RoundingMode.HALF_EVEN);
        max = n.get(n.size() - 1).setScale(2, RoundingMode.HALF_EVEN);


        //write results to output file
        br = getBF(csvPath);
        PrintWriter pw = getPR(destPath);

        String nColumns = "";
        for (int i = 0; i < columns.length; i++) {
            if (i == cIndex) {
                nColumns += String.format("%s,%s_z,", columns[i], columns[i]);
            } else {
                nColumns += columns[i] + ",";
            }
        }
        pw.println(nColumns.substring(0, nColumns.length() - 1));

        try {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] spRow = line.split(",");
                String nRow = "";
                for (int i = 0; i < spRow.length; i++) {
                    if (i == cIndex) {
                        BigDecimal num = new BigDecimal(spRow[i]);
                        num = num.subtract(mean);
                        num = num.divide(sd, 2, RoundingMode.HALF_EVEN);
                        num = num.setScale(2, RoundingMode.HALF_EVEN);
                        nRow += String.format("%s,%s,", spRow[i], num);
                    } else {
                        nRow += spRow[i] + ",";
                    }
                }
                pw.println(nRow.substring(0, nRow.length() - 1));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            br.close();
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public BufferedReader getBF(Path path) {
        BufferedReader br = null;
        try {
            FileInputStream fis = new FileInputStream(path.toFile());
            InputStreamReader isr = new InputStreamReader(fis);
            br = new BufferedReader(isr);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return br;
    }

    public PrintWriter getPR(Path path) {
        PrintWriter pw = null;
        try {
            FileWriter fw = new FileWriter(path.toFile());
            pw = new PrintWriter(fw);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pw;
    }

    @Override
    public BigDecimal mean() {
        return mean;
    }

    @Override
    public BigDecimal standardDeviation() {
        return sd;
    }

    @Override
    public BigDecimal variance() {
        return var;
    }

    @Override
    public BigDecimal median() {
        return median;
    }

    @Override
    public BigDecimal min() {
        return min;
    }

    @Override
    public BigDecimal max() {
        return max;
    }
}