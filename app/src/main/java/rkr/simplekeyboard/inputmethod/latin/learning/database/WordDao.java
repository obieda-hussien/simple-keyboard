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
 * Data Access Object for word-related database operations.
 */
@Dao
public interface WordDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertWord(WordEntity word);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertWords(List<WordEntity> words);
    
    @Query("SELECT * FROM words WHERE word LIKE :prefix || '%' ORDER BY frequency DESC, lastUsed DESC LIMIT :limit")
    List<WordEntity> getWordsByPrefix(String prefix, int limit);
    
    @Query("SELECT * FROM words WHERE isUserWord = 1 ORDER BY lastUsed DESC")
    List<WordEntity> getUserWords();
    
    @Query("SELECT * FROM words WHERE word = :word")
    WordEntity getWord(String word);
    
    @Query("UPDATE words SET frequency = frequency + 1, lastUsed = :timestamp WHERE word = :word")
    void incrementWordFrequency(String word, long timestamp);
    
    @Query("DELETE FROM words WHERE isUserWord = 0 AND lastUsed < :cutoffTime")
    void cleanupOldWords(long cutoffTime);
    
    @Query("DELETE FROM words")
    void clearAllWords();
}