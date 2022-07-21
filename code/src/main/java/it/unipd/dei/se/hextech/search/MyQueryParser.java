package it.unipd.dei.se.hextech.search;

import it.unipd.dei.se.hextech.analyzer.*;
import it.unipd.dei.se.hextech.parse.ParsedDocument;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Pattern;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.pattern.PatternReplaceFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.synonym.SynonymGraphFilter;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.analysis.tokenattributes.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.apache.lucene.search.*;

public class MyQueryParser {

  /** The query parser */
  private static QueryParser queryParser_q1;

  private static QueryParser queryParser_q2;
  private static QueryParser queryParser_q3;

  /** The boost for the queries */
  private static final float boost_q1 = 1.2f;

  private static float boost_q2;
  private static float boost_q3;
  private static float boost_q4;
  /** The boolean query builder */

  /** String that contains noun attributes */
  private static final String nounTypes = "NN NNS NNP NNPS";
  /** String that contains adjectives attributes */
  private static final String adjTypes = "JJ JJS JJR RB RBR RBS";

  public static final void setBoost (float[] boost_params){
    boost_q2 = boost_params[0];
    boost_q3 = boost_params[1];
    boost_q4 = boost_params[2];
  }

  private static final SynonymMap synonymMap = AnalyzerUtil.buildSynonymMap("synonyms_en.txt");

  static {
    queryParser_q1 =
      new QueryParser(
        ParsedDocument.FIELDS.CONTENTS,
        new Analyzer() {
          @Override
          protected Analyzer.TokenStreamComponents createComponents(String s) {
            final Tokenizer source = new WhitespaceTokenizer();
            // keep digits
            Pattern p_keep_digits = Pattern.compile("[^A-Za-z0-9]");
            TokenStream tokens = new PatternReplaceFilter(source, p_keep_digits, " ", true);
            Pattern p_remove_space = Pattern.compile(" ");
            tokens = new PatternReplaceFilter(tokens, p_remove_space, "", true);
            tokens = new StopFilter(tokens, AnalyzerUtil.loadStopList("lucene.txt"));

            return new Analyzer.TokenStreamComponents(source, tokens);
          }
        });
    queryParser_q2 = new QueryParser(ParsedDocument.FIELDS.CONTENTS, new AnalyzerFirstVersion());
    queryParser_q3 =
      new QueryParser(
        ParsedDocument.FIELDS.CONTENTS,
        new Analyzer() {
          @Override
          protected Analyzer.TokenStreamComponents createComponents(String s) {
            final Tokenizer source = new StandardTokenizer();

            TokenStream tokens = new LowerCaseOrBrandFilter(source);
            tokens = new LowerCaseCopyFilter(tokens);
            tokens = new StopFilter(tokens, AnalyzerUtil.loadStopList("OnixLextek_no_adj.txt"));
            tokens = new SynonymGraphFilter(tokens, synonymMap, true);
            return new Analyzer.TokenStreamComponents(source, tokens);
          }
        });
  }

  /** Build an array of queries starting from the topic */
  public static Query queryBuilder(Topic t) throws ParseException, IOException {
    System.out.println("Topic title: " + t.getTitle());

    BooleanQuery.Builder bq = new BooleanQuery.Builder();

    // Query one: complete query + soft stop word removal
    /*
    Query q1 = queryParser_q1.parse(QueryParserBase.escape(t.getTitle()));
    bq.add(new BoostQuery(q1, boost_q1), BooleanClause.Occur.SHOULD);
    System.out.println("Query one: " + q1);
    */


    // Query two: same analyzer used to index
    Query q2 = queryParser_q2.parse(QueryParserBase.escape(t.getTitle()));
    bq.add(new BoostQuery(q2, boost_q2), BooleanClause.Occur.SHOULD);
    System.out.println("Query two: " + q2);

    // Query three: synonyms
    Query q3 = queryParser_q3.parse(QueryParserBase.escape(t.getTitle()));
    bq.add(new BoostQuery(q3, boost_q3), BooleanClause.Occur.SHOULD);
    System.out.println("Query three: " + q3);

    Map<String, String> nouns_and_adj = get_nouns_and_adj(new AnalyzerPOS(), t.getTitle());

    // Query four: query with only n-grams
    for (String key : nouns_and_adj.keySet()) {
      if (nouns_and_adj.get(key).equals("N-GRAM")) {
        String[] terms = key.split(" ");
        Query q4 = new PhraseQuery(2, ParsedDocument.FIELDS.CONTENTS, terms);
        bq.add(new BoostQuery(q3, boost_q4), BooleanClause.Occur.SHOULD);
        System.out.println("Query n-gram: " + q4);
      }
    }
    System.out.println();
    return bq.build();
  }

  /**


  */
  static Map<String, String> get_nouns_and_adj(final Analyzer a, final String t)
      throws IOException {
    Map<String, String> result = new HashMap<>();

    // Create a new TokenStream for a dummy field
    final TokenStream stream = a.tokenStream("field", new StringReader(t));

    // Lucene tokens are decorated with different attributes whose values contain information about
    // the token,
    // e.g. the term represented by the token, the offset of the token, etc.

    // The term represented by the token
    final CharTermAttribute tokenTerm = stream.addAttribute(CharTermAttribute.class);

    // The type the token
    final TypeAttribute tokenType = stream.addAttribute(TypeAttribute.class);

    try {
      // Reset the stream before starting
      stream.reset();

      boolean start_n_gram = false;
      boolean one_gram = true;
      StringBuilder n_gram_string = new StringBuilder();
      while (stream.incrementToken()) {
        // System.out.printf("+ token: %s", tokenTerm.toString());
        // System.out.printf("  - type: %s%n", tokenType.type());
        if (nounTypes.contains(tokenType.type())) {
          if (start_n_gram) {
            n_gram_string.append(" ");
            one_gram = false;
          }
          n_gram_string.append(tokenTerm);
          start_n_gram = true;
        } else if (adjTypes.contains(tokenType.type())) {
          result.put(tokenTerm.toString(), tokenType.type());
          if (start_n_gram) {
            if (one_gram) result.put(n_gram_string.toString(), "ONE-GRAM");
            else result.put(n_gram_string.toString(), "N-GRAM");
            start_n_gram = false;
            one_gram = true;
            n_gram_string = new StringBuilder();
          }
        } else if (start_n_gram) {
          if (one_gram) result.put(n_gram_string.toString(), "ONE-GRAM");
          else result.put(n_gram_string.toString(), "N-GRAM");
          start_n_gram = false;
          one_gram = true;
          n_gram_string = new StringBuilder();
        }
      }
      if (start_n_gram) {
        if (one_gram) result.put(n_gram_string.toString(), "ONE-GRAM");
        else result.put(n_gram_string.toString(), "N-GRAM");
      }

      // Perform any end-of-stream operations
      stream.end();
    } finally {

      // Close the stream and release all the resources
      stream.close();
    }
    return result;
  }
}
