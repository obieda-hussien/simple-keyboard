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

package rkr.simplekeyboard.inputmethod.emoji;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data provider for emoji collections organized by categories.
 */
public class EmojiData {
    
    private static final Map<EmojiCategory, List<EmojiItem>> emojiMap = new HashMap<>();
    private static final List<EmojiItem> allEmojis = new ArrayList<>();
    
    static {
        initializeEmojis();
    }
    
    private static void initializeEmojis() {
        // Smileys & Emotions
        addEmoji("😀", "grinning face", EmojiCategory.SMILEYS, "smile", "happy", "grin");
        addEmoji("😃", "grinning face with big eyes", EmojiCategory.SMILEYS, "smile", "happy", "joy");
        addEmoji("😄", "grinning face with smiling eyes", EmojiCategory.SMILEYS, "smile", "happy", "laugh");
        addEmoji("😁", "beaming face with smiling eyes", EmojiCategory.SMILEYS, "smile", "happy", "beam");
        addEmoji("😆", "grinning squinting face", EmojiCategory.SMILEYS, "laugh", "happy", "squint");
        addEmoji("😅", "grinning face with sweat", EmojiCategory.SMILEYS, "laugh", "sweat", "relief");
        addEmoji("🤣", "rolling on the floor laughing", EmojiCategory.SMILEYS, "laugh", "rofl", "funny");
        addEmoji("😂", "face with tears of joy", EmojiCategory.SMILEYS, "laugh", "cry", "tears", "joy");
        addEmoji("🙂", "slightly smiling face", EmojiCategory.SMILEYS, "smile", "happy");
        addEmoji("🙃", "upside-down face", EmojiCategory.SMILEYS, "silly", "upside", "down");
        addEmoji("😉", "winking face", EmojiCategory.SMILEYS, "wink", "flirt");
        addEmoji("😊", "smiling face with smiling eyes", EmojiCategory.SMILEYS, "smile", "happy", "blush");
        addEmoji("😇", "smiling face with halo", EmojiCategory.SMILEYS, "angel", "innocent", "halo");
        addEmoji("🥰", "smiling face with hearts", EmojiCategory.SMILEYS, "love", "hearts", "adore");
        addEmoji("😍", "smiling face with heart-eyes", EmojiCategory.SMILEYS, "love", "heart", "eyes");
        addEmoji("🤩", "star-struck", EmojiCategory.SMILEYS, "star", "struck", "amazed");
        addEmoji("😘", "face blowing a kiss", EmojiCategory.SMILEYS, "kiss", "blow", "love");
        addEmoji("😗", "kissing face", EmojiCategory.SMILEYS, "kiss");
        addEmoji("☺️", "smiling face", EmojiCategory.SMILEYS, "smile", "happy");
        addEmoji("😚", "kissing face with closed eyes", EmojiCategory.SMILEYS, "kiss", "closed", "eyes");
        
        // Animals & Nature
        addEmoji("🐶", "dog face", EmojiCategory.ANIMALS, "dog", "pet", "puppy");
        addEmoji("🐱", "cat face", EmojiCategory.ANIMALS, "cat", "pet", "kitten");
        addEmoji("🐭", "mouse face", EmojiCategory.ANIMALS, "mouse", "rat");
        addEmoji("🐹", "hamster", EmojiCategory.ANIMALS, "hamster", "pet");
        addEmoji("🐰", "rabbit face", EmojiCategory.ANIMALS, "rabbit", "bunny");
        addEmoji("🦊", "fox", EmojiCategory.ANIMALS, "fox");
        addEmoji("🐻", "bear", EmojiCategory.ANIMALS, "bear");
        addEmoji("🐼", "panda", EmojiCategory.ANIMALS, "panda", "bear");
        addEmoji("🐯", "tiger face", EmojiCategory.ANIMALS, "tiger");
        addEmoji("🦁", "lion", EmojiCategory.ANIMALS, "lion");
        addEmoji("🐮", "cow face", EmojiCategory.ANIMALS, "cow", "moo");
        addEmoji("🐷", "pig face", EmojiCategory.ANIMALS, "pig");
        addEmoji("🐸", "frog", EmojiCategory.ANIMALS, "frog");
        addEmoji("🐵", "monkey face", EmojiCategory.ANIMALS, "monkey");
        addEmoji("🙈", "see-no-evil monkey", EmojiCategory.ANIMALS, "monkey", "see", "evil");
        addEmoji("🙉", "hear-no-evil monkey", EmojiCategory.ANIMALS, "monkey", "hear", "evil");
        addEmoji("🙊", "speak-no-evil monkey", EmojiCategory.ANIMALS, "monkey", "speak", "evil");
        
        // Food & Drink
        addEmoji("🍎", "red apple", EmojiCategory.FOOD, "apple", "fruit", "red");
        addEmoji("🍊", "tangerine", EmojiCategory.FOOD, "orange", "fruit");
        addEmoji("🍋", "lemon", EmojiCategory.FOOD, "lemon", "fruit", "yellow");
        addEmoji("🍌", "banana", EmojiCategory.FOOD, "banana", "fruit", "yellow");
        addEmoji("🍉", "watermelon", EmojiCategory.FOOD, "watermelon", "fruit");
        addEmoji("🍇", "grapes", EmojiCategory.FOOD, "grapes", "fruit");
        addEmoji("🍓", "strawberry", EmojiCategory.FOOD, "strawberry", "fruit", "red");
        addEmoji("🍑", "cherries", EmojiCategory.FOOD, "cherry", "fruit", "red");
        addEmoji("🍒", "cherries", EmojiCategory.FOOD, "cherries", "fruit", "red");
        addEmoji("🥝", "kiwi fruit", EmojiCategory.FOOD, "kiwi", "fruit", "green");
        addEmoji("🍅", "tomato", EmojiCategory.FOOD, "tomato", "red");
        addEmoji("🍆", "eggplant", EmojiCategory.FOOD, "eggplant", "purple");
        addEmoji("🥑", "avocado", EmojiCategory.FOOD, "avocado", "green");
        addEmoji("🌽", "corn", EmojiCategory.FOOD, "corn", "yellow");
        addEmoji("🌶️", "hot pepper", EmojiCategory.FOOD, "pepper", "hot", "spicy");
        addEmoji("🍕", "pizza", EmojiCategory.FOOD, "pizza", "food");
        addEmoji("🍔", "hamburger", EmojiCategory.FOOD, "burger", "hamburger", "food");
        addEmoji("🌭", "hot dog", EmojiCategory.FOOD, "hotdog", "food");
        addEmoji("🌮", "taco", EmojiCategory.FOOD, "taco", "mexican", "food");
        addEmoji("🌯", "burrito", EmojiCategory.FOOD, "burrito", "mexican", "food");
        
        // Travel & Places
        addEmoji("🚗", "automobile", EmojiCategory.TRAVEL, "car", "auto", "vehicle");
        addEmoji("🚕", "taxi", EmojiCategory.TRAVEL, "taxi", "cab", "car");
        addEmoji("🚙", "sport utility vehicle", EmojiCategory.TRAVEL, "suv", "car", "vehicle");
        addEmoji("🚌", "bus", EmojiCategory.TRAVEL, "bus", "vehicle");
        addEmoji("🚎", "trolleybus", EmojiCategory.TRAVEL, "trolley", "bus");
        addEmoji("🏎️", "racing car", EmojiCategory.TRAVEL, "race", "car", "fast");
        addEmoji("🚓", "police car", EmojiCategory.TRAVEL, "police", "car");
        addEmoji("🚑", "ambulance", EmojiCategory.TRAVEL, "ambulance", "medical");
        addEmoji("🚒", "fire engine", EmojiCategory.TRAVEL, "fire", "truck");
        addEmoji("🚐", "minibus", EmojiCategory.TRAVEL, "minibus", "van");
        addEmoji("🛻", "pickup truck", EmojiCategory.TRAVEL, "truck", "pickup");
        addEmoji("🚚", "delivery truck", EmojiCategory.TRAVEL, "truck", "delivery");
        addEmoji("🚛", "articulated lorry", EmojiCategory.TRAVEL, "truck", "semi");
        addEmoji("🚜", "tractor", EmojiCategory.TRAVEL, "tractor", "farm");
        addEmoji("🏍️", "motorcycle", EmojiCategory.TRAVEL, "motorcycle", "bike");
        addEmoji("🛵", "motor scooter", EmojiCategory.TRAVEL, "scooter", "bike");
        addEmoji("🚲", "bicycle", EmojiCategory.TRAVEL, "bicycle", "bike");
        addEmoji("🛴", "kick scooter", EmojiCategory.TRAVEL, "scooter", "kick");
        addEmoji("✈️", "airplane", EmojiCategory.TRAVEL, "airplane", "plane", "fly");
        addEmoji("🚁", "helicopter", EmojiCategory.TRAVEL, "helicopter", "fly");
        
        // Objects
        addEmoji("📱", "mobile phone", EmojiCategory.OBJECTS, "phone", "mobile", "cell");
        addEmoji("💻", "laptop", EmojiCategory.OBJECTS, "laptop", "computer");
        addEmoji("⌨️", "keyboard", EmojiCategory.OBJECTS, "keyboard", "computer");
        addEmoji("🖥️", "desktop computer", EmojiCategory.OBJECTS, "desktop", "computer");
        addEmoji("🖨️", "printer", EmojiCategory.OBJECTS, "printer");
        addEmoji("🖱️", "computer mouse", EmojiCategory.OBJECTS, "mouse", "computer");
        addEmoji("💾", "floppy disk", EmojiCategory.OBJECTS, "floppy", "disk", "save");
        addEmoji("💿", "optical disk", EmojiCategory.OBJECTS, "cd", "dvd", "disk");
        addEmoji("📷", "camera", EmojiCategory.OBJECTS, "camera", "photo");
        addEmoji("📹", "video camera", EmojiCategory.OBJECTS, "video", "camera");
        addEmoji("📺", "television", EmojiCategory.OBJECTS, "tv", "television");
        addEmoji("📻", "radio", EmojiCategory.OBJECTS, "radio");
        addEmoji("🎵", "musical note", EmojiCategory.OBJECTS, "music", "note");
        addEmoji("🎶", "musical notes", EmojiCategory.OBJECTS, "music", "notes");
        addEmoji("🎤", "microphone", EmojiCategory.OBJECTS, "microphone", "mic");
        addEmoji("🎧", "headphone", EmojiCategory.OBJECTS, "headphone", "music");
        addEmoji("📢", "loudspeaker", EmojiCategory.OBJECTS, "speaker", "loud");
        addEmoji("🔔", "bell", EmojiCategory.OBJECTS, "bell", "notification");
        addEmoji("🔕", "bell with slash", EmojiCategory.OBJECTS, "bell", "mute", "silent");
        addEmoji("💡", "light bulb", EmojiCategory.OBJECTS, "light", "bulb", "idea");
        
        // Symbols
        addEmoji("❤️", "red heart", EmojiCategory.SYMBOLS, "heart", "love", "red");
        addEmoji("🧡", "orange heart", EmojiCategory.SYMBOLS, "heart", "orange");
        addEmoji("💛", "yellow heart", EmojiCategory.SYMBOLS, "heart", "yellow");
        addEmoji("💚", "green heart", EmojiCategory.SYMBOLS, "heart", "green");
        addEmoji("💙", "blue heart", EmojiCategory.SYMBOLS, "heart", "blue");
        addEmoji("💜", "purple heart", EmojiCategory.SYMBOLS, "heart", "purple");
        addEmoji("🖤", "black heart", EmojiCategory.SYMBOLS, "heart", "black");
        addEmoji("🤍", "white heart", EmojiCategory.SYMBOLS, "heart", "white");
        addEmoji("🤎", "brown heart", EmojiCategory.SYMBOLS, "heart", "brown");
        addEmoji("💔", "broken heart", EmojiCategory.SYMBOLS, "heart", "broken", "sad");
        addEmoji("❣️", "heart exclamation", EmojiCategory.SYMBOLS, "heart", "exclamation");
        addEmoji("💕", "two hearts", EmojiCategory.SYMBOLS, "hearts", "love");
        addEmoji("💖", "sparkling heart", EmojiCategory.SYMBOLS, "heart", "sparkle");
        addEmoji("💗", "growing heart", EmojiCategory.SYMBOLS, "heart", "growing");
        addEmoji("💘", "heart with arrow", EmojiCategory.SYMBOLS, "heart", "arrow", "cupid");
        addEmoji("💝", "heart with ribbon", EmojiCategory.SYMBOLS, "heart", "ribbon", "gift");
        addEmoji("💞", "revolving hearts", EmojiCategory.SYMBOLS, "hearts", "revolving");
        addEmoji("💟", "heart decoration", EmojiCategory.SYMBOLS, "heart", "decoration");
        addEmoji("☮️", "peace symbol", EmojiCategory.SYMBOLS, "peace");
        addEmoji("✝️", "latin cross", EmojiCategory.SYMBOLS, "cross", "christian");
    }
    
    private static void addEmoji(String emoji, String description, EmojiCategory category, String... keywords) {
        EmojiItem item = new EmojiItem(emoji, description, category, keywords);
        allEmojis.add(item);
        
        emojiMap.computeIfAbsent(category, k -> new ArrayList<>()).add(item);
    }
    
    public static List<EmojiItem> getEmojisByCategory(EmojiCategory category) {
        return emojiMap.getOrDefault(category, new ArrayList<>());
    }
    
    public static List<EmojiItem> getAllEmojis() {
        return new ArrayList<>(allEmojis);
    }
    
    public static List<EmojiItem> searchEmojis(String query) {
        List<EmojiItem> results = new ArrayList<>();
        for (EmojiItem emoji : allEmojis) {
            if (emoji.matches(query)) {
                results.add(emoji);
            }
        }
        return results;
    }
}