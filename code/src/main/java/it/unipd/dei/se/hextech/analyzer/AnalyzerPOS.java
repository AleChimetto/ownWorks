package it.unipd.dei.se.hextech.analyzer;

import static it.unipd.dei.se.hextech.analyzer.AnalyzerUtil.*;

import java.io.IOException;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.opennlp.OpenNLPPOSFilter;
import org.apache.lucene.analysis.opennlp.OpenNLPTokenizer;

public class AnalyzerPOS extends Analyzer {

  public AnalyzerPOS() {
    super();
  }

  /**
   *
   * @param s the stream
   * @return the analyzation using the pos
   */
  @Override
  protected TokenStreamComponents createComponents(String s) {
    Tokenizer source;
    try {
      source =
          new OpenNLPTokenizer(
              TokenStream.DEFAULT_TOKEN_ATTRIBUTE_FACTORY,
              loadSentenceDetectorModel("en-sent.bin"),
              loadTokenizerModel("en-token.bin"));
    } catch (IOException e) {
      // The OpenNLPTokenizer seems to have a "wrong" signature declaring to throw an IOException
      // which actually
      // is never thrown. This forces us to wrap everything with try-catch.

      throw new IllegalStateException(
          String.format(
              "Unable to create the OpenNLPTokenizer: %s. This should never happen: surprised :-o",
              e.getMessage()),
          e);
    }

    TokenStream tokens = new OpenNLPPOSFilter(source, loadPosTaggerModel("en-pos-maxent.bin"));

    return new TokenStreamComponents(source, tokens);
  }
}
