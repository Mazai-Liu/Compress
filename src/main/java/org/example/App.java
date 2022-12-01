package org.example;

import java.io.IOException;

public class App 
{
    /**
     * 1 构造字节出现的频率表
     * 2 根据频率表，构造哈弗曼树
     * 3 根据哈弗曼树，构造编码表（字节 --> 编码）
     * 4 根据编码表，把源文件按字节编码，按一定格式生成压缩文件
     * 5 按相同原理解码
     * @param args
     * @throws IOException
     */
    public static void main( String[] args ) throws IOException {
        Compressor compressor = new Compressor();
        compressor.compress("data/goupi.txt");

        System.out.println("\nDecompress:");
        Decompressor decompressor = new Decompressor();
        decompressor.deCompress("data/goupi.txt.k");
    }
}
