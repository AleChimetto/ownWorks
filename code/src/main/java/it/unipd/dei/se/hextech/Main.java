package it.unipd.dei.se.hextech;

import it.unipd.dei.se.hextech.index.Indexer;
import it.unipd.dei.se.hextech.search.Searcher;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
  public static void main(String[] args) throws Exception {
    /** The input directory contains two files: - topics.xml - passages.jsonl.gz */
    String inputDir = "../Touche";

    /** Here the file run.txt will be written */
    String outputDir = "runs";

    /** The directory where the index will be build */
    String indexDir = "index";

    /** Force index rebuild */
    boolean force_index = false;

    /** Id for running */
    String id_run = "";

    /** Array for Query boost parameters */
    float[] boost_params = {0, 0, 0};

    /** Array for weight parameters */
    double[] weight_params = null;

    for (int i = 0; i < args.length; ++i) {
      switch (args[i]) {
        case "-i":
          if (++i == args.length) throw new Error("Missing inputDataset directory.");
          inputDir = args[i];
          break;
        case "-o":
          if (++i == args.length) throw new Error("Missing outputDir directory.");
          outputDir = args[i];
          break;
        case "-f":
          force_index = true;
          break;
        case "-id":
          if (++i == args.length) throw new Error("Missing Running Id ");
          id_run = args[i];
          break;
        case "-boost":
          for (int j = 0; j < 3; ++j) {
            if (++i == args.length) throw new Error("Missing Boost Parameter");
            boost_params[j] = Float.parseFloat(args[i]);
          }
          break;
        case "-weight":
          weight_params = new double[] {0, 0, 0, 0};
          for (int j = 0; j < 4; ++j) {
            if (++i == args.length) throw new Error("Missing Weight Parameter");
            weight_params[j] = Double.parseDouble(args[i]);
          }
          break;
        default:
          System.out.println("Usage: ./run.sh [-f] [-i $inputDataset] [-o $outputDir]");
      }
    }

    // Build index
    final FileSystem fs = FileSystems.getDefault();
    Path indexPath = fs.getPath(indexDir);
    if (force_index
        || !Files.isDirectory(indexPath)
        || !Files.list(indexPath).findAny().isPresent()) {
      Indexer index = new Indexer(inputDir + "/passages.jsonl.gz", indexDir, weight_params);
      index.index();
    }

    // TODO n????
    Searcher.search(indexDir, inputDir + "/topics.xml", "run_" + id_run, outputDir, boost_params);
  }
}
