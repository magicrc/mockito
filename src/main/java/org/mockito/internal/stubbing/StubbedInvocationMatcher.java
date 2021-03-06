/*
 * Copyright (c) 2007 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockito.internal.stubbing;

import java.io.Serializable;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.mockito.internal.invocation.InvocationMatcher;
import org.mockito.invocation.DescribedInvocation;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.invocation.MatchableInvocation;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.Stubbing;

@SuppressWarnings("unchecked")
public class StubbedInvocationMatcher extends InvocationMatcher implements Serializable, Stubbing {

    private static final long serialVersionUID = 4919105134123672727L;
    private final Queue<Answer> answers = new ConcurrentLinkedQueue<>();
    private final Strictness strictness;
    private final Object usedAtLock = new Object[0];
    private DescribedInvocation usedAt;

    public StubbedInvocationMatcher(
            Answer answer, MatchableInvocation invocation, Strictness strictness) {
        super(invocation.getInvocation(), invocation.getMatchers());
        this.strictness = strictness;
        this.answers.add(answer);
    }

    @Override
    public Object answer(InvocationOnMock invocation) throws Throwable {
        // see ThreadsShareGenerouslyStubbedMockTest
        Answer a;
        synchronized (answers) {
            a = answers.size() == 1 ? answers.peek() : answers.poll();
        }
        return a.answer(invocation);
    }

    public void addAnswer(Answer answer) {
        answers.add(answer);
    }

    public void markStubUsed(DescribedInvocation usedAt) {
        synchronized (usedAtLock) {
            this.usedAt = usedAt;
        }
    }

    @Override
    public boolean wasUsed() {
        synchronized (usedAtLock) {
            return usedAt != null;
        }
    }

    @Override
    public String toString() {
        return super.toString() + " stubbed with: " + answers;
    }

    @Override
    public Strictness getStrictness() {
        return strictness;
    }
}
