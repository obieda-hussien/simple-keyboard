/*
 * Copyright (C) 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rkr.simplekeyboard.inputmethod.latin.learning.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;

/**
 * Room entity for storing N-gram data (bigrams and trigrams).
 */
@Entity(tableName = "ngrams", primaryKeys = {"context", "nextWord"})
public class NGramEntity {
    @NonNull
    public String context; // The preceding word(s)
    @NonNull
    public String nextWord; // The following word
    public int frequency;   // How often this combination appears
    public int gramType;    // 2 for bigram, 3 for trigram
    public long lastUsed;
    
    public NGramEntity() {}
    
    @androidx.room.Ignore
    public NGramEntity(String context, String nextWord, int frequency, int gramType) {
        this.context = context;
        this.nextWord = nextWord;
        this.frequency = frequency;
        this.gramType = gramType;
        this.lastUsed = System.currentTimeMillis();
    }
}