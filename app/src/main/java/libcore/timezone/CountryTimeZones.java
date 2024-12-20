/*
 * Copyright (C) 2017 The Android Open Source Project
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

package libcore.timezone;

import android.icu.util.TimeZone;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Information about a country's time zones.
 * @hide
 */

public final class CountryTimeZones {

    /**
     * The result of lookup up a time zone using offset information (and possibly more).
     * @hide
     */

    public static final class OffsetResult {

        /** A zone that matches the supplied criteria. See also {@link #isOnlyMatch}. */
        private final TimeZone timeZone;

        /** True if there is one match for the supplied criteria */
        private final boolean isOnlyMatch;

        public OffsetResult(TimeZone timeZone, boolean isOnlyMatch) {
            this.timeZone = java.util.Objects.requireNonNull(timeZone);
            this.isOnlyMatch = isOnlyMatch;
        }

        public TimeZone getTimeZone() {
            return timeZone;
        }

        public boolean isOnlyMatch() {
            return isOnlyMatch;
        }

        @Override
        public String toString() {
            return "Result{"
                    + "timeZone='" + timeZone + '\''
                    + ", isOnlyMatch=" + isOnlyMatch
                    + '}';
        }
    }

    /**
     * A mapping to a time zone ID with some associated metadata.
     *
     * @hide
     */
    public static final class TimeZoneMapping {
        private final String timeZoneId;
        private final boolean shownInPicker;
        private final Long notUsedAfter;

        /** Memoized TimeZone object for {@link #timeZoneId}. */
        private TimeZone timeZone;

        TimeZoneMapping(String timeZoneId, boolean shownInPicker, Long notUsedAfter) {
            this.timeZoneId = Objects.requireNonNull(timeZoneId);
            this.shownInPicker = shownInPicker;
            this.notUsedAfter = notUsedAfter;
        }

        public String getTimeZoneId() {
            return timeZoneId;
        }

        public boolean isShownInPicker() {
            return shownInPicker;
        }

        public Long getNotUsedAfter() {
            return notUsedAfter;
        }

        /**
         * Returns a {@link TimeZone} object for this mapping, or {@code null} if the ID is unknown.
         */
        public TimeZone getTimeZone() {
            synchronized (this) {
                if (timeZone == null) {
                    TimeZone tz = TimeZone.getFrozenTimeZone(timeZoneId);
                    timeZone = tz;
                    if (TimeZone.UNKNOWN_ZONE_ID.equals(timeZone.getID())) {
                        // This shouldn't happen given the validation that takes place in
                        // createValidatedCountryTimeZones().
                        throw new IllegalStateException("Invalid zone in TimeZoneMapping: " + this);
                    }
                }
            }

            return TimeZone.UNKNOWN_ZONE_ID.equals(timeZone.getID()) ? null : timeZone;
        }

        /**
         * Returns {@code true} if the mapping is "effective" after {@code whenMillis}, i.e.
         * it is distinct from other "effective" times zones used in the country at/after that
         * time. This uses the {@link #notUsedAfter} metadata which ensures there is one time
         * zone remaining when there are multiple candidate zones with the same rules. The one
         * kept is based on country specific factors like population covered.
         */
        boolean isEffectiveAt(long whenMillis) {
            return notUsedAfter == null || whenMillis <= notUsedAfter;
        }

        // VisibleForTesting
        public static TimeZoneMapping createForTests(
                String timeZoneId, boolean showInPicker, Long notUsedAfter) {
            return new TimeZoneMapping(timeZoneId, showInPicker, notUsedAfter);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            TimeZoneMapping that = (TimeZoneMapping) o;
            return shownInPicker == that.shownInPicker &&
                    Objects.equals(timeZoneId, that.timeZoneId) &&
                    Objects.equals(notUsedAfter, that.notUsedAfter);
        }

        @Override
        public int hashCode() {
            return Objects.hash(timeZoneId, shownInPicker, notUsedAfter);
        }

        @Override
        public String toString() {
            return "TimeZoneMapping{"
                    + "timeZoneId='" + timeZoneId + '\''
                    + ", shownInPicker=" + shownInPicker
                    + ", notUsedAfter=" + notUsedAfter
                    + '}';
        }

