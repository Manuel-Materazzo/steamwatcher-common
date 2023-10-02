package com.steamwatcher.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class FileReaderUtils {

    private FileReaderUtils() {
    }

    /**
     * Searchs a file in the resources folder and on the current folder and gets an input stream
     *
     * @param filename name of the file to search for
     * @return inputstream of the file
     * @throws FileNotFoundException if no file is found with such name
     */
    public static InputStream getInputstream(String filename) throws FileNotFoundException {
        // provo a pescare il file dalle resources
        InputStream in = FileReaderUtils.class.getResourceAsStream("/files/" + filename);
        // se non è nel resources, provo con la cartella di esecuzione
        if (in == null) {
            in = new FileInputStream("./files/" + filename);
        }

        return in;
    }

    /**
     * Searchs a file in the resources folder and on the current folder and gets an buffered reader
     *
     * @param filename name of the file to search for
     * @return buffered reader of the file
     * @throws FileNotFoundException if no file is found with such name
     */
    public static BufferedReader getBufferedReader(String filename) throws FileNotFoundException {
        InputStream stream = getInputstream(filename);
        return new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
    }

}
