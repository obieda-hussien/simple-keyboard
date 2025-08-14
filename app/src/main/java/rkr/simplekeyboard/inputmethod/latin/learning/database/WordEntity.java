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
import androidx.room.PrimaryKey;

/**
 * Room entity for storing learned words and their frequencies.
 */
@Entity(tableName = "words")
public class WordEntity {
    @PrimaryKey
    @NonNull
    public String word;
    
    public int frequency;
    public boolean isUserWord;
    public long lastUsed;
    
    public WordEntity() {}
    
    @androidx.room.Ignore
    public WordEntity(String word, int frequency, boolean isUserWord) {
        this.word = word;
        this.frequency = frequency;
        this.isUserWord = isUserWord;
        this.lastUsed = System.currentTimeMillis();
    }
}