        /**
         * Returns {@code true} if one of the supplied {@link TimeZoneMapping} objects is for the
         * specified time zone ID.
         */
        static boolean containsTimeZoneId(
                List<TimeZoneMapping> timeZoneMappings, String timeZoneId) {
            for (TimeZoneMapping timeZoneMapping : timeZoneMappings) {
                if (timeZoneMapping.timeZoneId.equals(timeZoneId)) {
                    return true;
                }
            }
            return false;
        }
    }

    private final String countryIso;
    private final String defaultTimeZoneId;
    /**
     * {@code true} indicates the default time zone for a country is a good choice if a time zone
     * cannot be determined by other means.
     */
    private final boolean defaultTimeZoneBoosted;
    private final List<TimeZoneMapping> timeZoneMappings;
    private final boolean everUsesUtc;

    /**
     * Memoized frozen ICU TimeZone object for the default. Can be {@link TimeZone#UNKNOWN_ZONE} if
     * the {@link #defaultTimeZoneId} is missing or unrecognized.
     */
    private TimeZone defaultTimeZone;

    private CountryTimeZones(String countryIso, String defaultTimeZoneId,
            boolean defaultTimeZoneBoosted, boolean everUsesUtc,
            List<TimeZoneMapping> timeZoneMappings) {
        this.countryIso = java.util.Objects.requireNonNull(countryIso);
        this.defaultTimeZoneId = defaultTimeZoneId;
        this.defaultTimeZoneBoosted = defaultTimeZoneBoosted;
        this.everUsesUtc = everUsesUtc;
        // Create a defensive copy of the mapping list.
        this.timeZoneMappings = Collections.unmodifiableList(new ArrayList<>(timeZoneMappings));
    }

    /**
     * Creates a {@link CountryTimeZones} object containing only known time zone IDs.
     */
    public static CountryTimeZones createValidated(String countryIso, String defaultTimeZoneId,
            boolean defaultTimeZoneBoosted, boolean everUsesUtc,
            List<TimeZoneMapping> timeZoneMappings, String debugInfo) {

        // We rely on ZoneInfoDB to tell us what the known valid time zone IDs are. ICU may
        // recognize more but we want to be sure that zone IDs can be used with java.util as well as
        // android.icu and ICU is expected to have a superset.
//        String[] validTimeZoneIdsArray = ZoneInfoDb.getInstance().getAvailableIDs();
        String[] validTimeZoneIdsArray = null;
        HashSet<String> validTimeZoneIdsSet = new HashSet<>(Arrays.asList(validTimeZoneIdsArray));
        List<TimeZoneMapping> validCountryTimeZoneMappings = new ArrayList<>();
        for (TimeZoneMapping timeZoneMapping : timeZoneMappings) {
            String timeZoneId = timeZoneMapping.timeZoneId;
            if (!validTimeZoneIdsSet.contains(timeZoneId)) {
//                System.logW("Skipping invalid zone: " + timeZoneId + " at " + debugInfo);
            } else {
                validCountryTimeZoneMappings.add(timeZoneMapping);
            }
        }

        // We don't get too strict at runtime about whether the defaultTimeZoneId must be
        // one of the country's time zones because this is the data we have to use (we also
        // assume the data was validated by earlier steps). The default time zone ID must just
        // be a recognized zone ID: if it's not valid we leave it null.
        if (!validTimeZoneIdsSet.contains(defaultTimeZoneId)) {
//            System.logW("Invalid default time zone ID: " + defaultTimeZoneId
//                    + " at " + debugInfo);
            defaultTimeZoneId = null;
        }

        String normalizedCountryIso = normalizeCountryIso(countryIso);
        return new CountryTimeZones(
                normalizedCountryIso, defaultTimeZoneId, defaultTimeZoneBoosted, everUsesUtc,
                validCountryTimeZoneMappings);
    }

    /**
     * Returns the ISO code for the country.
     */
    public String getCountryIso() {
        return countryIso;
    }

    /**
     * Returns true if the ISO code for the country is a match for the one specified.
     */
    public boolean isForCountryCode(String countryIso) {
        return this.countryIso.equals(normalizeCountryIso(countryIso));
    }

