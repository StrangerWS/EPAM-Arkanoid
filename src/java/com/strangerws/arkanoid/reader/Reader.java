package com.strangerws.arkanoid.reader;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Reader {

    public int[][] readBrickArray() throws URISyntaxException, IOException {
        List<String> lines = new ArrayList<>();
        Files.lines(Paths.get(getClass().getResource("/txt/brickLayout.txt").toURI())).forEach(lines::add);

        int[][] result = new int[lines.size()][];
        for (int i = 0; i < lines.size(); i++) {
            String[] arr = lines.get(i).split(" ");
            result[i] = new int[arr.length];
            for (int j = 0; j < arr.length; j++) {
                result[i][j] = Integer.parseInt(arr[j]);
            }
        }

        return result;
    }
}
