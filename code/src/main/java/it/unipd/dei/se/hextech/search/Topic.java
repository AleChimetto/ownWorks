package it.unipd.dei.se.hextech.search;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Topic {

  /** Number of the topic */
  int number;
  /** The title of the topic*/
  String title;
  /** The objects of the topic*/
  String objects;
  /** The description of the given topic */
  String description;
  /** The narrative of the topic */
  String narrative;

  /**
   *
   * @param number number of the topic
   * @param title the title of the topic
   * @param objects the object (content) of the topic
   * @param description the description of the topic
   * @param narrative the narrative of the topic
   */
  public Topic(int number, String title, String objects, String description, String narrative) {
    this.description = description;
    this.narrative = narrative;
    this.objects = objects;
    this.number = number;
    this.title = title;
  }

  /**
   *
   * @param topicsFile the file where the topics are stored
   * @return the list of the topics
   * @throws IOException
   * @throws SAXException
   */
  public static List<Topic> read_topics(String topicsFile) throws IOException, SAXException {
    /** QUERY FILE READER */
    List<Topic> topics = new ArrayList<>();

    final Document doc;
    try {
      doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(topicsFile));
    } catch (ParserConfigurationException e) {
      return topics; // Cannot fail (since no config is passed)
    }

    final NodeList nList = doc.getElementsByTagName("topic");
    for (int i = 0; i < nList.getLength(); ++i) {
      Node n = nList.item(i);
      if (n.getNodeType() == Node.ELEMENT_NODE) {
        Element e = (Element) n;
        int number = Integer.parseInt(e.getElementsByTagName("number").item(0).getTextContent());
        String title = e.getElementsByTagName("title").item(0).getTextContent();
        String objects = e.getElementsByTagName("objects").item(0).getTextContent();
        String description = e.getElementsByTagName("description").item(0).getTextContent();
        String narrative = e.getElementsByTagName("narrative").item(0).getTextContent();

        topics.add(new Topic(number, title, objects, description, narrative));
      }
    }

    System.out.println(nList.getLength());

    return topics;
  }

  /**
   *
   * @return the object to string
   */
  @Override
  public String toString() {
    return "TopicObj{"
        + "number='"
        + number
        + '\''
        + ", title='"
        + title
        + '\''
        + ", objects='"
        + objects
        + '\''
        + ", description='"
        + description
        + '\''
        + ", narrative='"
        + narrative
        + '\''
        + '}';
  }

  /**
   *
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   *
   * @return the narrative
   */
  public String getNarrative() {
    return narrative;
  }

  /**
   *
   * @return the number of the topic
   */
  public int getNumber() {
    return number;
  }

  /**
   *
   * @return the object of the topic
   */
  public String getObjects() {
    return objects;
  }

  /**
   *
   * @return the title of the topic
   */
  public String getTitle() {
    return title;
  }
}
