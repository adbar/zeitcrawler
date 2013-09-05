/*	This is part of the Zeitcrawler (http://code.google.com/p/zeitcrawler/).
	Copyright (C) Adrien Barbaresi 2013.
	This is free software, released under the GNU GPL v3 license (http://www.gnu.org/licenses/gpl.html).

	WORK IN PROGRESS ! This is an experimental software part.
*/


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import javax.xml.stream.*;
import javax.xml.transform.stream.StreamSource;


/**
...
*/
public class ProcessFiles {

    private static int filecounter = 0;
    private static String dirname, exportname;
    private static String tagBuffer = null;
    private static boolean bHead, bBody, bSkip, bAuthor, bDate, bKeyword, bLink, bParagraph, bReference, bRessort, bSubressort, bSubtitle, bSupertitle, bTag, bTitle, bType, bUuid;
    private static XMLStreamReader stax;
    private static XMLStreamWriter xmlWriter;
    private static HashMap<String,String> attributesMap = new HashMap<String,String> ();

    /*
    * Main: test args
    */
    public static void main(String[] args) {
        if( args.length != 1) {
            System.out.println( "Error, parameter missing:\n java ProcessFiles directory/\n" );
            System.exit(0);
        }
        else {
            processDir( args[0] );
        }
    }

    /*
    * Read files in directory
    */
    private static void processDir(String dirname) {
        File folder = new File(dirname);
        File[] listOfFiles = folder.listFiles();
        if (listOfFiles != null) {
            printResults(listOfFiles.length);
            // File loop
            for (int i = 0; i < listOfFiles.length; i++) {
                String filestring = listOfFiles[i].toString();
                // File check + file name check (do not process the export files)
                if ( (listOfFiles[i].isFile()) && ("_export" != filestring.substring(filestring.length() - 11, filestring.length() - 4).intern()) ) {
                    enumerateFiles(filestring);
                    // Try-catch read XML
                    try {
                        XMLparseFile(listOfFiles[i]);
                    }
                    catch(IOException e) {
                        e.printStackTrace();
                    }
                    catch(XMLStreamException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        else {
            printResults(0);
        }
    }

    /*
    * Print total number of files, otherwise print error message
    */
    private static void printResults(int number) {
        if (number == 0) {
            System.out.println("The directory " + dirname + " does not exist or it is empty.");
        }
        else {
            System.out.println("Number of files in directory: " + number);
        }
    }

    /*
    * Enumerate the files, process export name
    */
    private static void enumerateFiles(String name) {
        System.out.println(++filecounter + "\t" + name);
        exportname = name.substring(0, name.length() - 4) + "_export.xml";
        System.out.println("Written as file: " + exportname);
    }


    /*
    * Scan the XML Files and print out an event
    */
    private static void XMLparseFile(File fh) throws IOException, XMLStreamException {

        // create StreamReader
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        stax = inputFactory.createXMLStreamReader( new StreamSource( fh ) );

        // create XMLStreamWriter output
        XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
        xmlWriter = xmlof.createXMLStreamWriter(new FileOutputStream (exportname), "UTF-8");

        // start
        xmlWriter.writeStartDocument("utf-8", "1.0");
        xmlWriter.writeCharacters("\n");

        //int event = stax.getEventType();
        while( stax.hasNext() ) {
            processEvent(stax, xmlWriter);
            stax.next();
        }
    }


    private static void processEvent(XMLStreamReader stax, XMLStreamWriter xmlWriter) throws XMLStreamException {

        switch (stax.getEventType()) {

            /* START ELEM */
            case XMLStreamConstants.START_ELEMENT:

                /* Head and body detection */
                if ( stax.getLocalName().equals( "head" ) ) {
                    bHead = true;
                    xmlWriter.writeStartElement("head");
                    xmlWriter.writeCharacters("\n");
                    break;
                }
                else if ( stax.getLocalName().equals( "body" ) ) {
                    bHead = false;
                    bBody = true;
                    break;
                }
                else if ( stax.getLocalName().equals( "teaser" ) ) {
                    bBody = false; // something else ?
                    xmlWriter.writeEndElement();
                    xmlWriter.writeCharacters("\n");
                    xmlWriter.writeComment("beginning teaser");
                    xmlWriter.writeCharacters("\n");
                    break;
                }
                else if ( stax.getLocalName().equals( "infobox" ) ) {
                    bSkip = true;
                    skippingComment("infobox");
                    break;
                }

                // Skip elements
                if (bSkip) {
                    break;
                }

                /* Head */
                if (bHead) {

                    /* Reference */
                    if ( stax.getLocalName().equals( "reference" ) ) {
                        int attributeCount = stax.getAttributeCount();
                        for (int i = 0; i < attributeCount; i++) {
                            if ( ("publication-date" != stax.getAttributeLocalName(i).intern()) && ("expires" != stax.getAttributeLocalName(i).intern()) && ("type" != stax.getAttributeLocalName(i).intern())) {
                                attributesMap.put(stax.getAttributeLocalName(i), stax.getAttributeValue(i));
                            }
                        }
                        bReference = true;
                    }

                    /* Attributes */
                    else if ( stax.getLocalName().equals( "attribute" ) ) {
                        int attributeCount = stax.getAttributeCount();
                        for (int i = 0; i < attributeCount; i++) {
                        // if (stax.getAttributeCount() >= 2) {
                        // stax.getAttributeLocalName(2).equals("name") && 

                            if ( stax.getAttributeLocalName(i).equals("name") ) {
                                // Author
                                if ( stax.getAttributeValue(i).equals("author") ) { 
                                    bAuthor = true;
                                }
                                // Date
                                else if ( stax.getAttributeValue(i).equals("date-last-modified") ) { 
                                    bDate = true;
                                }
                                // Ressort
                                else if ( stax.getAttributeValue(i).equals("ressort") ) { 
                                    bRessort = true;
                                }
                                // Sub-ressort
                                else if ( stax.getAttributeValue(i).equals("sub_ressort") ) { 
                                    bSubressort = true;
                                }
                                // Type
                                 /* <attribute ns="http://namespaces.zeit.de/CMS/document" name="type">article</attribute>
                                   <attribute ns="http://namespaces.zeit.de/CMS/meta" name="type">article</attribute> */
                                else if ( stax.getAttributeValue(i).equals("type") ) { 
                                    bType = true;
                                }

                                // UUID
                                if ( stax.getAttributeValue(i).equals("uuid") ) { 
                                    bUuid = true;
                                }
                            }

                        }
                    }

                    /* Tag and keyword search */
                    else if ( stax.getLocalName().equals( "keyword" ) ) {
                        bKeyword = true;
                    }
                    else if ( stax.getLocalName().equals( "tag" ) ) {
                        int attributeCount = stax.getAttributeCount();
                        for (int i = 0; i < attributeCount; i++) {
                            if ( "url_value" != stax.getAttributeLocalName(i).intern() ) {
                                attributesMap.put(stax.getAttributeLocalName(i), stax.getAttributeValue(i));
                            }
                        }
                        bTag = true;
                    }

                }

                /* Body */
                if (bBody) {

                    /* Paragraphs */
                    if (bParagraph) {
                        if ( (stax.getLocalName().equals( "strong" )) || (stax.getLocalName().equals( "em" )) || (stax.getLocalName().equals( "intertitle" )) ) { //
                            tagBuffer = stax.getLocalName();
                        }
                        else if ( stax.getLocalName().equals( "a" ) ) {
                            int attributeCount = stax.getAttributeCount();
                            for (int i = 0; i < attributeCount; i++) {
                                if ( stax.getAttributeLocalName(i).equals("href") ) {
                                    attributesMap.put(stax.getAttributeLocalName(i), stax.getAttributeValue(i));
                                }
                            }
                            bLink = true;
                            tagBuffer = "a";
                        }
                        else if ( stax.getLocalName().equals( "br" ) ) {
                            skippingComment("br");
                        }
                        else if ( stax.getLocalName().equals( "entity" ) ) {
                            int attributeCount = stax.getAttributeCount();
                            for (int i = 0; i < attributeCount; i++) {
                                if ( "url_value" != stax.getAttributeLocalName(i).intern() ) {
                                    attributesMap.put(stax.getAttributeLocalName(i), stax.getAttributeValue(i));
                                }
                            }
                            // bLink = true;
                            tagBuffer = "entity";
                        }
                        else {
                            String errorMsg = "tag not known: " + stax.getLocalName();
                            xmlWriter.writeComment(errorMsg);
                            xmlWriter.writeCharacters("\n");
                            System.out.println(errorMsg);
                        }
                    }

                    /* Titles search */
                    if ( stax.getLocalName().equals( "supertitle" ) ) {
                        bSupertitle = true;
                    }
                    else if ( stax.getLocalName().equals( "title" ) ) {
                        bTitle = true;
                    }
                    else if ( stax.getLocalName().equals( "subtitle" ) ) {
                        bSubtitle = true;
                    }

                    /* Paragraphs */
                    else if ( stax.getLocalName().equals( "p" ) ) {
                        xmlWriter.writeStartElement("p");
                        xmlWriter.writeCharacters("\n");
                        bParagraph = true;
                    }

                }

                break;
                 
            /* CHARACTERS */
            case XMLStreamConstants.CHARACTERS:

                /* head */
                if (bHead) {
                    if (bAuthor) {
                        printEvent("author", stax.getText());
                        writeEvent("author", stax.getText());
                        bAuthor = false;
                    }
                    else if (bDate) {
                        writeEvent("date", stax.getText());
                        bDate = false;
                    }
                    else if (bKeyword) {
                        writeEvent("keyword", stax.getText());
                        bKeyword = false;
                    }
                    else if (bReference) {
                        writeEvent("reference", stax.getText());
                        bReference = false;
                    }
                    else if (bRessort) {
                        writeEvent("ressort", stax.getText());
                        bRessort = false;
                    }
                    else if (bSubressort) {
                        writeEvent("subressort", stax.getText());
                        bSubressort = false;
                    }
                    else if (bType) {
                        writeEvent("type", stax.getText());
                        bType = false;
                    }
                    else if (bTag) {
                        writeEvent("tag", stax.getText());
                        bTag = false;
                    }
                    else if (bUuid) {
                        writeEvent("uuid", stax.getText().substring(10, 46));
                        bUuid = false;
                    }
                }
                /* body */
                else if (bBody) {
                    if (bSupertitle) {
                        writeEvent("supertitle", stax.getText());
                        bSupertitle = false;
                    }
                    else if (bTitle) {
                        printEvent("title", stax.getText());
                        writeEvent("title", stax.getText());
                        bTitle = false;
                    }
                    else if (bSubtitle) {
                        writeEvent("subtitle", stax.getText());
                        bSubtitle = false;
                    }
                    else if (bParagraph) {
                        if (tagBuffer != null) {
                            writeEvent(tagBuffer, stax.getText());
                            tagBuffer = null;
                        }
                        else {
                            xmlWriter.writeCharacters(stax.getText());
                        }
                    }
                }
                break;

            /* END BODY */
            case XMLStreamConstants.END_ELEMENT:
                if ( stax.getLocalName().equals( "body" ) ) {
                    bBody = false;
                    xmlWriter.writeComment("end body");
                    xmlWriter.writeCharacters("\n");
                }
                else if ( stax.getLocalName().equals( "infobox" ) ) {
                    bSkip = false;
                }
                else if ( stax.getLocalName().equals( "p" ) ) {
                    xmlWriter.writeCharacters("\n");
                    xmlWriter.writeEndElement();
                    xmlWriter.writeCharacters("\n");
                    bParagraph = false;
                }
                else if ( stax.getLocalName().equals( "subtitle" ) ) {
                    // Mark end of metadata
                    xmlWriter.writeEndElement();
                    xmlWriter.writeCharacters("\n");
                    xmlWriter.writeStartElement("body");
                    xmlWriter.writeCharacters("\n");
                }
                break;

            /* DEFAULT: don't do anything */
            default:
                break;

        }

    }


    /* Print the selected events to STDOUT */
    private static void printEvent(String tagname, String buffer) throws XMLStreamException {
        // Print to STDOUT
        System.out.println(tagname + ": " + buffer);
    }

    /* Write the selected events to XML */
    private static void writeEvent(String tagname, String buffer) throws XMLStreamException {
        // Print to XML
        xmlWriter.writeStartElement(tagname);
        // check if there are attributes to write
        if (! attributesMap.isEmpty() ) { // .size() > 0
            for ( String item : attributesMap.keySet() ) {
                xmlWriter.writeAttribute(item, attributesMap.get(item));
            }
            attributesMap.clear();
        }
        // write text contents
        xmlWriter.writeCharacters(buffer);
        xmlWriter.writeEndElement();
        // write newline only if the tag is not part of a paragraph
        if (tagBuffer == null) {
            xmlWriter.writeCharacters("\n");
        }
    }

    /* Write a comment to indicate something has been skipped */
    private static void skippingComment(String tagname) throws XMLStreamException {
        //xmlWriter.writeCharacters("\n");
        xmlWriter.writeComment("skipping tag: " + tagname);
        //xmlWriter.writeCharacters("\n");
    }

}

