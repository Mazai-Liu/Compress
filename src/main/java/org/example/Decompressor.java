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

    private void process(BufferedInputStream in) {
        BufferedOutputStream out = null;
        try {
            File file = new File(pathPrefix + "k" + fileName);
            if(!file.exists())
                file.createNewFile();
            out = new BufferedOutputStream(new FileOutputStream(file));

            byte[] buf = new byte[1024];
            byte[] buf_out = new byte[1024 * 12];
            int solved = 0, left, out_count = 0;
            TNode cur = root;
            while((left = in.read(buf)) > 0){
                for(int j = 0;j < left;j++){
                    byte b = buf[j];
                    for(int i = Math.min(7, compressedDataLength - solved);i >= 0;i--){
                        if(((b >> i) & 1) == 0)
                            cur = cur.left;
                        else
                            cur = cur.right;
                        if(cur.isLeaf){
                            buf_out[out_count++] = cur.value;
                            if(out_count == (1024 * 12)) {
                                out.write(buf_out);
                                out_count = 0;
                            }
                            cur = root;
                        }
                        solved ++;
                    }
                }
            }
            if(out_count != 0)
                out.write(buf_out, 0, out_count);
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
        this.filePath = filePath.substring(0, filePath.length() - 2);
        int last = this.filePath.lastIndexOf("/");
        fileName = this.filePath.substring(last + 1);
        pathPrefix = this.filePath.substring(0, last + 1);

        BufferedInputStream in = null;
        try {
            long start = System.currentTimeMillis();

            in = new BufferedInputStream(new FileInputStream(filePath));
            // 魔数
            byte[] magic = new byte[1];
            in.read(magic);
            if(magic[0] != 'k'){
                System.out.println("file format error!");
                System.exit(-1);
            }

            // 总大小
            checkTotalByte = readInt(in);

            // 频率表长
            tableLength = readInt(in);

            // 读取频率表
            readFre(in);
            long end1 = System.currentTimeMillis();
            System.out.println("make frequencyTable cost: " + (end1 - start) + "ms");

            // 构建哈弗曼树
            makeHuffman();
            long end2 = System.currentTimeMillis();
            System.out.println("make tree cost: " + (end2 - end1) + "ms");

            compressedDataLength = readInt(in);

            // 根据哈弗曼树解压
            process(in);
            long end3 = System.currentTimeMillis();
            System.out.println("make decompress cost: " + (end3 - end2) + "ms");
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

    private void readFre(BufferedInputStream in) throws IOException {
        for(int i = 0;i < tableLength;i++)
            frequency[i] = readInt(in);
    }

    /**
     * 读取四个字节并且组装为int
     * @param in
     * @return
     * @throws IOException
     */
    public int readInt(BufferedInputStream in) throws IOException {
        byte[] buf = new byte[4];
        in.read(buf);
        int res = 0;
        res |=  (((buf[0] & 0xFF) << 24) |
                ((buf[1] & 0xFF) << 16) |
                ((buf[2] & 0xFF) << 8) |
                (buf[3] & 0xFF));
        return res;
    }

//    public int bsToI(byte[] bytes) throws IOException {
//        int res = 0;
//        res |=  (((bytes[0] & 0xFF) << 24) |
//                ((bytes[1] & 0xFF) << 16) |
//                ((bytes[2] & 0xFF) << 8) |
//                (bytes[3] & 0xFF));
//        return res;
//    }
}
