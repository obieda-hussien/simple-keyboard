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

package rkr.simplekeyboard.inputmethod.latin.utils;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class for providing email suggestions based on user contacts and common domains.
 */
public class EmailSuggestionProvider {
    private static final String TAG = EmailSuggestionProvider.class.getSimpleName();
    
    // Common email domains for domain completion
    private static final List<String> COMMON_EMAIL_DOMAINS = Arrays.asList(
        "@gmail.com",
        "@outlook.com", 
        "@yahoo.com",
        "@hotmail.com",
        "@icloud.com",
        "@live.com",
        "@aol.com",
        "@protonmail.com"
    );
    
    // Maximum number of email suggestions to return
    private static final int MAX_EMAIL_SUGGESTIONS = 5;
    
    private final Context mContext;
    private Set<String> mCachedEmails = null;
    private long mLastCacheTime = 0;
    private static final long CACHE_VALIDITY_MS = 5 * 60 * 1000; // 5 minutes

    public EmailSuggestionProvider(Context context) {
        mContext = context;
    }

    /**
     * Checks if the GET_ACCOUNTS permission is granted.
     */
    public boolean hasAccountsPermission() {
        return ContextCompat.checkSelfPermission(mContext, Manifest.permission.GET_ACCOUNTS) 
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Gets email suggestions from user's device accounts.
     * Returns empty list if permission is not granted.
     */
    public List<String> getContactEmails() {
        if (!hasAccountsPermission()) {
            Log.d(TAG, "GET_ACCOUNTS permission not granted");
            return new ArrayList<>();
        }

        // Check cache validity
        long currentTime = System.currentTimeMillis();
        if (mCachedEmails != null && (currentTime - mLastCacheTime) < CACHE_VALIDITY_MS) {
            return new ArrayList<>(mCachedEmails);
        }

        // Refresh cache
        mCachedEmails = new HashSet<>();
        mLastCacheTime = currentTime;

        try {
            AccountManager accountManager = AccountManager.get(mContext);
            Account[] accounts = accountManager.getAccounts();

            for (Account account : accounts) {
                String email = account.name;
                if (email != null && isValidEmail(email)) {
                    mCachedEmails.add(email.trim().toLowerCase());
                    if (mCachedEmails.size() >= MAX_EMAIL_SUGGESTIONS * 2) {
                        break; // Limit the number of emails we collect
                    }
                }
            }
        } catch (SecurityException e) {
            Log.w(TAG, "Permission denied while reading accounts", e);
        } catch (Exception e) {
            Log.e(TAG, "Error reading accounts", e);
        }

        return new ArrayList<>(mCachedEmails);
    }

    /**
     * Gets domain completion suggestions for text ending with "@".
     * @param textBeforeAt The text before the "@" symbol
     * @return List of complete email suggestions with common domains
     */
    public List<String> getDomainCompletions(String textBeforeAt) {
        if (textBeforeAt == null || textBeforeAt.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String cleanText = textBeforeAt.trim();
        List<String> suggestions = new ArrayList<>();
        
        for (String domain : COMMON_EMAIL_DOMAINS) {
            suggestions.add(cleanText + domain);
            if (suggestions.size() >= MAX_EMAIL_SUGGESTIONS) {
                break;
            }
        }
        
        return suggestions;
    }

    /**
     * Filters contact emails that start with the given prefix.
     * @param prefix The text to match against email addresses
     * @return List of matching email addresses
     */
    public List<String> getFilteredContactEmails(String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            return getContactEmails();
        }

        String lowerPrefix = prefix.trim().toLowerCase();
        List<String> contactEmails = getContactEmails();
        List<String> filtered = new ArrayList<>();

        for (String email : contactEmails) {
            if (email.toLowerCase().startsWith(lowerPrefix)) {
                filtered.add(email);
                if (filtered.size() >= MAX_EMAIL_SUGGESTIONS) {
                    break;
                }
            }
        }

        return filtered;
    }

    /**
     * Basic email validation.
     */
    private boolean isValidEmail(String email) {
        return email != null && 
               email.contains("@") && 
               email.indexOf("@") > 0 && 
               email.indexOf("@") < email.length() - 1 &&
               !email.startsWith("@") &&
               !email.endsWith("@");
    }

    /**
     * Clears the email cache to force refresh on next access.
     */
    public void clearCache() {
        mCachedEmails = null;
        mLastCacheTime = 0;
    }
}