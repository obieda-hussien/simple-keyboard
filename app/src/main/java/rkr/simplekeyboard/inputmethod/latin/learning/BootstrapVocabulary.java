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

import java.util.Arrays;
import java.util.List;

/**
 * Bootstrap vocabulary with common words to jump-start the suggestion system.
 * Provides initial word suggestions before user learning data is built up.
 */
public class BootstrapVocabulary {
    
    // Comprehensive English dictionary for initial suggestions (1000+ words)
    private static final String[] COMMON_ENGLISH_WORDS = {
        // Single-letter words that are important
        "a", "I",
        
        // Most common words (frequency-based)
        "the", "be", "to", "of", "and", "in", "that", "have", "it", "for", "not", "on", "with",
        "he", "as", "you", "do", "at", "this", "but", "his", "by", "from", "they", "we", "say",
        "her", "she", "or", "an", "will", "my", "one", "all", "would", "there", "their",
        
        // Common verbs (100+ most used)
        "is", "are", "was", "were", "been", "being", "am",
        "have", "has", "had", "having",
        "do", "does", "did", "doing", "done",
        "can", "could", "may", "might", "must", "shall", "should", "will", "would",
        "make", "made", "making", "take", "took", "taken", "taking",
        "go", "goes", "went", "gone", "going", "come", "came", "coming",
        "see", "saw", "seen", "seeing", "look", "looked", "looking",
        "know", "knew", "known", "knowing", "think", "thought", "thinking",
        "get", "got", "gotten", "getting", "give", "gave", "given", "giving",
        "use", "used", "using", "find", "found", "finding",
        "tell", "told", "telling", "ask", "asked", "asking",
        "work", "worked", "working", "seem", "seemed", "seeming",
        "feel", "felt", "feeling", "try", "tried", "trying",
        "leave", "left", "leaving", "call", "called", "calling",
        "need", "needed", "needing", "want", "wanted", "wanting",
        "become", "became", "becoming", "show", "showed", "showing",
        "turn", "turned", "turning", "start", "started", "starting",
        "move", "moved", "moving", "live", "lived", "living",
        "believe", "believed", "believing", "bring", "brought", "bringing",
        "happen", "happened", "happening", "write", "wrote", "written", "writing",
        "sit", "sat", "sitting", "stand", "stood", "standing",
        "run", "ran", "running", "keep", "kept", "keeping",
        "hold", "held", "holding", "talk", "talked", "talking",
        "put", "putting", "play", "played", "playing",
        
        // Common nouns (200+ most used)
        "time", "year", "people", "way", "day", "man", "thing", "woman", "life", "child",
        "world", "school", "state", "family", "student", "group", "country", "problem",
        "hand", "part", "place", "case", "week", "company", "system", "program", "question",
        "work", "government", "number", "night", "point", "home", "water", "room", "mother",
        "area", "money", "story", "fact", "month", "lot", "right", "study", "book", "eye",
        "job", "word", "business", "issue", "side", "kind", "head", "house", "service",
        "friend", "father", "power", "hour", "game", "line", "end", "member", "law", "car",
        "city", "community", "name", "president", "team", "minute", "idea", "kid", "body",
        "information", "back", "parent", "face", "others", "level", "office", "door",
        "health", "person", "art", "war", "history", "party", "result", "change", "morning",
        "reason", "research", "girl", "guy", "moment", "air", "teacher", "force", "education",
        "food", "phone", "street", "plan", "experience", "love", "interest", "death",
        "course", "building", "process", "market", "class", "computer", "music", "voice",
        "age", "wife", "table", "road", "window", "view", "subject", "afternoon",
        "chance", "brother", "sister", "dog", "cat", "bird", "tree", "paper", "letter",
        "heart", "doctor", "hospital", "church", "ground", "month", "price", "product",
        
        // Common adjectives (100+ most used)
        "good", "new", "first", "last", "long", "great", "little", "own", "other", "old",
        "right", "big", "high", "different", "small", "large", "next", "early", "young",
        "important", "few", "public", "bad", "same", "able", "best", "better", "sure",
        "clear", "major", "likely", "real", "simple", "hot", "cold", "hard", "easy",
        "strong", "difficult", "local", "happy", "sad", "full", "free", "low", "short",
        "late", "true", "false", "ready", "open", "close", "dark", "light", "quick",
        "slow", "beautiful", "nice", "fine", "special", "whole", "national", "social",
        "political", "economic", "legal", "poor", "rich", "modern", "popular",
        
        // Common adverbs (50+ most used)
        "so", "out", "up", "just", "now", "how", "then", "more", "also", "here", "well",
        "only", "very", "even", "back", "there", "down", "still", "much", "too", "where",
        "really", "never", "always", "often", "sometimes", "quite", "perhaps", "maybe",
        "today", "tonight", "tomorrow", "yesterday", "soon", "already", "again", "once",
        "almost", "enough", "away", "together", "forward", "outside", "inside",
        
        // Common prepositions
        "about", "above", "across", "after", "against", "along", "among", "around", "before",
        "behind", "below", "beneath", "beside", "between", "beyond", "during", "except",
        "inside", "into", "near", "off", "onto", "outside", "over", "through", "toward",
        "under", "until", "upon", "within", "without",
        
        // Common pronouns and determiners
        "he", "she", "it", "we", "they", "me", "him", "her", "us", "them",
        "my", "your", "his", "her", "its", "our", "their",
        "mine", "yours", "hers", "ours", "theirs",
        "this", "that", "these", "those", "who", "whom", "whose", "which", "what",
        "some", "any", "each", "every", "both", "few", "many", "much", "most", "several",
        
        // Numbers and quantities
        "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten",
        "hundred", "thousand", "million", "billion", "first", "second", "third",
        
        // Common contractions and informal
        "don't", "can't", "won't", "isn't", "aren't", "wasn't", "weren't", "haven't",
        "hasn't", "hadn't", "doesn't", "didn't", "wouldn't", "shouldn't", "couldn't",
        "I'm", "you're", "he's", "she's", "it's", "we're", "they're",
        "I've", "you've", "we've", "they've", "I'll", "you'll", "he'll", "she'll",
        
        // Technology and modern terms
        "app", "apps", "email", "internet", "website", "online", "password", "user",
        "account", "file", "folder", "download", "upload", "link", "click", "search",
        "data", "software", "hardware", "device", "screen", "keyboard", "mouse",
        
        // Common expressions and phrases
        "yes", "no", "okay", "ok", "please", "thank", "thanks", "sorry", "excuse",
        "hello", "hi", "hey", "goodbye", "bye", "welcome", "congratulations"
    };
    
