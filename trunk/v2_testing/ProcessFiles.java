/*	This is part of the Zeitcrawler (http://code.google.com/p/zeitcrawler/).
	Copyright (C) Adrien Barbaresi 2013.
	This is free software, released under the GNU GPL v3 license (http://www.gnu.org/licenses/gpl.html).

	WORK IN PROGRESS ! This is an experimental software component.
*/


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.xml.stream.*;
import javax.xml.transform.stream.StreamSource;
import java.text.Normalizer;
import java.net.URLEncoder;


/**
 * Description...
 */
public class ProcessFiles {

    private int fileCounter, foundFiles;
    private String dirName, exportName;
    private static ArrayList<File> globalListOfFiles = new ArrayList<File>();
    private String tagBuffer, pBuffer, lastBuffer;
    private boolean verboseFlag;
    private boolean isSourceBody, isSourceHead, isParagraph, isSkip, isGallery, isOtherTag, isBody, isTable, isRoot, isDoubleP;
    private boolean dateFound;
    private String articleType;
    private XMLStreamReader stax;
    private XMLStreamWriter xmlWriter;
    private HashMap<String, String> attributesMap = new HashMap<String, String>(16);

    /* attribute values to write */
    private static Set<String> attributeValues = new HashSet<String>(Arrays.asList(
        new String[] {"author","copyrights","date-last-modified","last_modified_by","ressort","sub_ressort","type","uuid","volume","year"}
    ));
    /* tags to skip at all times (plus the other tags they contain) */
    private static Set<String> tagsToSkip = new HashSet<String>(Arrays.asList(
        new String[] {"audio-link","entry","form","gallery","image","image-credits","indexteaser","infobox","object","references","sup","teaser","timeline"}
    ));
    /* tags to buffer within paragraphs */
    private static Set<String> bufferedWithinP = new HashSet<String>(Arrays.asList(
        new String[] {"b","em","i","li","span","strong","sub","tr","u"}
    ));
    /* tags to skip within paragraphs */
    private static Set<String> tagsToSkipWithinP = new HashSet<String>(Arrays.asList(
        new String[] {"br","cite","defanghtml_span","div","font","iframe","line","link","meta","img","image","p","raw","script","span","style","supertitle","table","td","ul"}
    ));
    /* rest of the tags known outside paragraphs (for error checking) */
    private static Set<String> knownTagsRest = new HashSet<String>(Arrays.asList(
        new String[] {"a","audio","b","block","bibliografie-info","blockquote","body","br","bu","byline","caption","column","container","copyright","description","div","division","em","iframe","image-credits","img","link","portraitbox","relateds","small","script","strong","tbody","td","text","th","thead","thumbnail","tr","ul","video"}
    ));

    /*
     * Main: test arguments
     */
    public static void main(String[] args) {
        ProcessFiles pf = new ProcessFiles();
        // sanity check
        if ( (args.length < 1) && (args.length > 2) ) {
            System.err.println("Error, parameter missing and/or too many args:\n java ProcessFiles directory/");
        }
        else {
            // set verbose boolean
            if ( (args.length == 2) && (args[1].equals("-v")) ) {
                pf.verboseFlag = true;
            }
            // directory traversal, looking for files
            try {
                pf.lookForFiles(args[0]);
            }
            catch (Exception e) {
                //e.printStackTrace();
                System.err.println("Problem during directory traversal, parts of the program were not run.");
            }
            // file processing
            if (globalListOfFiles != null) {
                System.out.println("Found " + globalListOfFiles.size() + " files.");
                for (int i = 0; i < globalListOfFiles.size(); i++) {
                    try {
                        pf.processFile(globalListOfFiles.get(i));
                    }
                    catch (Exception e) {
                        //e.printStackTrace();
                        System.err.println("Problem with file:" + globalListOfFiles.get(i).toString() + ", parts of the program were not run.");
                    }
                }
            }
        }
    }

