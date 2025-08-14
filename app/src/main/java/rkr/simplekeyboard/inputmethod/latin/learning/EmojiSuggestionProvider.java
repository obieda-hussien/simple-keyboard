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

package rkr.simplekeyboard.inputmethod.latin.learning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides emoji suggestions based on typed keywords.
 * Supports both English and Arabic keywords.
 */
public class EmojiSuggestionProvider {
    
    private static final Map<String, List<String>> keywordToEmojis = new HashMap<>();
    
    static {
        initializeKeywordMap();
    }
    
    private static void initializeKeywordMap() {
        // Emotions & Feelings
        addKeywordMapping("love", "❤️", "😍", "🥰", "💕", "💖");
        addKeywordMapping("حب", "❤️", "😍", "🥰", "💕"); // Arabic: love
        addKeywordMapping("happy", "😀", "😃", "😄", "😁", "😊");
        addKeywordMapping("سعيد", "😀", "😃", "😄", "😁"); // Arabic: happy
        addKeywordMapping("sad", "😢", "😭", "😞", "💔");
        addKeywordMapping("حزين", "😢", "😭", "😞", "💔"); // Arabic: sad
        addKeywordMapping("angry", "😠", "😡", "🤬");
        addKeywordMapping("غاضب", "😠", "😡", "🤬"); // Arabic: angry
        addKeywordMapping("laugh", "😂", "🤣", "😆");
        addKeywordMapping("ضحك", "😂", "🤣", "😆"); // Arabic: laugh
        addKeywordMapping("smile", "😊", "🙂", "😀", "😃");
        addKeywordMapping("ابتسامة", "😊", "🙂", "😀"); // Arabic: smile
        addKeywordMapping("cry", "😢", "😭", "😿");
        addKeywordMapping("بكاء", "😢", "😭"); // Arabic: cry
        addKeywordMapping("surprised", "😲", "😮", "😯", "😱");
        addKeywordMapping("مفاجأة", "😲", "😮", "😱"); // Arabic: surprised
        
        // Animals
        addKeywordMapping("cat", "🐱", "🐈", "😸", "😻");
        addKeywordMapping("قطة", "🐱", "🐈", "😸"); // Arabic: cat
        addKeywordMapping("dog", "🐶", "🐕", "🦮", "🐩");
        addKeywordMapping("كلب", "🐶", "🐕"); // Arabic: dog
        addKeywordMapping("bear", "🐻", "🧸");
        addKeywordMapping("دب", "🐻", "🧸"); // Arabic: bear
        addKeywordMapping("lion", "🦁");
        addKeywordMapping("أسد", "🦁"); // Arabic: lion
        addKeywordMapping("mouse", "🐭", "🐁");
        addKeywordMapping("فأر", "🐭", "🐁"); // Arabic: mouse
        addKeywordMapping("rabbit", "🐰", "🐇");
        addKeywordMapping("أرنب", "🐰", "🐇"); // Arabic: rabbit
        
        // Food & Drink
        addKeywordMapping("food", "🍕", "🍔", "🌭", "🍟");
        addKeywordMapping("طعام", "🍕", "🍔", "🌭"); // Arabic: food
        addKeywordMapping("pizza", "🍕");
        addKeywordMapping("بيتزا", "🍕"); // Arabic: pizza
        addKeywordMapping("burger", "🍔");
        addKeywordMapping("برجر", "🍔"); // Arabic: burger
        addKeywordMapping("coffee", "☕", "🍵");
        addKeywordMapping("قهوة", "☕", "🍵"); // Arabic: coffee
        addKeywordMapping("tea", "🍵", "🧋");
        addKeywordMapping("شاي", "🍵", "🧋"); // Arabic: tea
        addKeywordMapping("apple", "🍎", "🍏");
        addKeywordMapping("تفاحة", "🍎", "🍏"); // Arabic: apple
        addKeywordMapping("banana", "🍌");
        addKeywordMapping("موز", "🍌"); // Arabic: banana
        
        // Transportation
        addKeywordMapping("car", "🚗", "🚙", "🏎️");
        addKeywordMapping("سيارة", "🚗", "🚙", "🏎️"); // Arabic: car
        addKeywordMapping("plane", "✈️", "🛩️");
        addKeywordMapping("طائرة", "✈️", "🛩️"); // Arabic: plane
        addKeywordMapping("train", "🚆", "🚄", "🚅");
        addKeywordMapping("قطار", "🚆", "🚄"); // Arabic: train
        addKeywordMapping("bike", "🚲", "🏍️");
        addKeywordMapping("دراجة", "🚲", "🏍️"); // Arabic: bike
        
        // Weather
        addKeywordMapping("sun", "☀️", "🌞", "🌅", "🌄");
        addKeywordMapping("شمس", "☀️", "🌞", "🌅"); // Arabic: sun
        addKeywordMapping("rain", "🌧️", "☔", "🌦️");
        addKeywordMapping("مطر", "🌧️", "☔", "🌦️"); // Arabic: rain
        addKeywordMapping("snow", "❄️", "☃️", "⛄");
        addKeywordMapping("ثلج", "❄️", "☃️", "⛄"); // Arabic: snow
        addKeywordMapping("cloud", "☁️", "⛅", "🌤️");
        addKeywordMapping("سحابة", "☁️", "⛅", "🌤️"); // Arabic: cloud
        
        // Common words
        addKeywordMapping("good", "👍", "👌", "✅");
        addKeywordMapping("جيد", "👍", "👌", "✅"); // Arabic: good
        addKeywordMapping("bad", "👎", "❌", "💩");
        addKeywordMapping("سيء", "👎", "❌"); // Arabic: bad
        addKeywordMapping("yes", "✅", "👍", "☑️");
        addKeywordMapping("نعم", "✅", "👍", "☑️"); // Arabic: yes
        addKeywordMapping("no", "❌", "👎", "🚫");
        addKeywordMapping("لا", "❌", "👎", "🚫"); // Arabic: no
        addKeywordMapping("ok", "👌", "✅", "👍");
        addKeywordMapping("thank", "🙏", "🤝", "🙇");
        addKeywordMapping("شكرا", "🙏", "🤝"); // Arabic: thank
        addKeywordMapping("hello", "👋", "😊", "🙂");
        addKeywordMapping("مرحبا", "👋", "😊", "🙂"); // Arabic: hello
        addKeywordMapping("bye", "👋", "😘", "✋");
        addKeywordMapping("وداعا", "👋", "😘"); // Arabic: bye
        
        // Time & Celebrations
        addKeywordMapping("birthday", "🎂", "🎉", "🎁", "🎈");
        addKeywordMapping("عيد ميلاد", "🎂", "🎉", "🎁"); // Arabic: birthday
        addKeywordMapping("party", "🎉", "🎊", "🥳");
        addKeywordMapping("حفلة", "🎉", "🎊", "🥳"); // Arabic: party
        addKeywordMapping("celebration", "🎉", "🎊", "🥳", "🎁");
        addKeywordMapping("احتفال", "🎉", "🎊", "🥳"); // Arabic: celebration
        
        // Objects & Technology
        addKeywordMapping("phone", "📱", "☎️", "📞");
        addKeywordMapping("هاتف", "📱", "☎️", "📞"); // Arabic: phone
        addKeywordMapping("computer", "💻", "🖥️", "⌨️");
        addKeywordMapping("كمبيوتر", "💻", "🖥️"); // Arabic: computer
        addKeywordMapping("camera", "📷", "📸", "📹");
        addKeywordMapping("كاميرا", "📷", "📸"); // Arabic: camera
        addKeywordMapping("music", "🎵", "🎶", "🎤", "🎧");
        addKeywordMapping("موسيقى", "🎵", "🎶", "🎤"); // Arabic: music
        
        // Numbers (common number-related keywords)
        addKeywordMapping("money", "💰", "💵", "💸", "🤑");
        addKeywordMapping("مال", "💰", "💵", "💸"); // Arabic: money
        addKeywordMapping("dollar", "💵", "💰");
        addKeywordMapping("دولار", "💵", "💰"); // Arabic: dollar
        addKeywordMapping("price", "💰", "💸", "🏷️");
        addKeywordMapping("سعر", "💰", "💸", "🏷️"); // Arabic: price
    }
    
    private static void addKeywordMapping(String keyword, String... emojis) {
        keywordToEmojis.put(keyword.toLowerCase(), Arrays.asList(emojis));
    }
    
    /**
     * Gets emoji suggestions for a given word.
     * @param word The word to find emoji suggestions for
     * @return List of emoji suggestions, empty if no matches
     */
    public static List<String> getEmojiSuggestions(String word) {
        if (word == null || word.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String lowerWord = word.toLowerCase().trim();
        
        // Direct keyword match
        List<String> directMatch = keywordToEmojis.get(lowerWord);
        if (directMatch != null && !directMatch.isEmpty()) {
            return new ArrayList<>(directMatch);
        }
        
        // Partial keyword match
        List<String> partialMatches = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : keywordToEmojis.entrySet()) {
            String keyword = entry.getKey();
            if (keyword.contains(lowerWord) || lowerWord.contains(keyword)) {
                partialMatches.addAll(entry.getValue());
                if (partialMatches.size() >= 3) {
                    break; // Limit partial matches
                }
            }
        }
        
        return partialMatches;
    }
    
    /**
     * Checks if a word has emoji suggestions available.
     */
    public static boolean hasEmojiSuggestions(String word) {
        return !getEmojiSuggestions(word).isEmpty();
    }
}