package com.lcg.study.jksj.jgsxly.w5;

/**
 * @description： 一致性hash算法带虚拟节点以及分布数量的标准差计算
 */
import java.util.*;
import java.util.stream.Collectors;

/**
 * 带虚拟节点的一致性Hash算法
 */
public class ConsistentHashingWithVirtualNode
{
    /**
     * 待添加入Hash环的服务器列表
     */
    private static String[] servers = {"192.168.0.0:111", "192.168.0.1:111", "192.168.0.2:111",
            "192.168.0.3:111", "192.168.0.4:111","192.168.0.5:111", "192.168.0.6:111", "192.168.0.7:111",
            "192.168.0.8:111", "192.168.0.9:111"};

    /**
     * 真实结点列表
     */
    private static List<String> realNodes = new LinkedList<String>();

    /**
     * 虚拟节点，key表示虚拟节点的hash值，value表示虚拟节点的名称
     */
    private static TreeMap<Integer, String> virtualNodes =
            new TreeMap<Integer, String>();

    /**
     * 虚拟节点的数目
     */
    private static final int VIRTUAL_NODES = 150;

    static
    {
        // 先把原始的服务器添加到真实结点列表中
        for (int i = 0; i < servers.length; i++)
            realNodes.add(servers[i]);

        // 再添加虚拟节点，遍历LinkedList使用foreach循环效率会比较高
        for (String str : realNodes)
        {
            for (int i = 0; i < VIRTUAL_NODES; i++)
            {
                String virtualNodeName = str + "&&VN" + String.valueOf(i);
                int hash = getHash(virtualNodeName);
                System.out.println("虚拟节点[" + virtualNodeName + "]被添加, hash值为" + hash);
                virtualNodes.put(hash, virtualNodeName);
            }
        }
        System.out.println();
    }

    /**
     * 使用FNV1_32_HASH算法计算服务器的Hash值,这里不使用重写hashCode的方法，最终效果没区别
     */
    private static int getHash(String str)
    {
        final int p = 16777619;
        int hash = (int)2166136261L;
        for (int i = 0; i < str.length(); i++)
            hash = (hash ^ str.charAt(i)) * p;
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;

        // 如果算出来的值为负数则取其绝对值
        if (hash < 0)
            hash = Math.abs(hash);
        return hash;
    }

    /**
     * 得到应当路由到的结点
     */
    private static String getServer(String node)
    {
        // 得到带路由的结点的Hash值
        int hash = getHash(node);
        // 向右寻找第一个 key
        Map.Entry<Integer, String> subEntry= virtualNodes.ceilingEntry(hash);
        // 第一个Key就是顺时针过去离node最近的那个结点
        subEntry = subEntry == null ? virtualNodes.firstEntry() : subEntry;
        // 返回对应的虚拟节点名称，这里字符串稍微截取一下
        String virtualNode = subEntry.getValue();
        return virtualNode.substring(0, virtualNode.indexOf("&&"));
    }

    /**
     * 构造测试数据
     * @return
     */
    public static List<String> getInitTestData(){
        List<String> dataList=new ArrayList<>();
         for (int i=1;i<=1000000;i++){
             dataList.add(i+"");
         }
         return dataList;
    }

    /**
     * 求标准差
     * @param datas
     * @return
     */
    public static Double getBzc(List<Integer> datas){
        if (datas==null||datas.size()==0){
            return 0.00;
        }
        Double avg=datas.stream().collect(Collectors.averagingLong(Integer::intValue));
        int total=0;
        for(int i=0;i<datas.size();i++){
            total += (datas.get(i)-avg)*(datas.get(i)-avg);   //求出方差
        }
        return Math.sqrt(total/datas.size());   //求出标准差
    }


    public static void main(String[] args)
    {
        List<String> nodes = getInitTestData();
        List<Integer> counts = new ArrayList<>();
        Map<String,Integer> countMap=new HashMap<>();
        for (int i = 0; i < nodes.size(); i++){
        int hash=getHash(nodes.get(i));
            String node=getServer(nodes.get(i));
            Integer count=countMap.get(node);
            if (count==null){
                countMap.put(node,1);
            }else{
                countMap.put(node,count+1);
            }
            System.out.println("[" + nodes.get(i) + "]的hash值为" +hash + ", 被路由到结点[" + node + "]");
        }

        for (Map.Entry<String, Integer> entry:countMap.entrySet()){
            counts.add(entry.getValue());
            System.out.println(entry.getKey()+"的数量：" +entry.getValue());
        }

        Double bzc=getBzc(counts);

        System.out.println("标准差为" +bzc);
    }
}
