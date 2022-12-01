package org.example;

import java.io.IOException;

public class App 
{
    public static void main( String[] args ) throws IOException {
        Compressor compressor = new Compressor();
        compressor.compress("data/goupi.txt");

        System.out.println("\nDecompress:");
        Decompressor decompressor = new Decompressor();
        decompressor.deCompress("data/goupi.txt.k");
    }
}
