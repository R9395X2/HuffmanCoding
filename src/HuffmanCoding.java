import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class HuffmanCoding {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String address = "src/test.txt"; //可改为用户自行输入
        //可改为根据开头是否有字典自动判断
        System.out.println("输入数字: (1压缩, 2解压)");
        switch (sc.nextInt()) { //增强Switch语句仅限jdk12之后
            case 1 -> compress(address);
            case 2 -> decompression(address);
        }
    }
    private static void compress(String address) { //压缩
        String s=load(address);  //读取文件
        Map<Character,Integer>map=new HashMap<>();   //统计频率放入List
        for(int i=0;i<s.length();i++){
            char c=s.charAt(i);
            map.merge(c, 1, Integer::sum);
        }
        List<Node> nodes=new ArrayList<>();
        List<Node> chars=new ArrayList<>();
        for (Map.Entry<Character, Integer> entry : map.entrySet()) {
            nodes.add(new Node(entry.getKey(), entry.getValue()));
        }
        HuffmanTree.createTree(nodes,chars); //生成Huffman树
        for(int i=0;i<chars.size();i++){  //按Huffman树生成Huffman编码
            Node pointer=chars.get(i);
            while(pointer.getParent()!=null){
                if(pointer.isLeftChild())
                    chars.get(i).code="0"+chars.get(i).code;
                if(pointer.isRightChild())
                    chars.get(i).code="1"+chars.get(i).code;
                pointer=pointer.getParent();
            }
        }
        //生成字典: "/"分割键值对, ":"分割key和value 优化：可以把字典一起转为二进制
        StringBuilder index= new StringBuilder();
        for(Node aChar:chars){
            index.append(aChar.key).append(":").append(aChar.code).append("/");
        }
        //替换正文
        String body=s;
        for(Node aChar:chars) {
            body = body.replace(Character.toString(aChar.key), aChar.code);
        }
        //转换为二进制
        String str=body;
        char num1="1".charAt(0);
        char tmp;
        byte[] target = new byte[str.length()/8+1];
        for(int i=0;i<str.length();i++){
            tmp=str.charAt(i);
            if(tmp==num1) {
                if (i % 8 == 0) {
                    target[i / 8] = (byte) ((int) target[i / 8] | 0x80);
                }
                if (i % 8 == 1) {
                    target[i / 8] = (byte) ((int) target[i / 8] | 0x40);
                }
                if (i % 8 == 2) {
                    target[i / 8] = (byte) ((int) target[i / 8] | 0x20);
                }
                if (i % 8 == 3) {
                    target[i / 8] = (byte) ((int) target[i / 8] | 0x10);
                }
                if (i % 8 == 4) {
                    target[i / 8] = (byte) ((int) target[i / 8] | 0x8);
                }
                if (i % 8 == 5) {
                    target[i / 8] = (byte) ((int) target[i / 8] | 0x4);
                }
                if (i % 8 == 6) {
                    target[i / 8] = (byte) ((int) target[i / 8] | 0x2);
                }
                if (i % 8 == 7) {
                    target[i / 8] = (byte) ((int) target[i / 8] | 0x1);
                }
            }
        }
        save(index.toString(),"src/test.index"); //输出字典文件
        try(OutputStream output=new FileOutputStream("src/test.huffman")) { //输出
                output.write(target);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("压缩成功！");
    }
    private static void decompression(String address) { //解压
        String body="";   //读取文件
        try(InputStream input= new FileInputStream("src/test.huffman")) {
            StringBuilder sb=new StringBuilder();
            for (; ; ) {
                int n = input.read();
                if (n == -1)
                    break;
                String nstr=Integer.toBinaryString(n&0xFF);
                while (nstr.length()!=8){
                    nstr="0"+nstr;
                }
                sb.append(nstr);
            }
            body=new String(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        String idxtmp=load("src/test.index");  //读字典
        String[] indexes=idxtmp.split("/");
        Map<String,String> index=new HashMap<>();
        for (String str : indexes) {
            index.put(str.substring(0, str.indexOf(':')),str.substring(str.indexOf(':')+1) );
        }

        StringBuilder res= new StringBuilder(); //解码结果res
        a:while (body.length()>0) { //解码
             for (Map.Entry<String, String> entry : index.entrySet()) {
                if (body.startsWith(entry.getValue())) {
                    res.append(entry.getKey());
                    body=body.substring(entry.getValue().length());
                }
                if(!body.contains("1"))
                    break a;
            }
        }
        save(res.toString(),address); //保存文件
        System.out.println("解压成功！");
    }
    private static String load(String address){ //读取文件  可以设置缓存一次多读几个字
        try (Reader reader = new FileReader(address, StandardCharsets.UTF_8)) {
            StringBuilder sb=new StringBuilder();
            for (; ; ) {
                int n = reader.read();
                if (n == -1)
                    break;
                sb.append((char) n);
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
    private static void save(String s,String address){ //保存文件
        try (Writer writer=new FileWriter(address,StandardCharsets.UTF_8)){
            writer.write(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
class Node implements Comparable<Node>{
    char key;
    int weight;
    String code="";

    private Node parent;
    private Node left;
    private Node right;

    public void setLeft(Node left) {
        this.left = left;
    }
    public void setRight(Node right) {
        this.right = right;
    }
    public void setParent(Node parent) {
        this.parent = parent;
    }
    public Node getLeft() {
        return left;
    }
    public Node getRight() {
        return right;
    }
    public Node getParent() {
        return parent;
    }
    public boolean isLeftChild(){
        return this == this.parent.getLeft();
    }
    public boolean isRightChild(){
        return this == this.parent.getRight();
    }
    public Node(char key, int weight) {
        this.key = key;
        this.weight = weight;
    }
    @Override
    public int compareTo(Node o){
        if(o.weight>this.weight){
            return 1;
        }else if(o.weight<this.weight){
            return -1;
        }
        return 0;
    }
}
class HuffmanTree{
    public static Node createTree(List<Node> nodes,List<Node> chars){
        while (nodes.size()>1){
            Collections.sort(nodes);
            Node left=nodes.get(nodes.size()-1);
            Node right=nodes.get(nodes.size()-2);
            Node parent=new Node('\0', left.weight+right.weight);
            parent.setLeft(left);
            parent.setRight(right);
            left.setParent(parent);
            right.setParent(parent);
            if(left.key!='\0')
                chars.add(left);
            if(right.key!='\0')
                chars.add(right);
            nodes.remove(left);   //移除原来的节点
            nodes.remove(right);
            nodes.add(parent);   //加入新节点
        }
        return nodes.get(0); //返回最后的唯一一个节点
    }
}
