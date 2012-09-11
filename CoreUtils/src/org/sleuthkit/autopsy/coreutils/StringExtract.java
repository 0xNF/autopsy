/*
 * Autopsy Forensic Browser
 *
 * Copyright 2012 Basis Technology Corp.
 * Contact: carrier <at> sleuthkit <dot> org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sleuthkit.autopsy.coreutils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.sleuthkit.autopsy.coreutils.StringExtract.StringExtractUnicodeTable.SCRIPT;

/**
 * Language and encoding aware utility to extract strings from stream of bytes
 * Currently supports UTF-16 LE, UTF-16 BE and UTF8 Latin, Cyrillic, Chinese,
 * Arabic
 *
 * TODO: - process control characters - testing: check non-printable common
 * chars sometimes extracted (font?) - handle tie better (when number of chars
 * in result is equal)
 */
public class StringExtract {

    private static final Logger logger = Logger.getLogger(StringExtract.class.getName());
    /**
     * min. number of extracted chars to qualify as string
     */
    public static final int MIN_CHARS_STRING = 4;
    private StringExtractUnicodeTable unicodeTable;
    /**
     * currently enabled scripts
     */
    private List<SCRIPT> enabledScripts;
    /**
     * supported scripts, can be overridden with enableScriptX methods
     */
    private static final List<SCRIPT> SUPPORTED_SCRIPTS =
            Arrays.asList(
            SCRIPT.LATIN_1, SCRIPT.LATIN_2, SCRIPT.ARABIC, SCRIPT.CYRILLIC, SCRIPT.HAN,
            SCRIPT.HIRAGANA, SCRIPT.KATAKANA, SCRIPT.HANGUL,
            SCRIPT.ARMENIAN, SCRIPT.BENGALI, SCRIPT.KHMER, SCRIPT.ETHIOPIC,
            SCRIPT.GEORGIAN, SCRIPT.HEBREW, SCRIPT.LAO, SCRIPT.MONGOLIAN, SCRIPT.THAI, SCRIPT.TIBETAN);
    //current total string buffer, reuse for performance
    private final StringBuilder curString = new StringBuilder();

    /**
     * Initializes the StringExtract utility Sets enabled scripts to all
     * supported ones
     */
    public StringExtract() {
        unicodeTable = StringExtractUnicodeTable.getInstance();

        if (unicodeTable == null) {
            throw new IllegalStateException("Unicode table not properly initialized, cannot instantiate StringExtract");
        }

        this.setEnabledScripts(SUPPORTED_SCRIPTS);
    }

    /**
     * Sets the enabled scripts to ones provided, resets previous setting
     *
     * @param scripts scripts to consider for when extracting strings
     */
    public final void setEnabledScripts(List<SCRIPT> scripts) {
        this.enabledScripts = scripts;
    }

    /**
     * Sets the enabled script to one provided, resets previous setting
     *
     * @param scripts script to consider for when extracting strings
     */
    public final void setEnabledScript(SCRIPT script) {

        this.enabledScripts = new ArrayList<SCRIPT>();
        this.enabledScripts.add(script);
    }

    /**
     * Check if extraction of the script is supported by the utility
     *
     * @param script script to check if supported
     * @return true if the the utility supports the extraction of the script
     */
    public static boolean isExtractionSupported(SCRIPT script) {
        return SUPPORTED_SCRIPTS.contains(script);
    }

    /**
     * Check if extraction of the script is enabled by this instance of the
     * utility.
     * For LATIN_2 (extended LATIN), enable also LATIN_1, even if it's not explicitely enabled.
     *
     * @param script script that was identified, to check if it is enabled
     * @return true if the the script extraction is enabled
     */
    public boolean isExtractionEnabled(SCRIPT script) {
        if (script.equals(SCRIPT.LATIN_1)) {
            return enabledScripts.contains(SCRIPT.LATIN_1)
                    || enabledScripts.contains(SCRIPT.LATIN_2);
        }
        else {
            return enabledScripts.contains(script);
        }

    }

    public static List<SCRIPT> getSupportedScripts() {
        return SUPPORTED_SCRIPTS;
    }

