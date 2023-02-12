<p align="center">
<h1 align="center">📦 ChatGPT</h1>
<div align="center">a small plugin that calls chatgpt </div>
</p>

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