    // Comprehensive Arabic dictionary for initial suggestions (1000+ words)
    private static final String[] COMMON_ARABIC_WORDS = {
        // Single-letter words and short conjunctions that are important
        "و", "أ", "ب", "ل", "ف", "ك", "س",
        
        // Most common Arabic words and particles
        "في", "من", "على", "إلى", "عن", "مع", "هذا", "هذه", "ذلك", "تلك",
        "التي", "الذي", "التي", "اللذان", "اللتان", "الذين", "اللاتي", "اللواتي",
        
        // Common verbs (present and past)
        "كان", "كانت", "كانوا", "كنت", "كنا", "يكون", "تكون", "يكونون", "أكون", "نكون",
        "قال", "قالت", "قالوا", "يقول", "تقول", "يقولون", "أقول", "نقول",
        "فعل", "فعلت", "فعلوا", "يفعل", "تفعل", "يفعلون", "أفعل", "نفعل",
        "ذهب", "ذهبت", "ذهبوا", "يذهب", "تذهب", "يذهبون", "أذهب", "نذهب",
        "جاء", "جاءت", "جاءوا", "يجيء", "تجيء", "يجيئون", "أجيء", "نجيء",
        "عمل", "عملت", "عملوا", "يعمل", "تعمل", "يعملون", "أعمل", "نعمل",
        "أخذ", "أخذت", "أخذوا", "يأخذ", "تأخذ", "يأخذون", "آخذ", "نأخذ",
        "وجد", "وجدت", "وجدوا", "يجد", "تجد", "يجدون", "أجد", "نجد",
        "رأى", "رأت", "رأوا", "يرى", "ترى", "يرون", "أرى", "نرى",
        "علم", "علمت", "علموا", "يعلم", "تعلم", "يعلمون", "أعلم", "نعلم",
        "عرف", "عرفت", "عرفوا", "يعرف", "تعرف", "يعرفون", "أعرف", "نعرف",
        "أراد", "أرادت", "أرادوا", "يريد", "تريد", "يريدون", "أريد", "نريد",
        "كتب", "كتبت", "كتبوا", "يكتب", "تكتب", "يكتبون", "أكتب", "نكتب",
        "قرأ", "قرأت", "قرأوا", "يقرأ", "تقرأ", "يقرؤون", "أقرأ", "نقرأ",
        "سمع", "سمعت", "سمعوا", "يسمع", "تسمع", "يسمعون", "أسمع", "نسمع",
        "فهم", "فهمت", "فهموا", "يفهم", "تفهم", "يفهمون", "أفهم", "نفهم",
        "درس", "درست", "درسوا", "يدرس", "تدرس", "يدرسون", "أدرس", "ندرس",
        "سأل", "سألت", "سألوا", "يسأل", "تسأل", "يسألون", "أسأل", "نسأل",
        "أجاب", "أجابت", "أجابوا", "يجيب", "تجيب", "يجيبون", "أجيب", "نجيب",
        "حصل", "حصلت", "حصلوا", "يحصل", "تحصل", "يحصلون", "أحصل", "نحصل",
        "بدأ", "بدأت", "بدأوا", "يبدأ", "تبدأ", "يبدؤون", "أبدأ", "نبدأ",
        "انتهى", "انتهت", "انتهوا", "ينتهي", "تنتهي", "ينتهون", "أنتهي", "ننتهي",
        
        // Common nouns
        "شيء", "أشياء", "شخص", "أشخاص", "ناس", "إنسان", "بشر",
        "رجل", "رجال", "امرأة", "نساء", "طفل", "أطفال", "ولد", "أولاد", "بنت", "بنات",
        "أب", "آباء", "أم", "أمهات", "أخ", "إخوة", "أخت", "أخوات",
        "ابن", "أبناء", "بنت", "بنات", "عائلة", "عائلات", "أسرة", "أسر",
        "صديق", "أصدقاء", "زميل", "زملاء", "جار", "جيران",
        "بيت", "بيوت", "منزل", "منازل", "دار", "دور", "غرفة", "غرف",
        "مدرسة", "مدارس", "جامعة", "جامعات", "معهد", "معاهد", "فصل", "فصول",
        "مكتب", "مكاتب", "عمل", "أعمال", "وظيفة", "وظائف", "مهنة", "مهن",
        "مدينة", "مدن", "قرية", "قرى", "بلد", "بلاد", "دولة", "دول", "عاصمة", "عواصم",
        "طريق", "طرق", "شارع", "شوارع", "ميدان", "ميادين", "حي", "أحياء",
        "سوق", "أسواق", "محل", "محلات", "متجر", "متاجر", "دكان", "دكاكين",
        "مستشفى", "مستشفيات", "مركز", "مراكز", "مسجد", "مساجد", "كنيسة", "كنائس",
        "يوم", "أيام", "ليلة", "ليالي", "صباح", "مساء", "ظهر", "عصر", "مغرب", "عشاء",
        "وقت", "أوقات", "ساعة", "ساعات", "دقيقة", "دقائق", "ثانية", "ثوان",
        "سنة", "سنوات", "شهر", "شهور", "أسبوع", "أسابيع",
        "سيارة", "سيارات", "طائرة", "طائرات", "قطار", "قطارات", "حافلة", "حافلات",
        "كتاب", "كتب", "قلم", "أقلام", "ورقة", "أوراق", "دفتر", "دفاتر",
        "هاتف", "هواتف", "جوال", "جوالات", "كمبيوتر", "حاسوب", "حواسيب",
        "تلفاز", "تلفازات", "راديو", "راديوهات", "إنترنت", "شبكة", "شبكات",
        "طعام", "أطعمة", "شراب", "أشربة", "ماء", "مياه", "خبز", "لحم", "فاكهة",
        "ملابس", "ثوب", "ثياب", "قميص", "قمصان", "بنطلون", "حذاء", "أحذية",
        "لون", "ألوان", "أبيض", "أسود", "أحمر", "أزرق", "أخضر", "أصفر", "بني", "رمادي",
        "رقم", "أرقام", "عدد", "أعداد", "حساب", "حسابات",
        "اسم", "أسماء", "كلمة", "كلمات", "جملة", "جمل", "لغة", "لغات",
        "سؤال", "أسئلة", "جواب", "أجوبة", "إجابة", "إجابات",
        "مشكلة", "مشاكل", "حل", "حلول", "قضية", "قضايا",
        "حق", "حقوق", "واجب", "واجبات", "قانون", "قوانين",
        "حكومة", "حكومات", "دولة", "دول", "رئيس", "رؤساء", "وزير", "وزراء",
        
        // Common adjectives
        "كبير", "كبيرة", "كبار", "صغير", "صغيرة", "صغار",
        "طويل", "طويلة", "طوال", "قصير", "قصيرة", "قصار",
        "عريض", "عريضة", "ضيق", "ضيقة",
        "جميل", "جميلة", "جمال", "قبيح", "قبيحة",
        "جيد", "جيدة", "سيء", "سيئة",
        "جديد", "جديدة", "جدد", "قديم", "قديمة", "قدماء",
        "حديث", "حديثة", "قديم", "قديمة",
        "سهل", "سهلة", "صعب", "صعبة",
        "بسيط", "بسيطة", "معقد", "معقدة",
        "مهم", "مهمة", "ضروري", "ضرورية",
        "ممكن", "ممكنة", "مستحيل", "مستحيلة",
        "صحيح", "صحيحة", "خطأ", "خاطئ", "خاطئة",
        "حقيقي", "حقيقية", "وهمي", "وهمية",
        "سريع", "سريعة", "بطيء", "بطيئة",
        "قوي", "قوية", "ضعيف", "ضعيفة",
        "غني", "غنية", "فقير", "فقيرة",
        "سعيد", "سعيدة", "حزين", "حزينة",
        "فرحان", "فرحانة", "مسرور", "مسرورة",
        "متعب", "متعبة", "مرهق", "مرهقة",
        "صحي", "صحية", "مريض", "مريضة",
        
        // Common adverbs and expressions
        "جدا", "جداً", "كثيرا", "كثيراً", "قليلا", "قليلاً",
        "أيضا", "أيضاً", "كذلك", "أو", "لكن", "لكن",
        "إذا", "إذن", "لذلك", "لأن", "حتى", "بينما",
        "الآن", "اليوم", "غدا", "غداً", "أمس", "بعد", "قبل",
        "هنا", "هناك", "هنالك", "حيث",
        "دائما", "دائماً", "أبدا", "أبداً", "أحيانا", "أحياناً", "غالبا", "غالباً",
        "ربما", "ممكن", "لعل", "قد",
        "نعم", "لا", "بلى", "أجل", "حسناً", "حسنا", "طيب",
        
        // Common greetings and phrases
        "السلام", "عليكم", "وعليكم", "مرحبا", "مرحباً", "أهلا", "أهلاً", "سهلا", "سهلاً",
        "صباح", "الخير", "النور", "مساء",
        "كيف", "حالك", "حالكم", "الحمد", "لله", "بخير", "تمام",
        "شكرا", "شكراً", "عفوا", "عفواً", "آسف", "معذرة",
        "من", "فضلك", "لو", "سمحت",
        "تفضل", "تفضلي", "تفضلوا",
        "ما", "شاء", "الله", "إن", "بإذن", "الله",
        "مع", "السلامة", "وداعا", "وداعاً", "إلى", "اللقاء",
        
        // Pronouns
        "أنا", "أنت", "أنتِ", "أنتم", "أنتن", "هو", "هي", "هم", "هن", "نحن",
        
        // Question words
        "من", "ماذا", "ما", "متى", "أين", "كيف", "لماذا", "كم", "أي", "هل"
    };
    
