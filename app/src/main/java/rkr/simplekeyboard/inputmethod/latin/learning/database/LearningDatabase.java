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

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * Room database for storing keyboard learning data.
 */
@Database(
    entities = {WordEntity.class, NGramEntity.class},
    version = 1,
    exportSchema = false
)
public abstract class LearningDatabase extends RoomDatabase {
    
    private static volatile LearningDatabase INSTANCE;
    
    public abstract WordDao wordDao();
    public abstract NGramDao ngramDao();
    
    public static LearningDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (LearningDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            LearningDatabase.class,
                            "learning_database"
                        )
                        .fallbackToDestructiveMigration()
                        .build();
                }
            }
        }
        return INSTANCE;
    }
}