<p align="center">
<h1 align="center">📦 ChatGPT</h1>
<div align="center">a small plugin that calls chatgpt </div>
</p>

[![English badge](https://img.shields.io/badge/%E8%8B%B1%E6%96%87-English-blue)](./README_en.md)
[![简体中文 badge](https://img.shields.io/badge/%E7%AE%80%E4%BD%93%E4%B8%AD%E6%96%87-Simplified%20Chinese-blue)](./README.md)

# 1. 配置阶段
## 1.1 依赖引入
`pom.xml`中引入依赖
```xml
        <dependency>
            <groupId>io.github.asleepyfish</groupId>
            <artifactId>chatgpt</artifactId>
            <version>1.0.6</version>
        </dependency>
```
## 1.2 配置application.yml文件
在`application.yml`文件中配置chatgpt相关参数
```yml
chatgpt:
  model: text-davinci-003
  token: sk-xxxxxxxxxxxxxxxxxxx
  retries: 10
```
这里的model是选择chatgpt哪个模型，默认填好的是最优的模型了，token就是上面申请的API KEYS，retries指的是当chatgpt第一次请求回答失败时，重新请求的次数（增加该参数的原因是因为大量访问的原因，在某一个时刻，chatgpt服务将处于无法访问的情况）

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
OpenAiUtils.createCompletion(prompt);
```
入参`prompt`即输入的问题的字符串。

还提供一个通用的静态方法是
```java
public static List<String> createCompletion(CompletionRequest completionRequest) {...}
```
入参`CompletionRequest `里包含模型的一些可调参数。

`OpenAiUtils`类中还提供了多个可供选择的静态方法，可以自行查看。

上述方法的返回参数是一个list，是因为调整参数返回答案`n`可以一次性返回多条不同的解答（`n`为`CompletionRequest`类中一个参数）。
### 2.1.1 测试
测试代码：
```java
@SpringBootTest
public class SpringTest {
    @Test
    public void chatGPTTest() {
        OpenAiUtils.createCompletion("use c++ write QuickSort").forEach(System.out::println);
    }
}
```
ChatGPT输出结果：
```c
#include <iostream> 
using namespace std; 

// A utility function to swap two elements 
void swap(int* a, int* b) 
{ 
	int t = *a; 
	*a = *b; 
	*b = t; 
} 

/* This function takes last element as pivot, places 
the pivot element at its correct position in sorted 
array, and places all smaller (smaller than pivot) 
to left of pivot and all greater elements to right 
of pivot */
int partition (int arr[], int low, int high) 
{ 
	int pivot = arr[high]; // pivot 
	int i = (low - 1); // Index of smaller element 

	for (int j = low; j <= high - 1; j++) 
	{ 
		// If current element is smaller than the pivot 
		if (arr[j] < pivot) 
		{ 
			i++; // increment index of smaller element 
			swap(&arr[i], &arr[j]); 
		} 
	} 
	swap(&arr[i + 1], &arr[high]); 
	return (i + 1); 
} 

/* The main function that implements QuickSort 
arr[] --> Array to be sorted, 
low --> Starting index, 
high --> Ending index */
void quickSort(int arr[], int low, int high) 
{ 
	if (low < high) 
	{ 
		/* pi is partitioning index, arr[p] is now 
		at right place */
		int pi = partition(arr, low, high); 

		// Separately sort elements before 
		// partition and after partition 
		quickSort(arr, low, pi - 1); 
		quickSort(arr, pi + 1, high); 
	} 
} 

/* Function to print an array */
void printArray(int arr[], int size) 
{ 
	int i; 
	for (i = 0; i < size; i++) 
		cout << arr[i] << " "; 
	cout << endl; 
} 

// Driver Code 
int main() 
{ 
	int arr[] = {10, 7, 8, 9, 1, 5}; 
	int n = sizeof(arr) / sizeof(arr[0]); 
	quickSort(arr, 0, n - 1); 
	cout << "Sorted array: " << endl; 
	printArray(arr, n); 
	return 0; 
}
```
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