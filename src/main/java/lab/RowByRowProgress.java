package lab;

import java.io.BufferedReader;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

/** First benchmark: read a UTF-8 CSV sequentially, one record (line) at a time. */
public final class RowByRowProgress {
    private RowByRowProgress() {}

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: java lab.RowByRowProgress <csv-file>");
            System.exit(2);
        }

        Path csv = Path.of(args[0]);
        long totalBytes = Files.size(csv);
        long startNanos = System.nanoTime();
        long nextReportNanos = startNanos;
        long rows = 0;
        long characters = 0;

        try (CountingInputStream input = new CountingInputStream(Files.newInputStream(csv));
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String row;
            while ((row = reader.readLine()) != null) {
                // Deliberately small per-row work for the baseline benchmark.
                rows++;
                characters += row.length();

                long now = System.nanoTime();
                if (now >= nextReportNanos) {
                    printProgress(input.bytesRead(), totalBytes, rows, now - startNanos);
                    nextReportNanos = now + Duration.ofSeconds(1).toNanos();
                }
            }
            printProgress(input.bytesRead(), totalBytes, rows, System.nanoTime() - startNanos);
        }

        long elapsedNanos = System.nanoTime() - startNanos;
        System.out.println();
        System.out.printf("Finished: %,d rows, %,d characters%n", rows, characters);
        System.out.printf("Elapsed: %s%n", formatDuration(elapsedNanos));
    }

    private static void printProgress(long readBytes, long totalBytes, long rows, long elapsedNanos) {
        double percent = totalBytes == 0 ? 100.0 : Math.min(100.0, readBytes * 100.0 / totalBytes);
        double seconds = Math.max(0.001, elapsedNanos / 1_000_000_000.0);
        double mibPerSecond = readBytes / 1024.0 / 1024.0 / seconds;
        System.out.printf("\rProgress: %6.2f%% | %,d rows | %.1f MiB/s", percent, rows, mibPerSecond);
    }

    private static String formatDuration(long elapsedNanos) {
        Duration duration = Duration.ofNanos(elapsedNanos);
        long minutes = duration.toMinutes();
        double seconds = (duration.toMillis() % 60_000) / 1_000.0;
        return String.format("%dm %.3fs", minutes, seconds);
    }

    private static final class CountingInputStream extends FilterInputStream {
        private long bytesRead;

        private CountingInputStream(InputStream input) {
            super(input);
        }

        @Override
        public int read() throws IOException {
            int value = super.read();
            if (value != -1) bytesRead++;
            return value;
        }

        @Override
        public int read(byte[] buffer, int offset, int length) throws IOException {
            int count = super.read(buffer, offset, length);
            if (count > 0) bytesRead += count;
            return count;
        }

        private long bytesRead() {
            return bytesRead;
        }
    }
}
