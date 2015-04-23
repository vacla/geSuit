package net.cubespace.geSuit.core.remote;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.common.collect.Lists;

import net.cubespace.geSuit.core.messages.LinkedMessage;

public class MessageWaiter {
    private LinkedList<WaitFuture<?>> waiting;

    public MessageWaiter() {
        waiting = Lists.newLinkedList();
    }

    public <T> Future<T> waitForReply(LinkedMessage<T> source) {
        WaitFuture<T> future = new WaitFuture<T>(source);
        waiting.add(future);

        return future;
    }

    public void checkMessage(LinkedMessage<?> message) {
        Iterator<WaitFuture<?>> it = waiting.iterator();

        while (it.hasNext()) {
            WaitFuture<?> future = it.next();

            if (future.isReply(message)) {
                it.remove();
                future.done(message);
            }
        }
    }

    private static class WaitFuture<T> implements Future<T> {
        private CountDownLatch latch = new CountDownLatch(1);
        private T value;
        private ExecutionException error;

        private LinkedMessage<T> source;

        public WaitFuture(LinkedMessage<T> source) {
            this.source = source;
        }

        public boolean isReply(LinkedMessage<?> reply) {
            return reply.isReply() && reply.isSource(source);
        }

        @SuppressWarnings("unchecked")
        public void done(LinkedMessage<?> reply) {
            try {
                value = (T) reply.getReply();
            } catch (ExecutionException e) {
                error = e;
            }
            latch.countDown();
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return latch.getCount() == 0;
        }

        @Override
        public T get() throws InterruptedException, ExecutionException {
            latch.await();

            if (error != null)
                throw error;
            return value;
        }

        @Override
        public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            if (!latch.await(timeout, unit))
                throw new TimeoutException();

            if (error != null)
                throw error;
            return value;
        }
    }
}
