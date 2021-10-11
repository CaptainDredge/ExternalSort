package com.captaindredge.ExternalSort;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;

public class ExternalSort {
    public static void main(String[] args) throws Exception {
        String inputFile = "Resources/BigFile.txt";
        String tempFolder = "tmp/";
        String outputFile = "SortedBigFile.txt";
        final long BLOCKSIZE = estimateAvailableMemory() / 1000;
        System.out.println("BlockSize (KB): " + BLOCKSIZE);

        URL res = ExternalSort.class.getResource(inputFile);
        Path inputFilePath = Paths.get(res.toURI());
        Path rootDir = inputFilePath.getParent();
        Path tempPath = rootDir.resolve(tempFolder);
        Path outPutFilePath = rootDir.resolve(outputFile);

        assert (Files.isRegularFile(inputFilePath));

        File file = inputFilePath.toFile();
        fillfileWithIntegers(file);
        List<File> chunkFiles = sortChunks(file, BLOCKSIZE, tempPath);
        mergeChunks(chunkFiles, outPutFilePath);
    }

    public static long estimateAvailableMemory() {
        System.gc();
        // http://stackoverflow.com/questions/12807797/java-get-available-memory
        Runtime r = Runtime.getRuntime();
        long allocatedMemory = r.totalMemory() - r.freeMemory();
        long presFreeMemory = r.maxMemory() - allocatedMemory;
        return presFreeMemory;
    }

    public static List<File> sortChunks(File inputFile, long blocksize, Path tempFolder) {

        String line = "";
        long currSize = 0;
        int counter = 0;
        List<Integer> nums = new ArrayList<>();
        List<File> tmpFiles = new ArrayList<>();
        try (BufferedReader bfr = new BufferedReader(new FileReader(inputFile))) {

            if (!Files.isDirectory(tempFolder)) {
                Files.createDirectories(tempFolder);
            }

            while ((line = bfr.readLine()) != null) {
                int num = Integer.parseInt(line);
                nums.add(num);
                currSize += Integer.BYTES;

                if (currSize > blocksize) {
                    Path outputPath = tempFolder.resolve(Integer.toString(counter) + ".txt");
                    File output = saveChunk(nums, outputPath);

                    tmpFiles.add(output);
                    nums.clear();
                    currSize = 0;
                    counter++;
                }
            }
            if (nums.size() > 0) {
                Path outputPath = tempFolder.resolve(Integer.toString(counter) + ".txt");
                File output = saveChunk(nums, outputPath);
                tmpFiles.add(output);
                nums.clear();
                currSize = 0;
                counter++;
            }
        } catch (EOFException e) {

            try {
                Path outputPath = tempFolder.resolve(Integer.toString(counter) + ".txt");
                File output = saveChunk(nums, outputPath);

                tmpFiles.add(output);
                nums.clear();
                currSize = 0;
                counter++;
            } catch (Exception ee) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tmpFiles;
    }

    public static File saveChunk(List<Integer> nums, Path outputPath) throws IOException {

        Collections.sort(nums);
        if (!Files.isRegularFile(outputPath))
            Files.createFile(outputPath);

        File output = outputPath.toFile();
        try (BufferedWriter bfw = new BufferedWriter(new FileWriter(output))) {
            for (int number : nums) {
                bfw.write(Integer.toString(number));
                bfw.newLine();
            }
            return output;
        }
    }

    public static void mergeChunks(List<File> chunks, Path resulPath) {

        List<BufferedReader> fileReaders = new ArrayList<>();
        try {
            if (!Files.isRegularFile(resulPath)) {
                Files.createFile(resulPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (BufferedWriter bfw = new BufferedWriter(new FileWriter(resulPath.toFile()))) {

            PriorityQueue<Map.Entry<BufferedReader, Integer>> pq = new PriorityQueue<>(Map.Entry.comparingByValue());

            for (var f : chunks) {
                fileReaders.add(new BufferedReader(new FileReader(f)));
                System.out.println(f.getName());
            }
            String line = "";
            for (var bfr : fileReaders) {
                if ((line = bfr.readLine()) != null) {
                    pq.add(new AbstractMap.SimpleEntry<>(bfr, Integer.parseInt(line)));
                }
            }

            while (!pq.isEmpty()) {
                var entry = pq.poll();
                Integer mine = entry.getValue();
                BufferedReader bfr = entry.getKey();
                bfw.write(mine.toString());
                bfw.newLine();
                if ((line = bfr.readLine()) != null)
                    pq.add(new AbstractMap.SimpleEntry<>(bfr, Integer.parseInt(line)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                for (var bfr : fileReaders)
                    if (bfr != null)
                        bfr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void fillfileWithIntegers(File file) {
        Random rand = new Random();
        final int max_bound = (int) 1E+9;
        int count = (int) 1E+7;

        try (BufferedWriter bfw = new BufferedWriter(new FileWriter(file))) {
            while (count != 0) {

                Integer value = rand.nextInt(max_bound);
                bfw.write(value.toString());
                bfw.newLine();
                count--;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
