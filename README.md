<p align="center">
<h1 align="center">📦 ChatGPT</h1>
<div align="center">实现可连续对话ChatGPT插件</div>
</p>

[![English badge](https://img.shields.io/badge/%E8%8B%B1%E6%96%87-English-blue)](./README_en.md)
[![简体中文 badge](https://img.shields.io/badge/%E7%AE%80%E4%BD%93%E4%B8%AD%E6%96%87-Simplified%20Chinese-blue)](./README.md)

> 本文Demo地址：https://github.com/asleepyfish/chatgpt-demo

> 流式输出结合Vue的Demo地址：[https://github.com/asleepyfish/chatgpt-vue](https://github.com/asleepyfish/chatgpt-vue)

**注意：流式输出在2.4节，请仔细阅读到最后，谢谢！**

# 版本更新说明
- 1.1.5 增加查询账单功能`billingUsage`（单位：美元），可以选择传入开始和结束日期查询（最多100天），或者不传入参，此时表示查询所有日期账单。
- 1.1.6 增加自定义OpenAiProxyService功能，支持单个SpringBoot中添加多个OpenAiProxyService实例，每个实例可以拥有个性化的参数；查询账单功能优化。


# 1. 配置阶段
## 1.1 依赖引入
`pom.xml`中引入依赖
- Latest Version: ![Maven Central](https://img.shields.io/maven-central/v/io.github.asleepyfish/chatgpt?color=blue)
- Maven:
```xml
<dependency>
    <groupId>io.github.asleepyfish</groupId>
    <artifactId>chatgpt</artifactId>
    <version>Latest Version</version>
</dependency>
```
## 1.2 配置application.yml文件
在`application.yml`文件中配置chatgpt相关参数（Optional为可选参数）

**注：大陆用户需要配置proxy-host和proxy-port来进行代理才能访问OpenAI服务**


| 参数                               | 解释                                                         |
| ---------------------------------- | ------------------------------------------------------------ |
| token                              | 申请的API KEYS                                               |
| proxy-host (Optional)              | 代理的ip                                                     |
| proxy-port (Optional)              | 代理的端口                                                   |
| model (Optional)                   | model可填可不填，默认即text-davinci-003                      |
| chat-model (Optional)              | 可填可不填，默认即gpt-3.5-turbo （ChatGPT当前最强模型，生成回答使用的就是这个模型） |
| retries (Optional)                 | 指的是当chatgpt第一次请求回答失败时，重新请求的次数（增加该参数的原因是因为大量访问的原因，在某一个时刻，chatgpt服务将处于无法访问的情况，不填的默认值为5） |
| session-expiration-time (Optional) | （单位（min））为这个会话在多久不访问后被销毁，这个值不填的时候，即表示所有问答处于同一个会话之下，相同user的会话永不销毁（增加请求消耗） |

例：
```yml
chatgpt:
  token: sk-xxxxxxxxxxxxxxx
  proxy-host: 127.0.0.1
  proxy-port: xxxx
  session-expiration-time: 10
```
**_其中token必填、大陆用户proxy-host、proxy-port也是必填的（某些你懂的原因）_**

上面的session-expiration-time参数很重要，是用来表示这个会话在多久不访问后被销毁，从而实现联系上下文的连续对话。

实现方式是通过ChatCompletionRequest中的user来区分某个会话，而session-expiration-time表示这个会话在多久不访问后被销毁。

**如果这里看不懂请看2.1节示例**
## 1.3 @EnableChatGPT注解
启动类上加入`@EnableChatGPT`注解则将ChatGPT服务注入到Spring中。
![在这里插入图片描述](https://img-blog.csdnimg.cn/2723c69669244b5da07c0752b0585945.png)

提供工具类 `OpenAiUtils`，它提供了相关的调用方法。
使用它的最简单方法是：

# 2 使用

## 2.1 生成回答
提供了工具类`OpenAiUtils`，里面提供了相关方法进行调用。
其中最简单的使用方法是：
```java
OpenAiUtils.createChatCompletion(content);// 不建议使用
```
入参`content`即输入的问题的字符串。但是不建议使用。

这里建议使用下面的方式，通过传入user的值，再结合`session-expiration-time`参数，可以实现指定某次会话，或者某个用户的连续对话。
```java
OpenAiUtils.createChatCompletion(content, user);// 建议使用
```

还提供一个通用的静态方法是
```java
public static List<String> createChatCompletion(ChatCompletionRequest chatCompletionRequest) {...}
```
入参`ChatCompletionRequest `里包含模型的一些可调参数。

`OpenAiUtils`类中还提供了多个可供选择的静态方法，可以自行查看。

上述方法的返回参数是一个list，是因为调整参数返回答案`n`可以一次性返回多条不同的解答（`n`为`ChatCompletionRequest`类中一个参数）。
### 2.1.1 测试
测试代码：
```java
@PostMapping("/chatTest")
public List<String> chatTest(String content) {
    return OpenAiUtils.createChatCompletion(content, "testUser");
}
```
Post请求
> 入参输入：Java序列化的方式

ChatGPT返回结果：
```text
[
  "\n\nJava序列化是将Java对象转换为字节序列的过程，以便在网络上传输或将其保存到磁盘上。Java提供了两种序列化方式：\n\n1. 基于Serializable接口的序列化\n\nSerializable接口是Java提供的一个标记接口，用于标记一个类可以被序列化。如果一个类实现了Serializable接口，那么它的所有非瞬态字段都会被序列化。序列化的过程可以通过ObjectOutputStream类来实现，反序列化的过程可以通过ObjectInputStream类来实现。\n\n2. 基于Externalizable接口的序列化\n\nExternalizable接口也是Java提供的一个标记接口，用于标记一个类可以被序列化。与Serializable接口不同的是，Externalizable接口需要实现writeExternal和readExternal方法，这两个方法分别用于序列化和反序列化。在序列化的过程中，只有被writeExternal方法显式写入的字段才会被序列化，而在反序列化的过程中，只有被readExternal方法显式读取的字段才会被反序列化。\n\n总的来说，基于Serializable接口的序列化更加简单，但是它会序列化所有非瞬态字段，包括一些不需要序列化的字段，而基于Externalizable接口的序列化可以更加灵活地控制序列化的过程。"
]
```
> 再次输入：有没有更加高效的序列化框架

ChatGPT返回结果：
```text
[
  "是的，Java中有很多高效的序列化框架，以下是一些常用的序列化框架：\n\n1. Protobuf\n\nProtobuf是Google开发的一种高效的序列化框架，它可以将结构化数据序列化为二进制格式，支持多种编程语言。相比于Java自带的序列化方式，Protobuf序列化后的数据更小，解析速度更快。\n\n2. Kryo\n\nKryo是一个快速、高效的Java序列化框架，它可以将Java对象序列化为字节数组，支持多种数据类型。Kryo序列化的速度比Java自带的序列化方式快很多，序列化后的数据也更小。\n\n3. FST\n\nFST是一个高性能的Java序列化框架，它可以将Java对象序列化为字节数组，支持多种数据类型。FST序列化的速度比Java自带的序列化方式快很多，序列化后的数据也更小。\n\n4. Avro\n\nAvro是一个高效的数据序列化系统，它可以将结构化数据序列化为二进制格式，支持多种编程语言。Avro序列化后的数据比Java自带的序列化方式更小，解析速度也更快。\n\n总的来说，这些高效的序列化框架都比Java自带的序列化方式更快、更小、更灵活，可以根据具体的需求选择合适的框架。"
]
```
可以看出上述两次问答是在一次会话中的，而前面所说的参数`session-expiration-time`即这个user所代表的会话多久没被继续访问时的销毁时间。单位（min）
## 2.2 生成图片
最简单的使用方式是
```java
OpenAiUtils.createImage(prompt);
```
入参表示生成图片的描述文字，还提供了一个通用的静态方法
```java
public static List<String> createImage(CreateImageRequest createImageRequest) {...}
```
入参`CreateImageRequest`中有一些可以使用的参数，其中`n`表示生成图片的数量，`responseFormat`表示生成图片的格式，格式中分为`url`和`b64_json`两种，如果希望返回的是url，则返回的url会在生成一个小时后消失，默认值是`url`。
### 2.2.1 测试
测试代码
```java
    @Test
    public void testGenerateImg() {
        OpenAiUtils.createImage("英短").forEach(System.out::println);
    }
```
结果
默认情况下会生成一个url，点击去就可以看到图片。
![在这里插入图片描述](https://img-blog.csdnimg.cn/41002f914607488cb1ad830f70eb492c.png)


## 2.3 下载图片
在3.2的基础上做了优化，直接使用`responseFormat`为`b64_json`然后解析成图片返回。简单使用方式如下：
```java
OpenAiUtils.downloadImage(prompt, response);
```
通用方式如下：
```java
public static void downloadImage(CreateImageRequest createImageRequest, HttpServletResponse response) {...}
```
当`CreateImageRequest`对象中设置的返回参数`n`大于1时，会将图片打包成一个zip包返回，当`n`等于1时直接返回图片。
### 2.3.1 测试
测试代码
```java
@RestController
public class ChatGPTController {
    @GetMapping("/downloadImage")
    public void downloadImage(String prompt, HttpServletResponse response) {
        OpenAiUtils.downloadImage(prompt, response);
    }
}
```
发送get请求，然后选择Send and Download
![在这里插入图片描述](https://img-blog.csdnimg.cn/9e2c00a9fc384d5ca191dbd5b2d04d64.png)

**我用的get 工具是idea里面下载的插件Fast Request的，用Postman也是可以的，但是要选择 Send and Download，上图中绿色的箭头是Send，蓝色的是Send and Download。**

![在这里插入图片描述](https://img-blog.csdnimg.cn/333b38ec121d463db78031236e0664de.png)

## 2.4 生成流式回答
生成流式回答的方法是`OpenAiUtils`的`createStreamChatCompletion`方法，本工具类重载了同名的多个参数的方法，其中最通用的方法是
```java
public static void createStreamChatCompletion(ChatCompletionRequest chatCompletionRequest, OutputStream os) {...}
```
最简单的方法是
```java
public static void createStreamChatCompletion(String content) {...}
```
其中的content即本次对话的问题。

这里需要主义的是，上述第一个方法中的`OutputStream os`其实是一个必传的对象，上述的最简单的方法实际上是默认传递的`System.out`这个`os`对象，也就是将流式问答的结果显示到IDEA的控制台。

如果需要将流式问答的结果显示到其他界面可以自发的传入`OutputStream os`对象，这里有一个简便的方法是
```java
public static void createStreamChatCompletion(String content, OutputStream os) {...}
```
只需要输入问题，和输出流对象即可。

下面将举例具体说明。（本文所有Demo的示例地址： [https://github.com/asleepyfish/chatgpt-demo](https://github.com/asleepyfish/chatgpt-demo)）
### 2.4.1 流式回答输出到IDEA控制台
代码如下：
```java
@GetMapping("/streamChat")
public void streamChat(String content) {
    // OpenAiUtils.createStreamChatCompletion(content, System.out);
    // 下面的默认和上面这句代码一样，是输出结果到控制台
    OpenAiUtils.createStreamChatCompletion(content);
}
```
然后使用Postman或者其他可以发送Get请求的工具发送请求。

本次测试的结果如下面的Gif图所示

![在这里插入图片描述](https://img-blog.csdnimg.cn/f951b9c30ecc457db467185608faecf2.gif)
### 2.4.2 流式回答输出到浏览器页面
上述的方法中输出流传入的是`System.out`对象，该对象实际上就是一个`PrintStream`对象，会把输出结果展示到控制台。

如果需要将输出结果在浏览器展示，可以从前端传入一个`HttpServletResponse response`对象，拿到这个`response`以后将`response.getOutputStream()`这个输出流对象传入`createStreamChatCompletion`方法的入参中。同时，为了避免结果输出到浏览器产生乱码和支持流式输出，需要`ContentType`和`CharacterEncoding`。

具体代码如下：
```java
@GetMapping("/streamChatWithWeb")
public void streamChatWithWeb(String content, HttpServletResponse response) throws IOException {
    // 需要指定response的ContentType为流式输出，且字符编码为UTF-8
    response.setContentType("text/event-stream");
    response.setCharacterEncoding("UTF-8");
    OpenAiUtils.createStreamChatCompletion(content, response.getOutputStream());
}
```
测试结果过程的Gif图如下所示：

![在这里插入图片描述](https://img-blog.csdnimg.cn/632cdcc9d59640caa388eefc00fe95b3.gif)

### 2.4.3 流式回答结合Vue输出到前端界面
调用的后端方法同`2.4.2`节方法`streamChatWithWeb`，前端只需要在界面传入问题，点击提问按钮即可返回结果流式输出到文本框中。

测试结果过程的Gif图如下所示：

![在这里插入图片描述](https://img-blog.csdnimg.cn/9f4b704876da4004ab0bd576bd951621.gif)
`Vue3` Demo的`Git`地址在文章开头有~