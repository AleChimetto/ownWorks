package it.unipd.dei.se.hextech.index;

/*
 * Copyright 2021-2022 University of Padua, Italy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.google.gson.Gson;
import it.unipd.dei.se.hextech.analyzer.AnalyzerFirstVersion;
import it.unipd.dei.se.hextech.parse.ParsedDocument;
import java.io.*;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.codecs.lucene91.Lucene91Codec;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * Introductory example on how to use <a href="https://lucene.apache.org/" target="_blank">Apache
 * Lucene</a> to index and search for toy documents.
 */
public class Indexer {
  /** The name of the {@code id} {@link Field} in a {@link Document}. */
  private String ID = "id";

  /** The name of the {@code body} {@link Field} in a {@link Document}. */
  private String BODY = "body";

  /** The name of the {@code quality} {@link Field} in a {@link Document}. */
  private String QUALITY = "quality";

  /** The path of the corpus */
  private static String corpusPath;

  /** The path of the index */
  private final String indexDir;

  /** Array to save the weight params for the quality field */
  double[] weight_params = null;

  /** The path to the directory containing the index. */
  public Indexer(String corpusPath, String indexDir, double[] weight_params) {
    Indexer.corpusPath = corpusPath;
    this.indexDir = indexDir;
    this.weight_params = weight_params;
  }

  /**
   * Indexes the provided {@link Document}s with the given {@link Analyzer}.
   *
   * @throws IOException if something goes wrong while indexing.
   */
  public void index() throws IOException {

    if (corpusPath.endsWith(".gz")) {
      try (FileInputStream fis = new FileInputStream(corpusPath);
          GZIPInputStream gis = new GZIPInputStream(fis);
          InputStreamReader isr = new InputStreamReader(gis);
          BufferedReader file = new BufferedReader(isr)) {
        index(file);
      }
    } else {
      try (FileReader jsonFile = new FileReader(corpusPath);
          BufferedReader file = new BufferedReader(jsonFile)) {
        index(file);
      }
    }
  }

  /**
   * Indexes the provided {@link Document}s with the given {@link AnalyzerFirstVersion}.
   * @param jsonFile the file to be read for indexing
   */
  private void index(BufferedReader jsonFile) {

    final FileSystem fs = FileSystems.getDefault();
    // get the path where to store the index
    Path indexPath = fs.getPath(indexDir);
    try {
      if (Files.notExists(indexPath)) {
        Files.createDirectory(indexPath);
      }
      if (!Files.isDirectory(indexPath)) {
        throw new IllegalStateException(
            String.format("%s is not a directory.", indexPath.toAbsolutePath().toString()));
      }
    } catch (IOException e) {
      System.out.println(e.getMessage());
    }
    try {
      final Directory directory = FSDirectory.open(indexPath);

      /** ---------------SETUP INDEXWRITER-------------------------- */
      final Analyzer analyzer = new AnalyzerFirstVersion();

      // Utility class for holding all the required configuration for the indexer
      final IndexWriterConfig config = new IndexWriterConfig(analyzer);

      // force to re-create the index if it already exists
      config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

      // set the similarity. BM25 is already the default one
      config.setSimilarity(new BM25Similarity());

      // set the text-based codec
      config.setCodec(new Lucene91Codec(Lucene91Codec.Mode.BEST_SPEED));

      // prevent using the compound file format
      config.setUseCompoundFile(false);

      System.out.printf("- Indexer successfully created%n");

      long start = System.currentTimeMillis();

      /** READ LINE BY LINE */
      int count = 0;
      Gson gson = new Gson();
      double small = 1;
      double big = 0;
      double medium = 0;
      int cnt = 0;
      try (final IndexWriter writer = new IndexWriter(directory, config)) {
        String line = jsonFile.readLine();
        while (line != null) {
          ParsedDocument p = gson.fromJson(line, ParsedDocument.class);

          Document d = new Document();
          d.add(new StringField(ID, p.getIdentifier(), Field.Store.YES));
          {
            String body = p.getBody();
            d.add(new Field(BODY, body, TextField.TYPE_STORED));
            d.add(new StoredField(QUALITY, IndexerUtil.quality(body, weight_params)));

            /*Some experimental test of the index */
            /**
            if(small >IndexerUtil.quality(body, weight_params)){//SMALL
              small = IndexerUtil.quality(body, weight_params);
              System.out.println("SMALL:"+body+" Quality:"+small);
            }

            if(big < IndexerUtil.quality(body, weight_params)){//BIG
              big = IndexerUtil.quality(body, weight_params);
              System.out.println("BIG:"+body+" Quality:"+big);
            }
            if(Math.random() < 0.0005)//SOME OF THEM
            {
              medium = medium + IndexerUtil.quality(body, weight_params);
              cnt++;
            }
            */
          }

          writer.addDocument(d);

          count++;
          if (count % 20000 == 0) System.out.println("Passages processed: " + count);
          line = jsonFile.readLine();
        }
        System.out.println("Medium:"+(medium/cnt));
        System.out.println("Small:"+small);
        System.out.println("Big:"+big);
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        directory.close();
      }

      System.out.printf("- Indexer successfully closed%n");

      System.out.println("Passages number " + count);
      System.out.println(
          "Total Time Taken : " + (System.currentTimeMillis() - start) / 1000 + " secs");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