    /**
     * Runs the byte buffer through the string extractor
     *
     * @param buff
     * @param len
     * @param offset
     * @return string extraction result, with the string extracted and
     * additional info
     */
    public StringExtractResult extract(byte[] buff, int len, int offset) {
        final int buffLen = buff.length;

        int processedBytes = 0;
        int curOffset = offset;
        int startOffset = offset;
        int curStringLen = 0;

        //reset curString buffer
        curString.delete(0, curString.length());

        //keep track of first byte offset that hasn't been processed
        //(one byte past the last byte processed in by last extraction)
        int firstUnprocessedOff = offset;

        while (curOffset < buffLen) {
            //shortcut, skip processing empty bytes
            if (buff[curOffset] == 0 && curOffset + 1 < buffLen && buff[curOffset + 1] == 0) {
                curOffset += 2;
                continue;
            }

            //extract using all methods and see which one wins
            StringExtractResult resUTF16 = null;
            if (curOffset % 2 == 0) {
                StringExtractResult resUTF16En1 = extractUTF16(buff, len, curOffset, true);
                StringExtractResult resUTF16En2 = extractUTF16(buff, len, curOffset, false);
                resUTF16 = resUTF16En1.numChars > resUTF16En2.numChars ? resUTF16En1 : resUTF16En2;
            }
            //results.add(extractUTF8(buff, len, curOffset));
            StringExtractResult resUTF8 = extractUTF8(buff, len, curOffset);

            StringExtractResult resWin;
            resWin = resUTF16 != null && resUTF16.numChars > resUTF8.numChars ? resUTF16 : resUTF8;

            if (resWin.numChars >= MIN_CHARS_STRING) {
                //record string 
                if (startOffset == offset) {
                    //advance start offset where first string starts it hasn't been advanced
                    startOffset = resWin.offset;
                }
                curStringLen += resWin.numChars;
                curString.append(resWin.textString);
                curString.append("\n");
                curStringLen += resWin.numChars + 1;

                //advance
                curOffset += resWin.numBytes;
                processedBytes += resWin.numBytes;
                firstUnprocessedOff = resWin.offset + resWin.numBytes;
            } else {
                //if no encodings worked, advance 1 byte
                ++curOffset;
                //++processedBytes;
            }
        }

        //build up the result
        StringExtractResult res = new StringExtractResult();
        res.numBytes = processedBytes;
        res.numChars = curStringLen;
        res.offset = startOffset;
        res.textString = curString.toString();
        res.firstUnprocessedOff = firstUnprocessedOff; //save that of the last winning result

        return res;
    }

    private StringExtractResult extractUTF16(byte[] buff, int len, int offset, boolean endianSwap) {
        StringExtractResult res = new StringExtractResult();

        int curOffset = offset;

        final StringBuilder tempString = new StringBuilder();

        SCRIPT currentScript = SCRIPT.NONE;

        boolean inControl = false;

        //while we have 2 byte chunks
        byte[] b = new byte[2];
        while (curOffset < len - 1) {
            b[0] = buff[curOffset++];
            b[1] = buff[curOffset++];

            if (endianSwap) {
                byte temp = b[0];
                b[0] = b[1];
                b[1] = temp;
            }

            //convert the byte sequence to 2 byte char
            //ByteBuffer bb = ByteBuffer.wrap(b);
            //int byteVal = bb.getInt();
            char byteVal = (char) b[1];
            byteVal = (char) (byteVal << 8);
            byteVal += b[0];

            //skip if beyond range
            if (byteVal > StringExtractUnicodeTable.UNICODE_TABLE_SIZE - 1) {
                break;
            }

            //lookup byteVal in the unicode table
            SCRIPT scriptFound = unicodeTable.getScript(byteVal);

            if (scriptFound == SCRIPT.NONE) {
                break;
            }

            /*
             else if (scriptFound == SCRIPT.CONTROL) {
             //update bytes processed
             res.numBytes += 2;
             continue;
             } else if (inControl) {
             break;
             }*/


            final boolean isGeneric = StringExtractUnicodeTable.isGeneric(scriptFound);
            //allow generic and one of enabled scripts we locked in to
            if (isGeneric
                    || isExtractionEnabled(scriptFound)) {

                if (currentScript == SCRIPT.NONE
                        && !isGeneric) {
                    //handle case when this is the first char in the string
                    //lock into the script
                    currentScript = scriptFound;
                }
                //check if we are within the same script we are locked on to, or COMMON
                if (currentScript == scriptFound
                        || isGeneric) {
                    if (res.numChars == 0) {
                        //set the start offset of the string
                        res.offset = curOffset;
                    }
                    //update bytes processed
                    res.numBytes += 2;
                    //append the char
                    ++res.numChars;
                    tempString.append(byteVal);
                } else {
                    //bail out
                    break;
                }
            } else {
                //bail out 
                break;
            }

        } //no more data

        res.textString = tempString.toString();

        return res;
    }