    /**
     * Returns the default time zone for the country. Can return null in cases when no data is
     * available or the time zone ID provided to
     * {@link #createValidated(String, String, boolean, boolean, List, String)} was not recognized.
     */
    public synchronized TimeZone getDefaultTimeZone() {
        if (defaultTimeZone == null) {
            TimeZone timeZone;
            if (defaultTimeZoneId == null) {
                timeZone = TimeZone.UNKNOWN_ZONE;
            } else {
                timeZone = TimeZone.getFrozenTimeZone(defaultTimeZoneId);
            }
            this.defaultTimeZone = timeZone;
        }
        return TimeZone.UNKNOWN_ZONE_ID.equals(defaultTimeZone.getID()) ? null : defaultTimeZone;
    }

    /**
     * Returns the default time zone ID for the country. Can return null in cases when no data is
     * available or the time zone ID provided to
     * {@link #createValidated(String, String, boolean, boolean, List, String)} was not recognized.
     */

    public String getDefaultTimeZoneId() {
        return defaultTimeZoneId;
    }

    /**
     * Qualifier for a country's default time zone. {@code true} indicates whether the default
     * would be a good choice <em>generally</em> when there's no other information available.
     */

    public boolean isDefaultTimeZoneBoosted() {
        return defaultTimeZoneBoosted;
    }

    /**
     * Returns an immutable, ordered list of time zone mappings for the country in an undefined but
     * "priority" order. The list can be empty if there were no zones configured or the configured
     * zone IDs were not recognized.
     */
    public List<TimeZoneMapping> getTimeZoneMappings() {
        return timeZoneMappings;
    }

    /**
     * Returns an immutable, ordered list of time zone mappings for the country in an undefined but
     * "priority" order, filtered so that only "effective" time zone IDs are returned. An
     * "effective" time zone is one that differs from another time zone used in the country after
     * {@code whenMillis}. The list can be empty if there were no zones configured or the configured
     * zone IDs were not recognized.
     */

    public List<TimeZoneMapping> getEffectiveTimeZoneMappingsAt(long whenMillis) {
        ArrayList<TimeZoneMapping> filteredList = new ArrayList<>(timeZoneMappings.size());
        for (TimeZoneMapping timeZoneMapping : timeZoneMappings) {
            if (timeZoneMapping.isEffectiveAt(whenMillis)) {
                filteredList.add(timeZoneMapping);
            }
        }
        return Collections.unmodifiableList(filteredList);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CountryTimeZones that = (CountryTimeZones) o;
        return defaultTimeZoneBoosted == that.defaultTimeZoneBoosted
                && everUsesUtc == that.everUsesUtc
                && countryIso.equals(that.countryIso)
                && Objects.equals(defaultTimeZoneId, that.defaultTimeZoneId)
                && timeZoneMappings.equals(that.timeZoneMappings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                countryIso, defaultTimeZoneId, defaultTimeZoneBoosted, timeZoneMappings,
                everUsesUtc);
    }

    @Override
    public String toString() {
        return "CountryTimeZones{"
                + "countryIso='" + countryIso + '\''
                + ", defaultTimeZoneId='" + defaultTimeZoneId + '\''
                + ", defaultTimeZoneBoosted=" + defaultTimeZoneBoosted
                + ", timeZoneMappings=" + timeZoneMappings
                + ", everUsesUtc=" + everUsesUtc
                + '}';
    }

    /**
     * Returns true if the country has at least one zone that is the same as UTC at the given time.
     */

