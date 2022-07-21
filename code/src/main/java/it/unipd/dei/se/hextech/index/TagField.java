package it.unipd.dei.se.hextech.index;

import java.io.Reader;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;

// TODO CHANGE THE NAME?
public class TagField extends Field {

  /** The type of the document body field */
  private static final FieldType TAGS_TYPE = new FieldType();

  static {
    TAGS_TYPE.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
    TAGS_TYPE.setTokenized(true);
    TAGS_TYPE.setStored(false);
  }

  /**
   * Create a new field for the body of a document.
   *
   * @param value the contents of the body of a document.
   */
  public TagField(final Reader value) {
    super("TAG", value, TAGS_TYPE);
  }

  /**
   * Create a new field for the body of a document.
   *
   * @param value the contents of the body of a document.
   */
  public TagField(final String value) {
    super("TAG", value, TAGS_TYPE);
  }
}
