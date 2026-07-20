package io.github.jutil.inputstreamprocessor.core;

/**
 * Immutable summary of a completed input-stream processing operation.
 *
 * <p>The processed count is the number of emitted items for which the client
 * consumer returned normally, including emitted {@code null} items. Items whose
 * consumer call terminates by throwing are not counted.</p>
 *
 * <p>A result is returned only when processing completes normally. When
 * {@link InputStreamProcessor#process(java.io.InputStream,
 * java.util.function.Consumer) process(...)} throws, no result is returned,
 * although consumer calls completed before the failure remain completed.</p>
 */
public final class ProcessingResult {

    private final long processedCount;

    ProcessingResult(long processedCount) {
        this.processedCount = processedCount;
    }

    /**
     * Returns the number of items, including {@code null} items, successfully
     * accepted by the client consumer.
     *
     * @return the successful consumer-call count
     */
    public long getProcessedCount() {
        return processedCount;
    }
}