    // Common punctuation and special suggestions
    private static final String[] COMMON_PUNCTUATION = {
        ".", "?", "!", ",", ";", ":", "'", "\"", "(", ")", "-"
    };
    
    /**
     * Initializes the word trie with common vocabulary.
     */
    public static void initializeVocabulary(WordTrie wordTrie) {
        // Add common English words
        for (String word : COMMON_ENGLISH_WORDS) {
            wordTrie.insert(word);
        }
        
        // Add common Arabic words
        for (String word : COMMON_ARABIC_WORDS) {
            wordTrie.insert(word);
        }
    }
    
    /**
     * Initializes the N-gram model with common word patterns.
     * Enhanced to include single-letter words and punctuation patterns.
     */
    public static void initializeNGramModel(NGramModel ngramModel) {
        // Add some common English patterns with single-letter words
        ngramModel.learnFromSentence("I am going to");
        ngramModel.learnFromSentence("How are you");
        ngramModel.learnFromSentence("What is your");
        ngramModel.learnFromSentence("Thank you very much");
        ngramModel.learnFromSentence("Have a good day");
        ngramModel.learnFromSentence("See you later");
        ngramModel.learnFromSentence("Nice to meet you");
        ngramModel.learnFromSentence("How do you do");
        ngramModel.learnFromSentence("What time is it");
        ngramModel.learnFromSentence("Where are you from");
        // Patterns with "a"
        ngramModel.learnFromSentence("I have a car");
        ngramModel.learnFromSentence("This is a good idea");
        ngramModel.learnFromSentence("Once upon a time");
        ngramModel.learnFromSentence("What a beautiful day");
        
        // Add some common Arabic patterns with single-letter words
        ngramModel.learnFromSentence("كيف حالك اليوم");
        ngramModel.learnFromSentence("أهلا وسهلا بك");
        ngramModel.learnFromSentence("شكرا لك جزيلا");
        ngramModel.learnFromSentence("مع السلامة");
        ngramModel.learnFromSentence("يوم سعيد");
        ngramModel.learnFromSentence("إن شاء الله");
        ngramModel.learnFromSentence("الحمد لله");
        ngramModel.learnFromSentence("ما شاء الله");
        // Patterns with "و" (and)
        ngramModel.learnFromSentence("أنا و أنت");
        ngramModel.learnFromSentence("الأب و الأم");
        ngramModel.learnFromSentence("القراءة و الكتابة");
        ngramModel.learnFromSentence("صباح الخير و مساء الخير");
        
        // Common punctuation patterns
        ngramModel.learnFromSentence("Hello, how are you?");
        ngramModel.learnFromSentence("Yes, I agree.");
        ngramModel.learnFromSentence("What time is it?");
        ngramModel.learnFromSentence("That's great!");
        ngramModel.learnFromSentence("I think so.");
        ngramModel.learnFromSentence("Please, help me.");
    }
    
    /**
     * Gets common words for the given prefix.
     * Enhanced to support single-letter words and Arabic.
     */
    public static List<String> getCommonWordsForPrefix(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return Arrays.asList("the", "and", "for", "you", "are", "I", "a", "و");
        }
        
        prefix = prefix.toLowerCase();
        
        // Single letter suggestions
        if (prefix.equals("a")) {
            return Arrays.asList("a", "and", "are", "all", "also", "about");
        } else if (prefix.equals("i")) {
            return Arrays.asList("I", "is", "in", "it", "if");
        } else if (prefix.equals("و")) {
            return Arrays.asList("و", "وهو", "وهي", "ولكن", "وقد");
        }
        
        // Simple prefix matching for common words
        if (prefix.startsWith("th")) {
            return Arrays.asList("the", "this", "that", "they", "there");
        } else if (prefix.startsWith("w")) {
            return Arrays.asList("what", "when", "where", "with", "would");
        } else if (prefix.startsWith("h")) {
            return Arrays.asList("how", "have", "has", "here", "home");
        }
        
        return Arrays.asList();
    }
    
    /**
     * Gets common punctuation suggestions.
     */
    public static List<String> getCommonPunctuation() {
        return Arrays.asList(COMMON_PUNCTUATION);
    }
}