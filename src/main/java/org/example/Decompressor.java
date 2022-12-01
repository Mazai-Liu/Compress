package org.example;

import com.alibaba.fastjson.JSON;
import org.junit.Test;

import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Queue;

public class Decompressor {
    public static final int OFFSET = 128;
    public int checkTotalByte;
    public int tableLength;
    public int compressedDataLength;
    public String filePath;

    public int[] frequency = new int[256];

    public TNode root;

    public String fileName;
    public String pathPrefix;
    // format:
    // magic total lengthOfEncodeTable EncodeTable lengthOfCompressedData CompressedData
    //  1B     4B        4B               xB             4B                   xB

    public Decompressor(){

    }


    public void deCompress(String filePath){
        long start = System.currentTimeMillis();

        readHead(filePath);

        long end = System.currentTimeMillis();
        System.out.println("deCompress cost time: " + (end - start) + "ms");
    }

    private void process(FileInputStream in) {
        FileOutputStream out = null;
        try {
            File file = new File(pathPrefix + "k" + fileName);
            if(!file.exists())
                file.createNewFile();
            out = new FileOutputStream(file);

            byte[] buf = new byte[1];
            int solved = 0;
            TNode cur = root;
            while(in.read(buf) > 0){
                byte b = buf[0];
                for(int i = Math.min(7, compressedDataLength - solved);i >= 0;i--){
                    if(((b >> i) & 1) == 0)
                        cur = cur.left;
                    else
                        cur = cur.right;
                    if(cur.isLeaf){
                        out.write(cur.value);
                        cur = root;
                    }
                    solved ++;
                }
            }
            out.flush();
            System.out.println("solved bits: " + solved);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                in.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void readHead(String filePath){
        this.filePath = filePath;
        int last = filePath.lastIndexOf("/");
        fileName = filePath.substring(last + 1);
        pathPrefix = filePath.substring(0, last + 1);

        FileInputStream in = null;
        try {
            in = new FileInputStream(filePath);
            byte[] magic = new byte[1];
            in.read(magic);
            if(magic[0] != 'k'){
                System.out.println("file format error!");
                System.exit(-1);
            }
            checkTotalByte = readInt(in);

            tableLength = readInt(in);

            // 读取频率表
            readFre(in);

            // 构建哈弗曼树
            makeHuffman();

            compressedDataLength = readInt(in);

            // 按哈弗曼树解压
            process(in);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }finally {
            try {
                in.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void makeHuffman() {
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
    }

    private void readFre(FileInputStream in) throws IOException {
        for(int i = 0;i < tableLength;i++)
            frequency[i] = readInt(in);
    }

    public int readInt(FileInputStream in) throws IOException {
        byte[] buf = new byte[4];
        in.read(buf);
        int res = 0;
        res |=  (((buf[0] & 0xFF) << 24) |
                ((buf[1] & 0xFF) << 16) |
                ((buf[2] & 0xFF) << 8) |
                (buf[3] & 0xFF));
        return res;
    }
}