    private StringExtractResult extractUTF8(byte[] buff, int len, int offset) {
        StringExtractResult res = new StringExtractResult();

        int curOffset = offset;
        int ch = 0; //character being extracted
        int chBytes; //num bytes consumed by current char (1 - 4)

        final StringBuilder tempString = new StringBuilder();

        SCRIPT currentScript = SCRIPT.NONE;

        boolean inControl = false;

        //decode and extract a character
        while (curOffset < len) {
            // based on "valid UTF-8 byte sequences" in the Unicode 5.0 book
            final int curByte = buff[curOffset] & 0xFF; //ensure we are not comparing signed bytes to ints
            if (curByte <= 0x7F) {
                chBytes = 1;
                ch = curByte;
            } else if (curByte <= 0xC1) {
                break;
            } else if (curByte <= 0xDF) {
                if (len - curOffset < 2) {
                    break;
                }
                final int curByte_1 = buff[curOffset + 1] & 0xFF;
                if (curByte_1 >= 0x80 && curByte_1 <= 0xBF) {
                    chBytes = 2;
                    ch = (((curByte & 0x1f) << 6) + (curByte_1 & 0x3f));
                } else {
                    break;
                }
            } else if (curByte == 0xE0) {
                if (len - curOffset < 3) {
                    break;
                }
                final int curByte_1 = buff[curOffset + 1] & 0xFF;
                final int curByte_2 = buff[curOffset + 2] & 0xFF;

                if (curByte_1 >= 0xA0 && curByte_1 <= 0xBF
                        && curByte_2 >= 0x80 && curByte_2 <= 0xBF) {
                    chBytes = 3;
                    ch = (((curByte & 0x0f) << 12) + ((curByte_1 & 0x3f) << 6) + (curByte_2 & 0x3f));
                } else {
                    break;
                }
            } else if (curByte <= 0xEC) {
                if (len - curOffset < 3) {
                    break;
                }
                final int curByte_1 = buff[curOffset + 1] & 0xFF;
                final int curByte_2 = buff[curOffset + 2] & 0xFF;
                if (curByte_1 >= 0x80 && curByte_1 <= 0xBF
                        && curByte_2 >= 0x80 && curByte_2 <= 0xBF) {
                    chBytes = 3;
                    ch = (((curByte & 0x0f) << 12) + ((curByte_1 & 0x3f) << 6) + (curByte_2 & 0x3f));
                } else {
                    break;
                }
            } else if (curByte == 0xED) {
                if (len - curOffset < 3) {
                    break;
                }
                final int curByte_1 = buff[curOffset + 1] & 0xFF;
                final int curByte_2 = buff[curOffset + 2] & 0xFF;
                if (curByte_1 >= 0x80 && curByte_1 <= 0x9F
                        && curByte_2 >= 0x80 && curByte_2 <= 0xBF) {
                    chBytes = 3;
                    ch = (((curByte & 0x0f) << 12) + ((curByte_1 & 0x3f) << 6) + (curByte_2 & 0x3f));
                } else {
                    break;
                }
            } else if (curByte <= 0xEF) {
                if (len - curOffset < 3) {
                    break;
                }
                final int curByte_1 = buff[curOffset + 1] & 0xFF;
                final int curByte_2 = buff[curOffset + 2] & 0xFF;
                if (curByte_1 >= 0x80 && curByte_1 <= 0xBF
                        && curByte_2 >= 0x80 && curByte_2 <= 0xBF) {
                    chBytes = 3;
                    ch = (((curByte & 0x0f) << 12) + ((curByte_1 & 0x3f) << 6) + (curByte_2 & 0x3f));
                } else {
                    break;
                }
            } else if (curByte == 0xF0) {
                if (len - curOffset < 4) {
                    break;
                }
                final int curByte_1 = buff[curOffset + 1] & 0xFF;
                final int curByte_2 = buff[curOffset + 2] & 0xFF;
                final int curByte_3 = buff[curOffset + 3] & 0xFF;
                if (curByte_1 >= 0x90 && curByte_1 <= 0xBF
                        && curByte_2 >= 0x80 && curByte_2 <= 0xBF
                        && curByte_3 >= 0x80 && curByte_3 <= 0xBF) {
                    chBytes = 4;
                    ch = (((curByte & 0x07) << 18) + ((curByte_1 & 0x3f) << 12) + ((curByte_2 & 0x3f) << 6) + (curByte_3 & 0x3f));
                } else {
                    break;
                }
            } else if (curByte <= 0xF3) {
                if (len - curOffset < 4) {
                    break;
                }
                final int curByte_1 = buff[curOffset + 1] & 0xFF;
                final int curByte_2 = buff[curOffset + 2] & 0xFF;
                final int curByte_3 = buff[curOffset + 3] & 0xFF;
                if (curByte_1 >= 0x80 && curByte_1 <= 0xBF
                        && curByte_2 >= 0x80 && curByte_2 <= 0xBF
                        && curByte_3 >= 0x80 && curByte_3 <= 0xBF) {
                    chBytes = 4;
                    ch = (((curByte & 0x07) << 18) + ((curByte_1 & 0x3f) << 12) + ((curByte_2 & 0x3f) << 6) + (curByte_3 & 0x3f));
                } else {
                    break;
                }
            } else {
                break;
            }


            curOffset += chBytes;

            //skip if beyond range
            if (ch > StringExtractUnicodeTable.UNICODE_TABLE_SIZE - 1) {
                break;
            }

            //lookup byteVal in the unicode table
            SCRIPT scriptFound = unicodeTable.getScript(ch);

            if (scriptFound == SCRIPT.NONE) {
                break;
            }

            /*else if (scriptFound == SCRIPT.CONTROL) {
             //update bytes processed
             res.numBytes += chBytes;
             continue;
             } else if (inControl) {
             break;
             }*/

            final boolean isGeneric = StringExtractUnicodeTable.isGeneric(scriptFound);
            //allow generic and one of enabled scripts we locked in to
            if (isGeneric
                    || isExtractionEnabled(scriptFound)) {

                if (currentScript == SCRIPT.NONE
                        && !isGeneric) {
                    //handle case when this is the first char in the string
                    //lock into the script
                    currentScript = scriptFound;
                }
                //check if we are within the same script we are locked on to, or COMMON
                if (currentScript == scriptFound
                        || isGeneric) {
                    if (res.numChars == 0) {
                        //set the start byte offset of the string
                        res.offset = curOffset;
                    }
                    //update bytes processed
                    res.numBytes += chBytes;
                    //append the char
                    ++res.numChars;
                    tempString.append((char) ch);
                } else {
                    //bail out
                    break;
                }
            } else {
                //bail out 
                break;
            }

        } //no more data

        res.textString = tempString.toString();

        return res;
    }

