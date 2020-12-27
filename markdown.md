Markdown 目录：
[TOC]

Markdown 标题：
# 这是 H1
## 这是 H2
### 这是 H3

Markdown 段落没有特殊的格式，直接编写文字就好　　
段落的换行是使用两个以上空格加上回车  
　缩进是使用全角空格

Markdown 在一行中用三个以上的星号、减号、底线来建立一个分隔线，行内不能有其他东西  
***
---
___

Markdown 列表：  
http://www.wowubuntu.com/markdown/#em

无序列表使用星号(*)、加号(+)或是减号(-)作为列表标记，这些标记后面要添加一个空格  
有序列表使用数字并加上 . 号来表示  
列表嵌套只需在子列表中的选项前面添加四个空格  
- 列表项目
1. 列表项目

如果要在列表项目内放进引用，那 > 就需要缩进：
* A list item with a blockquote:  
    > This is a blockquote
    > inside a list item.

如果要放代码区块的话，该区块就需要缩进两次，也就是 8 个空格或是 2 个制表符：
*   一列表项包含一个列表区块：  
    ```python
    #!/usr/bin/python3
    print("Hello, World!");
    ```
1.  This is a list item with two paragraphs.   
    Lorem ipsum dolor sit amet, consectetuer adipiscing elit.   
    Aliquam hendrerit mi posuere lectus.  
    * Vestibulum enim wisi, viverra nec, fringilla in, laoreet
    vitae, risus. Donec sit amet nisl. 
    * Aliquam semper ipsum sit amet velit.
2.  Suspendisse id sem consectetuer libero luctus adipiscing.

*斜体* 或 _斜体_  
**粗体**  
***加粗斜体***  
~~删除线~~  
<font color='yellow'>标红字体 </font>

Markdown 插入链接：
[链接文字](链接网址 "标题")

Markdown 插入图片：
![alt text](https://fanyi.baidu.com/favicon.ico "title")

Markdown 区块引用是在段落开头使用 > 符号 ，然后后面紧跟一个空格符号  
> 区块引用  
> 菜鸟教程  

>>>> 学的不仅是技术更是梦想  

<blockquote>引用区块一</blockquote>

Markdown 插入代码块：
```python
#!/usr/bin/python3
print("Hello, World!");
```  
<code> 
print("Hello, World!");
</code> 

`printf()`

Markdown 换行：
<br>

<!--哈哈我是注释，不会在浏览器中显示。-->

Markdown Table：
Markdown 制作表格使用 | 来分隔不同的单元格，使用 - 来分隔表头和其他行  
| 左对齐    | 右对齐     | 居中对齐   |
| :-        | -:        | :-:       |
| 单        | 元        | 格        |
| 单        | 元        | 格        |


