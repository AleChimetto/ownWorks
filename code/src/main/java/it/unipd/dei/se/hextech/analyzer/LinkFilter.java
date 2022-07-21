package it.unipd.dei.se.hextech.analyzer;

import java.io.IOException;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

public class LinkFilter extends TokenFilter {

  private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

  private final OffsetAttribute offsetAttribute = addAttribute(OffsetAttribute.class);

  protected LinkFilter(TokenStream input) {
    super(input);
  }

  @Override
  public boolean incrementToken() throws IOException {
    if (!input.incrementToken()) {
      return false;
    }

    String term = new String(termAtt.buffer());

    int startOffset = offsetAttribute.startOffset();
    int endOffset = offsetAttribute.endOffset();
    term = term.substring(0, endOffset - startOffset);
    // skip too short terms to be see as lnk
    if (term.length() < 3) {
      return true;
    }

    if (term.lastIndexOf("http") >= 0) {
      termAtt.setEmpty();
    }

    return true;
  }
}