    /**
     * Representation of the string extraction result
     */
    public class StringExtractResult implements Comparable<StringExtractResult> {

        int offset; ///< offset in input buffer where the first string starts
        int numBytes; ///< num bytes in input buffer consumed
        int numChars; ///< number of encoded characters extracted in the textString
        int firstUnprocessedOff; ///< first byte past the last byte used in extraction, offset+numBytes for a single result, but we keep track of it for multiple extractions
        String textString; ///< the actual text string extracted, of numChars long

        public int getFirstUnprocessedOff() {
            return firstUnprocessedOff;
        }

        public int getStartOffset() {
            return offset;
        }

        public int getNumBytes() {
            return numBytes;
        }

        public int getTextLength() {
            return numChars;
        }

        public String getText() {
            return textString;
        }

        @Override
        public int compareTo(StringExtractResult o) {
            //result with highest num of characters is less than (wins)
            //TODO handle tie - pick language with smallest number of chars
            return o.numChars - numChars;
        }
    }

    /**
     * Encapsulates the loaded unicode table and different scripts and provides
     * utilitities for the table and script lookup. Manages loading of the
     * unicode table. Used as a singleton to ensure minimal resource usage for
     * the unicode table.
     */
    public static class StringExtractUnicodeTable {

        public interface LanguageInfo {

