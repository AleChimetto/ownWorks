package it.unipd.dei.se.hextech.script;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 From a list of adjective look if these adjectives are descriptive or comparative using a online dictionary
 */
public class ResourcesScript {
  public static boolean findAnfAdjective(String word) throws IOException {
    String url = "https://www.dictionary.com/browse/" + word;
    URL obj = new URL(url);
    HttpURLConnection con = (HttpURLConnection) obj.openConnection();
    con.setRequestMethod("GET");
    con.setRequestProperty("User-Agent", "Mozilla/5.0");
    // int responseCode = con.getResponseCode();
    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
    String inputLine;
    StringBuffer response = new StringBuffer();
    String interestedInput = null;
    while ((inputLine = in.readLine()) != null) {
      response.append(inputLine);
      if (inputLine.lastIndexOf("adjective") > 0) {
        interestedInput = inputLine.toLowerCase();
        break; // to save time for not reading all the html page
      }
    }
    boolean comparative = false;
    if (interestedInput != null) {
      comparative = (interestedInput.lastIndexOf("comparative") > 0);
    }
    in.close();
    con.disconnect();
    return comparative;
    /*
        String responseBody = response.toString();
        Argument myResponse = gson.fromJson(responseBody, Argument.class);

        if(myResponse == null){
          myResponse = new Argument();
        }

        in.close();
    */
  }

  /**
  Write the adjective in the right file
  */
  public static void writeRightAdjective() {
    try {
      BufferedReader reader =
          new BufferedReader(new FileReader("code/src/main/resources/EnglishAdjectives.txt"));
      String currentLine = reader.readLine();
      FileWriter fileWritercomp = new FileWriter("code/src/main/resources/comparative.txt");
      FileWriter fileWriterdesc = new FileWriter("code/src/main/resources/descriptive.txt");
      PrintWriter printWriterComp = new PrintWriter(fileWritercomp);
      PrintWriter printWriterDesc = new PrintWriter(fileWriterdesc);
      while (currentLine != null) {
        currentLine = reader.readLine();
        if (currentLine != null && (currentLine.lastIndexOf("-") < 0)) {
          try {
            if (findAnfAdjective(currentLine)) {
              printWriterComp.println(currentLine);
            } else {
              printWriterDesc.println(currentLine);
            }
          } catch (IOException e) {
            printWriterDesc.println(currentLine);
          }
        }
      }
      printWriterComp.close();
      printWriterDesc.close();
      reader.close();

    } catch (IOException e) {
      System.out.println(e.getMessage());
    }
  }

  public static void main(String args[]) {
    writeRightAdjective();
  }
}
