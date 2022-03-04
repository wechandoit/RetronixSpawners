package net.wechandoit.src.utils;

import java.util.Objects;
import java.util.UUID;

public class Java9Utils {

    /* All code from here is directly from JDK9.
     *  @author https://github.com/AdoptOpenJDK/openjdk-jdk9
     */

    /**
     * Creates a {@code UUID} from the string standard representation as
     * described in the {@link #toString} method.
     *
     * @param  name
     *         A string that specifies a {@code UUID}
     *
     * @return  A {@code UUID} with the specified value
     *
     * @throws  IllegalArgumentException
     *          If name does not conform to the string representation as
     *          described in {@link #toString}
     *
     */
    public static UUID getUUIDfromString(String name) {
        int len = name.length();
        if (len > 36) {
            throw new IllegalArgumentException("UUID string too large");
        }

        int dash1 = name.indexOf('-', 0);
        int dash2 = name.indexOf('-', dash1 + 1);
        int dash3 = name.indexOf('-', dash2 + 1);
        int dash4 = name.indexOf('-', dash3 + 1);
        int dash5 = name.indexOf('-', dash4 + 1);

        // For any valid input, dash1 through dash4 will be positive and dash5
        // negative, but it's enough to check dash4 and dash5:
        // - if dash1 is -1, dash4 will be -1
        // - if dash1 is positive but dash2 is -1, dash4 will be -1
        // - if dash1 and dash2 is positive, dash3 will be -1, dash4 will be
        //   positive, but so will dash5
        if (dash4 < 0 || dash5 >= 0) {
            throw new IllegalArgumentException("Invalid UUID string: " + name);
        }

        long mostSigBits = parseLong(name, 0, dash1, 16) & 0xffffffffL;
        mostSigBits <<= 16;
        mostSigBits |= parseLong(name, dash1 + 1, dash2, 16) & 0xffffL;
        mostSigBits <<= 16;
        mostSigBits |= parseLong(name, dash2 + 1, dash3, 16) & 0xffffL;
        long leastSigBits = parseLong(name, dash3 + 1, dash4, 16) & 0xffffL;
        leastSigBits <<= 48;
        leastSigBits |= parseLong(name, dash4 + 1, len, 16) & 0xffffffffffffL;

        return new UUID(mostSigBits, leastSigBits);
    }

    /**
     * Parses the {@link CharSequence} argument as a signed {@code long} in
     * the specified {@code radix}, beginning at the specified
     * {@code beginIndex} and extending to {@code endIndex - 1}.
     *
     * <p>The method does not take steps to guard against the
     * {@code CharSequence} being mutated while parsing.
     *
     * @param      s   the {@code CharSequence} containing the {@code long}
     *                  representation to be parsed
     * @param      beginIndex   the beginning index, inclusive.
     * @param      endIndex     the ending index, exclusive.
     * @param      radix   the radix to be used while parsing {@code s}.
     * @return     the signed {@code long} represented by the subsequence in
     *             the specified radix.
     * @throws     NullPointerException  if {@code s} is null.
     * @throws     IndexOutOfBoundsException  if {@code beginIndex} is
     *             negative, or if {@code beginIndex} is greater than
     *             {@code endIndex} or if {@code endIndex} is greater than
     *             {@code s.length()}.
     * @throws     NumberFormatException  if the {@code CharSequence} does not
     *             contain a parsable {@code int} in the specified
     *             {@code radix}, or if {@code radix} is either smaller than
     *             {@link Character#MIN_RADIX} or larger than
     *             {@link Character#MAX_RADIX}.
     * @since  9
     */
    public static long parseLong(CharSequence s, int beginIndex, int endIndex, int radix)
            throws NumberFormatException {
        s = Objects.requireNonNull(s);

        if (beginIndex < 0 || beginIndex > endIndex || endIndex > s.length()) {
            throw new IndexOutOfBoundsException();
        }
        if (radix < Character.MIN_RADIX) {
            throw new NumberFormatException("radix " + radix +
                    " less than Character.MIN_RADIX");
        }
        if (radix > Character.MAX_RADIX) {
            throw new NumberFormatException("radix " + radix +
                    " greater than Character.MAX_RADIX");
        }

        boolean negative = false;
        int i = beginIndex;
        long limit = -Long.MAX_VALUE;

        if (i < endIndex) {
            char firstChar = s.charAt(i);
            if (firstChar < '0') { // Possible leading "+" or "-"
                if (firstChar == '-') {
                    negative = true;
                    limit = Long.MIN_VALUE;
                } else if (firstChar != '+') {
                    throw new NumberFormatException("Error at index "
                            + (i - beginIndex) + " in: \""
                            + s.subSequence(beginIndex, endIndex) + "\"");
                }
                i++;
            }
            if (i >= endIndex) { // Cannot have lone "+", "-" or ""
                throw new NumberFormatException("Error at index "
                        + (i - beginIndex) + " in: \""
                        + s.subSequence(beginIndex, endIndex) + "\"");
            }
            long multmin = limit / radix;
            long result = 0;
            while (i < endIndex) {
                // Accumulating negatively avoids surprises near MAX_VALUE
                int digit = Character.digit(s.charAt(i), radix);
                if (digit < 0 || result < multmin) {
                    throw new NumberFormatException("Error at index "
                            + (i - beginIndex) + " in: \""
                            + s.subSequence(beginIndex, endIndex) + "\"");
                }
                result *= radix;
                if (result < limit + digit) {
                    throw new NumberFormatException("Error at index "
                            + (i - beginIndex) + " in: \""
                            + s.subSequence(beginIndex, endIndex) + "\"");
                }
                i++;
                result -= digit;
            }
            return negative ? result : -result;
        } else {
            throw new NumberFormatException("");
        }
    }

}