            String getLanguages();
        }

        /**
         * Scripts listed in the unicodeTable loaded
         */
        public static enum SCRIPT implements LanguageInfo {

            NONE {
                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            COMMON {
                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            LATIN_1 {
                 @Override
                public String toString() {
                    return "Latin - Basic";
                }
                @Override
                public String getLanguages() {
                    return "English";
                }
            },
            GREEK {
                @Override
                public String toString() {
                    return "Greek";
                }

                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            CYRILLIC {
                @Override
                public String toString() {
                    return "Cyrillic";
                }

                @Override
                public String getLanguages() {
                    return "Russian, Bulgarian, Serbian, Moldovan";
                }
            },
            ARMENIAN {
                @Override
                public String toString() {
                    return "Armenian";
                }

                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            HEBREW {
                @Override
                public String toString() {
                    return "Hebrew";
                }

                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            ARABIC {
                @Override
                public String toString() {
                    return "Arabic";
                }

                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            SYRIAC {
                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            THAANA {
                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            DEVANAGARI {
                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            BENGALI {
                @Override
                public String toString() {
                    return "Bengali";
                }

                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            GURMUKHI {
                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            GUJARATI {
                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            ORIYA {
                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            TAMIL {
                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            TELUGU {
                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            KANNADA {
                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            MALAYALAM {
                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            SINHALA {
                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            THAI {
                @Override
                public String toString() {
                    return "Thai";
                }

                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            LAO {
                @Override
                public String toString() {
                    return "Laotian";
                }

                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            TIBETAN {
                @Override
                public String toString() {
                    return "Tibetian";
                }

                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            MYANMAR {
                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            GEORGIAN {
                @Override
                public String toString() {
                    return "Georgian";
                }

                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            HANGUL {
                @Override
                public String toString() {
                    return "Hangul";
                }

                @Override
                public String getLanguages() {
                    return "Korean";
                }
            },
            ETHIOPIC {
                @Override
                public String toString() {
                    return "Ethiopic";
                }

                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            CHEROKEE {
                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            CANADIAN_ABORIGINAL {
                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            OGHAM {
                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            RUNIC {
                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            KHMER {
                @Override
                public String toString() {
                    return "Khmer";
                }

                @Override
                public String getLanguages() {
                    return "Cambodian";
                }
            },
            MONGOLIAN {
                @Override
                public String toString() {
                    return "Mongolian";
                }

                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            HIRAGANA {
                @Override
                public String toString() {
                    return "Hiragana";
                }

                @Override
                public String getLanguages() {
                    return "Japanese";
                }
            },
            KATAKANA {
                @Override
                public String toString() {
                    return "Katakana";
                }

                @Override
                public String getLanguages() {
                    return "Japanese";
                }
            },
            BOPOMOFO {
                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            HAN {
                @Override
                public String toString() {
                    return "Han";
                }

                @Override
                public String getLanguages() {
                    return "Chinese, Japanese, Korean";
                }
            },
            YI {
                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            OLD_ITALIC {
                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            GOTHIC {
                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            DESERET {
                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            INHERITED {
                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            TAGALOG {
                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            HANUNOO {
                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            BUHID {
                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            TAGBANWA {
                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            LIMBU {
                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            TAI_LE {
                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            LINEAR_B {
                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            UGARITIC {
                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            SHAVIAN {
                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            OSMANYA {
                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            CYPRIOT {
                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            BRAILLE {
                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            BUGINESE {
                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            COPTIC {
                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            NEW_TAI_LUE {
                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            GLAGOLITIC {
                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            TIFINAGH {
                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            SYLOTI_NAGRI {
                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            OLD_PERSIAN {
                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            KHAROSHTHI {
                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            BALINESE {
                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            CUNEIFORM {
                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            PHOENICIAN {
                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            PHAGS_PA {
                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            NKO {
                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            CONTROL {
                @Override
                public String getLanguages() {
                    return toString();
                }
            },
            LATIN_2 {
                @Override
                public String toString() {
                    return "Latin - Extended";
                }

                @Override
                public String getLanguages() {
                    return "European";
                }
            }
        };
        private static final SCRIPT[] SCRIPT_VALUES = SCRIPT.values();
        private static final String PROPERTY_FILE = "StringExtract.properties";
        /**
         * table has an entry for every possible 2-byte value
         */
        private static final int UNICODE_TABLE_SIZE = 65536;
        /**
         * unicode lookup table with 2 byte index and value of script
         */
        private static final char[] unicodeTable = new char[UNICODE_TABLE_SIZE];
        private static StringExtractUnicodeTable instance = null; //the singleton instance

        /**
         * return instance of StringExtract of null if it could not be
         * initialized
         *
         * @return
         */
        public static synchronized StringExtractUnicodeTable getInstance() {
            if (instance == null) {
                instance = new StringExtractUnicodeTable();
                if (!instance.init()) {
                    //error condition
                    instance = null;
                }

            }
            return instance;
        }

        /**
         * Lookup and get script given byte value of a potential character
         *
         * @param value
         * @return the script type corresponding to the value
         */
        public SCRIPT getScript(int value) {
            char scriptVal = unicodeTable[value];
            return SCRIPT_VALUES[scriptVal];
        }

        /**
         * Check if the script belongs to generic/common (chars are shared
         * between different scripts)
         *
         * @param script to check for
         * @return true if the script is generic
         */
        public static boolean isGeneric(SCRIPT script) {
            return script == SCRIPT.COMMON; // || script == SCRIPT.LATIN_1;
        }

        public static int getUnicodeTableSize() {
            return UNICODE_TABLE_SIZE;
        }

        /**
         * Get the value of the script
         *
         * @param script the script to get value of
         * @return the value corresponding to ordering in the SCRIPT enum
         */
        public static int getScriptValue(SCRIPT script) {
            return script.ordinal();
        }

        public static SCRIPT scriptForString(String scriptStringVal) {
            SCRIPT script = SCRIPT.valueOf(scriptStringVal);
            return script;
        }

        /**
         * Initialization, loads unicode tables
         *
         * @return true if initialized properly, false otherwise
         */
        private boolean init() {
            Properties properties = new Properties();
            try {
                //properties.load(new FileInputStream("StringExtract.properties"));            
                InputStream inputStream = StringExtract.class.getResourceAsStream(PROPERTY_FILE);
                properties.load(inputStream);
                String table = properties.getProperty("UnicodeTable");
                StringTokenizer st = new StringTokenizer(table, " ");
                int toks = st.countTokens();
                //logger.log(Level.INFO, "TABLE TOKS: " + toks);
                if (toks != UNICODE_TABLE_SIZE) {
                    logger.log(Level.WARNING, "Unicode table corrupt, expecting: " + UNICODE_TABLE_SIZE, ", have: " + toks);
                    return false;
                }

                int tableIndex = 0;
                while (st.hasMoreTokens()) {
                    String tok = st.nextToken();
                    char code = (char) Integer.parseInt(tok);
                    unicodeTable[tableIndex++] = code;
                }

                logger.log(Level.INFO, "initialized, unicode table loaded");

            } catch (IOException ex) {
                logger.log(Level.WARNING, "Could not load" + PROPERTY_FILE);
                return false;
            }

            return true;

        }
    }
}
