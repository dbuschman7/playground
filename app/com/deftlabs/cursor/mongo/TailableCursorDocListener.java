/**
 * Copyright 2011, Deft Labs.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.deftlabs.cursor.mongo;

// Mongo
// Java
import java.util.EventListener;

import com.mongodb.DB;
import com.mongodb.DBObject;

/**
 * The tailable cursor doc listener interface. Used for the event based notification model.
 */
public abstract class TailableCursorDocListener implements EventListener {

    private TailableCursor cursor;
    private String id;

    protected TailableCursorDocListener(String id) {
        this.id = id;
    }

    public void start(DB db, TailableCursorOptions options) {

        // make sure I am my own listener
        options.setDocListener(this);
        options.setThreadName(id);

        // fire up the cursor
        cursor = new TailableCursorImpl(db, options);
        cursor.start();
    }

    public void stop() {
        if (cursor == null)
            return;

        if (cursor.isRunning()) {
            cursor.stop();
        }
    }

    /**
     * Called when a document is pulled from the tailable cursor.
     */
    public abstract void nextDoc(final DBObject pDoc);

}