    /*
     * Traverse all subdirectories and populate file list
     */
    public void lookForFiles(String objectname) throws Exception {
        File object = new File(objectname);
        if (object.isDirectory()) {
            System.out.println("Opening directory: " + object.toString());
            File[] listOfFiles = object.listFiles();
            if (listOfFiles != null) {
                foundFiles = 0;
                for (int i = 0; i < listOfFiles.length; i++) {
                    // add to file list
                    if (listOfFiles[i].isFile()) {
                        // getAbsoluteFile(), getName(), getPath()
                        fileSafeAdd(listOfFiles[i]);
                    }
                    // add to directory list
                    else if (listOfFiles[i].isDirectory()) {
                        lookForFiles(listOfFiles[i].toString());
                    }
                }
                System.out.println("Found " + foundFiles + " files in directory " + objectname);
            }
        }
        // add to list
        else if (object.isFile()) {
            fileSafeAdd(object);
        }
        else {
            throw new Exception(object.toString() + "will not be processed, it is neither a file nor a directory.");
        }
    }

    /*
     * File sanity check before adding to list
     */
    private void fileSafeAdd(File file) throws Exception {
        String filename = file.toString();
        if (filename.endsWith(".xml")) {
            if ("_export" != filename.substring(filename.length() - 11, filename.length() - 4).intern()) {
                // important (NFD normalization below)
                System.out.println(URLEncoder.encode(filename, "UTF-8"));
                filename = normalizeUnicode(filename);
                System.out.println(URLEncoder.encode(filename, "UTF-8"));
                File fileNormalized = new File(filename);
                System.out.println(URLEncoder.encode(fileNormalized.getName(), "UTF-8"));
                globalListOfFiles.add(fileNormalized);
                foundFiles++;
                if (verboseFlag) {
                    System.out.println("File name: " + filename);
                }
            }
        }
    }

    /*
     * Noisy bug: Normalize to "Normalization Form Canonical Decomposition" (NFD)
     * http://stackoverflow.com/questions/3610013/file-listfiles-mangles-unicode-names-with-jdk-6-unicode-normalization-issues
     */

    protected String normalizeUnicode(String str) {
        // NFC or NFD, both not working
        Normalizer.Form form = Normalizer.Form.NFD;
        if (! Normalizer.isNormalized(str, form)) {
            return Normalizer.normalize(str, form);
        }
        return str;
    }

    /*
     * Read file
     */
    private void processFile(File filename) throws Exception {
        enumerateFiles(filename.toString());
        // Try-catch read XML
        try {
            XMLparseFile(filename);
        }
        
        catch (IOException e) {
            e.printStackTrace();
            throw new Exception("IO Error: file not found or cannot open file", e);
        }
        
        catch (XMLStreamException e) {
            e.printStackTrace();
            throw new Exception("XMLStream Error", e);
        }
        /*
        catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new Exception("File not found", e);
        }
        */
    }

    /*
     * Enumerate the files, process export name
     */
    public void enumerateFiles(String name) {
        System.out.println(++fileCounter + "\t" + name);
        exportName = name.substring(0, name.length() - 4) + "_export.xml";
        if (verboseFlag) {
            System.out.println("writing as file: " + exportName);
        }
    }

