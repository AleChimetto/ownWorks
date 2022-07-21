package it.unipd.dei.se.hextech.parse;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.lucene.document.Field;

/** Represents a parsed document to be indexed. */
public class ParsedDocument {
  //TODO: to fix names
  /** The names of the {@link Field}s within the index. */
  public static final class FIELDS {

    /** The document identifier */
    public static final String ID = "id";

    /** The document body */
    public static final String CONTENTS = "body";

    public static final String QUALITY = "quality";
  }

  /** The unique document identifier. */
  private final String id;

  /** The body of the document. */
  private final String contents;



  /**
   * Creates a new parsed document
   *
   * @param id the unique document identifier.
   * @param body the body of the document.
   * @throws NullPointerException if {@code id} and/or {@code body} are {@code null}.
   * @throws IllegalStateException if {@code id} and/or {@code body} are empty.
   */
  public ParsedDocument(final String id, final String contents) {

    if (id == null) {
      throw new NullPointerException("Document identifier cannot be null.");
    }

    if (id.isEmpty()) {
      throw new IllegalStateException("Document identifier cannot be empty.");
    }

    this.id = id;

    if (contents == null) {
      throw new NullPointerException("Document contents cannot be null.");
    }

    if (contents.isEmpty()) {
      throw new IllegalStateException("Document contents cannot be empty.");
    }

    this.contents = contents;
  }

  /**
   * Returns the unique document identifier.
   *
   * @return the unique document identifier.
   */
  public String getIdentifier() {
    return id;
  }

  /**
   * Returns the body of the document.
   *
   * @return the body of the document.
   */
  public String getBody() {
    return contents;
  }

  @Override
  public final String toString() {
    ToStringBuilder tsb =
        new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("identifier", id)
            .append("contents", contents);

    return tsb.toString();
  }

  @Override
  public final boolean equals(Object o) {
    return (this == o) || ((o instanceof ParsedDocument) && id.equals(((ParsedDocument) o).id));
  }

  @Override
  public final int hashCode() {
    return 37 * id.hashCode();
  }
}
