
基于哈夫曼编码的单文件压缩/解压项目
---
> 项目语言：Java
---
## 哈夫曼编码

哈夫曼编码(Huffman Coding)，又称霍夫曼编码，是一种编码方式，哈夫曼编码是可变字长编码的一种。Huffman于1952年提出一种编码方法，该方法完全依据字符出现概率来构造异字头的平均长度最短的码字，有时称之为最佳编码，一般就叫做Huffman编码（有时也称为霍夫曼编码）。

通过构造哈弗曼树（也称最优二叉树），可以得到哈弗曼编码。

哈夫曼编码实现可以应用于数据压缩，主要有以下两点原因：

1. 哈夫曼编码是前缀编码，不会有二义性
2. 哈夫曼树即最优二叉树，是带权路径长度最短的树，故字符编码的总长最短

## 项目实现思路及流程
源文件每个字节存储所需8位bit，通过构造哈夫曼树得到带权路径最小的字节编码。此编码长度不一定是8位bit，且出现频率越高的字节，编码长度越短。

把每个字节按编码表写入压缩后的文件，解压时再根据相同原理解码。（解码不会有二义性）
### 1 构造字节出现的频率表

### 2 根据频率表，构造哈弗曼树

### 3 根据哈弗曼树，构造编码表（字节 --> 编码）

### 4 根据编码表，把源文件按字节编码，按一定格式生成压缩文件

### 5 按相同原理解码