    /*
     * Scan the XML Files and print out an event
     */
    private void XMLparseFile(File fh) throws IOException, XMLStreamException {

        System.out.println(URLEncoder.encode(fh.toString(), "UTF-8"));
        // create StreamReader
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        stax = inputFactory.createXMLStreamReader(new StreamSource(fh));

        // create XMLStreamWriter output
        XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
        xmlWriter = xmlof.createXMLStreamWriter(new FileOutputStream(exportName), "UTF-8");

        // start
        xmlWriter.writeStartDocument("utf-8", "1.0");
        xmlWriter.writeCharacters("\n");
        xmlWriter.writeStartElement("root");
        xmlWriter.writeCharacters("\n");

        // find and write original URL
        String filename = fh.getName();
 
        //if ("xml.xml" != filename.substring(filename.length() - 7, filename.length()).intern()) {
        String slug = filename.substring(0, filename.lastIndexOf("."));
        String originalURL = "http://www.zeit.de/" + slug.replace("%_%", "/");
        xmlWriter.writeStartElement("source");
        xmlWriter.writeCharacters(originalURL);
        xmlWriter.writeEndElement();
        xmlWriter.writeCharacters("\n");

        // Get and print article type
        if ("news" == filename.substring(0,4).intern()) {
            articleType = "DPA";
        }
        else if (filename.substring(0,9).matches("\\d{4}%_%\\d{2}")) {
            articleType = "print";
        }
        else {
            articleType = "online";
        }

        // assume it is relevant
        isGallery = false;
        // boolean for root element (avoid gallery skippping when it is root)
        isRoot = true;
        // reset date boolean
        dateFound = false;

        // int event = stax.getEventType();
        while (stax.hasNext()) {
            processEvent(stax, xmlWriter);
            stax.next();
        }
        
        // End of processing
        xmlWriter.writeComment("end");
        xmlWriter.writeCharacters("\n");

        // Gallery detected
        if (isGallery) {
            if (verboseFlag) {
                System.out.println("Not relevant: gallery detected.");
            }
            articleType = "gallery";
        }

        // Document category here
        xmlWriter.writeStartElement("metatype");
        xmlWriter.writeCharacters(articleType);
        xmlWriter.writeEndElement();
        xmlWriter.writeCharacters("\n");

        // end
        xmlWriter.writeEndDocument();
        xmlWriter.close();
        stax.close();
        // verbose gallery and no date found output
        if (verboseFlag) {
            if (! dateFound) {
                System.out.println("No date found.");
            }
            System.out.println("End of file processing.");
        }
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
                // skip flag here
                if (!isSkip) {
                    processCharacters();
                }
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
    private boolean checkSkip(String tagName, boolean startSequence) throws XMLStreamException {
        boolean result = false;
        // tags to skip
        if (tagsToSkip.contains(tagName)) {
            if ( (! isSourceHead) && (! isSourceBody) ) { // if (isRoot) {
                result = false;
                isRoot = false;
            }
            else {
                result = true;
                // print skipping comment once
                if (startSequence) {
                    skippingComment(tagName);
                }
            }
        }
        // p leading to audio player
        else if ("p" == tagName.intern()) {
            // beginning of sequence
            if (startSequence) {
                int attributeCount = stax.getAttributeCount();
                if (attributeCount == 1) {
                    if ( (stax.getAttributeLocalName(0).equals("id")) && (stax.getAttributeValue(0).equals("audio_player")) ) {
                        result = true;
                        skippingComment("p id=\"audio_player\"");
                    }
                }
            }
            // end of sequence
            else {
                result = true;
            }
        }
        return result;
    }

    /* Print the selected events to STDOUT */
    private void printEvent(String tagName, String buffer) throws XMLStreamException {
        // Print to STDOUT
        if (verboseFlag) {
            System.out.println(tagName + ": " + buffer);
        }
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
        // tag name + boolean = start tag
        if (checkSkip(localName, true)) {
            // return ?
            isSkip = true;
        }

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
            else if ( (localName.equals("container")) || (localName.equals("division")) ) {
                // Mark end of metadata (head)
                if (!isBody) {
                    xmlWriter.writeEndElement();
                    xmlWriter.writeCharacters("\n");
                    xmlWriter.writeStartElement("body");
                    xmlWriter.writeCharacters("\n");
                    isBody = true;
                }
            }

            /* Detect galleries */
            if ( (! isSourceHead) && (! isSourceBody) ) {
                // do not do anything if it is a gallery
                if (localName.equals("gallery")) {
                  xmlWriter.writeComment("gallery detected");
                  xmlWriter.writeCharacters("\n");
                  isGallery = true;
                }
            }

            /* Head */
            if (isSourceHead) {

                /* Reference
                if (localName.equals("reference")) {
                    int attributeCount = stax.getAttributeCount();
                    for (int i = 0; i < attributeCount; i++) {
                        if (("publication-date" != stax.getAttributeLocalName(i).intern()) && ("expires" != stax.getAttributeLocalName(i).intern()) && ("type" != stax.getAttributeLocalName(i).intern())) {
                            attributesMap.put(stax.getAttributeLocalName(i), stax.getAttributeValue(i));
                        }
                    }
                    tagBuffer = "reference";
                } */

                /* Attributes */
                if (localName.equals("attribute")) {
                    int attributeCount = stax.getAttributeCount();
                    for (int i = 0; i < attributeCount; i++) {
                        if (stax.getAttributeLocalName(i).equals("name")) {
                            String attributeValue = stax.getAttributeValue(i);
                            if (attributeValues.contains(attributeValue)) {
                                // shorten tag name
                                if ("date-last-modified" == attributeValue.intern()) {
                                    tagBuffer = "date";
                                    dateFound = true;
                                }
                                else if ("date-first-release" == attributeValue.intern()) {
                                    tagBuffer = "date-alt";
                                    dateFound = true;
                                }
                                // other cases
                                else {
                                    tagBuffer = attributeValue;
                                }
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
                /* alternatives ids */
                else if ( (localName.equals("uniqueid")) || (localName.equals("uuid")) ) {
                    tagBuffer = localName;
                }
            }

            /* Body */
            if (isSourceBody) {

                /* Paragraphs */
                // order by tag length ?
                if (isParagraph) {
                    // stays in output
                    if (bufferedWithinP.contains(localName)) {
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
                    else if (localName.equals("entity")) {
                        int attributeCount = stax.getAttributeCount();
                        for (int i = 0; i < attributeCount; i++) {
                            if ("url_value" != stax.getAttributeLocalName(i).intern()) {
                                attributesMap.put(stax.getAttributeLocalName(i), stax.getAttributeValue(i));
                            }
                        }
                        tagBuffer = "entity";
                    }
                    // skipped
                    else if (tagsToSkipWithinP.contains(localName)) {
                        skippingComment(localName);
                        if (localName.equals("p")) {
                            isDoubleP = true;
                        }
                    }
                    // print out unknown
                    else {
                        if (verboseFlag) {
                            String errorMsg = "unknown tag in p: " + localName;
                            xmlWriter.writeComment(errorMsg);
                            xmlWriter.writeCharacters("\n");
                            System.err.println(errorMsg + "\tfile: " + exportName);
                        }
                    }
                }
                /* Titles */
                else if ( (localName.equals("supertitle")) || (localName.equals("title")) || (localName.equals("subtitle")) ) {
                    tagBuffer = localName;
                }
                /* Paragraphs (beginning) */
                else if (localName.equals("p")) {
                    xmlWriter.writeStartElement("p");
                    xmlWriter.writeCharacters("\n");
                    isParagraph = true;
                }
                /* Tables */
                else if (localName.equals("table")) {
                    isTable = true;
                    xmlWriter.writeComment("skipping table formatting");
                    xmlWriter.writeCharacters("\n");
                }
                /* Other */
                else if ( (localName.equals("raw")) || (localName.equals("intertitle")) || (localName.equals("li")) || (localName.equals("blocker")) ) {
                    // Turn lists into paragraphs
                    if ( (localName.equals("li")) || (localName.equals("blocker")) ) {
                        xmlWriter.writeStartElement("p");
                    }
                    else {
                        xmlWriter.writeStartElement(localName);
                    }
                    xmlWriter.writeCharacters("\n");
                    isOtherTag = true;
                }
                /* Citations */
                else if (localName.equals("citation")) {
                    int attributeCount = stax.getAttributeCount();
                    for (int i = 0; i < attributeCount; i++) {
                        if ("layout" != stax.getAttributeLocalName(i).intern()) {
                            attributesMap.put(stax.getAttributeLocalName(i), stax.getAttributeValue(i));
                        }
                    }
                    tagBuffer = "citation";
                }
                /* the rest (error handling) */
                else {
                    if (verboseFlag) {
                        if (! knownTagsRest.contains(localName) ) {
                            String errorMsg = "unknown tag: " + localName;
                            xmlWriter.writeComment(errorMsg);
                            xmlWriter.writeCharacters("\n");
                            System.err.println(errorMsg  + "\tfile: " + exportName);
                        }
                    }
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
                // avoid empty tags
                if (stax.getText() != null) {
                    // System.out.println(tagBuffer);
                    writeEvent(tagBuffer, stax.getText(), false);
                }
                tagBuffer = null;
            }
            else {
                if (isParagraph) {
                    if (pBuffer != null) {
                        pBuffer = pBuffer + stax.getText();
                    }
                    else {
                        pBuffer = stax.getText();
                    }
                }
                // this tag may contain other tags
                else {
                    trimWrite(stax.getText());
                }
            }
        }
        else if (isTable) {
            xmlWriter.writeCharacters(stax.getText());
            xmlWriter.writeCharacters("\n");
        }

        // generic processing: everything that's left
        if (tagBuffer != null) {
            trimWrite(pBuffer);
            String contents = stax.getText();
            if (contents != null) {
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
            }
            tagBuffer = null;
        }

    }
    
    /* Process end tag */
    private void processEndTag(String localName) throws XMLStreamException {
        // check if skip
        if (isSkip) {
            // end of skip tag
            // tag name + boolean = end tag
            if (checkSkip(localName, false)) {
                isSkip = false;
            }
            /*if (!isParagraph) {
                if (checkSkip(localName, false)) {
                    isSkip = false;
                }
            }
            else {
                if (tagsToSkipWithinP.contains(localName)) {
                    isSkip = false;
                }
            }*/
        }
        else {
            // end body
            if (localName.equals("body")) {
                isSourceBody = false;
                isBody = false;
                xmlWriter.writeComment("end body");
                xmlWriter.writeCharacters("\n");
                xmlWriter.writeEndElement();
                xmlWriter.writeCharacters("\n");
            }
            // end of paragraph
            else if (localName.equals("p")) {
                trimWrite(pBuffer);
                // avoid double depth paragraphs
                if (isDoubleP) {
                    isDoubleP = false;
                }
                else {
                    xmlWriter.writeCharacters("\n");
                    try {
                        xmlWriter.writeEndElement();
                    }
                    catch (XMLStreamException e) {
                        System.err.println("Error, element already closed: " + lastBuffer);
                    }
                    xmlWriter.writeCharacters("\n");
                    isParagraph = false;
                }
            }
            // end of other
            else if (isOtherTag) {
                if ( (localName.equals("raw")) || (localName.equals("intertitle")) || (localName.equals("li")) || (localName.equals("blocker")) ) {
                    xmlWriter.writeCharacters("\n");
                    xmlWriter.writeEndElement();
                    xmlWriter.writeCharacters("\n");
                    isOtherTag = false;
                }
            }
            // end of table
            else if (isTable) {
                if (localName.equals("table")) {
                    //xmlWriter.writeEndElement();
                    //xmlWriter.writeCharacters("\n");
                    isTable = false;
                }
            }
        }
    }

    /* Trim, write and empty paragraph buffer */
    private void trimWrite(String buffer) throws XMLStreamException {
        if ( (buffer != "") && (buffer != null) ) {
            //try {
            // trim string
            buffer = buffer.replace("\n", " ");
            buffer = buffer.replaceAll(" +", " ");
            // buffer = buffer.trim();
            // print string and end of element
            xmlWriter.writeCharacters(buffer);
            lastBuffer = buffer;
            pBuffer = null;
            //}
            /*catch (NullPointerException e) {
                System.err.println("error:" + buffer);
            }*/
        }
    }

}
