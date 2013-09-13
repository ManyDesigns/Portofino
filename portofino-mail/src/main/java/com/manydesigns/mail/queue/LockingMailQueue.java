/*
 * Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.manydesigns.mail.queue;

import com.manydesigns.mail.queue.model.Email;

import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class LockingMailQueue implements MailQueue {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    protected final MailQueue mailQueue;
    protected final ReadWriteLock lock = new ReentrantReadWriteLock(true);

    public LockingMailQueue(MailQueue mailQueue) {
        this.mailQueue = mailQueue;
    }

    public String enqueue(Email email) throws QueueException {
        lock.writeLock().lock();
        try {
            return mailQueue.enqueue(email);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public List<String> getEnqueuedEmailIds() throws QueueException {
        lock.readLock().lock();
        try {
            return mailQueue.getEnqueuedEmailIds();
        } finally {
            lock.readLock().unlock();
        }
    }

    public Email loadEmail(String id) throws QueueException {
        lock.readLock().lock();
        try {
            return mailQueue.loadEmail(id);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void markSent(String id) throws QueueException {
        lock.writeLock().lock();
        try {
            mailQueue.markSent(id);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void markFailed(String id) throws QueueException {
        lock.writeLock().lock();
        try {
            mailQueue.markFailed(id);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void setKeepSent(boolean keepSent) {
        mailQueue.setKeepSent(keepSent);
    }

    public boolean isKeepSent() {
        return mailQueue.isKeepSent();
    }
}
