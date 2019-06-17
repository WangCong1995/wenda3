package com.nowcoder.service;

import com.nowcoder.controller.IndexController;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Service
public class SensitiveService implements InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(IndexController.class);
    //树根什么都不干
    private TrieNode rootNode = new TrieNode();

    @Override
    public void afterPropertiesSet() throws Exception {
        //把刚刚那些敏感词都读取进来

        try {
            //【读取 敏感词文件】
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("SensitiveWords.txt");
            InputStreamReader read = new InputStreamReader(is);
            BufferedReader bufferedReader = new BufferedReader(read);

            String lineTxt;
            while ((lineTxt = bufferedReader.readLine()) != null) {  //每次读文件中的一行
                addWord(lineTxt.trim());    //去除lineTxt的前后的空格，然后将其作为关键词，加入字典树
            }
            //运行至此，敏感词文件就读取完了。字典树叶构建好了

            read.close();   //实际上应该当在finally里面，if判断一下，有没有打开错误什么的


        } catch (Exception e) {
            logger.error("读取敏感词文件失败" + e.getMessage());

        }
    }


    /* 1.把敏感词字典树给构造出来，初始化字典树的时候，只需要去读取 敏感词文件（SensitiveWords.txt）就行了 */

    /**
     * 【功能：增加字典树中的关键词】
     * 可用其来向字典树中一个一个的加入敏感词，从而构建好字典树。
     * 例如，加入“abc”，则需要构建 a-->b-->c 的路径。缺结点就补上
     *
     * @param lineTxt 敏感词
     */
    private void addWord(String lineTxt) {
        TrieNode tempNode = rootNode;  //第一个指针指向根节点。根节点什么都不做

        //遍历 lineTxt的每个字符,从根节点一直往下走
        for (int i = 0; i < lineTxt.length(); ++i) {
            Character c = lineTxt.charAt(i);//lineTxt的当前字符

            //当前结点的子结点中，有没有与lineTxt的当前字符c匹配的子节点
            TrieNode node = tempNode.getSubNode(c);//如果有子节点，继续往下走

            if (node == null) { //如果说没有子节点。说明当前结点下面，还没有这个结点。那需要新建一个结点，以完善字典树的敏感词路径
                node = new TrieNode();

                tempNode.addSubNode(c, node); //需要在当前结点下面，加上这个新的子结点。
            }

            tempNode = node;//当前的结点已经指向下一个结点了

            //如果是最后一个字符的话,应该把这个结点标记为这个敏感词路径的最后一个结点
            if (i == lineTxt.length() - 1) {
                tempNode.setKeywordEnd(true);
            }
        }

    }

    /**
     * 【敏感词过滤】
     *
     * @param text 要过滤的文本
     * @return
     */
    public String filter(String text) {
        if (StringUtils.isBlank(text)) {
            return text;
        }

        StringBuilder result=new StringBuilder();   //替换之后的结果

        String replacement = "***";   //将敏感词替换为 "***"

        //下面是算法用到的三个指针
        TrieNode tempNode = rootNode;
        int begin = 0;
        int position = 0;

        while (position < text.length()) {  //当position走到文件尾时，敏感词过滤结束
            /*每次的position就去问，根结点的子节点中，有没有与之匹配的*/

            char c=text.charAt(position);   //把当前的字符取出来

            tempNode=tempNode.getSubNode(c);    //把当前根结点的子节点中，有没有与当前字符c匹配的

            if(tempNode==null){
                //如果当前结点的子节点中，没有能与当前字符匹配的。说明没有以 begin位置的字符开头的敏感词
                result.append(text.charAt(begin));//【将当前字符加入到 结果字符串中】

                /* 为下一轮匹配敏感词做准备 */
                position=begin+1;   //指向begin后面的一个字符。下一轮从新的begin开始匹配
                begin=position;
                tempNode=rootNode;

            }else if(tempNode.isKeywordEnd()){  //若是是结尾的结点
                //说明找到了一个敏感词
                result.append(replacement); //对敏感词打码，【将敏感词加入result中】

                /* 为下一轮匹配敏感词做准备 */
                position=position+1;
                begin=position;
                tempNode=rootNode;

            }else{
                ++position;     //当前字符匹配成功，先继续往下匹配。 直到匹配到结尾结点，或 没有以 begin位置的字符开头的敏感词。
            }
        }

        // 最终结果，走完了以后，result还要append最后一串。把最后一次处理的加上。
        //最后一段的时候，position到达了文件尾，while循环立即结束。但begin开头的字符串，既没有匹配上敏感词，也没有加入result。所以，我们需要把 最后的 begin位置开头的字符串，添加到结果中。
        result.append(text.substring(begin));
        return result.toString();

    }

    /**
     * 前缀树节点
     */
    private class TrieNode {

        private boolean end = false;//是不是某一个关键词的结尾。

        //当前节点下，所有的子结点。若有ab、ac、ad。那么结点a下面就由b、c、d三个子节点
        private Map<Character, TrieNode> subNodes = new HashMap<Character, TrieNode>();

        //给当前结点，增加一个子节点，放入上面的Map中
        public void addSubNode(Character key, TrieNode node) {
            subNodes.put(key, node);
        }


        //获取当前结点的下一个结点
        TrieNode getSubNode(Character key) {
            return subNodes.get(key);
        }


        //判断是不是结尾
        boolean isKeywordEnd() {
            return end;
        }

        void setKeywordEnd(boolean end) {
            this.end = end;
        }
    }

}
