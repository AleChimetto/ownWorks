package it.unipd.dei.se.hextech.analyzer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import org.apache.lucene.analysis.CharacterUtils;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

/**
 * Transforms all words to lowercase, except the words which are a both a brand and an English word.
 */
public class LowerCaseOrBrandFilter extends TokenFilter {

  private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
  /**A hashmap of the brands */
  private static final HashSet<String> filtered_brands;

  static {
    // Load list of brands which are also English words
    try (InputStream brands_file =
            LowerCaseOrBrandFilter.class
                .getClassLoader()
                .getResourceAsStream("filtered_brands.txt");
        InputStreamReader stream = new InputStreamReader(brands_file);
        BufferedReader reader = new BufferedReader(stream); ) {
      // Lowercase the brands.
      filtered_brands = new HashSet<>(reader.lines().map(s -> s.toLowerCase()).toList());
    } catch (Exception e) {
      throw new RuntimeException("Cannot load filtered_brands wordlist.");
    }
  }

  public LowerCaseOrBrandFilter(final TokenStream in) {
    super(in);
  }

  /**
   * Transforms all words to lowercase, except the words which are a both a brand and an English word.
   * @return true if it is a token , false otherwise
   * @throws IOException
   */
  @Override
  public final boolean incrementToken() throws IOException {
    if (input.incrementToken()) {
      // If termAtt is not in filtered_brands lowercase it
      if (filtered_brands.contains(termAtt.toString().toLowerCase()))
        CharacterUtils.toLowerCase(termAtt.buffer(), 0, termAtt.length());
      /* else
      System.out.format("Detected brand: %s%n", termAtt); */
      return true;
    } else return false;
  }
}
