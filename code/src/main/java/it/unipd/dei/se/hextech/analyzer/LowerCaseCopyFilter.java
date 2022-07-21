package it.unipd.dei.se.hextech.analyzer;

import java.io.IOException;
import org.apache.lucene.analysis.CharacterUtils;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

/*
 *  For each not lowercase token it expanses the token stream adding its lowercase version.
 */
public class LowerCaseCopyFilter extends TokenFilter {

  private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
  private LowerCaseCopyFilter.State state = null;

  public LowerCaseCopyFilter(final TokenStream in) {
    super(in);
  }

  /**
   * Check if a token is lowercase
   * @param s
   * @return true if it is lowercase otherwise false
   */
  private static boolean is_lowercase(char... s) {
    for (char c : s)
      if (Character.isUpperCase(c)) {
        // System.err.format("%c (0x%x) is not lowercase%n", c, (int) c);
        return false;
      }
    return true;
  }

  /**
   * This method if it found a token uppercase it creates two copies:
   * - one equal
   * - one lowercase
   * @return true if it is a token
   * @throws IOException
   */
  @Override
  public final boolean incrementToken() throws IOException {
    if (state != null) {
      // We have to convert a previous non-lowercase token
      this.restoreState(state);
      state = null;
      CharacterUtils.toLowerCase(termAtt.buffer(), 0, termAtt.length());
      // System.out.format(" → %s%n", termAtt);
      return true;
    } else if (input.incrementToken()) {
      if (!is_lowercase(termAtt.buffer())) {
        state = this.captureState();
        // System.out.format("LowerCaseCopyFilter: %s", termAtt);
      }
      return true;
    } else return false;
  }
}
