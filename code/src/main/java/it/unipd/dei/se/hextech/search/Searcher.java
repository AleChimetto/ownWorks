package it.unipd.dei.se.hextech.search;

import it.unipd.dei.se.hextech.parse.ParsedDocument;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.xml.sax.SAXException;

public class Searcher{

  private static final int MAX_DOC_PER_TOPIC = 1000; // From Touché

  private static final int IPER_MAX_DOCS = MAX_DOC_PER_TOPIC * 10;

  private static Directory directory;

  private static IndexReader indexReader;

  private static PrintWriter printWriter;

  /**
   *Search from index and topics.
   * @param indexDir the directory of the index
   * @param runID the id of the run
   * @param runPath the path where the run will be store
   * @param boost_params the boost param of the query
   * @throws IOException
   * @throws SAXException
   * @throws java.text.ParseException
   * @throws ParseException
   */
  public static void search(
      String indexDir, String topicsFile, String runID, String runPath, float[] boost_params)
      throws IOException, SAXException, java.text.ParseException, ParseException {
    search(indexDir, Topic.read_topics(topicsFile), runID, runPath, boost_params);
  }

  /**
   *  Search from index and topics.
   * @param indexDir the directory of the index
   * @param topics the topics to be search
   * @param runID the id of the run
   * @param runPath the path where the run will be store
   * @param boost_params the boost param of the query
   * @throws IOException
   * @throws ParseException
   */
  public static void search(
      String indexDir, List<Topic> topics, String runID, String runPath, float[] boost_params)
      throws IOException, ParseException {
    MyQueryParser.setBoost(boost_params);
    try {
      directory = FSDirectory.open(FileSystems.getDefault().getPath(indexDir));
      indexReader = DirectoryReader.open(directory);
      printWriter =
          new PrintWriter(
              Files.newBufferedWriter(
                  Paths.get(runPath).resolve("run.txt"),
                  StandardCharsets.UTF_8,
                  StandardOpenOption.CREATE,
                  StandardOpenOption.TRUNCATE_EXISTING,
                  StandardOpenOption.WRITE));

      /** INDEX SEARCHER SETUP */
      final IndexSearcher indexSearcher = new IndexSearcher(indexReader);
      indexSearcher.setSimilarity(new BM25Similarity());
      for (Topic topic : topics) {
        System.out.printf("Searching for topic number: %s%n", topic.getNumber());

        /** QUERY BUILDING */
        Query query = MyQueryParser.queryBuilder(topic);

        /** RETRIEVE ALL DOCUMENTS for re-ranking purposes */
        TopDocs topDocs = indexSearcher.search(query, IPER_MAX_DOCS);
        ScoreDoc[] scoreDoc = topDocs.scoreDocs;
        final Set<String> quality_field = new HashSet<String>();
        quality_field.add("quality");
        System.out.println("LENGTH" + scoreDoc.length);
        RetDoc[] documents =
            Arrays.stream(scoreDoc)
                .map(
                    (s) -> {
                      RetDoc temp = new RetDoc(0, null, 0, null, 0, null);
                      try {
                        final String docID =
                            indexReader.document(s.doc).get(ParsedDocument.FIELDS.ID);
                        final String body =
                            indexReader.document(s.doc).get(ParsedDocument.FIELDS.CONTENTS);
                        final double quality =
                            Double.parseDouble(
                                indexReader.document(s.doc).get(ParsedDocument.FIELDS.QUALITY));
                        temp =
                            new RetDoc(
                                topic.getNumber(),
                                docID,
                                s.score,
                                "hextech_" + runID,
                                quality,
                                body);
                      } catch (IOException e) {
                        e.printStackTrace();
                      }

                      return temp;
                    })
                .sorted()
                .toArray(RetDoc[]::new);
        // Testing purpose


        /** RE-RANKING */
        for (int i = 0; i < documents.length; i++) {
          documents[i].rank = i + 1;
          printWriter.println(documents[i]);
          /*
          if (i < 10) {
            System.out.println(
                documents[i] + " Quality:" + documents[i].quality + " Body:" + documents[i].body);
          }
           */
          if(documents[i].quality+"" == "0.3020361033064037"){
            System.out.println("----->"+ documents[i].body);
          }
          if(documents[i].quality+"" == "0.8178718322225281"){
            System.out.println("------>"+ documents[i].body);
          }
        }
        printWriter.flush();
      }
    } catch (Exception e) {
      System.out.println(e.getMessage());
      System.out.println("Error: unable to search");
    } finally {
      printWriter.close();
      indexReader.close();
      directory.close();
    }
  }
}
