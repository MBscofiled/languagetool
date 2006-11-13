/**
 * French Tagger
 */
package de.danielnaber.languagetool.tagging.fr;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


import com.dawidweiss.stemmers.Lametyzator;

import de.danielnaber.languagetool.AnalyzedToken;
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.tagging.Tagger;
import de.danielnaber.languagetool.tools.Tools;

/**
 * @author Marcin Milkowski
 *
 */
public class FrenchTagger implements Tagger {

  /* French Tagger
   * 
   * Based on inDICO, implemented in FSA
   * 
   * @author Marcin Milkowski
   */
  private static final String RESOURCE_FILENAME = "resource" +File.separator+ "fr" +File.separator+
  "french.dict"; 
    private Lametyzator morfologik = null;
    
  public List<AnalyzedTokenReadings> tag(final List<String> sentenceTokens)
      throws IOException {
    String[] taggerTokens;    
    List<AnalyzedTokenReadings> tokenReadings = new ArrayList<AnalyzedTokenReadings>();
    int pos = 0;
    //caching Lametyzator instance - lazy init
    if (morfologik == null) {   
       File resourceFile = JLanguageTool.getAbsoluteFile(RESOURCE_FILENAME); 
       //System.setProperty(Lametyzator.PROPERTY_NAME_LAMETYZATOR_DICT, resourceFile.getAbsolutePath());
       morfologik = new Lametyzator(Tools.getInputStream(resourceFile.getAbsolutePath()), "iso8859-15", '+');
    }
    
    for (Iterator<String> iter = sentenceTokens.iterator(); iter.hasNext();) {
      String word = iter.next();
      List<AnalyzedToken> l = new ArrayList<AnalyzedToken>();      
      String[] lowerTaggerTokens = null;
        taggerTokens = morfologik.stemAndForm(word);
        if (word != word.toLowerCase()) {
        lowerTaggerTokens = morfologik.stemAndForm(word.toLowerCase());
        }
                
    if (taggerTokens != null) {
       int i = 0;
        while (i < taggerTokens.length) {
            //Lametyzator returns data as String[]
            //first lemma, then annotations
            l.add(new AnalyzedToken(word, taggerTokens[i + 1], taggerTokens[i]));
            i = i + 2;
        } 
      }     
    if (lowerTaggerTokens != null) {
      int i = 0;
       while (i < lowerTaggerTokens.length) {
           //Lametyzator returns data as String[]
           //first lemma, then annotations
           l.add(new AnalyzedToken(word, lowerTaggerTokens[i + 1], lowerTaggerTokens[i]));
           i = i + 2;
       } 
     }        
    
    if (lowerTaggerTokens == null && taggerTokens == null) {
            l.add(new AnalyzedToken(word, null, pos));                       
    }
      pos += word.length();
      tokenReadings.add(new AnalyzedTokenReadings((AnalyzedToken[])l.toArray(new AnalyzedToken[0])));
   }
    
    return tokenReadings;

  }  
  
  /* (non-Javadoc)
   * @see de.danielnaber.languagetool.tagging.Tagger#createNullToken(java.lang.String, int)
   */
  public final Object createNullToken(final String token, final int startPos) {
    return new AnalyzedTokenReadings(new AnalyzedToken(token, null, startPos));
  }
  
  }
