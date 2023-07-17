<p align="center">
<h1 align="center">📦 ChatGPT</h1>
<div align="center">实现可连续对话ChatGPT插件</div>
</p>


[![English badge](https://img.shields.io/badge/%E8%8B%B1%E6%96%87-English-blue)](./README_en.md)
[![简体中文 badge](https://img.shields.io/badge/%E7%AE%80%E4%BD%93%E4%B8%AD%E6%96%87-Simplified%20Chinese-blue)](./README.md)

> 源码及更详细的介绍说明参见Git上的ReadME.md文档
> [https://github.com/asleepyfish/chatgpt](https://github.com/asleepyfish/chatgpt)

> 本文结合SpringBoot的Demo地址：[https://github.com/asleepyfish/chatgpt-demo](https://github.com/asleepyfish/chatgpt-demo)

> 流式输出结合Vue的Demo地址：[https://github.com/asleepyfish/chatgpt-vue](https://github.com/asleepyfish/chatgpt-vue)

==2023-05-28更新==

> 制作了一个基于本文sdk包的对话网站，可前往[http://chatgpt.alpacos.cn/](http://chatgpt.alpacos.cn/)体验。
> **[注]：** 由于本人的服务器的带宽较低，首次加载页面可能会略有些慢，请耐心等待，详细使用说明请查看另一篇博客~ 👇
> [ChatGPT网页版（基于SpringBoot和Vue）](https://blog.csdn.net/qq_41821963/article/details/130918024?spm=1001.2014.3001.5502)

==2023-07-17更新==

> 所有的测试Demo均包括结合Spring框架的（`ChatGPTController`类中）和`main`方法使用（`MainTest`类中）
>
> Demo地址：https://github.com/asleepyfish/chatgpt-demo

**注意：** 流式输出在2.4节，请仔细阅读到最后。

# 版本更新说明

- 1.1.5 增加查询账单功能`billingUsage`（单位：美元），可以选择传入开始和结束日期查询（最多100天），或者不传入参，此时表示查询所有日期账单。
- 1.1.6 增加自定义`OpenAiProxyService`功能，支持单个SpringBoot中添加多个`OpenAiProxyService`实例，每个实例可以拥有个性化的参数；查询账单功能优化。
- 1.2.0 增加`subscription`方法（查询订阅信息，包括订阅到期日和账号额度等信息，但是没有使用量情况，使用通过`billingUsage`方法查询使用量），增加`billing`方法，整合了`subscription`和`billingUsage`方法，出参包括订阅到期日、额度、使用量、余量等信息。增加对内部cache的多种操作，包括获取，赋值等操作。
- 1.2.1 `billing`方法中出参`dueDate`取值逻辑修改，`ChatGPTProperties`类支持build链式创建对象。
- 1.3.0 新增以下方法，每种方法均包含多种重载方法，具体使用请参考：[https://github.com/asleepyfish/chatgpt-demo](https://github.com/asleepyfish/chatgpt-demo)
    - `listModels`、`getModel`
    - `edit`
    - `embeddings`
    - `transcription`、`translation`
    - `createImageEdit`、`createImageVariation`
    - `listFiles`、`uploadFile`、`deleteFile`、`retrieveFile`、`retrieveFileContent`
    - `createFineTune`、`createFineTuneCompletion`、`listFineTunes`、`retrieveFineTune`、`cancelFineTune`、`listFineTuneEvents`、`deleteFineTune`
    - `createModeration`
- 1.3.1 支持自定义baseUrl，默认为 `https://api.openai.com/` ，配置参数在`ChatGPTProperties`类中，可通过`application.yml`配置。


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
| base-url (Optional)                | 默认为https://api.openai.com/，可不填                        |

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

## 2.5 查询账单和订阅

查询账单提供了两个方法，金额单位均为`美元(USD)`，且均未对小数位截取，可以根据需要自行选择保留结果小数点位数。

第一个是可以传入开始和结束日期，按照指定日期区间查询的方法：

```java
public String billingUsage(String startDate, String endDate) {...}
```

其中`startDate`和`endDate`区间范围不超过100天。

第二个方法是一个入参为可变参数的方法，当不传入参时，查询从`2023年1月1日`距今的账单的方法，如果有人的订阅日早于`2023年1月1日`可以传入自定义账单起始日期：

```java
public String billingUsage(String... startDate) {...}
```

查询订阅提供了一个方法，这个方法的出参中包括了订阅到期日，总额度等信息：

```java
public Subscription subscription() {...}
```

**由于查询总额度和查询使用量是两个接口，这里封装了一个方法来将几个比较有用的参数统一返回的方法，方法如下：**

```java
public Billing billing(String... startDate) {...}
```

这个方法的入参也是一个可变入参，不传参时，`startDate`默认为`2023-01-01`，如果账单开始日早于该天，可以传入指定的`startDate`。出参`Billing`中有四个参数：`dueDate`（额度到期日），`total`（额度总量），`usage`（已使用量），`balance`（余额）。

### 2.5.1 测试

测试代码如下：

```java
@GetMapping("/billing")
public void billing() {
    String monthUsage = OpenAiUtils.billingUsage("2023-04-01", "2023-05-01");
    log.info("四月使用：{}美元", monthUsage);
    String totalUsage = OpenAiUtils.billingUsage();
    log.info("一共使用：{}美元", totalUsage);
    String stageUsage = OpenAiUtils.billingUsage("2023-01-31");
    log.info("自从2023/01/31使用：{}美元", stageUsage);
    Subscription subscription = OpenAiUtils.subscription();
    log.info("订阅信息（包含到期日期，账户总额度等信息）：{}", subscription);
    // dueDate为到期日，total为总额度，usage为使用量，balance为余额
    Billing totalBilling = OpenAiUtils.billing();
    log.info("历史账单信息：{}", totalBilling);
    // 默认不传参的billing方法的使用量usage从2023-01-01开始，如果用户的账单使用早于该日期，可以传入开始日期startDate
    Billing posibleStartBilling = OpenAiUtils.billing("2022-01-01");
    log.info("可能的历史账单信息：{}", posibleStartBilling);
}
```

测试结果如下：

```txt
四月使用：0.9864320000000001美元
一共使用：2.2074280000000002美元
自从2023/01/31使用：2.2074280000000001美元
订阅信息（包含到期日期，账户总额度等信息）：Subscription(object=billing_subscription, hasPaymentMethod=false, canceled=false, canceledAt=null, delinquent=null, accessUntil=1688169600, softLimit=66667, hardLimit=83334, systemHardLimit=83334, softLimitUsd=4.00002, hardLimitUsd=5.00004, systemHardLimitUsd=5.00004, plan=Plan(title=Explore, id=free), accountName=Leo Mikey, poNumber=0, billingEmail=null, taxIds=null, billingAddress=null, businessAddress=null)
历史账单信息：Billing(dueDate=2023-07-01, total=5.00004, usage=2.2074280000000002, balance=2.7926119999999998)
可能的历史账单信息：Billing(dueDate=2023-07-01, total=5.00004, usage=2.2074280000000002, balance=2.7926119999999998)
```

## 2.6 自定义baseUrl

由于部分原因，用户可以选择搭建好的代理服务作为baseUrl，而不需要使用官方的`https://api.openai.com/`。

如果用户基于SpringBoot使用自定义baseUrl，可以参考下面的`application.yml`，配置chatgpt.base-url，如果不配置，则表示默认使用官方的`https://api.openai.com/`。

```yml
chatgpt:
  token: sk-xxxxxxxxxxxxxxxxxx
  proxy-host: 127.0.0.1 #可选
  proxy-port: 7890 #可选
  model: text-davinci-003 #可选
  chat-model: gpt-3.5-turbo #可选
  retries: 10 #可选，默认为5
  session-expiration-time: 30 #可选，不填则会话永不过期
  base-url: https://openai.api2d.net/ #可选，默认为https://api.openai.com/
```

然后相关调用代码如下：

```java
@PostMapping("/baseUrl")
public void baseUrl() {
    // 先在application.yml中配置chatgpt.base-url
    System.out.println("models列表：" + OpenAiUtils.listModels());
}
```

如果用户不想和Spring集成，则可以使用main方法调用的方式，如下所示：

```java
public static void main(String[] args) {
    ChatGPTProperties properties = ChatGPTProperties.builder().token("sk-xxx")
        .proxyHost("127.0.0.1")
        .proxyPort(7890)
        // 自定义baseUrl，若不需要自定义baseUrl，下一行可以去掉
        .baseUrl("https://openai.api2d.net/")
        .build();
    OpenAiProxyService openAiProxyService = new OpenAiProxyService(properties);
    System.out.println("models列表：" + openAiProxyService.listModels());
}
```

其中自定义了baseUrl为 `https://openai.api2d.net/` ，并调用了`listModels`方法。

## 2.7 模型（model）

提供了两个与模型相关的方法：

```java
// 列出全部models
List<Model> listModels() {...}
```

```java
// 获取指定model信息
Model getModel(String model) {...}
```

测试代码如下（下面为了测试方便性，均以`main`方法做示例，结合SpringBoot使用`OpenAiUtils`调用的方法请参考github上的demo示例）：

```java
ChatGPTProperties properties = ChatGPTProperties.builder().token("sk-xxx")
    .proxyHost("127.0.0.1")
    .proxyPort(7890)
    .build();
OpenAiProxyService openAiProxyService = new OpenAiProxyService(properties);
System.out.println("models列表：" + openAiProxyService.listModels());
System.out.println("=============================================");
System.out.println("text-davinci-003信息：" + openAiProxyService.getModel("text-davinci-003"));
```

测试结果（截取如下）：

```txt
models列表：[Model(id=whisper-1, object=model, ownedBy=openai-internal, permission=[Permission(id=modelperm-KlsZlfft3Gma8pI6A8rTnyjs,.....
=============================================
text-davinci-003信息：Model(id=text-davinci-003, object=model, ownedBy=openai-internal, permission=[Permission(id=modelperm-jepinXYt59ncUQrjQEIUEDyC, object=model_permission, created=1688551385, allowCreateEngine=false, allowSampling=true, allowLogProbs=false, allowSearchIndices=false, allowView=true, allowFineTuning=false, organization=*, group=null, isBlocking=false)], root=text-davinci-003, parent=null)

```

## 2.8 编辑（edit）

edit功能可以用来检查语句的拼写错误，代码的正确性等，api中重载了多种edit相关的方法，这里介绍最简单的一种，其他重载方法，可通过源码查看。

```java
String edit(String input, String instruction) {...}
```

测试代码如下：

```java
ChatGPTProperties properties = ChatGPTProperties.builder().token("sk-xxx")
    .proxyHost("127.0.0.1")
    .proxyPort(7890)
    .build();
OpenAiProxyService openAiProxyService = new OpenAiProxyService(properties);
String input = "What day of the wek is it?";
String instruction = "Fix the spelling mistakes";
System.out.println("编辑前：" + input);
// 下面这句和openAiProxyService.edit(input, instruction, EditModelEnum.TEXT_DAVINCI_EDIT_001);是一样的，默认使用模型TEXT_DAVINCI_EDIT_001
System.out.println("编辑后：" + openAiProxyService.edit(input, instruction));
System.out.println("=============================================");
input = "    public static void mian([String] args) {\n" +
    "        system.in.println(\"hello world\");\n" +
    "    }";
instruction = "Fix the code mistakes";
System.out.println("修正代码前：\n" + input);
System.out.println("修正代码后：\n" + openAiProxyService.edit(input, instruction, EditModelEnum.CODE_DAVINCI_EDIT_001));
```

测试结果如下：

```txt
编辑前：What day of the wek is it?
编辑后：What day of the week is it today?

=============================================
修正代码前：
    public static void mian([String] args) {
        system.in.println("hello world");
    }
修正代码后：
    public static void main(String[] args) {
        System.out.println("hello world");
    }
```

## 2.9 向量（embeddings）

有时候需要将文本内容转化成计算机可以识别到的向量形式，可以使用这个方法。api中重载了多种embeddings相关的方法，这里介绍最简单的一种，其他重载方法，可通过源码查看。

```java
List<Double> embeddings(String text) {...}
```

测试代码如下：

```java
ChatGPTProperties properties = ChatGPTProperties.builder().token("sk-xxx")
    .proxyHost("127.0.0.1")
    .proxyPort(7890)
    .build();
OpenAiProxyService openAiProxyService = new OpenAiProxyService(properties);
// 单文本
String text = "Once upon a time";
System.out.println("文本：" + text);
System.out.println("文本的嵌入向量：" + openAiProxyService.embeddings(text));
System.out.println("=============================================");
// 文本数组
String[] texts = {"Once upon a time", "There was a princess"};
System.out.println("文本数组：" + Arrays.toString(texts));
EmbeddingRequest embeddingRequest = EmbeddingRequest.builder()
    .model(EmbeddingModelEnum.TEXT_EMBEDDING_ADA_002.getModelName()).input(Arrays.asList(texts)).build();
System.out.println("文本数组的嵌入向量：" + openAiProxyService.embeddings(embeddingRequest));
```

测试结果截取如下：

```txt
文本：Once upon a time
文本的嵌入向量：[0.02122687, -0.02013961, -0.004614537, -0.011599556, -0.011213957, 0.0019817224, 0.0023404553, ...
文本数组：[Once upon a time, There was a princess]
文本数组的嵌入向量：EmbeddingResult(model=text-embedding-ada-002-v2, object=list, data=[Embedding(object=embedding, embedding=[0.02122687, -0.02013961, -0.004614537, -0.011599556, -0.011213957, 0.0019817224...
```

## 2.10 转录（transcription）

转录功能可以将诸如`mp3`、`mp4`等格式的音频文件转化成文字输出。

最简单的一种使用方法如下，其他重载方法可通过代码查看。

```java
String transcription(String filePath, AudioResponseFormatEnum audioResponseFormatEnum) {...}
```

下面是一首许嵩的《想象之中》的转录测试。

```java
ChatGPTProperties properties = ChatGPTProperties.builder().token("sk-xxx")
    .proxyHost("127.0.0.1")
    .proxyPort(7890)
    .build();
OpenAiProxyService openAiProxyService = new OpenAiProxyService(properties);
String filePath = "src/main/resources/audio/想象之中-许嵩.mp3";
System.out.println("语音文件转录后的json文本是：" + openAiProxyService.transcription(filePath, AudioResponseFormatEnum.JSON));
// File file = new File("src/main/resources/audio/想象之中-许嵩.mp3");
// System.out.println("语音文件转录后的json文本是：" + openAiProxyService.transcription(file, AudioResponseFormatEnum.JSON));
    
```

测试结果截取如下：

```txt
语音文件转录后的json文本是：{"text":"想像之中 想像之中 雨過一道彩虹 抬起了頭 瑟瑟灰色天空 想像之中 付出會有結果 毫無保留 信奉你的承諾 想像之中 這次要愛很久 我領略過 你眼裡的溫柔 熱戀以後 你忽然的冰凍 但若兩人丟給我去承受 想像中 很不同 想像中 一切都和後來不同 我承認 曾經那麼心動 你沒想像中那麼戀舊 回憶換不回你的溫柔 最後也不是故作冷漠 轉過頭 我怎麼又一滴淚落 我沒想像中那麼脆弱 分開後 形容也沒消瘦 一起踏過了幾座春秋 過了愛不是追逐佔有 想像之中 這次要愛很久 我領略過 你眼裡的溫柔 熱戀以後 你忽然的冰凍 但若兩人丟給我去承受 想像中 很不同 想像中 一切都和後來不同 我承認 曾經那麼心動 你沒想像中那麼戀舊 回憶換不回你的溫柔 最後也不是故作冷漠 轉過頭 我怎麼又一滴淚落 我沒想像中那麼脆弱 分開後 形容也沒消瘦 一起踏過了幾座春秋 過了愛不是追逐佔有 你沒想像中那麼戀舊 回憶換不回你的溫柔 最後也不是故作冷漠 轉過頭 我怎麼又一滴淚落 我沒想像中那麼脆弱 分開後 形容也沒消瘦 一起踏過了幾座春秋 領悟了愛不是追逐佔有"}
```

上述文字输出的时候是繁体，具体使用可以使用重载方法中一个代码`TranscriptionRequest`入参的方法，其中的`language`可以指定返回的语言，具体使用可以前往官网查看。

## 2.11 翻译（translation）

翻译功能可以将任意的音频文件翻译成英文。

最简单的一种使用方法如下，其他重载方法可通过代码查看。

```java
String translation(String filePath, AudioResponseFormatEnum audioResponseFormatEnum) {...}
```

下面是一首许嵩的《想象之中》的翻译测试。

```java
ChatGPTProperties properties = ChatGPTProperties.builder().token("sk-xxx")
    .proxyHost("127.0.0.1")
    .proxyPort(7890)
    .build();
OpenAiProxyService openAiProxyService = new OpenAiProxyService(properties);
String filePath = "src/main/resources/audio/想象之中-许嵩.mp3";
System.out.println("语音文件翻译成英文后的json文本是：" + openAiProxyService.translation(filePath, AudioResponseFormatEnum.JSON));
// File file = new File("src/main/resources/audio/想象之中-许嵩.mp3");
// System.out.println("语音文件翻译成英文后的json文本是：" + openAiProxyService.translation(file, AudioResponseFormatEnum.JSON));
    
```

测试结果截取如下：

```txt
语音文件翻译成英文后的json文本是：{"text":"Translated by Hua Chenyu English Subs In my imagination, there will be results Without keeping your promise In my imagination, this time I will love for a long time I have experienced the tenderness in your eyes After falling in love, you suddenly freeze But if two people are thrown to me to bear In my imagination, it's very different In my imagination, everything is different from the later I admit that I used to be so heartbroken You are not as long-lasting as I imagined Memories can't exchange your tenderness In the end, it's not lonely and lonely Why am I crying again? ..."}
```

# 3. 扩展

## 3.1 自定义OpenAiProxyService

由于之前的版本中使用@Bean的方式初始化`OpenAiProxyService`和`OpenAiUtils`，导致一个SpringBoot中实例是唯一的。

但是有时候需要在项目里自定义多个`OpenAiProxyService`实例，来装配不同的`ChatGPTProperties`信息（可以实例化多个Token（sk-xxxxxxxxxxx）使用）。

所以在`1.1.6`版本中新增了自定义`OpenAiProxyService`功能。在维持原有SpringBoot项目中全局的一个`OpenAiUtils`实例的基础上，现在可以自定义不同的`OpenAiProxyService`实例，并且实例之间的属性是完全隔离的。

下面是一个Demo用来展示使用方法。

```java
@GetMapping("/customToken")
public void customToken() {
	ChatGPTProperties chatGPTProperties = new ChatGPTProperties();
	chatGPTProperties.setToken("sk-002xxxxxxxxxxxxxxxxxxxxxxxxx");
	chatGPTProperties.setProxyHost("127.0.0.1");
	chatGPTProperties.setProxyPort(7890);
	OpenAiProxyService openAiProxyService = new OpenAiProxyService(chatGPTProperties, Duration.ZERO);
	// 直接使用new出来的openAiProxyService来调用方法，每个OpenAiProxyService都拥有自己的Token。
	// 这样在一个SpringBoot项目中，就可以有多个Token，可以有更多的免费额度供使用了
	openAiProxyService.createStreamChatCompletion("Java的三大特性是什么");
}
```

在上述方法中，新new了一个`ChatGPTProperties`对象，并且set了`token`为`sk-002xxxxxxxxxxxxxxxxxxxxxxxxx`（这里不需要设置除了`token`、`proxyHost`和`proxyPort`以外的其他属性，因为`ChatGPTProperties`的其他属性拥有默认值，如果需要对其他属性做修改，可以自行设置。**注意：sessionExpirationTime没有默认值，表示会话没有过期时间，如果需要设置会话过期时间，请set该值。**）

而在`application.yml`中设置的`token`为`sk-001xxxxxxxxxxxxxxxxxxxxxxxxx`，这个token是给全局唯一的`OpenAitils`用的，这样就可以通过`OpenAiProxyService`的构造方法new出来一个新的`OpenAiProxyService`实例，其中构造方法的第二个参数直接填`Duration.ZERO`就好，表示Http调用请求没有超时时间，后续版本更新中，我会新增一个只有一个入参的构造方法。

这样直接使用new出来的`openAiProxyService`来调用方法，每个`OpenAiProxyService`都拥有自己的Token。

在一个SpringBoot项目中，就可以有多个`Token`，可以有更多的免费额度供使用了。