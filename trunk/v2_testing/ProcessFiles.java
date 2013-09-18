/*	This is part of the Zeitcrawler (http://code.google.com/p/zeitcrawler/).
	Copyright (C) Adrien Barbaresi 2013.
	This is free software, released under the GNU GPL v3 license (http://www.gnu.org/licenses/gpl.html).

	WORK IN PROGRESS ! This is an experimental software component.
*/


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import javax.xml.stream.*;
import javax.xml.transform.stream.StreamSource;


/**
 * Description...
 */
public class ProcessFiles {

    private int fileCounter;
    private String dirName, exportName;
    private String tagBuffer = null;
    private String pBuffer;
    private boolean isSourceBody, isSourceHead, isParagraph, isSkip, isRelevant, isOtherTag, isBody;
    private XMLStreamReader stax;
    private XMLStreamWriter xmlWriter;
    private HashMap<String, String> attributesMap = new HashMap<String, String>();

    /*
     * Main: test arguments
     */
    public static void main(String[] args) {
        ProcessFiles pf = new ProcessFiles();
        if (args.length != 1) {
            System.out.println("Error, parameter missing:\n java ProcessFiles directory/");
        }
        else {
            try {
                pf.processDir(args[0]);
            }
            catch (Exception e) {
                e.printStackTrace();
                System.err.println("Problem, parts of the program were not run.");
            }
        }
    }