    public boolean hasUtcZone(long whenMillis) {
        // If the data tells us the country never uses UTC we don't have to check anything.
        if (!everUsesUtc) {
            return false;
        }

        for (TimeZoneMapping timeZoneMapping : getEffectiveTimeZoneMappingsAt(whenMillis)) {
            TimeZone timeZone = timeZoneMapping.getTimeZone();
            if (timeZone != null && timeZone.getOffset(whenMillis) == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a time zone for the country, if there is one, that matches the supplied properties.
     * If there are multiple matches and the {@code bias} is one of them then it is returned,
     * otherwise an arbitrary match is returned based on the {@link
     * #getEffectiveTimeZoneMappingsAt(long)} ordering.
     *
     * @param whenMillis the UTC time to match against
     * @param bias the time zone to prefer, can be {@code null} to indicate there is no preference
     * @param totalOffsetMillis the offset from UTC at {@code whenMillis}
     * @param isDst the Daylight Savings Time state at {@code whenMillis}. {@code true} means DST,
     *     {@code false} means not DST
     * @return an {@link OffsetResult} with information about a matching zone, or {@code null} if
     *     there is no match
     */

    public OffsetResult lookupByOffsetWithBias(long whenMillis, TimeZone bias,
            int totalOffsetMillis, boolean isDst) {
        return lookupByOffsetWithBiasInternal(whenMillis, bias, totalOffsetMillis, isDst);
    }

    /**
     * Returns a time zone for the country, if there is one, that matches the supplied properties.
     * If there are multiple matches and the {@code bias} is one of them then it is returned,
     * otherwise an arbitrary match is returned based on the {@link
     * #getEffectiveTimeZoneMappingsAt(long)} ordering.
     *
     * @param whenMillis the UTC time to match against
     * @param bias the time zone to prefer, can be {@code null} to indicate there is no preference
     * @param totalOffsetMillis the offset from UTC at {@code whenMillis}
     * @return an {@link OffsetResult} with information about a matching zone, or {@code null} if
     *     there is no match
     */

    public OffsetResult lookupByOffsetWithBias(long whenMillis, TimeZone bias,
            int totalOffsetMillis) {
        final Boolean isDst = null;
        return lookupByOffsetWithBiasInternal(whenMillis, bias, totalOffsetMillis, isDst);
    }

    /**
     * Returns a time zone for the country, if there is one, that matches the supplied properties.
     * If there are multiple matches and the {@code bias} is one of them then it is returned,
     * otherwise an arbitrary match is returned based on the {@link
     * #getEffectiveTimeZoneMappingsAt(long)} ordering.
     *
     * @param whenMillis the UTC time to match against
     * @param bias the time zone to prefer, can be {@code null}
     * @param totalOffsetMillis the offset from UTC at {@code whenMillis}
     * @param isDst the Daylight Savings Time state at {@code whenMillis}. {@code true} means DST,
     *     {@code false} means not DST, {@code null} means unknown
     */
    private OffsetResult lookupByOffsetWithBiasInternal(long whenMillis, TimeZone bias,
            int totalOffsetMillis, Boolean isDst) {
        List<TimeZoneMapping> timeZoneMappings = getEffectiveTimeZoneMappingsAt(whenMillis);
        if (timeZoneMappings.isEmpty()) {
            return null;
        }

        TimeZone firstMatch = null;
        boolean biasMatched = false;
        boolean oneMatch = true;
        for (TimeZoneMapping timeZoneMapping : timeZoneMappings) {
            TimeZone match = timeZoneMapping.getTimeZone();
            if (match == null
                    || !offsetMatchesAtTime(whenMillis, match, totalOffsetMillis, isDst)) {
                continue;
            }

            if (firstMatch == null) {
                firstMatch = match;
            } else {
                oneMatch = false;
            }
            if (bias != null && match.getID().equals(bias.getID())) {
                biasMatched = true;
            }
            if (firstMatch != null && !oneMatch && (bias == null || biasMatched)) {
                break;
            }
        }
        if (firstMatch == null) {
            return null;
        }

        TimeZone toReturn = biasMatched ? bias : firstMatch;
        return new OffsetResult(toReturn, oneMatch);
    }

    /**
     * Returns {@code true} if the specified {@code totalOffset} and {@code isDst} would be valid in
     * the {@code timeZone} at time {@code whenMillis}.
     * {@code totalOffetMillis} is always matched.
     * If {@code isDst} is {@code null}, this means the DST state is unknown.
     * If {@code isDst} is {@code false}, this means the zone must not be in DST.
     * If {@code isDst} is {@code true}, this means the zone must be in DST.
     */
    private static boolean offsetMatchesAtTime(long whenMillis, TimeZone timeZone,
            int totalOffsetMillis, Boolean isDst) {
        int[] offsets = new int[2];
        timeZone.getOffset(whenMillis, false /* local */, offsets);

        if (totalOffsetMillis != (offsets[0] + offsets[1])) {
            return false;
        }

        return isDst == null || (isDst == (offsets[1] != 0));
    }

    private static String normalizeCountryIso(String countryIso) {
        // Lowercase ASCII is normalized for the purposes of the code in this class.
        return countryIso.toLowerCase(Locale.US);
    }
}
