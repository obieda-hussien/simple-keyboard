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

package rkr.simplekeyboard.inputmethod.latin.gesture;

import android.graphics.PointF;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Swipe/Gesture typing recognizer that converts continuous touch paths into words.
 * Implements basic gesture recognition for glide typing (similar to Gboard's swipe feature).
 */
public class SwipeGestureRecognizer {
    
    // Gesture sampling parameters
    private static final int MIN_POINTS = 5;
    private static final float MIN_DISTANCE = 10.0f; // pixels
    
    // Key layout information (simplified QWERTY)
    private final Map<Character, PointF> keyPositions;
    private final float keyWidth;
    private final float keyHeight;
    
    /**
     * Represents a gesture path for swipe typing.
     */
    public static class GesturePath {
        private final List<PointF> points;
        private long startTime;
        private long endTime;
        
        public GesturePath() {
            this.points = new ArrayList<>();
            this.startTime = System.currentTimeMillis();
        }
        
        public void addPoint(float x, float y) {
            points.add(new PointF(x, y));
        }
        
        public void finish() {
            this.endTime = System.currentTimeMillis();
        }
        
        public List<PointF> getPoints() {
            return points;
        }
        
        public int getPointCount() {
            return points.size();
        }
        
        public long getDuration() {
            return endTime - startTime;
        }
        
        public boolean isValid() {
            return points.size() >= MIN_POINTS;
        }
    }
    
    /**
     * Constructor with keyboard layout information.
     */
    public SwipeGestureRecognizer(float keyWidth, float keyHeight) {
        this.keyWidth = keyWidth;
        this.keyHeight = keyHeight;
        this.keyPositions = initializeKeyPositions();
    }
    
    /**
     * Initializes the positions of keys on a standard QWERTY keyboard.
     */
    private Map<Character, PointF> initializeKeyPositions() {
        Map<Character, PointF> positions = new HashMap<>();
        
        // Row 1 (QWERTYUIOP)
        String row1 = "qwertyuiop";
        for (int i = 0; i < row1.length(); i++) {
            positions.put(row1.charAt(i), new PointF(
                i * keyWidth + keyWidth / 2,
                keyHeight / 2
            ));
        }
        
        // Row 2 (ASDFGHJKL)
        String row2 = "asdfghjkl";
        for (int i = 0; i < row2.length(); i++) {
            positions.put(row2.charAt(i), new PointF(
                (i + 0.5f) * keyWidth + keyWidth / 2,
                keyHeight * 1.5f
            ));
        }
        
        // Row 3 (ZXCVBNM)
        String row3 = "zxcvbnm";
        for (int i = 0; i < row3.length(); i++) {
            positions.put(row3.charAt(i), new PointF(
                (i + 1.5f) * keyWidth + keyWidth / 2,
                keyHeight * 2.5f
            ));
        }
        
        return positions;
    }
    
    /**
     * Recognizes a word from a gesture path.
     * Returns list of candidate words based on the swipe pattern.
     */
    public List<String> recognizeGesture(GesturePath gesturePath, List<String> dictionary) {
        List<String> candidates = new ArrayList<>();
        
        if (!gesturePath.isValid() || dictionary == null || dictionary.isEmpty()) {
            return candidates;
        }
        
        // Extract key sequence from gesture path
        String keySequence = extractKeySequence(gesturePath);
        
        if (keySequence == null || keySequence.isEmpty()) {
            return candidates;
        }
        
        // Find words that match the gesture pattern
        for (String word : dictionary) {
            if (matchesGesture(word, keySequence, gesturePath)) {
                candidates.add(word);
            }
        }
        
        return candidates;
    }
    
    /**
     * Extracts the sequence of keys that the gesture path passes through.
     */
    private String extractKeySequence(GesturePath gesturePath) {
        StringBuilder sequence = new StringBuilder();
        List<PointF> points = gesturePath.getPoints();
        
        if (points.isEmpty()) {
            return "";
        }
        
        // Sample points to reduce noise
        List<PointF> sampledPoints = samplePoints(points);
        
        // Find nearest key for each sampled point
        Character lastKey = null;
        for (PointF point : sampledPoints) {
            Character nearestKey = findNearestKey(point);
            if (nearestKey != null && !nearestKey.equals(lastKey)) {
                sequence.append(nearestKey);
                lastKey = nearestKey;
            }
        }
        
        return sequence.toString();
    }
    
    /**
     * Samples points from gesture to reduce noise and smooth the path.
     */
    private List<PointF> samplePoints(List<PointF> points) {
        List<PointF> sampled = new ArrayList<>();
        
        if (points.isEmpty()) {
            return sampled;
        }
        
        sampled.add(points.get(0)); // Always include first point
        
        PointF lastSampled = points.get(0);
        for (int i = 1; i < points.size(); i++) {
            PointF current = points.get(i);
            float distance = distance(lastSampled, current);
            
            if (distance >= MIN_DISTANCE) {
                sampled.add(current);
                lastSampled = current;
            }
        }
        
        // Always include last point
        if (!sampled.contains(points.get(points.size() - 1))) {
            sampled.add(points.get(points.size() - 1));
        }
        
        return sampled;
    }
    
    /**
     * Finds the nearest key to a given point.
     */
    private Character findNearestKey(PointF point) {
        Character nearest = null;
        float minDistance = Float.MAX_VALUE;
        
        for (Map.Entry<Character, PointF> entry : keyPositions.entrySet()) {
            float dist = distance(point, entry.getValue());
            if (dist < minDistance) {
                minDistance = dist;
                nearest = entry.getKey();
            }
        }
        
        return nearest;
    }
    
    /**
     * Checks if a word matches the gesture pattern.
     */
    private boolean matchesGesture(String word, String keySequence, GesturePath path) {
        if (word == null || word.isEmpty() || keySequence.isEmpty()) {
            return false;
        }
        
        String lowerWord = word.toLowerCase();
        
        // Basic matching: check if word's characters appear in key sequence in order
        int wordIndex = 0;
        int seqIndex = 0;
        
        while (wordIndex < lowerWord.length() && seqIndex < keySequence.length()) {
            if (lowerWord.charAt(wordIndex) == keySequence.charAt(seqIndex)) {
                wordIndex++;
                seqIndex++;
            } else {
                seqIndex++;
            }
        }
        
        // Word matches if all characters were found
        return wordIndex == lowerWord.length();
    }
    
    /**
     * Calculates Euclidean distance between two points.
     */
    private float distance(PointF p1, PointF p2) {
        float dx = p2.x - p1.x;
        float dy = p2.y - p1.y;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * Scores how well a word matches the gesture path.
     * Higher score means better match.
     */
    public double scoreGestureMatch(String word, GesturePath gesturePath) {
        if (word == null || word.isEmpty() || !gesturePath.isValid()) {
            return 0.0;
        }
        
        String keySequence = extractKeySequence(gesturePath);
        
        // Calculate match score based on:
        // 1. How many word characters appear in sequence
        // 2. Ratio of word length to sequence length
        // 3. Gesture smoothness
        
        double score = 0.0;
        String lowerWord = word.toLowerCase();
        
        // Character coverage score
        int matches = 0;
        int seqIndex = 0;
        for (int i = 0; i < lowerWord.length() && seqIndex < keySequence.length(); i++) {
            while (seqIndex < keySequence.length()) {
                if (lowerWord.charAt(i) == keySequence.charAt(seqIndex)) {
                    matches++;
                    seqIndex++;
                    break;
                }
                seqIndex++;
            }
        }
        
        double coverage = (double) matches / lowerWord.length();
        score += coverage * 100.0;
        
        // Length similarity score
        double lengthRatio = Math.min(lowerWord.length(), keySequence.length()) / 
                           (double) Math.max(lowerWord.length(), keySequence.length());
        score += lengthRatio * 50.0;
        
        return score;
    }
}
