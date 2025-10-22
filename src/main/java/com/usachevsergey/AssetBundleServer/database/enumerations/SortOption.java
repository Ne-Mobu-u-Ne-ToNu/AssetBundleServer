package com.usachevsergey.AssetBundleServer.database.enumerations;

public enum SortOption {
    NAME,
    DATE_ASC,
    DATE_DESC,
    POPULARITY_ASC,
    POPULARITY_DESC;

    public static SortOption getFromString(String option) {
        try {
            return SortOption.valueOf(option.toUpperCase());
        } catch (IllegalArgumentException e) {
            return NAME;
        }
    }
}
