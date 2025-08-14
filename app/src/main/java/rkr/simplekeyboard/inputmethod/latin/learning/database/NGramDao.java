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

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

/**
 * Data Access Object for N-gram related database operations.
 */
@Dao
public interface NGramDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNGram(NGramEntity ngram);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNGrams(List<NGramEntity> ngrams);
    
    @Query("SELECT * FROM ngrams WHERE context = :context ORDER BY frequency DESC LIMIT :limit")
    List<NGramEntity> getNGramsByContext(String context, int limit);
    
    @Query("SELECT * FROM ngrams WHERE gramType = :gramType ORDER BY frequency DESC")
    List<NGramEntity> getNGramsByType(int gramType);
    
    @Query("UPDATE ngrams SET frequency = frequency + 1, lastUsed = :timestamp WHERE context = :context AND nextWord = :nextWord")
    void incrementNGramFrequency(String context, String nextWord, long timestamp);
    
    @Query("DELETE FROM ngrams WHERE lastUsed < :cutoffTime")
    void cleanupOldNGrams(long cutoffTime);
    
    @Query("DELETE FROM ngrams")
    void clearAllNGrams();
}