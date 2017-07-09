import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class CountingFiles {

    public static void main(String[] args) throws InterruptedException {
        String inputFilePath = args[0];
        String outputFilePath = args[1];
        List<String> pathsList = readLinesFromFile(inputFilePath);
        List<CountTask> threads = new ArrayList<>(pathsList.size());
        ExecutorService executor = Executors.newCachedThreadPool();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            List<String> lines = new ArrayList<>();
            String format = "%-4s %-8d %s %n";
            for (int i = 0; i < threads.size(); i++) {
                CountTask thread = threads.get(i);
                lines.add(thread.path + ";" + thread.count);
                System.out.format(format, i + 1 + ".", thread.count, thread.path);
            }
            writeLinesToFile(outputFilePath, lines);
        }));
        pathsList.forEach(path -> threads.add(new CountTask(path)));
        threads.forEach(thread -> executor.execute(thread));
        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }

    private static class CountTask extends Thread {
        String path;
        long count = 0;

        public CountTask(String path) {
            this.path = path;
        }

        @Override
        public void run() {
            getNumberOfFiles(path);
        }

        private void getNumberOfFiles(String filePath) {
            for (File file : new File(filePath).listFiles()) {
                if (file.isDirectory()) {
                    getNumberOfFiles(file.getPath());
                } else count++;
            }
        }
    }

    private static List<String> readLinesFromFile(String path) {
        List<String> lines = new ArrayList<>();
        try (Stream<String> stream = Files.lines(Paths.get(path))) {
            stream.forEach(lines::add);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return lines;
        }
    }

    private static void writeLinesToFile(String path, List<String> lines) {
        try {
            Files.write(Paths.get(path), lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
