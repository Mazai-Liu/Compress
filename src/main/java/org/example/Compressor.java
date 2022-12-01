package org.example;

import org.junit.Test;

import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Queue;

public class Compressor {
    public static int[] frequency = new int[256];

    /**
     * 在字符编码方面，ASCII码为标准符号、数字、英文等进行了保留，取值范围是0～127，还有一部分作为扩展ASCII码128～255
     * 当操作系统采用非ASCII编码时(比如汉字编码)，一般用扩展ASCII码来进行，约定用128～255范围的编码连续2～3甚至4个来进行汉字编码
     *
     * 128~255是无符号的表示，有符号的8位byte只能表示-128~127.
     *
     * OFFSET作为，值为负数的字节的偏移量。
     */
    public static final int OFFSET = 128;

    public static final byte MAGIC = 'k';

    public static int ORIGIN_TOTAL;

    public static HashMap<Byte, String> encodeTable = new HashMap();

    public TNode root;

    public String fileName;
    public String pathPrefix;

    public static final int MOD = 8;

    public int totalCompressedBits;

    public Compressor(){

    }


    public void compress(String filePath){
        long start = System.currentTimeMillis();
        // 获取字符出现频率
        readFrequency(filePath);

        // 根据频率表，构建哈弗曼树
        makeHuffman();

        // 根据哈弗曼树，构建编码表（字节->编码）
        makeCodeTable();

        // 根据编码表，对源文件进行字节压缩
        doCompression(filePath);

        long end = System.currentTimeMillis();
        System.out.println("compress cost time: " + (end - start) + "ms");
    }

    public void readFrequency(String filePath){
        long start = System.currentTimeMillis();

        int last = filePath.lastIndexOf("/");
        fileName = filePath.substring(last + 1);
        pathPrefix = filePath.substring(0, last + 1);

        BufferedInputStream in = null;
        int total = 0;
        try {
            in = new BufferedInputStream(new FileInputStream(filePath));

            byte[] buf = new byte[1024 * 10];
            int left;
            while((left = in.read(buf)) > 0){
                // 统计字符出现频率
                for(int i = 0;i < left;i++)
                    frequency[buf[i] + OFFSET]++;
                total += left;
                ORIGIN_TOTAL = total;
            }
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try {
                in.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("origin file size:" + total + " bytes");

        long end = System.currentTimeMillis();

        System.out.println("make frequencyTable cost: " + (end - start) + "ms");
    }

    public void makeHuffman(){
        long start = System.currentTimeMillis();

        // make original queue
        Queue<TNode> queue = new PriorityQueue();
        for(int i = 0;i < 256;i++){
            if(frequency[i] > 0){
                queue.add(new TNode((byte)(i - OFFSET), frequency[i]));
            }
        }

        while(queue.size() > 1){
            TNode left = queue.poll();
            TNode right = queue.poll();

            TNode newNode = new TNode(left.fre + right.fre);
            newNode.left = left;
            newNode.right = right;

            queue.add(newNode);
        }
        root = queue.poll();

        long end = System.currentTimeMillis();
        System.out.println("make tree cost: " + (end - start) + "ms");
    }

    public void makeCodeTable(){
        long start = System.currentTimeMillis();
        dfs(root, "");

        long end = System.currentTimeMillis();
        System.out.println("make encodeTable cost: " + (end - start) + "ms");
    }

    void dfs(TNode root, String code){
        if(root.isLeaf){
            encodeTable.put(root.value, code);
            return;
        }
        dfs(root.left, code + "0");
        dfs(root.right, code + "1");
    }

    public void doCompression(String filePath){
        BufferedInputStream in = null;
        BufferedOutputStream out = null;
        try {
            in = new BufferedInputStream(new FileInputStream(filePath));

            String outputFile = pathPrefix + fileName + ".k";
            File file = new File(outputFile);
            if(!file.exists())
                file.createNewFile();
            out = new BufferedOutputStream(new FileOutputStream(file));

//             format:
//             magic total lengthOfEncodeTable EncodeTable lengthOfCompressedData CompressedData
//              1B     4B        4B               xB             4B                   xB
            out.write(MAGIC);
            writeInt(out, ORIGIN_TOTAL);

            System.out.println("magic:" + MAGIC);
            System.out.println("originTotal:" + ORIGIN_TOTAL);

            writeInt(out, 256);
            writeFre(out);
            System.out.println("frequencyTable size: " + 256 * 4);

            writeCompressedData(out, in);

            out.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                out.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void writeFre(BufferedOutputStream out) throws IOException {
        for(int i = 0;i < 256;i++) {
            writeInt(out, frequency[i]);
        }
    }

    private void writeCompressedData(BufferedOutputStream out, BufferedInputStream in) throws IOException {
        long start = System.currentTimeMillis();
        byte[] buf = new byte[1024];
        int left;

        StringBuilder sb = new StringBuilder(100000);
        while((left = in.read(buf)) > 0){
            for(int i = 0;i < left;i++){
                sb.append(encodeTable.get(buf[i]));
            }
        }
        String codes = sb.toString();

        long end1 = System.currentTimeMillis();
        System.out.println("make codes cost: " + (end1 - start) + "ms");

        totalCompressedBits = codes.length();
        System.out.println("totalCompressedBits:" + totalCompressedBits + ", Bytes: " + totalCompressedBits / 8 + ", bits: " + totalCompressedBits % 32);
        writeInt(out, totalCompressedBits);

        int i;
        for(i = 0;i + 4 * MOD <= totalCompressedBits;i += 4 * MOD){
            writeBits(out, codes.substring(i, i + 4 * MOD), 32);
        }
        if(i < totalCompressedBits){
            writeBits(out, codes.substring(i), totalCompressedBits - i);
        }

        long end2 = System.currentTimeMillis();
        System.out.println("make compressedData cost: " + (end2 - end1) + "ms");
    }
    private void writeBits(BufferedOutputStream out, String bits, int len) throws IOException {
        int to_write = 0;
        for(int i = 0;i < 32;i++){
            int bit;
            if(i >= len)
                bit = 0;
            else
                bit = bits.charAt(i) - '0';
            to_write |= ((1 & bit) << (32 - i - 1));
        }

        writeInt(out, to_write);
    }

    public static void writeInt(BufferedOutputStream out, int v) throws IOException {
        byte[] buf = new byte[4];
        buf[0] = (byte) ((v >>> 24) & 0xFF);
        buf[1] = (byte) ((v >>> 16) & 0xFF);
        buf[2] = (byte) ((v >>> 8) & 0xFF);
        buf[3] = (byte) (v & 0xFF);
        out.write(buf);
    }
}
