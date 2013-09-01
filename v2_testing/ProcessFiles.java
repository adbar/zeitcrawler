/*	This is part of the Zeitcrawler (http://code.google.com/p/zeitcrawler/).
	Copyright (C) Adrien Barbaresi 2013.
	This is free software, released under the GNU GPL v3 license (http://www.gnu.org/licenses/gpl.html).

	WORK IN PROGRESS ! This is an experimental software part.
*/


import java.io.File;
import javax.xml.stream.*;
import javax.xml.transform.stream.StreamSource;


/**
...
*/
public class ProcessFiles {

    private static int filecounter = 0;
    private static String dirname;
    private static boolean bHead, bBody, bAuthor, bTag, bDate, bUuid, bSupertitle, bTitle, bSubtitle;

    /*
    * Main
    */
    public static void main(String[] args) {
        // Load and test args
        dirname = args[0];
        File folder = new File(dirname);
        File[] listOfFiles = folder.listFiles();
        if (listOfFiles != null) {
            printResults(listOfFiles.length);
            // File loop
            for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].isFile()) {
                    enumerateFiles(listOfFiles[i].toString());
                    // Try-catch read XML
                    try {
                        XMLparseFile(listOfFiles[i]);
                    }
                    catch(XMLStreamException e){
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
    * Enumerate the files
    */
    private static void enumerateFiles(String name) {
        System.out.println(++filecounter + "\t" + name);
    }


    /*
    * Scan the XML Files and print out an event
    */
    private static void XMLparseFile(File fh) throws XMLStreamException {
        // create StreamReader
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLStreamReader stax = inputFactory.createXMLStreamReader( new StreamSource( fh ) );
        //int event = stax.getEventType();
        while( stax.hasNext() ) {
            int event = stax.next();

            switch(event) {

                /* START ELEM */
                case XMLStreamConstants.START_ELEMENT:

                    /* Head and body detection */
                    if ( stax.getLocalName().equals( "head" ) ) {
                        bHead = true;
                    }
                    else if ( stax.getLocalName().equals( "body" ) ) {
                        bHead = false;
                        bBody = true;
                    }
                    else if ( stax.getLocalName().equals( "teaser" ) ) {
                        bBody = false; // something else ?
                    }

                    /* head */
                    if (bHead) {

                        /* Reference */
                        if ( stax.getLocalName().equals( "reference" ) ) {
                            int attributeCount = stax.getAttributeCount();
                            for (int i = 0; i < attributeCount; i++) {
                                if ( stax.getAttributeLocalName(i).equals("author")  ) {
                                    System.out.println("ref. author: " + stax.getAttributeValue(i));
                                }
                            }
                        }

                        /* Attribute */
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
                                    if ( stax.getAttributeValue(i).equals("date-last-modified") ) { 
                                        bDate = true;
                                    }
                                    // UUID
                                    if ( stax.getAttributeValue(i).equals("uuid") ) { 
                                        bUuid = true;
                                    }
                                }

                            }
                        }

                        /* Tag search */
                        else if ( stax.getLocalName().equals( "tag" ) ) {
                            bTag = true;
                        }

                    }

                    /* body */
                    if (bBody) {

                        /* Titles search */
                        if ( bBody && stax.getLocalName().equals( "supertitle" ) ) {
                            bSupertitle = true;
                        }
                        else if ( bBody && stax.getLocalName().equals( "title" ) ) {
                            bTitle = true;
                        }
                        else if ( bBody && stax.getLocalName().equals( "subtitle" ) ) {
                            bSubtitle = true;
                        }

                    }

                    break;
                 
                /* CHARACTERS */
                case XMLStreamConstants.CHARACTERS:
                    /* head */
                    if (bHead) {
                        if (bAuthor) {
                            System.out.println("author: " + stax.getText());
                            bAuthor = false;
                        }
                        else if (bDate) {
                            System.out.println("date: " + stax.getText());
                            bDate = false;
                        }
                        else if (bTag) {
                            System.out.println("tag: " + stax.getText());
                            bTag = false;
                        }
                        else if (bUuid) {
                            System.out.println("uuid: " + stax.getText().substring(10, 46));
                            bUuid = false;
                        }
                    }
                    /* body */
                    if (bBody) {
                        if (bSupertitle) {
                            System.out.println("supertitle: " + stax.getText());
                            bSupertitle = false;
                        }
                        else if (bTitle) {
                            System.out.println("title: " + stax.getText());
                            bTitle = false;
                        }
                        else if (bSubtitle) {
                            System.out.println("subtitle: " + stax.getText());
                            bSubtitle = false;
                        }
                    }
                    break;

                /* END BODY */
                case XMLStreamConstants.END_ELEMENT:
                    if ( stax.getLocalName().equals( "body" ) ) {
                        bBody = false;
                    }
                    break;

                /* END DOCUMENT: close file */
                case XMLStreamConstants.END_DOCUMENT:
                    // System.out.println( "END_DOCUMENT: " );
                    stax.close();
                    break;

                /* DEFAULT: don't do anything */
                default:
                    break;


            }
        }
    }

}


//            String name = ( stax.hasName() ) ? stax.getLocalName().trim() : null;
//            String text = ( stax.hasText() ) ? stax.getText().trim() : null;

