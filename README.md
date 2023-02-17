<p align="center">
<h1 align="center">📦 ChatGPT</h1>
<div align="center">a small plugin that calls chatgpt </div>
</p>

#1. Create Completion
Provides the tool class `OpenAiUtils`, which provides related methods for calling.
The simplest way to use it is:

```java
OpenAiUtils.createCompletion(prompt);
```
The input parameter `prompt` is the string of the input question.

A common static method is also provided.
```java
public static List<String> createCompletion(CompletionRequest completionRequest) {...}
```
The input parameter `CompletionRequest` contains some adjustable parameters of the model.

There are also multiple static methods to choose from in the `OpenAiUtils` class, which can be viewed by yourself.

The return parameter of the above method is a list because the adjustment parameter returns the answer `n` to return multiple different answers at once (`n` is a parameter in the `CompletionRequest` class).

#2. Create Image
You can simply use the following code to generate some images
```java
OpenAiUtils.createImage(prompt);
```
As with `createCompletion`, some static methods are provided, which you can consult.
A common static method is also provided.
```java
public static List<String> createImage(CreateImageRequest createImageRequest) {...}
```
#3. Download Image
use this method you can directly download the image that you describe
```java
OpenAiUtils.downloadImage(prompt, response);
```
A common static method is also provided.
```java
public static void downloadImage(CreateImageRequest createImageRequest, HttpServletResponse response) {...}
```