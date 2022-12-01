package org.example;

public class TNode implements Comparable<TNode>{
    public byte value;
    public int fre;

    public TNode left;
    public TNode right;

    public boolean isLeaf;

    public TNode(byte value, int fre){
        this.value = value;
        this.fre = fre;
        this.isLeaf = true;
    }

    public TNode(int fre){
        this.fre = fre;
        this.isLeaf = false;
    }

    public int compareTo(TNode tNode) {
        return this.fre - tNode.fre;
    }
}
