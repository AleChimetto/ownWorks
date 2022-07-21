package it.unipd.dei.se.hextech.index;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Consumer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

public class IndexerUtil {

  private static final HashSet<String> comparison_adj;
  private static final HashSet<String> descriptive_adj;

  private static final double BIAS = 1;

  /**
   * Load in a hashmap all the data present in a file
   *
   * @param filename the name of the document
   * @return the hashmap with all the terms present in a file
   */
  private static HashSet<String> load_resource(String filename) {
    try (InputStream brands_file =
            IndexerUtil.class.getClassLoader().getResourceAsStream(filename);
        InputStreamReader stream = new InputStreamReader(brands_file);
        BufferedReader reader = new BufferedReader(stream); ) {
      // Lowercase the brands.
      return new HashSet<>(reader.lines().toList());
    } catch (Exception e) {
      throw new RuntimeException("Cannot load " + filename + " wordlist.");
    }
  }

  static {
    comparison_adj = load_resource("comparative.txt");
    descriptive_adj = load_resource("descriptive.txt");
  }

  /**
   * Evaluates the quality (in percentage) of a document, given its body
   *
   * @param body the body of the document
   * @param weight_params the weight passed for the quality function
   * @return the quality of the document
   */
  static double quality(String body, double[] weight_params) {
    if (weight_params == null) return 1;
    // Symbols frequencies
    double symbol_f = symbolFrequency(body);

    // freqAdj[0] contains freq of comparative adjectives while freq[1] the freq of descriptive ones
    double[] freqAdj = {0, 0};

    HashMap<String, Integer> tf = new HashMap<>();
    int[] vectorL = {0, 0};
    int countToken =
        tokenStreamMap(
            body,
            // Adjectives
            (String token) -> {
              if (token.length() <= 4) {
                vectorL[0]++;
              } else {
                vectorL[1]++;
              }
            },
            (String token) -> {
              // freqAdj[0] used for comparative adjectives
              if (comparison_adj.contains(token)) freqAdj[0]++;
              else if (descriptive_adj.contains(token)) freqAdj[1]++;
            });

    double ratioDiffL = 0.5 + (vectorL[0] - vectorL[1]) / countToken;
    // Normalization
    freqAdj[0] = freqAdj[0] / countToken;
    freqAdj[1] = freqAdj[1] / countToken;

    // - frequenza aggettivi comparativi vs descrittivi (rapporto frequenze)
    double freqAllAdj = freqAdj[0] + freqAdj[1];
    double ratioAdj = 0;
    if (freqAllAdj != 0) ratioAdj = freqAdj[0] / freqAllAdj;

    return convex(
        new double[] {1 - symbol_f, ratioAdj, freqAllAdj, ratioDiffL},
        weight_params,
        BIAS); // TODO: tune weights
  }

  /**
   * Returns a convex combination of (values...)(weights...)
   *
   * @param values the values of to be compute the convex combination
   * @param weights the weights for computing the convex combination
   * @param bias an optional bias for computing an affine convex combination
   * @return the frequencies of the special characters
   */
  static double convex(double[] values, double[] weights, double bias) {
    double sum = bias;
    double w = bias;
    int i = 0;
    for (double value : values) {
      double weight = 1;
      if (i < weights.length) weight = weights[i++];
      sum += value * weight;
      w += weight;
    }
    return sum / w;
  }

  static double convex(double[] values, double[] weights) {
    return convex(values, weights, 0);
  }

  /**
   * Compute the symbol frequency in the document
   *
   * @param body the body of the document
   * @return the frequency of the symbols
   */
  private static double symbolFrequency(String body) {
    double symbol_f = 0;
    final int len = body.length();
    final double ETA =
        Math.min(0.1, 100. / len /* 1% of doc */); // in 1/ETA steps the penalty reaches 0.5

    HashMap<Character, Double> special_penalties = new HashMap<>();
    special_penalties.put('!', 2.);
    for (char c :
        new char[] {
          '?', '%', '$', '&', '*', '+', '/', '<', '=', '>', '@', '_', '"', '\'', '(', ')', '[', ']'
        }) special_penalties.put(c, 1.);

    for (char c : body.toCharArray()) {
      if (!(c >= ',' && c <= ';' || c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z' || c == ' ')) {
        Double p = special_penalties.get(c);
        if (p == null) ++symbol_f;
        else {
          special_penalties.replace(c, p + ETA);
          symbol_f += 1 - 1 / p;
        }
      }
    }
    return symbol_f / len;
  }

  /**
   * Apply given functions to each element of the token stream
   *
   * @param stream the token stream to be mapped
   * @param functions the functions to be applied to each token
   * @return the number of tokens
   */
  @SafeVarargs
  private static int tokenStreamMap(TokenStream stream, Consumer<String>... functions) {
    int countToken = 0;
    try {
      final CharTermAttribute tokenTerm = stream.addAttribute(CharTermAttribute.class);
      stream.reset();
      while (stream.incrementToken()) {
        countToken++;
        String token = tokenTerm.toString();

        for (Consumer<String> fn : functions) fn.accept(token);
      }
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
    return countToken;
  }

  /**
   * Apply given functions to each token of the document
   *
   * @param body the body of the document to be tokenized
   * @param functions the functions to be applied to each token
   * @return the number of tokens
   */
  @SafeVarargs
  private static int tokenStreamMap(String body, Consumer<String>... functions) {
    final TokenStream stream;
    try (Analyzer analyzer = new StandardAnalyzer()) {
      // - tokenization
      stream = analyzer.tokenStream("field", new StringReader(body));
    }
    return tokenStreamMap(stream, functions);
  }
}
