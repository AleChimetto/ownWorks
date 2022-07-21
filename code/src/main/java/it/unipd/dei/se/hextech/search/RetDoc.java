package it.unipd.dei.se.hextech.search;

/** A retrieved document. */
public class RetDoc implements Comparable<RetDoc> {

  /**
   * SINCE WE DO NOT CLASSIFY THE STANCE --> stance = Q0;
   *
   * <p>public static enum Stance { FIRST, SECOND, NEUTRAL, NO }
   */

  /** The number of ranked documents */
  private static int rank_counter = 0;

  /** Resets the rank counter */
  public static void reset() {
    rank_counter = 0;
  }

  /** The topic number. */
  public int qid;

  /** The stance of the document (passage) toward the comparison objects. */
  public final String stance = "Q0";

  /** The document (passage) ID. */
  public String doc;

  public String body;

  /** The rank the document is retrieved at. */
  public int rank;

  /** The score that generated the ranking. */
   public double score;

  /** A tag that identifies your group and the method you used to produce the run. */
  public String tag;

   public double quality;

   public double finalScore;

  /**
   *
   * @param qid Query id
   * @param doc the document
   * @param score the score
   * @param tag the tag
   * @param quality the quality of the document
   * @param body the body of the document
   */
  public RetDoc(int qid, String doc, double score, String tag, double quality, String body) {
    this.qid = qid;
    this.doc = doc;
    this.score = score;
    this.tag = tag;
    this.quality = 1; //TODO change
    this.finalScore = quality * score;
    this.body = body;
  }

  /**
   *
   * @return Instantiates a returned document with stance. public RetDoc(int qid, Stance stance, String doc,
   *  int rank, float score, String tag) { this(qid, doc, rank, score, tag); this.stance = stance; }
   */
  @Override
  public String toString() {
    // String stance = this.stance != null ? this.stance.name() : "Q0";
    return qid + " " + stance + " " + doc + " " + rank + " " + finalScore + " " + tag;
  }


  /**
   * Sort in not increasing order
   * @param other the other RetDoc to be compared
   * @return the difference between the final score of this document and the other
   */
  public int compareTo(RetDoc other) {
    return (int) Math.signum(other.finalScore - this.finalScore);
  }
}
