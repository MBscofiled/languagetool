/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Marcin Miłkowski (http://www.languagetool.org)
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */

package org.languagetool.rules.pl;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.spelling.morfologik.MorfologikSpellerRule;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

public final class MorfologikPolishSpellerRule extends MorfologikSpellerRule {

  private static final String RESOURCE_FILENAME = "/pl/hunspell/pl_PL.dict";

  private static final Pattern POLISH_TOKENIZING_CHARS = Pattern.compile("(?:[Qq]uasi|[Nn]iby)-");

    /**
     * The set of prefixes that are not allowed to be split in the suggestions.
     */
    private final static Set<String> prefixes;

    //Polish prefixes that should never be used to
    //split parts of words
    static {
        final Set<String> tempSet = new HashSet<>();
        tempSet.add("arcy");  tempSet.add("neo");
        tempSet.add("pre");   tempSet.add("anty");
        tempSet.add("eks");   tempSet.add("bez");
        tempSet.add("beze");  tempSet.add("ekstra");
        tempSet.add("hiper"); tempSet.add("infra");
        tempSet.add("kontr"); tempSet.add("maksi");
        tempSet.add("midi");  tempSet.add("między");
        tempSet.add("mini");  tempSet.add("nad");
        tempSet.add("nade");  tempSet.add("około");
        tempSet.add("ponad"); tempSet.add("post");
        tempSet.add("pro");   tempSet.add("przeciw");
        tempSet.add("pseudo"); tempSet.add("super");
        tempSet.add("śród");  tempSet.add("ultra");
        tempSet.add("wice");  tempSet.add("wokół");
        tempSet.add("wokoło");
        prefixes = Collections.unmodifiableSet(tempSet);
    }

  public MorfologikPolishSpellerRule(ResourceBundle messages,
                                     Language language) throws IOException {
    super(messages, language);
    //TODO: use speller.convertsCase() instead, but we need to wait for 1.9 release
    setConvertsCase(true);
  }

  @Override
  public String getFileName() {
    return RESOURCE_FILENAME;
  }

  @Override
  public String getId() {
    return "MORFOLOGIK_RULE_PL_PL";
  }

  @Override
  public Pattern tokenizingPattern() {
    return POLISH_TOKENIZING_CHARS;
  }

    @Override
    protected List<RuleMatch> getRuleMatch(final String word, final int startPos)
    throws IOException {
        final List<RuleMatch> ruleMatches = new ArrayList<>();
        if (isMisspelled(speller, word) && isNotCompound(word)) {
            final RuleMatch ruleMatch = new RuleMatch(this, startPos, startPos
                    + word.length(), messages.getString("spelling"),
                    messages.getString("desc_spelling_short"));
            //If lower case word is not a misspelled word, return it as the only suggestion
            if (!isMisspelled(speller, word.toLowerCase(conversionLocale))) {
                List<String> suggestion = Arrays.asList(word.toLowerCase(conversionLocale));
                ruleMatch.setSuggestedReplacements(suggestion);
                ruleMatches.add(ruleMatch);
                return ruleMatches;
            }
            List<String> suggestions = speller.getSuggestions(word);
            suggestions.addAll(getAdditionalSuggestions(suggestions, word));
            if (!suggestions.isEmpty()) {
                ruleMatch.setSuggestedReplacements(orderSuggestions(suggestions,word));
            }
            ruleMatches.add(ruleMatch);
        }
        return ruleMatches;
    }

    /**
     * Check whether the word is a compound adjective or contains a non-splitting prefix.
     * Used to suppress false positives.
     *
     * @param word Word to be checked.
     * @return True if the word is not a compound.
     * @throws IOException
     * @since 2.5
     */
    private boolean isNotCompound(String word) throws IOException {
        List<String> probablyCorrectWords = new ArrayList<>();
        List<String> testedTokens = new ArrayList<>(2);
        for (int i = 2; i < word.length(); i++) {
            // chop from left to right
            final String first = word.substring(0, i);
            final String second = word.substring(i, word.length());
            if (prefixes.contains(first.toLowerCase(conversionLocale))
                    && !isMisspelled(speller, second)) {
                // ignore this match, it's fine
                probablyCorrectWords.add(word);
            } else {
                testedTokens.clear();
                testedTokens.add(first);
                testedTokens.add(second);
                List<AnalyzedTokenReadings> taggedToks =
                        language.getTagger().tag(testedTokens);
                if (taggedToks.size() == 2
                        // "białozielony", trzynastobitowy
                        && (taggedToks.get(0).hasPosTag("adja")
                        || (taggedToks.get(0).hasPosTag("num:comp")
                           && !taggedToks.get(0).hasPosTag("adv")))
                        && taggedToks.get(1).hasPartialPosTag("adj:")) {
                    probablyCorrectWords.add(word);
                }
            }
        }
        if (!probablyCorrectWords.isEmpty()) {
            addIgnoreTokens(probablyCorrectWords);
            return false;
        }
        return true;
    }
}