    /*
     * Read files in directory
     */
    private void processDir(String dirname) throws Exception {
        File folder = new File(dirname);
        File[] listOfFiles = folder.listFiles();
        if (listOfFiles != null) {
            printResults(listOfFiles.length);
            // File loop
            for (int i = 0; i < listOfFiles.length; i++) {
                String filestring = listOfFiles[i].toString();
                // File check + file name check (do not process the export files)
                if ((listOfFiles[i].isFile()) && ("_export" != filestring.substring(filestring.length() - 11, filestring.length() - 4).intern())) {
                    enumerateFiles(filestring);
                    // Try-catch read XML
                    try {
                        XMLparseFile(listOfFiles[i]);
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                        throw new Exception("IO Error: file not found or cannot open file", e);
                    }
                    catch (XMLStreamException e) {
                        e.printStackTrace();
                        throw new Exception("XMLStream Error", e);
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
    private void printResults(int number) {
        if (number == 0) {
            System.out.println("The directory " + dirName + " does not exist or it is empty.");
        }
        else {
            System.out.println("Number of files in directory: " + number);
        }
    }

    /*
     * Enumerate the files, process export name
     */
    private void enumerateFiles(String name) {
        System.out.println(++fileCounter + "\t" + name);
        exportName = name.substring(0, name.length() - 4) + "_export.xml";
        System.out.println("Written as file: " + exportName);
    }

    /*
     * Scan the XML Files and print out an event
     */
    private void XMLparseFile(File fh) throws IOException, XMLStreamException {

        // create StreamReader
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        stax = inputFactory.createXMLStreamReader(new StreamSource(fh));

        // create XMLStreamWriter output
        XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
        xmlWriter = xmlof.createXMLStreamWriter(
                new FileOutputStream(exportName), "UTF-8");

        // start
        xmlWriter.writeStartDocument("utf-8", "1.0");
        xmlWriter.writeCharacters("\n");
        xmlWriter.writeStartElement("root");
        xmlWriter.writeCharacters("\n");
        // assume it is relevant
        isRelevant = true;

        while (stax.hasNext()) {
            processEvent(stax, xmlWriter);
            stax.next();
        }
        
        // End of processing
        xmlWriter.writeComment("end");
        xmlWriter.writeCharacters("\n");
        xmlWriter.writeEndElement();
        xmlWriter.writeEndDocument();
        xmlWriter.close();
        stax.close();
        if (! isRelevant) {
            System.out.println("Not relevant: gallery detected.");
        }
        System.out.println("End of file processing.");
    }

    /* Send event to subfunctions */
    private void processEvent(XMLStreamReader stax, XMLStreamWriter xmlWriter) throws XMLStreamException {
        switch (stax.getEventType()) {
            /* START ELEM */
            case XMLStreamConstants.START_ELEMENT:
                processStartElem(stax.getLocalName());
                break;
            /* CHARACTERS */
            case XMLStreamConstants.CHARACTERS:
                processCharacters();
                break;
            /* END ELEMENTS */
            case XMLStreamConstants.END_ELEMENT:
                processEndTag(stax.getLocalName());
                break;
            /* DEFAULT: don't do anything */
            default:
                break;
        }
    }

    /* Find tags to skip */
    private boolean checkSkip(String tagName) {
        boolean result = false;
        if ( ("infobox" == tagName.intern()) || ("image" == tagName.intern()) || ("indexteaser" == tagName.intern()) ) {
            result = true;
        }
        return result;
    }

    /* Print the selected events to STDOUT */
    private void printEvent(String tagName, String buffer) throws XMLStreamException {
        // Print to STDOUT
        System.out.println(tagName + ": " + buffer);
    }

    /* Write the selected events to XML */
    private void writeEvent(String tagName, String buffer, boolean bNewline) throws XMLStreamException {
        // Print to XML
        xmlWriter.writeStartElement(tagName);
        // check if there are attributes to write // .size() > 0
        if (!attributesMap.isEmpty()) {
            for (String item : attributesMap.keySet()) {
                xmlWriter.writeAttribute(item, attributesMap.get(item));
            }
            attributesMap.clear();
        }
        // write text contents, trim string
        xmlWriter.writeCharacters(buffer.trim());
        xmlWriter.writeEndElement();
        if (bNewline) {
            xmlWriter.writeCharacters("\n");
        }
    }

    /* Write a comment to indicate something has been skipped */
    private void skippingComment(String tagName) throws XMLStreamException {
        xmlWriter.writeComment("skipping <" + tagName + ">");
        xmlWriter.writeCharacters("\n");
    }

    /* Process a start tag */
    private void processStartElem(String localName) throws XMLStreamException {

        /* If skip flag is off */
        if (!isSkip) {

            /* Head and body detection */
            if (localName.equals("head")) {
                isSourceHead = true;
                xmlWriter.writeStartElement("head");
                xmlWriter.writeCharacters("\n");
            }
            else if (localName.equals("body")) {
                isSourceHead = false;
                isSourceBody = true;
            }
            else if (localName.equals("division")) {
                // Mark end of metadata
                if (!isBody) {
                xmlWriter.writeEndElement();
                xmlWriter.writeCharacters("\n");
                xmlWriter.writeStartElement("body");
                xmlWriter.writeCharacters("\n");
                isBody = true;
                }
            }
            else if (localName.equals("teaser")) {
                isSourceBody = false; // something else ?
                xmlWriter.writeEndElement();
                xmlWriter.writeCharacters("\n");
            }
            else if (checkSkip(localName)) {
                isSkip = true;
                skippingComment(localName);
            }

            /* Detect galleries */
            if ( (! isSourceHead) && (! isSourceBody) ) {
                // do not do anything if it is a gallery
                if (localName.equals("gallery")) {
                  xmlWriter.writeComment("gallery detected");
                  xmlWriter.writeCharacters("\n");
                  isRelevant = false;
                }
            }

            /* Head */
            if (isSourceHead) {

                /* Reference */
                if (localName.equals("reference")) {
                    int attributeCount = stax.getAttributeCount();
                    for (int i = 0; i < attributeCount; i++) {
                        if (("publication-date" != stax.getAttributeLocalName(i).intern()) && ("expires" != stax.getAttributeLocalName(i).intern()) && ("type" != stax.getAttributeLocalName(i).intern())) {
                            attributesMap.put(stax.getAttributeLocalName(i), stax.getAttributeValue(i));
                        }
                    }
                    tagBuffer = "reference";
                }

                /* Attributes */
                else if (localName.equals("attribute")) {
                    int attributeCount = stax.getAttributeCount();
                    for (int i = 0; i < attributeCount; i++) {
                        if (stax.getAttributeLocalName(i).equals("name")) {
                            // Author
                            if (stax.getAttributeValue(i).equals("author")) {
                                tagBuffer = "author";
                            }
                            // Copyright
                            else if (stax.getAttributeValue(i).equals("copyrights")) {
                                tagBuffer = "copyrights";
                            }
                            // Date
                            else if (stax.getAttributeValue(i).equals("date-last-modified")) {
                                tagBuffer = "date";
                            }
                            // Ressort
                            else if (stax.getAttributeValue(i).equals("ressort")) {
                                tagBuffer = "ressort";
                            }
                            // Sub-ressort
                            else if (stax.getAttributeValue(i).equals("sub_ressort")) {
                                tagBuffer = "sub_ressort";
                            }
                            // Type
                            else if (stax.getAttributeValue(i).equals("type")) {
                                tagBuffer = "type";
                            }
                            // UUID
                            else if (stax.getAttributeValue(i).equals("uuid")) {
                                tagBuffer = "uuid";
                            }
                            // Volume
                            else if (stax.getAttributeValue(i).equals("volume")) {
                                tagBuffer = "volume";
                            }
                            // Year
                            else if (stax.getAttributeValue(i).equals("year")) {
                                tagBuffer = "year";
                            }
                        }
                    }
                }

                /* Tag and keyword search */
                else if (localName.equals("keyword")) {
                    tagBuffer = localName;
                }
                else if (localName.equals("tag")) {
                    int attributeCount = stax.getAttributeCount();
                    for (int i = 0; i < attributeCount; i++) {
                        if ("url_value" != stax.getAttributeLocalName(i).intern()) {
                            attributesMap.put(stax.getAttributeLocalName(i), stax.getAttributeValue(i));
                        }
                    }
                    tagBuffer = "tag";
                }
            }

            /* Body */
            if (isSourceBody) {

                /* Paragraphs */
                if (isParagraph) {
                    if ( (localName.equals("strong")) || (localName.equals("em")) ) { // || (localName.equals("intertitle"))
                        tagBuffer = localName;
                    }
                    else if (localName.equals("a")) {
                        int attributeCount = stax.getAttributeCount();
                        for (int i = 0; i < attributeCount; i++) {
                            if (stax.getAttributeLocalName(i).equals("href")) {
                                attributesMap.put(stax.getAttributeLocalName(i), stax.getAttributeValue(i));
                            }
                        }
                        tagBuffer = "a";
                    }
                    else if (localName.equals("br")) {
                        skippingComment("br");
                    }
                    else if (localName.equals("entity")) {
                        int attributeCount = stax.getAttributeCount();
                        for (int i = 0; i < attributeCount; i++) {
                            if ("url_value" != stax.getAttributeLocalName(i).intern()) {
                                attributesMap.put(stax.getAttributeLocalName(i), stax.getAttributeValue(i));
                            }
                        }
                        tagBuffer = "entity";
                    }
                    else {
                        String errorMsg = "unknown tag: " + localName;
                        xmlWriter.writeComment(errorMsg);
                        xmlWriter.writeCharacters("\n");
                        System.out.println(errorMsg);
                    }
                }
                /* Titles */
                else if ( (localName.equals("supertitle")) || (localName.equals("title")) || (localName.equals("subtitle")) ) {
                    tagBuffer = localName;
                }

                /* Paragraphs */
                else if (localName.equals("p")) {
                    xmlWriter.writeStartElement("p");
                    xmlWriter.writeCharacters("\n");
                    isParagraph = true;
                }

                /* Other */
                else if ( (localName.equals("raw")) || (localName.equals("intertitle")) ) {
                    xmlWriter.writeStartElement(localName);
                    xmlWriter.writeCharacters("\n");
                    isOtherTag = true;
                }

            }
        }

    }
    
    /* Process characters */
    private void processCharacters() throws XMLStreamException {
        // paragraphs
        if ( (isParagraph) || (isOtherTag) ) {
            if (tagBuffer != null) {
                trimWrite(pBuffer);
                writeEvent(tagBuffer, stax.getText(), false);
                tagBuffer = null;
            }
            else {
                if (isParagraph) {
                    pBuffer = pBuffer + stax.getText();
                }
                // this tag may contain other tags
                else {
                    xmlWriter.writeCharacters(stax.getText());
                }
            }
        }

        // generic processing: everything that's left
        if (tagBuffer != null) {
            trimWrite(pBuffer);
            String contents = stax.getText();
            // test if the string should be modified
            if (tagBuffer.intern() == "date") {
                contents = contents.substring(0, 10);
            }
            else if (tagBuffer.intern() == "uuid") {
                contents = contents.substring(contents.length() - 37, contents.length() - 1);
            }
            // write it to XML
            writeEvent(tagBuffer, contents, true);
            // print some metadata to screen
            if ( (tagBuffer.intern() == "author") || (tagBuffer.intern() == "title") ) {
                printEvent(tagBuffer, contents);
            }
            tagBuffer = null;
        }

    }
    
    /* Process end tag */
    private void processEndTag(String localName) throws XMLStreamException {
        // end body
        if (localName.equals("body")) {
            isSourceBody = false;
            isBody = false;
            xmlWriter.writeComment("end body");
            xmlWriter.writeCharacters("\n");
        }
        // end of skip tag
        else if (checkSkip(localName)) {
            isSkip = false;
        }
        // end of paragraph
        else if (localName.equals("p")) {
            trimWrite(pBuffer);
            xmlWriter.writeCharacters("\n");
            xmlWriter.writeEndElement();
            xmlWriter.writeCharacters("\n");
            isParagraph = false;
        }

        // end of other
        if (isOtherTag) {
            if ( (localName.equals("raw")) || (localName.equals("intertitle")) ) {
                xmlWriter.writeCharacters("\n");
                xmlWriter.writeEndElement();
                xmlWriter.writeCharacters("\n");
                isOtherTag = false;
            }
        }
    }

    /* Trim, write and empty paragraph buffer */
    private void trimWrite(String buffer) throws XMLStreamException {
        if ( (buffer != "") && (buffer != null)) {
            //try {
            // trim string
            buffer = buffer.replace("\n", " ");
            buffer = buffer.replaceAll(" +", " ");
            buffer = buffer.trim();
            // print string and end of element
            xmlWriter.writeCharacters(buffer);
            pBuffer = "";
            //}
            /*catch (NullPointerException e) {
                System.err.println("error:" + buffer);
            }*/
        }
    }

}
