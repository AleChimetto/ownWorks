package it.unipd.dei.se.hextech.analyzer;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.StandardTokenizer;



/**Analyzer used in the indexing */
public class AnalyzerFirstVersion extends Analyzer {

  @Override
  /**
   * Use the LowerCaseOrBrandFilter {@link LowerCaseCopyFilter}
   * after that it LowerCaseOrBrandFilter {@link LowerCaseOrBrandFilter},
   * after all it uses the LovinsStemFilter {@link LovinsStemFilter}
   * @param s is the token to be filtered
   * @return TokenStreamComponents
   */
  protected TokenStreamComponents createComponents(String s) {
    final Tokenizer source = new StandardTokenizer(); // TODO change with the parser of this

    TokenStream tokens = new StopFilter(source,AnalyzerUtil.loadStopList("OnixLextek_no_adj.txt"));

    tokens = new LowerCaseOrBrandFilter(tokens);
    tokens = new LowerCaseCopyFilter(tokens);
    tokens = new LovinsStemFilter(tokens); // Applying the LovinStemFilter
   // tokens = new LinkFilter(tokens);

    return new TokenStreamComponents(source, tokens);
  }
}
